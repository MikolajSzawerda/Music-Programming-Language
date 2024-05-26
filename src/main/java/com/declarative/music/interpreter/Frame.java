package com.declarative.music.interpreter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.declarative.music.interpreter.values.Reference;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Frame
{
    private final Map<String, Reference> variables;
    private final Map<Integer, List<String>> scopes;
    private int currentScope = 0;

    public Frame(final Map<String, Reference> variables)
    {
        this.variables = variables;
        this.scopes = new HashMap<>();
        enterScope();
        variables.forEach((k, v) -> {
            scopes.get(currentScope).add(k);
        });
    }

    public Frame()
    {
        variables = new HashMap<>();
        this.scopes = new HashMap<>();
        enterScope();
    }

    public Optional<Reference> getValue(String name)
    {
        return Optional.ofNullable(variables.get(name));
    }

    public Frame copy()
    {
        var f = new Frame(
            new HashMap<>(variables),
            new HashMap<>(scopes)
        );
        f.currentScope = Collections.max(scopes.keySet());
        return f;
    }

    public void saveValue(String name, Reference value)
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

    private record Scope(int scopeId, Reference value)
    {

    }

}
