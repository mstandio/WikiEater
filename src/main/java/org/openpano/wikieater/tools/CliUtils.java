package org.openpano.wikieater.tools;

import org.openpano.wikieater.data.CliData;

/**
 * @author mstandio
 */
public class CliUtils {

	public static final String help = "" 
			+ "-help, -h      Print this message." 
			+ "-menufile, -f  Use given menu file. "
			+ "                 File lists links used for WikiEater compilation"
			+ "                 and defines navigation menu structure. Content"
			+ "                 mimics small subset of mediawiki syntax."
			+ "                 Defaults to \"menu.txt\" in running directory."
			+ "-output, -o    Choose output directory. " 
			+ "                 Defaults to running directory." 
			+ "-cache, -c     Choose cache directory. "
			+ "                 Defaults to running directory"
			+ "-refresh       Remove cache before start.";

	public CliData parseArguments(String[] args) {
		CliData cliData = new CliData();
		cliData.menuFile = "./files/menu.txt";
		cliData.directoryCache = "./files/cache";
		cliData.directoryOutput = "./files/output";
		return cliData;
	}
}
