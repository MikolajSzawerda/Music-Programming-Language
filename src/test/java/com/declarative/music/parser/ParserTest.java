package com.declarative.music.parser;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.VariableReference;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.arithmetic.MulExpression;
import com.declarative.music.parser.production.expression.arithmetic.PlusUnaryExpression;
import com.declarative.music.parser.production.expression.array.ArrayExpression;
import com.declarative.music.parser.production.expression.array.ListComprehension;
import com.declarative.music.parser.production.expression.array.RangeExpression;
import com.declarative.music.parser.production.expression.lambda.LambdaCall;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.modifier.Modifier;
import com.declarative.music.parser.production.expression.modifier.ModifierExpression;
import com.declarative.music.parser.production.expression.modifier.ModifierItem;
import com.declarative.music.parser.production.expression.music.NoteExpression;
import com.declarative.music.parser.production.expression.pipe.InlineFuncCall;
import com.declarative.music.parser.production.expression.pipe.PipeExpression;
import com.declarative.music.parser.production.expression.relation.AndExpression;
import com.declarative.music.parser.production.expression.relation.EqExpression;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.type.SimpleType;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class ParserTest {
    @Test
    void shouldParseAssigment() throws Exception {
        final var code = "a=10;";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(new AssigmentStatement("a", new IntLiteral(10))));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNestedExpr() throws Exception {
        final var code = "a=(((10)));";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expectedValue = new IntLiteral(10);
        final var expected = new Program(List.of(new AssigmentStatement("a", expectedValue)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNoteExpression() throws Exception {
        final var code = "a=(C, 4) q;";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expectedValue = new NoteExpression("C", new IntLiteral(4), "q");
        final var expected = new Program(List.of(new AssigmentStatement("a", expectedValue)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseLambdaExpression() throws Exception {
        final var code = "a=with(Int a, String c) -> Void {Int b;};";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expectedValue = new LambdaExpression(new Parameters(List.of(
                new Parameter("Int", "a"),
                new Parameter("String", "c"))
        ), "Void", new Block(
                List.of(new Declaration(new SimpleType(Types.Int), "b", null))
        ));
        final var expected = new Program(List.of(new AssigmentStatement("a", expectedValue)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseVarDeclaration() throws Exception {
        final var code = "Int a;";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(new Declaration(new SimpleType(Types.Int), "a", null)));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseVarDeclarationWithAssigiment() throws Exception {
        final var code = "Scale a=(C, 4) q;";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new Declaration(new SimpleType(Types.Scale), "a", new NoteExpression("C", new IntLiteral(4), "q"))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseExpression() throws Exception {
        final var code = "1+2;";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1), new IntLiteral(2))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNestedExpression() throws Exception {
        final var code = "1+(2+3);";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1), new AddExpression(new IntLiteral(2), new IntLiteral(3)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseEqExpression() throws Exception {
        final var code = "1==2;";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new EqExpression(new IntLiteral(1), new IntLiteral(2))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseAndExpression() throws Exception {
        final var code = "1==2 && 3==4;";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AndExpression(new EqExpression(new IntLiteral(1), new IntLiteral(2)), new EqExpression(new IntLiteral(3), new IntLiteral(4)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseMulExpression() throws Exception {
        final var code = "1*2*3;";
        final var lexer = new Lexer(new StringReader(code));
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
        final var lexer = new Lexer(new StringReader(code));
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
        final var lexer = new Lexer(new StringReader(code));
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
        final var lexer = new Lexer(new StringReader(code));
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
        final var lexer = new Lexer(new StringReader(code));
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
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new LambdaCall("a", List.of(new IntLiteral(1), new IntLiteral(2))), new IntLiteral(1))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseFunctionCallFromRight() throws Exception {
        final var code = "1+a(1, 2);";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new AddExpression(new IntLiteral(1),
                        new LambdaCall("a", List.of(new IntLiteral(1), new IntLiteral(2))))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseIfStatement() throws Exception {
        final var code = "if(1==1){};";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new IfStatement(new EqExpression(new IntLiteral(1), new IntLiteral(1)), new Block(List.of()))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseIfElseIfElseStatement() throws Exception {
        final var code = "if(1==1){} else if(2==2) {} else {};";
        final var lexer = new Lexer(new StringReader(code));
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
    void shouldParsePipeExpression() throws Exception {
        final var code = "1+2 |> twice |> add a() b+1;";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new PipeExpression(
                                new AddExpression(new IntLiteral(1), new IntLiteral(2)),
                                new InlineFuncCall("twice", List.of())),
                        new InlineFuncCall("add", List.of(new LambdaCall("a", List.of()),
                                new AddExpression(new VariableReference("b"), new IntLiteral(1)))))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseModifier() throws Exception {
        final var code = "a{b=1+2, c=d};";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new ModifierExpression(new VariableReference("a"), new Modifier(
                        List.of(
                                new ModifierItem("b", new AddExpression(new IntLiteral(1), new IntLiteral(2))),
                                new ModifierItem("c", new VariableReference("d"))
                        )
                ))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseArray() throws Exception {
        final var code = "[1,a,b()];";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new ArrayExpression(List.of(new IntLiteral(1), new VariableReference("a"), new LambdaCall("b", List.of())))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseRange() throws Exception {
        final var code = "1->b;";
        final var lexer = new Lexer(new StringReader(code));
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
        final var lexer = new Lexer(new StringReader(code));
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

    @Test
    void shouldParseFor() throws Exception {
        final var code = "for(Int i in 1->3){};";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new ForStatement(new Declaration(new SimpleType(Types.Int), "i", null),
                        new RangeExpression(new IntLiteral(1), new IntLiteral(3)),
                        new Block(List.of())
                )
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseNoteAssigment() throws Exception {
        final var code = "let a = [E, (G, 2), D]{oct=4} |> mel;";
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new Declaration(null, "a",
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
    void shouldParseCast() throws Exception {
        final var code = "1+2 as Int;";
        final var lexer = new Lexer(new StringReader(code));
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
        final var lexer = new Lexer(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PlusUnaryExpression(new VariableReference("a"))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


}