package com.jcraft.jorbis;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

/** Adapts JOrbis' package-private PCM reader to a Java Sound stream. */
public final class VorbisDecoder {
    private VorbisDecoder() {
    }

    public static AudioInputStream decode(Path path) throws IOException, JOrbisException {
        VorbisFile vorbis = new VorbisFile(new FileInputStream(path.toFile()), null, 0);
        try {
            Info info = vorbis.getInfo(0);
            if (info == null || info.channels < 1 || info.rate < 1) {
                throw new IOException("Invalid Vorbis stream: " + path);
            }

            int frameSize = info.channels * 2;
            long expectedBytes = vorbis.pcm_total(-1) * frameSize;
            int initialCapacity = (int) Math.min(Math.max(expectedBytes, 32_768L), 8_000_000L);
            ByteArrayOutputStream pcm = new ByteArrayOutputStream(initialCapacity);
            byte[] chunk = new byte[8192];
            while (true) {
                int count = readPcm(vorbis, chunk, info.channels);
                if (count == 0) {
                    break;
                }
                if (count < 0) {
                    throw new IOException("Could not decode Vorbis stream: " + path);
                }
                pcm.write(chunk, 0, count);
            }

            byte[] decoded = pcm.toByteArray();
            AudioFormat format = new AudioFormat(info.rate, 16, info.channels, true, false);
            return new AudioInputStream(
                    new ByteArrayInputStream(decoded), format, decoded.length / frameSize);
        } finally {
            vorbis.close();
        }
    }

    /** Avoids JOrbis 0.0.17's broken channel offsets and endian packing in read(). */
    private static int readPcm(VorbisFile vorbis, byte[] buffer, int channels) throws IOException {
        float[][][] pcm = new float[1][][];
        int[] offsets = new int[channels];
        while (true) {
            if (vorbis.decode_ready) {
                if (vorbis.getInfo(-1).channels != channels) {
                    throw new IOException("Channel count changed within Vorbis stream");
                }
                int samples = vorbis.vd.synthesis_pcmout(pcm, offsets);
                if (samples > 0) {
                    samples = Math.min(samples, buffer.length / (channels * 2));
                    int destination = 0;
                    for (int frame = 0; frame < samples; frame++) {
                        for (int channel = 0; channel < channels; channel++) {
                            int value = Math.round(pcm[0][channel][offsets[channel] + frame] * 32768f);
                            value = Math.max(-32768, Math.min(32767, value));
                            buffer[destination++] = (byte) value;
                            buffer[destination++] = (byte) (value >>> 8);
                        }
                    }
                    vorbis.vd.synthesis_read(samples);
                    vorbis.pcm_offset += samples;
                    return destination;
                }
            }
            int result = vorbis.process_packet(1);
            if (result == 0) {
                return 0;
            }
            if (result < 0) {
                throw new IOException("Could not decode Vorbis packet");
            }
        }
    }
}
