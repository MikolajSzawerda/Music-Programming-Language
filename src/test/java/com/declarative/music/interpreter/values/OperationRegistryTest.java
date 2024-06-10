package com.declarative.music.interpreter.values;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class OperationRegistryTest
{
    private OperationRegistry registry;

    @BeforeEach
    void init()
    {
        registry = new OperationRegistry();
    }

    @Test
    void shouldAddIntVariants()
    {
        // given
        var left = new Variant<>(10, Integer.class);
        var right = new Variant<>(20, Integer.class);
        registry.register(Integer.class, Integer.class, Integer::sum, Integer.class);

        // when
        var value = registry.apply("add", left, right);

        // then
        Assertions.assertEquals(30, value.value());
    }

    @Test
    void shouldAddIntReferenceVariants()
    {
        // given
        var left = new Variant<>(new VariableReference<>(10), VariableReference.class);
        var right = new Variant<>(new VariableReference<>(20), VariableReference.class);
        registry.register(Integer.class, Integer.class, Integer::sum, Integer.class);

        // when
        var value = registry.apply("add", left, right);

        // then
        Assertions.assertEquals(30, value.value());
    }

    @Test
    void shouldAddIntReferenceAndIntVariants()
    {
        // given
        var left = new Variant<>(new VariableReference<>(10), VariableReference.class);
        var right = new Variant<>(20, Integer.class);
        registry.register(Integer.class, Integer.class, Integer::sum, Integer.class);

        // when
        var value = registry.apply("add", left, right);

        // then
        Assertions.assertEquals(30, value.value());
    }

}