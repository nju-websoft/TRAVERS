# -*- coding:utf-8 -*-

import os
import sys

fea = ''

def DFS(d):
    for lists in os.listdir(d):
        path = os.path.join(d, lists)
        if fea not in str(path):
            continue
        #print(str(path))
        if os.path.isdir(path):
            os.chdir(path)
            os.system('rm -rf *.pickle *.npz binarized_train*')
            DFS(path)

fea = sys.argv[1]
DFS('DataSets/')
