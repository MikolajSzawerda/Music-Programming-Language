package com.declarative.music.interpreter;

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
import com.declarative.music.parser.production.expression.pipe.InlineFuncCall;
import com.declarative.music.parser.production.expression.pipe.PipeExpression;
import com.declarative.music.parser.production.expression.relation.AndExpression;
import com.declarative.music.parser.production.expression.relation.EqExpression;
import com.declarative.music.parser.production.expression.relation.OrExpression;
import com.declarative.music.parser.production.literal.IntLiteral;


public interface Visitor
{

    void visit(AddExpression addExpression);

    void visit(LambdaExpression lambdaExpression);

    void visit(AssigmentStatement assigmentStatement);

    void visit(Block block);

    void visit(Declaration declaration);

    void visit(IfStatement ifStatement);

    void visit(Program program);

    void visit(MinusUnaryExpression minusUnaryExpression);

    void visit(MulExpression mulExpression);

    void visit(PlusUnaryExpression plusUnaryExpression);

    void visit(ArrayExpression arrayExpression);

    void visit(ListComprehension listComprehension);

    void visit(RangeExpression rangeExpression);

    void visit(FunctionCall functionCall);

    void visit(ModifierExpression modifierExpression);

    void visit(ConvolutionExpression convolutionExpression);

    void visit(NoteExpression noteExpression);

    void visit(InlineFuncCall inlineFuncCall);

    void visit(PipeExpression pipeExpression);

    void visit(AndExpression andExpression);

    void visit(EqExpression eqExpression);

    void visit(OrExpression orExpression);

    void visit(CastExpresion castExpresion);

    void visit(VariableReference variableReference);

    void visit(IntLiteral intLiteral);
}
