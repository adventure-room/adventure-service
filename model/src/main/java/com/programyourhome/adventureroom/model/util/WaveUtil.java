package com.programyourhome.adventureroom.model.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;

import javax.sound.sampled.AudioFormat;

public class WaveUtil {

    public static final int WAVE_HEADER_SIZE = 44;

    private WaveUtil() {
    }

    public static InputStream prependWaveHeader(InputStream pcmInputstream, AudioFormat format, int pcmDataLength) {
        int frameSize = format.getChannels() * (format.getSampleSizeInBits() / 8);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(WAVE_HEADER_SIZE);

        try {
            // Write the WAVE header, see: http://soundfile.sapp.org/doc/WaveFormat/
            writeString(outputStream, "RIFF"); // chunk id
            writeInt(outputStream, 36 + pcmDataLength); // chunk size
            writeString(outputStream, "WAVE"); // format
            writeString(outputStream, "fmt "); // subchunk 1 id
            writeInt(outputStream, 16); // subchunk 1 size
            writeShort(outputStream, (short) 1); // audio format (1 = PCM)
            writeShort(outputStream, (short) format.getChannels()); // number of channels
            writeInt(outputStream, (int) format.getSampleRate()); // sample rate
            writeInt(outputStream, (int) format.getSampleRate() * frameSize); // byte rate
            writeShort(outputStream, (short) frameSize); // block align
            writeShort(outputStream, (short) format.getSampleSizeInBits()); // bits per sample
            writeString(outputStream, "data"); // subchunk 2 id
            writeInt(outputStream, pcmDataLength); // subchunk 2 size
        } catch (IOException e) {
            throw new IllegalStateException("IOException while writing wave header", e);
        }

        return new SequenceInputStream(new ByteArrayInputStream(outputStream.toByteArray()), pcmInputstream);
    }

    /**
     * Write an int as a sequence of 4 bytes
     */
    private static void writeInt(final OutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    /**
     * Write a short as a sequence of 2 bytes
     */
    private static void writeShort(final OutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    /**
     * Write a String as a sequence of characters.
     */
    private static void writeString(final OutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

}
