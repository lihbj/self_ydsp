package com.godbo.ydsp.utils;

/**
 * TODO:
 *
 * @author 李海波
 * @version 1.o
 * @date 2020年1月19日00:07:17
 */
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class MyX509trustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException { }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException { }

    @Override
    public X509Certificate[] getAcceptedIssuers() {  return null;  }
}
