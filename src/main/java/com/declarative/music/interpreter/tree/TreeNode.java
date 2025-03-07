package com.declarative.music.interpreter.tree;

import com.declarative.music.interpreter.tree.modifier.ModifierVisitor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Getter
@NoArgsConstructor
public class TreeNode<T, V extends TreeNode<T, V>> implements Node<T> {
    private Node<T> root;
    private ModifierVisitor<T> modifier;

    private V self() {
        return (V) this;
    }

    public TreeNode(Node<T> root) {
        this.root = root;
    }

    public V appendToGroup(Node<T> node) {
        var nodeValue = node instanceof TreeNode<?, ?> ? ((TreeNode<T, V>) node).getRoot() : node;
        if (root == null) {
            root = nodeValue;
            return self();
        }
        if (root instanceof GroupNode<T> groupRoot) {
            groupRoot.nodes.add(nodeValue);
            return self();
        }
        if (root instanceof SequenceNode<T> || root instanceof SimpleNode<T>) {
            var newRoot = new GroupNode<T>();
            newRoot.nodes.add(root);
            if (nodeValue instanceof GroupNode<T> groupNode) {
                newRoot.nodes.addAll(groupNode.getChildren());
            } else {
                newRoot.nodes.add(nodeValue);
            }
            root = newRoot;
            return self();
        }
        throw new UnsupportedOperationException("Unkown node type");
    }

    public V appendToGroup(T value) {
        appendToGroup(new SimpleNode<>(value));
        return self();
    }

    public V appendToSequence(Node<T> node) {
        var nodeValue = node instanceof TreeNode<?, ?> ? ((TreeNode<T, V>) node).getRoot() : node;
        if (root == null) {
            root = nodeValue;
            return self();
        }
        if (root instanceof GroupNode<T> || root instanceof SimpleNode<T>) {
            var newRoot = new SequenceNode<T>();
            newRoot.nodes.add(root);
            if (nodeValue instanceof SequenceNode<T>) {
                newRoot.nodes.addAll(nodeValue.getChildren());
            } else {
                newRoot.nodes.add(nodeValue);

            }
            root = newRoot;
            return self();
        }
        if (root instanceof SequenceNode<T> rootNode) {
            if (nodeValue instanceof SequenceNode<T>) {
                rootNode.nodes.addAll(nodeValue.getChildren());
            } else {
                rootNode.nodes.add(nodeValue);
            }
            return self();
        }
        throw new UnsupportedOperationException("Unkown node type");
    }

    public <R> Node<R> map(Function<SimpleNode<T>, SimpleNode<R>> leafMapper) {
        return traverseNode(root, leafMapper);
    }

    private <R> Node<R> traverseNode(Node<T> node, Function<SimpleNode<T>, SimpleNode<R>> leafMapper) {
        if (node instanceof SimpleNode<T> simpleNode) {
            return leafMapper.apply(simpleNode);
        }
        if (node instanceof SequenceNode<T> sequenceNode) {
            var sequence = new SequenceNode<R>();
            for (var child : sequenceNode.getChildren()) {
                sequence.nodes.add(traverseNode(child, leafMapper));
            }
            return sequence;
        }
        if (node instanceof GroupNode<T> groupNode) {
            var group = new GroupNode<R>();
            for (var child : groupNode.getChildren()) {
                group.nodes.add(traverseNode(child, leafMapper));
            }
            return group;
        }
        throw new UnsupportedOperationException("Unknown node during traversal");

    }

    public V appendToSequence(T value) {
        appendToSequence(new SimpleNode<>(value));
        return self();
    }

    @Override
    public List<Node<T>> getChildren() {
        return null;
    }

    @Override
    public ModifierVisitor<T> modifier() {
        return modifier;
    }

    @Override
    public void setModifier(ModifierVisitor<T> visitor) {
        this.modifier = visitor;
        root.setModifier(visitor);
    }

    @Override
    public TreeNode<T, V> getModified() {
        if (modifier() == null) {
            return this;
        }
        return this.accept(modifier());
    }

    @Override
    public TreeNode<T, V> accept(ModifierVisitor<T> visitor) {
        return new TreeNode<T, V>(root.accept(visitor));
    }
}
