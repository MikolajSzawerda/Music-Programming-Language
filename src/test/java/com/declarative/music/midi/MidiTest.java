package com.declarative.music.midi;

import org.junit.jupiter.api.Test;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class MidiTest {
    @Test
    void shouldParseMidi() throws InvalidMidiDataException, IOException {
        final var file = new File("/home/mszawerd/IdeaProjects/music-programming-language/src/test/resources/unforgiven.mid");
        final var sequence = MidiSystem.getSequence(file);
        final var track = sequence.getTracks()[1];
        final var noteOne = new HashMap<Integer, Long>();
        final var notesAtTick = new TreeMap<Long, List<NoteExp>>();
        for (var i = 0; i < track.size(); i++) {
            final var event = track.get(i);
            final var message = event.getMessage();
            if (message instanceof final ShortMessage msg) {
                if (msg.getCommand() == ShortMessage.NOTE_ON) {
                    noteOne.put(msg.getData1(), event.getTick());
                }
                if (msg.getCommand() == ShortMessage.NOTE_OFF) {
                    final var start = noteOne.get(msg.getData1());
                    final var duration = event.getTick() - start;
                    notesAtTick
                            .computeIfAbsent(start, k -> new ArrayList<>())
                            .add(new NoteExp(msg.getData1(), start, duration));
                }
            }
        }
        System.out.println(notesAtTick);
    }

    @Test
    void shouldSaveTree() throws InvalidMidiDataException, IOException {
        final var progression = List.of(
                List.of(new Note(Pitch.C, 4, Duration.D_QUOTER, 0), new Note(Pitch.F, 4, Duration.D_EIGHT, -1), new Note(Pitch.E, 4, Duration.D_EIGHT, -1), new Note(Pitch.C, 4, Duration.D_QUOTER, -1)),
                List.of(new Note(Pitch.E, 4, Duration.D_QUOTER, 0)),
                List.of(new Note(Pitch.G, 4, Duration.D_QUOTER, 0))
        );

        final int ticksPerQuarterNote = 480;
        final int tempoBPM = 120;
        final Sequence sequence = new Sequence(Sequence.PPQ, ticksPerQuarterNote);
        final Track tempoTrack = sequence.createTrack();
        final int microsecPerQuarter = 60000000 / tempoBPM;

        final MetaMessage tempoMeta = new MetaMessage();
        final byte[] bt = {(byte) (microsecPerQuarter >> 16), (byte) (microsecPerQuarter >> 8), (byte) (microsecPerQuarter)};
        tempoMeta.setMessage(0x51, bt, 3);
        final MidiEvent tempoEvent = new MidiEvent(tempoMeta, 0);
        tempoTrack.add(tempoEvent);
        final var track = sequence.createTrack();

        for (final var phrase : progression) {
            long currentTick = phrase.getFirst().startTick();
            for (final var note : phrase) {
                final var pitch = getPitch(note);
                final var ticks = getTicks(note, ticksPerQuarterNote);
                final ShortMessage noteOn = new ShortMessage();
                noteOn.setMessage(ShortMessage.NOTE_ON, 0, pitch, 100);
                track.add(new MidiEvent(noteOn, currentTick));

                // Note OFF
                final ShortMessage noteOff = new ShortMessage();
                noteOff.setMessage(ShortMessage.NOTE_OFF, 0, pitch, 0);
                track.add(new MidiEvent(noteOff, currentTick + ticks));
                currentTick += ticks;
            }

        }


        MidiSystem.write(sequence, 1, new File("/home/mszawerd/IdeaProjects/music-programming-language/src/test/resources/out.mid"));
    }

    private int getPitch(final Note note) {
        return 12 * (note.octave() + 1) + note.pitch().ordinal();
    }

    private int getTicks(final Note note, final int quoter) {
        return switch (note.duration()) {
            case D_QUOTER -> quoter;
            case D_EIGHT -> quoter / 2;
        };
    }

    record NoteExp(int pitch, long tick, long duration) {
    }
}
