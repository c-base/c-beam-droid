package org.c_base.c_beam.voice;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Plays a stream of raw 16-bit mono PCM as it arrives, on its own thread so that the
 * WebSocket reader never blocks on the audio buffer. {@link #finish()} lets the rest
 * play out; {@link #stop()} cuts it off at once.
 */
class PcmPlayer {
    private static final String TAG = "PcmPlayer";
    private static final byte[] END = new byte[0];

    private final int sampleRate;
    private final LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();
    private volatile boolean stopped;
    private volatile AudioTrack track;
    private Thread thread;

    PcmPlayer(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    synchronized void feed(byte[] pcm) {
        if (stopped || pcm.length == 0) {
            return;
        }
        queue.offer(pcm);
        if (thread == null) {
            thread = new Thread(this::run, "PcmPlayer");
            thread.start();
        }
    }

    /** No more data is coming; whatever is queued still plays. */
    void finish() {
        queue.offer(END);
    }

    /** Silence now, e.g. because the microphone is about to open. */
    void stop() {
        stopped = true;
        queue.clear();
        queue.offer(END);
        AudioTrack t = track;
        if (t != null) {
            try {
                t.pause();
                t.flush();
            } catch (IllegalStateException ignored) {
                // Already released by the player thread.
            }
        }
    }

    boolean isPlaying() {
        return thread != null && thread.isAlive() && !stopped;
    }

    private void run() {
        int frame = sampleRate / 10 * 2; // 100 ms of 16-bit mono
        int minBuffer = AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack t;
        try {
            t = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(Math.max(minBuffer, frame * 20))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
        } catch (Exception e) {
            Log.w(TAG, "no audio track: " + e.getMessage());
            stopped = true;
            return;
        }
        track = t;
        try {
            t.play();
            while (!stopped) {
                byte[] buf = queue.take();
                if (buf == END) {
                    break;
                }
                int off = 0;
                while (off < buf.length && !stopped) {
                    int n = t.write(buf, off, buf.length - off);
                    if (n <= 0) {
                        break;
                    }
                    off += n;
                }
            }
            if (!stopped) {
                // stop() in streaming mode plays out what is buffered before it halts.
                t.stop();
            }
        } catch (InterruptedException ignored) {
            // Shutting down.
        } catch (IllegalStateException e) {
            Log.w(TAG, "playback ended: " + e.getMessage());
        } finally {
            track = null;
            t.release();
        }
    }
}
