package com.declarative.music.interpreter;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.declarative.music.lexer.LexerImpl;
import com.declarative.music.parser.Parser;
import com.declarative.music.parser.exception.ParsingException;


public class MidiIOTest
{
    @Test
    void shouldExportNotes() throws ParsingException, IOException
    {
        // given
        final var code = """
            let a =[C, E, G]{dur=q, oct=4};
            let temp = 0 & 1 |2 | 0 & 2 |0;
            let temp2 = 2 & 0 & 1 | 0 | 2 | 0 |0;
            let b = temp | temp & temp2 | temp & temp2;
            b >> a |> export "song.mid";
            """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        parser.parserProgram().accept(interpreter);
    }
}
