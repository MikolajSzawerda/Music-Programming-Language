package com.declarative.music.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class ContextManagerTest
{
    private ContextManager tested;

    @BeforeEach
    void init()
    {
        tested = new ContextManager();
    }

    @Test
    void shouldSaveVariable()
    {
        // given
        var varName = "a";
        var value = 10;
        // when
        tested.save(varName, value);

        // then
        var varReference = tested.get(varName).orElseThrow();
        Assertions.assertEquals(varReference.getValue(), value);
    }

    @Test
    void shouldWorkOnReferences()
    {
        // given
        var varName = "a";
        var value = 10;
        // when
        tested.save(varName, value);
        var varReference = tested.get(varName).orElseThrow();
        varReference.setValue(20);
        tested.save(varName, 30);

        // then
        Assertions.assertEquals(varReference.getValue(), 30);
    }

    @Test
    void shouldSaveAndGetVariable_afterEnteringFrame()
    {
        // given
        var varName = "a";
        // when
        tested.save(varName, 10);
        tested.enterNewFrame();
        tested.save(varName, 20);

        // then
        var varReference = tested.get(varName).orElseThrow();
        Assertions.assertEquals(varReference.getValue(), 20);

        tested.leaveFrame();
        varReference = tested.get(varName).orElseThrow();
        Assertions.assertEquals(varReference.getValue(), 10);
    }

    @Test
    void shouldRemoveVariableFromScope_afterLeavingIt()
    {
        // given
        var varName = "a";

        // when
        tested.save(varName, 10);
        tested.enterNewFrame();
        tested.save(varName, 20);
        tested.startNewScope();
        tested.save("b", 30);

        // then
        var varReference = tested.get("b").orElseThrow();
        Assertions.assertEquals(varReference.getValue(), 30);

        tested.leaveNewScope();

        Assertions.assertFalse(tested.contains("b"));
        Assertions.assertEquals(tested.get(varName).orElseThrow().getValue(), 20);
        tested.leaveFrame();

        varReference = tested.get(varName).orElseThrow();
        Assertions.assertEquals(varReference.getValue(), 10);
    }

}