package org.c_base.c_beam.voice;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.domain.C_beam;

/**
 * Hold-to-talk microphone button with its recording banner and result card.
 *
 * The microphone is open exactly while the finger rests on the button: lifting it sends
 * the recording to Whisper, sliding off the button first throws it away, and leaving the
 * activity does the same. Nothing but this button ever starts a recording.
 *
 * The transcript then goes through a {@link VoiceSession}; whatever comes back (an answer,
 * something done, or a proposal that needs a tap) lands in the card above the button.
 */
public class HoldToTalkController {
    public static final int PERMISSION_REQUEST_CODE = 43;

    /** Shorter presses are taken for accidental taps and never sent anywhere. */
    private static final long MIN_DURATION_MS = 500;
    /** How far the finger may stray from the button before lifting it means "discard". */
    private static final int CANCEL_SLOP_DP = 48;
    private static final long HINT_DURATION_MS = 1500;

    private static final int COLOR_RECORDING = 0xDDCC0000;
    private static final int COLOR_DISCARD = 0xDDCC6600;
    private static final int COLOR_HINT = 0xDD333333;
    /** Scrollback of the little terminal; older lines fall off the top. */
    private static final int TERMINAL_LINES = 12;

    private final Activity activity;
    private final View panel;
    private final ImageButton button;
    private final TextView status;
    private final ProgressBar level;
    private final View card;
    private final TextView terminal;
    private final View confirmRow;
    private final java.util.ArrayDeque<String> lines = new java.util.ArrayDeque<String>();
    private boolean transientLine;

    private final VoiceRecorder recorder = new VoiceRecorder();
    private final Handler main = new Handler(Looper.getMainLooper());
    private Transcriber transcriber;
    private VoiceSession session;
    private VoiceCommands.Action pendingAction;
    private ObjectAnimator pulse;
    /** Only exists while "antworten vorlesen" is switched on. */
    private Speaker speaker;
    private boolean warnedNoVoice;

    private boolean available;
    private boolean busy;
    private boolean cancelArmed;
    private boolean finished;
    private final int cancelSlopPx;

    private final Runnable hideStatus = new Runnable() {
        @Override
        public void run() {
            status.setVisibility(View.GONE);
        }
    };

