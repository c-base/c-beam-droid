package org.c_base.c_beam.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.c_base.c_beam.R;

public class WebViewFragment extends Fragment {
	private WebView webView;
	private String url = "https://www.c-base.org/";

	public void setUrl(String url) {
		this.url = url;
		if (webView != null) {
			webView.loadUrl(url);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_webview, container, false);
		webView = v.findViewById(R.id.webView);

		// Keep navigation inside the WebView, but leave SSL error handling to the
		// default implementation, which cancels the load on an invalid certificate.
		webView.setWebViewClient(new WebViewClient());
		initWebView();

		return v;
	}

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }
}
