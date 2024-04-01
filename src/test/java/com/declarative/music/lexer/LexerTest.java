package com.declarative.music.lexer;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LexerTest {


    @Test
    void shouldReadMultipleTokens() throws IOException {
        // given
        var code = "a=10";
        var lexer = new Lexer(new StringReader(code));

        // when
        List<Token> tokens = new LinkedList<>();
        tokens.add(lexer.getNextToken());
        tokens.add(lexer.getNextToken());
        tokens.add(lexer.getNextToken());
        assertThat(lexer.getNextToken()).isNull();
        assertThat(tokens).containsExactlyElementsOf(List.of(
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "a"),
                new Token(TokenType.T_OPERATOR, new Position(0, 0), '='),
                new Token(TokenType.T_NUMBER, new Position(0, 0), 10)
        ));

        // then
        assertThat(tokens).hasSize(3);
    }

    @Test
    void shouldHandleNewLine() throws IOException {
        // given
        var code = """
                               a=10
                                
                                
                b="20"
                """;
        var lexer = new Lexer(new StringReader(code));

        // when
        List<Token> tokens = new LinkedList<>();
        tokens.add(lexer.getNextToken());
        tokens.add(lexer.getNextToken());
        tokens.add(lexer.getNextToken());
        tokens.add(lexer.getNextToken());
        tokens.add(lexer.getNextToken());
        tokens.add(lexer.getNextToken());
        assertThat(lexer.getNextToken()).isNull();
        assertThat(tokens).containsExactlyElementsOf(List.of(
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "a"),
                new Token(TokenType.T_OPERATOR, new Position(0, 0), '='),
                new Token(TokenType.T_NUMBER, new Position(0, 0), 10),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "b"),
                new Token(TokenType.T_OPERATOR, new Position(0, 0), '='),
                new Token(TokenType.T_STRING, new Position(0, 0), "20")
        ));

        // then
        assertThat(tokens).hasSize(6);
    }


}