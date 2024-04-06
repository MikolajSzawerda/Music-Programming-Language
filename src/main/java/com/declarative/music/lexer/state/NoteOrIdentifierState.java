package com.declarative.music.lexer.state;

import com.declarative.music.lexer.token.Token;

import java.io.IOException;
import java.util.Set;

public class NoteOrIdentifierState extends LexerState {

    private final static Set<Character> pitchFirsts = Set.of(
            'A', 'B', 'C', 'D', 'E', 'F', 'G'
    );
    private final static Set<Character> rythmFirsts = Set.of(
            'd', 'l', 'w', 'q', 'e', 's', 't', 'h'
    );

    public NoteOrIdentifierState(final LexerContext lexerContext) {
        super(lexerContext);
    }

    @Override
    public Token processNext() throws IOException {
        final var currentChar = (char) lexerContext.getCurrentStreamChar();
        if (pitchFirsts.contains(currentChar)) {
            lexerContext.stateTransition(new PitchState(lexerContext));
            return null;
        }
        if (rythmFirsts.contains(currentChar)) {
            lexerContext.stateTransition(new RythmState(lexerContext));
            return null;
        }
        lexerContext.stateTransition(new IdentifierState(lexerContext));
        return null;


    }
}
