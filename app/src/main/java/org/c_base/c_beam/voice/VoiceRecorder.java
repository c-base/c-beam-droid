package org.c_base.c_beam.voice;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Records 16 kHz mono PCM from the microphone into memory and hands it back as WAV.
 *
 * The recording never touches the file system. The microphone is opened in start() and
 * released again in stop() or cancel(), so Android's microphone indicator goes away as soon
 * as the caller lets go. Must be driven from the main thread.
 */
public class VoiceRecorder {
    private static final String TAG = "VoiceRecorder";

    /** What Whisper resamples to internally, so nothing is lost by recording it directly. */
    private static final int SAMPLE_RATE = 16000;
    /** Hard cap on a single recording; the UI shows the remaining time. */
    public static final long MAX_DURATION_MS = 20000;
    /** Read in 100 ms chunks so the level meter updates smoothly. */
    private static final int CHUNK_BYTES = SAMPLE_RATE * 2 / 10;

    public interface Listener {
        /** Peak level of the latest chunk, 0..100, on the main thread. */
        void onLevel(int level, long elapsedMs);
        /** MAX_DURATION_MS reached; the recording has stopped itself but not been released. */
        void onLimitReached();
    }

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ByteArrayOutputStream pcm = new ByteArrayOutputStream();

    private AudioRecord record;
    private Thread thread;
    private volatile boolean running;
    private long startedAt;

    /** Opens the microphone and starts capturing. Returns false if the device refused. */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public boolean start(final Listener listener) {
        if (record != null) {
            return false;
        }
        int minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        int bufferSize = Math.max(minBuffer, CHUNK_BYTES * 4);
        AudioRecord r;
        try {
            r = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "AudioRecord rejected the format", e);
            return false;
        }
        if (r.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord did not initialise");
            r.release();
            return false;
        }

        pcm.reset();
        record = r;
        running = true;
        startedAt = SystemClock.elapsedRealtime();
        record.startRecording();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] chunk = new byte[CHUNK_BYTES];
                while (running) {
                    int n = record.read(chunk, 0, chunk.length);
                    if (n <= 0) {
                        break;
                    }
                    synchronized (pcm) {
                        pcm.write(chunk, 0, n);
                    }
                    final int level = peakLevel(chunk, n);
                    final long elapsed = SystemClock.elapsedRealtime() - startedAt;
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            if (running) {
                                listener.onLevel(level, elapsed);
                            }
                        }
                    });
                    if (elapsed >= MAX_DURATION_MS) {
                        running = false;
                        main.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onLimitReached();
                            }
                        });
                    }
                }
            }
        }, "VoiceRecorder");
        thread.start();
        return true;
    }

    public boolean isRecording() {
        return record != null;
    }

    public long elapsedMs() {
        return record == null ? 0 : SystemClock.elapsedRealtime() - startedAt;
    }

    /** Stops, releases the microphone and returns the recording as a WAV file in memory. */
    public byte[] stop() {
        if (!release()) {
            return null;
        }
        byte[] samples;
        synchronized (pcm) {
            samples = pcm.toByteArray();
            pcm.reset();
        }
        return toWav(samples);
    }

    /** Stops, releases the microphone and throws the recording away. */
    public void cancel() {
        release();
        synchronized (pcm) {
            pcm.reset();
        }
    }

    private boolean release() {
        if (record == null) {
            return false;
        }
        running = false;
        try {
            thread.join(500);
        } catch (InterruptedException ignored) {
        }
        try {
            record.stop();
        } catch (IllegalStateException ignored) {
        }
        record.release();
        record = null;
        thread = null;
        return true;
    }

    private static int peakLevel(byte[] buf, int n) {
        int peak = 0;
        for (int i = 0; i + 1 < n; i += 2) {
            int sample = (short) ((buf[i] & 0xff) | (buf[i + 1] << 8));
            peak = Math.max(peak, Math.abs(sample));
        }
        return peak * 100 / 32768;
    }

    /** Prepends the 44-byte RIFF/WAVE header for 16-bit mono PCM. */
    static byte[] toWav(byte[] samples) {
        ByteBuffer b = ByteBuffer.allocate(44 + samples.length).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes());
        b.putInt(36 + samples.length);
        b.put("WAVE".getBytes());
        b.put("fmt ".getBytes());
        b.putInt(16);                       // fmt chunk size
        b.putShort((short) 1);              // PCM
        b.putShort((short) 1);              // channels
        b.putInt(SAMPLE_RATE);
        b.putInt(SAMPLE_RATE * 2);          // byte rate
        b.putShort((short) 2);              // block align
        b.putShort((short) 16);             // bits per sample
        b.put("data".getBytes());
        b.putInt(samples.length);
        b.put(samples);
        return b.array();
    }
}
