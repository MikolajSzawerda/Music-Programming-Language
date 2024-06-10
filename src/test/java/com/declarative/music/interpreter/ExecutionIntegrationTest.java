package com.declarative.music.interpreter;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.SimpleNode;
import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.interpreter.values.template.IndexTree;
import com.declarative.music.lexer.LexerImpl;
import com.declarative.music.parser.Parser;
import com.declarative.music.parser.exception.ParsingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


public class ExecutionIntegrationTest {
    @Test
    void shouldCallFunction() throws ParsingException, IOException {
        // given
        final var code = """
                Int a = 1;
                let b = with(Int c)->Void{
                    a = c+2;
                };
                b(2);
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(4, interpreter.getManager().getGlobalFrame().getValue("a").orElseThrow().getValue());
    }

    @Test
    void shouldGetValueFromFunctionCall() throws ParsingException, IOException {
        // given
        final var code = """
                let b = with(Int x_a, Int x_b)->Int{
                    return x_a+x_b;
                };
                Int d = b(2, 3);
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(5, interpreter.getManager().getGlobalFrame().getValue("d").orElseThrow().getValue());
    }

    @Test
    void shouldGetValueFromFunctionCall_withIf() throws ParsingException, IOException {
        // given
        final var code = """
                let b = with(Int x_a, Int x_b)->Int{
                    if(x_a == x_b){
                        if(x_a - 1 == x_b - 1){
                            return x_a+x_b;
                        }
                    }
                    else if (x_a > x_b) {
                        if(x_a - 1 > x_b - 1){
                            return x_a;
                        }
                    }
                    return x_a-x_b;
                };
                Int d = b(2, 3);
                Int u = b(3, 3);
                Int j = b(4, 3);
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(-1, interpreter.getManager().getGlobalFrame().getValue("d").orElseThrow().getValue());
        Assertions.assertEquals(6, interpreter.getManager().getGlobalFrame().getValue("u").orElseThrow().getValue());
        Assertions.assertEquals(4, interpreter.getManager().getGlobalFrame().getValue("j").orElseThrow().getValue());
    }

    @Test
    void shouldHandleVariableShadowing() throws ParsingException, IOException {
        // given
        final var code = """
                Int x = 10;
                let a = with()->Int{
                    Int x = 20;
                    return x;
                };
                Int b = a();
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(20, interpreter.getManager().getGlobalFrame().getValue("b").orElseThrow().getValue());
        Assertions.assertEquals(10, interpreter.getManager().getGlobalFrame().getValue("x").orElseThrow().getValue());
    }

    @Test
    void shouldHandlePassByReference() throws ParsingException, IOException {
        // given
        final var code = """
                Int x = 10;
                let mutate = with(Int a)->Void{
                    a = 20;
                };
                mutate(x);
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(20, interpreter.getManager().getGlobalFrame().getValue("x").orElseThrow().getValue());
    }

    @Test
    void shouldHanldeNestedFunctionCalls() throws ParsingException, IOException {
        // given
        final var code = """
                Int x = 10;
                let a = with(Int x)->Int{
                    x = 30;
                    return x;
                };
                let b = with()->Int{
                    Int x = 20;
                    return a(x);
                };
                Int c = b();
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(30, interpreter.getManager().getGlobalFrame().getValue("c").orElseThrow().getValue());
        Assertions.assertEquals(10, interpreter.getManager().getGlobalFrame().getValue("x").orElseThrow().getValue());
    }

    @Test
    void shouldHanldeRecursionCalls() throws ParsingException, IOException {
        // given
        final var code = """
                lam(Int)->Int a;
                a = with(Int x)->Int{
                    if(x==0){
                        return -1;
                    }
                    return a(x-10);
                };
                Int c = a(30);
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(-1, interpreter.getManager().getGlobalFrame().getValue("c").orElseThrow().getValue());
    }

    @Test
    void shouldHanldePipeExpression() throws ParsingException, IOException {
        // given
        final var code = """
                let id = with(Int x)->Int{
                    return x;
                };
                let inc = with(Int x)->Int{
                    return x+1;
                };
                let add = with(Int x, Int a)->Int{
                    return (x |> id)+(a |> id);
                };
                Int a = 1 |> inc |> inc |> add 2;
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(5, interpreter.getManager().getGlobalFrame().getValue("a").orElseThrow().getValue());
    }

    @Test
    void shouldHanldeLambdaCall() throws ParsingException, IOException {
        // given
        final var code = """
                let fun = with(Int x)->lam(Int)->Int{
                    return (
                    with(Int a)->Int{
                        return a;
                    }
                    );
                };
                Int a = fun(1)(2);
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(2, interpreter.getManager().getGlobalFrame().getValue("a").orElseThrow().getValue());
    }

    @Test
    void shouldHanldeLambdaCall_withClousure() throws ParsingException, IOException {
        // given
        final var code = """
                let fun = with(Int x)->lam(Int)->Int{
                    Int d = 2;
                    return (
                    with(Int a)->Int{
                        return a+x+d;
                    }
                    );
                };
                Int a = fun(1)(2);
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();
        // when
        parser.parserProgram().accept(interpreter);

        // then
        Assertions.assertEquals(5, interpreter.getManager().getGlobalFrame().getValue("a").orElseThrow().getValue());
    }

