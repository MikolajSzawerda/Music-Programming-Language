package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class OperatorOrUnknownState extends LexerState {
    public OperatorOrUnknownState(final LexerContext lexerContext) {
        super(lexerContext);
    }


    @Override
    public Token processNext() throws IOException {
        final var operator = tryBuildOperator('>', Set.of('=', '>'))
                .or(() -> tryBuildOperator('<', Set.of('|', '=')))
                .or(() -> tryBuildOperator('-', Set.of('>', '=')))
                .or(() -> tryBuildOperator('=', Set.of('=')))
                .or(() -> tryBuildOperator('&', Set.of('&', '=')))
                .or(() -> tryBuildOperator('|', Set.of('>', '|')))
                .or(() -> tryBuildOperator('*', Set.of('=')))
                .or(() -> tryBuildOperator('^', Set.of('=')))
                .or(() -> tryBuildOperator('+', Set.of('=')));
        lexerContext.stateTransition(new IdleState(lexerContext));
        return operator.orElse(null);
    }

    @SneakyThrows
    private Optional<Token> tryBuildOperator(final char first, final Set<Character> lasts) {
        final var currentChar = (char) lexerContext.getCurrentStreamChar();
        if (currentChar == first) {
            final var nextChar = (char) lexerContext.getNextStreamChar();
            String value = String.valueOf(currentChar);
            if (lasts.contains(nextChar)) {
                lexerContext.getNextStreamChar();
                value += nextChar;
            }
            return Optional.of(new Token(TokenType.T_OPERATOR, new Position(0, 0), value));
        }
        return Optional.empty();
    }

}
