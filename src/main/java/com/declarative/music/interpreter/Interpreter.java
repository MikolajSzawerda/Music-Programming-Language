package com.declarative.music.interpreter;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.declarative.music.interpreter.values.IntReference;
import com.declarative.music.parser.production.AssigmentStatement;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Declaration;
import com.declarative.music.parser.production.ForStatement;
import com.declarative.music.parser.production.IfStatement;
import com.declarative.music.parser.production.Program;
import com.declarative.music.parser.production.ReturnStatement;
import com.declarative.music.parser.production.assign.DivAssignStatement;
import com.declarative.music.parser.production.assign.MinusAssignStatement;
import com.declarative.music.parser.production.assign.ModuloAssignStatement;
import com.declarative.music.parser.production.assign.MulAssignStatement;
import com.declarative.music.parser.production.assign.ParalerAssignStatement;
import com.declarative.music.parser.production.assign.PlusAssignStatement;
import com.declarative.music.parser.production.assign.PowAssignStatement;
import com.declarative.music.parser.production.assign.SequenceAssignStatement;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.VariableReference;
import com.declarative.music.parser.production.expression.arithmetic.AddExpression;
import com.declarative.music.parser.production.expression.arithmetic.DivExpression;
import com.declarative.music.parser.production.expression.arithmetic.MinusExpression;
import com.declarative.music.parser.production.expression.arithmetic.MinusUnaryExpression;
import com.declarative.music.parser.production.expression.arithmetic.ModuloExpression;
import com.declarative.music.parser.production.expression.arithmetic.MulExpression;
import com.declarative.music.parser.production.expression.arithmetic.PlusUnaryExpression;
import com.declarative.music.parser.production.expression.arithmetic.PowExpression;
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
import com.declarative.music.parser.production.expression.relation.AndExpression;
import com.declarative.music.parser.production.expression.relation.EqExpression;
import com.declarative.music.parser.production.expression.relation.GreaterEqExpression;
import com.declarative.music.parser.production.expression.relation.GreaterExpression;
import com.declarative.music.parser.production.expression.relation.LessEqExpression;
import com.declarative.music.parser.production.expression.relation.LessExpression;
import com.declarative.music.parser.production.expression.relation.NegateExpression;
import com.declarative.music.parser.production.expression.relation.NotEqExpression;
import com.declarative.music.parser.production.expression.relation.OrExpression;
import com.declarative.music.parser.production.literal.BoolLiteral;
import com.declarative.music.parser.production.literal.FloatLiteral;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.literal.StringLiter;
import com.declarative.music.parser.production.type.ArrayType;
import com.declarative.music.parser.production.type.InferenceType;
import com.declarative.music.parser.production.type.LambdaType;
import com.declarative.music.parser.production.type.SimpleType;

import lombok.Getter;


public class Interpreter implements Visitor
{
    @Getter
    private final ContextManager manager;
    @Getter
    private Object currentValue;
    private boolean returned = false;

    public Interpreter(final ContextManager manager)
    {
        this.manager = manager;
    }

    public Interpreter()
    {
        manager = new ContextManager();
    }

