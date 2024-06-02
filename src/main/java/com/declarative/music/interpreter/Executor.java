package com.declarative.music.interpreter;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.declarative.music.interpreter.values.LambdaClousure;
import com.declarative.music.interpreter.values.OperationRegistry;
import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.interpreter.values.music.Chord;
import com.declarative.music.interpreter.values.music.MusicNode;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.Phrase;
import com.declarative.music.interpreter.values.music.Pitch;
import com.declarative.music.interpreter.values.music.Rythm;
import com.declarative.music.parser.production.AssigmentStatement;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Declaration;
import com.declarative.music.parser.production.ForStatement;
import com.declarative.music.parser.production.IfStatement;
import com.declarative.music.parser.production.Parameters;
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
import com.declarative.music.parser.production.expression.Expression;
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


public class Executor implements Visitor
{
    @Getter
    private final ContextManager manager;
    @Getter
    private Variant<?> currentValue;
    private boolean returned = false;

    public Executor(final ContextManager manager)
    {
        this.manager = manager;
    }

    public Executor()
    {
        manager = new ContextManager();
    }

    private final static OperationRegistry addRegistry = new OperationRegistry()
        .register(Integer.class, Integer.class, Integer::sum, Integer.class);
    private final static OperationRegistry eqRegistry = new OperationRegistry()
        .register(Integer.class, Integer.class, Integer::equals, Boolean.class);
    private final static OperationRegistry greaterRegistry = new OperationRegistry()
        .register(Integer.class, Integer.class, (a, b) -> a > b, Boolean.class);
    private final static OperationRegistry minusRegistry = new OperationRegistry()
        .register(Integer.class, Integer.class, (a, b) -> a - b, Integer.class);
    private final static OperationRegistry sequenceRegistry = new OperationRegistry()
        .register(Phrase.class, Phrase.class, (a, b) -> {
            a.getNodes().addAll(b.getNodes());
            return a;
        }, Phrase.class)
        .register(Chord.class, Phrase.class, (a, b) -> {
            var c = new Phrase();
            c.getNodes().add(a);
            c.getNodes().addAll(b.getNodes());
            return c;
        }, Phrase.class)
        .register(Phrase.class, Chord.class, (a, b) -> {
            a.getNodes().add(b);
            return a;
        }, Phrase.class)
        .register(Chord.class, Chord.class, (a, b) -> {
            var c = new Phrase();
            c.getNodes().add(a);
            c.getNodes().add(b);
            return c;
        }, Phrase.class);

    private final static OperationRegistry chordRegistry = new OperationRegistry()
        .register(Chord.class, Chord.class, (a, b) -> {
            var c = new Chord();
            c.getNodes().add(a);
            c.getNodes().add(b);
            return c;
        }, Chord.class)
        .register(Chord.class, Phrase.class, (a, b) -> {
            var c = new Chord();
            c.getNodes().add(a);
            c.getNodes().add(b);
            return c;
        }, Chord.class)
        .register(Phrase.class, Chord.class, (a, b) -> {
            var c = new Chord();
            c.getNodes().add(a);
            c.getNodes().add(b);
            return c;
        }, Chord.class)
        .register(Phrase.class, Phrase.class, (a, b) -> {
            var c = new Chord();
            c.getNodes().add(a);
            c.getNodes().add(b);
            return c;
        }, Chord.class);

    @Override
    public void visit(final AddExpression addExpression)
    {
        addExpression.left().accept(this);
        var left = currentValue;
        addExpression.right().accept(this);
        currentValue = addRegistry.apply("add", left, currentValue);
    }

    @Override
    public void visit(final LambdaExpression lambdaExpression)
    {
        var frame = manager.getFrames().empty() ? manager.getGlobalFrame().copy() : manager.getFrames().peek().copy();
        currentValue = new Variant<>(new LambdaClousure(lambdaExpression, frame), LambdaClousure.class);
    }

    @Override
    public void visit(final AssigmentStatement assigmentStatement)
    {
        assigmentStatement.value().accept(this);
        if (!manager.contains(assigmentStatement.identifier()))
        {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        // TODO throw when doesn't exists
        manager.upsert(assigmentStatement.identifier(), currentValue);
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
            manager.insert(declaration.name(), null);
            return;
        }
        declaration.value().accept(this);
        manager.insert(declaration.name(), currentValue);
        currentValue = null;
    }

    @Override
    public void visit(final IfStatement ifStatement)
    {
        ifStatement.condition().accept(this);
        if (currentValue.castTo(Boolean.class))
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
        currentValue = new Variant<>(-currentValue.castTo(Integer.class), Integer.class);
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
        var elements = new LinkedList<>();
        arrayExpression.items().forEach(item -> {
            item.accept(this);
            elements.add(currentValue);
        });
        currentValue = new Variant<>(elements, List.class);

    }

