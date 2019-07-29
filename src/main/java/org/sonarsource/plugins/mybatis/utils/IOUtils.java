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
        try (LineNumberReader lineNumberReader =
            new LineNumberReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
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
        }
        return lineNumber;
    }
}
