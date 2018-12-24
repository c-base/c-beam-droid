package org.c_base.c_beam.fragment;

import android.net.http.SslCertificate;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class C_portalWebViewFragment extends Fragment {
    protected WebView webView;
    private String url;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = getResources().openRawResource(R.raw.cacert_class3); // stored at \app\src\main\res\raw
            final Certificate certificate = cf.generateCertificate(caInput);
            caInput.close();

            View v = inflater.inflate(R.layout.fragment_c_portal_webview, container, false);
            webView = (WebView) v.findViewById(R.id.c_portalWebView);
            webView.setWebViewClient(new WebViewClient() {
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    SslCertificate sslCertificate = error.getCertificate();
                    Certificate cert = getX509Certificate(sslCertificate);
                    if (cert != null && certificate != null) {
                        try {
                            // Reference: https://developer.android.com/reference/java/security/cert/Certificate.html#verify(java.security.PublicKey)
                            cert.verify(certificate.getPublicKey()); // Verify here...
                            handler.proceed();
                        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
                            super.onReceivedSslError(view, handler, error);
                            e.printStackTrace();
                        }
                    } else {
                        super.onReceivedSslError(view, handler, error);
                    }
                }
            });
            webView.getSettings().setJavaScriptEnabled(true);
            return v;
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    private Certificate getX509Certificate(SslCertificate sslCertificate) {
        Bundle bundle = SslCertificate.saveState(sslCertificate);
        byte[] bytes = bundle.getByteArray("x509-certificate");
        if (bytes == null) {
            return null;
        } else {
            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                return certFactory.generateCertificate(new ByteArrayInputStream(bytes));
            } catch (CertificateException e) {
                return null;
            }
        }
    }

}
