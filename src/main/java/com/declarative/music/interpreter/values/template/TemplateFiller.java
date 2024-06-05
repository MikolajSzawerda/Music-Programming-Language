package com.declarative.music.interpreter.values.template;

import java.util.List;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.interpreter.values.music.Chord;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.NoteNode;
import com.declarative.music.interpreter.values.music.Phrase;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TemplateFiller
{
    private final List<Variant<Note>> musicList;

    public Node<Note> applyTemplate(Node<Integer> templateTree)
    {
        return traverse(templateTree);
    }

    private Node<Note> traverse(Node<Integer> node)
    {
        if (node instanceof IndexNode)
        {
            Integer idx = ((IndexNode) node).getValue();
            return new NoteNode(musicList.get(idx).value());
        }
        if (node instanceof SequenceNode<Integer>)
        {
            var phrase = new Phrase();
            for (var child : node.getChildren())
            {
                phrase.accept(traverse(child));
            }
            return phrase;
        }
        if (node instanceof GroupNode<Integer>)
        {
            var chord = new Chord();
            for (var sibling : node.getSiblings())
            {
                chord.accept(traverse(sibling));
            }
            return chord;
        }
        throw new UnsupportedOperationException("Unknown tree node");

    }

}
