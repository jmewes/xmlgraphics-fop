/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.pdf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;

import org.apache.commons.logging.Log;
import org.apache.fop.apps.FOUserAgent;

/**
 * This class acts as a factory for PDF encryption support. It enables the
 * feature to be optional to FOP depending on the availability of JCE.
 */
public class PDFEncryptionManager {

    /**
     * Indicates whether JCE is available.
     * @return boolean true if JCE is present
     */
    public static boolean isJCEAvailable() {
        try {
            Class clazz = Class.forName("javax.crypto.Cipher");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Checks whether the necessary algorithms are available.
     * @return boolean True if all necessary algorithms are present
     */
    public static boolean checkAvailableAlgorithms() {
        if (!isJCEAvailable()) {
            return false;
        } else {
            Provider[] providers;
            providers = Security.getProviders("Cipher.RC4");
            if (providers == null) {
                return false;
            }
            providers = Security.getProviders("MessageDigest.MD5");
            if (providers == null) {
                return false;
            }
            return true;
        }
    }
    

    /**
     * Sets up PDF encryption if PDF encryption is requested by registering
     * a <code>PDFEncryptionParams</code> object with the user agent and if
     * the necessary cryptographic support is available.
     * @param userAgent the user agent
     * @param pdf the PDF document to setup encryption for
     * @param log the logger to send warnings to
     */
    public static void setupPDFEncryption(FOUserAgent userAgent, 
                                          PDFDocument pdf,
                                          Log log) {
        if (userAgent == null) {
            throw new NullPointerException("User agent must not be null");
        }
        if (pdf == null) {
            throw new NullPointerException("PDF document must not be null");
        }
        if (userAgent.getPDFEncryptionParams() != null) {
            if (!checkAvailableAlgorithms()) {
                if (isJCEAvailable()) {
                    log.warn("PDF encryption has been requested, JCE is "
                            + "available but there's no "
                            + "JCE provider available that provides the "
                            + "necessary algorithms. The PDF won't be "
                            + "encrypted.");
                } else {
                    log.warn("PDF encryption has been requested but JCE is "
                            + "unavailable! The PDF won't be encrypted.");
                }
            }
            pdf.setEncryption(userAgent.getPDFEncryptionParams());
        }
    }
    
    /**
     * Creates a new PDFEncryption instance if PDF encryption is available.
     * @param objnum PDF object number
     * @param params PDF encryption parameters
     * @return PDFEncryption the newly created instance, null if PDF encryption
     * is unavailable.
     */
    public static PDFEncryption newInstance(int objnum, PDFEncryptionParams params) {
        try {
            Class clazz = Class.forName("org.apache.fop.pdf.PDFEncryptionJCE");
            Method makeMethod = clazz.getMethod("make", 
                        new Class[] {int.class, PDFEncryptionParams.class});
            Object obj = makeMethod.invoke(null, 
                        new Object[] {new Integer(objnum), params});
            return (PDFEncryption)obj;
        } catch (ClassNotFoundException e) {
            if (checkAvailableAlgorithms()) {
                System.out.println("JCE and algorithms available, but the "
                    + "implementation class unavailable. Please do a full "
                    + "rebuild.");
            }
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
