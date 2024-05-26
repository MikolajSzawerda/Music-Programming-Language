package com.declarative.music.interpreter;

import com.declarative.music.parser.production.expression.lambda.LambdaExpression;


public record LambdaClousure(LambdaExpression expression, Frame frame)
{
}
