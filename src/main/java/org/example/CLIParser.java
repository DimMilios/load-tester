package org.example;

import org.apache.commons.cli.*;

import java.net.URI;
import java.util.List;

record CLIArgs(int number, int concurrency, URI uri){};
class CLIParser {
    private final String[] args;
    private final Options options;
    private final CommandLineParser parser;

    CLIParser(String[] args) {
        this.args = args;
        this.options = new Options();
        options.addOption("n", "number", true, "Number of requests to run.");
        options.addOption("c", "concurrency", true, "Number of workers to run concurrently.");

        this.parser = new DefaultParser();
    }

    void printHelp() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("load-tester", options);
    }

    CLIArgs getArguments() throws ParseException {
        CommandLine commandLine = parser.parse(options, args);
        int number = Integer.parseInt(commandLine.getOptionValue("n"));
        int concurrency = Integer.parseInt(commandLine.getOptionValue("c"));
        List<String> remainingArgs = commandLine.getArgList();
        URI uri = URI.create(remainingArgs.get(remainingArgs.size() - 1));

        return new CLIArgs(number, concurrency, uri);
    }
}
