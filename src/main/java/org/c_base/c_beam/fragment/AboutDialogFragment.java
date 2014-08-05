package org.c_base.c_beam.fragment;

import org.c_base.c_beam.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


/**
 * Show an "About" dialog
 */
public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.label_about);
        builder.setPositiveButton(android.R.string.ok, null);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.about_dialog, null);

        TextView about = (TextView) view.findViewById(R.id.about_text);
        about.setText(Html.fromHtml(getString(R.string.about_text, getVersionName())));
        about.setMovementMethod(LinkMovementMethod.getInstance());

        builder.setView(view);

        return builder.create();
    }

    private String getVersionName() {
        String version = "?";
        try {
            Activity context = getActivity();
            String packageName = context.getPackageName();
            PackageInfo pi = context.getPackageManager().getPackageInfo(packageName, 0);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // do nothing
        }
        return version;
    }
}
