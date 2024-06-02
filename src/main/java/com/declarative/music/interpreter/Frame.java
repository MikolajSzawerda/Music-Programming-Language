package com.declarative.music.interpreter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.declarative.music.interpreter.values.VariableReference;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Frame
{
    private final List<Map<String, VariableReference>> scopes;

    public Frame(final Map<String, VariableReference> variables)
    {
        scopes = new LinkedList<>();
        scopes.addFirst(new HashMap<>(variables));
    }

    public Frame()
    {
        this.scopes = new LinkedList<>();
        enterScope();
    }

    public Optional<VariableReference> getValue(String name)
    {
        return scopes.stream()
            .filter(scope -> scope.containsKey(name))
            .filter(scope -> scope.get(name) != null)
            .map(scope -> scope.get(name))
            .findFirst();
    }

    public Frame copy()
    {
        var newScope = new LinkedList<Map<String, VariableReference>>();
        scopes.forEach(scope -> newScope.add(new HashMap<>(scope)));
        return new Frame(newScope);
    }

    public void saveValue(String name, VariableReference value)
    {
        getValue(name).ifPresent(val -> {
            if (val.getClass() != value.getClass())
            {
                throw new RuntimeException("INTERPRETATION ERROR required %s provided %s"
                    .formatted(val.getClass().getSimpleName(), value.getClass().getSimpleName()));
            }
        });
        scopes.getFirst().put(name, value);
    }

    public void enterScope()
    {
        scopes.addFirst(new HashMap<>());
    }

    public void leaveScope()
    {
        if (scopes.isEmpty())
        {
            throw new IllegalStateException("You are trying to leave scope that was not started");
        }
        scopes.removeFirst();
    }

    public boolean contains(String name)
    {
        return scopes.stream()
            .anyMatch(scope -> scope.containsKey(name));
    }

    public boolean scopeContains(String name)
    {
        return scopes.getFirst().containsKey(name);
    }

}
