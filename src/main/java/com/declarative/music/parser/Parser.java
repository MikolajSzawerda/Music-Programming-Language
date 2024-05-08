package com.declarative.music.parser;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.terminals.OperatorEnum;
import com.declarative.music.lexer.token.Position;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.parser.exception.*;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.assign.*;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.Expression;
import com.declarative.music.parser.production.expression.VariableReference;
import com.declarative.music.parser.production.expression.arithmetic.*;
import com.declarative.music.parser.production.expression.array.ArrayExpression;
import com.declarative.music.parser.production.expression.array.ListComprehension;
import com.declarative.music.parser.production.expression.array.RangeExpression;
import com.declarative.music.parser.production.expression.lambda.ExecutionCall;
import com.declarative.music.parser.production.expression.lambda.FunctionCall;
import com.declarative.music.parser.production.expression.lambda.LambdaCall;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;
import com.declarative.music.parser.production.expression.modifier.Modifier;
import com.declarative.music.parser.production.expression.modifier.ModifierExpression;
import com.declarative.music.parser.production.expression.modifier.ModifierItem;
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import static com.declarative.music.lexer.token.TokenType.*;


public class Parser {
    private static final Map<OperatorEnum, BiFunction<Expression, Expression, Expression>> rootSuplier = Map.ofEntries(
            Map.entry(OperatorEnum.O_PLUS, AddExpression::new),
            Map.entry(OperatorEnum.O_MINUS, MinusExpression::new),
            Map.entry(OperatorEnum.O_DIVIDE, DivExpression::new),
            Map.entry(OperatorEnum.O_MUL, MulExpression::new),
            Map.entry(OperatorEnum.O_EQ, EqExpression::new),
            Map.entry(OperatorEnum.O_GREATER, GreaterExpression::new),
            Map.entry(OperatorEnum.O_GREATER_EQ, GreaterEqExpression::new),
            Map.entry(OperatorEnum.O_LESS, LessExpression::new),
            Map.entry(OperatorEnum.O_LESS_EQ, LessEqExpression::new),
            Map.entry(OperatorEnum.O_AND, AndExpression::new),
            Map.entry(OperatorEnum.O_ARROW, RangeExpression::new),
            Map.entry(OperatorEnum.O_SIM, SequenceExpression::new),
            Map.entry(OperatorEnum.O_AMPER, ParallerExpression::new),
            Map.entry(OperatorEnum.O_DOUBLE_GR, ConvolutionExpression::new),
            Map.entry(OperatorEnum.O_NEQ, NotEqExpression::new),
            Map.entry(OperatorEnum.O_MOD, ModuloExpression::new),
            Map.entry(OperatorEnum.O_OR, OrExpression::new),
            Map.entry(OperatorEnum.O_POW, PowExpression::new)
    );

    private static final Map<OperatorEnum, TriFunction<String, Expression, Position, Statement>> assignSupplier = Map.of(
            OperatorEnum.O_AMPER_ASSIGN, ParalerAssignStatement::new,
            OperatorEnum.O_MINUS_ASSIGN, MinusAssignStatement::new,
            OperatorEnum.O_MUL_ASSIGN, MulAssignStatement::new,
            OperatorEnum.O_MOD_ASSIGN, ModuloAssignStatement::new,
            OperatorEnum.O_PLUS_ASSIGN, PlusAssignStatement::new,
            OperatorEnum.O_SIM_ASSIGN, SequenceAssignStatement::new,
            OperatorEnum.O_POW_ASSIGN, PowAssignStatement::new,
            OperatorEnum.O_DIVIDE_ASSIGN, DivAssignStatement::new,
            OperatorEnum.O_ASSIGN, AssigmentStatement::new
    );
    private static final Set<TokenType> inlineArgumentsEnd = Set.of(
            TokenType.T_EOF,
            TokenType.T_R_PARENTHESIS,
            TokenType.T_SEMICOLON,
            TokenType.T_R_QAD_PARENTHESIS
    );
    private final Lexer lexer;
    private Token nextToken = null;
    private Token currentIdentifierToken = null;
    private Token currentPitchToken = null;
    private Position currentStmtPosition;

    public Parser(final Lexer lexer) {
        this.lexer = new FilteredLexer(lexer);
    }

