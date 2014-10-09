#!/bin/bash

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
  echo "Usage: <IN_FILE> <GS_FILE> <HW_PREFIX: e.g., hw1, or hw2> <LIST_FILE: each record has [id] [input_file] [output_file] [CPE_desc_name]> <MAVEN_DIR: Maven folder root, e.g.,  ~/.m2/repository/edu/cmu/lti/11791/f14/hw1/>"
  exit 1
}

# An input file
IN_FILE=$1

if [ "$IN_FILE" == "" ] ; then
  usage "Missing IN_FILE"
fi

if [ ! -f "$IN_FILE" ] ; then
  usage "IN_FILE is not a file"
fi

# Gold standard file
GS_FILE=`realpath_repl $2`

if [ "$IN_FILE" == "" ] ; then
  usage "Missing GS_FILE"
fi

if [ ! -f "$GS_FILE" ] ; then
  usage "GS_FILE is not a file"
fi

# Prefix, e.g., hw1
HW_PREFIX=$3

if [ "$HW_PREFIX" == "" ] ; then
  usage "Missing HW_PREFIX"
fi

# As input this script takes a file, where each line is:
# <student it> <in file> <CpeDescriptorFile> 
# Results are saved in the folder results.
# If we failed to run some student's archive, we add her/his name to the file
# results/failed.txt
LIST_FILE=$4

if [ "$IN_FILE" == "" ] ; then
  usage "Missing LIST_FILE"
fi

if [ ! -f "$LIST_FILE" ] ; then
  usage "LIST_FILE is not a file"
fi

MAVEN_DIR=$5

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
  inf=`realpath_repl "$IN_FILE"`
  cd "$BASE_DIR"
  if [ "$?" !=  "0" ] ; then
    echo "Failure cd to '$BASE_DIR'"
    exit 1
  fi
  if [ ! -h "$stud_in_file" ]
  then
    ln -s "$inf" "$stud_in_file"
  fi
  if [ "$?" !=  "0" ] ; then
    echo "Failure creating a link to input file!"
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
      id=`echo $line|awk '{print $1}'`
      stud_in_file=`echo $line|awk '{print $2}'`
      stud_out_file=`echo $line|awk '{print $3}'`
      cpe=`echo $line|awk '{print $4}'`
      echo "Student: $id, input file: $stud_in_file,  output file: $stud_out_file  CPE file: $cpe"

      success=1

      # Should print the latest one
      pom=`ls $MAVEN_DIR/$HW_PREFIX-$id/*/*.pom|$SCRIPT_DIR/sort_pom_ver.py|tail -1` 

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
          if [ ! -f "$stud_out_file" ] 
          then
            echo "Output file is  missing, let's execute $artid" 
            echo "mvn clean compile exec:java -Dexec.mainClass=RunJarCPE  -Dexec.args=\"$cpe\""
            mvn clean compile exec:java -Dexec.mainClass=RunJarCPE  -Dexec.args="$cpe"
            if [ "$?" != "0" ]
            then
              str="$artid ERROR running '$pom'"
              echo $str 
              echo $str  >> "$REPORT_FILE"
              success=0
            fi
          fi

          if [ ! -f "$stud_out_file" ] 
          then
            str="$artid ERROR missing output file $stud_out_file"
            echo $str 
            echo $str  >> "$REPORT_FILE"
            success=0
          else
            "$SCRIPT_DIR"/precision_recall.py --truth "$GS_FILE" --test "$stud_out_file" 2>&1|tee out
            score=`grep 'F1 Score:' out`
            echo "$artid $ver SUCCESS $score" >> "$REPORT_FILE"
          fi
          if [ "$success" = "0" ] ; then
            rm -f "$stud_out_file"
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


