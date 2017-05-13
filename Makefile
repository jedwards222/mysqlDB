# Makefile for mysqldb
#
# James Edwards and Shashwat Chaturvedi, May 2017

CLASSPATHJ=.:/Users/James/Desktop/mysql-connector-java-5.1.42-bin.jar
CLASSPATHS=.:/Users/Shashwat/Desktop/mysql-connector-java-5.1.42-bin.jar
# Define a path for yourself, Shash: CLASSPATHS=...

make:
	javac mysqldb.java

run:
	java -classpath $(CLASSPATHS) mysqldb
	# java -classpath $(CLASSPATHS) mysqldb

clean:
	rm -f *.class
	rm -f mysql
