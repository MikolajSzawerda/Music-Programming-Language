package com.declarative.music.parser;

import com.declarative.music.lexer.terminals.OperatorEnum;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.parser.exception.ParsingException;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.assign.*;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.expression.VariableReference;
import com.declarative.music.parser.production.expression.arithmetic.*;
import com.declarative.music.parser.production.expression.array.ArrayExpression;
import com.declarative.music.parser.production.expression.array.ListComprehension;
import com.declarative.music.parser.production.expression.array.RangeExpression;
import com.declarative.music.parser.production.expression.lambda.FunctionCall;
import com.declarative.music.parser.production.expression.lambda.LambdaCall;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.modifier.Modifier;
import com.declarative.music.parser.production.expression.modifier.ModifierExpression;
import com.declarative.music.parser.production.expression.modifier.ModifierItem;
import com.declarative.music.parser.production.expression.music.NoteExpression;
import com.declarative.music.parser.production.expression.music.SequenceExpression;
import com.declarative.music.parser.production.expression.pipe.InlineFuncCall;
import com.declarative.music.parser.production.expression.pipe.PipeExpression;
import com.declarative.music.parser.production.expression.relation.AndExpression;
import com.declarative.music.parser.production.expression.relation.*;
import com.declarative.music.parser.production.expression.relation.NegateExpression;
import com.declarative.music.parser.production.literal.BoolLiteral;
import com.declarative.music.parser.production.literal.FloatLiteral;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.literal.StringLiter;
import com.declarative.music.parser.production.type.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ParserTest {
    private static final Position POSITION = new Position(0, 0);

    private static Stream<Arguments> provideAssigmentWithExpressionStatement() {
        return Stream.of(
                Arguments.of(OperatorEnum.O_PLUS_ASSIGN, (TriFunction<String, Expression, Position, Statement>) PlusAssignStatement::new),
                Arguments.of(OperatorEnum.O_MINUS_ASSIGN, (TriFunction<String, Expression, Position, Statement>) MinusAssignStatement::new),
                Arguments.of(OperatorEnum.O_DIVIDE_ASSIGN, (TriFunction<String, Expression, Position, Statement>) DivAssignStatement::new),
                Arguments.of(OperatorEnum.O_MUL_ASSIGN, (TriFunction<String, Expression, Position, Statement>) MulAssignStatement::new),
                Arguments.of(OperatorEnum.O_SIM_ASSIGN, (TriFunction<String, Expression, Position, Statement>) SequenceAssignStatement::new),
                Arguments.of(OperatorEnum.O_AMPER_ASSIGN, (TriFunction<String, Expression, Position, Statement>) ParalerAssignStatement::new),
                Arguments.of(OperatorEnum.O_POW_ASSIGN, (TriFunction<String, Expression, Position, Statement>) PowAssignStatement::new),
                Arguments.of(OperatorEnum.O_MOD_ASSIGN, (TriFunction<String, Expression, Position, Statement>) ModuloAssignStatement::new)
        );
    }


    private static Stream<Arguments> provideRelationExpression() {
        return Stream.of(
                Arguments.of(OperatorEnum.O_EQ, (BiFunction<Expression, Expression, Expression>) EqExpression::new),
                Arguments.of(OperatorEnum.O_NEQ, (BiFunction<Expression, Expression, Expression>) NotEqExpression::new),
                Arguments.of(OperatorEnum.O_GREATER_EQ, (BiFunction<Expression, Expression, Expression>) GreaterEqExpression::new),
                Arguments.of(OperatorEnum.O_LESS_EQ, (BiFunction<Expression, Expression, Expression>) LessEqExpression::new),
                Arguments.of(OperatorEnum.O_GREATER, (BiFunction<Expression, Expression, Expression>) GreaterExpression::new),
                Arguments.of(OperatorEnum.O_LESS, (BiFunction<Expression, Expression, Expression>) LessExpression::new)
        );
    }

    public static Stream<Arguments> provideLiterals() {
        return Stream.of(
                Arguments.of(List.of(new Token(TokenType.T_INT_NUMBER, pos(0), 10)), new IntLiteral(10, pos(0))),
                Arguments.of(List.of(new Token(TokenType.T_TRUE, pos(0), "true")), new BoolLiteral(true, pos(0))),
                Arguments.of(List.of(new Token(TokenType.T_FALSE, pos(0), "false")), new BoolLiteral(false, pos(0))),
                Arguments.of(List.of(new Token(TokenType.T_PITCH, pos(0), "C")), new NoteExpression("C", null, null, pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_L_PARENTHESIS, pos(0), null),
                        new Token(TokenType.T_PITCH, pos(1), "C"),
                        new Token(TokenType.T_COMMA, pos(2), null),
                        new Token(TokenType.T_INT_NUMBER, pos(3), 10),
                        new Token(TokenType.T_R_PARENTHESIS, pos(4), null)
                ), new NoteExpression("C", new IntLiteral(10, pos(3)), null, pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_L_PARENTHESIS, pos(0), null),
                        new Token(TokenType.T_PITCH, pos(1), "C"),
                        new Token(TokenType.T_COMMA, pos(2), null),
                        new Token(TokenType.T_INT_NUMBER, pos(3), 10),
                        new Token(TokenType.T_R_PARENTHESIS, pos(4), null),
                        new Token(TokenType.T_RHYTHM, pos(5), "q")
                ), new NoteExpression("C", new IntLiteral(10, pos(3)), "q", pos(0))),
                Arguments.of(List.of(new Token(TokenType.T_RHYTHM, pos(0), "q")), new NoteExpression(null, null, "q", pos(0))),
                Arguments.of(List.of(new Token(TokenType.T_STRING, pos(0), "10")), new StringLiter("10", pos(0))),
                Arguments.of(List.of(new Token(TokenType.T_FLOATING_NUMBER, pos(0), 10.0)), new FloatLiteral(10D, pos(0)))
        );
    }

    public static Stream<Arguments> provideNoteExpressions() {
        return Stream.of(
                Arguments.of(List.of(new Token(TokenType.T_RHYTHM, pos(0), "q")), new NoteExpression(null, null, "q", pos(0))),
                Arguments.of(List.of(new Token(TokenType.T_PITCH, pos(0), "C")), new NoteExpression("C", null, null, pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_L_PARENTHESIS, pos(0), null),
                        new Token(TokenType.T_PITCH, pos(1), "C"),
                        new Token(TokenType.T_COMMA, pos(2), null),
                        new Token(TokenType.T_INT_NUMBER, pos(3), 10),
                        new Token(TokenType.T_R_PARENTHESIS, pos(4), null)
                ), new NoteExpression("C", new IntLiteral(10, pos(3)), null, pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_L_PARENTHESIS, pos(0), null),
                        new Token(TokenType.T_PITCH, pos(1), "C"),
                        new Token(TokenType.T_COMMA, pos(2), null),
                        new Token(TokenType.T_INT_NUMBER, pos(3), 10),
                        new Token(TokenType.T_R_PARENTHESIS, pos(4), null),
                        new Token(TokenType.T_RHYTHM, pos(5), "q")
                ), new NoteExpression("C", new IntLiteral(10, pos(3)), "q", pos(0)))
        );
    }

    public static Stream<Arguments> provideDeclarationTypes() {
        return Stream.of(
                Arguments.of(List.of(new Token(TokenType.T_IDENTIFIER, pos(0), "Int")), new SimpleType(Types.Int, pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_L_QAD_PARENTHESIS, pos(0), null),
                        new Token(TokenType.T_R_QAD_PARENTHESIS, pos(1), null),
                        new Token(TokenType.T_IDENTIFIER, pos(2), "Int")), new ArrayType(new SimpleType(Types.Int, pos(2)), pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_L_QAD_PARENTHESIS, pos(0), null),
                        new Token(TokenType.T_R_QAD_PARENTHESIS, pos(1), null),
                        new Token(TokenType.T_L_QAD_PARENTHESIS, pos(2), null),
                        new Token(TokenType.T_R_QAD_PARENTHESIS, pos(3), null),
                        new Token(TokenType.T_IDENTIFIER, pos(4), "Int")), new ArrayType(new ArrayType(new SimpleType(Types.Int, pos(4)), pos(2)), pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_LAMBDA, pos(0), null),
                        new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                        new Token(TokenType.T_IDENTIFIER, pos(2), "Int"),
                        new Token(TokenType.T_R_PARENTHESIS, pos(3), null),
                        new Token(TokenType.T_OPERATOR, pos(4), OperatorEnum.O_ARROW),
                        new Token(TokenType.T_IDENTIFIER, pos(5), "Int")

                ), new LambdaType(List.of(new SimpleType(Types.Int, pos(2))), new SimpleType(Types.Int, pos(5)), pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_L_QAD_PARENTHESIS, pos(0), null),
                        new Token(TokenType.T_R_QAD_PARENTHESIS, pos(1), null),
                        new Token(TokenType.T_LAMBDA, pos(2), null),
                        new Token(TokenType.T_L_PARENTHESIS, pos(3), null),
                        new Token(TokenType.T_IDENTIFIER, pos(4), "Int"),
                        new Token(TokenType.T_R_PARENTHESIS, pos(5), null),
                        new Token(TokenType.T_OPERATOR, pos(6), OperatorEnum.O_ARROW),
                        new Token(TokenType.T_IDENTIFIER, pos(7), "Int")

                ), new ArrayType(new LambdaType(List.of(new SimpleType(Types.Int, pos(4))), new SimpleType(Types.Int, pos(7)), pos(2)), pos(0)))
        );
    }


    public static Stream<Arguments> provideWrongIfStatements() {
        return Stream.of(
                Arguments.of(List.of(
                        new Token(TokenType.T_ELSE, pos(8), null),
                        new Token(TokenType.T_IF, pos(9), null),
                        new Token(TokenType.T_L_PARENTHESIS, pos(10), null),
                        new Token(TokenType.T_INT_NUMBER, pos(11), 3),
                        new Token(TokenType.T_OPERATOR, pos(12), OperatorEnum.O_EQ),
                        new Token(TokenType.T_INT_NUMBER, pos(13), 4),
                        new Token(TokenType.T_R_PARENTHESIS, pos(14), null),
                        new Token(TokenType.T_L_CURL_PARENTHESIS, pos(15), null),
                        new Token(TokenType.T_R_CURL_PARENTHESIS, pos(16), null),
                        new Token(TokenType.T_ELSE, pos(17), null),
                        new Token(TokenType.T_L_CURL_PARENTHESIS, pos(18), null),
                        new Token(TokenType.T_R_CURL_PARENTHESIS, pos(19), null)
                )),
                Arguments.of(List.of(
                        new Token(TokenType.T_ELSE, pos(8), null),
                        new Token(TokenType.T_L_PARENTHESIS, pos(10), null),
                        new Token(TokenType.T_INT_NUMBER, pos(11), 3),
                        new Token(TokenType.T_OPERATOR, pos(12), OperatorEnum.O_EQ),
                        new Token(TokenType.T_INT_NUMBER, pos(13), 4),
                        new Token(TokenType.T_R_PARENTHESIS, pos(14), null),
                        new Token(TokenType.T_L_CURL_PARENTHESIS, pos(15), null),
                        new Token(TokenType.T_R_CURL_PARENTHESIS, pos(16), null)
                )),
                Arguments.of(List.of(
                        new Token(TokenType.T_ELSE, pos(17), null),
                        new Token(TokenType.T_L_CURL_PARENTHESIS, pos(18), null),
                        new Token(TokenType.T_R_CURL_PARENTHESIS, pos(19), null)
                ))
        );
    }


    public static Stream<Arguments> provideExpression() {
        return Stream.of(
                Arguments.of(List.of(new Token(TokenType.T_INT_NUMBER, pos(0), 1)), new IntLiteral(1, POSITION)),
                Arguments.of(List.of(
                        new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                        new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_ARROW),
                        new Token(TokenType.T_INT_NUMBER, pos(2), 2)
                ), new RangeExpression(new IntLiteral(1, pos(0)), new IntLiteral(2, pos(2)))),
                Arguments.of(List.of(
                        new Token(TokenType.T_L_PARENTHESIS, pos(0), null),
                        new Token(TokenType.T_PITCH, pos(1), "C"),
                        new Token(TokenType.T_COMMA, pos(2), null),
                        new Token(TokenType.T_INT_NUMBER, pos(3), 10),
                        new Token(TokenType.T_R_PARENTHESIS, pos(4), null)
                ), new NoteExpression("C", new IntLiteral(10, pos(3)), null, pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_L_PARENTHESIS, pos(0), null),
                        new Token(TokenType.T_PITCH, pos(1), "C"),
                        new Token(TokenType.T_COMMA, pos(2), null),
                        new Token(TokenType.T_INT_NUMBER, pos(3), 10),
                        new Token(TokenType.T_R_PARENTHESIS, pos(4), null),
                        new Token(TokenType.T_RHYTHM, pos(5), "q")
                ), new NoteExpression("C", new IntLiteral(10, pos(3)), "q", pos(0))),
                Arguments.of(List.of(
                        new Token(TokenType.T_IDENTIFIER, pos(0), "a")
                ), new VariableReference("a", pos(0))),
                Arguments.of(List.of(
                                new Token(TokenType.T_L_PARENTHESIS, pos(0), null),
                                new Token(TokenType.T_IDENTIFIER, pos(1), "a"),
                                new Token(TokenType.T_OPERATOR, pos(2), OperatorEnum.O_PIPE),
                                new Token(TokenType.T_IDENTIFIER, pos(3), "call"),
                                new Token(TokenType.T_R_PARENTHESIS, pos(4), null)
                        ),
                        new PipeExpression(new VariableReference("a", pos(1)), new InlineFuncCall("call", List.of(), pos(3)))),
                Arguments.of(List.of(
                                new Token(TokenType.T_WITH, pos(0), null),
                                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                                new Token(TokenType.T_IDENTIFIER, pos(2), "Int"),
                                new Token(TokenType.T_IDENTIFIER, pos(3), "a"),
                                new Token(TokenType.T_R_PARENTHESIS, pos(4), null),
                                new Token(TokenType.T_OPERATOR, pos(5), OperatorEnum.O_ARROW),
                                new Token(TokenType.T_IDENTIFIER, pos(6), "Int"),
                                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(7), null),
                                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(8), null)
                        ),
                        new LambdaExpression(new Parameters(List.of(new Parameter(new SimpleType(Types.Int, pos(2)), "a"))),
                                new SimpleType(Types.Int, pos(6)),
                                new Block(List.of(), pos(7)), pos(0))),
                Arguments.of(
                        List.of(
                                new Token(TokenType.T_IDENTIFIER, pos(0), "a"),
                                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                                new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                                new Token(TokenType.T_R_PARENTHESIS, pos(3), null)
                        ), new FunctionCall("a", List.of(new IntLiteral(1, pos(2))), pos(0))),
                Arguments.of(
                        List.of(
                                new Token(TokenType.T_IDENTIFIER, pos(0), "a"),
                                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                                new Token(TokenType.T_R_PARENTHESIS, pos(3), null)
                        ), new FunctionCall("a", List.of(), pos(0))),
                Arguments.of(
                        List.of(
                                new Token(TokenType.T_IDENTIFIER, pos(0), "a"),
                                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                                new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                                new Token(TokenType.T_R_PARENTHESIS, pos(3), null),
                                new Token(TokenType.T_L_PARENTHESIS, pos(3), null),
                                new Token(TokenType.T_INT_NUMBER, pos(4), 2),
                                new Token(TokenType.T_R_PARENTHESIS, pos(5), null)
                        ), new LambdaCall(
                                new FunctionCall("a", List.of(new IntLiteral(1, pos(2))), pos(0)),
                                List.of(new IntLiteral(2, pos(4))), pos(0)
                        )),
                Arguments.of(List.of(
                                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PLUS),
                                new Token(TokenType.T_INT_NUMBER, pos(2), 2))
                        , new AddExpression(new IntLiteral(1, pos(0)), new IntLiteral(2, pos(2)))),
                Arguments.of(List.of(
                        new Token(TokenType.T_OPERATOR, pos(0), OperatorEnum.O_NEGATE),
                        new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                        new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                        new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_EQ),
                        new Token(TokenType.T_INT_NUMBER, pos(4), 2),
                        new Token(TokenType.T_R_PARENTHESIS, pos(5), null)
                ), new NegateExpression(new EqExpression(new IntLiteral(1, pos(2)), new IntLiteral(2, pos(4))))),
                Arguments.of(List.of(
                                new Token(TokenType.T_OPERATOR, pos(0), OperatorEnum.O_PLUS),
                                new Token(TokenType.T_INT_NUMBER, pos(1), 2))
                        , new PlusUnaryExpression(new IntLiteral(2, pos(1))))
        );
    }

    private static Position pos(final int column) {
        return new Position(0, column);
    }

    @Test
    void shouldParseAssigment() throws ParsingException, IOException {
        final var tokens = List.of(
                new Token(TokenType.T_IDENTIFIER, pos(0), "a"),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_ASSIGN),
                new Token(TokenType.T_INT_NUMBER, pos(2), 10),
                new Token(TokenType.T_SEMICOLON, pos(3), null)
        );
        final var parser = new Parser(new LexerMock(tokens, false));
        final var expected = new Program(List.of(new AssigmentStatement("a", new IntLiteral(10, pos(2)), pos(0))));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldThrowWhenAssigmentWithNoValue() {
        final var tokens = List.of(
                new Token(TokenType.T_IDENTIFIER, POSITION, "a"),
                new Token(TokenType.T_OPERATOR, POSITION, OperatorEnum.O_ASSIGN),
                new Token(TokenType.T_SEMICOLON, POSITION, null)
        );
        final var parser = new Parser(new LexerMock(tokens, false));

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR expected expression when parsing assigment");
    }

    @ParameterizedTest
    @MethodSource("provideLiterals")
    void shouldParseExpressionAsStatement(final List<Token> tokens, final Statement parsedValue) throws Exception {
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(parsedValue));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideAssigmentWithExpressionStatement")
    void shouldParseAssigmentExpression(final OperatorEnum assignOperator, final TriFunction<String, Expression, Position, Statement> statementFactory) throws ParsingException, IOException {
        final var variableName = "a";
        final var variableValue = 10;
        final var tokens = List.of(
                new Token(TokenType.T_IDENTIFIER, pos(0), variableName),
                new Token(TokenType.T_OPERATOR, pos(1), assignOperator),
                new Token(TokenType.T_INT_NUMBER, pos(2), variableValue)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(statementFactory.apply(variableName, new IntLiteral(variableValue, pos(2)), pos(0))));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNestedExpr() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_L_PARENTHESIS, pos(0), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(2), null),
                new Token(TokenType.T_INT_NUMBER, pos(3), 10),
                new Token(TokenType.T_R_PARENTHESIS, pos(4), null),
                new Token(TokenType.T_R_PARENTHESIS, pos(5), null),
                new Token(TokenType.T_R_PARENTHESIS, pos(6), null)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expectedValue = new IntLiteral(10, pos(3));
        final var expected = new Program(List.of(expectedValue));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldThrow_WhenNoClosingParenthesis() {
        final var tokens = List.of(
                new Token(TokenType.T_L_PARENTHESIS, pos(0), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(2), null),
                new Token(TokenType.T_INT_NUMBER, pos(3), 10),
                new Token(TokenType.T_R_PARENTHESIS, pos(4), null),
                new Token(TokenType.T_R_PARENTHESIS, pos(5), null)
        );
        final var parser = new Parser(new LexerMock(tokens));

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR missing closing \"[T_R_PARENTHESIS]\" parenthesis");
    }

    @Test
    void shouldParseLambdaType() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_LAMBDA, pos(0), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_R_PARENTHESIS, pos(2), null),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_ARROW),
                new Token(TokenType.T_IDENTIFIER, pos(4), "Int"),
                new Token(TokenType.T_IDENTIFIER, pos(5), "a")
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new Declaration(
                        new LambdaType(List.of(), new SimpleType(Types.Int, pos(4)), pos(0)),
                        "a", null)
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @ParameterizedTest
    @MethodSource("provideNoteExpressions")
    void shouldParseNoteExpression(final List<Token> noteExpression, final NoteExpression expectedValue) throws Exception {
        final var parser = new Parser(new LexerMock(noteExpression));
        final var expected = new Program(List.of(expectedValue));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideNoteExpressions")
    void shouldParseNestedNoteExpression(final List<Token> noteExpression, final NoteExpression expectedValue) throws Exception {
        final var tokens = new LinkedList<Token>();
        tokens.add(new Token(TokenType.T_L_PARENTHESIS, pos(0), null));
        tokens.addAll(noteExpression);
        tokens.add(new Token(TokenType.T_R_PARENTHESIS, pos(noteExpression.size() + 1), null));
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(expectedValue));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @Test
    void shouldParseNoteSequence() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_PITCH, pos(0), "E"),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_SIM),
                new Token(TokenType.T_PITCH, pos(2), "G")
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new SequenceExpression(
                        new NoteExpression("E", null, null, pos(0)),
                        new NoteExpression("G", null, null, pos(2)))));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseLambdaExpression() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_WITH, pos(0), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_IDENTIFIER, pos(2), "Int"),
                new Token(TokenType.T_IDENTIFIER, pos(3), "a"),
                new Token(TokenType.T_R_PARENTHESIS, pos(4), null),
                new Token(TokenType.T_OPERATOR, pos(5), OperatorEnum.O_ARROW),
                new Token(TokenType.T_IDENTIFIER, pos(6), "Int"),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(7), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(8), null)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expectedValue = new LambdaExpression(
                new Parameters(List.of(new Parameter(new SimpleType(Types.Int, pos(2)), "a"))),
                new SimpleType(Types.Int, pos(6)),
                new Block(List.of(), pos(7)),
                pos(0)
        );
        final var expected = new Program(List.of(expectedValue));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @ParameterizedTest
    @MethodSource("provideDeclarationTypes")
    void shouldParseVarDeclaration(final List<Token> typeToken, final Type expectedType) throws Exception {
        final var tokens = new LinkedList<Token>();
        tokens.addAll(typeToken);
        tokens.add(new Token(TokenType.T_IDENTIFIER, pos(typeToken.size() + 1), "a"));
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(new Declaration(expectedType, "a", null)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideDeclarationTypes")
    void shouldParseVarDeclarationWithAssgiment(final List<Token> typeToken, final Type expectedType) throws Exception {
        final var tokens = new LinkedList<Token>();
        tokens.addAll(typeToken);
        tokens.addAll(List.of(
                new Token(TokenType.T_IDENTIFIER, pos(typeToken.size() + 1), "a"),
                new Token(TokenType.T_OPERATOR, pos(typeToken.size() + 2), OperatorEnum.O_ASSIGN),
                new Token(TokenType.T_INT_NUMBER, pos(typeToken.size() + 3), 1)
        ));
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(new Declaration(expectedType, "a", new IntLiteral(1, pos(typeToken.size() + 3)))));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldThrow_WhenNoRightExpression() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 10),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PLUS)
        );
        final var parser = new Parser(new LexerMock(tokens));

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR expected expression when parsing binary operator");
    }


    @Test
    void shouldParseNestedExpression() throws Exception {
        //1+(2+3);
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PLUS),
                new Token(TokenType.T_L_PARENTHESIS, pos(2), null),
                new Token(TokenType.T_INT_NUMBER, pos(3), 2),
                new Token(TokenType.T_OPERATOR, pos(4), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, pos(5), 3),
                new Token(TokenType.T_R_PARENTHESIS, pos(6), null)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1, pos(0)), new AddExpression(new IntLiteral(2, pos(3)), new IntLiteral(3, pos(5))))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @Test
    void shouldParseNestedLeftExpression() throws Exception {
        //(1+2)+3;
        final var tokens = List.of(
                new Token(TokenType.T_L_PARENTHESIS, pos(0), null),

                new Token(TokenType.T_INT_NUMBER, pos(1), 1),
                new Token(TokenType.T_OPERATOR, pos(2), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, pos(3), 2),
                new Token(TokenType.T_R_PARENTHESIS, pos(4), null),
                new Token(TokenType.T_OPERATOR, pos(5), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, pos(6), 3)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new AddExpression(new AddExpression(new IntLiteral(1, pos(1)), new IntLiteral(2, pos(3))), new IntLiteral(3, pos(6)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @ParameterizedTest
    @MethodSource("provideRelationExpression")
    void shouldParseRelationExpression(final OperatorEnum relationOperator, final BiFunction<Expression, Expression, Expression> factory) throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), relationOperator),
                new Token(TokenType.T_INT_NUMBER, pos(2), 2)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                factory.apply(new IntLiteral(1, pos(0)), new IntLiteral(2, pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseAndExpression() throws Exception {
        //1==2 && 3==4;
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_EQ),
                new Token(TokenType.T_INT_NUMBER, pos(2), 2),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_AND),
                new Token(TokenType.T_INT_NUMBER, pos(4), 3),
                new Token(TokenType.T_OPERATOR, pos(5), OperatorEnum.O_EQ),
                new Token(TokenType.T_INT_NUMBER, pos(6), 4)
        );

        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new AndExpression(
                        new EqExpression(new IntLiteral(1, pos(0)), new IntLiteral(2, pos(2))),
                        new EqExpression(new IntLiteral(3, pos(4)), new IntLiteral(4, pos(6))))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseMulExpression() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_MUL),
                new Token(TokenType.T_INT_NUMBER, pos(2), 2),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_MUL),
                new Token(TokenType.T_INT_NUMBER, pos(4), 3)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new MulExpression(new MulExpression(new IntLiteral(1, pos(0)), new IntLiteral(2, pos(2))), new IntLiteral(3, pos(4)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseAddExpression() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, pos(2), 2),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_MINUS),
                new Token(TokenType.T_INT_NUMBER, pos(4), 3)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new MinusExpression(new AddExpression(new IntLiteral(1, pos(0)), new IntLiteral(2, pos(2))), new IntLiteral(3, pos(4)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParsePrioritizedExpression() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_MUL),
                new Token(TokenType.T_INT_NUMBER, pos(2), 2),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, pos(4), 3)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new AddExpression(new MulExpression(new IntLiteral(1, pos(0)), new IntLiteral(2, pos(2))), new IntLiteral(3, pos(4)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseVariableReference() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_IDENTIFIER, pos(0), "a"),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, pos(2), 1)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new AddExpression(new VariableReference("a", pos(0)), new IntLiteral(1, pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseVariableReferenceFromRight() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PLUS),
                new Token(TokenType.T_IDENTIFIER, pos(2), "a")
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1, pos(0)), new VariableReference("a", pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseFunctionCall_WhenInExpression() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_IDENTIFIER, pos(0), "a"),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                new Token(TokenType.T_COMMA, pos(3), null),
                new Token(TokenType.T_INT_NUMBER, pos(4), 2),
                new Token(TokenType.T_R_PARENTHESIS, pos(5), null),
                new Token(TokenType.T_OPERATOR, pos(6), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, pos(7), 3)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new AddExpression(new FunctionCall("a", List.of(new IntLiteral(1, pos(2)), new IntLiteral(2, pos(4))), pos(0)), new IntLiteral(3, pos(7)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseFunctionCallFromRight_WhenInExpression() throws Exception {
        final var tokens = List.of(

                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PLUS),
                new Token(TokenType.T_IDENTIFIER, pos(2), "a"),
                new Token(TokenType.T_L_PARENTHESIS, pos(3), null),
                new Token(TokenType.T_INT_NUMBER, pos(4), 1),
                new Token(TokenType.T_COMMA, pos(5), null),
                new Token(TokenType.T_INT_NUMBER, pos(6), 2),
                new Token(TokenType.T_R_PARENTHESIS, pos(7), null)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new AddExpression(
                        new IntLiteral(1, pos(0)),
                        new FunctionCall("a", List.of(new IntLiteral(1, pos(4)), new IntLiteral(2, pos(6))), pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseFunctionCallWithLambdaAsArgument() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_IDENTIFIER, pos(0), "a"),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                new Token(TokenType.T_COMMA, pos(3), null),
                new Token(TokenType.T_WITH, pos(4), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(5), null),
                new Token(TokenType.T_R_PARENTHESIS, pos(6), null),
                new Token(TokenType.T_OPERATOR, pos(7), OperatorEnum.O_ARROW),
                new Token(TokenType.T_IDENTIFIER, pos(8), "Int"),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(9), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(10), null),
                new Token(TokenType.T_R_PARENTHESIS, pos(11), null));
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new FunctionCall("a",
                        List.of(new IntLiteral(1, pos(2)),
                                new LambdaExpression(new Parameters(List.of()), new SimpleType(Types.Int, pos(8)), new Block(List.of(), pos(9)), pos(4))),
                        pos(0)
                )
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseHigherOrderFunctionCall() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_IDENTIFIER, pos(0), "a"),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                new Token(TokenType.T_COMMA, pos(3), null),
                new Token(TokenType.T_INT_NUMBER, pos(4), 2),
                new Token(TokenType.T_R_PARENTHESIS, pos(5), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(6), null),
                new Token(TokenType.T_INT_NUMBER, pos(7), 3),
                new Token(TokenType.T_COMMA, pos(8), null),
                new Token(TokenType.T_INT_NUMBER, pos(9), 4),
                new Token(TokenType.T_R_PARENTHESIS, pos(10), null)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new LambdaCall(new FunctionCall("a", List.of(
                        new IntLiteral(1, pos(2)), new IntLiteral(2, pos(4))
                ), pos(0)), List.of(
                        new IntLiteral(3, pos(7)),
                        new IntLiteral(4, pos(9))
                ), pos(0))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @Test
    void shouldParseIfStatement() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_IF, pos(0), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_EQ),
                new Token(TokenType.T_INT_NUMBER, pos(4), 1),
                new Token(TokenType.T_R_PARENTHESIS, pos(5), null),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(6), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(7), null)
        );
        final var parser = new Parser(new LexerMock(tokens, false));
        final var expected = new Program(List.of(
                new IfStatement(new EqExpression(new IntLiteral(1, pos(2)), new IntLiteral(1, pos(4))), new Block(List.of(), pos(6)), pos(0))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @Test
    void shouldParseIfElseIfElseStatement() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_IF, pos(0), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_EQ),
                new Token(TokenType.T_INT_NUMBER, pos(4), 2),
                new Token(TokenType.T_R_PARENTHESIS, pos(5), null),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(6), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(7), null),
                new Token(TokenType.T_ELSE, pos(8), null),
                new Token(TokenType.T_IF, pos(9), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(10), null),
                new Token(TokenType.T_INT_NUMBER, pos(11), 3),
                new Token(TokenType.T_OPERATOR, pos(12), OperatorEnum.O_EQ),
                new Token(TokenType.T_INT_NUMBER, pos(13), 4),
                new Token(TokenType.T_R_PARENTHESIS, pos(14), null),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(15), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(16), null),
                new Token(TokenType.T_ELSE, pos(17), null),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(18), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(19), null)
        );
        final var parser = new Parser(new LexerMock(tokens, false));
        final var expected = new Program(List.of(
                new IfStatement(
                        new EqExpression(new IntLiteral(1, pos(2)), new IntLiteral(2, pos(4))),
                        new Block(List.of(), pos(6)),
                        new IfStatement(
                                new EqExpression(new IntLiteral(3, pos(11)), new IntLiteral(4, pos(13))),
                                new Block(List.of(), pos(15)),
                                new IfStatement(null, new Block(List.of(), pos(18)), pos(17)),
                                pos(9)),
                        pos(0)
                )

        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseIfElseStatement() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_IF, pos(0), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_EQ),
                new Token(TokenType.T_INT_NUMBER, pos(4), 1),
                new Token(TokenType.T_R_PARENTHESIS, pos(5), null),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(6), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(7), null),
                new Token(TokenType.T_ELSE, pos(8), null),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(9), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(10), null)
        );
        final var parser = new Parser(new LexerMock(tokens, false));
        final var expected = new Program(List.of(
                new IfStatement(new EqExpression(new IntLiteral(1, pos(2)), new IntLiteral(1, pos(4))), new Block(List.of(), pos(6)),
                        new IfStatement(null, new Block(List.of(), pos(9)), pos(8)), pos(0)))
        );

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideWrongIfStatements")
    void shouldThrow_WhenNoFirstIfPart(final List<Token> tokens) {
        final var parser = new Parser(new LexerMock(tokens, false));

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR else statement without if statement");
    }

    @Test
    void shouldParseInlineCall_WithNoArguments() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, pos(2), "twice")
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new PipeExpression(
                        new IntLiteral(1, pos(0)),
                        new InlineFuncCall("twice", List.of(), pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseMultiplePipe() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, pos(2), "once"),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, pos(4), "twice"),
                new Token(TokenType.T_OPERATOR, pos(5), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, pos(6), 1)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new PipeExpression(new PipeExpression(
                        new IntLiteral(1, pos(0)),
                        new InlineFuncCall("once", List.of(), pos(2)))
                        , new InlineFuncCall("twice", List.of(new PlusUnaryExpression(new IntLiteral(1, pos(6)))), pos(4)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseInlineCall_WithArgument() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, pos(2), "twice"),
                new Token(TokenType.T_IDENTIFIER, pos(3), "a")
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new PipeExpression(
                        new IntLiteral(1, pos(0)),
                        new InlineFuncCall("twice", List.of(new VariableReference("a", pos(3))), pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseInlineCall_WithArgument(final List<Token> expressionTokens, final Expression expectedExpression) throws Exception {
        final var tokens = new LinkedList<Token>();
        tokens.addAll(List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, pos(2), "fun")
        ));
        tokens.addAll(expressionTokens);
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new PipeExpression(
                        new IntLiteral(1, pos(0)),
                        new InlineFuncCall("fun", List.of(expectedExpression), pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseInlineCall_WithArguments(final List<Token> expressionTokens, final Expression expectedExpression) throws Exception {
        final var tokens = new LinkedList<Token>();
        tokens.addAll(List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, pos(2), "fun")
        ));
        tokens.addAll(expressionTokens);
        tokens.add(
                new Token(TokenType.T_COMMA, pos(2), null)
        );
        tokens.addAll(expressionTokens);
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new PipeExpression(
                        new IntLiteral(1, pos(0)),
                        new InlineFuncCall("fun", List.of(expectedExpression, expectedExpression), pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseInlineCall_WithSecondArgumentNested(final List<Token> expressionTokens, final Expression expectedExpression) throws Exception {
        final var tokens = new LinkedList<Token>();
        tokens.addAll(List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, pos(2), "fun")
        ));
        tokens.addAll(expressionTokens);
        tokens.add(new Token(TokenType.T_COMMA, pos(2), null));
        tokens.add(new Token(TokenType.T_L_PARENTHESIS, pos(3), null));
        tokens.addAll(expressionTokens);
        tokens.add(new Token(TokenType.T_R_PARENTHESIS, pos(4), null));
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new PipeExpression(
                        new IntLiteral(1, pos(0)),
                        new InlineFuncCall("fun", List.of(expectedExpression, expectedExpression), pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @Test
    void shouldThrow_WhenNothingAfterPipeOperator() {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PIPE)
        );
        final var parser = new Parser(new LexerMock(tokens));

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR missing inline call after pipe operator");
    }


    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseModifier(final List<Token> expressionTokens, final Expression expression) throws Exception {
        final var tokens = new LinkedList<Token>();
        tokens.addAll(List.of(
                new Token(TokenType.T_IDENTIFIER, pos(0), "a"),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_IDENTIFIER, pos(2), "b"),
                new Token(TokenType.T_OPERATOR, pos(3), OperatorEnum.O_ASSIGN),
                new Token(TokenType.T_INT_NUMBER, pos(4), 1),
                new Token(TokenType.T_COMMA, pos(5), null),
                new Token(TokenType.T_IDENTIFIER, pos(6), "c"),
                new Token(TokenType.T_OPERATOR, pos(7), OperatorEnum.O_ASSIGN)


        ));
        tokens.addAll(expressionTokens);
        tokens.add(new Token(TokenType.T_R_CURL_PARENTHESIS, pos(10), null));
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new ModifierExpression(new VariableReference("a", pos(0)), new Modifier(
                        List.of(
                                new ModifierItem("b", new IntLiteral(1, pos(4))),
                                new ModifierItem("c", expression)
                        )
                ), pos(0))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseArray(final List<Token> expressionTokens, final Expression expression) throws Exception {
        final var tokens = new LinkedList<Token>();
        tokens.addAll(List.of(
                new Token(TokenType.T_L_QAD_PARENTHESIS, pos(0), null),
                new Token(TokenType.T_INT_NUMBER, pos(1), 1),
                new Token(TokenType.T_COMMA, pos(2), null),
                new Token(TokenType.T_IDENTIFIER, pos(3), "a"),
                new Token(TokenType.T_COMMA, pos(2), null)
        ));
        tokens.addAll(expressionTokens);
        tokens.add(new Token(TokenType.T_COMMA, pos(3), null));
        tokens.addAll(expressionTokens);
        tokens.add(new Token(TokenType.T_R_QAD_PARENTHESIS, pos(4), null));
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new ArrayExpression(List.of(
                        new IntLiteral(1, pos(1)),
                        new VariableReference("a", pos(3)),
                        expression, expression
                ), pos(0))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseArrayWithPipe() throws Exception {
        //[[1]|>a,[1]|>b];
        final var tokens = List.of(
                new Token(TokenType.T_L_QAD_PARENTHESIS, pos(0), null),
                new Token(TokenType.T_L_QAD_PARENTHESIS, pos(1), null),
                new Token(TokenType.T_INT_NUMBER, pos(2), 1),
                new Token(TokenType.T_R_QAD_PARENTHESIS, pos(3), null),
                new Token(TokenType.T_OPERATOR, pos(4), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, pos(5), "a"),
                new Token(TokenType.T_COMMA, pos(6), null),
                new Token(TokenType.T_L_QAD_PARENTHESIS, pos(7), null),
                new Token(TokenType.T_INT_NUMBER, pos(8), 2),
                new Token(TokenType.T_R_QAD_PARENTHESIS, pos(9), null),
                new Token(TokenType.T_OPERATOR, pos(10), OperatorEnum.O_PIPE),
                new Token(TokenType.T_IDENTIFIER, pos(11), "b"),
                new Token(TokenType.T_R_QAD_PARENTHESIS, pos(12), null)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new ArrayExpression(List.of(
                        new PipeExpression(new ArrayExpression(List.of(new IntLiteral(1, pos(2))), pos(1)), new InlineFuncCall("a", List.of(), pos(5))),
                        new PipeExpression(new ArrayExpression(List.of(new IntLiteral(2, pos(8))), pos(7)), new InlineFuncCall("b", List.of(), pos(11)))
                ), pos(0)
                )));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldThrow_WhenNothingAfterCommaInArray() {
        final var tokens = List.of(
                new Token(TokenType.T_L_QAD_PARENTHESIS, pos(0), null),
                new Token(TokenType.T_INT_NUMBER, pos(1), 1),
                new Token(TokenType.T_COMMA, pos(2), null)
        );
        final var parser = new Parser(new LexerMock(tokens));

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR expected expression when parsing array item");
    }

    @Test
    void shouldParseRange() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_ARROW),
                new Token(TokenType.T_INT_NUMBER, pos(2), 10)
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new RangeExpression(new IntLiteral(1, pos(0)), new IntLiteral(10, pos(2)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseListComprehension() throws Exception {
        //[x*2 <| x 1->b];
        final var tokens = List.of(
                new Token(TokenType.T_L_QAD_PARENTHESIS, pos(0), null),
                new Token(TokenType.T_IDENTIFIER, pos(1), "x"),
                new Token(TokenType.T_OPERATOR, pos(2), OperatorEnum.O_MUL),
                new Token(TokenType.T_INT_NUMBER, pos(3), 2),
                new Token(TokenType.T_OPERATOR, pos(4), OperatorEnum.O_LIST_COMPR),
                new Token(TokenType.T_IDENTIFIER, pos(5), "x"),
                new Token(TokenType.T_INT_NUMBER, pos(6), 1),
                new Token(TokenType.T_OPERATOR, pos(7), OperatorEnum.O_ARROW),
                new Token(TokenType.T_IDENTIFIER, pos(8), "b"),
                new Token(TokenType.T_R_QAD_PARENTHESIS, pos(9), null)

        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new ListComprehension(new MulExpression(new VariableReference("x", pos(1)), new IntLiteral(2, pos(3))),
                        new VariableReference("x", pos(5)),
                        new RangeExpression(new IntLiteral(1, pos(6)), new VariableReference("b", pos(8)))
                        , pos(0))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideDeclarationTypes")
    void shouldParseFor(final List<Token> typeToken, final Type expectedType) throws Exception {
        //for(%s i in 1->b){}
        final var tokens = new LinkedList<>(List.of(
                new Token(TokenType.T_FOR, pos(0), null),
                new Token(TokenType.T_L_PARENTHESIS, pos(1), null)
        ));
        tokens.addAll(typeToken);
        tokens.addAll(List.of(
                new Token(TokenType.T_IDENTIFIER, pos(typeToken.size() + 3), "i"),
                new Token(TokenType.T_IN, pos(typeToken.size() + 4), null),
                new Token(TokenType.T_INT_NUMBER, pos(6), 1),
                new Token(TokenType.T_OPERATOR, pos(7), OperatorEnum.O_ARROW),
                new Token(TokenType.T_IDENTIFIER, pos(8), "b"),
                new Token(TokenType.T_R_PARENTHESIS, pos(9), null),
                new Token(TokenType.T_L_CURL_PARENTHESIS, pos(10), null),
                new Token(TokenType.T_R_CURL_PARENTHESIS, pos(11), null)

        ));
        final var parser = new Parser(new LexerMock(tokens, false));
        final var expected = new Program(List.of(
                new ForStatement(new Declaration(expectedType, "i", null),
                        new RangeExpression(new IntLiteral(1, pos(6)), new VariableReference("b", pos(8))),
                        new Block(List.of(), pos(10)), pos(0)
                )
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseCast() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_INT_NUMBER, pos(0), 1),
                new Token(TokenType.T_OPERATOR, pos(1), OperatorEnum.O_PLUS),
                new Token(TokenType.T_INT_NUMBER, pos(2), 2),
                new Token(TokenType.T_AS, pos(3), null),
                new Token(TokenType.T_IDENTIFIER, pos(4), "Int")
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1, pos(0)), new CastExpresion(new IntLiteral(2, pos(2)), new SimpleType(Types.Int, pos(4)), pos(3)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParsePlusUnaryExpression() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_OPERATOR, pos(0), OperatorEnum.O_PLUS),
                new Token(TokenType.T_IDENTIFIER, pos(1), "a")
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new PlusUnaryExpression(new VariableReference("a", pos(1)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseMinusUnaryExpression() throws Exception {
        final var tokens = List.of(
                new Token(TokenType.T_OPERATOR, pos(0), OperatorEnum.O_MINUS),
                new Token(TokenType.T_IDENTIFIER, pos(1), "a")
        );
        final var parser = new Parser(new LexerMock(tokens));
        final var expected = new Program(List.of(
                new MinusUnaryExpression(new VariableReference("a", pos(1)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseMultipleStatements() throws Exception {
        //Int a;a+=2;
        final var tokens = List.of(
                new Token(TokenType.T_IDENTIFIER, pos(0), "Int"),
                new Token(TokenType.T_IDENTIFIER, pos(1), "a"),
                new Token(TokenType.T_SEMICOLON, pos(2), null),
                new Token(TokenType.T_IDENTIFIER, pos(3), "a"),
                new Token(TokenType.T_OPERATOR, pos(4), OperatorEnum.O_PLUS_ASSIGN),
                new Token(TokenType.T_INT_NUMBER, pos(5), 2),
                new Token(TokenType.T_SEMICOLON, pos(6), null)
        );
        final var parser = new Parser(new LexerMock(tokens, false));
        final var expected = new Program(List.of(
                new Declaration(new SimpleType(Types.Int, pos(0)), "a", null),
                new PlusAssignStatement("a", new IntLiteral(2, pos(5)), pos(3))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

}