#!/bin/bash
# Setting memory limits
export MAVEN_OPTS="-Xmx10000m -Xms2048m"

SED_OPT=" -r "
if [[ "$OSTYPE" == "darwin"* ]]
then
  echo "Mac!!!"
  # On Mac sed have different options
  SED_OPT=" -E "
else
  echo "Not Mac"
fi

# Change this to 1 if you want to stop after each failure to manually expect failure reasons
STOP_AFTER_FAILURE=1

# A hacky realpath replacement pilfered from http://stackoverflow.com/a/3572105/2120401
realpath_repl() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}

usage() {
  echo $1
  echo "Usage: <INPUT_DIR> <LIST_FILE: each record has [team-project-subdir cpe-descriptor-name]> <MAVEN_DIR: Maven folder root, e.g.,  ~/.m2/repository/edu/cmu/lti/11791/f14/hw1/>"
  exit 1
}

# Input dir
INPUT_DIR=`realpath_repl $1`

if [ "$INPUT_DIR" == "" ] ; then
  usage "Missing INPUT_DIR"
fi

if [ ! -d "$INPUT_DIR" ] ; then
  usage "INPUT_DIR is not a directory"
fi

# As input this script takes a file, where each line is:
# <student it> ...
# Results are saved in the folder results.
# If we failed to run some student's archive, we add her/his name to the file
# results/failed.txt
LIST_FILE=$2

if [ "$LIST_FILE" == "" ] ; then
  usage "Missing LIST_FILE"
fi

if [ ! -f "$LIST_FILE" ] ; then
  usage "LIST_FILE is not a file"
fi

echo "" >> $LIST_FILE

MAVEN_DIR=$3

if [ "$MAVEN_DIR" == "" ] ; then
  usage "Missing MAVEN_DIR"
fi

if [ ! -d "$MAVEN_DIR" ] ; then
  usage "MAVEN_DIR is not a dir!"
fi

# Beware this script cleans up everything before start

echo -n > report.txt
REPORT_FILE=`realpath_repl report.txt`
echo "Error file: $REPORT_FILE"

# We expect the script precision_recall.py to be located in this directory.
# It should also contain a pom.xml template and 
# a special Java file RunJarCPE.java that can  run a CPE  using
# descriptors from a jar. A standard UIMA example SimpleRunCPE can't do it.
SCRIPT_DIR=`dirname $0`
SCRIPT_DIR=`realpath_repl "$SCRIPT_DIR"`
echo "SCRIPT_DIR=$SCRIPT_DIR"

create_run_dir() {
  echo "POM-file: $pom group id: $gid artifact id: $artid version '$ver' base dir: '$BASE_DIR'"
  if [ ! -d "$BASE_DIR" ] ; then
    mkdir -p "$BASE_DIR/src/main/java"
    mkdir -p "$BASE_DIR/src/main/resources"
  fi
  cp "$SCRIPT_DIR/RunJarCPE.java" "$BASE_DIR/src/main/java"
  if [ "$?" !=  "0" ] ; then
    echo "Failure copying the java file!"
    exit 1
  fi
  cat "$SCRIPT_DIR/pom-template.xml"|sed "s|INSERT_DEPENDENCY_HERE|<groupId>$gid</groupId><artifactId>$artid</artifactId><version>$ver</version>|" > "$BASE_DIR/pom.xml"
  if [ "$?" !=  "0" ] ; then
    echo "Failure creating pom.xml  file!"
    exit 1
  fi
  cd "$BASE_DIR"
  if [ "$?" !=  "0" ] ; then
    echo "Failure cd to '$BASE_DIR'"
    exit 1
  fi
  if [ ! -h "input" ]
  then
    ln -s "$INPUT_DIR" input
  fi
  if [ "$?" !=  "0" ] ; then
    echo "Failure creating a link to input dir $INPUT_DIR!"
    exit 1
  fi
  if [ ! -h "project.properties" ]
  then
    ln -s "$SCRIPT_DIR/project.properties" project.properties
  fi
  if [ "$?" !=  "0" ] ; then
    echo "Failure creating a link to $SCRIPT_DIR/project.properties!"
    exit 1
  fi
  cd -
}

# Let's rock'n'roll
n=`wc -l "$LIST_FILE"|awk '{print $1}'`
n=$(($n+1))
for ((i=1;i<$n;++i))
  do
    line=`head -$i "$LIST_FILE"|tail -1`
    if [ "$line" !=  "" ] 
    then
      subproj_dir=`echo $line|awk '{print $1}'`
      cpe=`echo $line|awk '{print $2}'`
      echo "Project: $subproj_dir, CPE file: $cpe"

      success=1

      # Should print the latest one
      pom=`ls $MAVEN_DIR/$subproj_dir/*/*.pom|$SCRIPT_DIR/sort_pom_ver.py|tail -1` 

      if [ "$pom" = "" ]
      then
        str="$id ERROR can't find POM"
        echo $str 
        echo $str  >> "$REPORT_FILE"
        success=0
      else
        gid=`grep  '<groupId>' "$pom"|head -1|sed $SED_OPT 's/^.*<groupId>//'|sed $SED_OPT 's/<.groupId>.*$//'`
        ver=`grep  '<version>' "$pom"|head -1|sed $SED_OPT 's/^.*<version>//'|sed $SED_OPT 's/<.version>.*$//'`
        artid=`grep  '<artifactId>' "$pom"|head -1|sed $SED_OPT 's/^.*<artifactId>//'|sed $SED_OPT 's/<.artifactId>.*$//'`
        BASE_DIR=run/$artid

        if [ "$gid" = "" -o "$ver" = "" -o "$artid" = "" ] 
        then
          str="$artid ERROR Can't obtain groupId, version, or artifactId from '$pom'"
          echo $str 
          echo $str  >> "$REPORT_FILE"
          success=0
        else 
          create_run_dir $ver $artid
          cd "$BASE_DIR"



          echo "mvn clean compile exec:java -Dexec.mainClass=RunJarCPE  -Dexec.args=\"$cpe\""
          mvn clean compile exec:java -Dexec.mainClass=RunJarCPE  -Dexec.args="$cpe"
          if [ "$?" != "0" ]
          then
            str="$artid ERROR running '$pom'"
            echo $str 
            echo $str  >> "$REPORT_FILE"
            success=0
          fi

          cd -
        fi
      fi 

      if [ "$STOP_AFTER_FAILURE" = "1" -a "$success" != "1" ] ; then
        echo "Stopping after a failure!"
        exit 1
      fi
    fi
  done

echo "# of student records processed: $ns"


