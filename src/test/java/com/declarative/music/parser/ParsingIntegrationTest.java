package com.declarative.music.parser;

import com.declarative.music.lexer.LexerImpl;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.expression.VariableReference;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.arithmetic.MinusUnaryExpression;
import com.declarative.music.parser.production.expression.array.ArrayExpression;
import com.declarative.music.parser.production.expression.lambda.FunctionCall;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.modifier.Modifier;
import com.declarative.music.parser.production.expression.modifier.ModifierExpression;
import com.declarative.music.parser.production.expression.modifier.ModifierItem;
import com.declarative.music.parser.production.expression.music.NoteExpression;
import com.declarative.music.parser.production.expression.music.ParallerExpression;
import com.declarative.music.parser.production.expression.music.SequenceExpression;
import com.declarative.music.parser.production.expression.pipe.InlineFuncCall;
import com.declarative.music.parser.production.expression.pipe.PipeExpression;
import com.declarative.music.parser.production.expression.relation.AndExpression;
import com.declarative.music.parser.production.expression.relation.EqExpression;
import com.declarative.music.parser.production.expression.relation.OrExpression;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.type.*;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ParsingIntegrationTest {
    private static Position pos(final int column) {
        return new Position(0, column);
    }

    @Test
    void shouldParseLambdaExpressionWithPipe() throws Exception {
        final var code = "with(Int a, []Int c, lam(Int)->Void d) -> Void {Int b;} |> call;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(
                List.of(
                        new PipeExpression(
                                new LambdaExpression(new Parameters(List.of(
                                        new Parameter(new SimpleType(Types.Int, pos(5)), "a"),
                                        new Parameter(new ArrayType(new SimpleType(Types.Int, pos(14)), pos(12)), "c"),
                                        new Parameter(new LambdaType(List.of(new SimpleType(Types.Int, pos(25))), new SimpleType(Types.Void, pos(31)), pos(21)), "d")
                                )
                                ), new SimpleType(Types.Void, pos(42)),
                                        new Block(List.of(new Declaration(new SimpleType(Types.Int, pos(48)), "b", null)), pos(47)), pos(0)),
                                new InlineFuncCall("call", List.of(), pos(59)))
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
                                new EqExpression(new IntLiteral(1, pos(0)), new IntLiteral(2, pos(3))),
                                new AndExpression(
                                        new EqExpression(new IntLiteral(3, pos(8)), new IntLiteral(4, pos(11))),
                                        new EqExpression(new IntLiteral(5, pos(16)), new IntLiteral(6, pos(19)))
                                )),
                        new EqExpression(new IntLiteral(7, pos(24)), new IntLiteral(8, pos(27))))
        )
        );

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParseIfStatementWithPipe() throws Exception {
        final var code = "if((1|>mel) || (2 |> mel)){}";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new IfStatement(new OrExpression(new PipeExpression(new IntLiteral(1, pos(4)), new InlineFuncCall("mel", List.of(), pos(7))),
                        new PipeExpression(new IntLiteral(2, pos(16)), new InlineFuncCall("mel", List.of(), pos(21)))
                ), new Block(List.of(), pos(26)), pos(0))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParsePipeExpression() throws Exception {
        final var code = "1+2 |> twice -1 |> add a(), b+1;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new PipeExpression(
                                new AddExpression(new IntLiteral(1, pos(0)), new IntLiteral(2, pos(2))),
                                new InlineFuncCall("twice", List.of(new MinusUnaryExpression(new IntLiteral(1, pos(14)))), pos(7))),
                        new InlineFuncCall("add", List.of(new FunctionCall("a", List.of(), pos(23)),
                                new AddExpression(new VariableReference("b", pos(28)), new IntLiteral(1, pos(30)))), pos(19)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @Test
    void shouldParsePipeExpression_WithPipeAsInlineArgument() throws Exception {
        final var code = "a |> twice (2 |> twice) |> twice;";
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        final var expected = new Program(List.of(
                new PipeExpression(
                        new PipeExpression(new VariableReference("a", pos(0)),
                                new InlineFuncCall("twice", List.of(new PipeExpression(new IntLiteral(2, pos(12)), new InlineFuncCall("twice", List.of(), pos(17)))), pos(5))
                        ), new InlineFuncCall("twice", List.of(), pos(27))
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
                new Declaration(new InferenceType(pos(0)), "a",
                        new PipeExpression(
                                new ModifierExpression(
                                        new ArrayExpression(
                                                List.of(
                                                        new NoteExpression("E", null, null, pos(9)),
                                                        new NoteExpression("G", new IntLiteral(2, pos(16)), null, pos(12)),
                                                        new NoteExpression("D", null, null, pos(20))
                                                ), pos(8)
                                        ),
                                        new Modifier(List.of(
                                                new ModifierItem("oct", new IntLiteral(4, pos(27)))
                                        ))
                                        , pos(8)),
                                new InlineFuncCall("mel", List.of(), pos(33))
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
                                                new NoteExpression("E", null, null, pos(1)),
                                                new NoteExpression("G", new IntLiteral(2, pos(8)), null, pos(4)),
                                                new NoteExpression("D", null, null, pos(12))
                                        ), pos(0)
                                ),
                                new Modifier(List.of(
                                        new ModifierItem("oct", new IntLiteral(4, pos(19)))
                                ))
                                , pos(0)),
                        new InlineFuncCall("mel", List.of(), pos(25))
                )
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
                                        new NoteExpression("C", null, null, pos(1)), new NoteExpression("E", new IntLiteral(4, pos(9)), "q", pos(5))
                                ), new NoteExpression("G", null, null, pos(16)))
                        , new NoteExpression("E", null, null, pos(21)))
        ));

        final var program = parser.parserProgram();

        assertThat(program).isEqualToComparingFieldByFieldRecursively(expected);
    }


}
