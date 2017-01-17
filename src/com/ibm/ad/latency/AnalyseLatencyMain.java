package com.ibm.ad.latency;

import java.io.File;

// handle size of input and generate multiple files
// Check if creating a csv would not be faster (stream input/ stream output)

public class AnalyseLatencyMain {
	private final static String usage = "java AnalyseLatencyMain <file name> [-hd]";
	private static boolean debugMode = false;
	private static String inputFileDir = null;
	

	public static void main(String[] args) {
		int r = parseCommandLine(args);
		if (r == 0) {
			File latencyInput = new File(inputFileDir);
			if (latencyInput.exists()) {
				AnalyseLatency al = new AnalyseLatency();
				al.analyse(latencyInput);
			} else {
				System.out.println("File does not exist");
			}
		}
	}

	/**
	 * Parse command line
	 * 
	 */
	private static int parseCommandLine(String[] argv) {
		int r = 0;
		for (int i = 0; i < argv.length; i++) {
			if (argv[i].startsWith("-")) { // This an option
				if (argv[i].equalsIgnoreCase("-h") || argv[i].equalsIgnoreCase("-help")) {
					System.out.println(usage);
					return -1;
				} else if (argv[i].equalsIgnoreCase("-debug")) {
					debugMode = true;
					// Cases where we are expecting other arguments
				} else if (argv[i].equalsIgnoreCase("-url") || argv[i].equalsIgnoreCase("-u")) {
					if (isValidArg(argv, i)) {
					} else {
						System.out.println(usage);
						return -1;
					}
				} else {
					System.out.println(usage);
					return -1;
				}
			} else { // If other arguments
				// expect file names
				inputFileDir = argv[i];
			}
		} // end for

		// Do some extra validation
		// Nothing here
		if (debugMode) {
			System.out.println("verbose: " + debugMode);
		}
		return r;
	}
	// check that the file is valid either a file or a directory
	private static boolean isValidArg(String [] argv, int i) {
		File latencyInput = new File(inputFileDir);
		return true;
	}

}
