package com.declarative.music.parser;

import static com.declarative.music.lexer.token.TokenType.T_COMMA;
import static com.declarative.music.lexer.token.TokenType.T_ELSE;
import static com.declarative.music.lexer.token.TokenType.T_FALSE;
import static com.declarative.music.lexer.token.TokenType.T_FLOATING_NUMBER;
import static com.declarative.music.lexer.token.TokenType.T_IF;
import static com.declarative.music.lexer.token.TokenType.T_INT_NUMBER;
import static com.declarative.music.lexer.token.TokenType.T_LET;
import static com.declarative.music.lexer.token.TokenType.T_L_QAD_PARENTHESIS;
import static com.declarative.music.lexer.token.TokenType.T_OPERATOR;
import static com.declarative.music.lexer.token.TokenType.T_PITCH;
import static com.declarative.music.lexer.token.TokenType.T_RHYTHM;
import static com.declarative.music.lexer.token.TokenType.T_STRING;
import static com.declarative.music.lexer.token.TokenType.T_TRUE;
import static com.declarative.music.lexer.token.TokenType.T_WITH;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import com.declarative.music.lexer.Lexer;
import com.declarative.music.lexer.terminals.OperatorEnum;
import com.declarative.music.lexer.token.Token;
import com.declarative.music.lexer.token.TokenType;
import com.declarative.music.parser.exception.SyntaxException;
import com.declarative.music.parser.production.AssigmentStatement;
import com.declarative.music.parser.production.Block;
import com.declarative.music.parser.production.Declaration;
import com.declarative.music.parser.production.ForStatement;
import com.declarative.music.parser.production.IfStatement;
import com.declarative.music.parser.production.Parameter;
import com.declarative.music.parser.production.Parameters;
import com.declarative.music.parser.production.Program;
import com.declarative.music.parser.production.ReturnStatement;
import com.declarative.music.parser.production.Statement;
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
import com.declarative.music.parser.production.type.LambdaType;
import com.declarative.music.parser.production.type.SimpleType;
import com.declarative.music.parser.production.type.Type;


public class Parser
{
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

    private static final Map<OperatorEnum, BiFunction<String, Expression, Statement>> assignSupplier = Map.of(
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
//        TokenType.T_COMMA,
        TokenType.T_R_PARENTHESIS,
        TokenType.T_SEMICOLON,
        TokenType.T_R_QAD_PARENTHESIS
    );
    private final Lexer lexer;
    private Token nextToken = null;
    private Token currentIdentifierToken = null;
    private Token currentPitchToken = null;

    public Parser(final Lexer lexer)
    {
        this.lexer = new FilteredLexer(lexer);
    }

    public Program parserProgram() throws Exception
    {
        consumeToken();

        final List<Statement> statements = new LinkedList<>();
        var stmt = parseStatement();
        statements.add(stmt);
        while (nextToken != null && nextToken.type() != TokenType.T_EOF)
        {
            stmt = parseStatement();
            statements.add(stmt);
        }

        return new Program(statements);
    }

    private void consumeToken() throws IOException
    {
        nextToken = lexer.getNextToken();
    }

