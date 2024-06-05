package com.declarative.music.interpreter.values.music;

import java.util.List;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.tree.NodeAppenderVisitor;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.tree.SimpleNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Note implements Node<Note>, NodeAppenderVisitor<Note>
{
    private Pitch pitch;
    private int octave;
    private Rythm duration;

    @Override
    public List<Node<Note>> getSiblings()
    {
        throw new UnsupportedOperationException("This is simple node!");
    }

    @Override
    public List<Node<Note>> getChildren()
    {
        throw new UnsupportedOperationException("This is simple node!");
    }

    @Override
    public void accept(final NodeAppenderVisitor<Note> visitor)
    {
        visitor.visit(new SimpleNode<>(this));
    }

    @Override
    public void visit(final SequenceNode<Note> node)
    {
        node.nodes.add(this);
    }

    @Override
    public void visit(final GroupNode<Note> node)
    {
        node.nodes.add(this);
    }

    @Override
    public void visit(final SimpleNode<Note> noteSimpleNode)
    {

    }
}
