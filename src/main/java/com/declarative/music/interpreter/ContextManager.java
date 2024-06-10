package com.declarative.music.interpreter;

import com.declarative.music.interpreter.values.VariableReference;
import com.declarative.music.interpreter.values.Variant;
import lombok.Getter;

import java.util.Optional;
import java.util.Stack;


@Getter
public class ContextManager {
    private final Frame globalFrame;
    private final Stack<Frame> frames = new Stack<>();

    public ContextManager(final Frame globalFrame) {
        this.globalFrame = globalFrame;
    }

    public ContextManager() {
        globalFrame = new Frame();
    }

    public void upsert(String name, Variant<?> value) {
        var frame = frames.empty() ? globalFrame : frames.peek();
        frame.getValue(name).filter(ref -> ref.getValue() != null).ifPresentOrElse(ref -> {
            if (ref.getValue().getClass() != value.valueType()) {
                throw new RuntimeException("INTERPRETATION ERROR required %s provided %s"
                        .formatted(ref.getValue().getClass().getSimpleName(), value.valueType().getSimpleName()));
            }
            ref.setValue(value.value());
        }, () -> frame.saveValue(name, new VariableReference<>(value.value())));
    }

    void insert(String name, Variant<?> value) {
        var frame = frames.empty() ? globalFrame : frames.peek();
        if (frame.scopeContains(name)) {
            throw new IllegalArgumentException("Variable named: %s is already present in frame!".formatted(name));
        }
        if (value == null) {
            frame.saveValue(name, null);
            return;
        }
        if (value.type() == VariableReference.class) {
            frame.saveValue(name, value.castTo(VariableReference.class));
            return;
        }
        frame.saveValue(name, new VariableReference<>(value.value()));
    }

    public Optional<VariableReference> get(String name) {
        if (frames.empty()) {
            return globalFrame.getValue(name);
        }
        return frames
                .peek()
                .getValue(name)
                .or(() -> globalFrame.getValue(name));

    }

    public void enterNewFrame() {
        frames.push(new Frame());
    }

    public void enterNewFrame(Frame frame) {
        frames.push(frame);

    }

    public void leaveFrame() {
        frames.pop();
    }

    public void startNewScope() {
        if (frames.empty()) {
            globalFrame.enterScope();
            return;
        }
        frames.peek().enterScope();
    }

    public void leaveNewScope() {
        if (frames.empty()) {
            globalFrame.leaveScope();
            return;
        }
        frames.peek().leaveScope();
    }

    public boolean contains(String name) {
        if (frames.empty()) {
            return globalFrame.contains(name);
        }
        return frames.peek().contains(name) ||
                globalFrame.contains(name);
    }
}