    private static Expression retrieveExpression(final Callable<Expression> nodeSuplier) throws ParsingException {
        final Expression left;
        try {
            left = nodeSuplier.call();
        } catch (final SyntaxException exception) {
            throw new RequiredExpressionException(exception, "binary operator");
        } catch (final ParsingException e) {
            throw e.copy();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return left;
    }

    public Program parserProgram() throws IOException, ParsingException {
        consumeToken();

        final List<Statement> statements = new LinkedList<>();
        var stmt = parseStatement();
        statements.add(stmt);
        while (nextToken != null && nextToken.type() != TokenType.T_EOF) {
            stmt = parseStatement();
            statements.add(stmt);
        }

        return new Program(statements);
    }

    private void consumeToken() throws IOException {
        nextToken = lexer.getNextToken();
    }

    private Statement parseStatement() throws ParsingException, IOException {
        final Statement res = tryParseNoSemicolonedStmt();
        if (res != null) return res;
        return tryParseSemicolonedStmt();
    }

    private Statement tryParseSemicolonedStmt() throws ParsingException, IOException {
        final var res = new Try<>(TokenType.T_LET, () -> parseDeclaration(new InferenceType(currentStmtPosition)))
                .orElse(TokenType.T_RETURN, () -> new ReturnStatement(tryParseExpression(), currentStmtPosition))
                .orElse(TokenType.T_LAMBDA, () -> parseDeclaration(parseLambdaType(currentStmtPosition)))
                .orElse(TokenType.T_L_QAD_PARENTHESIS, this::tryParseArrayTypeOrArray)
                .orElseNoConsume(TokenType.T_IDENTIFIER, this::tryParseWithIdentifierAsFirst)
                .orElseNoConsume(TokenType.T_L_PARENTHESIS, () -> tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)))
                .orElseNoConsume(Set.of(T_FLOATING_NUMBER, T_INT_NUMBER,
                        T_OPERATOR, T_STRING, T_PITCH, T_RHYTHM, T_WITH, T_FALSE, T_TRUE), this::tryParseExpression)
                .get();
        if (res != null) {
            require(TokenType.T_SEMICOLON);
            return res;
        }
        throw new UnknownProductionException(nextToken);
    }

    private Statement tryParseNoSemicolonedStmt() throws ParsingException, IOException {
        return new Try<Statement>(TokenType.T_IF, this::parseIfStatement)
                .orElse(TokenType.T_FOR, this::parseForStatement)
                .orElseNoConsume(T_ELSE, () -> {
                    throw new WrongElseStatement(nextToken);
                })
                .get();
    }

    private Statement tryParseArrayTypeOrArray() throws IOException, ParsingException {
        if (nextToken.type() == TokenType.T_R_QAD_PARENTHESIS) {
            consumeToken();
            return parseDeclaration(new ArrayType(tryParseType(), currentStmtPosition));
        }
        var arrayExpression = parseArrayExpressionWithCheckedFirst();
        arrayExpression = tryEnrichExpressionWithModifier(arrayExpression);
        arrayExpression = tryEnrichExpressionWithPipe(arrayExpression);
        return arrayExpression;
    }

    private Statement tryParseWithIdentifierAsFirst() throws ParsingException, IOException {
        Type type = null;
        if ((type = tryParseSimpleType()) != null) {
            return parseDeclaration(type);
        }
        currentIdentifierToken = nextToken;
        consumeToken();
        if (nextToken.type() == T_OPERATOR && assignSupplier.containsKey((OperatorEnum) nextToken.value())) {
            final var varName = currentIdentifierToken.value();
            final var position = currentIdentifierToken.position();
            currentIdentifierToken = null;
            final var factory = assignSupplier.get((OperatorEnum) nextToken.value());
            consumeToken();
            final var expression = requireExpression("assigment");
            return factory.apply((String) varName, expression, position);
        }
        return tryParseExpression();
    }

    private Expression tryParseExpression() throws ParsingException, IOException {
        return tryEnrichExpressionWithPipe(tryParseUnpipeableExpression());
    }

    private Expression tryEnrichExpressionWithPipe(Expression expression) throws ParsingException, IOException {
        while (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_PIPE) {
            consumeToken();
            try {
                final InlineFuncCall right = tryParseInlineFuncCall();
                expression = new PipeExpression(expression, right);
            } catch (final ParsingException e) {
                throw new MissingInlineCallException(nextToken);
            }
        }
        return expression;
    }

    private Expression parseArrayExpressionWithCheckedFirst() throws ParsingException, IOException {
        final var startPosition = currentStmtPosition;
        final var expression = requireExpression("first list item");
        if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_LIST_COMPR) {
            consumeToken();
            final var tempVariable = require(TokenType.T_IDENTIFIER);
            final var range = requireExpression("list comprehension iterable");
            require(TokenType.T_R_QAD_PARENTHESIS);
            return new ListComprehension(expression, new VariableReference((String) tempVariable.value(), tempVariable.position()), range, startPosition);
        }
        final var items = parseCommaSeparatedEntity(expression, () -> requireExpression("array item"));
        require(TokenType.T_R_QAD_PARENTHESIS);
        return new ArrayExpression(items, startPosition);
    }

    private Modifier tryParseModifier() throws ParsingException, IOException {
        if (nextToken.type() != TokenType.T_L_CURL_PARENTHESIS) {
            return null;
        }
        consumeToken();
        final var modifiers = parseCommaSeparatedEntity(this::parseModifierItem);
        require(TokenType.T_R_CURL_PARENTHESIS);
        return new Modifier(modifiers);
    }

    private ModifierItem parseModifierItem() throws IOException, ParsingException {
        final var name = (String) require(TokenType.T_IDENTIFIER).value();
        require(OperatorEnum.O_ASSIGN);
        final var expression = requireExpression("modifier item");
        return new ModifierItem(name, expression);
    }

    private InlineFuncCall tryParseInlineFuncCall() throws ParsingException, IOException {
        final var nameToken = require(TokenType.T_IDENTIFIER);
        final var name = (String) nameToken.value();
        if (nextToken.type() == T_COMMA || !areInlineArguments()) {
            return new InlineFuncCall(name, List.of(), nameToken.position());
        }
        final var arguments = parseCommaSeparatedEntity(this::tryParseUnpipeableExpression);
        return new InlineFuncCall(name, arguments, nameToken.position());
    }

    private Expression tryParseUnpipeableExpression() throws ParsingException, IOException {
        var expression = new Try<>(T_L_QAD_PARENTHESIS, this::parseArrayExpressionWithCheckedFirst)
                .orElseNoConsume(T_WITH, this::tryParseLambdaExpression)
                .get();
        if (currentIdentifierToken == null && expression == null) {
            if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_PLUS) {
                consumeToken();
                expression = new PlusUnaryExpression(tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)));
            } else if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_MINUS) {
                consumeToken();
                expression = new MinusUnaryExpression(tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)));
            } else if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_NEGATE) {
                consumeToken();
                expression = new NegateExpression(tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)));
            }
        }
        if (expression == null) {
            expression = tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR));
        }
        return tryEnrichExpressionWithModifier(expression);
    }

    private Expression tryEnrichExpressionWithModifier(Expression expression) throws ParsingException, IOException {
        final Modifier modifier;
        if ((modifier = tryParseModifier()) != null) {
            expression = new ModifierExpression(expression, modifier, expression.position());
        }
        return expression;
    }

    private boolean areInlineArguments() {
        if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_PIPE) {
            return false;
        }
        return (nextToken.type() != T_OPERATOR || (nextToken.value() == OperatorEnum.O_NEGATE || nextToken.value() == OperatorEnum.O_PLUS
                || nextToken.value() == OperatorEnum.O_MINUS))
                && !inlineArgumentsEnd.contains(nextToken.type());
    }

    private Expression tryParseAndExpression() throws ParsingException, IOException {
        return tryParseExpressionNode(this::tryParseRelExpression, Set.of(OperatorEnum.O_AND));
    }

    private Expression tryParseRelExpression() throws ParsingException, IOException {
        return tryParseExpressionNode(this::tryParseAddExpression,
                Set.of(OperatorEnum.O_EQ,
                        OperatorEnum.O_GREATER_EQ,
                        OperatorEnum.O_LESS,
                        OperatorEnum.O_LESS_EQ,
                        OperatorEnum.O_GREATER,
                        OperatorEnum.O_NEQ, OperatorEnum.O_ARROW));
    }

    private Expression tryParseAddExpression() throws ParsingException, IOException {
        return tryParseExpressionNode(this::tryParseMulExpression, Set.of(OperatorEnum.O_PLUS, OperatorEnum.O_SIM, OperatorEnum.O_MINUS));
    }

    private Expression tryParseMulExpression() throws ParsingException, IOException {
        return tryParseExpressionNode(this::tryParseHExpression,
                Set.of(OperatorEnum.O_MUL, OperatorEnum.O_AMPER, OperatorEnum.O_DIVIDE, OperatorEnum.O_MOD));
    }

    private Expression tryParseHExpression() throws ParsingException, IOException {
        final var term = tryParseExpressionNode(this::tryParseLeaf, Set.of(OperatorEnum.O_POW, OperatorEnum.O_DOUBLE_GR));
        if (nextToken.type() == TokenType.T_AS) {
            final var position = nextToken.position();
            consumeToken();
            final var type = tryParseType();
            return new CastExpresion(term, type, position);
        }
        return term;
    }

    private Expression tryParseLeaf() throws ParsingException, IOException {
        if (currentIdentifierToken != null) {
            final var position = currentIdentifierToken.position();
            final var name = (String) currentIdentifierToken.value();
            currentIdentifierToken = null;
            return parseVariableOrFuncCall(name, position);
        }
        if (currentPitchToken != null) {
            final var position = currentPitchToken.position();
            final var pitch = (String) currentPitchToken.value();
            currentPitchToken = null;
            return new NoteExpression(pitch, null, null, position);
        }
        if (nextToken.type() == TokenType.T_L_PARENTHESIS) {

            final var note = tryParseNoteExpression();
            if (note != null) {
                return note;
            }
            final var expresion = tryParseExpression();
            require(TokenType.T_R_PARENTHESIS);
            return expresion;
        }

        if (nextToken.type() == TokenType.T_IDENTIFIER) {
            final var position = nextToken.position();
            final var name = (String) nextToken.value();
            consumeToken();
            return parseVariableOrFuncCall(name, position);

        }
        final var value = require(Set.of(
                T_INT_NUMBER, T_FLOATING_NUMBER, T_STRING, T_PITCH, T_RHYTHM, T_TRUE, T_FALSE
        ));
        return switch (value.type()) {
            case T_INT_NUMBER -> new IntLiteral((int) value.value(), value.position());
            case T_PITCH -> new NoteExpression((String) value.value(), null, null, value.position());
            case T_RHYTHM -> new NoteExpression(null, null, (String) value.value(), value.position());
            case T_FLOATING_NUMBER -> new FloatLiteral((double) value.value(), value.position());
            case T_STRING -> new StringLiter((String) value.value(), value.position());
            case T_TRUE -> new BoolLiteral(true, value.position());
            case T_FALSE -> new BoolLiteral(false, value.position());
            default -> throw new IllegalStateException();
        };
    }

    private Expression parseVariableOrFuncCall(final String name, final Position position) throws ParsingException, IOException {
        if (nextToken.type() == TokenType.T_L_PARENTHESIS) {
            return parseExecutionCall(name, position);
        }
        return new VariableReference(name, position);

    }

    private ExecutionCall parseExecutionCall(final String name, final Position position) throws ParsingException, IOException {
        require(TokenType.T_L_PARENTHESIS);
        if (nextToken.type() == TokenType.T_R_PARENTHESIS) {
            consumeToken();
            return new FunctionCall(name, List.of(), position);
        }

        ExecutionCall call = new FunctionCall(name, parseArguments(), position);
        while (nextToken.type() == TokenType.T_L_PARENTHESIS) {
            consumeToken();
            call = new LambdaCall(call, parseArguments(), position);
        }
        return call;

    }

    private List<Expression> parseArguments() throws ParsingException, IOException {
        final var arguments = parseCommaSeparatedEntity(this::tryParseExpression);
        require(TokenType.T_R_PARENTHESIS);
        return arguments;
    }

    private Expression tryParseExpressionNode(final Callable<Expression> nodeSuplier, final Set<OperatorEnum> operators)
            throws ParsingException, IOException {
        Expression left = retrieveExpression(nodeSuplier);
        while (nextToken.type() == T_OPERATOR && operators.contains((OperatorEnum) nextToken.value())) {
            final var currentOperator = (OperatorEnum) nextToken.value();
            if (!rootSuplier.containsKey(currentOperator)) {
                throw new UnsupportedBinaryOperator(nextToken);
            }
            consumeToken();
            final var right = retrieveExpression(nodeSuplier);
            left = rootSuplier.get(currentOperator).apply(left, right);
        }
        return left;
    }

    private LambdaExpression tryParseLambdaExpression() throws ParsingException, IOException {
        if (nextToken.type() == T_WITH) {
            final var startPosition = nextToken.position();
            consumeToken();
            final var parameters = parseParameters();
            require(OperatorEnum.O_ARROW);
            final var returnType = tryParseType();
            final var block = tryParseBlock();
            return new LambdaExpression(parameters, returnType, block, startPosition);
        }
        return null;
    }

    private NoteExpression tryParseNoteExpression() throws ParsingException, IOException {
        final var startPosition = nextToken.position();
        consumeToken();
        if (nextToken.type() == T_PITCH) {
            final var pitch = nextToken;
            consumeToken();
            if (nextToken.type() != TokenType.T_COMMA) {
                currentPitchToken = pitch;
                return null;
            }
            require(TokenType.T_COMMA);
            final var octave = requireExpression("note octave");
            require(TokenType.T_R_PARENTHESIS);
            String duration = null;
            if (nextToken.type() == T_RHYTHM) {
                duration = (String) nextToken.value();
                consumeToken();
            }
            return new NoteExpression((String) pitch.value(), octave, duration, startPosition);
        }
        return null;
    }

    private Parameters parseParameters() throws IOException, ParsingException {
        require(TokenType.T_L_PARENTHESIS);
        if (nextToken.type() == TokenType.T_R_PARENTHESIS) {
            consumeToken();
            return new Parameters(List.of());
        }
        final var parameters = parseCommaSeparatedEntity(this::parseParameterItem);
        require(TokenType.T_R_PARENTHESIS);
        return new Parameters(parameters);

    }

    private Parameter parseParameterItem() throws ParsingException, IOException {
        final var parameterType = tryParseType();
        final var parameterName = require(TokenType.T_IDENTIFIER).value();
        return new Parameter(parameterType, (String) parameterName);
    }

    private Expression requireExpression(final String production) throws RequiredExpressionException {
        try {
            return tryParseExpression();
        } catch (final ParsingException e) {
            throw new RequiredExpressionException(e, production);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Type tryParseType() throws IOException, ParsingException {
        if (nextToken.type() == TokenType.T_IDENTIFIER) {
            return tryParseSimpleType();
        }
        if (nextToken.type() == TokenType.T_L_QAD_PARENTHESIS) {
            final var position = nextToken.position();
            consumeToken();
            require(TokenType.T_R_QAD_PARENTHESIS);
            return new ArrayType(tryParseType(), position);
        }
        if (nextToken.type() == TokenType.T_LAMBDA) {
            final var position = nextToken.position();
            consumeToken();
            return parseLambdaType(position);
        }
        return null;

    }

    private Type parseLambdaType(final Position position) throws IOException, ParsingException {
        require(TokenType.T_L_PARENTHESIS);
        if (nextToken.type() == TokenType.T_R_PARENTHESIS) {
            consumeToken();
            require(OperatorEnum.O_ARROW);
            final var returnType = tryParseType();
            return new LambdaType(List.of(), returnType, position);
        }
        final var types = parseCommaSeparatedEntity(this::tryParseType);
        require(TokenType.T_R_PARENTHESIS);
        require(OperatorEnum.O_ARROW);
        final var returnType = tryParseType();
        return new LambdaType(types, returnType, position);
    }

    private Type tryParseSimpleType() throws IOException {
        try {
            final var position = nextToken.position();
            final var type = Types.valueOf((String) nextToken.value());
            consumeToken();
            return new SimpleType(type, position);
        } catch (final IllegalArgumentException ex) {
            return null;
        }

    }

    private Statement parseDeclaration(final Type type) throws ParsingException, IOException {
        final var varName = (String) require(TokenType.T_IDENTIFIER).value();
        if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_ASSIGN) {
            consumeToken();
            return new Declaration(type, varName, tryParseExpression());
        }
        return new Declaration(type, varName, null);

    }

    private Statement parseForStatement() throws Exception {
        require(TokenType.T_L_PARENTHESIS);
        final var type = tryParseType();
        final var name = (String) require(TokenType.T_IDENTIFIER).value();
        require(TokenType.T_IN);
        final var iterable = requireExpression("for statement iterable");
        require(TokenType.T_R_PARENTHESIS);
        final var block = tryParseBlock();
        return new ForStatement(new Declaration(type, name, null), iterable, block, currentStmtPosition);

    }

    private Block tryParseBlock() throws ParsingException, IOException {
        final var position = nextToken.position();
        require(TokenType.T_L_CURL_PARENTHESIS);
        final var stms = new LinkedList<Statement>();
        while (nextToken.type() != TokenType.T_R_CURL_PARENTHESIS) {
            stms.add(parseStatement());
        }
        require(TokenType.T_R_CURL_PARENTHESIS);

        return new Block(stms, position);
    }

    private IfStatement parseIfStatement() throws ParsingException, IOException {
        final var startPosition = currentStmtPosition;
        require(TokenType.T_L_PARENTHESIS);
        final var condition = requireExpression("if statement");
        require(TokenType.T_R_PARENTHESIS);
        final var block = tryParseBlock();
        IfStatement alternative = null;
        if (nextToken.type() == TokenType.T_ELSE) {
            final var position = nextToken.position();
            consumeToken();
            if (nextToken.type() == TokenType.T_L_CURL_PARENTHESIS) {
                final var elseBlock = tryParseBlock();
                alternative = new IfStatement(null, elseBlock, position);
            } else {
                currentStmtPosition = nextToken.position();
                require(TokenType.T_IF);
                alternative = parseIfStatement();
            }
        }
        return new IfStatement(condition, block, alternative, startPosition);

    }

    private Token require(final Set<TokenType> requiredTokenTypes, final Set<Object> requiredValue) throws IOException, ParsingException {
        if (nextToken != null && requiredTokenTypes.contains(nextToken.type())) {
            final var currentToken = nextToken;
            if (requiredValue == null) {
                consumeToken();
                return currentToken;
            }
            if (requiredValue.contains(currentToken.value())) {
                consumeToken();
                return currentToken;
            }
        }
        if (Set.of(T_R_PARENTHESIS, T_R_QAD_PARENTHESIS, T_R_CURL_PARENTHESIS).stream()
                .anyMatch(requiredTokenTypes::contains)) {
            throw new MissingParenthesisException(requiredTokenTypes, nextToken);
        }
        throw new SyntaxException(requiredTokenTypes, nextToken);
    }

    private void require(final OperatorEnum requiredValue) throws IOException, ParsingException {
        require(Set.of(T_OPERATOR), Set.of(requiredValue));
    }

    private Token require(final TokenType requiredTokenTypes) throws IOException, ParsingException {
        return require(Set.of(requiredTokenTypes), null);
    }

    private Token require(final Set<TokenType> requiredTokenTypes) throws IOException, ParsingException {
        return require(requiredTokenTypes, null);
    }

    private <T> List<T> parseCommaSeparatedEntity(final Callable<T> itemSupplier) throws ParsingException, IOException {
        try {
            final var firstItem = itemSupplier.call();
            return parseCommaSeparatedEntity(firstItem, itemSupplier);
        } catch (final ParsingException e) {
            throw new ParsingException(e);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> parseCommaSeparatedEntity(final T firstItem, final Callable<T> itemSupplier) throws ParsingException, IOException {
        assert firstItem != null;
        final var result = new LinkedList<T>();

        result.add(firstItem);
        while (nextToken.type() == T_COMMA) {
            consumeToken();
            try {
                result.add(itemSupplier.call());
            } catch (final ParsingException e) {
                throw new ParsingException(e);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    private class Try<T> {
        private T result;

        public Try(final TokenType intitToken, final Callable<T> initSupplier) throws ParsingException, IOException {
            this.result = tryCall(Set.of(intitToken), initSupplier);
        }

        private T tryCall(final Set<TokenType> tokens, final Callable<T> initSupplier, final boolean consume) throws ParsingException, IOException {
            if (tokens.contains(nextToken.type())) {
                currentStmtPosition = nextToken.position();
                if (consume) {
                    consumeToken();
                }
                try {
                    return initSupplier.call();
                } catch (final ParsingException exception) {
                    throw new ParsingException(exception);
                } catch (final Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
            return null;
        }

        private T tryCall(final Set<TokenType> tokens, final Callable<T> initSupplier) throws ParsingException, IOException {
            return tryCall(tokens, initSupplier, true);
        }

        public Try<T> orElse(final TokenType tokens, final Callable<T> supplier) throws ParsingException, IOException {
            return orElse(Set.of(tokens), supplier);
        }

        public Try<T> orElseNoConsume(final TokenType tokens, final Callable<T> supplier) throws ParsingException, IOException {
            return orElseNoConsume(Set.of(tokens), supplier);
        }

        public Try<T> orElse(final Set<TokenType> tokens, final Callable<T> supplier) throws ParsingException, IOException {
            if (result == null) {
                this.result = tryCall(tokens, supplier);

            }
            return this;
        }

        public Try<T> orElseNoConsume(final Set<TokenType> tokens, final Callable<T> supplier) throws ParsingException, IOException {
            if (result == null) {
                this.result = tryCall(tokens, supplier, false);

            }
            return this;
        }

        public T get() {
            return result;
        }
    }
}
