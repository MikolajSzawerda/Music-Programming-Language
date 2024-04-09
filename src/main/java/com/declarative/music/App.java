package com.declarative.music;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class App {
    public static void main(final String[] args) throws IOException {
        final var filename = getFilename(args).orElseThrow();
        log.debug("Interpreting file: {}", filename);
        final var lexer = new Lexer(new FileReader(filename));
        Token token;
        while ((token = lexer.getNextToken()).type() != TokenType.T_EOF) {
            log.info("Read: {}", token);
        }
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
