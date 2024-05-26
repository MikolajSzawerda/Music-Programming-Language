package com.declarative.music.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.declarative.music.interpreter.values.VariableReference;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.Declaration;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.type.SimpleType;
import com.declarative.music.parser.production.type.Types;


class ValueInterpreterTest
{
    private ValueInterpreter tested;
    private final Position POS = new Position(0, 0);

    @BeforeEach
    void init()
    {
        tested = new ValueInterpreter();
    }

    @Test
    void shouldDeclareAndAssignValue()
    {
        // given
        var variableName = "a";
        var variableValue = 1;
        var stmt = new Declaration(new SimpleType(Types.Int, POS), variableName, new IntLiteral(variableValue, POS));

        // when
        stmt.accept(tested);
        var frame = tested.getManager().getGlobalFrame();

        // then
        assertEquals(variableValue, frame.getValue(variableName).orElseThrow().getValue());
    }

    @Test
    void shouldDeclareAndAssignExpression()
    {
        // given
        var variableName = "a";
        var expectedValue = 3;
        var stmt = new Declaration(new SimpleType(Types.Int, POS), variableName, new AddExpression(new IntLiteral(1, POS), new IntLiteral(2, POS)));

        // when
        stmt.accept(tested);
        var frame = tested.getManager().getGlobalFrame();

        // then
        assertEquals(frame.getValue(variableName).map(VariableReference::getValue).orElseThrow(), expectedValue);
    }
}