# -*- coding:utf-8 -*-

import os
import sys
import random
import math

alpha = 0.5
TopK = 20

def ReadData(name):
    op = open(name, 'r', encoding='utf8')
    cnt = 0
    dat = []
    for line in op.readlines():
        line = line.replace('\r', '')
        line = line.replace('\n', '')
        if len(line) <= 1:
            continue
        dat.append([int(x.split(':')[0]) for x in line.split(' ')[2:-1]])
    op.close()
    print('Read End!', end='')
    return dat            

def Solve(d):
    print(d + ' ', end='')
    path = os.path.join(d, 'train.txt')
    data = ReadData(path)
    wt = open(os.path.join(d, 'alpha.txt'), 'w', encoding='utf8')
    rec = {}
    l = len(data)
    ids = [i for i in range(l)]
    reslist = []
    ret = 0.0
    for rk in range(TopK):
        random.shuffle(ids)
        id_rec = -1
        val_rec = -1.0
        for i in ids:
            if i in reslist: continue
            su = sum([(1.0 - alpha) if pro not in rec.keys() else (1.0 - alpha) ** rec[pro] for pro in data[i]])
            if su > val_rec:
                val_rec = su
                id_rec = i
        reslist.append(id_rec)
        for pro in data[id_rec]:
            if pro not in rec.keys():
                rec[pro] = 0.0
            rec[pro] += 1.0
        ret += val_rec / math.log(rk + 2)
        if rk > 0: wt.write(' ')
        wt.write(str(ret))
    wt.write('\r\n')
    for i in range(TopK):
        if i > 0:
            wt.write(' ')
        wt.write(str(reslist[i]))
    wt.write('\r\n')
    wt.close()
    print(' Solve End!')
            
def DFS(d):
    flag = False
    for dirs in os.listdir(d):
        if 'cold.txt' in dirs:
            flag = True
            break
    if flag:
        Solve(d)
        return
    for dirs in os.listdir(d):
        path = os.path.join(d, dirs)
        if os.path.isdir(path):
            DFS(path)

DFS('DataSets/')