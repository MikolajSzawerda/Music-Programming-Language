package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;

import java.io.IOException;

public class IdentifierState extends LexerState {
    private final StringBuilder tokenBuilder;

    public IdentifierState(final LexerContext lexerContext) {
        super(lexerContext);
        tokenBuilder = new StringBuilder();
    }


    @Override
    public Token processNext() throws IOException {
        var currentChar = lexerContext.getCurrentStreamChar();
        while (currentChar != -1 && Character.isLetterOrDigit(currentChar)) {
            tokenBuilder.append((char) currentChar);
            currentChar = lexerContext.getNextStreamChar();
        }
        lexerContext.stateTransition(new IdleState(lexerContext));
        return KeywordsMap.getKeywordType(tokenBuilder.toString())
                .map(keywordType -> new Token(keywordType, new Position(0, 0), null))
                .orElse(new Token(TokenType.T_IDENTIFIER, new Position(0, 0), tokenBuilder.toString()));
    }

}
