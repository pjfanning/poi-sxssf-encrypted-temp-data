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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;

public class XlsxUtils {
    public static void save(final InputStream inputStream, final String filename, final String pwd)
            throws InvalidFormatException, IOException, GeneralSecurityException {
        try {
            POIFSFileSystem fs = new POIFSFileSystem();
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor enc = Encryptor.getInstance(info);
            enc.confirmPassword(pwd);
            try(OPCPackage opc = OPCPackage.open(inputStream);
                    FileOutputStream fos = new FileOutputStream(filename)) {
                opc.save(enc.getDataStream(fs));
                fs.writeFilesystem(fos);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
    
    public static void checkTempFiles() throws IOException {
        String tmpDir = System.getProperty(TempFile.JAVA_IO_TMPDIR) + "/poifiles";
        File tempDir = new File(tmpDir);
        if(tempDir.exists()) {
            String[] tempFiles = tempDir.list();
            if(tempFiles.length > 0) {
                System.out.println("found files in poi temp dir " + tempDir.getAbsolutePath());
                for(String filename : tempDir.list()) {
                    System.out.println("file: " + filename);
                }
            }
        } else {
            System.out.println("unable to find poi temp dir");
        }
    }
    
}
