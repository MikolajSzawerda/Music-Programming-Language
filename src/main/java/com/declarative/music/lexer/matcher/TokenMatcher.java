package com.declarative.music.lexer.matcher;

import com.declarative.music.lexer.token.Token;

import javax.annotation.Nullable;
import java.io.IOException;

public interface TokenMatcher {
    @Nullable
    Token matchNextToken() throws IOException;
}
