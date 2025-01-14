package com.declarative.music.lexer.state;

import com.declarative.music.lexer.LexerContext;
import com.declarative.music.lexer.terminals.OperatorEnum;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;


public class CommentOrDivisionState extends LexerState {
    public CommentOrDivisionState(final LexerContext lexerContext) {
        super(lexerContext);
    }

    @Override
    public Token processNext() throws IOException {
        final var startPosition = lexerContext.getCurrentPosition();
        final var currentChar = lexerContext.getNextStreamChar();
        if ((char) currentChar == '/') {
            lexerContext.getNextStreamChar();
            lexerContext.stateTransition(new CommentState(lexerContext));
            return null;
        }
        lexerContext.stateTransition(new IdleState(lexerContext));
        if ((char) currentChar == '=') {
            lexerContext.getNextStreamChar();
            return new Token(TokenType.T_OPERATOR, startPosition, OperatorEnum.O_DIVIDE_ASSIGN);

        }
        return new Token(TokenType.T_OPERATOR, startPosition, OperatorEnum.O_DIVIDE);
    }

}
