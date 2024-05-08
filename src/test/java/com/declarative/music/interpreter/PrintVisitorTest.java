package com.declarative.music.interpreter;

import com.declarative.music.lexer.token.Position;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.type.SimpleType;
import com.declarative.music.parser.production.type.Types;
import org.junit.jupiter.api.Test;

import java.util.List;

class PrintVisitorTest {
    private static final Position POS = new Position(0, 0);

    @Test
    void shouldPrintIntLiteral() {
        final var visitor = new PrintVisitor(System.out);
        new Block(List.of(
                new AddExpression(new IntLiteral(1, POS), new IntLiteral(2, POS)),
                new LambdaExpression(new Parameters(
                        List.of(
                                new Parameter(new SimpleType(Types.Int, POS), "a"),
                                new Parameter(new SimpleType(Types.String, POS), "b")
                        )
                ),
                        new SimpleType(Types.Int, POS),
                        new Block(List.of(new AddExpression(new IntLiteral(1, POS), new IntLiteral(2, POS))), POS)
                        , POS),
                new AssigmentStatement("a", new IntLiteral(2, POS), POS),
                new Declaration(new SimpleType(Types.Int, POS), "b", new IntLiteral(2, POS))
        ), POS).accept(visitor);
    }
}