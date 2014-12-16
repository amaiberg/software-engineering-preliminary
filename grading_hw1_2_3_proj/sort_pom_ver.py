#!/usr/bin/python
# A hacky script to sort POM files by version
import sys
import re
def key_func(s):
  res = []
  s=s[(s.rfind('/')+1): ]
  for t in re.split('[.-]', s):
    res.append(t.zfill(10))
  ret = '.'.join(res)
  #print s + '->' + ret
  return ret

lines = sys.stdin.readlines()

for i in range(0,len(lines)):
  lines[i] = lines[i].strip()

for s in sorted(lines,key=key_func):
  print s
