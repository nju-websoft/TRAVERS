# -*- coding:utf-8 -*-

import sys
import os

names = [
    'train.txt',
    'test.txt',
    'vali.txt'
]

groups = [
    'Or11b',
    'Or21b',
    'Or21o',
    'Or22b'
]

def solve(f1, f2):
    rd = open(f1, 'r', encoding='utf8')
    wt = open(f2, 'w', encoding='utf8')
    for line in rd.readlines():
        line = line.replace('\r', '')
        line = line.replace('\n', '')
        spl = line.split(' ')
        pri = ''
        pri += spl[0]
        pri += ' ' + spl[1] + ' '
        spl = spl[2:-1]
        spl.sort(key = lambda x : int(x.split(':')[0]))
        pri += ' '.join(spl)
        pri += ' #'
        wt.write(pri + '\r\n')
    rd.close()
    wt.close()

def main():
    '''
    for g in groups:
        for id in range(1, 6):
            for n in names:
                f1 = g + '/' + 'Fold' + str(id) + '/' + n
                f2 = './news/' + f1
                f1 = './' + f1
                solve(f1, f2)
    '''
    solve('./test.txt', './res.txt')

if __name__ == '__main__':
    main()
