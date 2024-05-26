package com.declarative.music.interpreter;

import java.util.Optional;
import java.util.Stack;

import com.declarative.music.interpreter.values.IntReference;
import com.declarative.music.interpreter.values.LambdaReference;
import com.declarative.music.interpreter.values.Reference;
import com.declarative.music.interpreter.values.StringReference;

import lombok.Getter;


@Getter
public class ContextManager
{
    private final Frame globalFrame;
    private final Stack<Frame> frames = new Stack<>();

    public ContextManager(final Frame globalFrame)
    {
        this.globalFrame = globalFrame;
    }

    public ContextManager()
    {
        globalFrame = new Frame();
    }

    public void save(String name, Object value)
    {
        var frame = frames.empty() ? globalFrame : frames.peek();
        if (frame.contains(name) && frame.getValue(name).isPresent())
        {
            frame.getValue(name).ifPresent(ref -> {
                if (ref.getValue().getClass() != value.getClass())
                {
                    throw new RuntimeException("INTERPRETATION ERROR");
                }
                ref.setValue(value);
            });
            return;
        }
        if (value == null)
        {
            frame.saveValue(name, null);
            return;
        }
        if (value.getClass() == Integer.class)
        {
            frame.saveValue(name, new IntReference(name, (int) value));
        }
        else if (value.getClass() == String.class)
        {
            frame.saveValue(name, new StringReference(name, (String) value));
        }
        else if (value.getClass() == LambdaClousure.class)
        {
            frame.saveValue(name, new LambdaReference((LambdaClousure) value));
        }
        else if (value.getClass() == IntReference.class)
        {
            frame.saveValue(name, (IntReference) value);
        }
        else
        {
            throw new RuntimeException("INTERPRETATION ERROR unknown value");
        }
    }

    void declare(String name, Object value)
    {
        var frame = frames.empty() ? globalFrame : frames.peek();
        if (value == null)
        {
            frame.saveValue(name, null);
            return;
        }
        if (value.getClass() == Integer.class)
        {
            frame.saveValue(name, new IntReference(name, (int) value));
        }
        else if (value.getClass() == String.class)
        {
            frame.saveValue(name, new StringReference(name, (String) value));
        }
        else if (value.getClass() == LambdaClousure.class)
        {
            frame.saveValue(name, new LambdaReference((LambdaClousure) value));
        }
        else if (value.getClass() == IntReference.class)
        {
            frame.saveValue(name, (IntReference) value);
        }
        else
        {
            throw new RuntimeException("INTERPRETATION ERROR unknown value");
        }
    }

    public Optional<Reference> get(String name)
    {
        if (frames.empty())
        {
            return globalFrame.getValue(name);
        }
        return frames
            .peek()
            .getValue(name)
            .or(() -> globalFrame.getValue(name));

    }

    public void enterNewFrame()
    {
        frames.push(new Frame());
    }

    public void enterNewFrame(Frame frame)
    {
        frames.push(frame);

    }

    public void leaveFrame()
    {
        frames.pop();
    }

    public void startNewScope()
    {
        if (frames.empty())
        {
            globalFrame.enterScope();
            return;
        }
        frames.peek().enterScope();
    }

    public void leaveNewScope()
    {
        if (frames.empty())
        {
            globalFrame.leaveScope();
            return;
        }
        frames.peek().leaveScope();
    }

    public boolean contains(String name)
    {
        if (frames.empty())
        {
            return globalFrame.contains(name);
        }
        return frames.peek().contains(name) ||
            globalFrame.contains(name);
    }
}
