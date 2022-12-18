# -*- coding:utf-8 -*-

import os
import sys

fea = ''

def DFS(d):
    for lists in os.listdir(d):
        path = os.path.join(d, lists)
        if os.path.isdir(path):
            os.chdir(path)
            os.system('rm -rf *')

DFS('./testoutput/average')
DFS('./fullruns')
