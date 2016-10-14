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

import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;


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

public class SxssfSavePasswordProtectedXlsx {
    
    public static void main(String[] args) {
        try {
            if(args.length != 2) {
                throw new Exception("Expected 2 params: filename and password");
            }
            XlsxUtils.checkTempFiles();
            String filename = args[0];
            String password = args[1];
            SXSSFWorkbookWithCustomZipEntrySource wb = new SXSSFWorkbookWithCustomZipEntrySource();
            try {
                for(int i = 0; i < 10; i++) {
                    SXSSFSheet sheet = wb.createSheet("Sheet" + i);
                    for(int r = 0; r < 1000; r++) {
                        SXSSFRow row = sheet.createRow(r);
                        for(int c = 0; c < 100; c++) {
                            SXSSFCell cell = row.createCell(c);
                            cell.setCellValue("abcd");
                        }
                    }
                }
                EncryptedTempData tempData = new EncryptedTempData();
                try {
                    wb.write(tempData.getOutputStream());
                    XlsxUtils.save(tempData.getInputStream(), filename, password);
                    System.out.println("Saved " + filename);
                } finally {
                    tempData.dispose();
                }
            } finally {
                wb.close();
                wb.dispose();
            }
            XlsxUtils.checkTempFiles();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
}