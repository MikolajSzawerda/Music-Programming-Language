package com.declarative.music.interpreter.tree;

import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.Pitch;
import com.declarative.music.interpreter.values.music.Rythm;
import com.declarative.music.interpreter.values.template.IndexTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class TreeTest {

    private IndexTree tested;

    @BeforeEach
    void init() {
        tested = new IndexTree();
    }

    @Test
    void shouldCreateSequenceTree() {
        // given
        var expectedRoot = new SequenceNode<Integer>();
        expectedRoot.nodes.addAll(List.of(
                new SimpleNode<>(1),
                new SimpleNode<>(2),
                new SimpleNode<>(3)
        ));

        // when
        tested
                .appendToSequence(new SimpleNode<>(1))
                .appendToSequence(new SimpleNode<>(2))
                .appendToSequence(new SimpleNode<>(3));

        assertThat(tested.getRoot()).isEqualToComparingFieldByFieldRecursively(expectedRoot);
    }

    @Test
    void shouldCreateNestedTrees() {
        // given
        var expectedRoot = new SequenceNode<Integer>();
        expectedRoot.nodes.addAll(List.of(
                new SimpleNode<>(1),
                new SimpleNode<>(2),
                new SimpleNode<>(3)
        ));

        // when
        tested
                .appendToSequence(new SimpleNode<>(1))
                .appendToSequence(new SimpleNode<>(2))
                .appendToSequence(new SimpleNode<>(3));

        assertThat(tested.getRoot()).isEqualToComparingFieldByFieldRecursively(expectedRoot);
    }

    @Test
    void shouldTransformTree() {
        // given
        tested
                .appendToSequence(1)
                .appendToGroup(new GroupNode<>(List.of(
                        new SimpleNode<>(0),
                        new SimpleNode<>(1)
                )))
                .appendToSequence(2);
        var nodeSupplier = List.of(
                new Note(Pitch.C, 4, Rythm.q),
                new Note(Pitch.E, 4, Rythm.q),
                new Note(Pitch.G, 4, Rythm.q)
        );
        var expectedRoot = new SequenceNode<>();
        expectedRoot.nodes.addAll(List.of(
                new GroupNode<>(
                        List.of(
                                new SimpleNode<>(new Note(Pitch.E, 4, Rythm.q)),
                                new SimpleNode<>(new Note(Pitch.C, 4, Rythm.q)),
                                new SimpleNode<>(new Note(Pitch.E, 4, Rythm.q))
                        )),
                new SimpleNode<>(new Note(Pitch.G, 4, Rythm.q)))
        );

        // when
        var transformed = tested.map((node) -> new SimpleNode<>(nodeSupplier.get(node.value)));

        assertThat(transformed).isEqualToComparingFieldByFieldRecursively(expectedRoot);
    }

    @Test
    void shouldCreateGroupTree() {
        // given
        var expectedRoot = new GroupNode<>();
        expectedRoot.nodes.addAll(List.of(
                new SimpleNode<>(1),
                new SimpleNode<>(2),
                new SimpleNode<>(3)
        ));

        // when
        tested
                .appendToGroup(new SimpleNode<>(1))
                .appendToGroup(new SimpleNode<>(2))
                .appendToGroup(new SimpleNode<>(3));

        assertThat(tested.getRoot()).isEqualToComparingFieldByFieldRecursively(expectedRoot);
    }

    @Test
    void shouldAppendGroupTree() {
        // given
        var expectedRoot = new GroupNode<>();
        expectedRoot.nodes.addAll(List.of(
                new SimpleNode<>(1),
                new SimpleNode<>(2),
                new SimpleNode<>(3)
        ));

        // when
        tested
                .appendToGroup(1)
                .appendToGroup(new GroupNode<>(List.of(
                        new SimpleNode<>(2),
                        new SimpleNode<>(3)
                )));

        assertThat(tested.getRoot()).isEqualToComparingFieldByFieldRecursively(expectedRoot);
    }

    @Test
    void shouldAppendSequenceTree() {
        // given
        var expectedRoot = new SequenceNode<>();
        expectedRoot.nodes.addAll(List.of(
                new SimpleNode<>(1),
                new SimpleNode<>(2),
                new SimpleNode<>(3)
        ));

        // when
        tested
                .appendToSequence(1)
                .appendToSequence(new SequenceNode<>(List.of(
                        new SimpleNode<>(2),
                        new SimpleNode<>(3)
                )));

        assertThat(tested.getRoot()).isEqualToComparingFieldByFieldRecursively(expectedRoot);
    }

    @Test
    void shouldCreateGroupTree_FromSequence() {
        // given
        // (1 | 2) & 3
        var expectedRoot = new GroupNode<>();
        expectedRoot.nodes.addAll(List.of(
                new SequenceNode<>(List.of(
                        new SimpleNode<>(1),
                        new SimpleNode<>(2)
                )),
                new SimpleNode<>(3)
        ));

        // when
        tested
                .appendToSequence(1)
                .appendToSequence(2)
                .appendToGroup(3);

        assertThat(tested.getRoot()).isEqualToComparingFieldByFieldRecursively(expectedRoot);
    }

    @Test
    void shouldCreateSequenceTree_FromGroup() {
        // given
        // (1 & 2) | 3
        var expectedRoot = new SequenceNode<>();
        expectedRoot.nodes.addAll(List.of(
                new GroupNode<>(List.of(
                        new SimpleNode<>(1),
                        new SimpleNode<>(2)
                )),
                new SimpleNode<>(3)
        ));

        // when
        tested
                .appendToGroup(1)
                .appendToGroup(2)
                .appendToSequence(3);

        assertThat(tested.getRoot()).isEqualToComparingFieldByFieldRecursively(expectedRoot);
    }

    @Test
    void shouldCreateCompoundTree() {
        // given
        // 1 | 2 & 3 | 4 | 5 | 6 | 7 | 8 & 9
        var expectedRoot = new SequenceNode<>();
        expectedRoot.nodes.addAll(List.of(
                new SimpleNode<>(1),
                new GroupNode<>(List.of(
                        new SimpleNode<>(2),
                        new SimpleNode<>(3)
                )),
                new SimpleNode<>(4),
                new SimpleNode<>(5),
                new SimpleNode<>(6),
                new SimpleNode<>(7),
                new GroupNode<>(List.of(
                        new SimpleNode<>(8),
                        new SimpleNode<>(9)
                ))
        ));

        // when
        tested.appendToSequence(1);
        var groupTree = new IndexTree();
        groupTree.appendToGroup(2);
        groupTree.appendToGroup(3);
        tested.appendToSequence(groupTree);
        var sequenceTree = new IndexTree();
        sequenceTree.appendToSequence(4);
        sequenceTree.appendToSequence(5);
        tested.appendToSequence(sequenceTree);
        tested.appendToSequence(new SequenceNode<>(List.of(
                new SimpleNode<>(6),
                new SimpleNode<>(7)
        )));
        tested.appendToSequence(new GroupNode<>(List.of(
                new SimpleNode<>(8),
                new SimpleNode<>(9)
        )));

        assertThat(tested.getRoot()).isEqualToComparingFieldByFieldRecursively(expectedRoot);
    }

}