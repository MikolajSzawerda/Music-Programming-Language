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


public interface Visitor {

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

    void visit(SequenceExpression sequenceExpression);

    void visit(ParallerExpression parallerExpression);

    void visit(FloatLiteral floatLiteral);

    void visit(DivAssignStatement divAssignStatement);

    void visit(MinusAssignStatement minusAssignStatement);

    void visit(ModuloAssignStatement moduloAssignStatement);

    void visit(MulAssignStatement mulAssignStatement);

    void visit(ParalerAssignStatement paralerAssignStatement);

    void visit(PlusAssignStatement plusAssignStatement);

    void visit(PowAssignStatement powAssignStatement);

    void visit(SequenceAssignStatement sequenceAssignStatement);

    void visit(DivExpression divExpression);

    void visit(MinusExpression minusExpression);

    void visit(ModuloExpression moduloExpression);

    void visit(PowExpression powExpression);

    void visit(LambdaCall lambdaCall);

    void visit(GreaterEqExpression greaterEqExpression);

    void visit(GreaterExpression greaterExpression);

    void visit(LessEqExpression lessEqExpression);

    void visit(LessExpression lessExpression);

    void visit(NegateExpression negateExpression);

    void visit(NotEqExpression notEqExpression);

    void visit(BoolLiteral boolLiteral);

    void visit(StringLiter stringLiter);

    void visit(ForStatement forStatement);

    void visit(ReturnStatement returnStatement);
}
