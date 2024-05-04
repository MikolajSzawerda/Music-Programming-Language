package com.declarative.music.parser.production.type;

import java.util.List;

public record LambdaType(List<Type> parameter, Type returnType) implements Type {
}
