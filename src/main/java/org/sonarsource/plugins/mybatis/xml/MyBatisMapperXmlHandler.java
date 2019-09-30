package org.sonarsource.plugins.mybatis.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class MyBatisMapperXmlHandler {

    private void handleMapperElement(Document document) {
        Element root = document.getRootElement();
        // iterate through child elements of root
        List<Element> elementList = root.elements();
        for (Element element : elementList) {
            if ("resultMap".equals(element.getName()) || "parameterMap".equals(element.getName())
                || "cache".equals(element.getName()) || "cache-ref".equals(element.getName())) {
                // remove resultMap,parameterMap,cache,cache-ref element
                root.remove(element);
            } else {
                // handle attributes
                List<Attribute> attributeList = element.attributes();
                List<Attribute> toRemoveAttributeList = new ArrayList();
                if (!attributeList.isEmpty()) {
                    for (Attribute attribute : attributeList) {
                        // generate toRemoveAttributeList
                        if ("parameterType".equals(attribute.getName()) || "resultMap".equals(attribute.getName())
                            || "resultType".equals(attribute.getName()) || "parameterMap".equals(attribute.getName())) {
                            toRemoveAttributeList.add(attribute);
                        }
                    }
                }
                if (!toRemoveAttributeList.isEmpty()) {
                    for (Attribute attribute : toRemoveAttributeList) {
                        // remove some attributes
                        element.remove(attribute);
                    }
                }
                // handle selectKey child element
                List<Element> childElementList = element.elements();
                for(Element childElement : childElementList){
                    if("selectKey".equals(childElement.getName())) {
                        element.remove(childElement);
                    }
                }

            }
            // handle TypeHandler,javaType,jdbcType,resultMap
            String text = element.getText();
            text = text.replaceAll(",[\\s]*typeHandler=[.*A-Za-z0-9_=,]*}", "}");
            text = text.replaceAll(",[\\s]*javaType=[.A-Za-z0-9_=,]*}", "}");
            text = text.replaceAll(",[\\s]*jdbcType=[.A-Za-z0-9_=,]*}", "}");
            text = text.replaceAll(",[\\s]*resultMap=[.A-Za-z0-9_=,]*}", "}");
            element.setText(text);
        }

        // handle `test` attribute of `if` or `when` element
        List<Element> ifElements = document.selectNodes("//if");
        List<Element> whenElements = document.selectNodes("//when");
        List<Element> ifAndWhenElements = new ArrayList<>();
        ifAndWhenElements.addAll(ifElements);
        ifAndWhenElements.addAll(whenElements);
        for (Element element : ifAndWhenElements) {
            List<Attribute> attributeList = element.attributes();
            if (!attributeList.isEmpty()) {
                for (Attribute attribute : attributeList) {
                    // replace test attribute value
                    if ("test".equals(attribute.getName())) {
                        attribute.setValue(attribute.getValue().replaceAll("\\.", "-"));
                    }
                }
            }
        }
    }

    public void handleMapperFile(File sourceFile, File dstFile) throws DocumentException, IOException {
        XmlParser xmlParser = new XmlParser();
        Document document = xmlParser.parse(sourceFile);
        Element root = document.getRootElement();
        if ("mapper".equals(root.getName())) {
            handleMapperElement(document);
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(new FileOutputStream(dstFile), format);
            writer.write(document);
            writer.close();
        }
    }

}
