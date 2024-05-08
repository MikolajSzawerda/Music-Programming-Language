package com.declarative.music.parser;

import com.declarative.music.lexer.LexerImpl;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.assign.*;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.expression.VariableReference;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.arithmetic.MinusUnaryExpression;
import com.declarative.music.parser.production.expression.arithmetic.MulExpression;
import com.declarative.music.parser.production.expression.arithmetic.PlusUnaryExpression;
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
import com.declarative.music.parser.production.expression.music.ParallerExpression;
import com.declarative.music.parser.production.expression.music.SequenceExpression;
import com.declarative.music.parser.production.expression.pipe.InlineFuncCall;
import com.declarative.music.parser.production.expression.pipe.PipeExpression;
import com.declarative.music.parser.production.expression.relation.*;
import com.declarative.music.parser.production.literal.BoolLiteral;
import com.declarative.music.parser.production.literal.FloatLiteral;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.literal.StringLiter;
import com.declarative.music.parser.production.type.ArrayType;
import com.declarative.music.parser.production.type.LambdaType;
import com.declarative.music.parser.production.type.SimpleType;
import com.declarative.music.parser.production.type.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.StringReader;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ParserTest {
    private static Stream<Arguments> provideAssigmentWithExpressionStatement() {
        return Stream.of(
                Arguments.of("+=", (BiFunction<String, Expression, Statement>) PlusAssignStatement::new),
                Arguments.of("-=", (BiFunction<String, Expression, Statement>) MinusAssignStatement::new),
                Arguments.of("/=", (BiFunction<String, Expression, Statement>) DivAssignStatement::new),
                Arguments.of("*=", (BiFunction<String, Expression, Statement>) MulAssignStatement::new),
                Arguments.of("|=", (BiFunction<String, Expression, Statement>) SequenceAssignStatement::new),
                Arguments.of("&=", (BiFunction<String, Expression, Statement>) ParalerAssignStatement::new),
                Arguments.of("^=", (BiFunction<String, Expression, Statement>) PowAssignStatement::new),
                Arguments.of("%=", (BiFunction<String, Expression, Statement>) ModuloAssignStatement::new)
        );
    }

    private static Stream<Arguments> provideRelationExpression() {
        return Stream.of(
                Arguments.of("==", (BiFunction<Expression, Expression, Expression>) EqExpression::new),
                Arguments.of("!=", (BiFunction<Expression, Expression, Expression>) NotEqExpression::new),
                Arguments.of(">=", (BiFunction<Expression, Expression, Expression>) GreaterEqExpression::new),
                Arguments.of("<=", (BiFunction<Expression, Expression, Expression>) LessEqExpression::new),
                Arguments.of(">", (BiFunction<Expression, Expression, Expression>) GreaterExpression::new),
                Arguments.of("<", (BiFunction<Expression, Expression, Expression>) LessExpression::new)
        );
    }

    public static Stream<Arguments> provideLiterals() {
        return Stream.of(
                Arguments.of("10", new IntLiteral(10)),
                Arguments.of("true", new BoolLiteral(true)),
                Arguments.of("false", new BoolLiteral(false)),
                Arguments.of("10.0", new FloatLiteral(10.0)),
                Arguments.of("\"10\"", new StringLiter("10")),
                Arguments.of("C", new NoteExpression("C", null, null)),
                Arguments.of("q", new NoteExpression(null, null, "q")),
                Arguments.of("(C, 4)", new NoteExpression("C", new IntLiteral(4), null)),
                Arguments.of("(C, 4) q", new NoteExpression("C", new IntLiteral(4), "q"))
        );
    }

    public static Stream<Arguments> provideNoteExpressions() {
        return Stream.of(
                Arguments.of(("(C, 4)"), new NoteExpression("C", new IntLiteral(4), null)),
                Arguments.of(("(C, 4) q"), new NoteExpression("C", new IntLiteral(4), "q")),
                Arguments.of(("C"), new NoteExpression("C", null, null)),
                Arguments.of(("q"), new NoteExpression(null, null, "q"))
        );
    }

    public static Stream<Arguments> provideDeclarationTypes() {
        return Stream.of(
                Arguments.of("Int", new SimpleType(Types.Int)),
                Arguments.of("let", new SimpleType(Types.LET)),
                Arguments.of("[]Int", new ArrayType(new SimpleType(Types.Int))),
                Arguments.of("[][]Int", new ArrayType(new ArrayType(new SimpleType(Types.Int)))),
                Arguments.of("lam(Int)->Int", new LambdaType(List.of(new SimpleType(Types.Int)), new SimpleType(Types.Int))),
                Arguments.of("[]lam(Int)->Int", new ArrayType(new LambdaType(List.of(new SimpleType(Types.Int)), new SimpleType(Types.Int))))
        );
    }

    public static Stream<Arguments> provideExpressionLiterals() {
        return Stream.of(
                Arguments.of("1", "2", new IntLiteral(1), new IntLiteral(2)),
                Arguments.of("(1)", "(2)", new IntLiteral(1), new IntLiteral(2)),
                Arguments.of("1.0", "2", new FloatLiteral(1D), new IntLiteral(2)),
                Arguments.of("1.0", "2.0", new FloatLiteral(1D), new FloatLiteral(2D)),
                Arguments.of("1", "2.0", new IntLiteral(1), new FloatLiteral(2D)),
                Arguments.of("\"a\"", "\"b\"", new StringLiter("a"), new StringLiter("b"))
        );
    }

    public static Stream<Arguments> provideWrongIfStatements() {
        return Stream.of(
                Arguments.of("else if(1==1){}"),
                Arguments.of("else {}"),
                Arguments.of("else(1==1) {}")
        );
    }

    public static Stream<Arguments> provideExpression() {
        return Stream.of(
                Arguments.of("1", new IntLiteral(1)),
                Arguments.of("1->2", new RangeExpression(new IntLiteral(1), new IntLiteral(2))),
                Arguments.of("(C, 4)", new NoteExpression("C", new IntLiteral(4), null)),
                Arguments.of("(C, 4) q", new NoteExpression("C", new IntLiteral(4), "q")),
                Arguments.of("a", new VariableReference("a")),
                Arguments.of("(a |> call)", new PipeExpression(new VariableReference("a"), new InlineFuncCall("call", List.of()))),
                Arguments.of("with(Int a)->Int {}",
                        new LambdaExpression(new Parameters(List.of(new Parameter(new SimpleType(Types.Int), "a"))),
                                new SimpleType(Types.Int),
                                new Block(List.of()))),
                Arguments.of("a(1)", new FunctionCall("a", List.of(new IntLiteral(1)))),
                Arguments.of("a(1)(2)", new LambdaCall(new FunctionCall("a", List.of(new IntLiteral(1))), List.of(new IntLiteral(2)))),
                Arguments.of("1 + 1", new AddExpression(new IntLiteral(1), new IntLiteral(1))),
                Arguments.of("+ 1", new PlusUnaryExpression(new IntLiteral(1))),
                Arguments.of("!(1==1)", new NegateExpression(new EqExpression(new IntLiteral(1), new IntLiteral(1))))
        );
    }

    @Test
    void shouldParseAssigment() throws Exception {
        final var code = "a=10;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(new AssigmentStatement("a", new IntLiteral(10))));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldThrowWhenAssigmentWithNoValue() throws Exception {
        final var code = "a=;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR expected expression when parsing assigment");
    }

    @ParameterizedTest
    @MethodSource("provideLiterals")
    void shouldParseExpressionAsStatement(final String value, final Statement parsedValue) throws Exception {
        final var code = "%s;".formatted(value);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(parsedValue));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideAssigmentWithExpressionStatement")
    void shouldParseAssigmentExpression(final String assigmentOperator, final BiFunction<String, Expression, Statement> statementFactory) throws Exception {
        final var variableName = "a";
        final var variableValue = 10;
        final var code = "%s %s %d;".formatted(variableName, assigmentOperator, variableValue);

        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(statementFactory.apply(variableName, new IntLiteral(variableValue))));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNestedExpr() throws Exception {
        final var code = "(((10)));";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expectedValue = new IntLiteral(10);
        final var expected = new Program(List.of(expectedValue));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldThrow_WhenNoClosingParenthesis() throws Exception {
        final var code = "(((10));";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR missing closing \"[T_R_PARENTHESIS]\" parenthesis");
    }

    @Test
    void shouldParseNestedExpressionWithAssigment() throws Exception {
        final var code = "a=(((10)));";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AssigmentStatement("a", new IntLiteral(10))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNestedLambdaType() throws Exception {
        final var code = "lam(lam(Int)->Int)->Int a;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new Declaration(new LambdaType(List.of(new LambdaType(List.of(new SimpleType(Types.Int)), new SimpleType(Types.Int))),
                        new SimpleType(Types.Int)),
                        "a", null)
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseLambdaType_WithNoParameters() throws Exception {
        final var code = "lam()->Int a;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new Declaration(
                        new LambdaType(List.of(), new SimpleType(Types.Int)),
                        "a", null)
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideNoteExpressions")
    void shouldParseNoteAssigment(final String noteExpression, final NoteExpression expectedValue) throws Exception {
        final var code = "a=%s;".formatted(noteExpression);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(new AssigmentStatement("a", expectedValue)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideNoteExpressions")
    void shouldParseNoteExpression(final String noteExpression, final NoteExpression expectedValue) throws Exception {
        final var lexer = new LexerImpl(new StringReader("%s;".formatted(noteExpression)));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(expectedValue));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideNoteExpressions")
    void shouldParseNestedNoteAssigment(final String noteExpression, final NoteExpression expectedValue) throws Exception {
        final var code = "a=(%s);".formatted(noteExpression);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(new AssigmentStatement("a", expectedValue)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideNoteExpressions")
    void shouldParseNestedNoteExpression(final String noteExpression, final NoteExpression expectedValue) throws Exception {
        final var code = "(%s);".formatted(noteExpression);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(expectedValue));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNoteSequence() throws Exception {
        final var code = "((E, 4) w | (G, 4) w);";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new SequenceExpression(
                        new NoteExpression("E", new IntLiteral(4), "w"),
                        new NoteExpression("G", new IntLiteral(4), "w"))));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseLambdaExpression() throws Exception {
        final var code = "a=with(Int a, String c) -> Int {Int b;return b+2;};";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expectedValue = new LambdaExpression(new Parameters(List.of(
                new Parameter(new SimpleType(Types.Int), "a"),
                new Parameter(new SimpleType(Types.String), "c"))
        ), new SimpleType(Types.Int), new Block(
                List.of(
                        new Declaration(new SimpleType(Types.Int), "b", null),
                        new ReturnStatement(new AddExpression(new VariableReference("b"), new IntLiteral(2)))
                )
        ));
        final var expected = new Program(List.of(new AssigmentStatement("a", expectedValue)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseLambdaExpressionWithPipe() throws Exception {
        final var code = "with(Int a, []Int c, lam(Int)->Void d) -> Void {Int b;} |> call;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(new PipeExpression(new LambdaExpression(new Parameters(List.of(
                new Parameter(new SimpleType(Types.Int), "a"),
                new Parameter(new ArrayType(new SimpleType(Types.Int)), "c"),
                new Parameter(new LambdaType(List.of(new SimpleType(Types.Int)), new SimpleType(Types.Void)), "d")
        )), new SimpleType(Types.Void), new Block(
                List.of(new Declaration(new SimpleType(Types.Int), "b", null))
        )), new InlineFuncCall("call", List.of()))));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideDeclarationTypes")
    void shouldParseVarDeclaration(final String type, final Type expectedType) throws Exception {
        final var code = "%s a;".formatted(type);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(new Declaration(expectedType, "a", null)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseLambdaDeclaration() throws Exception {
        final var code = "lam(Int, Int)->Int NWD;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(new Declaration(
                new LambdaType(List.of(new SimpleType(Types.Int), new SimpleType(Types.Int)), new SimpleType(Types.Int)),
                "NWD", null)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseArrayTypeDeclaration() throws Exception {
        final var code = "[][]Int a;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(new Declaration(new ArrayType(new ArrayType(new SimpleType(Types.Int))), "a", null)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseVarDeclarationWithAssigiment() throws Exception {
        final var code = "Scale a=(C, 4) q;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new Declaration(new SimpleType(Types.Scale), "a", new NoteExpression("C", new IntLiteral(4), "q"))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpressionLiterals")
    void shouldParseExpression(final String left, final String right, final Expression leftExpr, final Expression rightExpr) throws Exception {
        final var code = "%s+%s;".formatted(left, right);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(leftExpr, rightExpr)
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpressionLiterals")
    void shouldThrow_WhenNoRightExpression(final String left, final String right, final Expression leftExpr, final Expression rightExpr) throws Exception {
        final var code = "%s+;".formatted(left);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR expected expression when parsing binary operator");
    }

    @Test
    void shouldParseNestedExpression() throws Exception {
        final var code = "1+(2+3);";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1), new AddExpression(new IntLiteral(2), new IntLiteral(3)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNestedLeftExpression() throws Exception {
        final var code = "(1+2)+3;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new AddExpression(new IntLiteral(1), new IntLiteral(2)), new IntLiteral(3))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideRelationExpression")
    void shouldParseRelationExpression(final String operator, final BiFunction<Expression, Expression, Expression> factory) throws Exception {
        final var code = "1 %s 2;".formatted(operator);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                factory.apply(new IntLiteral(1), new IntLiteral(2))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseAndExpression() throws Exception {
        final var code = "1==2 && 3==4;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AndExpression(new EqExpression(new IntLiteral(1), new IntLiteral(2)), new EqExpression(new IntLiteral(3), new IntLiteral(4)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNegateUnaryOperator() throws Exception {
        final var code = "!(1==2 && 3==4);";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new NegateExpression(new AndExpression(new EqExpression(new IntLiteral(1), new IntLiteral(2)),
                        new EqExpression(new IntLiteral(3), new IntLiteral(4))))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseAndAndOrExpression() throws Exception {
        final var code = "1==2 || 3==4 && 5==6 || 7==8;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new OrExpression(
                        new OrExpression(
                                new EqExpression(new IntLiteral(1), new IntLiteral(2)),
                                new AndExpression(
                                        new EqExpression(new IntLiteral(3), new IntLiteral(4)),
                                        new EqExpression(new IntLiteral(5), new IntLiteral(6))
                                )),
                        new EqExpression(new IntLiteral(7), new IntLiteral(8)))
        )
        );

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseMulExpression() throws Exception {
        final var code = "1*2*3;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new MulExpression(new MulExpression(new IntLiteral(1), new IntLiteral(2)), new IntLiteral(3))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseAddExpression() throws Exception {
        final var code = "1+2+3;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new AddExpression(new IntLiteral(1), new IntLiteral(2)), new IntLiteral(3))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParsePrioritizedExpression() throws Exception {
        final var code = "1*2+3;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new MulExpression(new IntLiteral(1), new IntLiteral(2)), new IntLiteral(3))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseVariableReference() throws Exception {
        final var code = "a+1;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new VariableReference("a"), new IntLiteral(1))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseVariableReferenceFromRight() throws Exception {
        final var code = "1+a;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1), new VariableReference("a"))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseFunctionCall() throws Exception {
        final var code = "a(1, 2)+1;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new FunctionCall("a", List.of(new IntLiteral(1), new IntLiteral(2))), new IntLiteral(1))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseFunctionCallWithLambdaAsArgument() throws Exception {
        final var code = "a(1, with()->Int{});";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new FunctionCall("a",
                        List.of(new IntLiteral(1), new LambdaExpression(new Parameters(List.of()), new SimpleType(Types.Int), new Block(List.of()))))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseHigherOrderFunctionCall() throws Exception {
        final var code = "a(1, 2)(3, 4);";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new LambdaCall(new FunctionCall("a", List.of(
                        new IntLiteral(1), new IntLiteral(2)
                )), List.of(
                        new IntLiteral(3),
                        new IntLiteral(4)
                ))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseFunctionCallFromRight() throws Exception {
        final var code = "1+a(1, 2);";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1),
                        new FunctionCall("a", List.of(new IntLiteral(1), new IntLiteral(2))))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseIfStatement() throws Exception {
        final var code = "if(1==1){}";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new IfStatement(new EqExpression(new IntLiteral(1), new IntLiteral(1)), new Block(List.of()))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseIfStatementWithPipe() throws Exception {
        final var code = "if((1|>mel) || (2 |> mel)){}";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new IfStatement(new OrExpression(new PipeExpression(new IntLiteral(1), new InlineFuncCall("mel", List.of())),
                        new PipeExpression(new IntLiteral(2), new InlineFuncCall("mel", List.of()))
                ), new Block(List.of()))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseIfElseIfElseStatement() throws Exception {
        final var code = "if(1==1){} else if(2==2) {} else {}";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new IfStatement(new EqExpression(new IntLiteral(1), new IntLiteral(1)), new Block(List.of()),
                        new IfStatement(new EqExpression(new IntLiteral(2), new IntLiteral(2)), new Block(List.of()),
                                new IfStatement(null, new Block(List.of()))))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseIfElseStatement() throws Exception {
        final var code = "if(1==1){} else {}";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new IfStatement(new EqExpression(new IntLiteral(1), new IntLiteral(1)), new Block(List.of()),
                        new IfStatement(null, new Block(List.of()))))
        );

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideWrongIfStatements")
    void shouldThrow_WhenNoFirstIfPart(final String code) {
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR else statement without if statement");
    }

    @Test
    void shouldParsePipeExpression() throws Exception {
        final var code = "1+2 |> twice -1 |> add a(), b+1;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new PipeExpression(
                                new AddExpression(new IntLiteral(1), new IntLiteral(2)),
                                new InlineFuncCall("twice", List.of(new MinusUnaryExpression(new IntLiteral(1))))),
                        new InlineFuncCall("add", List.of(new FunctionCall("a", List.of()),
                                new AddExpression(new VariableReference("b"), new IntLiteral(1)))))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseInlineCall_WithNoArguments() throws Exception {
        final var code = "1 |> twice;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new IntLiteral(1),
                        new InlineFuncCall("twice", List.of()))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseInlineCall_WithArgument(final String argumentCode, final Expression expectedExpression) throws Exception {
        final var code = "1 |> fun %s;".formatted(argumentCode);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new IntLiteral(1),
                        new InlineFuncCall("fun", List.of(expectedExpression)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseInlineCall_WithArguments(final String argumentCode, final Expression expectedExpression) throws Exception {
        final var code = "1 |> fun %s, %s;".formatted(argumentCode, argumentCode);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new IntLiteral(1),
                        new InlineFuncCall("fun", List.of(expectedExpression, expectedExpression)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseInlineCall_WithSecondArgumentNested(final String argumentCode, final Expression expectedExpression) throws Exception {
        final var code = "1 |> fun %s, (%s);".formatted(argumentCode, argumentCode);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new IntLiteral(1),
                        new InlineFuncCall("fun", List.of(expectedExpression, expectedExpression)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldThrow_WhenNothingAfterPipeOperator() {
        final var code = "1 |>;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR missing inline call after pipe operator");
    }

    @Test
    void shouldParsePipeExpression_WithPipeAsInlineArgument() throws Exception {
        final var code = "a |> twice (2 |> twice) |> twice;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new PipeExpression(new VariableReference("a"),
                                new InlineFuncCall("twice", List.of(new PipeExpression(new IntLiteral(2), new InlineFuncCall("twice", List.of()))))
                        ), new InlineFuncCall("twice", List.of())
                )
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseModifier(final String exprCode, final Expression expression) throws Exception {
        final var code = "a{b=1+2, c=%s};".formatted(exprCode);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new ModifierExpression(new VariableReference("a"), new Modifier(
                        List.of(
                                new ModifierItem("b", new AddExpression(new IntLiteral(1), new IntLiteral(2))),
                                new ModifierItem("c", expression)
                        )
                ))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideExpression")
    void shouldParseArray(final String exprCode, final Expression expression) throws Exception {
        final var code = "[1,a,b(),%s,%s];".formatted(exprCode, exprCode);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new ArrayExpression(List.of(
                        new IntLiteral(1),
                        new VariableReference("a"), new FunctionCall("b", List.of()),
                        expression, expression
                ))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseArrayWithPipe() throws Exception {
        final var code = "[[1]|>a,[1]|>b];";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new ArrayExpression(List.of(
                        new PipeExpression(new ArrayExpression(List.of(new IntLiteral(1))), new InlineFuncCall("a", List.of())),
                        new PipeExpression(new ArrayExpression(List.of(new IntLiteral(1))), new InlineFuncCall("b", List.of())))
                )
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldThrow_WhenNothingAfterCommaInArray() {
        final var code = "[1,];";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);

        assertThatThrownBy(parser::parserProgram)
                .hasMessageStartingWith("SYNTAX ERROR expected expression when parsing array item");
    }

    @Test
    void shouldParseRange() throws Exception {
        final var code = "1->b;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new RangeExpression(new IntLiteral(1), new VariableReference("b"))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseListComprehension() throws Exception {
        final var code = "[x*2 <| x 1->b];";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new ListComprehension(new MulExpression(new VariableReference("x"), new IntLiteral(2)),
                        new VariableReference("x"),
                        new RangeExpression(new IntLiteral(1), new VariableReference("b"))
                )
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @ParameterizedTest
    @MethodSource("provideDeclarationTypes")
    void shouldParseFor(final String typeCode, final Type expectedType) throws Exception {
        final var code = "for(%s i in 1->3){}".formatted(typeCode);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new ForStatement(new Declaration(expectedType, "i", null),
                        new RangeExpression(new IntLiteral(1), new IntLiteral(3)),
                        new Block(List.of())
                )
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNoteSequenceAssigment() throws Exception {
        final var code = "let a = [E, (G, 2), D]{oct=4} |> mel;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new Declaration(new SimpleType(Types.LET), "a",
                        new PipeExpression(
                                new ModifierExpression(
                                        new ArrayExpression(
                                                List.of(
                                                        new NoteExpression("E", null, null),
                                                        new NoteExpression("G", new IntLiteral(2), null),
                                                        new NoteExpression("D", null, null)
                                                )
                                        ),
                                        new Modifier(List.of(
                                                new ModifierItem("oct", new IntLiteral(4))
                                        ))
                                ),
                                new InlineFuncCall("mel", List.of())
                        ))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNoteExpressionAsStatement() throws Exception {
        final var code = "[E, (G, 2), D]{oct=4} |> mel;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new ModifierExpression(
                                new ArrayExpression(
                                        List.of(
                                                new NoteExpression("E", null, null),
                                                new NoteExpression("G", new IntLiteral(2), null),
                                                new NoteExpression("D", null, null)
                                        )
                                ),
                                new Modifier(List.of(
                                        new ModifierItem("oct", new IntLiteral(4))
                                ))
                        ),
                        new InlineFuncCall("mel", List.of())
                )
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseCast() throws Exception {
        final var code = "1+2 as Int;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1), new CastExpresion(new IntLiteral(2), new SimpleType(Types.Int)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseUnaryExpression() throws Exception {
        final var code = "+a;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PlusUnaryExpression(new VariableReference("a"))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseMultipleStatements() throws Exception {
        final var code = "Int a;a+=2;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new Declaration(new SimpleType(Types.Int), "a", null),
                new PlusAssignStatement("a", new IntLiteral(2))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParse() throws Exception {
        final var code =
                "1+3*4^7-1+3/6*12;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);

        final var program = parser.parserProgram();
        assert program != null;
    }

    @Test
    void shouldParseMusicTree() throws Exception {
        final var code = "(C | (E, 4) q | G) & E;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new ParallerExpression(
                        new SequenceExpression(
                                new SequenceExpression(
                                        new NoteExpression("C", null, null), new NoteExpression("E", new IntLiteral(4), "q")
                                ), new NoteExpression("G", null, null))
                        , new NoteExpression("E", null, null))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

}