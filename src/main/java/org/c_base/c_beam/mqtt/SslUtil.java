package org.c_base.c_beam.mqtt;

import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.PEMParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SslUtil {
    public static final String CA_CERTIFICATE_ALIAS = "ca-certificate";

    public static SSLSocketFactory getSocketFactory(InputStream certificateInputStream)
            throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException {

        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        PEMParser parser = new PEMParser(new BufferedReader(new InputStreamReader(certificateInputStream)));
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate((X509CertificateHolder) parser.readObject());
        parser.close();

        // CA certificate is used to authenticate server
        KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        caKeyStore.load(null, null);
        caKeyStore.setCertificateEntry(CA_CERTIFICATE_ALIAS, certificate);
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(caKeyStore);

        // finally, create SSL socket factory
        SSLContext context = SSLContext.getInstance("TLSv1");
        context.init(null, trustManagerFactory.getTrustManagers(), null);

        return context.getSocketFactory();
    }
}