package com.declarative.music.lexer;

import com.declarative.music.lexer.token.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringReader;

class LexerStreamReaderTest {

    @Test
    void shouldProvideCorrectChars() throws IOException {
        // given
        final var expectedText = "Hello ";
        final var expectedEndPosition = new Position(0, expectedText.length());
        final var tested = new LexerStreamReader(new StringReader(expectedText));

        // when
        final var textBuilder = new StringBuilder();
        int c = 0;
        while ((c = tested.read()) != -1) {
            textBuilder.append((char) c);
        }
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedText, textBuilder.toString()),
                () -> Assertions.assertEquals(expectedEndPosition, tested.getCurrentPosition())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello\nWorld", "Hello\rWorld", "Hello\r\nWorld"})
    void shouldHandleLineBreaks(final String text) throws IOException {
        // given
        final var expectedText = "Hello\nWorld";
        final var expectedEndPosition = new Position(1, "World".length());
        final var tested = new LexerStreamReader(new StringReader(text));

        // when
        final var textBuilder = new StringBuilder();
        int c = 0;
        while ((c = tested.read()) != -1) {
            textBuilder.append((char) c);
        }

        Assertions.assertEquals(expectedText, textBuilder.toString());
        Assertions.assertEquals(expectedEndPosition, tested.getCurrentPosition());
    }

}