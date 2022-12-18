# -*- coding: utf-8 -*-

import sys
import os
sys.path.append(os.path.join(os.path.dirname(__file__), '../..'))
from models.linearmodel import LinearModel
from algorithms.DBGD.tddbgd import TD_DBGD
import numpy as np
from sys import maxint
import copy
from scipy.spatial.distance import cosine
import utils.rankings as rnk
# Dueling Bandit Gradient Descent
class TD_NSGD(TD_DBGD):

    def __init__(self, n_candidates, GRAD_SIZE, EXP_SIZE, TB_QUEUE_SIZE=None, TB_WINDOW_SIZE=None, *args, **kargs):
        super(TD_NSGD, self).__init__(*args, **kargs)
        self.model = LinearModel(n_features = self.n_features,
                                 learning_rate = self.learning_rate,
                                 n_candidates = n_candidates)
        self.GRAD_SIZE = GRAD_SIZE
        self.EXP_SIZE = EXP_SIZE
        self.TB_QUEUE_SIZE = TB_QUEUE_SIZE
        self.TB_WINDOW_SIZE = TB_WINDOW_SIZE
        self.sample_basis = True
        self.clicklist = np.empty([self.GRAD_SIZE,1], dtype=int) #click array
        self.grad = np.zeros([self.GRAD_SIZE,self.n_features], dtype=float)
        self.gradCol = 0

        # DQ tie-break related lists
        self.difficult_NDCG =[]
        self.difficult_queries =[]
        self.difficult_document =[]
        self.difficult_time =[]
        self.query_id = 0

    @staticmethod
    def default_parameters():
        parent_parameters = TD_DBGD.default_parameters()
        parent_parameters.update({
          'n_candidates': 9,
          })
        return parent_parameters

    def update_to_interaction(self, clicks, stop_index=None):
        winners, ranker_clicks = self.multileaving.winning_rankers_with_clicks(clicks)

        # Fill out recent difficult query queues.
        if self.TB_QUEUE_SIZE > 0:   
            self.fill_difficult_query(clicks)
        # Trigger difficult-query tie-break strategy
        if len(self.difficult_queries) < self.TB_QUEUE_SIZE and len(winners) > 1:
            winners = self.tieBreak_difficultQuery(winners)

        self.model.update_to_mean_winners(winners)

        cl_sorted = sorted(ranker_clicks) # in ascending order
        for i in range(1, len(ranker_clicks)):
            # only save subset of rankers (worst 4 ouf of 9 rankers)
            # add if current cl is smaller than or equal to maximum form the set of candidates
            if ranker_clicks[i] <= cl_sorted[3] and ranker_clicks[i]<ranker_clicks[0]:
                self.clicklist[self.gradCol] = ranker_clicks[i] -ranker_clicks[0]
                self.grad[self.gradCol] = self.model.gs[i-1]
                self.gradCol = (self.gradCol + 1) % self.GRAD_SIZE # update to reflect next column to be updaed



    def _create_train_ranking(self, query_id, query_feat, inverted):
        self.query_id = query_id
        assert inverted == False
        #  Get the worst gradients by click
        nums = []
        dif = self.GRAD_SIZE - self.EXP_SIZE
        for i in range(0, dif):
            max = -maxint-1
            n = 0
            # Choose
            for j in range(0, self.GRAD_SIZE):
                if self.clicklist[j] > max and j not in nums:
                    max = self.clicklist[j] #  The better cl value to be excluded
                    n = j # index of it
            nums.append(n)

        #  create subset of gradient matrix
        grad_temp = np.zeros([self.EXP_SIZE, self.n_features], dtype=float)
        c = 0
        for i in range(0,self.GRAD_SIZE):
            if i not in nums:
                # The wrost 'EXP_SIZE' gradients from grad[] added to gr_temp
                grad_temp[c] = copy.deepcopy(self.grad[i])
                c = c + 1

        self.model.sample_candidates_null_space(grad_temp, query_feat, self.sample_basis)
        scores = self.model.candidate_score(query_feat)
        rankings = rnk.rank_single_query(scores, inverted=False, n_results=self.n_results)
        multileaved_list = self.multileaving.make_multileaving(rankings)
        return multileaved_list

    def fill_difficult_query(self, clicks):
        #  Set up for tie breaker- keep track of difficult queries
        #  Find the rank of first clicked document
        ndcg_current = 0
        clickedList = []
        for count, elem in enumerate(clicks):
            if elem == 1: # if clicked
                ndcg_current += 1 / (count + 1.0)
                # Keep track of clicked documents of current query
                clickedList.append(self._last_ranking[count])

        # If difficult queries for tie breaking is not filled up, add current query
        if len(self.difficult_NDCG) < self.TB_QUEUE_SIZE and ndcg_current > 0:
            self.difficult_NDCG.append(ndcg_current)
            self.difficult_queries.append(self.query_id)
            self.difficult_document.append(clickedList)  # first clicked doc to follow
            self.difficult_time.append(self.n_interactions)
        else:
            # If already filled up, check if current query is more difficult than any saved query.
            if len(self.difficult_NDCG) > 0:
                flag = False
                for i in range(len(self.difficult_NDCG)):
                    if self.n_interactions - self.difficult_time[i] > self.TB_WINDOW_SIZE:
                    # Maintain queries winthin the window size
                        flag = True
                        index = i
                        break
                if not flag and max(self.difficult_NDCG) > ndcg_current and ndcg_current > 0:
                    # Current query is more difficult than one of queued ones
                    flag = True
                    index = self.difficult_NDCG.index(max(self.difficult_NDCG))
                if flag:
                    self.difficult_NDCG[index] = ndcg_current
                    self.difficult_queries[index] = self.query_id
                    self.difficult_document[index] = clickedList
                    self.difficult_time[index] = self.n_interactions

    def tieBreak_difficultQuery(self, winners):
        # ScoreList keeps track of ranks each tied candidate perform in tie breaking
        scoreList = np.zeros(self.model.n_models)
        # Iterate through 10 stored difficult queries
        for count_q, diff_query in enumerate(self.difficult_queries):
            query_feat = self.get_query_features(diff_query,
                                       self._train_features,
                                       self._train_query_ranges)
            scores = self.model.candidate_score(query_feat)
            rankings = rnk.rank_single_query(scores, inverted=False, n_results=self.n_results)

            # Iterate through tied candidates
            for winner in winners:
                candidate_NDCG = 0.0
                for count_d, doc in enumerate(self.difficult_document[count_q]):
                    # Calculate NDCG performance in current difficult query
                    diff_doc_rank = np.where(rankings[winner] == self.difficult_document[count_q][count_d])[0][0]
                    temp = 1 / (diff_doc_rank + 1.0)
                    candidate_NDCG += 1 / (diff_doc_rank + 1.0)

                # Add the NDCG value of diff. query
                scoreList[winner] += candidate_NDCG
        # Ranker with the least sum of NDCGs is the winner
        maxRank_score = np.max(scoreList[np.nonzero(scoreList)])
        winner = scoreList.tolist().index(maxRank_score)
        return [winner]
