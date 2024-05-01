package com.declarative.music.parser;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.terminals.OperatorEnum;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.parser.production.*;
import com.declarative.music.parser.production.expression.CastExpresion;
import com.declarative.music.parser.production.expression.Expression;
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
import com.declarative.music.parser.production.expression.modifier.Modifier;
import com.declarative.music.parser.production.expression.modifier.ModifierExpression;
import com.declarative.music.parser.production.expression.modifier.ModifierItem;
import com.declarative.music.parser.production.expression.music.NoteExpression;
import com.declarative.music.parser.production.expression.pipe.InlineFuncCall;
import com.declarative.music.parser.production.expression.pipe.PipeExpression;
import com.declarative.music.parser.production.expression.relation.AndExpression;
import com.declarative.music.parser.production.expression.relation.EqExpression;
import com.declarative.music.parser.production.literal.IntLiteral;
import com.declarative.music.parser.production.type.LeafType;
import com.declarative.music.parser.production.type.Type;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class Parser {
    private static final Map<OperatorEnum, BiFunction<Expression, Expression, Expression>> rootSuplier = Map.of(
            OperatorEnum.O_PLUS, AddExpression::new,
            OperatorEnum.O_MUL, MulExpression::new,
            OperatorEnum.O_EQ, EqExpression::new,
            OperatorEnum.O_AND, AndExpression::new,
            OperatorEnum.O_ARROW, RangeExpression::new
    );
    private final Lexer lexer;
    private Token nextToken = null;
    private Token currentIdentifierToken = null;

    public Program parserProgram() throws Exception {
        consumeToken();

        final List<Statement> statements = new LinkedList<>();
        var stmt = parseStatement();
        require(TokenType.T_SEMICOLON);
        statements.add(stmt);
        while (stmt != null && (nextToken != null && nextToken.type() != TokenType.T_EOF)) {
            stmt = parseStatement();
            require(TokenType.T_SEMICOLON);
            statements.add(stmt);
        }

        return new Program(statements);
    }

    private void consumeToken() throws IOException {
        nextToken = lexer.getNextToken();
    }


    private Statement parseStatement() throws Exception {
        return switch (nextToken.type()) {
            case T_IF -> {
                consumeToken();
                yield parseIfStatement();
            }
            case T_FOR -> {
                consumeToken();
                yield parseForStatemnt();
            }
            case T_WITH -> {
                consumeToken();
                yield parseLambdaExpression();
            }
            case T_LET -> {
                consumeToken();
                yield parseDeclaration(null);
            }
            case T_L_QAD_PARENTHESIS -> {
                consumeToken();
                if (nextToken.type() == TokenType.T_R_QAD_PARENTHESIS) {
                    yield parseArrayType();
                }
                yield tryParseArrayExpression(true);
            }
            default -> {
                Type type = null;
                if ((type = tryParseType()) != null) {
                    yield parseDeclaration(type);
                }
                if (nextToken.type() == TokenType.T_IDENTIFIER) {
                    currentIdentifierToken = nextToken;
                    consumeToken();
                    if (nextToken.type() == TokenType.T_OPERATOR && nextToken.value() == OperatorEnum.O_ASSIGN) {
                        AssigmentStatement assigmentStatement = null;
                        if ((assigmentStatement = tryParseAssigment()) != null) {
                            yield assigmentStatement;
                        }
                    }

                    Expression expression = null;
                    if ((expression = tryParseExpression()) != null) {
                        yield expression;
                    }

                }
                Expression expression = null;
                if ((expression = tryParseExpression()) != null) {
                    yield expression;
                }
                throw new UnsupportedOperationException();
            }
        };

    }

    private Expression tryParseExpression() throws Exception {

        Expression expression = tryParseArrayExpression(false);
        if (expression == null) {
            expression = tryParseLambdaExpression();
        }
        if (expression == null) {
            if (currentIdentifierToken == null) {
                if (nextToken.type() == TokenType.T_OPERATOR && nextToken.value() == OperatorEnum.O_PLUS) {
                    consumeToken();
                    expression = new PlusUnaryExpression(tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)));
                } else if (nextToken.type() == TokenType.T_OPERATOR && nextToken.value() == OperatorEnum.O_MINUS) {
                    consumeToken();
                    expression = new MinusUnaryExpression(tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)));
                }
            }
            if (expression == null) {
                expression = tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR));
            }
        }

        Modifier modifier = null;
        if ((modifier = tryParseModifier()) != null) {
            expression = new ModifierExpression(expression, modifier);
        }

        while (nextToken.type() == TokenType.T_OPERATOR && nextToken.value() == OperatorEnum.O_PIPE) {
            consumeToken();
            final InlineFuncCall right = tryParseInlineFuncCall();
            expression = new PipeExpression(expression, right);
        }

        return expression;
    }

    private Expression tryParseArrayExpression(final boolean checked) throws Exception {
        if (!checked && nextToken.type() != TokenType.T_L_QAD_PARENTHESIS) {
            return null;
        }
        if (!checked) {
            consumeToken();
        }
        final var expression = tryParseExpression();
        if (nextToken.type() == TokenType.T_OPERATOR && nextToken.value() == OperatorEnum.O_LIST_COMPR) {
            consumeToken();
            final var tempVariable = require(TokenType.T_IDENTIFIER);
            final var range = tryParseExpression();
            require(TokenType.T_R_QAD_PARENTHESIS);
            return new ListComprehension(expression, new VariableReference((String) tempVariable.value()), range);
        }
        final var items = new LinkedList<Expression>();
        items.add(expression);
        while (nextToken.type() == TokenType.T_COMMA) {
            consumeToken();
            items.add(tryParseExpression());
        }
        require(TokenType.T_R_QAD_PARENTHESIS);
        return new ArrayExpression(items);
    }

    private Modifier tryParseModifier() throws Exception {
        if (nextToken.type() != TokenType.T_L_CURL_PARENTHESIS) {
            return null;
        }
        final var modifiers = new LinkedList<ModifierItem>();
        consumeToken();
        var name = (String) require(TokenType.T_IDENTIFIER).value();
        require(TokenType.T_OPERATOR, OperatorEnum.O_ASSIGN);
        var expression = tryParseExpression();
        modifiers.add(new ModifierItem(name, expression));
        while (nextToken.type() == TokenType.T_COMMA) {
            consumeToken();
            name = (String) require(TokenType.T_IDENTIFIER).value();
            require(TokenType.T_OPERATOR, OperatorEnum.O_ASSIGN);
            expression = tryParseExpression();
            modifiers.add(new ModifierItem(name, expression));
        }
        require(TokenType.T_R_CURL_PARENTHESIS);
        return new Modifier(modifiers);
    }

    private InlineFuncCall tryParseInlineFuncCall() throws Exception {
        final var name = (String) require(TokenType.T_IDENTIFIER).value();
        final var arguments = new LinkedList<Expression>();
        while (nextToken.type() != TokenType.T_OPERATOR && nextToken.value() != OperatorEnum.O_PIPE
                && nextToken.type() != TokenType.T_EOF
                && nextToken.type() != TokenType.T_COMMA
                && nextToken.type() != TokenType.T_R_PARENTHESIS
                && nextToken.type() != TokenType.T_SEMICOLON
                && nextToken.type() != TokenType.T_R_QAD_PARENTHESIS) {
            arguments.add(tryParseExpression());
        }
        return new InlineFuncCall(name, arguments);

    }

    private Expression tryParseAndExpression() throws Exception {
        return tryParseExpressionNode(this::tryParseRelExpression, Set.of(OperatorEnum.O_AND));
    }

    private Expression tryParseRelExpression() throws Exception {
        return tryParseExpressionNode(this::tryParseAddExpression, Set.of(OperatorEnum.O_EQ));
    }


    private Expression tryParseAddExpression() throws Exception {
        return tryParseExpressionNode(this::tryParseMulExpression, Set.of(OperatorEnum.O_PLUS));
    }

    private Expression tryParseMulExpression() throws Exception {
        return tryParseExpressionNode(this::tryParseHExpression, Set.of(OperatorEnum.O_MUL));
    }

    private Expression tryParseHExpression() throws Exception {
        final var term = tryParseExpressionNode(this::tryParseLeaf, Set.of(OperatorEnum.O_ARROW));
        if (nextToken.type() == TokenType.T_AS) {
            consumeToken();
            final var type = tryParseType();
            return new CastExpresion(term, type);
        }
        return term;
    }

    private Expression tryParseLeaf() throws Exception {
        if (currentIdentifierToken != null) {
            final var name = (String) currentIdentifierToken.value();
            currentIdentifierToken = null;
            return parseVariableOrFuncCall(name);
        }
        if (nextToken.type() == TokenType.T_L_PARENTHESIS) {
            final var pitch = tryParseNoteExpression();
            if (pitch != null) return pitch;
            final var expresion = tryParseExpression();
            require(TokenType.T_R_PARENTHESIS);
            return expresion;
        }

        if (nextToken.type() == TokenType.T_IDENTIFIER) {
            final var name = (String) nextToken.value();
            consumeToken();
            return parseVariableOrFuncCall(name);

        }
        final var value = require(Set.of(
                TokenType.T_INT_NUMBER, TokenType.T_FLOATING_NUMBER, TokenType.T_STRING, TokenType.T_PITCH
        ));
        if (value.type() == TokenType.T_PITCH) {
            return new NoteExpression((String) value.value(), null, null);
        }
        return new IntLiteral((int) value.value());
    }

    private Expression parseVariableOrFuncCall(final String name) throws Exception {
        if (nextToken.type() != TokenType.T_L_PARENTHESIS) {
            return new VariableReference(name);
        }
        consumeToken();
        if (nextToken.type() == TokenType.T_R_PARENTHESIS) {
            consumeToken();
            return new LambdaCall(name, List.of());
        }
        Expression expression = null;
        if ((expression = tryParseExpression()) == null) {
            return new LambdaCall(name, List.of());
        }
        final var arguments = new LinkedList<Expression>();
        arguments.add(expression);
        while (nextToken.type() == TokenType.T_COMMA) {
            consumeToken();
            if ((expression = tryParseExpression()) != null) {
                arguments.add(expression);
            }
        }
        require(TokenType.T_R_PARENTHESIS);
        return new LambdaCall(name, arguments);
    }


    private Expression tryParseExpressionNode(final Callable<Expression> nodeSuplier, final Set<OperatorEnum> operators) throws Exception {
        var left = nodeSuplier.call();
        while (nextToken.type() == TokenType.T_OPERATOR && operators.contains((OperatorEnum) nextToken.value())) {
            final var currentOperator = (OperatorEnum) nextToken.value();
            consumeToken();
            final var right = nodeSuplier.call();
            left = rootSuplier.get(currentOperator).apply(left, right);
        }
        return left;
    }

    private LambdaExpression tryParseLambdaExpression() throws Exception {
        if (nextToken.type() == TokenType.T_WITH) {
            consumeToken();
            final var parameters = parseParameters();
            require(TokenType.T_OPERATOR, OperatorEnum.O_ARROW);
            final var returnType = require(TokenType.T_IDENTIFIER);
            final var block = tryParseBlock();
            return new LambdaExpression(parameters, (String) returnType.value(), block);
        }
        return null;
    }

    private NoteExpression tryParseNoteExpression() throws Exception {
        consumeToken();
        if (nextToken.type() == TokenType.T_PITCH) {
            final var pitch = nextToken.value();
            consumeToken();
            require(TokenType.T_COMMA);
            final var octave = tryParseExpression();
            require(TokenType.T_R_PARENTHESIS);
            String duration = null;
            if (nextToken.type() == TokenType.T_RHYTHM) {
                duration = (String) nextToken.value();
                consumeToken();
            }
            return new NoteExpression((String) pitch, octave, duration);
        }
        return null;
    }

    private Parameters parseParameters() throws IOException {
        require(TokenType.T_L_PARENTHESIS);
        if (nextToken.type() == TokenType.T_R_PARENTHESIS) {
            return new Parameters(List.of());
        }
        final var parameters = new LinkedList<Parameter>();
        while (nextToken.type() != TokenType.T_R_PARENTHESIS) {
            final var parameterType = require(TokenType.T_IDENTIFIER).value();
            final var parameterName = require(TokenType.T_IDENTIFIER).value();
            parameters.add(new Parameter((String) parameterType, (String) parameterName));
            if (nextToken.type() == TokenType.T_R_PARENTHESIS) {
                break;
            }
            require(TokenType.T_COMMA);
        }
        require(TokenType.T_R_PARENTHESIS);
        return new Parameters(parameters);

    }

    private AssigmentStatement tryParseAssigment() throws Exception {
        if (currentIdentifierToken.type() != TokenType.T_IDENTIFIER && nextToken.type() != TokenType.T_OPERATOR && nextToken.value() != OperatorEnum.O_ASSIGN) {
            return null;
        }
        final var varName = currentIdentifierToken.value();
        currentIdentifierToken = null;
        require(TokenType.T_OPERATOR, OperatorEnum.O_ASSIGN);
        final var expression = tryParseExpression();
        if (expression == null) {
            throw new IllegalStateException();
        }
        return new AssigmentStatement((String) varName, expression);

    }

    private Type tryParseType() throws IOException {
        try {
            if (nextToken.type() != TokenType.T_IDENTIFIER) {
                return null;
            }
            final var type = Types.valueOf((String) nextToken.value());
            consumeToken();
            return new LeafType(type);
        } catch (final IllegalArgumentException ex) {
            return null;
        }

    }

    private Statement parseArrayExpression() {
        return null;

    }

    private Statement parseArrayType() {
        return null;

    }

    private Statement parseDeclaration(final Type type) throws Exception {
        final var varName = (String) require(TokenType.T_IDENTIFIER).value();
        if (nextToken.type() == TokenType.T_OPERATOR && nextToken.value() == OperatorEnum.O_ASSIGN) {
            consumeToken();
            return new Declaration(type, varName, tryParseExpression());
        }
        return new Declaration(type, varName, null);

    }

    private Statement parseLambdaExpression() {
        return null;

    }

    private Statement parseForStatemnt() throws Exception {
        require(TokenType.T_L_PARENTHESIS);
        final var type = tryParseType();
        final var name = (String) require(TokenType.T_IDENTIFIER).value();
        require(TokenType.T_IN);
        final var iterable = tryParseExpression();
        require(TokenType.T_R_PARENTHESIS);
        final var block = tryParseBlock();
        return new ForStatement(new Declaration(type, name, null), iterable, block);

    }

    private Block tryParseBlock() throws Exception {
        require(TokenType.T_L_CURL_PARENTHESIS);
        final var stms = new LinkedList<Statement>();
        while (nextToken.type() != TokenType.T_R_CURL_PARENTHESIS) {
            stms.add(parseStatement());
            require(TokenType.T_SEMICOLON);
        }
        require(TokenType.T_R_CURL_PARENTHESIS);

        return new Block(stms);
    }

    private IfStatement parseIfStatement() throws Exception {
        require(TokenType.T_L_PARENTHESIS);
        final var condition = tryParseExpression();
        require(TokenType.T_R_PARENTHESIS);
        final var block = tryParseBlock();
        IfStatement alternative = null;
        if (nextToken.type() == TokenType.T_ELSE) {
            consumeToken();
            if (nextToken.type() == TokenType.T_L_CURL_PARENTHESIS) {
                final var elseBlock = tryParseBlock();
                alternative = new IfStatement(null, elseBlock);
            } else {
                require(TokenType.T_IF);
                alternative = parseIfStatement();
            }
        }
        return new IfStatement(condition, block, alternative);

    }

    private Token require(final Set<TokenType> requiredTokenTypes, final Object requiredValue) throws IOException {
        if (nextToken != null && requiredTokenTypes.contains(nextToken.type())) {
            final var currentToken = nextToken;
            if (requiredValue == null) {
                consumeToken();
                return currentToken;
            }
            if (requiredValue == currentToken.value()) {
                consumeToken();
                return currentToken;
            }
        }
        //TODO custom exc
        throw new RuntimeException();
    }

    private Token require(final TokenType requiredTokenTypes, final Object requiredValue) throws IOException {
        return require(Set.of(requiredTokenTypes), requiredValue);
    }

    private Token require(final TokenType requiredTokenTypes) throws IOException {
        return require(Set.of(requiredTokenTypes), null);
    }

    private Token require(final Set<TokenType> requiredTokenTypes) throws IOException {
        return require(requiredTokenTypes, null);
    }
}
