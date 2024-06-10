package com.declarative.music.interpreter;

import com.declarative.music.interpreter.type.*;
import com.declarative.music.interpreter.values.OperationRegistry;
import com.declarative.music.interpreter.values.Variant;
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
import com.declarative.music.parser.production.type.SimpleType;
import com.declarative.music.parser.production.type.Type;
import com.declarative.music.parser.production.type.Types;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TypeChecker implements Visitor {
    @Getter
    private Variant<TypeCheck> currentValue;
    private final ContextManager manager;
    private final boolean returned = false;
    private static final Map<String, OperationRegistry> REGISTRY = Map.ofEntries(
            Map.entry(SequenceExpression.class.getSimpleName(), new OperationRegistry()
                    .register(IntType.class, IntType.class, (a, b) -> new TemplateType(), TypeCheck.class)
                    .register(IntType.class, TemplateType.class, (a, b) -> new TemplateType(), TypeCheck.class)
                    .register(TemplateType.class, IntType.class, (a, b) -> new TemplateType(), TypeCheck.class)
                    .register(TemplateType.class, TemplateType.class, (a, b) -> new TemplateType(), TypeCheck.class)

                    .register(NoteType.class, NoteType.class, (a, b) -> new PhraseType(), TypeCheck.class)
                    .register(NoteType.class, PhraseType.class, (a, b) -> new PhraseType(), TypeCheck.class)
                    .register(PhraseType.class, NoteType.class, (a, b) -> new PhraseType(), TypeCheck.class)
                    .register(PhraseType.class, PhraseType.class, (a, b) -> new PhraseType(), TypeCheck.class)
            ),
            Map.entry(ParallerExpression.class.getSimpleName(), new OperationRegistry()
                    .register(IntType.class, IntType.class, (a, b) -> new TemplateType(), TypeCheck.class)
                    .register(IntType.class, TemplateType.class, (a, b) -> new TemplateType(), TypeCheck.class)
                    .register(TemplateType.class, IntType.class, (a, b) -> new TemplateType(), TypeCheck.class)
                    .register(TemplateType.class, TemplateType.class, (a, b) -> new TemplateType(), TypeCheck.class)
                    .register(NoteType.class, NoteType.class, (a, b) -> new PhraseType(), TypeCheck.class)
                    .register(NoteType.class, PhraseType.class, (a, b) -> new PhraseType(), TypeCheck.class)
                    .register(PhraseType.class, NoteType.class, (a, b) -> new PhraseType(), TypeCheck.class)
                    .register(PhraseType.class, PhraseType.class, (a, b) -> new PhraseType(), TypeCheck.class)
            ),
            Map.entry(ConvolutionExpression.class.getSimpleName(), new OperationRegistry()
                    .register(TemplateType.class, com.declarative.music.interpreter.type.ArrayType.class, (a, b) -> {
                        if (b.getType() instanceof NoteType) {
                            return new PhraseType();
                        }
                        throw new UnsupportedOperationException("Unsupported array type for convolution");
                    }, TypeCheck.class)
                    .register(TemplateType.class, NoteType.class, (a, b) -> new PhraseType(), TypeCheck.class)
            )
    );

    record BuiltInFunction(Parameters parameters, Type returnType) {
    }

    private final Map<String, BuiltInFunction> builtinFunctions = Map.of(
            "print", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new com.declarative.music.parser.production.type.InferenceType(null), "value")
            )), new SimpleType(Types.Void, null)),
            "at", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new com.declarative.music.parser.production.type.InferenceType(null), "array"),
                    new Parameter(new SimpleType(Types.Int, null), "index")
            )), new com.declarative.music.parser.production.type.InferenceType(null)),
            "head", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new com.declarative.music.parser.production.type.InferenceType(null), "array"),
                    new Parameter(new SimpleType(Types.Int, null), "index")
            )), new com.declarative.music.parser.production.type.InferenceType(null)),
            "transpose", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new com.declarative.music.parser.production.type.InferenceType(null), "tree"),
                    new Parameter(new SimpleType(Types.Int, null), "index")
            )), new SimpleType(Types.Phrase, null)),
            "mel", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new com.declarative.music.parser.production.type.InferenceType(null), "array")
            )), new SimpleType(Types.Phrase, null)),
            "harm", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new com.declarative.music.parser.production.type.InferenceType(null), "array")
            )), new SimpleType(Types.Phrase, null)),
            "song", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new com.declarative.music.parser.production.type.InferenceType(null), "tree"),
                    new Parameter(new SimpleType(Types.Int, null), "bpm"),
                    new Parameter(new SimpleType(Types.String, null), "instrument")
            )), new SimpleType(Types.Song, null)),
            "export", new BuiltInFunction(new Parameters(List.of(
                    new Parameter(new com.declarative.music.parser.production.type.InferenceType(null), "midiTree"),
                    new Parameter(new SimpleType(Types.String, null), "fileName")
            )), new SimpleType(Types.Void, null))
    );

    public TypeChecker(ContextManager manager) {
        this.manager = manager;
    }

    private Variant<TypeCheck> moveCurrentValue() {
        var value = currentValue;
        currentValue = null;
        return value;
    }

    private void consumeCurrentValue(Runnable conumptionFunction) {
        conumptionFunction.run();
        currentValue = null;
    }

    //region Expression
    @Override
    public void visit(AddExpression expression) {
        expression.left().accept(this);
        var left = moveCurrentValue();
        expression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }

    }

    @Override
    public void visit(MulExpression expression) {
        expression.left().accept(this);
        var left = moveCurrentValue();
        expression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(DivExpression expression) {
        expression.left().accept(this);
        var left = moveCurrentValue();
        expression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(MinusExpression expression) {
        expression.left().accept(this);
        var left = moveCurrentValue();
        expression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(ModuloExpression expression) {
        expression.left().accept(this);
        var left = moveCurrentValue();
        expression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(PowExpression expression) {
        expression.left().accept(this);
        var left = moveCurrentValue();
        expression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }
    //endregion

    //region Statements
    @Override
    public void visit(AssigmentStatement assigmentStatement) {
        if (!manager.contains(assigmentStatement.identifier())) {
            throw new RuntimeException("SEMANTIC ERROR");
        }

        assigmentStatement.value().accept(this);
        var savedType = getCurrentTypeByName(assigmentStatement.identifier());
        if (!currentValue.value().isCompatible(savedType)) {
            throw new RuntimeException("SEMANTIC ERROR cannot assign value of %s to variable of %s"
                    .formatted(currentValue.value().getClass().getSimpleName(), savedType.getClass().getSimpleName()));
        }
        consumeCurrentValue(() -> manager.upsert(assigmentStatement.identifier(), currentValue));
    }

    private TypeCheck getCurrentTypeByName(String variableName) {
        return (TypeCheck) manager.get(variableName).orElseThrow().getValue();
    }

    @Override
    public void visit(Block block) {
        manager.startNewScope();
        block.statements().forEach(stmt -> {
            if (!returned) {
                stmt.accept(this);
            }
        });
        manager.leaveNewScope();
    }

    @Override
    public void visit(Declaration declaration) {
        if (declaration.value() == null) {
            declaration.type().accept(this);
            manager.insert(declaration.name(), moveCurrentValue());
            return;
        }
        declaration.value().accept(this);
        var rightType = moveCurrentValue();
        declaration.type().accept(this);
        if (!rightType.value().isCompatible(currentValue.value())) {
            throw new RuntimeException("SEMANTIC ERROR cannot assign value to variable of different type %s".formatted(declaration.position()));
        }
        manager.insert(declaration.name(), rightType);
    }

    @Override
    public void visit(IfStatement ifStatement) {

    }

    @Override
    public void visit(Program program) {
        program.statements().forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visit(DivAssignStatement divAssignStatement) {

    }

    @Override
    public void visit(MinusAssignStatement minusAssignStatement) {

    }

    @Override
    public void visit(ModuloAssignStatement moduloAssignStatement) {

    }

    @Override
    public void visit(MulAssignStatement mulAssignStatement) {

    }

    @Override
    public void visit(ParalerAssignStatement paralerAssignStatement) {

    }

    @Override
    public void visit(PlusAssignStatement plusAssignStatement) {

    }

    @Override
    public void visit(PowAssignStatement powAssignStatement) {

    }

    @Override
    public void visit(SequenceAssignStatement sequenceAssignStatement) {

    }

    @Override
    public void visit(ForStatement forStatement) {

    }

    @Override
    public void visit(ReturnStatement returnStatement) {

    }
    //endregion

    //region Liter
    @Override
    public void visit(IntLiteral intLiteral) {
        currentValue = new Variant<>(new IntType(), TypeCheck.class);
    }

    @Override
    public void visit(FloatLiteral floatLiteral) {
        currentValue = new Variant<>(new DoubleType(), TypeCheck.class);

    }

    @Override
    public void visit(BoolLiteral boolLiteral) {
        currentValue = new Variant<>(new BooleanType(), TypeCheck.class);
    }

    @Override
    public void visit(StringLiter stringLiter) {
        currentValue = new Variant<>(new StringType(), TypeCheck.class);
    }
    //endregion


    @Override
    public void visit(LambdaExpression expression) {
        var params = new LinkedList<TypeCheck>();
        for (var param : expression.parameters().parameters()) {
            param.type().accept(this);
            params.add(moveCurrentValue().value());
        }
        expression.returnType().accept(this);
        var value = new com.declarative.music.interpreter.type.LambdaType(params, moveCurrentValue().value());
        currentValue = new Variant<>(value, TypeCheck.class);
    }


    @Override
    public void visit(MinusUnaryExpression expression) {
        expression.value().accept(this);
    }


    @Override
    public void visit(PlusUnaryExpression plusUnaryExpression) {
        plusUnaryExpression.value().accept(this);
    }

    @Override
    public void visit(ArrayExpression arrayExpression) {
        arrayExpression.items().getFirst().accept(this);
    }

    @Override
    public void visit(ListComprehension listComprehension) {
    }

    @Override
    public void visit(RangeExpression rangeExpression) {
        currentValue = new Variant<>(new ArrayType(new IntType()), TypeCheck.class);
    }

    @Override
    public void visit(FunctionCall functionCall) {
        if (builtinFunctions.containsKey(functionCall.name())) {
            var returnType = builtinFunctions.get(functionCall.name()).returnType;
            returnType.accept(this);
            return;
        }
        var lambda = (com.declarative.music.interpreter.type.LambdaType) manager.get(functionCall.name()).orElseThrow().getValue();
        currentValue = new Variant<>(lambda.getReturnType(), TypeCheck.class);
    }

    @Override
    public void visit(ModifierExpression modifierExpression) {
        currentValue = new Variant<>(new com.declarative.music.interpreter.type.ArrayType(new NoteType()), TypeCheck.class);
    }

    @Override
    public void visit(ConvolutionExpression convolutionExpression) {
        convolutionExpression.left().accept(this);
        var left = moveCurrentValue();
        convolutionExpression.right().accept(this);
        var value = REGISTRY.get(ConvolutionExpression.class.getSimpleName())
                .apply(ConvolutionExpression.class.getSimpleName(), left, currentValue);
        currentValue = (Variant<TypeCheck>) value;
    }

    @Override
    public void visit(NoteExpression noteExpression) {
        currentValue = new Variant<>(new NoteType(), TypeCheck.class);
    }

    @Override
    public void visit(InlineFuncCall inlineFuncCall) {

    }

    @Override
    public void visit(PipeExpression pipeExpression) {
        pipeExpression.left().accept(this);
        if (builtinFunctions.containsKey(pipeExpression.right().name())) {
            var returnType = builtinFunctions.get(pipeExpression.right().name()).returnType;
            returnType.accept(this);
            return;
        }
        var lambda = (com.declarative.music.interpreter.type.LambdaType) manager.get(pipeExpression.right().name()).orElseThrow().getValue();
        currentValue = new Variant<>(lambda.getReturnType(), TypeCheck.class);
    }

    @Override
    public void visit(AndExpression andExpression) {
        andExpression.left().accept(this);
        var left = moveCurrentValue();
        andExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(EqExpression eqExpression) {
        eqExpression.left().accept(this);
        var left = moveCurrentValue();
        eqExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(OrExpression orExpression) {
        orExpression.left().accept(this);
        var left = moveCurrentValue();
        orExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(CastExpresion castExpresion) {
        castExpresion.type().accept(this);
    }

    @Override
    public void visit(VariableReference variableReference) {
        currentValue = new Variant<>(getCurrentTypeByName(variableReference.name()), TypeCheck.class);
    }


    @Override
    public void visit(SequenceExpression sequenceExpression) {
        sequenceExpression.left().accept(this);
        var left = moveCurrentValue();
        sequenceExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator %s".formatted(sequenceExpression.left().position()));
        }
        currentValue = (Variant<TypeCheck>) REGISTRY.get(sequenceExpression.getClass().getSimpleName())
                .apply(sequenceExpression.getClass().getSimpleName(), left, currentValue);
    }

    @Override
    public void visit(ParallerExpression parallerExpression) {
        parallerExpression.left().accept(this);
        var left = moveCurrentValue();
        parallerExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }


    @Override
    public void visit(LambdaCall lambdaCall) {
        lambdaCall.call().accept(this);
        var returnType = moveCurrentValue().castTo(com.declarative.music.interpreter.type.LambdaType.class).getReturnType();
        currentValue = new Variant<>(returnType, TypeCheck.class);

    }

    @Override
    public void visit(GreaterEqExpression greaterEqExpression) {
        greaterEqExpression.left().accept(this);
        var left = moveCurrentValue();
        greaterEqExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(GreaterExpression greaterExpression) {
        greaterExpression.left().accept(this);
        var left = moveCurrentValue();
        greaterExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(LessEqExpression lessEqExpression) {
        lessEqExpression.left().accept(this);
        var left = moveCurrentValue();
        lessEqExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(LessExpression lessExpression) {
        lessExpression.left().accept(this);
        var left = moveCurrentValue();
        lessExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }

    @Override
    public void visit(NegateExpression negateExpression) {
        negateExpression.expression().accept(this);
    }

    @Override
    public void visit(NotEqExpression notEqExpression) {
        notEqExpression.left().accept(this);
        var left = moveCurrentValue();
        notEqExpression.right().accept(this);
        if (!currentValue.value().isCompatible(left.value())) {
            throw new RuntimeException("SEMANTIC ERROR different types on left and right of binary operator");
        }
    }


    @Override
    public void visit(SimpleType simpleType) {
        currentValue = new Variant<>(switch (simpleType.type()) {
            case Int -> new IntType();
            case Double -> new DoubleType();
            case String -> new StringType();
            case Bool -> new BooleanType();
            case Phrase -> new PhraseType();
            case Template -> new TemplateType();
            case Song -> new SongType();
            case Void -> new com.declarative.music.interpreter.type.InferenceType(); //TODO change this
            default -> throw new IllegalStateException("Unexpected value: " + simpleType.type());
        }, TypeCheck.class);
    }

    @Override
    public void visit(com.declarative.music.parser.production.type.LambdaType lambdaType) {
        var params = new LinkedList<TypeCheck>();
        for (var param : lambdaType.parameter()) {
            param.accept(this);
            params.add(moveCurrentValue().value());
        }
        lambdaType.returnType().accept(this);
        var value = new com.declarative.music.interpreter.type.LambdaType(params, moveCurrentValue().value());
        currentValue = new Variant<>(value, TypeCheck.class);
    }

    @Override
    public void visit(com.declarative.music.parser.production.type.InferenceType inferenceType) {
        currentValue = new Variant<>(new com.declarative.music.interpreter.type.InferenceType(), TypeCheck.class);

    }

    @Override
    public void visit(com.declarative.music.parser.production.type.ArrayType arrayType) {
        arrayType.arrayType().accept(this);
        currentValue = new Variant<>(new com.declarative.music.interpreter.type.ArrayType(moveCurrentValue().value()), TypeCheck.class);
    }
}
