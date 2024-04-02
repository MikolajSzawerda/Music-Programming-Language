package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class IdleState extends LexerState {

    public IdleState(final LexerContext lexerContext) {
        super(lexerContext);
    }


    @Override
    public Token processNext() throws IOException {
        skipWhites();
        if (lexerContext.getCurrentStreamChar() == -1) {
            return new Token(TokenType.T_EOF, new Position(0, 0), null);
        }
        final var currentStreamChar = (char) lexerContext.getCurrentStreamChar();
        if (Character.isDigit(currentStreamChar)) {
            lexerContext.stateTransition(new NumberState(lexerContext));
        } else if (currentStreamChar == '"') {
            lexerContext.stateTransition(new StringState(lexerContext));
        } else if (currentStreamChar == '/') {
            lexerContext.stateTransition(new CommentOrDivisionState(lexerContext));
        } else if (Character.isLetter(currentStreamChar)) {
            lexerContext.stateTransition(new IdentifierState(lexerContext));
        } else {
            lexerContext.stateTransition(new OperatorOrUnknownState(lexerContext));
        }
        return null;
    }

    private void skipWhites() throws IOException {
        if (lexerContext.getCurrentStreamChar() == -1) {
            lexerContext.getNextStreamChar();
        }
        while (lexerContext.getCurrentStreamChar() != -1) {
            if (!Character.isWhitespace((char) lexerContext.getCurrentStreamChar())) {
                return;
            }
            lexerContext.getNextStreamChar();
        }
    }

}
