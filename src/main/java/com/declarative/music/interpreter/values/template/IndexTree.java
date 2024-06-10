package com.declarative.music.interpreter.values.template;

import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.tree.SimpleNode;
import com.declarative.music.interpreter.tree.TreeNode;

public class IndexTree extends TreeNode<Integer, IndexTree> {
    public IndexTree(Node<Integer> root) {
        super(root);
    }

    public IndexTree() {

    }

    public IndexTree copy() {
        var newRoot = super.map((node) -> new SimpleNode<>(node.getValue()));
        return new IndexTree(newRoot);
    }
}