    private Statement parseStatement() throws Exception
    {
        //Statements with no need of semicolon after
        var res = new Try<Statement>(TokenType.T_IF, this::parseIfStatement)
            .orElse(TokenType.T_FOR, this::parseForStatemnt)
            .orElseNoConsume(T_ELSE, () -> {
                throw new SyntaxException(Set.of(T_IF), nextToken, "if stmt should start with if token");
            })
            .get();
        if (res != null)
        {
            return res;
        }
        res = new Try<>(TokenType.T_LET, () -> parseDeclaration(new SimpleType(Types.LET)))
            .orElse(TokenType.T_RETURN, () -> new ReturnStatement(tryParseExpression()))
            .orElse(TokenType.T_LAMBDA, () -> parseDeclaration(parseLambdaType()))
            .orElse(TokenType.T_L_QAD_PARENTHESIS, this::tryParseArrayTypeOrArray)
            .orElseNoConsume(TokenType.T_IDENTIFIER, this::tryParseWithIdentifierAsFirst)
            .orElseNoConsume(TokenType.T_L_PARENTHESIS, () -> tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)))
            .orElseNoConsume(Set.of(T_FLOATING_NUMBER, T_INT_NUMBER,
                T_OPERATOR, T_STRING, T_PITCH, T_RHYTHM, T_WITH, T_FALSE, T_TRUE), this::tryParseExpression)
            .get();
        if (res != null)
        {
            require(TokenType.T_SEMICOLON);
            return res;
        }
        final var msg = "L: %d C: %d Token: (%s, %s)".formatted(nextToken.position().line() + 1,
            nextToken.position().characterNumber(), nextToken.type(), nextToken.value()
        );
        throw new UnsupportedOperationException(msg);
    }

    private Statement tryParseArrayTypeOrArray() throws Exception
    {
        if (nextToken.type() == TokenType.T_R_QAD_PARENTHESIS)
        {
            consumeToken();
            return parseDeclaration(new ArrayType(tryParseType()));
        }
        var arrayExpression = parseArrayExpressionWithCheckedFirst();
        arrayExpression = tryEnrichExpressionWithModifier(arrayExpression);
        arrayExpression = tryEnrichExpressionWithPipe(arrayExpression);
        return arrayExpression;
    }

    private Statement tryParseWithIdentifierAsFirst() throws Exception
    {
        Type type = null;
        if ((type = tryParseSimpleType()) != null)
        {
            return parseDeclaration(type);
        }
        currentIdentifierToken = nextToken;
        consumeToken();
        if (nextToken.type() == T_OPERATOR && assignSupplier.containsKey((OperatorEnum) nextToken.value()))
        {
            Statement assigmentStatement = null;
            if ((assigmentStatement = tryParseAssigment()) != null)
            {
                return assigmentStatement;
            }
        }
        return tryParseExpression();
    }

    private Expression tryParseExpression() throws Exception
    {
        return tryEnrichExpressionWithPipe(tryParseUnpipeableExpression());
    }

    private Expression tryEnrichExpressionWithPipe(Expression expression) throws Exception
    {
        while (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_PIPE)
        {
            consumeToken();
            try
            {

                final InlineFuncCall right = tryParseInlineFuncCall();
                expression = new PipeExpression(expression, right);
            }
            catch (SyntaxException e)
            {
                throw new SyntaxException(e.getRequiredTokenTypes(), e.getCurrentToken(), "inline call expected after pipe operator");
            }
        }
        return expression;
    }

    private Expression parseArrayExpressionWithCheckedFirst() throws Exception
    {
        final var expression = tryParseExpression();
        if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_LIST_COMPR)
        {
            consumeToken();
            final var tempVariable = require(TokenType.T_IDENTIFIER);
            final var range = tryParseExpression();
            require(TokenType.T_R_QAD_PARENTHESIS);
            return new ListComprehension(expression, new VariableReference((String) tempVariable.value()), range);
        }
        final var items = new LinkedList<Expression>();
        items.add(expression);
        while (nextToken.type() == TokenType.T_COMMA)
        {
            consumeToken();
            try
            {

                items.add(tryParseExpression());
            }
            catch (SyntaxException e)
            {
                throw new SyntaxException(e.getRequiredTokenTypes(), e.getCurrentToken(), "no expression after comma in array expression");
            }
        }
        require(TokenType.T_R_QAD_PARENTHESIS);
        return new ArrayExpression(items);
    }

    private Modifier tryParseModifier() throws Exception
    {
        if (nextToken.type() != TokenType.T_L_CURL_PARENTHESIS)
        {
            return null;
        }
        final var modifiers = new LinkedList<ModifierItem>();
        consumeToken();
        var name = (String) require(TokenType.T_IDENTIFIER).value();
        require(OperatorEnum.O_ASSIGN);
        var expression = tryParseExpression();
        modifiers.add(new ModifierItem(name, expression));
        while (nextToken.type() == TokenType.T_COMMA)
        {
            consumeToken();
            name = (String) require(TokenType.T_IDENTIFIER).value();
            require(OperatorEnum.O_ASSIGN);
            expression = tryParseExpression();
            modifiers.add(new ModifierItem(name, expression));
        }
        require(TokenType.T_R_CURL_PARENTHESIS);
        return new Modifier(modifiers);
    }

    private InlineFuncCall tryParseInlineFuncCall() throws Exception
    {
        final var name = (String) require(TokenType.T_IDENTIFIER).value();
        final var arguments = new LinkedList<Expression>();
        if (!areInlineArgumentsEnd())
        {
            arguments.add(tryParseUnpipeableExpression());
        }

        while (!areInlineArgumentsEnd())
        {
            require(T_COMMA);
            arguments.add(tryParseUnpipeableExpression());
        }
        return new InlineFuncCall(name, arguments);

    }

    private Expression tryParseUnpipeableExpression() throws Exception
    {
        var result = new Try<>(T_L_QAD_PARENTHESIS, this::parseArrayExpressionWithCheckedFirst)
            .orElseNoConsume(T_WITH, this::tryParseLambdaExpression)
            .get();
        if (result != null)
        {
            return tryEnrichExpressionWithModifier(result);
        }
        Expression expression = null;
        if (currentIdentifierToken == null)
        {
            if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_PLUS)
            {
                consumeToken();
                expression = new PlusUnaryExpression(tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)));
            }
            else if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_MINUS)
            {
                consumeToken();
                expression = new MinusUnaryExpression(tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)));
            }
            else if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_NEGATE)
            {
                consumeToken();
                expression = new NegateExpression(tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR)));
            }
        }
        if (expression == null)
        {
            expression = tryParseExpressionNode(this::tryParseAndExpression, Set.of(OperatorEnum.O_OR));
        }
        return tryEnrichExpressionWithModifier(expression);
    }

    private Expression tryEnrichExpressionWithModifier(Expression expression) throws Exception
    {
        Modifier modifier = null;
        if ((modifier = tryParseModifier()) != null)
        {
            expression = new ModifierExpression(expression, modifier);
        }
        return expression;
    }

    private boolean areInlineArgumentsEnd()
    {
        if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_PIPE)
        {
            return true;
        }
        return (nextToken.type() == T_OPERATOR && (nextToken.value() != OperatorEnum.O_NEGATE && nextToken.value() != OperatorEnum.O_PLUS
            && nextToken.value() != OperatorEnum.O_MINUS))
            || inlineArgumentsEnd.contains(nextToken.type());
    }

    private Expression tryParseAndExpression() throws Exception
    {
        return tryParseExpressionNode(this::tryParseRelExpression, Set.of(OperatorEnum.O_AND));
    }

    private Expression tryParseRelExpression() throws Exception
    {
        return tryParseExpressionNode(this::tryParseAddExpression,
            Set.of(OperatorEnum.O_EQ,
                OperatorEnum.O_GREATER_EQ,
                OperatorEnum.O_LESS,
                OperatorEnum.O_LESS_EQ,
                OperatorEnum.O_GREATER,
                OperatorEnum.O_NEQ));
    }

    private Expression tryParseAddExpression() throws Exception
    {
        return tryParseExpressionNode(this::tryParseMulExpression, Set.of(OperatorEnum.O_PLUS, OperatorEnum.O_SIM, OperatorEnum.O_MINUS));
    }

    private Expression tryParseMulExpression() throws Exception
    {
        return tryParseExpressionNode(this::tryParseHExpression,
            Set.of(OperatorEnum.O_MUL, OperatorEnum.O_AMPER, OperatorEnum.O_DIVIDE, OperatorEnum.O_MOD));
    }

    private Expression tryParseHExpression() throws Exception
    {
        final var term = tryParseExpressionNode(this::tryParseLeaf, Set.of(OperatorEnum.O_ARROW, OperatorEnum.O_POW, OperatorEnum.O_DOUBLE_GR));
        if (nextToken.type() == TokenType.T_AS)
        {
            consumeToken();
            final var type = tryParseSimpleType();
            return new CastExpresion(term, type);
        }
        return term;
    }

    private Expression tryParseLeaf() throws Exception
    {
        if (currentIdentifierToken != null)
        {
            final var name = (String) currentIdentifierToken.value();
            currentIdentifierToken = null;
            return parseVariableOrFuncCall(name);
        }
        if (currentPitchToken != null)
        {
            final var pitch = (String) currentPitchToken.value();
            currentPitchToken = null;
            return new NoteExpression(pitch, null, null);
        }
        if (nextToken.type() == TokenType.T_L_PARENTHESIS)
        {

            final var pitch = tryParseNoteExpression();
            if (pitch != null)
            {
                return pitch;
            }
            final var expresion = tryParseExpression();
            try
            {
                require(TokenType.T_R_PARENTHESIS);

            }
            catch (SyntaxException e)
            {
                throw new SyntaxException(e.getRequiredTokenTypes(), e.getCurrentToken(), "no closing ) parenthesis");
            }
            return expresion;
        }

        if (nextToken.type() == TokenType.T_IDENTIFIER)
        {
            final var name = (String) nextToken.value();
            consumeToken();
            return parseVariableOrFuncCall(name);

        }
        final var value = require(Set.of(
            T_INT_NUMBER, T_FLOATING_NUMBER, T_STRING, T_PITCH, T_RHYTHM, T_TRUE, T_FALSE
        ));
        return switch (value.type())
        {
            case T_INT_NUMBER -> new IntLiteral((int) value.value());
            case T_PITCH -> new NoteExpression((String) value.value(), null, null);
            case T_RHYTHM -> new NoteExpression(null, null, (String) value.value());
            case T_FLOATING_NUMBER -> new FloatLiteral((double) value.value());
            case T_STRING -> new StringLiter((String) value.value());
            case T_TRUE -> new BoolLiteral(true);
            case T_FALSE -> new BoolLiteral(false);
            default -> throw new IllegalStateException();
        };
    }

    private Expression parseVariableOrFuncCall(final String name) throws Exception
    {
        if (nextToken.type() == TokenType.T_L_PARENTHESIS)
        {
            return parseExecutionCall(name);
        }
        return new VariableReference(name);

    }

    private ExecutionCall parseExecutionCall(final String name) throws Exception
    {
        require(TokenType.T_L_PARENTHESIS);
        if (nextToken.type() == TokenType.T_R_PARENTHESIS)
        {
            consumeToken();
            return new FunctionCall(name, List.of());
        }

        ExecutionCall call = new FunctionCall(name, parseArguments());
        while (nextToken.type() == TokenType.T_L_PARENTHESIS)
        {
            consumeToken();
            call = new LambdaCall(call, parseArguments());
        }
        return call;

    }

    private List<Expression> parseArguments() throws Exception
    {
        final var arguments = new LinkedList<Expression>();
        arguments.add(tryParseExpression());
        while (nextToken.type() == TokenType.T_COMMA)
        {
            consumeToken();
            arguments.add(tryParseExpression());
        }
        require(TokenType.T_R_PARENTHESIS);
        return arguments;
    }

    private Expression tryParseExpressionNode(final Callable<Expression> nodeSuplier, final Set<OperatorEnum> operators) throws Exception
    {
        var left = nodeSuplier.call();
        while (nextToken.type() == T_OPERATOR && operators.contains((OperatorEnum) nextToken.value()))
        {
            final var currentOperator = (OperatorEnum) nextToken.value();
            if (!rootSuplier.containsKey(currentOperator))
            {
                final var msg = "L: %d C: %d Token: (%s, %s)".formatted(nextToken.position().line() + 1,
                    nextToken.position().characterNumber(), nextToken.type(), nextToken.value()
                );
                throw new UnsupportedOperationException(msg);
            }
            consumeToken();
            try
            {
                final var right = nodeSuplier.call();
                left = rootSuplier.get(currentOperator).apply(left, right);

            }
            catch (SyntaxException e)
            {
                throw new SyntaxException(e.getRequiredTokenTypes(), e.getCurrentToken(), "right expression expected after binary operator");
            }
        }
        return left;
    }

    private LambdaExpression tryParseLambdaExpression() throws Exception
    {
        if (nextToken.type() == T_WITH)
        {
            consumeToken();
            final var parameters = parseParameters();
            require(OperatorEnum.O_ARROW);
            final var returnType = tryParseType();
            final var block = tryParseBlock();
            return new LambdaExpression(parameters, returnType, block);
        }
        return null;
    }

    private NoteExpression tryParseNoteExpression() throws Exception
    {

        consumeToken();
        if (nextToken.type() == T_PITCH)
        {
            final var pitch = nextToken;
            consumeToken();
            if (nextToken.type() != TokenType.T_COMMA)
            {
                currentPitchToken = pitch;
                return null;
            }
            require(TokenType.T_COMMA);
            final var octave = tryParseExpression();
            require(TokenType.T_R_PARENTHESIS);
            String duration = null;
            if (nextToken.type() == T_RHYTHM)
            {
                duration = (String) nextToken.value();
                consumeToken();
            }
            return new NoteExpression((String) pitch.value(), octave, duration);
        }
        return null;
    }

    private Parameters parseParameters() throws IOException
    {
        require(TokenType.T_L_PARENTHESIS);
        if (nextToken.type() == TokenType.T_R_PARENTHESIS)
        {
            consumeToken();
            return new Parameters(List.of());
        }
        final var parameters = new LinkedList<Parameter>();
        while (nextToken.type() != TokenType.T_R_PARENTHESIS)
        {
            final var parameterType = tryParseType();
            final var parameterName = require(TokenType.T_IDENTIFIER).value();
            parameters.add(new Parameter(parameterType, (String) parameterName));
            if (nextToken.type() == TokenType.T_R_PARENTHESIS)
            {
                break;
            }
            require(TokenType.T_COMMA);
        }
        require(TokenType.T_R_PARENTHESIS);
        return new Parameters(parameters);

    }

    private Statement tryParseAssigment() throws Exception
    {
        final var varName = currentIdentifierToken.value();
        currentIdentifierToken = null;
        final var factory = assignSupplier.get((OperatorEnum) nextToken.value());
        consumeToken();
        try
        {
            return factory.apply((String) varName, tryParseExpression());
        }
        catch (SyntaxException e)
        {
            throw new SyntaxException(Set.of(), nextToken, "no expression provided to assign");
        }

    }

    private Type tryParseType() throws IOException
    {
        if (nextToken.type() == T_LET)
        {
            consumeToken();
            return new SimpleType(Types.LET);
        }
        if (nextToken.type() == TokenType.T_IDENTIFIER)
        {
            return tryParseSimpleType();
        }
        if (nextToken.type() == TokenType.T_L_QAD_PARENTHESIS)
        {
            consumeToken();
            require(TokenType.T_R_QAD_PARENTHESIS);
            return new ArrayType(tryParseType());
        }
        if (nextToken.type() == TokenType.T_LAMBDA)
        {
            consumeToken();
            return parseLambdaType();
        }
        return null;

    }

    private Type parseLambdaType() throws IOException
    {
        require(TokenType.T_L_PARENTHESIS);
        if (nextToken.type() == TokenType.T_R_PARENTHESIS)
        {
            consumeToken();
            require(OperatorEnum.O_ARROW);
            final var returnType = tryParseType();
            return new LambdaType(List.of(), returnType);
        }
        final var types = new LinkedList<Type>();
        types.add(tryParseType());
        while (nextToken.type() == TokenType.T_COMMA)
        {
            require(TokenType.T_COMMA);
            types.add(tryParseType());
        }
        require(TokenType.T_R_PARENTHESIS);
        require(OperatorEnum.O_ARROW);
        final var returnType = tryParseType();
        return new LambdaType(types, returnType);
    }

    private Type tryParseSimpleType() throws IOException
    {
        try
        {
            final var type = Types.valueOf((String) nextToken.value());
            consumeToken();
            return new SimpleType(type);
        }
        catch (final IllegalArgumentException ex)
        {
            return null;
        }

    }

    private Type parseArrayType() throws IOException
    {
        if (nextToken.type() == TokenType.T_IDENTIFIER)
        {
            return new ArrayType(tryParseSimpleType());
        }
        require(TokenType.T_L_QAD_PARENTHESIS);
        require(TokenType.T_R_QAD_PARENTHESIS);
        return new ArrayType(parseArrayType());

    }

    private Statement parseDeclaration(final Type type) throws Exception
    {
        final var varName = (String) require(TokenType.T_IDENTIFIER).value();
        if (nextToken.type() == T_OPERATOR && nextToken.value() == OperatorEnum.O_ASSIGN)
        {
            consumeToken();
            return new Declaration(type, varName, tryParseExpression());
        }
        return new Declaration(type, varName, null);

    }

    private Statement parseForStatemnt() throws Exception
    {
        require(TokenType.T_L_PARENTHESIS);
        final var type = tryParseType();
        final var name = (String) require(TokenType.T_IDENTIFIER).value();
        require(TokenType.T_IN);
        final var iterable = tryParseExpression();
        require(TokenType.T_R_PARENTHESIS);
        final var block = tryParseBlock();
        return new ForStatement(new Declaration(type, name, null), iterable, block);

    }

    private Block tryParseBlock() throws Exception
    {
        require(TokenType.T_L_CURL_PARENTHESIS);
        final var stms = new LinkedList<Statement>();
        while (nextToken.type() != TokenType.T_R_CURL_PARENTHESIS)
        {
            stms.add(parseStatement());
        }
        require(TokenType.T_R_CURL_PARENTHESIS);

        return new Block(stms);
    }

    private IfStatement parseIfStatement() throws Exception
    {
        require(TokenType.T_L_PARENTHESIS);
        final var condition = tryParseExpression();
        require(TokenType.T_R_PARENTHESIS);
        final var block = tryParseBlock();
        IfStatement alternative = null;
        if (nextToken.type() == TokenType.T_ELSE)
        {
            consumeToken();
            if (nextToken.type() == TokenType.T_L_CURL_PARENTHESIS)
            {
                final var elseBlock = tryParseBlock();
                alternative = new IfStatement(null, elseBlock);
            }
            else
            {
                require(TokenType.T_IF);
                alternative = parseIfStatement();
            }
        }
        return new IfStatement(condition, block, alternative);

    }

    private Token require(final Set<TokenType> requiredTokenTypes, final Set<Object> requiredValue) throws IOException
    {
        if (nextToken != null && requiredTokenTypes.contains(nextToken.type()))
        {
            final var currentToken = nextToken;
            if (requiredValue == null)
            {
                consumeToken();
                return currentToken;
            }
            if (requiredValue.contains(currentToken.value()))
            {
                consumeToken();
                return currentToken;
            }
        }
        throw new SyntaxException(requiredTokenTypes, nextToken);
    }

    private void require(OperatorEnum requiredValue) throws IOException
    {
        require(Set.of(T_OPERATOR), Set.of(requiredValue));
    }

    private Token require(final TokenType requiredTokenTypes) throws IOException
    {
        return require(Set.of(requiredTokenTypes), null);
    }

    private Token require(final Set<TokenType> requiredTokenTypes) throws IOException
    {
        return require(requiredTokenTypes, null);
    }

    private class Try<T>
    {
        private T result;

        public Try(final TokenType intitToken, final Callable<T> initSupplier) throws Exception
        {
            this.result = tryCall(Set.of(intitToken), initSupplier);
        }

        private T tryCall(Set<TokenType> tokens, final Callable<T> initSupplier, boolean consume) throws Exception
        {
            if (tokens.contains(nextToken.type()))
            {
                if (consume)
                {
                    consumeToken();
                }
                return initSupplier.call();
            }
            return null;
        }

        private T tryCall(Set<TokenType> tokens, final Callable<T> initSupplier) throws Exception
        {
            return tryCall(tokens, initSupplier, true);
        }

        public Try<T> orElse(TokenType tokens, Callable<T> supplier) throws Exception
        {
            return orElse(Set.of(tokens), supplier);
        }

        public Try<T> orElseNoConsume(TokenType tokens, Callable<T> supplier) throws Exception
        {
            return orElseNoConsume(Set.of(tokens), supplier);
        }

        public Try<T> orElse(Set<TokenType> tokens, Callable<T> supplier) throws Exception
        {
            if (result == null)
            {
                this.result = tryCall(tokens, supplier);

            }
            return this;
        }

        public Try<T> orElseNoConsume(Set<TokenType> tokens, Callable<T> supplier) throws Exception
        {
            if (result == null)
            {
                this.result = tryCall(tokens, supplier, false);

            }
            return this;
        }

        public T get()
        {
            return result;
        }
    }
}
