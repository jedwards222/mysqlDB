# Makefile for mysqldb
#
# James Edwards and Shashwat Chaturvedi, May 2017

CLASSPATHS=.:/Users/Shashwat/Desktop/mysql-connector-java-5.1.42-bin.jar
CLASSPATHJ=.:/Users/James/Desktop/mysql-connector-java-5.1.42-bin.jar

make:
	javac mysqldb.java

run:
	java -classpath $(CLASSPATHJ) mysqldb
	# java -classpath $(CLASSPATHS) mysqldb

clean:
	rm -f *.class
	rm -f mysql
