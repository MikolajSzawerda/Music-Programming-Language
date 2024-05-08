package com.declarative.music;

import com.declarative.music.interpreter.PrintVisitor;
import com.declarative.music.lexer.LexerImpl;
import com.declarative.music.parser.Parser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.FileReader;
import java.util.Optional;


@Slf4j
public class App {
    public static void main(final String[] args) throws Exception {
        final var filename = getFilename(args).orElseThrow();
        log.debug("Interpreting file: {}", filename);
        final var lexer = new LexerImpl(new FileReader(filename));
        final var parser = new Parser(lexer);
        final var program = parser.parserProgram();
        log.info("Program parsed");
        final var printer = new PrintVisitor(System.out);
        program.accept(printer);
    }

    private static Optional<String> getFilename(final String[] args) {
        final var options = new Options();
        options.addOption(new Option("f", true, "filename"));
        try {
            final CommandLine cmd = new DefaultParser().parse(options, args);
            return Optional.of(cmd.getOptionValue("f"));
        } catch (final ParseException e) {
            log.error("Error when parsing arguments", e);
        }
        return Optional.empty();
    }
}
