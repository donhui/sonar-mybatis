package org.sonarsource.plugins.mybatis.utils;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.*;

public class IOUtils {

    private IOUtils() {}

    private static final Logger LOGGER = Loggers.get(IOUtils.class);

    /**
     * search file, get the line number which matches the keyword firstly
     * 
     * @param filePath
     * @param keyWord1
     * @param keyWord2
     * @return
     */
    public static Integer getLineNumber(String filePath, String keyWord1, String keyWord2) {
        Integer lineNumber = null;
        // lineNumberReader
        LineNumberReader lineNumberReader = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            lineNumberReader = new LineNumberReader(inputStreamReader);
            String readLine = null;
            while ((readLine = lineNumberReader.readLine()) != null) {
                // check if contains
                if (readLine.contains(keyWord1) && readLine.contains(keyWord2)) {
                    lineNumber = lineNumberReader.getLineNumber();
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.warn(e.toString());
        } finally {
            // close stream
            closeStream(lineNumberReader);
            closeStream(inputStreamReader);
            closeStream(fileInputStream);
        }
        return lineNumber;
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                LOGGER.warn(e.toString());
                stream = null;
            }
        }
    }
}
