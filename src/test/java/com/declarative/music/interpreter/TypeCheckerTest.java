package com.declarative.music.interpreter;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Declaration;
import com.declarative.music.parser.production.Parameter;
import com.declarative.music.parser.production.Parameters;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.expression.array.ArrayExpression;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.music.NoteExpression;
import com.declarative.music.parser.production.expression.music.ParallerExpression;
import com.declarative.music.parser.production.expression.music.SequenceExpression;
import com.declarative.music.parser.production.literal.FloatLiteral;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.type.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TypeCheckerTest {
    private TypeChecker tested;
    private static final Position POS = new Position(0, 0);

    private static LambdaExpression createLambda(List<Type> parameters, Type returnType) {
        return new LambdaExpression(
                new Parameters(parameters.stream().map(type -> new Parameter(type, "")).toList()),
                returnType,
                new Block(List.of(), POS),
                POS
        );
    }

    private static Type createLambdaType(List<Types> parameters, Type returnType) {
        return new LambdaType(
                parameters.stream()
                        .map(t -> (Type) new SimpleType(t, POS))
                        .toList(), returnType, POS);
    }

    private static Stream<Arguments> provideBadTypes() {
        return Stream.of(
                Arguments.of(new SimpleType(Types.Int, POS), new FloatLiteral(1.0, POS)),
                Arguments.of(new SimpleType(Types.Double, POS), new IntLiteral(1, POS)),
                Arguments.of(new SimpleType(Types.String, POS), new FloatLiteral(1.0, POS)),
                Arguments.of(new SimpleType(Types.Bool, POS), new FloatLiteral(1.0, POS)),
                Arguments.of(new SimpleType(Types.Phrase, POS), new SequenceExpression(
                        new IntLiteral(1, POS),
                        new IntLiteral(2, POS)
                )),
                Arguments.of(new ArrayType(new SimpleType(Types.Double, POS), POS),
                        new ArrayExpression(List.of(
                                new IntLiteral(1, POS)
                        ), POS)
                ),
                Arguments.of(createLambdaType(List.of(Types.Int, Types.Double), new SimpleType(Types.Int, POS)),
                        createLambda(List.of(new SimpleType(Types.Double, POS), new SimpleType(Types.Double, POS)), new SimpleType(Types.Int, POS)))
        );
    }

    private static Stream<Arguments> provideCorrectTypes() {
        return Stream.of(
                Arguments.of(new SimpleType(Types.Template, POS), new SequenceExpression(
                        new ParallerExpression(
                                new IntLiteral(1, POS),
                                new IntLiteral(2, POS)
                        ),
                        new ParallerExpression(
                                new IntLiteral(1, POS),
                                new IntLiteral(2, POS)
                        )
                )),
                Arguments.of(new SimpleType(Types.Phrase, POS), new SequenceExpression(
                        new NoteExpression("C", new IntLiteral(4, POS), "q", POS),
                        new NoteExpression("E", new IntLiteral(4, POS), "q", POS)
                ))
        );
    }

    @BeforeEach
    void init() {
        tested = new TypeChecker(new ContextManager());
    }

    @ParameterizedTest
    @MethodSource("provideBadTypes")
    void shouldThrow_WhenBadTypeInDeclaration(Type leftType, Expression rightValue) {
        // given
        var stmt = new Declaration(
                leftType, "test", rightValue
        );

        // when
        assertThatThrownBy(() -> stmt.accept(tested))
                .hasMessageStartingWith("SEMANTIC ERROR cannot assign value to variable of different type");
    }

    @ParameterizedTest
    @MethodSource("provideCorrectTypes")
    void shouldNotThrow_WhenCorrectType(Type leftType, Expression rightValue) {
        // given
        var stmt = new Declaration(
                leftType, "test", rightValue
        );

        // when
        stmt.accept(tested);
    }
}