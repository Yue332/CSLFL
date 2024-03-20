import pickle
import sys, os
import numpy as np

pr = sys.argv[1]
seed = int(sys.argv[2])
lr = float(sys.argv[3])
batch_size = int(sys.argv[4])
def splitCamel(token):
        ans = []
        tmp = ""
        for i, x in enumerate(token):
            if i != 0 and x.isupper() and token[i - 1].islower() or x in '$.' or token[i - 1] in '.$':
                ans.append(tmp)
                tmp = x.lower()
            else:
                tmp += x.lower()
        ans.append(tmp)
        return ans
p = pickle.load(open(pr + 'res_%d_%s_%s.pkl'%(seed,lr,batch_size), 'rb'))
f = pickle.load(open(pr + '.pkl', 'rb'))

print(len(f), len(p))
#assert(0)
score = []
score2 = []
eps = {}
best_ids = []
for _, i in enumerate(p):
    maxn = 1e9
    xs = p[i]
    score.extend(xs[0])
    print(i, xs[0], xs[1])
    minl = 1e9
    for x in f[i]['ans']:
        m = xs[1].index(x)
        minl = min(minl, m)
    score2.append(minl)
    rrdic = {}
    for x in f[i]['methods']:
        rrdic[f[i]['methods'][x]] = x#".".join(x.split(":")[0].split(".")[-2:])
    
    for x in f[i]['ftest']:
        print(splitCamel(".".join(x.split(":")[0].split(".")[-2:])), x, ".".join(x.split(":")[0].split(".")[-2:]))
    print("-----")
    for x in f[i]['ans']:
        print(splitCamel(rrdic[x]), rrdic[x], ',')
    print("-----")
    print(rrdic, f[i]['ans'])
    print(splitCamel(rrdic[xs[1][0]]), rrdic[xs[1][0]], ',', xs[1][0], f[i]['ans'])
    #print(f[i]['methods'], f[i]['ftest'], f[i]['ans'])
    for x in xs[2]:
        if x in eps:
            eps[x] += 1
        else:
            eps[x] = 1
    if 10 in xs[2]:
        best_ids.append(i)
    #print(xs[2])
    #score.append(maxn)

with open(pr + 'result_final_%d_%s_%s'%(seed,lr, batch_size), 'w') as pp:
    pp.write("lr: %f seed %d batch_size %d\n"%(lr, seed, batch_size))
    pp.write('num: %s\n'%len(p))
    pp.write('%d: %d\n'%(10, eps[10]))
    pp.write(str(sorted(eps.items(), key=lambda x:x[1])))

print(len(score))
a = []
for i, x in enumerate(score):
    if x != 0:
        a.append(i)
print(a)
print(len(score))
print(score.count(0))
print(score2.count(0))
print(eps)
c1 = 0
for x in score:
    if x < 3:
        c1 += 1
c2 = 0
for x in score:
    if x < 5:
        c2 += 1
print('top35',c1, c2)
print(sorted(eps.items(), key=lambda x:x[1]))

print(best_ids)
print(len(best_ids))

