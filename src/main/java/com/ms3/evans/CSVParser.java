package com.ms3.evans;

import com.sun.xml.internal.messaging.saaj.soap.JpegDataContentHandler;

import java.awt.event.ActionListener;
import java.util.Scanner;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.*;
import javax.swing.*;
import java.awt.*;

/**
 * Submission for the MS3 coding challenge. This class takes as a command
 * line input a CSV file and generates an SQLite database file, a bad CSV
 * file, and a log file with statistics.
 *
 * @author Hunter Evans
 * @version 1.0.0
 */
public class CSVParser extends JPanel {

    /**
     * numReceived - number of records in the CSV file
     */
    private static int numReceived = 0;

    /**
     * numSuccessful - number of successful insertions into db
     */
    private static int numSuccessful = 0;

    /**
     * insertionStmt - the final line used to insert valid records
     */
    private static String insertionStmt = "";

    private static JTextArea textArea;

    /**
     * Main method for the class, which launches the parser.
     */
    public static void main(String[] args) {

        JFrame frame = new JFrame();
        textArea = new JTextArea();

        frame.setTitle("CSV to SQLite Database Parser");
        frame.setSize(600,400);
        frame.setLocation(200,200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.getContentPane().add(textArea);

        // Generate Swingx file chooser.
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);

        // Get file from file chooser
        File file = chooser.getSelectedFile();
        textArea.append("You have selected: " + file.getName() + "\n");

        try {
            // Parse file from the chooser.
            parseFile(file);
            textArea.append("Program complete!\n");
        }
        // If file not found, print error message.
        catch (FileNotFoundException e) {
            System.err.println("\n" + e + "\n");
        }

    }

    /**
     * This method executes the actions at a high level needed to parse the
     * file, including setting up scanners and print writers, generating the
     * headers, iterating over the CSV, and writing the log file.
     *
     * @param fileArg - The CSV file
     * @throws FileNotFoundException - For the scanners and print writers
     */
    private static void parseFile(File fileArg) throws FileNotFoundException {

        textArea.append("Beginning file parse...\n");

        // Try to open file scanner
        Scanner fileScanner = new Scanner(fileArg);

        // Generate the filename w/o extension
        String filename = fileArg.getName().substring(0, fileArg.getName().length() - 4);

        // Generate print writer for bad CSV file
        PrintWriter badPrintWriter = new PrintWriter(filename + "-bad.csv");

        // Get headers from first line
        ArrayList<String> headers = customSplit(fileScanner.nextLine());

        try {
            // Create new database file
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + filename + ".db");

            textArea.append("Connection established to database file.\nCreating new table...\n");

            // Create string for table creation
            String sql = "CREATE TABLE IF NOT EXISTS " + filename + "(\n";
            // Header string for insertion (later)
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

            textArea.append("Table successfully created.\nBeginning record processing...\n");

            // Begin insertion string
            insertionStmt = "INSERT INTO " + filename + headerString + "\nVALUES";

            // Iterate over the file while there are still lines
            while (fileScanner.hasNext()) {

                // Call the method to parse the line.
                parseLine(fileScanner.nextLine(), badPrintWriter, headers.size());

                // Print out a status report every 100 entries.
                if (numReceived % 100 == 0) {
                    textArea.append(numReceived + "\tRecords Processed.\n");
                }
            }

            textArea.append("Finished processing. Beginning record insertion...\n");

            // Modify and execute the insertion statement.
            insertionStmt = insertionStmt.substring(0,insertionStmt.length()-1) + ";";
            conn.createStatement().execute(insertionStmt);

            textArea.append("All records successfully inserted. Outputting statistics...\n");

        }
        // Catch any exceptions during creation/insertion.
        catch (SQLException e) {
            System.err.println("\n" + e.getMessage() + "\n");
        }

        // Close the bad CSV print writer
        badPrintWriter.close();

        // Generate a print writer for the log file.
        PrintWriter pw = new PrintWriter(filename + ".log");
        // Write the statistics.
        pw.println("Number of records received: \t" + numReceived);
        pw.println("Number of records successful: \t" + numSuccessful);
        pw.println("Number of records failed: \t\t" + (numReceived - numSuccessful));
        // Close the log file print writer.
        pw.close();

    }
    /**
     * This method performs the necessary actions to parse a single line of the
     * CSV, including writing to the bad CSV, splitting, and inserting into the
     * database file.
     *
     * @param line - The line from the CSV.
     * @param badPW - The print writer for the bad CSV.
     * @param numHeaders - The number of columns (headers) for the database.
     */
    private static void parseLine(String line, PrintWriter badPW, int numHeaders) {

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

            // Iterate over parts of line, adding quotations as necessary.
            for (String s : lineParts) {

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
            badPW.println(line);
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