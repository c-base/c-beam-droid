package org.c_base.c_beam.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.c_base.c_beam.R;

public class AboutDialogFragment extends DialogFragment {

    public AboutDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.about_dialog, null);

        TextView textView = view.findViewById(R.id.about_text);

        String version = "?";
        Context context = getActivity();
        if (context != null) {
            try {
                String packageName = context.getPackageName();
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
                version = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("AboutDialogFragment", "Failed to get version name", e);
            }
        }

        textView.setText(Html.fromHtml(getString(R.string.about_text, version)));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                })
                .create();
    }
}
