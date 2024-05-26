package com.declarative.music.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.declarative.music.interpreter.values.Variant;


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
        tested.insert(varName, new Variant<>(value, Integer.class));

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
        tested.insert(varName, new Variant<>(value, Integer.class));
        var varReference = tested.get(varName).orElseThrow();
        varReference.setValue(20);
        tested.upsert(varName, new Variant<>(30, Integer.class));

        // then
        Assertions.assertEquals(varReference.getValue(), 30);
    }

    @Test
    void shouldSaveAndGetVariable_afterEnteringFrame()
    {
        // given
        var varName = "a";
        // when
        tested.insert(varName, new Variant<>(10, Integer.class));
        tested.enterNewFrame();
        tested.insert(varName, new Variant<>(20, Integer.class));

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
        tested.insert(varName, new Variant<>(10, Integer.class));
        tested.enterNewFrame();
        tested.insert(varName, new Variant<>(20, Integer.class));
        tested.startNewScope();
        tested.insert("b", new Variant<>(30, Integer.class));

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