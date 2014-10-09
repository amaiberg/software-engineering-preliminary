An example of using a (resource) dictionary from inside of a deployed maven jar
===================================================================================
Usage. First, install the dictionary. Go to the folder **dict-resource**, type:
```
mvn install
```
Then go to the directory **dict-user**, then type:
```
mvn exec:java -Dexec.mainClass=test
```
The content of the dictionary file should be printed. For details, study the file **dict-user/src/main/java/test.java**
