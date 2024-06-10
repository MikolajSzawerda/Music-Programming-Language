package com.declarative.music.midi;

import com.declarative.music.interpreter.values.music.MusicTree;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;


public class MidiRenderer {
    public static void renderAndSaveMidi(MusicTree musicTree, String path, int bpm) throws InvalidMidiDataException, IOException {
        var res = MidiMapper.mapToEventStamps(musicTree.getModified());
        final int ticksPerQuarterNote = 480;
        final Sequence sequence = new Sequence(Sequence.PPQ, ticksPerQuarterNote);
        final Track tempoTrack = sequence.createTrack();
        final int microsecPerQuarter = 60000000 / bpm;

        final MetaMessage tempoMeta = new MetaMessage();
        final byte[] bt = {(byte) (microsecPerQuarter >> 16), (byte) (microsecPerQuarter >> 8), (byte) (microsecPerQuarter)};
        tempoMeta.setMessage(0x51, bt, 3);
        final MidiEvent tempoEvent = new MidiEvent(tempoMeta, 0);
        tempoTrack.add(tempoEvent);
        final var track = sequence.createTrack();
        long currentTick = 0;
        long maxTick = 0;
        for (final var noteBlock : res.entrySet()) {
            currentTick = maxTick;
            for (var note : noteBlock.getValue()) {
                var midiNote = MidiNote.from(note);
                final var pitch = getPitch(midiNote);
                final var ticks = getTicks(midiNote, ticksPerQuarterNote);

                final ShortMessage noteOn = new ShortMessage();
                noteOn.setMessage(ShortMessage.NOTE_ON, 0, pitch, 100);
                var onEvent = new MidiEvent(noteOn, currentTick);
                track.add(onEvent);

                final ShortMessage noteOff = new ShortMessage();
                noteOff.setMessage(ShortMessage.NOTE_OFF, 0, pitch, 0);
                track.add(new MidiEvent(noteOff, currentTick + ticks));
                maxTick = Math.max(maxTick, currentTick + ticks);
            }
        }

        MidiSystem.write(sequence, 1, new File(path));
    }

    private static int getPitch(final MidiNote note) {
        return 12 * (note.octave() + 1) + note.pitch().ordinal();
    }

    private static int getTicks(final MidiNote note, final int quoter) {
        return switch (note.duration()) {
            case dl -> quoter * 16;
            case l -> quoter * 8;
            case w -> quoter * 4;
            case h -> quoter * 2;
            case q -> quoter;
            case e -> quoter / 2;
            case s -> quoter / 4;
            case t -> quoter / 8;
        };
    }
}
