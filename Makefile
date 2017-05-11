# Makefile for mysqldb
#
# James Edwards and Shashwat Chaturvedi, May 2017

CLASSPATHJ=/Users/James/Documents/Dartmouth/3rd\ Year/17S/CS61/mysqlDB/mysql-connector-java.jar

make:
	javac mysqldb.java

run:
	# java -cp $(CLASSPATHJ) mysqldb
	java mysqldb

clean:
	rm -f *.class
	rm -f mysql
