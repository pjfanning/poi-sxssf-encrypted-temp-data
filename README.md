# poi-sxssf-encrypted-temp-data

When you produce an xlsx data using the [https://poi.apache.org/spreadsheet/how-to.html#sxssf](SXSSF) API, there are a number of temp files created. This sample demonstrates how to subclass SXSSFWorkbook so that the temp files can have encrypted data.
This code requires access to some methods that were private in POI 3.15. This is why the sample contains POI jars that were built from recent POI sources. 

## Running the sample code

- [Install SBT](http://www.scala-sbt.org/)
- `sbt "run <filename> <password>"`


The test code generates a sample document and saves it using the provided filename and password.
