# CS61 Lab 2

##### James Edwards and Shashwat Chaturvedi, May 2017

### Overview

This is a database application for a journal. Editors, authors, and reviewers can login to interact with manuscripts in various ways.

### Compilation and Running

Run `make` to compile the java source code, and `make run` to run the resulting executable. You must have the file `mysql-connector-java-5.1.42-bin.jar` somewhere in your file system, and specify the path to that file within the Makefile (example is provided).

Run `make clean` to remove the executable and object file created by compilation.

### Assumptions/Limitations

1. User input is enforced rigidly to conform to the instructions (case sensitive).
