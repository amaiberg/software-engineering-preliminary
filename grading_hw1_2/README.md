This is a grading script (for Linux) as well as instructions for running it.
===================================================

1) Make sure you project compiles. Then, install it to a local Maven repository:
```
mvn clean compile install
```

2) Make sure you install a **realpath** script. On Ubuntu, type:
```
sudo apt-get install realpath
```

3) Create a one-line registry file **your_info.txt** that specifies your ID, input, output file and the name of your CpeDescriptor.xml. An example (**ID**, as usual, is your Andew/CS id) for Homework **2**:
```
ID hw2.in hw2-ID.out CpeDescriptor.xml
```

Create some test directory. Go there. Create the file **hw2.in** as a copy of **sample.in**:

```
cp <path to sample.in>/sample.in hw2.in
```

Similarly, create the file **hw2.gold_stand** that actually contains the contents of **sample.out**:
```
cp <path to sample.out>/sample.out hw2.gold_stand
```

Finally, from **Bash** you can run the grading script as follows:
```
./grader.sh hw2.in hw2.gold_stand hw2 your_info.txt ~/.m2/repository/edu/cmu/lti/11791/f14/hw2/ 2>&1|tee out.log
```

If all is fine, in the end, the script should print something like:
```
Precision: 0.593141289438
Recall: 0.682988469436
F1 Score: 0.634901989575
```

