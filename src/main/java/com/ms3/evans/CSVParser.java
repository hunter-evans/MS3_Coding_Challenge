package com.ms3.evans;

import java.util.Scanner;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.*;
import javax.swing.*;

/**
 * Submission for the MS3 coding challenge. This class takes as input a
 * CSV file using a GUI and outputs a SQLite database file, a bad CSV
 * file, and a log file with statistics in the directory of the program.
 *
 * @author Hunter Evans
 * @version 1.0.0
 */
public class CSVParser extends JPanel {
    /**
     * textArea - The text area representing output for the program.
     */
    private static JTextArea textArea;

    /**
     * fileScanner - The scanner for the CSV file.
     */
    private static Scanner fileScanner;

    /**
     * filename - The name of the csv (w/o file extension)
     */
    private static String filename;

    /**
     * badPrintWriter - The print writer for the bad CSV file.
     */
    private static PrintWriter badPrintWriter;

    /**
     * headers - The ArrayList containing the headers for the db.
     */
    private static ArrayList<String> headers;

    /**
     * conn - The connection to the database file.
     */
    private static Connection conn;

    /**
     * insertionStmt - The final line used to insert valid records.
     */
    private static String insertionStmt = "";

    /**
     * numReceived - The number of records in the CSV file.
     */
    private static int numReceived = 0;

    /**
     * numSuccessful - The number of successful insertions into db.
     */
    private static int numSuccessful = 0;


    /**
     * Main method for the class, which creates the GUIs and launches
     * the parser.
     */
    public static void main(String[] args) {
        // Create JFrame to hold the text
        JFrame frame = new JFrame();
        // Instantiate the text area.
        textArea = new JTextArea();
        // Create a scroll bar for the text area and add to frame.
        frame.getContentPane().add(new JScrollPane(textArea));

        // Set the title of the GUI
        frame.setTitle("CSV to SQLite Database Parser");
        // Set the dimensions (resizeable) of the GUI
        frame.setSize(600,400);
        frame.setResizable(true);
        // Set the default location.
        frame.setLocation(200,200);
        // Ensure that the window is visible
        frame.setVisible(true);
        // Set the default closing action (terminate)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Generate file chooser GUI.
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);

