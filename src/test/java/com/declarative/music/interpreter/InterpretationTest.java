package com.declarative.music.interpreter;

import com.declarative.music.interpreter.values.LambdaClousure;
import com.declarative.music.interpreter.values.VariableReference;
import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.interpreter.values.music.MusicTree;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.Pitch;
import com.declarative.music.interpreter.values.music.Rythm;
import com.declarative.music.interpreter.values.template.IndexTree;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.assign.*;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.arithmetic.MinusUnaryExpression;
import com.declarative.music.parser.production.expression.arithmetic.PlusUnaryExpression;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class InterpretationTest {
    private Executor tested;
    private static final Position POS = new Position(0, 0);
    private static final String VAR_NAME = "testVar";

    private static Stream<Arguments> provideBinaryAssigments() {
        return Stream.of(
                Arguments.of(new Variant<>(1, Integer.class), new PlusAssignStatement(VAR_NAME, new IntLiteral(1, POS), POS), new Variant<>(2, Integer.class)),
                Arguments.of(new Variant<>(2, Integer.class), new DivAssignStatement(VAR_NAME, new IntLiteral(2, POS), POS), new Variant<>(1, Integer.class)),
                Arguments.of(new Variant<>(5, Integer.class), new ModuloAssignStatement(VAR_NAME, new IntLiteral(2, POS), POS), new Variant<>(1, Integer.class)),
                Arguments.of(new Variant<>(2, Integer.class), new MulAssignStatement(VAR_NAME, new IntLiteral(2, POS), POS), new Variant<>(4, Integer.class)),
                Arguments.of(new Variant<>(2, Integer.class), new PowAssignStatement(VAR_NAME, new IntLiteral(2, POS), POS), new Variant<>(4, Integer.class)),
                Arguments.of(
                        new Variant<>(new MusicTree()
                                .appendToSequence(createNote(Pitch.C)), MusicTree.class),
                        new SequenceAssignStatement(
                                VAR_NAME,
                                new NoteExpression("E", new IntLiteral(4, POS), "q", POS),
                                POS
                        ),
                        new Variant<>(new MusicTree()
                                .appendToSequence(createNote(Pitch.C))
                                .appendToSequence(createNote(Pitch.E)), MusicTree.class)),
                Arguments.of(
                        new Variant<>(new MusicTree()
                                .appendToSequence(createNote(Pitch.C)), MusicTree.class),
                        new ParalerAssignStatement(
                                VAR_NAME,
                                new NoteExpression("E", new IntLiteral(4, POS), "q", POS),
                                POS
                        ),
                        new Variant<>(new MusicTree()
                                .appendToGroup(createNote(Pitch.C))
                                .appendToGroup(createNote(Pitch.E)), MusicTree.class)),
                Arguments.of(new Variant<>(1, Integer.class), new MinusAssignStatement(VAR_NAME, new IntLiteral(1, POS), POS), new Variant<>(0, Integer.class))
        );
    }

    public static Stream<Arguments> provideNoteExpressions() {
        return Stream.of(
                //C | E
                Arguments.of(new SequenceExpression(
                                createNoteExpression("C"),
                                createNoteExpression("E")
                        ), new Variant<>(
                                new MusicTree()
                                        .appendToSequence(createNote(Pitch.C))
                                        .appendToSequence(createNote(Pitch.E)), MusicTree.class)
                ),
                // C | (E | G)
                Arguments.of(new SequenceExpression(
                                createNoteExpression("C"),
                                new SequenceExpression(
                                        createNoteExpression("E"),
                                        createNoteExpression("G")
                                )
                        ), new Variant<>(
                                new MusicTree()
                                        .appendToSequence(createNote(Pitch.C))
                                        .appendToSequence(createNote(Pitch.E))
                                        .appendToSequence(createNote(Pitch.G)), MusicTree.class)
                ),
                //C & (E & G)
                Arguments.of(new ParallerExpression(
                                createNoteExpression("C"),
                                new ParallerExpression(
                                        createNoteExpression("E"),
                                        createNoteExpression("G")
                                )
                        ), new Variant<>(
                                new MusicTree()
                                        .appendToGroup(createNote(Pitch.C))
                                        .appendToGroup(createNote(Pitch.E))
                                        .appendToGroup(createNote(Pitch.G)), MusicTree.class)
                ),
                //(C | E) & G
                Arguments.of(new ParallerExpression(
                                new SequenceExpression(
                                        createNoteExpression("C"),
                                        createNoteExpression("E")
                                ),
                                createNoteExpression("G")

                        ), new Variant<>(
                                new MusicTree()
                                        .appendToGroup(
                                                new MusicTree()
                                                        .appendToSequence(createNote(Pitch.C))
                                                        .appendToSequence(createNote(Pitch.E))
                                        )
                                        .appendToGroup(createNote(Pitch.G)), MusicTree.class)
                ),
                //C | E & G
                Arguments.of(new SequenceExpression(

                                createNoteExpression("C"),
                                new ParallerExpression(
                                        createNoteExpression("E"),
                                        createNoteExpression("G")
                                )

                        ), new Variant<>(
                                new MusicTree()
                                        .appendToSequence(createNote(Pitch.C))
                                        .appendToSequence(
                                                new MusicTree()
                                                        .appendToGroup(createNote(Pitch.E))
                                                        .appendToGroup(createNote(Pitch.G))
                                        )
                                , MusicTree.class)
                ),
                //(C | E) & (G | E)
                Arguments.of(new ParallerExpression(

                                new SequenceExpression(
                                        createNoteExpression("C"),
                                        createNoteExpression("E")
                                ),
                                new SequenceExpression(
                                        createNoteExpression("G"),
                                        createNoteExpression("E")
                                )

                        ), new Variant<>(
                                new MusicTree()
                                        .appendToSequence(
                                                new MusicTree()
                                                        .appendToGroup(createNote(Pitch.C))
                                                        .appendToGroup(createNote(Pitch.E))
                                        )
                                        .appendToSequence(
                                                new MusicTree()
                                                        .appendToGroup(createNote(Pitch.G))
                                                        .appendToGroup(createNote(Pitch.E))
                                        )
                                , MusicTree.class)
                ),
                //C & E | G & E
                Arguments.of(new SequenceExpression(

                                new ParallerExpression(
                                        createNoteExpression("C"),
                                        createNoteExpression("E")
                                ),
                                new ParallerExpression(
                                        createNoteExpression("G"),
                                        createNoteExpression("E")
                                )

                        ), new Variant<>(
                                new MusicTree()
                                        .appendToGroup(
                                                new MusicTree()
                                                        .appendToSequence(createNote(Pitch.C))
                                                        .appendToSequence(createNote(Pitch.E))
                                        )
                                        .appendToGroup(
                                                new MusicTree()
                                                        .appendToSequence(createNote(Pitch.G))
                                                        .appendToSequence(createNote(Pitch.E))
                                        )
                                , MusicTree.class)
                ),
                //C & E
                Arguments.of(new ParallerExpression(
                                createNoteExpression("C"),
                                createNoteExpression("E")
                        ), new Variant<>(
                                new MusicTree()
                                        .appendToGroup(createNote(Pitch.C))
                                        .appendToGroup(createNote(Pitch.E)), MusicTree.class)
                )
        );
    }

    private static Stream<Arguments> provideIndexExpressions() {
        return Stream.of(
                //0 | 1
                Arguments.of(new SequenceExpression(
                                new IntLiteral(0, POS),
                                new IntLiteral(1, POS)
                        ), new Variant<>(
                                new IndexTree()
                                        .appendToSequence(0)
                                        .appendToSequence(1), IndexTree.class)
                ),
                // 0 | (1 | 2)
                Arguments.of(new SequenceExpression(
                                new IntLiteral(0, POS),
                                new SequenceExpression(
                                        new IntLiteral(1, POS),
                                        new IntLiteral(2, POS)
                                )
                        ), new Variant<>(
                                new IndexTree()
                                        .appendToSequence(0)
                                        .appendToSequence(1)
                                        .appendToSequence(2), IndexTree.class)
                ),
                //0 & (1 & 2)
                Arguments.of(new ParallerExpression(
                                new IntLiteral(0, POS),
                                new ParallerExpression(
                                        new IntLiteral(1, POS),
                                        new IntLiteral(2, POS)
                                )
                        ), new Variant<>(
                                new IndexTree()
                                        .appendToGroup(0)
                                        .appendToGroup(1)
                                        .appendToGroup(2), IndexTree.class)
                ),
                //(C | E) & (G | E)
                Arguments.of(new ParallerExpression(

                                new SequenceExpression(
                                        new IntLiteral(0, POS),
                                        new IntLiteral(1, POS)
                                ),
                                new SequenceExpression(
                                        new IntLiteral(2, POS),
                                        new IntLiteral(3, POS)
                                )

                        ), new Variant<>(
                                new IndexTree()
                                        .appendToSequence(
                                                new IndexTree()
                                                        .appendToGroup(0)
                                                        .appendToGroup(1)
                                        )
                                        .appendToSequence(
                                                new IndexTree()
                                                        .appendToGroup(2)
                                                        .appendToGroup(3)
                                        )
                                , IndexTree.class)
                ),
                //0 & 1
                Arguments.of(new ParallerExpression(
                                new IntLiteral(0, POS),
                                new IntLiteral(1, POS)
                        ), new Variant<>(
                                new IndexTree()
                                        .appendToGroup(0)
                                        .appendToGroup(1), IndexTree.class)
                )
        );
    }

    @BeforeEach
    void init() {
        tested = new Executor();
    }

    //region Arithmetic Expression
    @ParameterizedTest
    @MethodSource("com.declarative.music.interpreter.StubsFactory#provideArithmeticExpressions")
    void shouldCalculateArithmeticExpression(Expression expression, Variant<?> expectedValue) {
        // when
        expression.accept(tested);

        // then
        assertThat(tested.getCurrentValue()).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("com.declarative.music.interpreter.StubsFactory#provideIncompatibleArithmeticExpressions")
    void shouldThrowWhenDifferentTypes(Expression expression, Class<?> leftType, Class<?> rightType) {
        // when
        assertThatThrownBy(() -> expression.accept(tested))
                .hasMessageStartingWith("INTERPRETATION ERROR Unsupported types: %s %s for operation: %s".
                        formatted(leftType.getSimpleName(), rightType.getSimpleName(), expression.getClass().getSimpleName()));

    }

    @Test
    void shouldThrow_WhenUnsupportedMinusUnaryExpression() {
        // given
        var expression = new MinusUnaryExpression(new StringLiter("a", POS));
        // when
        assertThatThrownBy(() -> expression.accept(tested))
                .hasMessageStartingWith("INTERPRETATION ERROR cannot negate String type");

    }

    @Test
    void shouldThrow_WhenUnsupportedPlusUnaryExpression() {
        // given
        var expression = new PlusUnaryExpression(new StringLiter("a", POS));
        // when
        assertThatThrownBy(() -> expression.accept(tested))
                .hasMessageStartingWith("INTERPRETATION ERROR cannot plus String type");

    }
    //endregion

    //region Cast expression
    @ParameterizedTest
    @MethodSource("com.declarative.music.interpreter.StubsFactory#provideCastArguments")
    void shouldCastExpression(Expression value, Types targetType, Variant<?> expectedValue) {
        // when
        StubsFactory.createCastExpression(value, targetType).accept(tested);

        // then
        assertThat(tested.getCurrentValue()).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("com.declarative.music.interpreter.StubsFactory#provideUncastableArguments")
    void shouldThrow_WhenUnCastableExpression(Expression value, Types targetType) {
        // when
        assertThatThrownBy(() -> StubsFactory.createCastExpression(value, targetType).accept(tested))
                .hasMessageStartingWith("INTERPRETATION ERROR Cannot cast");
    }
    //endregion

    //region Logical Expression
    @ParameterizedTest
    @MethodSource("com.declarative.music.interpreter.StubsFactory#provideBooleanExpressions")
    void shouldCalculateOrderingExpression(Expression expression, Variant<?> expectedValue) {
        // when
        expression.accept(tested);

        // then
        assertThat(tested.getCurrentValue()).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("com.declarative.music.interpreter.StubsFactory#provideIncompatibleOrderingExpressions")
    void shouldThrowOrdering_WhenDifferentTypes(Expression expression, Class<?> leftType, Class<?> rightType) {
        // when
        assertThatThrownBy(() -> expression.accept(tested))
                .hasMessageStartingWith("INTERPRETATION ERROR Unsupported types: %s %s for operation: %s".
                        formatted(leftType.getSimpleName(), rightType.getSimpleName(), expression.getClass().getSimpleName()));

    }

    @ParameterizedTest
    @MethodSource("com.declarative.music.interpreter.StubsFactory#provideCompoundRelationExpressions")
    void shouldCalculateComplexRelationExpression(Expression expression, Variant<?> expectedValue) {
        // when
        expression.accept(tested);

        // then
        assertThat(tested.getCurrentValue()).isEqualTo(expectedValue);
    }
    //endregion

    //region Binary assigment
    @ParameterizedTest
    @MethodSource("provideBinaryAssigments")
    void shouldExecuteExpressionWithAssigment(Variant<?> initialValue, Statement binaryAssignStmt, Variant<?> expectedValue) {
        // given
        tested.getManager().insert(VAR_NAME, initialValue);

        // when
        binaryAssignStmt.accept(tested);

        // then
        assertThat(tested.getManager().get(VAR_NAME).orElseThrow())
                .isEqualToComparingFieldByFieldRecursively(expectedValue);

    }
    //endregion

    //region Music and Index tree
    private static NoteExpression createNoteExpression(String pitch) {
        return new NoteExpression(pitch, new IntLiteral(4, POS), "q", POS);
    }


    private static Note createNote(Pitch pitch) {
        return new Note(pitch, 4, Rythm.q);
    }

    @ParameterizedTest
    @MethodSource("provideNoteExpressions")
    void shouldHandleMusicTreeExpression(Expression noteExpression, Variant<?> expectedTree) {
        // when
        noteExpression.accept(tested);

        // then
        assertThat(tested.getCurrentValue()).isEqualToComparingFieldByFieldRecursively(expectedTree);
    }

    @ParameterizedTest
    @MethodSource("provideIndexExpressions")
    void shouldHandleIndexTreeExpression(Expression indexExpression, Variant<?> expectedTree) {
        // when
        indexExpression.accept(tested);

        // then
        assertThat(tested.getCurrentValue()).isEqualToComparingFieldByFieldRecursively(expectedTree);
    }
    //endregion

    @Test
    void shouldDeclareAndAssignValue() {
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
    void shouldAssignValue() {
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
    void shouldHandleIfStatement() {
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
    void shouldThrow_whenAssigmentToUnknownVariable() {
        // given
        var variableName = "a";
        var variableValue = 1;
        var stmt = new AssigmentStatement(variableName, new IntLiteral(variableValue, POS), POS);

        // when
        assertThatThrownBy(() -> stmt.accept(tested))
                .hasMessageStartingWith("INTERPRETATION ERROR");
    }

    @Test
    void shouldThrow_whenAssigmentWithWrongValueType() {
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
    void shouldDeclareAndAssignExpression() {
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
    void shouldThrow_whenInBinaryOperationUncorrectType() {
        // given
        var stmt = new AddExpression(new IntLiteral(1, POS), new FloatLiteral(2D, POS));

        // when
        assertThatThrownBy(() -> stmt.accept(tested))
                .hasMessageStartingWith("INTERPRETATION ERROR");
    }

    @Test
    void shouldAssignLambdaDeclaration() {
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
    void shouldHandlePipeExpression() {
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
    void shouldHandleArrayExpression() {
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
    void shouldHandleListComprehension_WithArrayIterable() {
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
    void shouldHandleListComprehension() {
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

    @Test
    void shouldHandleAtBuiltInMethod() {
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
