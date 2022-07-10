package org.sonarsource.plugins.mybatis.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class IOUtils {

    private IOUtils() {
        super();
    }

    private static final Logger LOGGER = Loggers.get(IOUtils.class);

    /**
     * Search file, get the line number which matches the keyword firstly
     * 
     * @param filePath
     * @param stmtId
     * @param sqlCmdType
     * @return
     */
    public static int getLineNumber(final InputStream fileStream, final String stmtId, final String sqlCmdType) {
        int lineNumber = 1;// numero mayor a cero en caso que "no se llame"
        final String sqlCmdTypeAux = "<" + sqlCmdType;
        final String stmtIdAuxDoublequote = stmtId + "\"";
        final String stmtIdAuxSimplequote = stmtId + "'";
        try (LineNumberReader lineNumberReader = new LineNumberReader(
                new InputStreamReader(fileStream, StandardCharsets.UTF_8))) {
            String readLine = null;
            while ((readLine = lineNumberReader.readLine()) != null) {
                // check if contains
                if (readLine.toLowerCase().contains(sqlCmdTypeAux)
                        && (readLine.contains(stmtIdAuxDoublequote) || readLine.contains(stmtIdAuxSimplequote))) {
                    lineNumber = lineNumberReader.getLineNumber();
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.toString());
        }
        LOGGER.debug("lineNumber found: " + lineNumber);
        if (0 == lineNumber) {
            LOGGER.warn("dosnt found line number.");
        }
        return lineNumber;
    }
}
