package com.declarative.music.interpreter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.values.LambdaClousure;
import com.declarative.music.interpreter.values.VariableReference;
import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.Pitch;
import com.declarative.music.interpreter.values.music.Rythm;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.AssigmentStatement;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Declaration;
import com.declarative.music.parser.production.IfStatement;
import com.declarative.music.parser.production.Parameter;
import com.declarative.music.parser.production.Parameters;
import com.declarative.music.parser.production.ReturnStatement;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.array.ArrayExpression;
import com.declarative.music.parser.production.expression.array.ListComprehension;
import com.declarative.music.parser.production.expression.array.RangeExpression;
import com.declarative.music.parser.production.expression.lambda.FunctionCall;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.music.NoteExpression;
import com.declarative.music.parser.production.expression.music.ParallerExpression;
import com.declarative.music.parser.production.expression.music.SequenceExpression;
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
    private Executor tested;
    private static final Position POS = new Position(0, 0);

    private static SequenceExpression provideSequenceExpression()
    {
        return new SequenceExpression(
            new NoteExpression("C", new IntLiteral(1, POS), "q", POS),
            new NoteExpression("E", new IntLiteral(1, POS), "q", POS)
        );
    }

    private static SequenceNode<Note> provideNoteSequence()
    {
        return new SequenceNode<>(List.of(
            new Note(Pitch.C, 1, Rythm.q),
            new Note(Pitch.E, 1, Rythm.q)
        ));
    }

    private static ParallerExpression provideParallerExpression()
    {
        return new ParallerExpression(
            new NoteExpression("C", new IntLiteral(1, POS), "q", POS),
            new NoteExpression("E", new IntLiteral(1, POS), "q", POS)
        );
    }

    private static GroupNode<Note> provideChord()
    {
        return new GroupNode<>(new ArrayList<>(List.of(
            new Note(Pitch.C, 1, Rythm.q),
            new Note(Pitch.E, 1, Rythm.q)
        )));
    }

    private static Stream<Arguments> provideMusicExpressions()
    {
        return Stream.of(
            Arguments.of(provideSequenceExpression(), provideNoteSequence()),
            Arguments.of(provideParallerExpression(), provideChord()),
            Arguments.of(new SequenceExpression(
                provideParallerExpression(),
                new NoteExpression("C", new IntLiteral(1, POS), "q", POS)
            ), new SequenceNode<Note>(List.of(provideChord(), new Note(Pitch.C, 1, Rythm.q))))
        );
    }

    @BeforeEach
    void init()
    {
        tested = new Executor();
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
        var frame = new Frame(new HashMap<>(Map.of(variableName, new VariableReference<Integer>(variableValue))));
        tested = new Executor(new ContextManager(frame));
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
        var frame = new Frame(new HashMap<>(Map.of(variableName, new VariableReference<Integer>(variableValue))));
        tested = new Executor(new ContextManager(frame));
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
        var frame = new Frame(new HashMap<>(Map.of(variableName, new VariableReference<Integer>(variableValue))));
        tested = new Executor(new ContextManager(frame));
        var stmt = new AssigmentStatement(variableName, new StringLiter(newValue, POS), POS);

        // when
        assertThatThrownBy(() -> stmt.accept(tested))
            .hasMessageStartingWith("INTERPRETATION ERROR required Integer provided String");
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
        var lambda = new LambdaClousure(new LambdaExpression(
            new Parameters(List.of(new Parameter(new SimpleType(Types.Int, POS), variableName))),
            new SimpleType(Types.Int, POS),
            new Block(List.of(new ReturnStatement(new com.declarative.music.parser.production.expression.VariableReference(variableName, POS), POS)),
                POS),
            POS
        ), new Frame());
        tested.getManager().insert("fun", new Variant<>(lambda, LambdaClousure.class));
        var stmt = new PipeExpression(new IntLiteral(1, POS), new InlineFuncCall("fun", List.of(), POS));

        // when
        stmt.accept(tested);

        assertEquals(1, ((Variant<Integer>) tested.getCurrentValue()).value());
    }

    @Test
    void shouldHandleArrayExpression()
    {
        // given
        var stmt = new ArrayExpression(List.of(
            new IntLiteral(1, POS),
            new IntLiteral(2, POS),
            new com.declarative.music.parser.production.expression.VariableReference("a", POS)
        ), POS);
        tested.getManager().insert("a", new Variant<>(1, Integer.class));

        // when
        stmt.accept(tested);

        // then
        assertThat(tested.getCurrentValue().value())
            .isEqualToComparingFieldByFieldRecursively(List.of(
                new Variant<>(1, Integer.class),
                new Variant<>(2, Integer.class),
                new Variant<>(new VariableReference<>(1), VariableReference.class)
            ));
    }

    @Test
    void shouldHandleListComprehension_WithArrayIterable()
    {
        // given
        var stmt = new ListComprehension(
            new AddExpression(new com.declarative.music.parser.production.expression.VariableReference("x", POS), new IntLiteral(2, POS)),
            new com.declarative.music.parser.production.expression.VariableReference("x", POS),
            new ArrayExpression(List.of(
                new IntLiteral(1, POS),
                new IntLiteral(2, POS),
                new com.declarative.music.parser.production.expression.VariableReference("a", POS)
            ), POS), POS);
        tested.getManager().insert("a", new Variant<>(10, Integer.class));

        // when
        stmt.accept(tested);

        // then
        assertThat(tested.getCurrentValue().value())
            .isEqualToComparingFieldByFieldRecursively(List.of(
                new Variant<>(3, Integer.class),
                new Variant<>(4, Integer.class),
                new Variant<>(12, Integer.class)
            ));
    }

    @Test
    void shouldHandleListComprehension()
    {
        // given
        var stmt = new ListComprehension(
            new AddExpression(new com.declarative.music.parser.production.expression.VariableReference("x", POS), new IntLiteral(2, POS)),
            new com.declarative.music.parser.production.expression.VariableReference("x", POS),
            new RangeExpression(new IntLiteral(1, POS), new IntLiteral(5, POS)), POS);

        // when
        stmt.accept(tested);

        // then
        assertThat(tested.getCurrentValue().value())
            .isEqualToComparingFieldByFieldRecursively(List.of(
                new Variant<>(3, Integer.class),
                new Variant<>(4, Integer.class),
                new Variant<>(5, Integer.class),
                new Variant<>(6, Integer.class)
            ));
    }

    @ParameterizedTest
    @MethodSource("provideMusicExpressions")
    void shouldHandleMusicExpressions(Expression musicExpression, Node<Note> expectedTree)
    {
        // when
        musicExpression.accept(tested);

        // then
        assertThat(tested.getCurrentValue().value()).isEqualToComparingFieldByFieldRecursively(expectedTree);
    }

    @Test
    void shouldHandleAtBuiltInMethod()
    {
        // given
        var atFuncCall = new FunctionCall("at", List.of(
            new com.declarative.music.parser.production.expression.VariableReference("arr", POS),
            new IntLiteral(1, POS)
        ), POS);
        tested.getManager().insert("arr", new Variant<>(List.of(
            new Variant<>(1, Integer.class),
            new Variant<>(2, Integer.class),
            new Variant<>(3, Integer.class)
        ), List.class));

        // when
        atFuncCall.accept(tested);

        // then
        assertThat(tested.getCurrentValue()).isEqualTo(new Variant<>(2, Integer.class));
    }

}
