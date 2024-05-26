package com.declarative.music.interpreter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.declarative.music.interpreter.values.VariableReference;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ValueFrame
{
    private final Map<String, VariableReference> variables;
    private final Map<Integer, List<String>> scopes;
    private int currentScope = 0;

    public ValueFrame(final Map<String, VariableReference> variables)
    {
        this.variables = variables;
        this.scopes = new HashMap<>();
        enterScope();
        variables.forEach((k, v) -> {
            scopes.get(currentScope).add(k);
        });
    }

    public ValueFrame()
    {
        variables = new HashMap<>();
        this.scopes = new HashMap<>();
        enterScope();
    }

    public Optional<VariableReference> getValue(String name)
    {
        return Optional.ofNullable(variables.get(name));
    }

    public ValueFrame copy()
    {
        var newScope = new HashMap<Integer, List<String>>();
        scopes.forEach((k, v) -> newScope.put(k, new LinkedList<>(v)));
        var f = new ValueFrame(
            new HashMap<>(variables),
            newScope
        );
        f.currentScope = Collections.max(scopes.keySet());
        return f;
    }

    public void saveValue(String name, VariableReference value)
    {
        getValue(name).ifPresent(val -> {
            if (val.getClass() != value.getClass())
            {
                throw new RuntimeException("INTERPRETATION ERROR");
            }
        });
        variables.put(name, value);
        scopes.get(currentScope).add(name);
    }

    public void enterScope()
    {
        currentScope++;
        scopes.put(currentScope, new LinkedList<>());
    }

    public void leaveScope()
    {
        if (currentScope == 1)
        {
            throw new IllegalStateException("You are trying to leave scope that was not started");
        }
        scopes.get(currentScope).forEach(variables::remove);
        scopes.remove(currentScope);
        currentScope--;
    }

    public boolean contains(String name)
    {
        return variables.containsKey(name);
    }

    public boolean scopeContains(String name)
    {
        return scopes.get(currentScope).contains(name);
    }

}
