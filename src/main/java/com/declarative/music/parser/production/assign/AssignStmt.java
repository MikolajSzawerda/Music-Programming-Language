package com.declarative.music.parser.production.assign;

import com.declarative.music.parser.production.Statement;
import com.declarative.music.parser.production.expression.Expression;

public interface AssignStmt extends Statement {
    String identifier();

    Expression value();
}