        try {
            // Parse file from the chooser.
            parseFile(chooser.getSelectedFile());
            textArea.append("Program complete! Please close the program.\n");
        }
        // If file not found, print error message.
        catch (FileNotFoundException e) {
            textArea.append("\n" + e + "\n");
        }
    }

    /**
     * This method executes the actions at a high level needed to parse the
     * file, including setting up scanners and print writers, generating the
     * headers, connecting to the database, iterating over the CSV, and
     * writing the log file.
     *
     * @param fileArg - The CSV file
     * @throws FileNotFoundException - For the scanners and print writers
     */
    private static void parseFile(File fileArg) throws FileNotFoundException {

        textArea.append("You have selected: " + fileArg.getName() + "\nBeginning file parse...\n");

        // Try to open file scanner (ensure UTF-8 encoding for non-standard characters).
        fileScanner = new Scanner(fileArg, "UTF-8");

        // Generate the filename w/o extension
        filename = fileArg.getName().substring(0, fileArg.getName().length() - 4);

        // Generate print writer for bad CSV file
        badPrintWriter = new PrintWriter(filename + "-bad.csv");

        // Get headers from first line
        headers = customSplit(fileScanner.nextLine());

        try {
            // Generate the db file and create table, getting a header string back
            String headerString = generateDBTable();

            textArea.append("Table successfully created.\nBeginning record processing...\n");

            // Begin the insertion process
            insertIntoDB(headerString);

            textArea.append("All records successfully inserted. Outputting statistics...\n");
        }
        // Catch any exceptions during creation/insertion.
        catch (SQLException e) {
            textArea.append("\n" + e.getMessage() + "\n");
        }

        // Close the bad CSV print writer
        badPrintWriter.close();

        // Generate a print writer for the log file.
        PrintWriter pw = new PrintWriter(filename + ".log");
        // Write the statistics.
        pw.println("Number of records received:   " + numReceived);
        pw.println("Number of records successful: " + numSuccessful);
        pw.println("Number of records failed:     " + (numReceived - numSuccessful));
        // Close the log file print writer.
        pw.close();
    }

    /**
     * This method establishes a connection to the db file, creates the table for
     * the db file, and generates a string of the headers (used for insertion).
     *
     * @return - The string (A,B,C...) where A,B,C... are the headers of the db.
     * @throws SQLException - For db connection and table creation.
     */
    public static String generateDBTable() throws SQLException {
        // Create new database file
        conn = DriverManager.getConnection("jdbc:sqlite:" + filename + ".db");

        textArea.append("Connection established to database file.\nCreating new table...\n");

        // Create string for table creation
        String sql = "CREATE TABLE IF NOT EXISTS " + filename + "(\n";
        // Header string for insertion (used later)
        String headerString = "(";

        // Iterate through headers, adding to both strings
        for (String s : headers) {
            sql = sql + "\t" + s + " TEXT NOT NULL,\n";
            headerString = headerString + s + ",";
        }

        // Properly cap off each string
        sql = sql.substring(0,sql.length()-2) + "\n);";
        headerString = headerString.substring(0,headerString.length()-1) + ")";

        // Execute the creation statement
        conn.createStatement().execute(sql);

        // Return the generated header string.
        return headerString;
    }

    /**
     * This method performs the insertion process by generating the insertion
     * string, iterating over the lines of the file, and inserting the string
     * into the db.
     *
     * @param headerString - The previously generated header string.
     * @throws SQLException - For the insertion process
     */
    public static void insertIntoDB(String headerString) throws SQLException {
        // Begin insertion string
        insertionStmt = "INSERT INTO " + filename + headerString + "\nVALUES";

        // Iterate over the file while there are still lines
        while (fileScanner.hasNext()) {
            // Call the method to parse the line.
            parseLine(fileScanner.nextLine(), headers.size());

            // Print out a status report every 100 entries.
            if (numReceived % 100 == 0) {
                textArea.append(numReceived + "\tRecords Processed.\n");
            }
        }

        textArea.append("Finished processing. Beginning record insertion...\n");

        // Modify and execute the insertion statement.
        insertionStmt = insertionStmt.substring(0,insertionStmt.length()-1) + ";";
        conn.createStatement().execute(insertionStmt);
    }


    /**
     * This method performs the necessary actions to parse a single line of the
     * CSV, including writing to the bad CSV, splitting, and inserting into the
     * database file.
     *
     * @param line - The line from the CSV.
     * @param numHeaders - The number of columns (headers) for the database.
     */
    private static void parseLine(String line, int numHeaders) {
        // Increment number of received records
        numReceived++;

        // Split line
        ArrayList<String> lineParts = customSplit(line);

        // If there are the correct number of entries
        if (lineParts.size() == numHeaders) {
            // Increment the number of successful records
            numSuccessful++;

            // Generate record for insertion
            String sql = "\n\t(";

            // Iterate over parts of line
            for (String s : lineParts) {
                // Add quotations if they are not present
                if (s.substring(0,1).compareTo("\"") != 0) {
                    sql = sql + "\"" + s + "\"" + ",";
                }
                else {
                    sql = sql + s + ",";
                }
            }

            // Modify the end of the string
            insertionStmt = insertionStmt + sql.substring(0,sql.length()-1) + "),";
        }
        // If there are not the correct number of entries
        else {
            // Write the line to the bad CSV file
            badPrintWriter.println(line);
        }
    }

    /**
     * This method performs a custom split of the line. This will split a
     * line based on the location of commas which are not between a set of
     * quotation marks.
     *
     * @see https://mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
     *
     * @param line - The line to be split
     * @return An ArrayList of the atoms of the line.
     */
    private static ArrayList<String> customSplit(String line) {
        // Create expandable list
        ArrayList<String> result = new ArrayList<String>();
        // Create buffer to contain characters
        StringBuffer curVal = new StringBuffer();
        // Determines if we are in quotes or not.
        boolean inQuotes = false;

        // Convert line into iterable array
        char[] chars = line.toCharArray();

        // Iterate over the characters in the line
        for (char ch : chars) {
            if (inQuotes) {
                // Move out of inQuote mode if second quote encountered
                if (ch == '"') {
                    inQuotes = false;
                }
                // Always add the character to the buffer (even quotes)
                curVal.append(ch);
            }
            else {
                // When delimiter encountered, add to list and flush the buffer
                if (ch == ',') {
                    // Only add/flush if the buffer has anything in it
                    if (curVal.length() > 0) {
                        result.add(curVal.toString());
                        curVal = new StringBuffer();
                    }
                }
                else {
                    // Move into inQuote mode for first quote encountered
                    if (ch == '"') {
                        inQuotes = true;
                    }
                    // Only break when end of line encountered
                    else if (ch == '\r' || ch == '\n') {
                        break;
                    }
                    // Always add the character to the buffer (even quotes)
                    curVal.append(ch);
                }
            }
        }

        // Add the last buffer to the list
        result.add(curVal.toString());
        // Return the list
        return result;
    }
}