    // handleTouch() does call performClick() on ACTION_UP; lint only looks inside onTouch().
    @SuppressLint("ClickableViewAccessibility")
    public HoldToTalkController(Activity activity, View panel) {
        this.activity = activity;
        this.panel = panel;
        button = panel.findViewById(R.id.voice_button);
        status = panel.findViewById(R.id.voice_status);
        level = panel.findViewById(R.id.voice_level);
        card = panel.findViewById(R.id.voice_card);
        terminal = panel.findViewById(R.id.voice_terminal);
        confirmRow = panel.findViewById(R.id.voice_confirm);
        cancelSlopPx = (int) (CANCEL_SLOP_DP * activity.getResources().getDisplayMetrics().density);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouch(v, event);
            }
        });
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pendingAction == null) {
                    card.setVisibility(View.GONE);
                }
            }
        });
        panel.findViewById(R.id.voice_confirm_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoiceCommands.Action action = pendingAction;
                if (action == null) {
                    return;
                }
                pendingAction = null;
                confirmRow.setVisibility(View.GONE);
                commands().execute(action);
                print(activity.getString(R.string.voice_executed, action.description));
                say(activity.getString(R.string.voice_spoken_executed));
            }
        });
        panel.findViewById(R.id.voice_confirm_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingAction = null;
                confirmRow.setVisibility(View.GONE);
                print(activity.getString(R.string.voice_discarded));
            }
        });
    }

    /** Shows the panel only where the AI server can be reached, i.e. in the crew network. */
    public void setAvailable(boolean available) {
        this.available = available;
        panel.setVisibility(available ? View.VISIBLE : View.GONE);
        if (!available) {
            abort();
        }
        syncSpeaker();
    }

    /** Called from onDestroy: lets go of the text-to-speech engine. */
    public void release() {
        abort();
        if (speaker != null) {
            speaker.shutdown();
            speaker = null;
        }
    }

    /** Starts or drops the text-to-speech engine as the setting changes (checked on resume). */
    private void syncSpeaker() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean wanted = prefs.getBoolean(Settings.VOICE_SPEAK, false);
        if (wanted && speaker == null) {
            speaker = new Speaker(activity);
            warnedNoVoice = false;
        } else if (!wanted && speaker != null) {
            speaker.shutdown();
            speaker = null;
        }
    }

    private void say(String text) {
        if (speaker == null) {
            return;
        }
        if (speaker.lacksGermanVoice()) {
            if (!warnedNoVoice) {
                warnedNoVoice = true;
                print(activity.getString(R.string.voice_no_german_voice));
            }
            return;
        }
        speaker.speak(text);
    }

    /** Called from onPause: a recording must not outlive the visible activity. */
    public void abort() {
        if (speaker != null) {
            speaker.stop();
        }
        if (recorder.isRecording()) {
            recorder.cancel();
            stopRecordingUi();
            status.setVisibility(View.GONE);
        }
        if (transcriber != null) {
            transcriber.cancel();
            transcriber = null;
        }
        if (session != null) {
            session.cancel();
            session = null;
        }
        // A proposal that was never confirmed dies with the screen, and so does a
        // progress line whose result will never arrive.
        pendingAction = null;
        confirmRow.setVisibility(View.GONE);
        if (transientLine) {
            print(activity.getString(R.string.voice_discarded));
        }
        busy = false;
        button.setEnabled(true);
    }

    public void onPermissionResult(int requestCode, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showHint(R.string.voice_hold_hint, COLOR_HINT);
        }
    }

    private boolean handleTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!available || busy) {
                    return true;
                }
                if (!hasPermission()) {
                    explainAndRequestPermission();
                    return true;
                }
                startRecording(v);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (recorder.isRecording() && !finished) {
                    boolean outside = event.getX() < -cancelSlopPx
                            || event.getY() < -cancelSlopPx
                            || event.getX() > v.getWidth() + cancelSlopPx
                            || event.getY() > v.getHeight() + cancelSlopPx;
                    setCancelArmed(outside);
                }
                return true;

            case MotionEvent.ACTION_UP:
                v.performClick();
                if (recorder.isRecording() && !finished) {
                    if (cancelArmed) {
                        discard(R.string.voice_discarded);
                    } else if (recorder.elapsedMs() < MIN_DURATION_MS) {
                        discard(R.string.voice_hold_hint);
                    } else {
                        finishRecording();
                    }
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                if (recorder.isRecording() && !finished) {
                    discard(R.string.voice_discarded);
                }
                return true;

            default:
                return false;
        }
    }

    private void startRecording(View v) {
        // Checked inline (not via hasPermission()) so lint can see it next to the call.
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        boolean started = recorder.start(new VoiceRecorder.Listener() {
            @Override
            public void onLevel(int lvl, long elapsedMs) {
                level.setProgress(lvl);
                if (!cancelArmed) {
                    status.setText(activity.getString(R.string.voice_recording,
                            clock(elapsedMs), clock(VoiceRecorder.MAX_DURATION_MS)));
                }
            }

            @Override
            public void onLimitReached() {
                if (!finished) {
                    finishRecording();
                }
            }
        });
        if (!started) {
            showHint(R.string.voice_mic_failed, COLOR_HINT);
            return;
        }
        finished = false;
        cancelArmed = false;
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        // Otherwise the phone would record its own voice.
        if (speaker != null) {
            speaker.stop();
        }

        // The terminal keeps its scrollback; only an unanswered proposal is dropped.
        pendingAction = null;
        confirmRow.setVisibility(View.GONE);
        main.removeCallbacks(hideStatus);
        status.setBackgroundColor(COLOR_RECORDING);
        status.setText(activity.getString(R.string.voice_recording, clock(0),
                clock(VoiceRecorder.MAX_DURATION_MS)));
        status.setVisibility(View.VISIBLE);
        level.setProgress(0);
        level.setVisibility(View.VISIBLE);
        button.setActivated(true);
        startPulse();
    }

    private void setCancelArmed(boolean armed) {
        if (armed == cancelArmed) {
            return;
        }
        cancelArmed = armed;
        status.setBackgroundColor(armed ? COLOR_DISCARD : COLOR_RECORDING);
        if (armed) {
            status.setText(R.string.voice_release_to_discard);
        }
    }

    private void discard(int hintRes) {
        finished = true;
        recorder.cancel();
        stopRecordingUi();
        showHint(hintRes, COLOR_HINT);
    }

    private void finishRecording() {
        finished = true;
        byte[] wav = recorder.stop();
        stopRecordingUi();
        button.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if (wav == null) {
            return;
        }

        busy = true;
        button.setEnabled(false);
        main.removeCallbacks(hideStatus);
        status.setVisibility(View.GONE);
        // Progress shows up in the terminal itself; the line is replaced by the result.
        printTransient(prompt() + " " + activity.getString(R.string.voice_transcribing));

        transcriber = new Transcriber(AiServer.baseUrl(activity));
        transcriber.transcribe(wav, new Transcriber.Callback() {
            @Override
            public void onTranscript(String text) {
                transcriber = null;
                if (text.isEmpty()) {
                    done();
                    print(prompt() + " " + activity.getString(R.string.voice_empty));
                    return;
                }
                print(prompt() + " " + text);
                understand(text);
            }

            @Override
            public void onError(String message) {
                transcriber = null;
                done();
                print(prompt());
                print(activity.getString(R.string.voice_error, message));
            }
        });
    }

    /** Hands the transcript to the model and shows whatever comes back. */
    private void understand(String text) {
        printTransient(activity.getString(R.string.voice_thinking));

        ChatModel model = new ChatModel(AiServer.baseUrl(activity), AiServer.model(activity));
        session = new VoiceSession(model, commands(), username(), new VoiceSession.Listener() {
            @Override
            public void onAnswer(String reply) {
                done();
                print(reply);
                say(reply);
            }

            @Override
            public void onExecuted(VoiceCommands.Action action) {
                done();
                print(activity.getString(R.string.voice_executed, action.description));
                say(activity.getString(R.string.voice_spoken_executed));
            }

            @Override
            public void onConfirmRequired(VoiceCommands.Action action) {
                done();
                pendingAction = action;
                print(action.description + " [j/n]");
                confirmRow.setVisibility(View.VISIBLE);
                say(activity.getString(R.string.voice_spoken_confirm, action.description));
            }

            @Override
            public void onError(String message) {
                done();
                print(activity.getString(R.string.voice_error, message));
            }
        });
        session.start(text);
    }

    /** Back to idle: the button works again and the status banner goes away. */
    private void done() {
        busy = false;
        session = null;
        button.setEnabled(true);
        status.setVisibility(View.GONE);
    }

    /** A progress line ("transkribiere…", "denke nach…") that the next print replaces. */
    private void printTransient(String text) {
        print(text);
        transientLine = true;
    }

    /** Appends to the terminal, like the activity log: scrollback, then a fresh prompt. */
    private void print(String text) {
        if (transientLine) {
            lines.pollLast();
            transientLine = false;
        }
        for (String line : text.split("\n")) {
            lines.addLast(line);
        }
        while (lines.size() > TERMINAL_LINES) {
            lines.removeFirst();
        }
        StringBuilder b = new StringBuilder();
        for (String line : lines) {
            b.append(line).append('\n');
        }
        b.append(prompt());
        terminal.setText(b);
        card.setVisibility(View.VISIBLE);
    }

    private String prompt() {
        return username() + "@c-beam>";
    }

    private VoiceCommands commands() {
        return new VoiceCommands(C_beam.getInstance(), username());
    }

    private String username() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString(Settings.USERNAME, "bernd");
    }

    private void stopRecordingUi() {
        level.setVisibility(View.GONE);
        button.setActivated(false);
        stopPulse();
    }

    private void showHint(int textRes, int color) {
        main.removeCallbacks(hideStatus);
        status.setBackgroundColor(color);
        status.setText(textRes);
        status.setVisibility(View.VISIBLE);
        main.postDelayed(hideStatus, HINT_DURATION_MS);
    }

    private void startPulse() {
        stopPulse();
        pulse = ObjectAnimator.ofPropertyValuesHolder(button,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.15f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.15f));
        pulse.setDuration(600);
        pulse.setRepeatCount(ValueAnimator.INFINITE);
        pulse.setRepeatMode(ValueAnimator.REVERSE);
        pulse.start();
    }

    private void stopPulse() {
        if (pulse != null) {
            pulse.cancel();
            pulse = null;
        }
        button.setScaleX(1f);
        button.setScaleY(1f);
    }

    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Says what gets recorded and where it goes before Android asks for the permission. */
    private void explainAndRequestPermission() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.voice_permission_title)
                .setMessage(R.string.voice_permission_text)
                .setNegativeButton(R.string.voice_permission_no, null)
                .setPositiveButton(R.string.voice_permission_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                PERMISSION_REQUEST_CODE);
                    }
                })
                .show();
    }

    private static String clock(long ms) {
        long s = ms / 1000;
        return (s / 60) + ":" + (s % 60 < 10 ? "0" : "") + (s % 60);
    }
}
