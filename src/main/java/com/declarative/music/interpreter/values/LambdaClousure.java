package com.declarative.music.interpreter.values;

import com.declarative.music.interpreter.Frame;
import com.declarative.music.parser.production.expression.lambda.LambdaExpression;


public record LambdaClousure(LambdaExpression expression, Frame frame)
{
}
