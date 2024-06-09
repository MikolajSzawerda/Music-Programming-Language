package com.declarative.music.interpreter.values.template;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.tree.SimpleNode;
import com.declarative.music.interpreter.values.Variant;
import com.declarative.music.interpreter.values.music.MusicTree;
import com.declarative.music.interpreter.values.music.Note;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class TemplateFiller {
    private final List<Variant<Node<Note>>> musicList;

    public MusicTree applyTemplate(IndexTree templateTree) {
        return new MusicTree(traverse(templateTree.getRoot()));
    }

    private Node<Note> traverse(Node<Integer> node) {
        if (node instanceof SimpleNode<Integer> simpleNode) {
            return musicList.get(simpleNode.getValue()).value();
        }
        if (node instanceof SequenceNode<Integer>) {
            var sequence = new MusicTree();
            for (var item : node.getChildren()) {
                sequence.appendToSequence(traverse(item));
            }
//            node.getChildren()
//                    .stream().map(this::traverse)
//                    .forEach(sequence::appendToSequence);
            return sequence;
        }
        if (node instanceof GroupNode<Integer>) {
            var group = new MusicTree();
            node.getSiblings()
                    .stream().map(this::traverse)
                    .forEach(group::appendToGroup);
            return group;
        }
        throw new UnsupportedOperationException("Unknown tree node");

    }

}
