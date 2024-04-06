package com.declarative.music.lexer;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.lexer.utils.LexerUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LexerTest {


    @Test
    void shouldReadMultipleTokens() throws IOException {
        // given
        final var code = "a=10";
        final var lexer = new Lexer(new StringReader(code));

        // when
        final var tokens = LexerUtils.getAllTokens(lexer);

        // then
        assertThat(lexer.getNextToken()).isEqualTo(new Token(TokenType.T_EOF, new Position(0, 0), null));
        assertThat(tokens).containsExactlyElementsOf(List.of(
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "a"),
                new Token(TokenType.T_OPERATOR, new Position(0, 0), "="),
                new Token(TokenType.T_NUMBER, new Position(0, 0), 10)
        ));
    }

    @Test
    void shouldHandleNewLine() throws IOException {
        // given
        final var code = """
                               let a=10.23; //Hello world
                                
                                
                let b="20";
                """;
        final var lexer = new Lexer(new StringReader(code));

        // when
        final var tokens = LexerUtils.getAllTokens(lexer);


        // then
        assertThat(lexer.getNextToken()).isEqualTo(new Token(TokenType.T_EOF, new Position(0, 0), null));
        assertThat(tokens).containsExactlyElementsOf(List.of(
                new Token(TokenType.T_LET, new Position(0, 0), null),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "a"),
                new Token(TokenType.T_OPERATOR, new Position(0, 0), "="),
                new Token(TokenType.T_NUMBER, new Position(0, 0), 10.23),
                new Token(TokenType.T_SEMICOLON, new Position(0, 0), null),
                new Token(TokenType.T_COMMENT, new Position(0, 0), "Hello world"),
                new Token(TokenType.T_LET, new Position(0, 0), null),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "b"),
                new Token(TokenType.T_OPERATOR, new Position(0, 0), "="),
                new Token(TokenType.T_STRING, new Position(0, 0), "20"),
                new Token(TokenType.T_SEMICOLON, new Position(0, 0), null)
        ));
    }

    @Test
    void shouldHandleLambdaDefinition() throws IOException {
        // given
        final var code = """
                let fun = with(Note a) -> Note {
                    return (C, 4) q;
                };
                """;
        final var lexer = new Lexer(new StringReader(code));

        // when
        final var tokens = LexerUtils.getAllTokens(lexer);


        // then
        assertThat(lexer.getNextToken()).isEqualTo(new Token(TokenType.T_EOF, new Position(0, 0), null));
        assertThat(tokens).containsExactlyElementsOf(List.of(
                new Token(TokenType.T_LET, new Position(0, 0), null),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "fun"),
                new Token(TokenType.T_OPERATOR, new Position(0, 0), "="),
                new Token(TokenType.T_WITH, new Position(0, 0), null),
                new Token(TokenType.T_L_PARENTHESIS, new Position(0, 0), null),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "Note"),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "a"),
                new Token(TokenType.T_R_PARENTHESIS, new Position(0, 0), null),
                new Token(TokenType.T_OPERATOR, new Position(0, 0), "->"),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "Note"),
                new Token(TokenType.T_L_CURL_PARENTHESIS, new Position(0, 0), null),
                new Token(TokenType.T_RETURN, new Position(0, 0), null),
                new Token(TokenType.T_L_PARENTHESIS, new Position(0, 0), null),
                new Token(TokenType.T_PITCH, new Position(0, 0), "C"),
                new Token(TokenType.T_COMMA, new Position(0, 0), null),
                new Token(TokenType.T_NUMBER, new Position(0, 0), 4),
                new Token(TokenType.T_R_PARENTHESIS, new Position(0, 0), null),
                new Token(TokenType.T_RHYTHM, new Position(0, 0), "q"),
                new Token(TokenType.T_SEMICOLON, new Position(0, 0), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, new Position(0, 0), null),
                new Token(TokenType.T_SEMICOLON, new Position(0, 0), null)
        ));
    }


}