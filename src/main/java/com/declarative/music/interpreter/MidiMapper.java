package com.declarative.music.interpreter;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.tree.SimpleNode;
import com.declarative.music.interpreter.values.music.MusicTree;
import com.declarative.music.interpreter.values.music.Note;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MidiMapper {
    public static Map<Integer, List<Note>> mapToEventStamps(MusicTree musicTree) {
        TreeMap<Integer, List<Note>> orderedMap = new TreeMap<>();
        traverseTree(musicTree.getRoot(), orderedMap, 0);
        return orderedMap;
    }

    private static int traverseTree(Node<Note> node, TreeMap<Integer, List<Note>> map, int startTime) {
        if (node instanceof final SimpleNode<Note> noteNode) {
            var note = enrichNoteWithModifier(noteNode);
            map.computeIfAbsent(startTime, k -> new ArrayList<>()).add(note);
            return calcDuration(note);
        }
        if (node instanceof GroupNode<Note>) {
            int maxDur = 0;
            for (Node<Note> sibling : node.getSiblings()) {
                var dur = traverseTree(sibling, map, startTime);
                maxDur = Math.max(dur, maxDur);
            }
            return maxDur;
        }
        if (node instanceof SequenceNode<Note>) {
            var time = startTime;
            for (Node<Note> child : node.getChildren()) {
                var duration = traverseTree(child, map, time);
                time += duration;
            }
            return time;
        }

        throw new UnsupportedOperationException("Unspupported midi node");
    }

    private static Note enrichNoteWithModifier(SimpleNode<Note> node) {
        var modifier = node.getModifier();
        var note = node.getValue();
        if (modifier == null) {
            return node.getValue();
        }
        return new Note(
                Optional.ofNullable(note.getPitch()).orElse(modifier.getPitch()),
                Optional.ofNullable(note.getOctave()).orElse(modifier.getOctave()),
                Optional.ofNullable(note.getDuration()).orElse(modifier.getRythm())
        );
    }

    private static int calcDuration(Note note) {
        return switch (note.getDuration()) {
            case e -> 1;
            case q -> 2;
            case null, default -> 0;
        };
    }
}
