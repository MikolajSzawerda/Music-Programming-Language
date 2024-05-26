package com.declarative.music.interpreter;

import com.declarative.music.parser.production.expression.lambda.LambdaExpression;


public record ValueLambdaClousure(LambdaExpression expression, ValueFrame frame)
{
}
