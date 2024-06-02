package com.declarative.music.midi;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import org.junit.jupiter.api.Test;

import com.declarative.music.interpreter.Executor;
import com.declarative.music.interpreter.values.music.Note;
import com.declarative.music.lexer.LexerImpl;
import com.declarative.music.parser.Parser;
import com.declarative.music.parser.exception.ParsingException;


public class MidiTest
{
    @Test
    void shouldParseMidi() throws InvalidMidiDataException, IOException
    {
        final var file = new File("/home/mszawerd/IdeaProjects/music-programming-language/src/test/resources/unforgiven.mid");
        final var sequence = MidiSystem.getSequence(file);
        final var track = sequence.getTracks()[1];
        final var noteOne = new HashMap<Integer, Long>();
        final var notesAtTick = new TreeMap<Long, List<NoteExp>>();
        for (var i = 0; i < track.size(); i++)
        {
            final var event = track.get(i);
            final var message = event.getMessage();
            if (message instanceof final ShortMessage msg)
            {
                if (msg.getCommand() == ShortMessage.NOTE_ON)
                {
                    noteOne.put(msg.getData1(), event.getTick());
                }
                if (msg.getCommand() == ShortMessage.NOTE_OFF)
                {
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
    void shouldSaveTree() throws InvalidMidiDataException, IOException, ParsingException
    {
        final var code = """
            let music = (C, 4) q | (E, 4) q | (G, 4) q | (C, 4) e | (D, 4) e;
            """;
        final var lexer = new LexerImpl(new StringReader(code));
        final var parser = new Parser(lexer);
        var interpreter = new Executor();

        // when
        parser.parserProgram().accept(interpreter);
        var value = (LinkedList<Note>) interpreter.getManager().get("music").orElseThrow().getValue();

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

//        for (final var phrase : progression)
//        {
        long currentTick = 0;
        for (final var note : value)
        {
            var midiNote = MidiNote.from(note);
            final var pitch = getPitch(midiNote);
            final var ticks = getTicks(midiNote, ticksPerQuarterNote);
            final ShortMessage noteOn = new ShortMessage();
            noteOn.setMessage(ShortMessage.NOTE_ON, 0, pitch, 100);
            track.add(new MidiEvent(noteOn, currentTick));

            // MidiNote OFF
            final ShortMessage noteOff = new ShortMessage();
            noteOff.setMessage(ShortMessage.NOTE_OFF, 0, pitch, 0);
            track.add(new MidiEvent(noteOff, currentTick + ticks));
            currentTick += ticks;
        }

//        }

        MidiSystem.write(sequence, 1, new File("out.mid"));
    }

    private int getPitch(final MidiNote note)
    {
        return 12 * (note.octave() + 1) + note.pitch().ordinal();
    }

    private int getTicks(final MidiNote note, final int quoter)
    {
        return switch (note.duration())
        {
            case q -> quoter;
            case e -> quoter / 2;
        };
    }

    record NoteExp(int pitch, long tick, long duration)
    {
    }
}
