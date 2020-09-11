# MS3_Coding_Challenge
This repository consists of a submission to the MS3 Coding Challenge for evaluation of software development skill.

## Summary
This Java program takes a CSV file `<input-filename>.csv` as input through the use of GUIs. The program then parses the file, checking each record in the CSV file and inserting the valid records into an SQLite database. The program outputs three new files:

- `<input-filename>.db`: An SQLite database file
- `<input-filename>-bad.csv`: A CSV file containing invalid entries
- `<input-filename>.log`: A log file containing statistics about the parsing process.

## Execution
To run this program, simply download the `parser_challenge-1.0-SNAPSHOT-jar-with-dependencies.jar` executablefile and double-click on it. A default CSV file is also provided in this repo for the sake of testing the program (`ms3Interview.csv`).

## Design
The flow of execution for this program is as follows (messages are outputted in a GUI to inform the user):

1. Setup the GUIs (java.swingx) and accept a file from the user.
2. Open Scanners (java.util) and PrintWriters (java.io) for all files.
3. Establish a connection to the database file (sqlite-JDBC).
4. Generate and execute a table creation command for the database.
5. Split each record of the file.
6. Verify the entries in each record.
7. Generate an insertion command with valid entries.
8. Write invalid entries to the appropriate file.
9. Execute the insertion command.
10. Write the statistics to the log file.

This program was built in IntelliJ CE (2019.1.4) with Apache Maven. The process of extracting the records is performed iteratively as to minimize repeated looping through the input file. Precautions have been put in place to ensure that this program will scale for large files (e.g. regular PrintWriter flushing and SQLite insertions); not only does this ensure that memory limits will not be reached, but also this slightly decreases execution time (approx. 1 minute for 6000 records). The file scanning uses a UTF-8 character set to ensure that the file can be comprised of diverse characters. The program also uses as little parameter passing as possible to increase memory efficiency. The program is broken down into a series of methods that perform a few simple actions, ensuring readibility. The program can be found in the `CSVParser.java` file.

There is only one assumption made by this program. The name of the CSV file cannot contain any spaces; this presented issues when creating the database file even with the use of escape characters. Otherwise, this program will accept any valid CSV file (in any location) with any number of columns and entries.
