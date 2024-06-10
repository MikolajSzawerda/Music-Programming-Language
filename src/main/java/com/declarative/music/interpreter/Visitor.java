package com.declarative.music.interpreter;

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

    void visit(SimpleType simpleType);

    void visit(LambdaType lambdaType);

    void visit(InferenceType inferenceType);

    void visit(ArrayType arrayType);
}
