package com.declarative.music.interpreter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.declarative.music.interpreter.values.VariableReference;
import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.AssigmentStatement;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Declaration;
import com.declarative.music.parser.production.IfStatement;
import com.declarative.music.parser.production.Parameter;
import com.declarative.music.parser.production.Parameters;
import com.declarative.music.parser.production.ReturnStatement;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.pipe.InlineFuncCall;
import com.declarative.music.parser.production.expression.pipe.PipeExpression;
import com.declarative.music.parser.production.expression.relation.EqExpression;
import com.declarative.music.parser.production.literal.FloatLiteral;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.literal.StringLiter;
import com.declarative.music.parser.production.type.InferenceType;
import com.declarative.music.parser.production.type.SimpleType;
import com.declarative.music.parser.production.type.Types;


public class InterpretationTest
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
        assertEquals(frame.getValue(variableName).map(com.declarative.music.interpreter.values.VariableReference::getValue).orElseThrow(),
            variableValue);
    }

    @Test
    void shouldAssignValue()
    {
        // given
        var variableName = "a";
        var variableValue = 1;
        var newValue = 2;
        var frame = new ValueFrame(new HashMap<>(Map.of(variableName, new VariableReference<Integer>(variableValue))));
        tested = new ValueInterpreter(new ValueContextManager(frame));
        var stmt = new AssigmentStatement(variableName, new IntLiteral(newValue, POS), POS);

        // when
        stmt.accept(tested);

        // then
        assertEquals(frame.getValue(variableName).map(com.declarative.music.interpreter.values.VariableReference::getValue).orElseThrow(), newValue);
    }

    @Test
    void shouldHandleIfStatement()
    {
        // given
        var variableName = "a";
        var variableValue = 1;
        var newValue = 2;
        var frame = new ValueFrame(new HashMap<>(Map.of(variableName, new VariableReference<Integer>(variableValue))));
        tested = new ValueInterpreter(new ValueContextManager(frame));
        var stmt = new IfStatement(
            new EqExpression(new IntLiteral(1, POS), new IntLiteral(1, POS)),
            new Block(List.of(new AssigmentStatement(variableName, new IntLiteral(newValue, POS), POS)), POS),
            POS
        );

        // when
        stmt.accept(tested);

        // then
        assertEquals(newValue, frame.getValue(variableName).map(VariableReference::getValue).orElseThrow());
    }

    @Test
    void shouldThrow_whenAssigmentToUnknownVariable()
    {
        // given
        var variableName = "a";
        var variableValue = 1;
        var stmt = new AssigmentStatement(variableName, new IntLiteral(variableValue, POS), POS);

        // when
        assertThatThrownBy(() -> stmt.accept(tested))
            .hasMessageStartingWith("INTERPRETATION ERROR");
    }

    @Test
    void shouldThrow_whenAssigmentWithWrongValueType()
    {
        // given
        var variableName = "a";
        var variableValue = 1;
        var newValue = "a";
        var frame = new ValueFrame(new HashMap<>(Map.of(variableName, new VariableReference<Integer>(variableValue))));
        tested = new ValueInterpreter(new ValueContextManager(frame));
        var stmt = new AssigmentStatement(variableName, new StringLiter(newValue, POS), POS);

        // when
        assertThatThrownBy(() -> stmt.accept(tested))
            .hasMessageStartingWith("INTERPRETATION ERROR");
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
        assertEquals(frame.getValue(variableName).map(com.declarative.music.interpreter.values.VariableReference::getValue).orElseThrow(),
            expectedValue);
    }

    @Test
    void shouldThrow_whenInBinaryOperationUncorrectType()
    {
        // given
        var stmt = new AddExpression(new IntLiteral(1, POS), new FloatLiteral(2D, POS));

        // when
        assertThatThrownBy(() -> stmt.accept(tested))
            .hasMessageStartingWith("INTERPRETATION ERROR");
    }

    @Test
    void shouldAssignLambdaDeclaration()
    {
        var variableName = "a";
        var lambda = new LambdaExpression(
            new Parameters(List.of(new Parameter(new SimpleType(Types.Int, POS), "a"))),
            new SimpleType(Types.Int, POS),
            new Block(List.of(new ReturnStatement(new IntLiteral(1, POS), POS)), POS),
            POS
        );

        var stmt = new Declaration(new InferenceType(POS), variableName, lambda);

        stmt.accept(tested);

    }

    @Test
    void shouldHandlePipeExpression()
    {
        // given
        var variableName = "a";
        var lambda = new ValueLambdaClousure(new LambdaExpression(
            new Parameters(List.of(new Parameter(new SimpleType(Types.Int, POS), variableName))),
            new SimpleType(Types.Int, POS),
            new Block(List.of(new ReturnStatement(new com.declarative.music.parser.production.expression.VariableReference(variableName, POS), POS)),
                POS),
            POS
        ), new ValueFrame());
        tested.getManager().insert("fun", new Variant<>(lambda, ValueLambdaClousure.class));
        var stmt = new PipeExpression(new IntLiteral(1, POS), new InlineFuncCall("fun", List.of(), POS));

        // when
        stmt.accept(tested);

        assertEquals(1, ((Variant<Integer>) tested.getCurrentValue()).value());
    }

}
