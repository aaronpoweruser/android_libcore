/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.xnet.provider.jsse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import libcore.base.EmptyArray;

/**
 *
 * TrustManagerFactory service provider interface implementation.
 *
 * @see javax.net.ssl.TrustManagerFactorySpi
 */
public class TrustManagerFactoryImpl extends TrustManagerFactorySpi {

    private KeyStore keyStore;

    /**
     * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(KeyStore)
     */
    @Override
    public void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks != null) {
            keyStore = ks;
        } else {
            if (System.getProperty("javax.net.ssl.trustStore") == null) {
                String filename = System.getProperty("java.home") + "/etc/security/cacerts.bks";
                System.setProperty("javax.net.ssl.trustStore", filename);
            }
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            String keyStoreName = System.getProperty("javax.net.ssl.trustStore");
            String keyStorePwd = null;
            if (keyStoreName == null || keyStoreName.equalsIgnoreCase("NONE") || keyStoreName.isEmpty()) {
                try {
                    keyStore.load(null, null);
                } catch (IOException e) {
                    throw new KeyStoreException(e);
                } catch (CertificateException e) {
                    throw new KeyStoreException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new KeyStoreException(e);
                }
            } else {
                keyStorePwd = System.getProperty("javax.net.ssl.trustStorePassword");
                char[] pwd;
                if (keyStorePwd == null) {
                    pwd = EmptyArray.CHAR;
                } else {
                    pwd = keyStorePwd.toCharArray();
                }
                try {
                    InputStream in = null;
                    try {
                        in = new BufferedInputStream(new FileInputStream(keyStoreName));
                        keyStore.load(in, pwd);
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                } catch (FileNotFoundException e) {
                    throw new KeyStoreException(e);
                } catch (IOException e) {
                    throw new KeyStoreException(e);
                } catch (CertificateException e) {
                    throw new KeyStoreException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new KeyStoreException(e);
                }
            }
        }

    }

    /**
     * @see javax.net.ssl#engineInit(ManagerFactoryParameters)
     */
    @Override
    public void engineInit(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException(
                "ManagerFactoryParameters not supported");
    }

    /**
     * @see javax.net.ssl#engineGetTrustManagers()
     */
    @Override
    public TrustManager[] engineGetTrustManagers() {
        if (keyStore == null) {
            throw new IllegalStateException(
                    "TrustManagerFactory is not initialized");
        }
        return new TrustManager[] { new TrustManagerImpl(keyStore) };
    }
}
