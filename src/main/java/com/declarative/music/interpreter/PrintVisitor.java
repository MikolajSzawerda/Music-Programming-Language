package com.declarative.music.interpreter;

import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.assign.*;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.VariableReference;
import com.declarative.music.parser.production.expression.arithmetic.*;
import com.declarative.music.parser.production.expression.array.ArrayExpression;
import com.declarative.music.parser.production.expression.array.ListComprehension;
import com.declarative.music.parser.production.expression.array.RangeExpression;
import com.declarative.music.parser.production.expression.lambda.FunctionCall;
import com.declarative.music.parser.production.expression.lambda.LambdaCall;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.modifier.ModifierExpression;
import com.declarative.music.parser.production.expression.music.ConvolutionExpression;
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

    private void writeHeader(final Interpretable item) {
        write("%s:".formatted(item.getClass().getSimpleName()));
    }

    @Override
    public void visit(final AddExpression addExpression) {
        writeHeader(addExpression);
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
        assigmentStatement.value().accept(this);
        indentation--;
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
        if (declaration.value() == null) {
            indentation--;
            return;
        }
        declaration.value().accept(this);
        indentation--;
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
        if (ifStatement.otherwise() != null) {
            ifStatement.otherwise().accept(this);
        }
        indentation--;
    }

    @Override
    public void visit(final Program program) {
        program.statements().forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visit(final MinusUnaryExpression minusUnaryExpression) {
        write("minusUnaryExpression:");
        indentation++;
        minusUnaryExpression.value().accept(this);
        indentation--;
    }

    @Override
    public void visit(final MulExpression mulExpression) {
        writeHeader(mulExpression);
        indentation++;
        mulExpression.left().accept(this);
        mulExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final PlusUnaryExpression plusUnaryExpression) {
        write("plusUnaryExpression:");
        indentation++;
        plusUnaryExpression.value().accept(this);
        indentation--;
    }

    @Override
    public void visit(final ArrayExpression arrayExpression) {
        write("arrayExpression:");
        indentation++;
        for (final var item : arrayExpression.items()) {
            item.accept(this);
        }
        indentation--;
    }

    @Override
    public void visit(final ListComprehension listComprehension) {
        writeHeader(listComprehension);
        indentation++;
        listComprehension.tempName().accept(this);
        write("mapper: ");
        indentation++;
        listComprehension.mapper().accept(this);
        indentation--;
        listComprehension.iterable().accept(this);
        indentation--;
    }

    @Override
    public void visit(final RangeExpression rangeExpression) {
        writeHeader(rangeExpression);
        indentation++;
        rangeExpression.start().accept(this);
        rangeExpression.end().accept(this);
        indentation--;
    }

    @Override
    public void visit(final FunctionCall functionCall) {
        writeHeader(functionCall);
        indentation++;
        write("name: %s".formatted(functionCall.name()));
        write("arguments:");
        indentation++;

        for (final var expr : functionCall.arguments()) {
            expr.accept(this);
        }
        indentation -= 2;
    }

    @Override
    public void visit(final ModifierExpression modifierExpression) {
        write("modifierExpression:");
        indentation++;

        write("modifier:");
        indentation++;
        for (final var modifier : modifierExpression.modifier().modifiers()) {
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
    public void visit(final ConvolutionExpression convolutionExpression) {
        writeHeader(convolutionExpression);
        indentation++;
        convolutionExpression.left().accept(this);
        convolutionExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final NoteExpression noteExpression) {
        write("noteExpression:");
        indentation++;
        if (noteExpression.pitch() != null) {
            write("pitch: %s".formatted(noteExpression.pitch()));
        }
        if (noteExpression.octave() != null) {
            write("octave:");
            indentation++;
            noteExpression.octave().accept(this);
            indentation--;
        }
        if (noteExpression.duration() != null) {
            write("duration: %s".formatted(noteExpression.duration()));
        }
        indentation--;
    }

    @Override
    public void visit(final InlineFuncCall inlineFuncCall) {
        write("inlineFuncCall: %s".formatted(inlineFuncCall.name()));
        if (inlineFuncCall.arguments().isEmpty()) {
            return;
        }
        indentation++;
        write("arguments:");
        indentation++;
        for (final var arg : inlineFuncCall.arguments()) {
            arg.accept(this);
        }
        indentation -= 2;
    }

    @Override
    public void visit(final PipeExpression pipeExpression) {
        write("pipeExpression:");
        indentation++;
        pipeExpression.right().accept(this);
        pipeExpression.left().accept(this);
        indentation--;
    }

    @Override
    public void visit(final AndExpression andExpression) {
        writeHeader(andExpression);
        indentation++;
        andExpression.left().accept(this);
        andExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final EqExpression eqExpression) {
        writeHeader(eqExpression);
        indentation++;
        eqExpression.left().accept(this);
        eqExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final OrExpression orExpression) {
        writeHeader(orExpression);
        indentation++;
        orExpression.left().accept(this);
        orExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final CastExpresion castExpresion) {
        write("castExpression:");
        indentation++;
        write("type: %s".formatted(castExpresion.type()));
        castExpresion.value().accept(this);
        indentation--;
    }

    @Override
    public void visit(final VariableReference variableReference) {
        write("variableReference: %s".formatted(variableReference.name()));
    }

    @Override
    public void visit(final IntLiteral intLiteral) {
        write("IntLiteral: %d".formatted(intLiteral.value()));
    }

    @Override
    public void visit(final SequenceExpression sequenceExpression) {
        write("sequenceExpression:");
        indentation++;
        sequenceExpression.left().accept(this);
        sequenceExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final ParallerExpression parallerExpression) {
        write("parallerExpression:");
        indentation++;
        parallerExpression.left().accept(this);
        parallerExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final FloatLiteral floatLiteral) {
        write("floatLiteral: %f".formatted(floatLiteral.value()));
    }

    private void writeAssigmentStatement(final AssignStmt statement) {
        writeHeader(statement);
        indentation++;
        write("variable: %s".formatted(statement.identifier()));
        statement.value().accept(this);
        indentation--;
    }

    @Override
    public void visit(final DivAssignStatement divAssignStatement) {
        writeAssigmentStatement(divAssignStatement);
    }

    @Override
    public void visit(final MinusAssignStatement minusAssignStatement) {
        writeAssigmentStatement(minusAssignStatement);

    }

    @Override
    public void visit(final ModuloAssignStatement moduloAssignStatement) {
        writeAssigmentStatement(moduloAssignStatement);

    }

    @Override
    public void visit(final MulAssignStatement mulAssignStatement) {
        writeAssigmentStatement(mulAssignStatement);

    }

    @Override
    public void visit(final ParalerAssignStatement paralerAssignStatement) {
        writeAssigmentStatement(paralerAssignStatement);

    }

    @Override
    public void visit(final PlusAssignStatement plusAssignStatement) {
        writeAssigmentStatement(plusAssignStatement);

    }

    @Override
    public void visit(final PowAssignStatement powAssignStatement) {
        writeAssigmentStatement(powAssignStatement);

    }

    @Override
    public void visit(final SequenceAssignStatement sequenceAssignStatement) {
        writeAssigmentStatement(sequenceAssignStatement);

    }

    @Override
    public void visit(final DivExpression divExpression) {
        writeHeader(divExpression);
        indentation++;
        divExpression.left().accept(this);
        divExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final MinusExpression minusExpression) {
        writeHeader(minusExpression);
        indentation++;
        minusExpression.left().accept(this);
        minusExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final ModuloExpression moduloExpression) {
        writeHeader(moduloExpression);
        indentation++;
        moduloExpression.left().accept(this);
        moduloExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final PowExpression powExpression) {
        writeHeader(powExpression);
        indentation++;
        powExpression.left().accept(this);
        powExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final LambdaCall lambdaCall) {
        writeHeader(lambdaCall);
        indentation++;
        write("arguments:");
        indentation++;

        for (final var expr : lambdaCall.arguments()) {
            expr.accept(this);
        }
        indentation -= 2;
    }

    @Override
    public void visit(final GreaterEqExpression greaterEqExpression) {
        writeHeader(greaterEqExpression);
        indentation++;
        greaterEqExpression.left().accept(this);
        greaterEqExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final GreaterExpression greaterExpression) {
        writeHeader(greaterExpression);
        indentation++;
        greaterExpression.left().accept(this);
        greaterExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final LessEqExpression lessEqExpression) {
        writeHeader(lessEqExpression);
        indentation++;
        lessEqExpression.left().accept(this);
        lessEqExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final LessExpression lessExpression) {
        writeHeader(lessExpression);
        indentation++;
        lessExpression.left().accept(this);
        lessExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final NegateExpression negateExpression) {
        writeHeader(negateExpression);
        indentation++;
        negateExpression.accept(this);
        indentation--;
    }

    @Override
    public void visit(final NotEqExpression notEqExpression) {
        writeHeader(notEqExpression);
        indentation++;
        notEqExpression.left().accept(this);
        notEqExpression.right().accept(this);
        indentation--;
    }

    @Override
    public void visit(final BoolLiteral boolLiteral) {
        write("boolLiteral: %s".formatted(boolLiteral.value()));
    }

    @Override
    public void visit(final StringLiter stringLiter) {
        write("stringLiter: %s".formatted(stringLiter.value()));

    }

    @Override
    public void visit(final ForStatement forStatement) {
        writeHeader(forStatement);
        indentation++;
        forStatement.declaration().accept(this);
        forStatement.iterable().accept(this);
        forStatement.instructions().accept(this);
        indentation--;
    }

    @Override
    public void visit(final ReturnStatement returnStatement) {
        writeHeader(returnStatement);
        indentation++;
        returnStatement.value().accept(this);
        indentation--;
    }
}
