package com.declarative.music.interpreter.values;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class VariableReference<T>
{
    private T value;

}
