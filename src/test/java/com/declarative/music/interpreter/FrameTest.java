package com.declarative.music.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.declarative.music.interpreter.values.IntReference;


class FrameTest
{
    private Frame tested;

    @BeforeEach
    void init()
    {
        tested = new Frame();
    }

    @Test
    void shouldSaveVariable()
    {
        // given
        var varName = "a";
        var value = 10;
        // when
        tested.saveValue(varName, new IntReference(value));

        // then
        var varReference = tested.getValue(varName).orElseThrow();
        Assertions.assertEquals(varReference.getValue(), value);
    }

    @Test
    void shouldSaveAndGetVariable_afterEnteringScope()
    {
        // given
        var varName = "b";
        var value = 10;
        tested.saveValue("a", new IntReference(1));

        // when
        tested.enterScope();
        tested.saveValue(varName, new IntReference(value));

        // then
        Assertions.assertEquals(tested.getValue(varName).orElseThrow().getValue(), value);
        Assertions.assertEquals(tested.getValue("a").orElseThrow().getValue(), 1);

        tested.leaveScope();
        Assertions.assertFalse(tested.contains(varName));
        Assertions.assertEquals(tested.getValue("a").orElseThrow().getValue(), 1);
    }

}