    @Test
    void shouldHanldeListComprehension() throws ParsingException, IOException {
        // given
        final var code = """
                let a = 10;
                let arr = [x+2 <| x [1,2,a] ];
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();
        // when
        parser.parserProgram().accept(interpreter);

        // then
        assertThat(interpreter.getManager().getGlobalFrame().getValue("arr").orElseThrow().getValue())
                .isEqualToComparingFieldByFieldRecursively(List.of(
                        new Variant<>(3, Integer.class),
                        new Variant<>(4, Integer.class),
                        new Variant<>(12, Integer.class)
                ));
    }

    @Test
    void shouldHanldeMusicTree() throws ParsingException, IOException {
        // given
        final var code = """
                let music = ((C, 4) q | (E, 4) q | (G, 4) q) &
                ((C, 4) q & (E, 4) q | (G, 4) q);
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);
    }

    @Test
    void shouldHanldeTemplateTree() throws ParsingException, IOException {
        // given
        final var code = """
                let temp = 0 | 1 & 2;
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);
    }

    //TODO here is very bad bug!!!!
    @Test
    void shouldHanldeConvolutionOperator() throws ParsingException, IOException {
        // given
        final var code = """
                let temp = (2 | 0 & 1) >> ([C, E, G]{dur=q});
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);
    }

    @Test
    void shouldHanldeBuiltInAtFunction() throws ParsingException, IOException {
        // given
        final var code = """
                let a = [1, 2, 3] |> at 1;
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        assertThat(interpreter.getManager().getGlobalFrame().getValue("a").orElseThrow())
                .isEqualToComparingFieldByFieldRecursively(
                        new Variant<>(2, Integer.class)
                );
    }

    @Test
    void shouldHanldeModifierExpression() throws ParsingException, IOException {
        // given
        final var code = """
                let a = [C, E, G]{dur=q} |> mel;
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
    }

    @Test
    void shouldHanldePipeExpression_WithMusicTree() throws ParsingException, IOException {
        // given
        final var code = """
                let temp = 0 | 1 & 2;
                let notes = [C, E, G];
                                
                let a = temp >> notes |> print;
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
    }

    @Test
    void shouldHanldeComplexMusicTree() throws ParsingException, IOException {
        // given
        final var code = """
                let a =[C, E, G]{dur=q, oct=4};
                let temp = 0 & 1 |2 | 0 & 2 |0;
                let temp2 = 2 & 0 & 1 | 0 | 2 | 0 |0;
                let b = temp | temp & temp2;
                                                """;
        var expectedTree = new IndexTree()
                .appendToSequence(new GroupNode<>(List.of(new SimpleNode<>(0), new SimpleNode<>(1))))
                .appendToSequence(2)
                .appendToSequence(new GroupNode<>(List.of(new SimpleNode<>(0), new SimpleNode<>(2))))
                .appendToSequence(0);
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        assertThat(interpreter.getManager().get("temp").orElseThrow().getValue())
                .isEqualToComparingFieldByFieldRecursively(expectedTree);
    }

    @Test
    void shouldHanldeForLoop() throws ParsingException, IOException {
        // given
        final var code = """
                Int acc=0;
                for(Int a in [1,2,3,2+2]){
                    acc=acc+a;
                }
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        assertThat(interpreter.getManager().getGlobalFrame().getValue("acc").orElseThrow())
                .isEqualToComparingFieldByFieldRecursively(
                        new Variant<>(10, Integer.class)
                );
    }

    @Test
    void shouldThrow_WhenWrongValueTypeAssigment() throws ParsingException, IOException {
        // given
        final var code = """
                let a = 1;
                a = 2.0;
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        assertThatThrownBy(() -> parser.parserProgram().accept(interpreter))
                .hasMessageStartingWith("SEMANTIC ERROR cannot assign value of DoubleType to variable of IntType");

    }

    @Test
    void shouldHandleVariablesWithSameName_WhenInDifferentScope() throws ParsingException, IOException {
        // given
        final var code = """
                let a = 1;
                if(1==1){
                    let a = 2;
                    if(2==2){
                        let a = 3;
                    }
                }
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);

        // then
        assertThat(interpreter.getManager().getGlobalFrame().getValue("a").orElseThrow())
                .isEqualToComparingFieldByFieldRecursively(
                        new Variant<>(1, Integer.class)
                );

    }

    @Test
    void shouldPrintObjects() throws ParsingException, IOException {
        // given
        final var code = """
                let a = 1;
                print(a);
                let b = (C#, 4) q;
                print(b);
                let c = (C, 4) q | (D, 4) e;
                print(c);
                print(0 |(1+a));
                """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);
    }
}
