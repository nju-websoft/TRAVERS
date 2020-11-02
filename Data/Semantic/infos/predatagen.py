# -*- coding:utf-8 -*-

name = input()
suf = ['11b', '21b', '21o', '22b']
for su in suf:
    op = open(name + '_' + su + '.txt', 'r', encoding='utf8');
    wt = ''
    for i in range(10):
        s = op.readline()
        s = s.replace('\r', '')
        s = s.replace('\n', '')
        if i > 0:
            wt += ','
        wt += s
        s = op.readline()
    print(wt)
