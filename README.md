# CS61 Lab 2

##### James Edwards and Shashwat Chaturvedi, May 2017

### Overview

This is a database application for a journal. Editors, authors, and reviewers can login to interact with manuscripts in various ways.

### Compilation and Running

Run `make` to compile the java source code, then run the code in `setup.sql` to initialize the tables and some preliminary data in the database, and then `make run` to run the java executable. You must have the file `mysql-connector-java-5.1.42-bin.jar` somewhere in your file system, and specify the path to that file within the Makefile (example is provided).

Run `make clean` to remove the executable and object file created by compilation.

### Assumptions/Limitations

1. User input is enforced rigidly to conform to the instructions (case sensitive). If the user inputs extra words, numbers, or incorrect commands, behavior is unpredictable.

2. Editors have a large degree of power according to our interpretation of the business rules. Editors are not stopped from making actions on manuscripts that were not 'assigned' to them - we treat this assignment as more of a guidelines to the editors of which manuscripts they should prioritize.

3. Some commands differ from the business rules for an easier experience for users. For example, registering authors only have to enter their name with the initial `register` command. From there, the program will prompt the user for more input to get the rest of the required information from them. This also ensures that the user can tell exactly what input they gave that was invalid in case of an error.

4. Author affiliation is not related to a particular manuscript - it is always current to the most recently submitted manuscript. While this is slightly against a suggestion from a Piazza post, it is in line with our design throughout parts a-d of the lab.

5. If a reviewer resigns, all of their reviews are deleted from the database.

### Testing

We went through every possible command in a variety of normal use cases and edge cases. Our database was robust against these edge cases and preventing corruption of data.

### TODO

1. editor logged in: reject/accept should check manuscript status

2. editor logged in: issue period must be between 1 and 4

3. author logged in: submit manuscript without enough ri code reviewers logs you out <-- Well it doesn't log you out but sends you back to main page 

4. Haven't checked all of reviewer's functions that closely -- also would be easier to check further editor abilities if we had some more actions for editor to take (ie. see all reviewers)

5. Add a function to list aoi code descriptions and their numbers <-- Just added lines to author. Think we only need it two places?
