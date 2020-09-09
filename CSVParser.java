import java.util.Scanner;
import java.io.*;

/**
 *
 */
public class CSVParser {

	// numReceived - number of records in the CSV file	
	public static int numReceived = 0;
	
	// numSuccessful - number of successful insertions into db
	public static int numSuccessful = 0;

	// headers - headers in the CSV file
	public static String[] headers;

	// filename - name of the CSV file (w/o file extension)
	public static String filename;

	// fileScanner - Scanner to read in CSV file
	public static Scanner fileScanner;

	// badPrintWriter - Print Writer for bad CSV
	public static PrintWriter badPrintWriter;

	/**
	 *
	 */
	public static void main(String[] args) {
	
		// First check if the user supplied a command line argument	
		if (args.length > 0) {

			try {

				// Try to open file scanner
				fileScanner = new Scanner(new File(args[0]));

				// Generate the filename w/o extension
				filename = args[0].substring(0, args[0].length() - 4);

				try {

					// Generate print writer for bad CSV file
					badPrintWriter = new PrintWriter(filename + "-bad.csv");

				}
				// If somehow file not found, print error message.
				catch (FileNotFoundException e) {

					System.err.println("\n" + e + "\n");

				}

				// Parse the file
				parseFile();

				// Close the bad CSV print writer
				badPrintWriter.close();

			}
			// If file not found, print error message.
			catch (FileNotFoundException e) {

				System.err.println("\n" + e + "\n");

			}

		}
		// If no argument, print an error message with the proper format.
		else {

			System.err.println("\nError: Too few command line arguments. Please provide a file name.\n");
			System.err.println("Format: java CSVParser <CSV-filename>\n");

		}
		
		// Generate log file
		generateLogFile();

	}

	/**
	 *
	 */
	public static void parseFile() {

		// Get headers from first line
		headers = fileScanner.nextLine().split(",");

		// Iterate over the file while there are still lines
		while (fileScanner.hasNext()) {

			parseLine(fileScanner.nextLine());

		}

	}

	/**
	 *
	 */
	public static void parseLine(String line) {

		// Increment number of received records
		numReceived++;
		
		// Split line
		String[] lineParts = line.split(",");

		// If there are the correct number of entries
		if (lineParts.length == headers.length) {

			// Increment the number of successful records
			numSuccessful++;

			// Insert record into database

		}
		// If there are not the correct number of entries
		else {
		
			// Write the line to the bad CSV file
			badPrintWriter.println();

		}

	}

	/**
	 *
	 */
	public static void generateLogFile() {

		// Open a print writer for the log file
		try {

			PrintWriter pw = new PrintWriter(filename + ".log");

			// Write the statistics.
			pw.println("Number of records received: \t" + numReceived);
			pw.println("Number of records successful: \t" + numSuccessful);
			pw.println("Number of records failed: \t\t" + (numReceived - numSuccessful));

			// Close the print writer.
			pw.close();

		}
		// If somehow file not found, print error message.
		catch (FileNotFoundException e) {
				
			System.err.println("\n" + e + "\n");

		}

	}
	
}
