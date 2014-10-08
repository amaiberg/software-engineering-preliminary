#!/usr/bin/python

import os
import distutils.version
import commands

def read_student_list(list_path):
  return [line.strip() for line in open(list_path)]


def get_jar_path(root, artifact):
  artifact_path = os.path.join(root, artifact)
  if not os.path.exists(artifact_path): return None
  versions = [version for version in os.listdir(artifact_path)
              if os.path.isdir(os.path.join(artifact_path, version))]
  version = max(versions, key=distutils.version.LooseVersion)
  jar_path = os.path.join(artifact_path, version,
                          '%s-%s.jar' % (artifact, version))
  # print jar_path
  if  not os.path.exists(jar_path):
    print "Path doesn't exist: " + jar_path
  assert os.path.exists(jar_path)
  return jar_path


def extract_pdf(jar_path, path):
  cmd = 'unzip %s %s' % (jar_path, path)
  result = commands.getoutput(cmd).strip()
  return result[result.rfind(' ')+1:]

import sys

#print sys.argv
#sys.exit(0)

if __name__ == "__main__":
  root = sys.argv[1]
  student_list = sys.argv[2]
  for student in read_student_list(student_list):
    for part in ['report', 'uml']:
      path = 'docs/hw1-%s-' + part + '.*'
      jar_path = get_jar_path(root, 'hw1-%s' % student)
      if jar_path is not None:
        artifact = os.path.basename(jar_path)
        result = extract_pdf(jar_path, path % student)
        if result.endswith('*'):
          print '%s\t%s\tno %s' % (student, artifact, part)
        else:
          print '%s\t%s\t%s' % (student, artifact, result)
      else:
        print '%s\t\tnot submitted' % student
