package com.declarative.music.interpreter;

import com.declarative.music.interpreter.tree.SimpleNode;
import com.declarative.music.interpreter.values.LambdaClousure;
import com.declarative.music.interpreter.values.OperationRegistry;
import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.interpreter.values.music.*;
import com.declarative.music.interpreter.values.template.IndexTree;
import com.declarative.music.midi.MidiRenderer;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.assign.*;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.Expression;
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
import com.declarative.music.parser.production.type.*;
import lombok.Getter;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Executor implements Visitor {
    @Getter
    private final ContextManager manager;
    @Getter
    private Variant<?> currentValue;
    private boolean returned = false;

    public Executor(final ContextManager manager) {
        this.manager = manager;
    }

    public Executor() {
        manager = new ContextManager();
    }

    private Variant<?> moveCurrentValue() {
        var value = currentValue;
        currentValue = null;
        return value;
    }

    private final static Map<String, OperationRegistry> REGISTRY = Map.ofEntries(
            Map.entry(AddExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, Integer::sum, Integer.class)
                    .register(Double.class, Double.class, Double::sum, Double.class)
                    .register(String.class, String.class, String::concat, String.class)),
            Map.entry(MinusExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> a - b, Integer.class)
                    .register(Double.class, Double.class, (a, b) -> a - b, Double.class)),
            Map.entry(DivExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> a / b, Integer.class)
                    .register(Double.class, Double.class, (a, b) -> a / b, Double.class)),
            Map.entry(MulExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> a * b, Integer.class)
                    .register(Double.class, Double.class, (a, b) -> a * b, Double.class)
                    .register(String.class, Integer.class, String::repeat, String.class)),
            Map.entry(ModuloExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> a % b, Integer.class)
                    .register(Double.class, Double.class, (a, b) -> a % b, Double.class)),
            Map.entry(PowExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> (int) Math.pow(a, b), Integer.class)
                    .register(Double.class, Double.class, Math::pow, Double.class)),
            Map.entry(EqExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, Integer::equals, Boolean.class)
                    .register(Double.class, Double.class, Double::equals, Boolean.class)
                    .register(Boolean.class, Boolean.class, Boolean::equals, Boolean.class)
                    .register(String.class, String.class, String::equals, Boolean.class)),
            Map.entry(GreaterExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> a > b, Boolean.class)
                    .register(Double.class, Double.class, (a, b) -> a > b, Boolean.class)
                    .register(String.class, String.class, (a, b) -> a.length() > b.length(), Boolean.class)),
            Map.entry(GreaterEqExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> a >= b, Boolean.class)
                    .register(Double.class, Double.class, (a, b) -> a >= b, Boolean.class)
                    .register(String.class, String.class, (a, b) -> a.length() >= b.length(), Boolean.class)),
            Map.entry(LessExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> a < b, Boolean.class)
                    .register(Double.class, Double.class, (a, b) -> a < b, Boolean.class)
                    .register(String.class, String.class, (a, b) -> a.length() < b.length(), Boolean.class)),
            Map.entry(LessEqExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> a <= b, Boolean.class)
                    .register(Double.class, Double.class, (a, b) -> a <= b, Boolean.class)
                    .register(String.class, String.class, (a, b) -> a.length() <= b.length(), Boolean.class)),
            Map.entry(NotEqExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Integer.class, Integer.class, (a, b) -> !a.equals(b), Boolean.class)
                    .register(Double.class, Double.class, (a, b) -> !a.equals(b), Boolean.class)
                    .register(Boolean.class, Boolean.class, (a, b) -> !a.equals(b), Boolean.class)
                    .register(String.class, String.class, (a, b) -> !a.equals(b), Boolean.class)),
            Map.entry(AndExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Boolean.class, Boolean.class, (a, b) -> a && b, Boolean.class)),
            Map.entry(SequenceExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Note.class, Note.class, (a, b) -> new MusicTree().appendToSequence(a).appendToSequence(b), MusicTree.class)
                    .register(MusicTree.class, Note.class, (a, b) -> new MusicTree().appendToSequence(a.copy()).appendToSequence(b), MusicTree.class)
                    .register(Note.class, MusicTree.class, (a, b) -> new MusicTree().appendToSequence(a).appendToSequence(b.copy()), MusicTree.class)
                    .register(MusicTree.class, MusicTree.class, (a, b) -> new MusicTree().appendToSequence(a.copy()).appendToSequence(b.copy()), MusicTree.class)

                    .register(Integer.class, Integer.class, (a, b) -> new IndexTree().appendToSequence(a).appendToSequence(b), IndexTree.class)
                    .register(IndexTree.class, Integer.class, (a, b) -> new IndexTree().appendToSequence(a.copy()).appendToSequence(b), IndexTree.class)
                    .register(Integer.class, IndexTree.class, (a, b) -> new IndexTree().appendToSequence(a).appendToSequence(b.copy()), IndexTree.class)
                    .register(IndexTree.class, IndexTree.class, (a, b) -> new IndexTree().appendToSequence(a.copy()).appendToSequence(b.copy()), IndexTree.class)
            ),
            Map.entry(ParallerExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Note.class, Note.class, (a, b) -> new MusicTree().appendToGroup(a).appendToGroup(b), MusicTree.class)
                    .register(MusicTree.class, Note.class, (a, b) -> new MusicTree().appendToGroup(a.copy()).appendToGroup(b), MusicTree.class)
                    .register(Note.class, MusicTree.class, (a, b) -> new MusicTree().appendToGroup(a).appendToGroup(b.copy()), MusicTree.class)
                    .register(MusicTree.class, MusicTree.class, (a, b) -> new MusicTree().appendToGroup(a.copy()).appendToGroup(b.copy()), MusicTree.class)

                    .register(Integer.class, Integer.class, (a, b) -> new IndexTree().appendToGroup(a).appendToGroup(b), IndexTree.class)
                    .register(IndexTree.class, Integer.class, (a, b) -> new IndexTree().appendToGroup(a.copy()).appendToGroup(b), IndexTree.class)
                    .register(Integer.class, IndexTree.class, (a, b) -> new IndexTree().appendToGroup(a).appendToGroup(b.copy()), IndexTree.class)
                    .register(IndexTree.class, IndexTree.class, (a, b) -> new IndexTree().appendToGroup(a.copy()).appendToGroup(b.copy()), IndexTree.class)
            ),
            Map.entry(OrExpression.class.getSimpleName(), new OperationRegistry()
                    .register(Boolean.class, Boolean.class, (a, b) -> a || b, Boolean.class))
    );

    record BuiltInFunction(Parameters parameters, Consumer<Map<String, Variant<?>>> code) {
    }

    private final Map<String, BuiltInFunction> builtinFunctions = Map.of(
            "print", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new InferenceType(null), "value")
            )), (arguments) -> {
                System.out.println(arguments.get("value").value().toString());
            }),
            "at", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new InferenceType(null), "array"),
                    new Parameter(new SimpleType(Types.Int, null), "index")
            )), (arguments) -> {
                var index = arguments.get("index").castTo(Integer.class);
                var iterable = arguments.get("array").castTo(List.class);
                currentValue = (Variant<?>) iterable.get(index);
            }),
            "mel", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new InferenceType(null), "array")
            )), (arguments) -> {
                var iterable = arguments.get("array").castTo(List.class);
                var sequence = new MusicTree();
                for (var item : iterable) {
                    var node = (Variant<MusicTree>) item;
                    sequence.appendToSequence(node.value());
                }
                currentValue = new Variant<>(sequence, MusicTree.class);
            }),
            "export", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new InferenceType(null), "midiTree"),
                    new Parameter(new SimpleType(Types.String, null), "fileName")
            )), (arguments) -> {
                var index = (MusicTree) arguments.get("midiTree").castTo(MusicTree.class);
                var fileName = arguments.get("fileName").castTo(String.class);
                try {
                    MidiRenderer.renderAndSaveMidi(index, fileName, 120);
                } catch (InvalidMidiDataException | IOException e) {
                    throw new RuntimeException(e);
                }
                currentValue = null;
            })
    );

    @Override
    public void visit(final AddExpression addExpression) {
        addExpression.left().accept(this);
        var left = moveCurrentValue();
        addExpression.right().accept(this);
        currentValue = REGISTRY.get(addExpression.getClass().getSimpleName()).apply(addExpression.getClass().getSimpleName(), left, currentValue);
    }

    @Override
    public void visit(final LambdaExpression lambdaExpression) {
        var frame = manager.getFrames().empty() ? manager.getGlobalFrame().copy() : manager.getFrames().peek().copy();
        currentValue = new Variant<>(new LambdaClousure(lambdaExpression, frame), LambdaClousure.class);
    }


    //region Statement

    //region Program
    @Override
    public void visit(final Program program) {
        program.statements().forEach(stmt -> stmt.accept(this));
    }
    //endregion

    //region Block
    @Override
    public void visit(final Block block) {
        manager.startNewScope();
        block.statements().forEach(stmt -> {
            if (!returned) {
                stmt.accept(this);
            }
        });
        manager.leaveNewScope();
    }
    //endregion

    //region Assigment
    @Override
    public void visit(final AssigmentStatement assigmentStatement) {
        if (!manager.contains(assigmentStatement.identifier())) {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        assigmentStatement.value().accept(this);
        consumeCurrentValue(() -> manager.upsert(assigmentStatement.identifier(), currentValue));

    }

    @Override
    public void visit(final PlusAssignStatement plusAssignStatement) {
        var varName = plusAssignStatement.identifier();
        if (!manager.contains(varName)) {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        new AddExpression(new VariableReference(varName, plusAssignStatement.position()), plusAssignStatement.value())
                .accept(this);
        consumeCurrentValue(() -> manager.upsert(varName, currentValue));
    }


    @Override
    public void visit(final DivAssignStatement divAssignStatement) {
        var varName = divAssignStatement.identifier();
        if (!manager.contains(varName)) {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        new DivExpression(new VariableReference(varName, divAssignStatement.position()), divAssignStatement.value())
                .accept(this);
        consumeCurrentValue(() -> manager.upsert(varName, currentValue));
    }

    @Override
    public void visit(final MinusAssignStatement minusAssignStatement) {
        var varName = minusAssignStatement.identifier();
        if (!manager.contains(varName)) {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        new MinusExpression(new VariableReference(varName, minusAssignStatement.position()), minusAssignStatement.value())
                .accept(this);
        consumeCurrentValue(() -> manager.upsert(varName, currentValue));

    }

    @Override
    public void visit(final ModuloAssignStatement moduloAssignStatement) {
        var varName = moduloAssignStatement.identifier();
        if (!manager.contains(varName)) {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        new ModuloExpression(new VariableReference(varName, moduloAssignStatement.position()), moduloAssignStatement.value())
                .accept(this);
        consumeCurrentValue(() -> manager.upsert(varName, currentValue));
    }

    @Override
    public void visit(final MulAssignStatement mulAssignStatement) {
        var varName = mulAssignStatement.identifier();
        if (!manager.contains(varName)) {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        new MulExpression(new VariableReference(varName, mulAssignStatement.position()), mulAssignStatement.value())
                .accept(this);
        consumeCurrentValue(() -> manager.upsert(varName, currentValue));
    }

    @Override
    public void visit(final ParalerAssignStatement paralerAssignStatement) {
        var varName = paralerAssignStatement.identifier();
        if (!manager.contains(varName)) {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        new ParallerExpression(new VariableReference(varName, paralerAssignStatement.position()), paralerAssignStatement.value())
                .accept(this);
        consumeCurrentValue(() -> manager.upsert(varName, currentValue));
    }


    @Override
    public void visit(final PowAssignStatement powAssignStatement) {
        var varName = powAssignStatement.identifier();
        if (!manager.contains(varName)) {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        new PowExpression(new VariableReference(varName, powAssignStatement.position()), powAssignStatement.value())
                .accept(this);
        consumeCurrentValue(() -> manager.upsert(varName, currentValue));
    }

    @Override
    public void visit(final SequenceAssignStatement sequenceAssignStatement) {
        var varName = sequenceAssignStatement.identifier();
        if (!manager.contains(varName)) {
            throw new RuntimeException("INTERPRETATION ERROR");
        }

        new SequenceExpression(new VariableReference(varName, sequenceAssignStatement.position()), sequenceAssignStatement.value())
                .accept(this);
        consumeCurrentValue(() -> manager.upsert(varName, currentValue));
    }
    //endregion

    //region If
    @Override
    public void visit(final IfStatement ifStatement) {
        ifStatement.condition().accept(this);
        var value = moveCurrentValue();
        if (value.castTo(Boolean.class)) {
            ifStatement.instructions().accept(this);
        } else if (ifStatement.otherwise() != null) {
            ifStatement.otherwise().accept(this);
        }

    }
    //endregion

    //region Declaration
    @Override
    public void visit(final Declaration declaration) {
        if (declaration.value() == null) {
            manager.insert(declaration.name(), null);
            return;
        }
        declaration.value().accept(this);
        manager.insert(declaration.name(), currentValue);
        currentValue = null;
    }
    //endregion

    //region For
    @Override
    public void visit(final ForStatement forStatement) {
        forStatement.iterable().accept(this);
        var iterable = currentValue.castTo(List.class);
        currentValue = null;
        var varName = forStatement.declaration().name();

        for (var item : iterable) {
            manager.startNewScope();
            manager.insert(varName, (Variant<?>) item);
            forStatement.instructions().accept(this);
            manager.leaveNewScope();
        }
    }
    //endregion

    //endregion

    //region Expression

    //region Literal
    @Override
    public void visit(final FloatLiteral floatLiteral) {
        currentValue = new Variant<>(floatLiteral.value(), Double.class);
    }

    @Override
    public void visit(final BoolLiteral boolLiteral) {
        currentValue = new Variant<>(boolLiteral.value(), Boolean.class);
    }

    @Override
    public void visit(final StringLiter stringLiter) {
        currentValue = new Variant<>(stringLiter.value(), String.class);
    }

    @Override
    public void visit(final IntLiteral intLiteral) {
        currentValue = new Variant<>(intLiteral.value(), Integer.class);
    }
    //endregion

    //region Unary
    @Override
    public void visit(final MinusUnaryExpression minusUnaryExpression) {
        minusUnaryExpression.value().accept(this);
        currentValue = switch (currentValue.valueType().getSimpleName()) {
            case "Integer" -> new Variant<>(-currentValue.castTo(Integer.class), Integer.class);
            case "Double" -> new Variant<>(-currentValue.castTo(Double.class), Double.class);
            default ->
                    throw new IllegalStateException("INTERPRETATION ERROR cannot negate %s type".formatted(currentValue.type().getSimpleName()));
        };
    }

    @Override
    public void visit(final PlusUnaryExpression plusUnaryExpression) {
        plusUnaryExpression.value().accept(this);
        currentValue = switch (currentValue.valueType().getSimpleName()) {
            case "Integer" -> new Variant<>(currentValue.castTo(Integer.class), Integer.class);
            case "Double" -> new Variant<>(currentValue.castTo(Double.class), Double.class);
            default ->
                    throw new IllegalStateException("INTERPRETATION ERROR cannot plus %s type".formatted(currentValue.type().getSimpleName()));
        };

    }
    //endregion

    //region Arithmetic
    @Override
    public void visit(final MulExpression mulExpression) {
        mulExpression.left().accept(this);
        var left = moveCurrentValue();
        mulExpression.right().accept(this);
        currentValue = REGISTRY.get(mulExpression.getClass().getSimpleName()).apply(mulExpression.getClass().getSimpleName(), left, currentValue);

    }

    @Override
    public void visit(final DivExpression divExpression) {
        divExpression.left().accept(this);
        var left = moveCurrentValue();
        divExpression.right().accept(this);
        currentValue = REGISTRY.get(divExpression.getClass().getSimpleName()).apply(divExpression.getClass().getSimpleName(), left, currentValue);

    }

    @Override
    public void visit(final MinusExpression minusExpression) {
        minusExpression.left().accept(this);
        var left = moveCurrentValue();
        minusExpression.right().accept(this);
        currentValue = REGISTRY.get(minusExpression.getClass().getSimpleName()).apply(minusExpression.getClass().getSimpleName(), left, currentValue);
    }

    @Override
    public void visit(final ModuloExpression moduloExpression) {
        moduloExpression.left().accept(this);
        var left = moveCurrentValue();
        moduloExpression.right().accept(this);
        currentValue =
                REGISTRY.get(moduloExpression.getClass().getSimpleName()).apply(moduloExpression.getClass().getSimpleName(), left, currentValue);

    }

    @Override
    public void visit(final PowExpression powExpression) {
        powExpression.left().accept(this);
        var left = moveCurrentValue();
        powExpression.right().accept(this);
        currentValue = REGISTRY.get(powExpression.getClass().getSimpleName()).apply(powExpression.getClass().getSimpleName(), left, currentValue);

    }
    //endregion

    //region Relation
    @Override
    public void visit(final AndExpression andExpression) {
        andExpression.left().accept(this);
        var left = moveCurrentValue();
        andExpression.right().accept(this);
        currentValue = REGISTRY.get(andExpression.getClass().getSimpleName()).apply(andExpression.getClass().getSimpleName(), left, currentValue);

    }

    @Override
    public void visit(final EqExpression eqExpression) {

        eqExpression.left().accept(this);
        var left = moveCurrentValue();
        eqExpression.right().accept(this);
        currentValue = REGISTRY.get(eqExpression.getClass().getSimpleName()).apply(eqExpression.getClass().getSimpleName(), left, currentValue);
    }

    @Override
    public void visit(final OrExpression orExpression) {
        orExpression.left().accept(this);
        var left = moveCurrentValue();
        orExpression.right().accept(this);
        currentValue = REGISTRY.get(orExpression.getClass().getSimpleName()).apply(orExpression.getClass().getSimpleName(), left, currentValue);

    }

    @Override
    public void visit(final GreaterEqExpression greaterEqExpression) {
        greaterEqExpression.left().accept(this);
        var left = moveCurrentValue();
        greaterEqExpression.right().accept(this);
        currentValue =
                REGISTRY.get(greaterEqExpression.getClass().getSimpleName()).apply(greaterEqExpression.getClass().getSimpleName(), left, currentValue);
    }

    @Override
    public void visit(final GreaterExpression greaterExpression) {
        greaterExpression.left().accept(this);
        var left = moveCurrentValue();
        greaterExpression.right().accept(this);
        currentValue = REGISTRY.get(greaterExpression.getClass().getSimpleName()).apply("gr", left, currentValue);
    }

    @Override
    public void visit(final LessEqExpression lessEqExpression) {
        lessEqExpression.left().accept(this);
        var left = moveCurrentValue();
        lessEqExpression.right().accept(this);
        currentValue =
                REGISTRY.get(lessEqExpression.getClass().getSimpleName()).apply(lessEqExpression.getClass().getSimpleName(), left, currentValue);

    }

    @Override
    public void visit(final LessExpression lessExpression) {
        lessExpression.left().accept(this);
        var left = moveCurrentValue();
        lessExpression.right().accept(this);
        currentValue = REGISTRY.get(lessExpression.getClass().getSimpleName()).apply(lessExpression.getClass().getSimpleName(), left, currentValue);

    }

    @Override
    public void visit(final NegateExpression negateExpression) {
        negateExpression.expression().accept(this);
        currentValue = new Variant<>(!currentValue.castTo(Boolean.class), Boolean.class);

    }

    @Override
    public void visit(final NotEqExpression notEqExpression) {
        notEqExpression.left().accept(this);
        var left = moveCurrentValue();
        notEqExpression.right().accept(this);
        currentValue = REGISTRY.get(notEqExpression.getClass().getSimpleName()).apply(notEqExpression.getClass().getSimpleName(), left, currentValue);

    }
    //endregion

    //region Iterable
    @Override
    public void visit(final ArrayExpression arrayExpression) {
        var elements = new LinkedList<>();
        arrayExpression.items().forEach(item -> {
            item.accept(this);
            elements.add(currentValue);
            currentValue = null;
        });
        currentValue = new Variant<>(elements, List.class);

    }

    @Override
    public void visit(final ListComprehension listComprehension) {
        listComprehension.iterable().accept(this);
        var results = new LinkedList<>();
        var iterable = currentValue.castTo(List.class);
        for (var i : iterable) {
            manager.startNewScope();
            manager.insert(listComprehension.tempName().name(), (Variant<?>) i);
            listComprehension.mapper().accept(this);
            results.add(currentValue);
            manager.leaveNewScope();
        }
        currentValue = new Variant<>(results, List.class);

    }

    @Override
    public void visit(final RangeExpression rangeExpression) {
        rangeExpression.start().accept(this);
        var start = currentValue;
        rangeExpression.end().accept(this);
        var ints = IntStream.range(start.castTo(Integer.class), currentValue.castTo(Integer.class)).boxed()
                .map(value -> new Variant<>(value, Integer.class))
                .toList();
        currentValue = new Variant<>(ints, List.class);
    }
    //endregion

    //region Pipe
    @Override
    public void visit(final PipeExpression pipeExpression) {
        pipeExpression.left().accept(this);
        pipeExpression.right().accept(this);
    }
    //endregion

    //region Call
    @Override
    public void visit(final FunctionCall functionCall) {
        Optional.ofNullable(builtinFunctions.get(functionCall.name())).ifPresentOrElse(
                func -> {
                    var arguments = getArguments(func.parameters(), functionCall.arguments());
                    func.code().accept(arguments);
                },
                () -> {
                    var lambda = manager.get(functionCall.name()).orElseThrow();
                    executeCall(functionCall.arguments(), (LambdaClousure) lambda.getValue());
                }
        );
    }

    @Override
    public void visit(final LambdaCall lambdaCall) {
        lambdaCall.call().accept(this);
        var closure = currentValue.castTo(LambdaClousure.class);
        executeCall(lambdaCall.arguments(), closure);

    }

    @Override
    public void visit(final InlineFuncCall inlineFuncCall) {
        if (builtinFunctions.containsKey(inlineFuncCall.name())) {
            var func = builtinFunctions.get(inlineFuncCall.name());
            var arguments = getInlineArguments(func.parameters.parameters(), inlineFuncCall.arguments());
            func.code.accept(arguments);
            return;
        }
        var lambda = (LambdaClousure) manager.get(inlineFuncCall.name()).orElseThrow().getValue();
        var stmts = lambda.expression();
        var params = stmts.parameters().parameters();
        var callArguments = inlineFuncCall.arguments();
        // TODO common logic with other calls
        final var arguments = getInlineArguments(params, callArguments);
        manager.enterNewFrame();
        arguments.forEach(manager::insert);
        stmts.instructions().accept(this);
        manager.leaveFrame();
        returned = false;
    }

    private HashMap<String, Variant<?>> getInlineArguments(final List<Parameter> params, final List<Expression> callArguments) {
        var arguments = new HashMap<String, Variant<?>>();
        for (int i = 0; i < params.size(); i++) {
            arguments.put(params.get(i).name(), currentValue);
            if (i < callArguments.size()) {
                callArguments.get(i).accept(this);
            }
        }
        return arguments;
    }

    private HashMap<String, Variant<?>> getArguments(final Parameters parameters, final List<Expression> args) {
        var arguments = new HashMap<String, Variant<?>>();
        zip(parameters.parameters(), args)
                .forEach(entry -> {
                    entry.getValue().accept(this);
                    arguments.put(entry.getKey().name(), currentValue);
                    currentValue = null;
                });
        return arguments;
    }

    private void executeCall(final List<Expression> args, final LambdaClousure clousure) {
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
    //endregion

    //region Modifier
    @Override
    public void visit(final ModifierExpression modifierExpression) {
        modifierExpression.modified().accept(this);
        var variant = currentValue;
        var notes = variant.castTo(List.class);

        currentValue = null;
        var results = new HashMap<String, Variant<?>>();
        for (var item : modifierExpression.modifier().modifiers()) {
            item.expression().accept(this);
            results.put(item.name(), currentValue);
            currentValue = null;
        }
        var updateValues = ((List<Variant<?>>) notes).stream()
                .map(val -> val.castTo(Note.class))
                .map(note -> {
                    var node = new SimpleNode<>(note);
                    node.modifier = NoteModifier.builder()
                            .withRythm(Optional.ofNullable(results.get("dur")).map(v -> v.castTo(Note.class).getDuration()).orElse(null))
                            .withOctave(Optional.ofNullable(results.get("oct")).map(v -> v.castTo(Integer.class)).orElse(4))
                            .build();
                    return new MusicTree().appendToSequence(node);
                })
                .map(val -> new Variant<>(val, MusicTree.class))
                .toList();
        currentValue = new Variant<>(updateValues, List.class);

    }
    //endregion

    //endregion


    @Override
    public void visit(final ConvolutionExpression convolutionExpression) {
        convolutionExpression.left().accept(this);
        var left = currentValue.castTo(IndexTree.class);
        currentValue = null;
        convolutionExpression.right().accept(this);
        var musicIterable = currentValue.castTo(List.class);
        var transformed = left.map((indexNode) -> {
            var element = (Variant<MusicTree>) musicIterable.get(indexNode.getValue());
            var note = element.castTo(MusicTree.class).getRoot();
            var oldNode = (SimpleNode<Note>) note;
            var node = new SimpleNode<>(oldNode.getValue());
            node.modifier = oldNode.modifier;
            return node;
        });
        currentValue = new Variant<>(new MusicTree(transformed), MusicTree.class);

    }

    @Override
    public void visit(final CastExpresion castExpresion) {
        castExpresion.value().accept(this);
        var value = moveCurrentValue();
        castExpresion.type().accept(this);
        var key = value.valueType().getSimpleName() + "," + currentValue.castTo(Class.class).getSimpleName();
        currentValue = switch (key) {
            case "Integer,Double" -> new Variant<>(Double.valueOf(value.castTo(Integer.class)), Double.class);
            case "Integer,Integer", "Double,Double", "String,String" -> value;
            case "Double,Integer" -> new Variant<>(value.castTo(Double.class).intValue(), Integer.class);
            case "String,Integer" -> new Variant<>(Integer.valueOf(value.castTo(String.class)), Integer.class);
            case "String,Double" -> new Variant<>(Double.valueOf(value.castTo(String.class)), Double.class);
            case "Integer,String" -> new Variant<>(value.castTo(Integer.class).toString(), String.class);
            case "Double,String" -> new Variant<>(value.castTo(Double.class).toString(), String.class);
            default -> throw new IllegalStateException("INTERPRETATION ERROR Cannot cast %s to %s".
                    formatted(value.valueType().getSimpleName(), currentValue.castTo(Class.class).getSimpleName()));
        };
    }

    @Override
    public void visit(final VariableReference variableReference) {
        currentValue =
                new Variant<>(manager.get(variableReference.name()).orElseThrow(), com.declarative.music.interpreter.values.VariableReference.class);
    }


    @Override
    public void visit(final NoteExpression noteExpression) {
        var noteBuilder = Note.builder();
        Optional.ofNullable(noteExpression.pitch()).ifPresent(val -> noteBuilder.pitch(Pitch.valueOf(val)));
        Optional.ofNullable(noteExpression.octave()).ifPresent(expr -> expr.accept(this));
        Optional.ofNullable(currentValue).ifPresent(val -> noteBuilder.octave(val.castTo(Integer.class)));
        currentValue = null;
        Optional.ofNullable(noteExpression.duration()).ifPresent(val -> noteBuilder.duration(Rythm.valueOf(noteExpression.duration())));
        currentValue = new Variant<>(noteBuilder.build(), Note.class);

    }

    @Override
    public void visit(final SequenceExpression sequenceExpression) {
        sequenceExpression.left().accept(this);
        var left = moveCurrentValue();
        sequenceExpression.right().accept(this);
        currentValue = REGISTRY.get(sequenceExpression.getClass().getSimpleName())
                .apply(sequenceExpression.getClass().getSimpleName(), left, currentValue);
    }

    @Override
    public void visit(final ParallerExpression parallerExpression) {
        parallerExpression.left().accept(this);
        var left = moveCurrentValue();
        parallerExpression.right().accept(this);
        currentValue = REGISTRY.get(parallerExpression.getClass().getSimpleName())
                .apply(parallerExpression.getClass().getSimpleName(), left, currentValue);
    }


    @Override
    public void visit(final ReturnStatement returnStatement) {
        returnStatement.value().accept(this);
        returned = true;
    }

    @Override
    public void visit(final SimpleType simpleType) {
        var valueType = switch (simpleType.type()) {
            case Int -> Integer.class;
            case Double -> Double.class;
            case String -> String.class;
            case null, default -> throw new UnsupportedOperationException("Unknown type");
        };
        currentValue = new Variant<>(valueType, Class.class);
    }

    @Override
    public void visit(final LambdaType lambdaType) {
    }

    @Override
    public void visit(final InferenceType inferenceType) {

    }

    @Override
    public void visit(final ArrayType arrayType) {

    }

    private void consumeCurrentValue(Runnable conumptionFunction) {
        conumptionFunction.run();
        currentValue = null;
    }

    private static <T, U> Stream<AbstractMap.SimpleEntry<T, U>> zip(List<T> list1, List<U> list2) {
        if (list1.size() != list2.size()) {
            throw new IllegalArgumentException("Lists must be of equal size");
        }

        return IntStream.range(0, list1.size())
                .mapToObj(i -> new AbstractMap.SimpleEntry<>(list1.get(i), list2.get(i)));
    }
}
