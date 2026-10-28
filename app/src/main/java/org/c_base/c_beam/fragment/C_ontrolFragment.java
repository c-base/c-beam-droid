package org.c_base.c_beam.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.c_base.c_beam.R;
import org.c_base.c_beam.activity.BamActivity;
import org.c_base.c_beam.domain.C_beam;
import org.c_base.c_beam.util.Helper;


public class C_ontrolFragment extends Fragment {
    private ProgressBar input;
    private final C_ontrolFragment cf = this;
    private AlertDialog pd;
    private int progress;
    // the fragment framework re-instantiates fragments through the no-arg
    // constructor when an activity is restored from saved state, so the
    // C_beam singleton is fetched here instead of being passed in
    private final C_beam c_beam = C_beam.getInstance();

    public C_ontrolFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        View v = inflater.inflate(R.layout.fragment_c_ontrol, container, false);

        TextView pattern = v.findViewById(R.id.textView1);
        Helper.setFont(getActivity(), pattern);

        Button b = v.findViewById(R.id.button_self_destruct);

        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(v.getContext());
                b.setTitle(R.string.self_destruct_in_progress);
                input = new ProgressBar(v.getContext(), null, android.R.attr.progressBarStyleHorizontal);
                b.setView(input);
                b.setPositiveButton(R.string.self_destruct_thx, null);

                pd = b.create();
                pd.show();

                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        for (progress = 0; progress < 100; progress++) {
                            try {
                                if (progress < 97) {
                                    Thread.sleep(5);
                                } else {
                                    Thread.sleep(2000);
                                }
                                //Thread.sleep(2*progress);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            input.setProgress(progress);
                        }
                        pd.dismiss();
                        Intent myIntent = new Intent(cf.getActivity(), BamActivity.class);
                        startActivityForResult(myIntent, 0);
                    }

                    public int getProgress() {
                        return progress;
                    }
                };
                Thread thread = new Thread(r);
                thread.start();


            }
        });

        b = v.findViewById(R.id.button_liftoff);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(v.getContext());
                b.setTitle(R.string.lift_off_title);
                b.setMessage(R.string.lift_off_message);
                b.setPositiveButton(R.string.button_ok, null);
                b.show();
            }
        });

        b = v.findViewById(R.id.button_unlabeled);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                throw new WTFException("I told you not to press the button...");
            }
        });

        // disabled: bluewall/darkwall and the software-endlager have no function on the station right now
        /*
        ToggleButton t = v.findViewById(R.id.toggleButtonBluewall);
        t.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton t = (ToggleButton) v;
                if (c_beam != null) {
                    if (t.isChecked()) {
                        c_beam.bluewall();
                    } else {
                        c_beam.bluewall();
                    }
                }
            }
        });

        b = v.findViewById(R.id.button_softwareverbrennung);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.hwstorage();
            }
        });
        */
        /* */
        b = v.findViewById(R.id.button1);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(0);
            }
        });

        b = v.findViewById(R.id.button2);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(1);
            }
        });

        b = v.findViewById(R.id.button3);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(2);
            }
        });

        // disabled: the c_leuse leds know only off, rainbow, success, failure and notbeleuchtung; this legacy id falls back to rainbow (was blaugruen)
        /*
        b = v.findViewById(R.id.button4);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(3);
            }
        });
        */

        // legacy id 4 is what the server maps to "failure", the red pattern
        b = v.findViewById(R.id.button5);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(4);
            }
        });

        // disabled: legacy id 4 is "failure" (red) on the c_leuse leds, the rot button sends it now (was blau)
        /*
        b = v.findViewById(R.id.button6);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(4);
            }
        });
        */

        // disabled: the c_leuse leds know only off, rainbow, success, failure and notbeleuchtung; this legacy id falls back to rainbow (was weiss)
        /*
        b = v.findViewById(R.id.button7);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(6);
            }
        });
        */

        // disabled: the c_leuse leds have no speed setting, set_stripe_speed is a no-op on the server
        /*
        b = v.findViewById(R.id.button_speed_plus_9);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_speed(9);
            }
        });


        b = v.findViewById(R.id.button8);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_speed(6);
            }
        });

        b = v.findViewById(R.id.button9);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_speed(3);
            }
        });

        b = v.findViewById(R.id.button10);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_speed(0);
            }
        });

        b = v.findViewById(R.id.button11);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_speed(-3);
            }
        });

        b = v.findViewById(R.id.button12);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_speed(-6);
            }
        });

        b = v.findViewById(R.id.button_speed_minus_9);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_speed(-9);
            }
        });
        */

        b = v.findViewById(R.id.button_emergency_lights);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.notbeleuchtung();
            }
        });

        // disabled: the c_leuse leds know only off, rainbow, success, failure and notbeleuchtung; this legacy id falls back to rainbow (was blauer punkt)
        /*
        b = v.findViewById(R.id.button_small_blue);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(7);
            }
        });
        */

        // disabled: the c_leuse leds know only off, rainbow, success, failure and notbeleuchtung; this legacy id falls back to rainbow (was gruener punkt)
        /*
        b = v.findViewById(R.id.button_small_green);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(8);
            }
        });
        */

        // disabled: the c_leuse leds know only off, rainbow, success, failure and notbeleuchtung; this legacy id falls back to rainbow (was roter punkt)
        /*
        b = v.findViewById(R.id.button_small_red);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_pattern(9);
            }
        });
        */

        b = v.findViewById(R.id.button_pattern_default);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                c_beam.set_stripe_default();
            }
        });

        return v;
    }

    static class WTFException extends RuntimeException {
        public WTFException(String message) {
            super(message);
        }

    }


}
