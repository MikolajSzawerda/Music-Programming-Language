package com.declarative.music.interpreter;

import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Parameter;
import com.declarative.music.parser.production.Parameters;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.expression.arithmetic.*;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
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
}
