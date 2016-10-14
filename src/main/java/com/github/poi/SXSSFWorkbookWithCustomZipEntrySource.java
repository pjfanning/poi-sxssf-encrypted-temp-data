/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package com.github.poi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.poifs.crypt.ChainingMode;
import org.apache.poi.poifs.crypt.CipherAlgorithm;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SheetDataWriter;

public class SXSSFWorkbookWithCustomZipEntrySource extends SXSSFWorkbook {
    
    public SXSSFWorkbookWithCustomZipEntrySource() {
        super(20);
        setCompressTempFiles(true);
    }
    
    @Override
    public void write(OutputStream stream) throws IOException {
        flushSheets();
        EncryptedTempData tempData = new EncryptedTempData();
        try(OutputStream os = tempData.getOutputStream()) {
            getXSSFWorkbook().write(os);
        }
        ZipEntrySource source = null;
        try {
            // provide ZipEntrySource to poi which decrypts on the fly
            source = AesZipFileZipEntrySource.createZipEntrySource(tempData.getInputStream());
            injectData(source, stream);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        } finally {
            tempData.dispose();
            IOUtils.closeQuietly(source);
        }
    }
    
    @Override
    protected SheetDataWriter createSheetDataWriter() throws IOException {
        return new SheetDataWriterWithDecorator();
    }

    static class SheetDataWriterWithDecorator extends SheetDataWriter {
        final static CipherAlgorithm cipherAlgorithm = CipherAlgorithm.aes128;
        SecretKeySpec skeySpec;
        byte[] ivBytes;
        
        public SheetDataWriterWithDecorator() throws IOException {
            super();
        }
        
        void init() {
            if(skeySpec == null) {
                SecureRandom sr = new SecureRandom();
                ivBytes = new byte[16];
                byte[] keyBytes = new byte[16];
                sr.nextBytes(ivBytes);
                sr.nextBytes(keyBytes);
                skeySpec = new SecretKeySpec(keyBytes, cipherAlgorithm.jceId);
            }
        }

        @Override
        protected OutputStream decorateOutputStream(FileOutputStream fos) {
            init();
            Cipher ciEnc = CryptoFunctions.getCipher(skeySpec, cipherAlgorithm, ChainingMode.cbc, ivBytes, Cipher.ENCRYPT_MODE, "PKCS5Padding");
            return new CipherOutputStream(fos, ciEnc);
        }
        
        @Override
        protected InputStream decorateInputStream(FileInputStream fis) {
            Cipher ciDec = CryptoFunctions.getCipher(skeySpec, cipherAlgorithm, ChainingMode.cbc, ivBytes, Cipher.DECRYPT_MODE, "PKCS5Padding");
            return new CipherInputStream(fis, ciDec);
        }
    }
}
