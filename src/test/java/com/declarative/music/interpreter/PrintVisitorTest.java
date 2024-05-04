package com.declarative.music.interpreter;

import com.declarative.music.parser.Types;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.type.SimpleType;
import org.junit.jupiter.api.Test;

import java.util.List;

class PrintVisitorTest {
    @Test
    void shouldPrintIntLiteral() {
        final var visitor = new PrintVisitor(System.out);
        new Block(List.of(
                new AddExpression(new IntLiteral(1), new IntLiteral(2)),
                new LambdaExpression(new Parameters(
                        List.of(
                                new Parameter(new SimpleType(Types.Int), "a"),
                                new Parameter(new SimpleType(Types.String), "b")
                        )
                ),
                        new SimpleType(Types.Int),
                        new Block(List.of(new AddExpression(new IntLiteral(1), new IntLiteral(2))))
                ),
                new AssigmentStatement("a", new IntLiteral(2)),
                new Declaration(new SimpleType(Types.Int), "b", new IntLiteral(2))
        )).accept(visitor);
    }
}