    @Override
    public void visit(final ListComprehension listComprehension)
    {
        listComprehension.iterable().accept(this);
        var results = new LinkedList<>();
        var iterable = currentValue.castTo(List.class);
        for (var i : iterable)
        {
            manager.startNewScope();
            manager.insert(listComprehension.tempName().name(), (Variant<?>) i);
            listComprehension.mapper().accept(this);
            results.add(currentValue);
            manager.leaveNewScope();
        }
        currentValue = new Variant<>(results, List.class);

    }

    @Override
    public void visit(final RangeExpression rangeExpression)
    {
        rangeExpression.start().accept(this);
        var start = currentValue;
        rangeExpression.end().accept(this);
        var ints = IntStream.range(start.castTo(Integer.class), currentValue.castTo(Integer.class)).boxed()
            .map(value -> new Variant<>(value, Integer.class))
            .toList();
        currentValue = new Variant<>(ints, List.class);
    }

    @Override
    public void visit(final FunctionCall functionCall)
    {
        var lambda = manager.get(functionCall.name()).orElseThrow();
        executeCall(functionCall.arguments(), (LambdaClousure) lambda.getValue());
    }

    @Override
    public void visit(final LambdaCall lambdaCall)
    {
        lambdaCall.call().accept(this);
        var closure = currentValue.castTo(LambdaClousure.class);
        executeCall(lambdaCall.arguments(), closure);

    }

    @Override
    public void visit(final InlineFuncCall inlineFuncCall)
    {
        var lambda = (LambdaClousure) manager.get(inlineFuncCall.name()).orElseThrow().getValue();
        var stmts = lambda.expression();
        var arguments = new HashMap<String, Variant<?>>();
        var params = stmts.parameters().parameters();
        // TODO common logic with other calls
        for (int i = 0; i < params.size(); i++)
        {
            arguments.put(params.get(i).name(), currentValue);
            if (i < inlineFuncCall.arguments().size())
            {
                inlineFuncCall.arguments().get(i).accept(this);
            }
        }
        manager.enterNewFrame();
        arguments.forEach(manager::insert);
        stmts.instructions().accept(this);
        manager.leaveFrame();
        returned = false;
    }

    private HashMap<String, Variant<?>> getArguments(final Parameters parameters, final List<Expression> args)
    {
        var arguments = new HashMap<String, Variant<?>>();
        zip(parameters.parameters(), args)
            .forEach(entry -> {
                entry.getValue().accept(this);
                arguments.put(entry.getKey().name(), currentValue);
                currentValue = null;
            });
        return arguments;
    }

    private void executeCall(final List<Expression> args, final LambdaClousure clousure)
    {
        var stmts = clousure.expression();
        final var arguments = getArguments(stmts.parameters(), args);
        manager.enterNewFrame(clousure.frame().copy());
        manager.startNewScope();
        arguments.forEach(manager::insert);
        stmts.instructions().accept(this);
        manager.leaveNewScope();
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
        eqExpression.right().accept(this);
        currentValue = eqRegistry.apply("eq", left, currentValue);
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
        currentValue =
            new Variant<>(manager.get(variableReference.name()).orElseThrow(), com.declarative.music.interpreter.values.VariableReference.class);
    }

    @Override
    public void visit(final IntLiteral intLiteral)
    {
        currentValue = new Variant<>(intLiteral.value(), Integer.class);
    }

    @Override
    public void visit(final NoteExpression noteExpression)
    {
        noteExpression.octave().accept(this);
        var note = new Note(Pitch.valueOf(noteExpression.pitch()), currentValue.castTo(Integer.class), Rythm.valueOf(noteExpression.duration()));
        currentValue = new Variant<>(new Phrase(new ArrayList<MusicNode>(List.of(note))), Phrase.class);

    }

    @Override
    public void visit(final SequenceExpression sequenceExpression)
    {
        sequenceExpression.left().accept(this);
        var left = currentValue;
        sequenceExpression.right().accept(this);
        currentValue = sequenceRegistry.apply("seq", left, currentValue);
    }

    @Override
    public void visit(final ParallerExpression parallerExpression)
    {
        parallerExpression.left().accept(this);
        var left = currentValue;
        parallerExpression.right().accept(this);
        currentValue = chordRegistry.apply("par", left, currentValue);
    }

    @Override
    public void visit(final FloatLiteral floatLiteral)
    {
        currentValue = new Variant<>(floatLiteral.value(), Double.class);
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
        minusExpression.right().accept(this);
        currentValue = minusRegistry.apply("minus", left, currentValue);
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
    public void visit(final GreaterEqExpression greaterEqExpression)
    {
        throw new UnsupportedOperationException("GreaterEqExpression not implemented!");
    }

    @Override
    public void visit(final GreaterExpression greaterExpression)
    {
        greaterExpression.left().accept(this);
        var left = currentValue;
        greaterExpression.right().accept(this);
        currentValue = greaterRegistry.apply("minus", left, currentValue);
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
        currentValue = new Variant<>(stringLiter.value(), String.class);
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
