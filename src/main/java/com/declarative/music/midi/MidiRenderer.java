package com.declarative.music.midi;

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.declarative.music.interpreter.MidiMapper;
import com.declarative.music.interpreter.tree.Node;
import com.declarative.music.interpreter.values.music.Note;


public class MidiRenderer
{
    public static void renderAndSaveMidi(Node<Note> musicTree, String path, int bpm) throws InvalidMidiDataException, IOException
    {
        var res = MidiMapper.mapToEventStamps(musicTree);
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

        for (final var noteBlock : res.entrySet())
        {
            long currentTick = noteBlock.getKey() * ticksPerQuarterNote / 2;
            for (var note : noteBlock.getValue())
            {
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
            }
        }

        MidiSystem.write(sequence, 1, new File(path));
    }

    private static int getPitch(final MidiNote note)
    {
        return 12 * (note.octave() + 1) + note.pitch().ordinal();
    }

    private static int getTicks(final MidiNote note, final int quoter)
    {
        return switch (note.duration())
        {
            case q -> quoter;
            case e -> quoter / 2;
        };
    }
}
