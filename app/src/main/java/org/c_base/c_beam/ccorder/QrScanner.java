package org.c_base.c_beam.ccorder;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Reads QR codes from the c-corder's camera preview while the scanner is switched on.
 *
 * Hooks into the preview as a {@link Camera.PreviewCallback} and hands one frame at a
 * time to ZXing on a worker thread; frames that arrive while a decode is running are
 * dropped, which keeps the load at a few decodes per second. Only the luminance plane
 * of the NV21 frame is read, so the camera's colour effects do not matter.
 */
public class QrScanner implements Camera.PreviewCallback {
    private static final String TAG = "QrScanner";
    /** The same code staying in view is reported once, not on every frame. */
    private static final long REPEAT_MS = 3000;

    public interface Listener {
        /** On the main thread, with the decoded text. */
        void onCode(String text);
    }

    private final Camera camera;
    private final Listener listener;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final MultiFormatReader reader = new MultiFormatReader();
    private volatile boolean busy;
    private volatile boolean stopped;
    private int width;
    private int height;
    private String focusBefore;
    private String lastText;
    private long lastAt;

    public QrScanner(Camera camera, Listener listener) {
        this.camera = camera;
        this.listener = listener;
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        reader.setHints(hints);
    }

    /** Switches the camera to continuous focus where it can and starts receiving frames. */
    public void start() {
        try {
            Camera.Parameters p = camera.getParameters();
            Camera.Size size = p.getPreviewSize();
            width = size.width;
            height = size.height;
            List<String> modes = p.getSupportedFocusModes();
            if (modes != null && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                focusBefore = p.getFocusMode();
                p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(p);
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "could not prepare the camera for scanning", e);
        }
        camera.setPreviewCallback(this);
    }

    /** Stops receiving frames and puts the focus mode back. Safe to call twice. */
    public void stop() {
        stopped = true;
        try {
            camera.setPreviewCallback(null);
            if (focusBefore != null) {
                Camera.Parameters p = camera.getParameters();
                p.setFocusMode(focusBefore);
                camera.setParameters(p);
                focusBefore = null;
            }
        } catch (RuntimeException e) {
            // the camera may already be released; nothing left to undo then
        }
        worker.shutdown();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera cam) {
        if (busy || stopped || data == null) {
            return;
        }
        if (data.length < width * height * 3 / 2) {
            // the preview size changed under us (surfaceChanged sets it); read it again
            try {
                Camera.Size size = cam.getParameters().getPreviewSize();
                width = size.width;
                height = size.height;
            } catch (RuntimeException e) {
                return;
            }
            if (data.length < width * height * 3 / 2) {
                return;
            }
        }
        busy = true;
        final int w = width;
        final int h = height;
        worker.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    decode(data, w, h);
                } finally {
                    busy = false;
                }
            }
        });
    }

    private void decode(byte[] data, int w, int h) {
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, w, h, 0, 0, w, h, false);
        try {
            Result result = reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));
            deliver(result.getText());
        } catch (NotFoundException e) {
            // no code in this frame, the normal case
        } catch (RuntimeException e) {
            Log.w(TAG, "decode failed", e);
        } finally {
            reader.reset();
        }
    }

    private void deliver(final String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (text.equals(lastText) && now - lastAt < REPEAT_MS) {
            return;
        }
        lastText = text;
        lastAt = now;
        main.post(new Runnable() {
            @Override
            public void run() {
                if (!stopped) {
                    listener.onCode(text);
                }
            }
        });
    }
}
