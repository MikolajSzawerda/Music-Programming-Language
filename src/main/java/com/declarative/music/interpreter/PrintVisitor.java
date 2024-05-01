package com.declarative.music.interpreter;

import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.VariableReference;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.arithmetic.MinusUnaryExpression;
import com.declarative.music.parser.production.expression.arithmetic.MulExpression;
import com.declarative.music.parser.production.expression.arithmetic.PlusUnaryExpression;
import com.declarative.music.parser.production.expression.array.ArrayExpression;
import com.declarative.music.parser.production.expression.array.ListComprehension;
import com.declarative.music.parser.production.expression.array.RangeExpression;
import com.declarative.music.parser.production.expression.lambda.LambdaCall;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.modifier.ModifierExpression;
import com.declarative.music.parser.production.expression.music.ConvolutionExpression;
import com.declarative.music.parser.production.expression.music.NoteExpression;
import com.declarative.music.parser.production.expression.pipe.InlineFuncCall;
import com.declarative.music.parser.production.expression.pipe.PipeExpression;
import com.declarative.music.parser.production.expression.relation.AndExpression;
import com.declarative.music.parser.production.expression.relation.EqExpression;
import com.declarative.music.parser.production.expression.relation.OrExpression;
import com.declarative.music.parser.production.literal.IntLiteral;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.OutputStream;

@RequiredArgsConstructor
public class PrintVisitor implements Visitor {
    private final OutputStream outputStream;
    private int indentation = 0;

    private String indent(final String text) {
        return "    ".repeat(indentation) + text;
    }

    @SneakyThrows
    private void write(final String text) {
        outputStream.write((indent(text) + "\n").getBytes());
    }

    @Override
    public void visit(final AddExpression addExpression) {
        write("addExpression:");
        indentation++;
        addExpression.left().accept(this);
        addExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final LambdaExpression lambdaExpression) {
        write("lambda:");
        indentation++;
        write("parameters:");
        indentation++;
        for (final var param : lambdaExpression.parameters().parameters()) {
            write("%s: %s".formatted(param.name(), param.type()));
        }
        indentation--;
        write("returns: %s".formatted(lambdaExpression.returnType()));
        lambdaExpression.instructions().accept(this);
        indentation--;
    }

    @Override
    public void visit(final AssigmentStatement assigmentStatement) {
        write("assigment:");
        indentation++;
        write("variable: %s".formatted(assigmentStatement.identifier()));
        write("value:");
        indentation++;
        assigmentStatement.value().accept(this);
        indentation -= 2;
    }

    @Override
    public void visit(final Block block) {
        write("block:");
        indentation++;
        block.statements().forEach(statement -> statement.accept(this));
        indentation--;
    }

    @Override
    public void visit(final Declaration declaration) {
        write("declaration:");
        indentation++;
        write("type: %s".formatted(declaration.type()));
        write("name: %s".formatted(declaration.name()));
        write("value:");
        indentation++;
        declaration.value().accept(this);
        indentation -= 2;
    }

    @Override
    public void visit(final IfStatement ifStatement) {
        write("ifStatement");
        indentation++;
        write("condition:");
        indentation++;
        ifStatement.condition().accept(this);
        indentation--;
        ifStatement.instructions().accept(this);
        ifStatement.otherwise().accept(this);
        indentation--;
    }

    @Override
    public void visit(final Program program) {

    }

    @Override
    public void visit(final MinusUnaryExpression minusUnaryExpression) {

    }

    @Override
    public void visit(final MulExpression mulExpression) {

    }

    @Override
    public void visit(final PlusUnaryExpression plusUnaryExpression) {

    }

    @Override
    public void visit(final ArrayExpression arrayExpression) {

    }

    @Override
    public void visit(final ListComprehension listComprehension) {

    }

    @Override
    public void visit(final RangeExpression rangeExpression) {

    }

    @Override
    public void visit(final LambdaCall lambdaCall) {

    }

    @Override
    public void visit(final ModifierExpression modifierExpression) {

    }

    @Override
    public void visit(final ConvolutionExpression convolutionExpression) {

    }

    @Override
    public void visit(final NoteExpression noteExpression) {

    }

    @Override
    public void visit(final InlineFuncCall inlineFuncCall) {

    }

    @Override
    public void visit(final PipeExpression pipeExpression) {

    }

    @Override
    public void visit(final AndExpression andExpression) {

    }

    @Override
    public void visit(final EqExpression eqExpression) {

    }

    @Override
    public void visit(final OrExpression orExpression) {

    }

    @Override
    public void visit(final CastExpresion castExpresion) {

    }

    @Override
    public void visit(final VariableReference variableReference) {

    }

    @Override
    public void visit(final IntLiteral intLiteral) {
        write("IntLiteral: %d".formatted(intLiteral.value()));
    }
}
