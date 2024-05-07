package com.declarative.music.interpreter;

import java.io.OutputStream;

import com.declarative.music.parser.production.AssigmentStatement;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Declaration;
import com.declarative.music.parser.production.IfStatement;
import com.declarative.music.parser.production.Program;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.VariableReference;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.arithmetic.MinusUnaryExpression;
import com.declarative.music.parser.production.expression.arithmetic.MulExpression;
import com.declarative.music.parser.production.expression.arithmetic.PlusUnaryExpression;
import com.declarative.music.parser.production.expression.array.ArrayExpression;
import com.declarative.music.parser.production.expression.array.ListComprehension;
import com.declarative.music.parser.production.expression.array.RangeExpression;
import com.declarative.music.parser.production.expression.lambda.FunctionCall;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.modifier.ModifierExpression;
import com.declarative.music.parser.production.expression.music.ConvolutionExpression;
import com.declarative.music.parser.production.expression.music.NoteExpression;
import com.declarative.music.parser.production.expression.music.ParallerExpression;
import com.declarative.music.parser.production.expression.music.SequenceExpression;
import com.declarative.music.parser.production.expression.pipe.InlineFuncCall;
import com.declarative.music.parser.production.expression.pipe.PipeExpression;
import com.declarative.music.parser.production.expression.relation.AndExpression;
import com.declarative.music.parser.production.expression.relation.EqExpression;
import com.declarative.music.parser.production.expression.relation.OrExpression;
import com.declarative.music.parser.production.literal.FloatLiteral;
import com.declarative.music.parser.production.literal.IntLiteral;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


@RequiredArgsConstructor
public class PrintVisitor implements Visitor
{
    private final OutputStream outputStream;
    private int indentation = 0;

    private String indent(final String text)
    {
        return "    ".repeat(indentation) + text;
    }

    @SneakyThrows
    private void write(final String text)
    {
        outputStream.write((indent(text) + "\n").getBytes());
    }

    @Override
    public void visit(final AddExpression addExpression)
    {
        write("addExpression:");
        indentation++;
        addExpression.left().accept(this);
        addExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final LambdaExpression lambdaExpression)
    {
        write("lambda:");
        indentation++;
        write("parameters:");
        indentation++;
        for (final var param : lambdaExpression.parameters().parameters())
        {
            write("%s: %s".formatted(param.name(), param.type()));
        }
        indentation--;
        write("returns: %s".formatted(lambdaExpression.returnType()));
        lambdaExpression.instructions().accept(this);
        indentation--;
    }

    @Override
    public void visit(final AssigmentStatement assigmentStatement)
    {
        write("assigment:");
        indentation++;
        write("variable: %s".formatted(assigmentStatement.identifier()));
        assigmentStatement.value().accept(this);
        indentation--;
    }

    @Override
    public void visit(final Block block)
    {
        write("block:");
        indentation++;
        block.statements().forEach(statement -> statement.accept(this));
        indentation--;
    }

    @Override
    public void visit(final Declaration declaration)
    {
        write("declaration:");
        indentation++;
        write("type: %s".formatted(declaration.type()));
        write("name: %s".formatted(declaration.name()));
        if (declaration.value() == null)
        {
            indentation--;
            return;
        }
        declaration.value().accept(this);
        indentation--;
    }

    @Override
    public void visit(final IfStatement ifStatement)
    {
        write("ifStatement");
        indentation++;
        write("condition:");
        indentation++;
        ifStatement.condition().accept(this);
        indentation--;
        ifStatement.instructions().accept(this);
        if (ifStatement.otherwise() != null)
        {
            ifStatement.otherwise().accept(this);
        }
        indentation--;
    }

    @Override
    public void visit(final Program program)
    {
        program.statements().forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visit(final MinusUnaryExpression minusUnaryExpression)
    {
        write("minusUnaryExpression:");
        indentation++;
        minusUnaryExpression.value().accept(this);
        indentation--;
    }

    @Override
    public void visit(final MulExpression mulExpression)
    {

    }

    @Override
    public void visit(final PlusUnaryExpression plusUnaryExpression)
    {
        write("plusUnaryExpression:");
        indentation++;
        plusUnaryExpression.value().accept(this);
        indentation--;
    }

    @Override
    public void visit(final ArrayExpression arrayExpression)
    {
        write("arrayExpression:");
        indentation++;
        for (var item : arrayExpression.items())
        {
            item.accept(this);
        }
        indentation--;
    }

    @Override
    public void visit(final ListComprehension listComprehension)
    {

    }

    @Override
    public void visit(final RangeExpression rangeExpression)
    {

    }

    @Override
    public void visit(final FunctionCall functionCall)
    {

    }

    @Override
    public void visit(final ModifierExpression modifierExpression)
    {
        write("modifierExpression:");
        indentation++;

        write("modifier:");
        indentation++;
        for (var modifier : modifierExpression.modifier().modifiers())
        {
            write("name: %s".formatted(modifier.name()));
            write("value:");
            indentation++;
            modifier.expression().accept(this);
            indentation--;
        }
        indentation--;
        modifierExpression.modified().accept(this);
        indentation--;

    }

    @Override
    public void visit(final ConvolutionExpression convolutionExpression)
    {

    }

    @Override
    public void visit(final NoteExpression noteExpression)
    {
        write("noteExpression:");
        indentation++;
        if (noteExpression.pitch() != null)
        {
            write("pitch: %s".formatted(noteExpression.pitch()));
        }
        if (noteExpression.octave() != null)
        {
            write("octave:");
            indentation++;
            noteExpression.octave().accept(this);
            indentation--;
        }
        if (noteExpression.duration() != null)
        {
            write("duration: %s".formatted(noteExpression.duration()));
        }
        indentation--;
    }

    @Override
    public void visit(final InlineFuncCall inlineFuncCall)
    {
        write("inlineFuncCall: %s".formatted(inlineFuncCall.name()));
        if (inlineFuncCall.arguments().isEmpty())
        {
            return;
        }
        indentation++;
        write("arguments:");
        indentation++;
        for (var arg : inlineFuncCall.arguments())
        {
            arg.accept(this);
        }
        indentation -= 2;
    }

    @Override
    public void visit(final PipeExpression pipeExpression)
    {
        write("pipeExpression:");
        indentation++;
        pipeExpression.right().accept(this);
        pipeExpression.left().accept(this);
        indentation--;
    }

    @Override
    public void visit(final AndExpression andExpression)
    {

    }

    @Override
    public void visit(final EqExpression eqExpression)
    {

    }

    @Override
    public void visit(final OrExpression orExpression)
    {

    }

    @Override
    public void visit(final CastExpresion castExpresion)
    {
        write("castExpression:");
        indentation++;
        write("type: %s".formatted(castExpresion.type()));
        castExpresion.value().accept(this);
        indentation--;
    }

    @Override
    public void visit(final VariableReference variableReference)
    {
        write("variableReference: %s".formatted(variableReference.name()));
    }

    @Override
    public void visit(final IntLiteral intLiteral)
    {
        write("IntLiteral: %d".formatted(intLiteral.value()));
    }

    @Override
    public void visit(final SequenceExpression sequenceExpression)
    {
        write("sequenceExpression:");
        indentation++;
        sequenceExpression.left().accept(this);
        sequenceExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final ParallerExpression parallerExpression)
    {
        write("parallerExpression:");
        indentation++;
        parallerExpression.left().accept(this);
        parallerExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final FloatLiteral floatLiteral)
    {
        write("floatLiteral: %f".formatted(floatLiteral.value()));
    }
}
