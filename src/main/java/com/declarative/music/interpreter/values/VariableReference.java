package com.declarative.music.interpreter.values;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class VariableReference<T>
{
    private T value;

}