    @Override
    public void visit(final AddExpression addExpression)
    {
        addExpression.left().accept(this);
        var left = currentValue;
        Integer leftValue;
        Integer rightValue;
        if (left.getClass() != Integer.class && left.getClass() != IntReference.class)
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        if (left.getClass() == Integer.class)
        {
            leftValue = (Integer) left;
        }
        else
        {
            leftValue = (Integer) ((IntReference) left).getValue();
        }

        addExpression.right().accept(this);
        if (currentValue.getClass() != Integer.class && currentValue.getClass() != IntReference.class)
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }
        if (currentValue.getClass() == Integer.class)
        {
            rightValue = (Integer) currentValue;
        }
        else
        {
            rightValue = (Integer) ((IntReference) currentValue).getValue();
        }
        currentValue = leftValue + rightValue;
    }

    @Override
    public void visit(final LambdaExpression lambdaExpression)
    {
        var frame = manager.getFrames().empty() ? manager.getGlobalFrame().copy() : manager.getFrames().peek().copy();
        currentValue = new LambdaClousure(lambdaExpression, frame);
    }

    @Override
    public void visit(final AssigmentStatement assigmentStatement)
    {
        assigmentStatement.value().accept(this);
        if (!manager.contains(assigmentStatement.identifier()))
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }
        manager.save(assigmentStatement.identifier(), currentValue);
        currentValue = null;
    }

    @Override
    public void visit(final Block block)
    {
        manager.startNewScope();
        block.statements().forEach(stmt -> {
            if (!returned)
            {
                stmt.accept(this);
            }
        });
        manager.leaveNewScope();
    }

    @Override
    public void visit(final Declaration declaration)
    {
        if (declaration.value() == null)
        {
            manager.save(declaration.name(), null);
            return;
        }
        declaration.value().accept(this);
        manager.declare(declaration.name(), currentValue);
        currentValue = null;
    }

    @Override
    public void visit(final IfStatement ifStatement)
    {
        ifStatement.condition().accept(this);
        if ((Boolean) currentValue)
        {
            currentValue = null;
            ifStatement.instructions().accept(this);
        }
        else if (ifStatement.otherwise() != null)
        {
            ifStatement.otherwise().accept(this);
        }

    }

    @Override
    public void visit(final Program program)
    {
        program.statements().forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visit(final MinusUnaryExpression minusUnaryExpression)
    {
        minusUnaryExpression.value().accept(this);
        currentValue = -(Integer) currentValue;
    }

    @Override
    public void visit(final MulExpression mulExpression)
    {
        throw new UnsupportedOperationException("MulExpression not implemented!");

    }

    @Override
    public void visit(final PlusUnaryExpression plusUnaryExpression)
    {
        throw new UnsupportedOperationException("PlusUnaryExpression not implemented!");

    }

    @Override
    public void visit(final ArrayExpression arrayExpression)
    {
        throw new UnsupportedOperationException("ArrayExpression not implemented!");

    }

    @Override
    public void visit(final ListComprehension listComprehension)
    {
        throw new UnsupportedOperationException("ListComprehension not implemented!");

    }

    @Override
    public void visit(final RangeExpression rangeExpression)
    {
        throw new UnsupportedOperationException("RangeExpression not implemented!");

    }

    @Override
    public void visit(final FunctionCall functionCall)
    {
        var lambda = manager.get(functionCall.name()).orElseThrow();
        var clousure = (LambdaClousure) lambda.getValue();
        var stmts = clousure.expression();

        var arguments = new HashMap<String, Object>();
        zip(stmts.parameters().parameters(), functionCall.arguments())
            .forEach(entry -> {
                entry.getValue().accept(this);
                arguments.put(entry.getKey().name(), currentValue);
                currentValue = null;
            });
        manager.enterNewFrame(clousure.frame());
        arguments.forEach(manager::declare);
        stmts.instructions().accept(this);
        manager.leaveFrame();
        returned = false;
    }

    @Override
    public void visit(final ModifierExpression modifierExpression)
    {
        throw new UnsupportedOperationException("ModifierExpression not implemented!");

    }

    @Override
    public void visit(final ConvolutionExpression convolutionExpression)
    {
        throw new UnsupportedOperationException("ConvolutionExpression not implemented!");

    }

    @Override
    public void visit(final NoteExpression noteExpression)
    {
        throw new UnsupportedOperationException("NoteExpression not implemented!");

    }

    @Override
    public void visit(final InlineFuncCall inlineFuncCall)
    {
        var lambda = (LambdaClousure) manager.get(inlineFuncCall.name()).orElseThrow().getValue();
        var stmts = lambda.expression();
        var arguments = new HashMap<String, Object>();
        var params = stmts.parameters().parameters();
        for (int i = 0; i < params.size(); i++)
        {
            arguments.put(params.get(i).name(), currentValue);
            if (i < inlineFuncCall.arguments().size())
            {
                inlineFuncCall.arguments().get(i).accept(this);
            }
        }
        manager.enterNewFrame();
        arguments.forEach(manager::save);
        stmts.instructions().accept(this);
        manager.leaveFrame();
        returned = false;
    }

    @Override
    public void visit(final PipeExpression pipeExpression)
    {
        pipeExpression.left().accept(this);
        pipeExpression.right().accept(this);
    }

    @Override
    public void visit(final AndExpression andExpression)
    {
        throw new UnsupportedOperationException("AndExpression not implemented!");

    }

    @Override
    public void visit(final EqExpression eqExpression)
    {

        eqExpression.left().accept(this);
        var left = currentValue;
        Integer leftValue;
        Integer rightValue;
        if (left.getClass() != Integer.class && left.getClass() != IntReference.class)
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        if (left.getClass() == Integer.class)
        {
            leftValue = (Integer) left;
        }
        else
        {
            leftValue = (Integer) ((IntReference) left).getValue();
        }

        eqExpression.right().accept(this);
        if (currentValue.getClass() != Integer.class && currentValue.getClass() != IntReference.class)
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }
        if (currentValue.getClass() == Integer.class)
        {
            rightValue = (Integer) currentValue;
        }
        else
        {
            rightValue = (Integer) ((IntReference) currentValue).getValue();
        }
        currentValue = leftValue.equals(rightValue);
    }

    @Override
    public void visit(final OrExpression orExpression)
    {
        throw new UnsupportedOperationException("OrExpression not implemented!");

    }

    @Override
    public void visit(final CastExpresion castExpresion)
    {
        throw new UnsupportedOperationException("CastExpresion not implemented!");

    }

    @Override
    public void visit(final VariableReference variableReference)
    {
        currentValue = manager.get(variableReference.name()).orElseThrow();
    }

    @Override
    public void visit(final IntLiteral intLiteral)
    {
        currentValue = intLiteral.value();
    }

    @Override
    public void visit(final SequenceExpression sequenceExpression)
    {
        throw new UnsupportedOperationException("SequenceExpression not implemented!");

    }

    @Override
    public void visit(final ParallerExpression parallerExpression)
    {
        throw new UnsupportedOperationException("ParallerExpression not implemented!");

    }

    @Override
    public void visit(final FloatLiteral floatLiteral)
    {
        currentValue = floatLiteral.value();
    }

    @Override
    public void visit(final DivAssignStatement divAssignStatement)
    {
        throw new UnsupportedOperationException("DivAssignStatement not implemented!");

    }

    @Override
    public void visit(final MinusAssignStatement minusAssignStatement)
    {
        throw new UnsupportedOperationException("MinusAssignStatement not implemented!");

    }

    @Override
    public void visit(final ModuloAssignStatement moduloAssignStatement)
    {
        throw new UnsupportedOperationException("ModuloAssignStatement not implemented!");

    }

    @Override
    public void visit(final MulAssignStatement mulAssignStatement)
    {
        throw new UnsupportedOperationException("MulAssignStatement not implemented!");

    }

    @Override
    public void visit(final ParalerAssignStatement paralerAssignStatement)
    {
        throw new UnsupportedOperationException("ParalerAssignStatement not implemented!");

    }

    @Override
    public void visit(final PlusAssignStatement plusAssignStatement)
    {
        throw new UnsupportedOperationException("PlusAssignStatement not implemented!");

    }

    @Override
    public void visit(final PowAssignStatement powAssignStatement)
    {
        throw new UnsupportedOperationException("PowAssignStatement not implemented!");

    }

    @Override
    public void visit(final SequenceAssignStatement sequenceAssignStatement)
    {
        throw new UnsupportedOperationException("SequenceAssignStatement not implemented!");

    }

    @Override
    public void visit(final DivExpression divExpression)
    {
        throw new UnsupportedOperationException("DivExpression not implemented!");

    }

    @Override
    public void visit(final MinusExpression minusExpression)
    {
        minusExpression.left().accept(this);
        var left = currentValue;
        Integer leftValue;
        Integer rightValue;
        if (left.getClass() != Integer.class && left.getClass() != IntReference.class)
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        if (left.getClass() == Integer.class)
        {
            leftValue = (Integer) left;
        }
        else
        {
            leftValue = (Integer) ((IntReference) left).getValue();
        }

        minusExpression.right().accept(this);
        if (currentValue.getClass() != Integer.class && currentValue.getClass() != IntReference.class)
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }
        if (currentValue.getClass() == Integer.class)
        {
            rightValue = (Integer) currentValue;
        }
        else
        {
            rightValue = (Integer) ((IntReference) currentValue).getValue();
        }
        currentValue = leftValue - rightValue;
    }

    @Override
    public void visit(final ModuloExpression moduloExpression)
    {
        throw new UnsupportedOperationException("ModuloExpression not implemented!");

    }

    @Override
    public void visit(final PowExpression powExpression)
    {
        throw new UnsupportedOperationException("PowExpression not implemented!");

    }

    @Override
    public void visit(final LambdaCall lambdaCall)
    {
        lambdaCall.call().accept(this);
        var closure = (LambdaClousure) currentValue;
        var stmts = closure.expression();
        var arguments = new HashMap<String, Object>();
        zip(stmts.parameters().parameters(), lambdaCall.arguments())
            .forEach(entry -> {
                entry.getValue().accept(this);
                arguments.put(entry.getKey().name(), currentValue);
                currentValue = null;
            });
        manager.enterNewFrame(closure.frame());

        arguments.forEach(manager::save);
        stmts.instructions().accept(this);
        manager.leaveFrame();
        returned = false;

    }

    @Override
    public void visit(final GreaterEqExpression greaterEqExpression)
    {
        throw new UnsupportedOperationException("GreaterEqExpression not implemented!");
    }

    @Override
    public void visit(final GreaterExpression greaterExpression)
    {
        greaterExpression.left().accept(this);
        var left = currentValue;
        Integer leftValue;
        Integer rightValue;
        if (left.getClass() != Integer.class && left.getClass() != IntReference.class)
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        if (left.getClass() == Integer.class)
        {
            leftValue = (Integer) left;
        }
        else
        {
            leftValue = (Integer) ((IntReference) left).getValue();
        }

        greaterExpression.right().accept(this);
        if (currentValue.getClass() != Integer.class && currentValue.getClass() != IntReference.class)
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }
        if (currentValue.getClass() == Integer.class)
        {
            rightValue = (Integer) currentValue;
        }
        else
        {
            rightValue = (Integer) ((IntReference) currentValue).getValue();
        }
        currentValue = leftValue > rightValue;
    }

    @Override
    public void visit(final LessEqExpression lessEqExpression)
    {
        throw new UnsupportedOperationException("LessEqExpression not implemented!");

    }

    @Override
    public void visit(final LessExpression lessExpression)
    {
        throw new UnsupportedOperationException("LessExpression not implemented!");

    }

    @Override
    public void visit(final NegateExpression negateExpression)
    {
        throw new UnsupportedOperationException("NegateExpression not implemented!");

    }

    @Override
    public void visit(final NotEqExpression notEqExpression)
    {
        throw new UnsupportedOperationException("NotEqExpression not implemented!");

    }

    @Override
    public void visit(final BoolLiteral boolLiteral)
    {
        throw new UnsupportedOperationException("BoolLiteral not implemented!");

    }

    @Override
    public void visit(final StringLiter stringLiter)
    {
        currentValue = stringLiter.value();
    }

    @Override
    public void visit(final ForStatement forStatement)
    {
        throw new UnsupportedOperationException("ForStatement not implemented!");

    }

    @Override
    public void visit(final ReturnStatement returnStatement)
    {
        returnStatement.value().accept(this);
        returned = true;
    }

    @Override
    public void visit(final SimpleType simpleType)
    {

    }

    @Override
    public void visit(final LambdaType lambdaType)
    {

    }

    @Override
    public void visit(final InferenceType inferenceType)
    {

    }

    @Override
    public void visit(final ArrayType arrayType)
    {

    }

    private static <T, U> Stream<AbstractMap.SimpleEntry<T, U>> zip(List<T> list1, List<U> list2)
    {
        if (list1.size() != list2.size())
        {
            throw new IllegalArgumentException("Lists must be of equal size");
        }

        return IntStream.range(0, list1.size())
            .mapToObj(i -> new AbstractMap.SimpleEntry<>(list1.get(i), list2.get(i)));
    }
}