package org.c_base.c_beam.fragment;

import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.c_base.c_beam.R;

public class C_portalWebViewFragment extends Fragment {
    protected WebView webView;
    private String url;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        View v = inflater.inflate(R.layout.fragment_c_portal_webview, container, false);
        webView = (WebView) v.findViewById(R.id.c_portalWebView);
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        if (webView != null)
            webView.loadUrl(url);
    }

    public void setUrl(String url) {
        this.url = url;
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

}
