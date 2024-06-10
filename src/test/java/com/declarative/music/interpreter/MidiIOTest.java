package com.declarative.music.interpreter;

import com.declarative.music.lexer.LexerImpl;
import com.declarative.music.parser.Parser;
import com.declarative.music.parser.exception.ParsingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;


public class MidiIOTest {
    @Test
    void shouldExportNotes() throws ParsingException, IOException {
        // given
        final var code = """
                let a =[C, E, G]{dur=q, oct=4};
                let temp = 0 & 1 |2 | 0 & 2 |0;
                let temp2 = 2 & 0 & 1 | 0 | 2 | 0 |0;
                let b = temp | temp & temp2 | temp & temp2;
                let musicA = b>>a;
                let musicB = b >>a |> transpose 2;
                let musicC = musicA | musicB;
                musicC |> export "src/test/resources/song.mid";
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        parser.parserProgram().accept(interpreter);
    }
}
