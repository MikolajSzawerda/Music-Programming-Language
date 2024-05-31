package com.declarative.music.lexer.state;

import com.declarative.music.lexer.LexerContext;
import com.declarative.music.lexer.expection.UnknownTokenTypeException;
import com.declarative.music.lexer.terminals.OperatorMap;
import com.declarative.music.lexer.terminals.PunctuationMap;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;


public class OperatorOrUnknownState extends LexerState {
    private Position startPosition;

    public OperatorOrUnknownState(final LexerContext lexerContext) {
        super(lexerContext);
    }

    @Override
    public Token processNext() throws IOException {
        startPosition = lexerContext.getCurrentPosition();
        final var operator = tryBuildPunctuation()
                .or(() -> tryBuildOperator('>', Set.of('=', '>')))
                .or(() -> tryBuildOperator('<', Set.of('|', '=')))
                .or(() -> tryBuildOperator('-', Set.of('>', '=')))
                .or(() -> tryBuildOperator('=', Set.of('=')))
                .or(() -> tryBuildOperator('&', Set.of('&', '=')))
                .or(() -> tryBuildOperator('|', Set.of('>', '|', '=')))
                .or(() -> tryBuildOperator('*', Set.of('=')))
                .or(() -> tryBuildOperator('^', Set.of('=')))
                .or(() -> tryBuildOperator('%', Set.of('=')))
                .or(() -> tryBuildOperator('!', Set.of('=')))
                .or(() -> tryBuildOperator('/', Set.of('=')))
                .or(() -> tryBuildOperator('+', Set.of('=')));
        lexerContext.stateTransition(new IdleState(lexerContext));
        return operator.orElseThrow(() -> new UnknownTokenTypeException(lexerContext.getCurrentPosition()));
    }

    private Optional<Token> tryBuildPunctuation() throws IOException {
        final var currentChar = (char) lexerContext.getCurrentStreamChar();
        final var result = PunctuationMap.getPunctuation(currentChar)
                .map(punctuation -> new Token(punctuation, startPosition, null));
        if (result.isPresent()) {
            lexerContext.getNextStreamChar();

        }
        return result;
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
            return Optional.of(new Token(TokenType.T_OPERATOR,
                    startPosition,
                    OperatorMap.getOperator(value).orElseThrow(() -> new UnknownTokenTypeException(lexerContext.getCurrentPosition()))));
        }
        return Optional.empty();
    }

}
