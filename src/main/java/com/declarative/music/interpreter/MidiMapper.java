package com.declarative.music.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.declarative.music.interpreter.tree.GroupNode;
import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.tree.SequenceNode;
import com.declarative.music.interpreter.values.music.Note;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MidiMapper
{
    public static Map<Integer, List<Note>> mapToEventStamps(Node<Note> musicTree)
    {
        TreeMap<Integer, List<Note>> orderedMap = new TreeMap<>();
        traverseTree(musicTree, orderedMap, 0);
        return orderedMap;
    }

    private static int traverseTree(Node<Note> node, TreeMap<Integer, List<Note>> map, int startTime)
    {
        if (node instanceof final Note note)
        {
            map.computeIfAbsent(startTime, k -> new ArrayList<>()).add((Note) node);
            return calcDuration(note);
        }
        if (node instanceof GroupNode<Note>)
        {
            int maxDur = 0;
            for (Node<Note> sibling : node.getSiblings())
            {
                var dur = traverseTree(sibling, map, startTime);
                maxDur = Math.max(dur, maxDur);
            }
            return maxDur;
        }
        if (node instanceof SequenceNode<Note>)
        {
            var time = startTime;
            for (Node<Note> child : node.getChildren())
            {
                var duration = traverseTree(child, map, time);
                time += duration;
            }
            return time;
        }

        throw new UnsupportedOperationException("Unspupported midi node");
    }

    private static int calcDuration(Note note)
    {
        return switch (note.getDuration())
        {
            case e -> 1;
            case q -> 2;
            case null, default -> 0;
        };
    }

    private static int calculateDuration(Node<Note> node)
    {
        if (node instanceof final Note note)
        {
            return switch (note.getDuration())
            {
                case e -> 1;
                case q -> 2;
                case null, default -> 0;
            };
        }
        else
        {
            int totalDuration = 0;
            for (Node<Note> child : node.getChildren())
            {
                totalDuration += calculateDuration(child);
            }
            return totalDuration;
        }
    }
}
