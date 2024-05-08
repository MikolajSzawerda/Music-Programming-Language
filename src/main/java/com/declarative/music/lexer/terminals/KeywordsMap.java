package com.declarative.music.lexer.terminals;

import com.declarative.music.lexer.token.TokenType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeywordsMap {
    private static final Map<String, TokenType> tokensMapping = Map.ofEntries(
            Map.entry("let", TokenType.T_LET),
            Map.entry("with", TokenType.T_WITH),
            Map.entry("if", TokenType.T_IF),
            Map.entry("else", TokenType.T_ELSE),
            Map.entry("for", TokenType.T_FOR),
            Map.entry("return", TokenType.T_RETURN),
            Map.entry("in", TokenType.T_IN),
            Map.entry("as", TokenType.T_AS),
            Map.entry("lam", TokenType.T_LAMBDA),
            Map.entry("true", TokenType.T_TRUE),
            Map.entry("false", TokenType.T_FALSE)
    );

    public static Optional<TokenType> getKeywordType(final String token) {
        return Optional.ofNullable(tokensMapping.get(token));
    }
}
