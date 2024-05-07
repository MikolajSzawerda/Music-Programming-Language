package com.declarative.music.lexer.terminals;

import com.declarative.music.lexer.token.TokenType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeywordsMap {
    private static final Map<String, TokenType> tokensMapping = Map.of(
            "let", TokenType.T_LET,
            "with", TokenType.T_WITH,
            "if", TokenType.T_IF,
            "for", TokenType.T_FOR,
            "return", TokenType.T_RETURN
    );

    public static Optional<TokenType> getKeywordType(final String token) {
        return Optional.ofNullable(tokensMapping.get(token));
    }
}
