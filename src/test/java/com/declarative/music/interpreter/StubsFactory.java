package com.declarative.music.interpreter;

import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.interpreter.values.music.MusicTree;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.Pitch;
import com.declarative.music.interpreter.values.music.Rythm;
import com.declarative.music.interpreter.values.template.IndexTree;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Parameter;
import com.declarative.music.parser.production.Parameters;
import com.declarative.music.parser.production.assign.*;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.expression.arithmetic.*;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.music.NoteExpression;
import com.declarative.music.parser.production.expression.music.ParallerExpression;
import com.declarative.music.parser.production.expression.music.SequenceExpression;
import com.declarative.music.parser.production.expression.relation.*;
import com.declarative.music.parser.production.literal.BoolLiteral;
import com.declarative.music.parser.production.literal.FloatLiteral;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.literal.StringLiter;
import com.declarative.music.parser.production.type.SimpleType;
import com.declarative.music.parser.production.type.Types;
import org.junit.jupiter.params.provider.Arguments;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

public class StubsFactory {
    static final Position POS = new Position(0, 0);
    static final String VAR_NAME = "testVar";

    private static Expression createExpression(Class<?> expressionType, Class<?> literalType, Object leftValue, Object rightValue) {
        try {
            var binaryConstructor = expressionType.getConstructor(Expression.class, Expression.class);
            var literalConstructor = literalType.getConstructor(leftValue.getClass(), Position.class);
            return (Expression) binaryConstructor.newInstance(literalConstructor.newInstance(leftValue, POS), literalConstructor.newInstance(rightValue, POS));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    static Expression createCastExpression(Expression toCast, Types targetType) {
        return new CastExpresion(toCast, new SimpleType(targetType, POS), POS);
    }

    public static Stream<Arguments> provideArithmeticExpressions() {
        return Stream.of(
                //1+2=3
                Arguments.of(createExpression(AddExpression.class, IntLiteral.class, 1, 2), new Variant<>(3, Integer.class)),
                //1+2+1+2
                Arguments.of(new AddExpression(
                        createExpression(AddExpression.class, IntLiteral.class, 1, 2),
                        createExpression(AddExpression.class, IntLiteral.class, 1, 2)
                ), new Variant<>(6, Integer.class)),
                //2*2+3*2
                Arguments.of(new AddExpression(
                        createExpression(MulExpression.class, IntLiteral.class, 2, 2),
                        createExpression(MulExpression.class, IntLiteral.class, 3, 2)
                ), new Variant<>(10, Integer.class)),
                //(1.0/2.0 * (4+4) as Double ) as Int
                Arguments.of(
                        createCastExpression(
                                new MulExpression(
                                        createExpression(DivExpression.class, FloatLiteral.class, 1.0, 2.0),
                                        createCastExpression(createExpression(AddExpression.class, IntLiteral.class, 4, 4),
                                                Types.Double)),
                                Types.Int),
                        new Variant<>(4, Integer.class)),
                Arguments.of(new AddExpression(
                        new MulExpression(
                                createExpression(PowExpression.class, IntLiteral.class, 2, 3),
                                createExpression(AddExpression.class, IntLiteral.class, 2, 2)
                        ),
                        createExpression(MulExpression.class, IntLiteral.class, 3, 2)
                ), new Variant<>(38, Integer.class)),
                Arguments.of(createExpression(AddExpression.class, FloatLiteral.class, 1.5, 2.5), new Variant<>(4.0, Double.class)),
                Arguments.of(createExpression(AddExpression.class, StringLiter.class, "a", "b"), new Variant<>("ab", String.class)),
                Arguments.of(createExpression(MinusExpression.class, IntLiteral.class, 1, 2), new Variant<>(-1, Integer.class)),
                Arguments.of(createExpression(MinusExpression.class, FloatLiteral.class, 1.5, 2.5), new Variant<>(-1.0, Double.class)),
                Arguments.of(createExpression(DivExpression.class, IntLiteral.class, 1, 2), new Variant<>(0, Integer.class)),
                Arguments.of(createExpression(DivExpression.class, FloatLiteral.class, 1.0, 2.0), new Variant<>(0.5, Double.class)),
                Arguments.of(createExpression(MulExpression.class, IntLiteral.class, 2, 2), new Variant<>(4, Integer.class)),
                Arguments.of(createExpression(MulExpression.class, FloatLiteral.class, 2.0, 2.0), new Variant<>(4.0, Double.class)),
                Arguments.of(createExpression(PowExpression.class, IntLiteral.class, 2, 2), new Variant<>(4, Integer.class)),
                Arguments.of(createExpression(PowExpression.class, FloatLiteral.class, 2.0, 2.0), new Variant<>(4.0, Double.class)),
                Arguments.of(new MulExpression(new StringLiter("a", POS), new IntLiteral(2, POS)), new Variant<>("aa", String.class)),
                Arguments.of(new PlusUnaryExpression(new IntLiteral(1, POS)), new Variant<>(1, Integer.class)),
                Arguments.of(new PlusUnaryExpression(new FloatLiteral(1.0, POS)), new Variant<>(1.0, Double.class)),
                Arguments.of(new MinusUnaryExpression(new IntLiteral(1, POS)), new Variant<>(-1, Integer.class)),
                Arguments.of(new MinusUnaryExpression(new FloatLiteral(1.0, POS)), new Variant<>(-1.0, Double.class)),
                Arguments.of(createExpression(ModuloExpression.class, IntLiteral.class, 10, 3), new Variant<>(1, Integer.class))
        );
    }

    public static Stream<Arguments> provideBooleanExpressions() {
        return Stream.of(
                Arguments.of(createExpression(EqExpression.class, IntLiteral.class, 10, 3), new Variant<>(false, Boolean.class)),
                Arguments.of(createExpression(EqExpression.class, StringLiter.class, "a", "b"), new Variant<>(false, Boolean.class)),
                Arguments.of(createExpression(EqExpression.class, StringLiter.class, "a", "a"), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(EqExpression.class, IntLiteral.class, 10, 10), new Variant<>(true, Boolean.class)),

                Arguments.of(createExpression(NotEqExpression.class, IntLiteral.class, 10, 3), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(NotEqExpression.class, StringLiter.class, "a", "b"), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(NotEqExpression.class, StringLiter.class, "a", "a"), new Variant<>(false, Boolean.class)),
                Arguments.of(createExpression(NotEqExpression.class, IntLiteral.class, 10, 10), new Variant<>(false, Boolean.class)),

                Arguments.of(createExpression(LessExpression.class, IntLiteral.class, 10, 3), new Variant<>(false, Boolean.class)),
                Arguments.of(createExpression(LessExpression.class, StringLiter.class, "a", "b"), new Variant<>(false, Boolean.class)),
                Arguments.of(createExpression(LessExpression.class, StringLiter.class, "a", "aa"), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(LessExpression.class, IntLiteral.class, 10, 10), new Variant<>(false, Boolean.class)),
                Arguments.of(createExpression(LessExpression.class, IntLiteral.class, 3, 10), new Variant<>(true, Boolean.class)),


                Arguments.of(createExpression(LessEqExpression.class, IntLiteral.class, 10, 3), new Variant<>(false, Boolean.class)),
                Arguments.of(createExpression(LessEqExpression.class, StringLiter.class, "a", "b"), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(LessEqExpression.class, StringLiter.class, "a", "aa"), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(LessEqExpression.class, IntLiteral.class, 10, 10), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(LessEqExpression.class, IntLiteral.class, 3, 10), new Variant<>(true, Boolean.class)),

                Arguments.of(createExpression(GreaterEqExpression.class, IntLiteral.class, 10, 3), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(GreaterEqExpression.class, IntLiteral.class, 3, 30), new Variant<>(false, Boolean.class)),
                Arguments.of(createExpression(GreaterEqExpression.class, StringLiter.class, "a", "b"), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(GreaterEqExpression.class, StringLiter.class, "a", "a"), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(GreaterEqExpression.class, IntLiteral.class, 10, 10), new Variant<>(true, Boolean.class)),

                Arguments.of(createExpression(GreaterExpression.class, IntLiteral.class, 10, 3), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(GreaterExpression.class, StringLiter.class, "a", "b"), new Variant<>(false, Boolean.class)),
                Arguments.of(createExpression(GreaterExpression.class, StringLiter.class, "aa", "a"), new Variant<>(true, Boolean.class)),
                Arguments.of(createExpression(GreaterExpression.class, IntLiteral.class, 10, 10), new Variant<>(false, Boolean.class)),

                Arguments.of(new NegateExpression(new BoolLiteral(true, POS)), new Variant<>(false, Boolean.class)),
                Arguments.of(new NegateExpression(new BoolLiteral(false, POS)), new Variant<>(true, Boolean.class)),
                Arguments.of(new NegateExpression(createExpression(EqExpression.class, IntLiteral.class, 10, 3)), new Variant<>(true, Boolean.class))
        );
    }

    public static Stream<Arguments> provideCompoundRelationExpressions() {
        return Stream.of(
                Arguments.of(
                        new AndExpression(
                                createExpression(EqExpression.class, IntLiteral.class, 10, 10),
                                createExpression(EqExpression.class, StringLiter.class, "a", "a")
                        ), new Variant<>(true, Boolean.class)),
                Arguments.of(
                        new AndExpression(
                                new BoolLiteral(true, POS),
                                createExpression(EqExpression.class, StringLiter.class, "a", "a")
                        ), new Variant<>(true, Boolean.class)),
                Arguments.of(new OrExpression(
                        createExpression(EqExpression.class, IntLiteral.class, 10, 3),
                        createExpression(EqExpression.class, StringLiter.class, "a", "a")
                ), new Variant<>(true, Boolean.class)),
                Arguments.of(new OrExpression(
                        new BoolLiteral(false, POS),
                        createExpression(EqExpression.class, StringLiter.class, "a", "a")
                ), new Variant<>(true, Boolean.class)),
                Arguments.of(new AndExpression(
                        createExpression(EqExpression.class, IntLiteral.class, 10, 10),
                        createExpression(EqExpression.class, StringLiter.class, "a", "b")
                ), new Variant<>(false, Boolean.class)),
                Arguments.of(new AndExpression(
                        new BoolLiteral(true, POS),
                        new BoolLiteral(false, POS)
                ), new Variant<>(false, Boolean.class)),
                Arguments.of(new OrExpression(
                        new BoolLiteral(false, POS),
                        new BoolLiteral(false, POS)
                ), new Variant<>(false, Boolean.class)),
                Arguments.of(new OrExpression(
                        createExpression(EqExpression.class, IntLiteral.class, 10, 3),
                        createExpression(EqExpression.class, StringLiter.class, "a", "b")
                ), new Variant<>(false, Boolean.class))
        );
    }

    static Stream<Arguments> provideIncompatibleArithmeticExpressions() {
        return Stream.of(
                Arguments.of(new AddExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class),
                Arguments.of(new MulExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class),
                Arguments.of(new DivExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class),
                Arguments.of(new ModuloExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class),
                Arguments.of(new PowExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class),
                Arguments.of(new MinusExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class)
        );
    }

    static Stream<Arguments> provideIncompatibleOrderingExpressions() {
        return Stream.of(
                Arguments.of(new EqExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class),
                Arguments.of(new GreaterEqExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class),
                Arguments.of(new LessEqExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class),
                Arguments.of(new LessExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class),
                Arguments.of(new NotEqExpression(new IntLiteral(1, POS), new FloatLiteral(1.0, POS)), Integer.class, Double.class)
        );
    }

    static Stream<Arguments> provideCastArguments() {
        return Stream.of(
                Arguments.of(new IntLiteral(1, POS), Types.Double, new Variant<>(1.0, Double.class)),
                Arguments.of(new FloatLiteral(1.0, POS), Types.Int, new Variant<>(1, Integer.class)),
                Arguments.of(new FloatLiteral(1.0, POS), Types.Double, new Variant<>(1.0, Double.class)),
                Arguments.of(new IntLiteral(1, POS), Types.Int, new Variant<>(1, Integer.class)),
                Arguments.of(new IntLiteral(1, POS), Types.String, new Variant<>("1", String.class)),
                Arguments.of(new FloatLiteral(1.0, POS), Types.String, new Variant<>("1.0", String.class)),
                Arguments.of(new StringLiter("1.0", POS), Types.Double, new Variant<>(1.0, Double.class)),
                Arguments.of(new StringLiter("1", POS), Types.Int, new Variant<>(1, Integer.class)),
                Arguments.of(createExpression(AddExpression.class, IntLiteral.class, 1, 1), Types.Double, new Variant<>(2.0, Double.class)),
                Arguments.of(createExpression(AddExpression.class, StringLiter.class, "1", "2"), Types.Int, new Variant<>(12, Integer.class)),
                Arguments.of(createExpression(AddExpression.class, StringLiter.class, "1", "2.0"), Types.Double, new Variant<>(12.0, Double.class)),
                Arguments.of(createExpression(AddExpression.class, FloatLiteral.class, 1.0, 1.0), Types.Int, new Variant<>(2, Integer.class))
        );
    }

    static Stream<Arguments> provideUncastableArguments() {
        return Stream.of(
                Arguments.of(new LambdaExpression(
                        new Parameters(List.of(new Parameter(new SimpleType(Types.Int, POS), "a"))),
                        new SimpleType(Types.Int, POS),
                        new Block(List.of(), POS),
                        POS
                ), Types.Int)
        );
    }

    static Stream<Arguments> provideBinaryAssigments() {
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

    private static NoteExpression createNoteExpression(String pitch) {
        return new NoteExpression(pitch, new IntLiteral(4, POS), "q", POS);
    }


    private static Note createNote(Pitch pitch) {
        return new Note(pitch, 4, Rythm.q);
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

    static Stream<Arguments> provideIndexExpressions() {
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
}
