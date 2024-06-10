package com.declarative.music.midi;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.tree.SimpleNode;
import com.declarative.music.interpreter.values.music.MusicTree;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.interpreter.values.music.Rythm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MidiMapper {
    public static Map<Integer, List<Note>> mapToEventStamps(MusicTree musicTree) {
        TreeMap<Integer, List<Note>> orderedMap = new TreeMap<>();
        traverseTree(musicTree.getRoot(), orderedMap, 0);
        return orderedMap;
    }

    private static int traverseTree(Node<Note> node, TreeMap<Integer, List<Note>> map, int startTime) {
        if (node instanceof final SimpleNode<Note> noteNode) {
            var note = noteNode.getModified().getValue();
            map.computeIfAbsent(startTime, k -> new ArrayList<>()).add(note);
            return Rythm.values().length - note.getDuration().ordinal() + 1;
        }
        if (node instanceof GroupNode<Note>) {
            int maxDur = 0;
            for (Node<Note> sibling : node.getChildren()) {
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
}
