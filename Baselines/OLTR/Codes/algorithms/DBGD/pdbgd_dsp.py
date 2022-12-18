# -*- coding: utf-8 -*-

import sys
import os
sys.path.append(os.path.join(os.path.dirname(__file__), '../..'))
import utils.rankings as rnk
from algorithms.DBGD.tddbgd import TD_DBGD
from multileaving.ProbabilisticMultileave import ProbabilisticMultileave
import numpy as np
import math

# Probabilistic Interleaving Dueling Bandit Gradient Descent
class P_DBGD_DSP(TD_DBGD):

  def __init__(self, k_initial, k_increase, PM_n_samples, PM_tau, prev_qeury_len=None, docspace=[False,0], *args, **kargs):
    super(P_DBGD_DSP, self).__init__(*args, **kargs)

    self.multileaving = ProbabilisticMultileave(
                             n_samples = PM_n_samples,
                             tau = PM_tau,
                             n_results=self.n_results)

    self.k_initial = k_initial
    self.k_increase = k_increase

    self.prev_qeury_len = prev_qeury_len # queue size of features from previous queries
    if prev_qeury_len:
      self.prev_feat_list = []
    # for document space length experiment
    # docspace=[True,3] means use superset of document space with three additional documents to perfect DS user examined.
    self.docspace = docspace  

  @staticmethod
  def default_parameters():
    parent_parameters = TD_DBGD.default_parameters()
    parent_parameters.update({
      'learning_rate': 0.01,
      'learning_rate_decay': 1.0,
      'PM_n_samples': 10000,
      'PM_tau': 3.0,
      })
    return parent_parameters

  def _create_train_ranking(self, query_id, query_feat, inverted):
    # Save query_id to get access to query_feat when updating
    self.query_id = query_id
    assert inverted==False
    self.model.sample_candidates()
    scores = self.model.candidate_score(query_feat)
    inverted_rankings = rnk.rank_single_query(scores,
                                              inverted=True,
                                              n_results=None)
    multileaved_list = self.multileaving.make_multileaving(inverted_rankings)
    return multileaved_list


  def update_to_interaction(self, clicks, stop_index=None):

    winners = self.multileaving.winning_rankers(clicks)
    ###############################################################
    if True in clicks:
      # For projection
      # keep track of feature vectors of doc list
      viewed_list = []
      # index of last click
      last_click = max(loc for loc, val in enumerate(clicks) if val == True)
      # prevent last_click+k from exceeding interleaved list length
      k_current = self.k_initial
      if self.k_increase:
        # gradually increast k
        k_current += int(self.n_interactions/1000)
      last_doc_index = min(last_click+k_current, len(self._last_ranking))

      if self.docspace[0] and stop_index is not None: # for document space length experiment
        # create sub/super set of perfect document space user examined. 
        # user examined documents coming from ccm, where user leaves.
        last_doc_index = stop_index + self.docspace[1] + 1 # 1 added for stopping document, which has been examined.
        last_doc_index = max(last_doc_index,1) # At least 1
        last_doc_index = min(last_doc_index,len(self._last_ranking)) # At most length of current list

      query_feat = self.get_query_features(self.query_id,
                                       self._train_features,
                                       self._train_query_ranges)
      for i in range(last_doc_index):
        docid = self._last_ranking[i]
        feature = query_feat[docid]
        viewed_list.append(feature)
      add_list = viewed_list

      # Append feature vectors from previous queries
      if self.prev_qeury_len:
        if len(self.prev_feat_list) > 0:
          viewed_list = np.append(viewed_list,self.prev_feat_list, axis=0)

        # Add examined feature vectors of current query to be used in later iterations
        for i in add_list:
          if len(self.prev_feat_list) >= self.prev_qeury_len :
            self.prev_feat_list.pop(0)  # Remove oldest document feature.
          # if prev_feat_list is not filled up, add current list
          self.prev_feat_list.append(i)

      self.model.update_to_mean_winners(winners,viewed_list)
    ###############################################################
    else:
      self.model.update_to_mean_winners(winners)

