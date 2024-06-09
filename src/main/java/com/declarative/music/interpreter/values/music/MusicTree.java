package com.declarative.music.interpreter.values.music;

import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.tree.SimpleNode;
import com.declarative.music.interpreter.tree.TreeNode;

public class MusicTree extends TreeNode<Note, MusicTree> {
    public MusicTree(Node<Note> root) {
        super(root);
    }

    public MusicTree() {

    }

    public MusicTree copy() {
        var newRoot = super.map((node) -> new SimpleNode<>(node.getValue()));
        return new MusicTree(newRoot);
    }
}
