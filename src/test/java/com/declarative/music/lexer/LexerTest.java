package com.declarative.music.lexer;

import com.declarative.music.lexer.terminals.OperatorEnum;
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
        final var lexer = new LexerImpl(new StringReader(code));

        // when
        final var tokens = LexerUtils.getAllTokens(lexer);

        // then
        assertThat(lexer.getNextToken()).isEqualTo(new Token(TokenType.T_EOF, new Position(0, 4), null));
        assertThat(tokens).containsExactlyElementsOf(List.of(
                new Token(TokenType.T_IDENTIFIER, new Position(0, 0), "a"),
                new Token(TokenType.T_OPERATOR, new Position(0, 1), OperatorEnum.O_ASSIGN),
                new Token(TokenType.T_INT_NUMBER, new Position(0, 2), 10)
        ));
    }

    @Test
    void shouldHandleNewLine() throws IOException {
        // given
        final var code = """
                   let a=10.23; //Hello world
                                
                                
                let b="20";
                """;
        final var lexer = new LexerImpl(new StringReader(code));

        // when
        final var tokens = LexerUtils.getAllTokens(lexer);


        // then
        assertThat(lexer.getNextToken()).isEqualTo(new Token(TokenType.T_EOF, new Position(4, 0), null));
        assertThat(tokens).containsExactlyElementsOf(List.of(
                new Token(TokenType.T_LET, new Position(0, 3), null),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 7), "a"),
                new Token(TokenType.T_OPERATOR, new Position(0, 8), OperatorEnum.O_ASSIGN),
                new Token(TokenType.T_FLOATING_NUMBER, new Position(0, 9), 10.23),
                new Token(TokenType.T_SEMICOLON, new Position(0, 14), null),
                new Token(TokenType.T_COMMENT, new Position(0, 18), "Hello world"),
                new Token(TokenType.T_LET, new Position(3, 0), null),
                new Token(TokenType.T_IDENTIFIER, new Position(3, 4), "b"),
                new Token(TokenType.T_OPERATOR, new Position(3, 5), OperatorEnum.O_ASSIGN),
                new Token(TokenType.T_STRING, new Position(3, 6), "20"),
                new Token(TokenType.T_SEMICOLON, new Position(3, 10), null)
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
        final var lexer = new LexerImpl(new StringReader(code));

        // when
        final var tokens = LexerUtils.getAllTokens(lexer);


        // then
        assertThat(lexer.getNextToken()).isEqualTo(new Token(TokenType.T_EOF, new Position(3, 0), null));
        assertThat(tokens).containsExactlyElementsOf(List.of(
                new Token(TokenType.T_LET, new Position(0, 0), null),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 4), "fun"),
                new Token(TokenType.T_OPERATOR, new Position(0, 8), OperatorEnum.O_ASSIGN),
                new Token(TokenType.T_WITH, new Position(0, 10), null),
                new Token(TokenType.T_L_PARENTHESIS, new Position(0, 14), null),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 15), "Note"),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 20), "a"),
                new Token(TokenType.T_R_PARENTHESIS, new Position(0, 21), null),
                new Token(TokenType.T_OPERATOR, new Position(0, 23), OperatorEnum.O_ARROW),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 26), "Note"),
                new Token(TokenType.T_L_CURL_PARENTHESIS, new Position(0, 31), null),
                new Token(TokenType.T_RETURN, new Position(1, 4), null),
                new Token(TokenType.T_L_PARENTHESIS, new Position(1, 11), null),
                new Token(TokenType.T_PITCH, new Position(1, 12), "C"),
                new Token(TokenType.T_COMMA, new Position(1, 13), null),
                new Token(TokenType.T_INT_NUMBER, new Position(1, 15), 4),
                new Token(TokenType.T_R_PARENTHESIS, new Position(1, 16), null),
                new Token(TokenType.T_RHYTHM, new Position(1, 18), "q"),
                new Token(TokenType.T_SEMICOLON, new Position(1, 19), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, new Position(2, 0), null),
                new Token(TokenType.T_SEMICOLON, new Position(2, 1), null)
        ));
    }

    @Test
    void shouldParseMathExpression() throws IOException {
        // given
        final var code = "-(4^20.0 )/  2+ 4*7 |> a";
        final var lexer = new LexerImpl(new StringReader(code));

        // when
        final var tokens = LexerUtils.getAllTokens(lexer);

        // then
        assertThat(lexer.getNextToken()).isEqualTo(new Token(TokenType.T_EOF, new Position(0, 24), null));
        assertThat(tokens).containsExactlyElementsOf(List.of(
                new Token(TokenType.T_OPERATOR, new Position(0, 0), OperatorEnum.O_MINUS),
                new Token(TokenType.T_L_PARENTHESIS, new Position(0, 1), null),
                new Token(TokenType.T_INT_NUMBER, new Position(0, 2), 4),
                new Token(TokenType.T_OPERATOR, new Position(0, 3), OperatorEnum.O_POW),
                new Token(TokenType.T_FLOATING_NUMBER, new Position(0, 4), 20.0),
                new Token(TokenType.T_R_PARENTHESIS, new Position(0, 9), null),
                new Token(TokenType.T_OPERATOR, new Position(0, 10), OperatorEnum.O_DIVIDE),
                new Token(TokenType.T_INT_NUMBER, new Position(0, 13), 2),
                new Token(TokenType.T_OPERATOR, new Position(0, 14), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, new Position(0, 16), 4),
                new Token(TokenType.T_OPERATOR, new Position(0, 17), OperatorEnum.O_MUL),
                new Token(TokenType.T_INT_NUMBER, new Position(0, 18), 7),
                new Token(TokenType.T_OPERATOR, new Position(0, 20), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, new Position(0, 23), "a")
        ));
    }


}