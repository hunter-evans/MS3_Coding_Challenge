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

		/**
		 *
		 */
		public static void main(String[] args) {
			
			// Get file (command line? GUI?)
			
			// Try catch file opening
			
			// If open, call parseFile()

			// Else, print error message

			// Close file

			// Write log file
		}

		/**
		 *
		 */
		public static parseFile() {

			// Get headers from first line

			// For-loop over lines

			// Call parseLine() on each one

		}

		/**
		 *
		 */
		public static parseLine() {

			// Increment total lines
			
			// Split line

			// If number entries good -> insert, increment success

			// If number entries bad -> write to bad csv
		}

}
