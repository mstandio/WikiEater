package org.openpano.wikieater.tools;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.openpano.wikieater.data.CliData;

/**
 * @author mstandio
 */
public class CliUtils {

	@SuppressWarnings("static-access")
	public CliData parseArguments(String[] args) {

		Option optionHelp = new Option("h", "help", false, "Print this message.");

		Option optionMenuFile = OptionBuilder
				.hasArg()
				.withArgName("file")
				.withDescription(
						"Use given menu file. File lists links used for WikiEater compilation "
								+ "and defines navigation menu structure. Content "
								+ "mimics a subset of MediaWiki syntax. " + "Defaults to ./menu.txt")
				.withLongOpt("menufile").create("f");

		Option optionOutput = OptionBuilder.hasArg().withArgName("directory")
				.withDescription("Choose output directory. " + "Defaults to ./output ").withLongOpt("output")
				.create("o");

		Option optionCache = OptionBuilder.hasArg().withArgName("directory")
				.withDescription("Choose cache directory. " + "Defaults to ./cache ").withLongOpt("cache").create("c");

		Option optionRefresh = new Option("r", "refresh", false, "Remove cache before start.");

		Options options = new Options();
		options.addOption(optionHelp);
		options.addOption(optionMenuFile);
		options.addOption(optionOutput);
		options.addOption(optionCache);
		options.addOption(optionRefresh);

		CliData cliData = new CliData();

		try {
			CommandLineParser commandLineParser = new PosixParser();
			CommandLine commandLine = commandLineParser.parse(options, args);
			if (commandLine.hasOption("h")) {
				cliData.showHelp = true;
			}
			if (commandLine.hasOption("f")) {
				cliData.menuFile = commandLine.getOptionValue("f");
			}
			if (commandLine.hasOption("o")) {
				cliData.outputDir = commandLine.getOptionValue("o");
			}
			if (commandLine.hasOption("c")) {
				cliData.cacheDir = commandLine.getOptionValue("c");
			}
			if (commandLine.hasOption("r")) {
				cliData.refreshCache = true;
			}
		} catch (ParseException e) {
			System.out.println("Invalid command: " + e.getMessage());
			cliData.showHelp = true;
		} finally {
			if (cliData.showHelp) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("WikiEater [-option] [file|directory]", options);
			}
		}
		return cliData;
	}
}
