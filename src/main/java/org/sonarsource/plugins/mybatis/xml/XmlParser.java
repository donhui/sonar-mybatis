package org.sonarsource.plugins.mybatis.xml;

import org.apache.xerces.impl.Constants;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.xml.sax.SAXException;

import java.io.File;

public class XmlParser {
    private static final Logger LOGGER = Loggers.get(XmlParser.class);

    public Document parse(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        reader.setValidation(false);
        try {
            reader.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (SAXException e) {
            LOGGER.warn(e.toString());
        }
        return reader.read(file);
    }
}
