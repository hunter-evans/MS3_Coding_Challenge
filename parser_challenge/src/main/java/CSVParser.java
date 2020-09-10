package com.MS3.app;

import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;

import org.sqlite.JDBC;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

/**
 * Submission for the MS3 coding challenge. This class takes as a command
 * line input a CSV file and generates an SQLite database file, a bad CSV
 * file, and a log file with statistics.
 *
 * @author Hunter Evans
 * @version 1.0.0
 */
public class CSVParser {

	/**
	 * numReceived - number of records in the CSV file	
	 */
	public static int numReceived = 0;
	
	/**
	 * numSuccessful - number of successful insertions into db
	 */
	public static int numSuccessful = 0;

	/**
	 * Main method for the class. Performs command line input sanitation and
	 * launches the parser.
	 *
	 * @param args - The command line arguments.
	 */
	public static void main(String[] args) {

		try {
			// try {
			// 	Class.forName("org.sqlite.JDBC");
			// } catch (ClassNotFoundException e) {
			// 	System.err.println(e);
			// }

			Connection conn = DriverManager.getConnection("jdbc:sqlite:sample.db");
			DatabaseMetaData meta = conn.getMetaData();
			System.out.println("The driver name is " + meta.getDriverName());
			System.out.println("A new database has been created.");

		}
		catch (SQLException e) {
			System.err.println("Caught exception");
			System.err.println("\n" + e.getMessage() + "\n");
		}
	
		// First check if the user supplied a command line argument	
		if (args.length > 0) {
			// Next check if supplied a CSV file (i.e. the file name ends .csv)
			if(args[0].substring(args[0].length() - 4).compareTo(".csv") == 0) {

				try {

					// Parse file only if sanity checks pass
					parseFile(args[0]);

				}
				// If file not found, print error message.
				catch (FileNotFoundException e) {

					System.err.println("\n" + e + "\n");

				}

			}
			// If CSV not supplied, print error message.
			else {

				System.err.println("\nError: CSV file not supplied (missing .csv extension).\n");

			}

		}
		// If no argument, print an error message with the proper format.
		else {

			System.err.println("\nError: Too few command line arguments. Please provide a file name.\n");
			System.err.println("Format: java CSVParser <CSV-filename>\n");

		}
		
	}

	/**
	 * This method executes the actions at a high level needed to parse the
	 * file, including setting up scanners and print writers, generating the
	 * headers, iterating over the CSV, and writing the log file.
	 *
	 * @param fileArg - The name of the file
	 * @throws FileNotFoundException - For the scanners and print writers
	 */
	public static void parseFile(String fileArg) throws FileNotFoundException {

		// Try to open file scanner
		Scanner fileScanner = new Scanner(new File(fileArg));

		// Generate the filename w/o extension
		String filename = fileArg.substring(0, fileArg.length() - 4);

		// Generate print writer for bad CSV file
		PrintWriter badPrintWriter = new PrintWriter(filename + "-bad.csv");

		// Get headers from first line
		ArrayList<String> headers = customSplit(fileScanner.nextLine());

		// TODO: Setup db file writer with headers

		// Iterate over the file while there are still lines
		while (fileScanner.hasNext()) {

			parseLine(fileScanner.nextLine(), badPrintWriter, headers.size());

		}

		// Close the bad CSV print writer
		badPrintWriter.close();
		
		// Generate a print writer for the log file.
		PrintWriter pw = new PrintWriter(filename + ".log");

		// Write the statistics.
		pw.println("Number of records received: \t" + numReceived);
		pw.println("Number of records successful: \t" + numSuccessful);
		pw.println("Number of records failed: \t\t" + (numReceived - numSuccessful));

		// Close the print writer.
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
	public static void parseLine(String line, PrintWriter badPW, int numHeaders) {

		// Increment number of received records
		numReceived++;
		
		// Split line
		ArrayList<String> lineParts = customSplit(line);

		// If there are the correct number of entries
		if (lineParts.size() == numHeaders) {

			// Increment the number of successful records
			numSuccessful++;

			// TODO: Insert record into database

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
	public static ArrayList<String> customSplit(String line) {

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
