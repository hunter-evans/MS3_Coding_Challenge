import java.util.Scanner;
import java.io.*;

/**
 *
 */
public class CSVParser {

		// recReceived - number of records in the CSV file	
		public static int recReceived;
		
		// recSuccess - number of successful insertions into db
		public static int recSuccess;

		// numHeaders - number of headers in the CSV file
		public static int numHeaders;

		// fileScanner - Scanner to read in CSV file
		public static Scanner fileScanner;

		/**
		 *
		 */
		public static void main(String[] args) {
		
			// First check if the user supplied a command line argument	
			if (args.length > 0) {

				// Try to open file scanner
				try {

					fileScanner = new Scanner(new File(args[0]));
					System.out.println(fileScanner.nextLine());

					// Parse the file
					parseFile();

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
			
			
			// If open, call parseFile()

			// Close file

			// Write log file
		}

		/**
		 *
		 */
		public static void parseFile() {

			// Get headers from first line

			// For-loop over lines

			// Call parseLine() on each one

		}

		/**
		 *
		 */
		public static void parseLine() {

			// Increment total lines
			
			// Split line

			// If number entries good -> insert, increment success

			// If number entries bad -> write to bad csv
		}

}
