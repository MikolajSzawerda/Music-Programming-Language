package com.declarative.music.lexer.terminals;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OperatorMap {
    private static final Map<String, OperatorEnum> operators = Map.ofEntries(
            Map.entry(">", OperatorEnum.O_GREATER),
            Map.entry(">=", OperatorEnum.O_GREATER_EQ),
            Map.entry(">>", OperatorEnum.O_DOUBLE_GR),
            Map.entry("<", OperatorEnum.O_LESS),
            Map.entry("<=", OperatorEnum.O_LESS_EQ),
            Map.entry("<|", OperatorEnum.O_LIST_COMPR),
            Map.entry("->", OperatorEnum.O_ARROW),
            Map.entry("-=", OperatorEnum.O_MINUS_ASSIGN),
            Map.entry("=", OperatorEnum.O_ASSIGN),
            Map.entry("==", OperatorEnum.O_EQ),
            Map.entry("&", OperatorEnum.O_AMPER),
            Map.entry("&&", OperatorEnum.O_AND),
            Map.entry("&=", OperatorEnum.O_AMPER_ASSIGN),
            Map.entry("|>", OperatorEnum.O_PIPE),
            Map.entry("||", OperatorEnum.O_OR),
            Map.entry("*", OperatorEnum.O_MUL),
            Map.entry("*=", OperatorEnum.O_MUL_ASSIGN),
            Map.entry("^", OperatorEnum.O_POW),
            Map.entry("^=", OperatorEnum.O_POW_ASSIGN),
            Map.entry("%", OperatorEnum.O_MOD),
            Map.entry("%=", OperatorEnum.O_MOD_ASSIGN),
            Map.entry("!=", OperatorEnum.O_NEQ),
            Map.entry("+", OperatorEnum.O_PLUS),
            Map.entry("-", OperatorEnum.O_MINUS),
            Map.entry("/=", OperatorEnum.O_DIVIDE_ASSIGN),
            Map.entry("|", OperatorEnum.O_SIM),
            Map.entry("|=", OperatorEnum.O_SIM_ASSIGN),
            Map.entry("+=", OperatorEnum.O_PLUS_ASSIGN));

    public static Optional<OperatorEnum> getOperator(final String operator) {
        return Optional.ofNullable(operators.get(operator));
    }
}
