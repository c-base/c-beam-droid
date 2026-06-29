package org.c_base.c_beam.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.c_base.c_beam.R;

import java.io.InputStream;
import java.security.cert.CertificateFactory;

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

		try (InputStream caInput = getResources().openRawResource(R.raw.cacert)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cf.generateCertificate(caInput);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, android.net.http.SslError error) {
                    handler.proceed();
                }
            });

            initWebView();
        } catch (Exception e) {
            Log.e("WebViewFragment", "Error initializing WebView with custom CA", e);
            webView.setWebViewClient(new WebViewClient());
            initWebView();
        }
		return v;
	}

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }
}
