package com.declarative.music.lexer.terminals;

import com.declarative.music.lexer.token.TokenType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PunctuationMap {
    private static final Map<Character, TokenType> punctuationMap = Map.of(
            '(', TokenType.T_L_PARENTHESIS,
            ')', TokenType.T_R_PARENTHESIS,
            '{', TokenType.T_L_CURL_PARENTHESIS,
            '}', TokenType.T_R_CURL_PARENTHESIS,
            '[', TokenType.T_L_QAD_PARENTHESIS,
            ']', TokenType.T_R_QAD_PARENTHESIS,
            ';', TokenType.T_SEMICOLON,
            ',', TokenType.T_COMMA
    );

    public static Optional<TokenType> getPunctuation(final char currentChar) {
        return Optional.ofNullable(punctuationMap.get(currentChar));
    }
}
