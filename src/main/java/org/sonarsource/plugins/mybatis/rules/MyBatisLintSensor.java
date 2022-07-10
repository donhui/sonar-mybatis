package org.sonarsource.plugins.mybatis.rules;

import org.antlr.sql.dialects.Dialects;
import org.antlr.sql.models.AntlrContext;
import org.apache.commons.lang.StringUtils;
import org.sonarsource.plugins.mybatis.builder.xml.XMLMapperBuilder;
import org.sonarsource.plugins.mybatis.mapping.BoundSql;
import org.sonarsource.plugins.mybatis.mapping.MappedStatement;
import org.sonarsource.plugins.mybatis.mapping.SqlCommandType;
import org.sonarsource.plugins.mybatis.mapping.SqlSource;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.xml.Xml;
import org.sonarsource.plugins.mybatis.Constant;
import org.sonarsource.plugins.mybatis.Constants;
import org.sonarsource.plugins.mybatis.fillers.Filler;
import org.sonarsource.plugins.mybatis.fillers.IssuesFiller;
import org.sonarsource.plugins.mybatis.sensors.BaseSensor;
import org.sonarsource.plugins.mybatis.utils.IOUtils;
import org.sonarsource.plugins.mybatis.xml.MyBatisMapperXmlHandler;
import org.sonarsource.plugins.mybatis.xml.XmlParser;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.sonarsource.plugins.mybatis.MyBatisPlugin.SONAR_MYBATIS_SKIP;
import static org.sonarsource.plugins.mybatis.MyBatisPlugin.STMTID_EXCLUDE_KEY;

/**
 * The goal of this Sensor is analysis mybatis mapper files and generate issues.
 */
public class MyBatisLintSensor extends BaseSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(MyBatisLintSensor.class);

    private static final String LEFT_SLASH = "/";

    protected SensorContext context;
    private List<String> stmtIdExcludeList = new ArrayList<>();
    private final Filler filler =  new IssuesFiller();




    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.name("MyBatisLint Sensor");
        descriptor.onlyOnLanguage(Xml.KEY);
    }

    @Override
    public void execute(final SensorContext context) {
        this.context = context;
        final Configuration config = context.config();

        final String dialect = config.get(Constants.PLUGIN_SQL_DIALECT).orElse("mysql").toUpperCase();

        final Dialects sqlDialect = Dialects.valueOf(dialect.toUpperCase());

        Boolean sonarMyBatisSkipBooleanValue = Boolean.FALSE;
        Optional<Boolean> sonarMyBatisSkipValue = config.getBoolean(SONAR_MYBATIS_SKIP);
        if (sonarMyBatisSkipValue.isPresent()) {
            sonarMyBatisSkipBooleanValue = sonarMyBatisSkipValue.get();
        }
        if (Boolean.TRUE.equals(sonarMyBatisSkipBooleanValue)) {
            LOGGER.info("MyBatis sensor is skiped.");
            return;
        }
        String[] stmtIdExclude = config.getStringArray(STMTID_EXCLUDE_KEY);
        Collections.addAll(stmtIdExcludeList, stmtIdExclude);
        LOGGER.info("stmtIdExcludeList: " + stmtIdExcludeList.toString());
        // analysis mybatis mapper files and generate issues
        Map<String, InputFile> mybatisMapperMap = new HashMap<>(16);
        List<File> reducedFileList = new ArrayList<>();
        // handle mybatis mapper file and add it to mybatisConfiguration
        FileSystem fs = context.fileSystem();
        Iterable<InputFile> xmlInputFiles = fs.inputFiles(fs.predicates().hasLanguage("xml"));
        org.sonarsource.plugins.mybatis.session.Configuration mybatisConfiguration = new org.sonarsource.plugins.mybatis.session.Configuration();
        for (InputFile xmlInputFile : xmlInputFiles) {
            String xmlFilePath = xmlInputFile.uri().getPath();
            try {
                InputStream inputStream = new ByteArrayInputStream(xmlInputFile.contents().getBytes());
                XmlParser xmlParser = new XmlParser();
                Document document = xmlParser.parse(inputStream);
                Element rootElement = document.getRootElement();
                String publicIdOfDocType = "";
                DocumentType documentType = document.getDocType();
                if (null != documentType) {
                    publicIdOfDocType = documentType.getPublicID();
                    if (null == publicIdOfDocType) {
                        publicIdOfDocType = "";
                    }
                }
                if ("mapper".equals(rootElement.getName()) && publicIdOfDocType.contains("mybatis.org")) {
                    LOGGER.info("handle mybatis mapper xml:" + xmlFilePath);
                    // handle mybatis mapper file
                    inputStream.reset();
                    mybatisMapperMap.put(xmlFilePath, xmlInputFile);
                    System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");

                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream,
                        mybatisConfiguration, xmlFilePath, mybatisConfiguration.getSqlFragments());
                    xmlMapperBuilder.parse();
                }
            } catch (DocumentException | IOException e) {
                LOGGER.warn(e.toString());
            }
        }
        // parse MappedStatements
        Set<MappedStatement> stmts = new HashSet<MappedStatement>(mybatisConfiguration.getMappedStatements());
        parseStatement(stmts, mybatisMapperMap, sqlDialect);

        // clean reduced.xml
        cleanFiles(reducedFileList);
    }

    private void parseStatement(Set<MappedStatement> stmts, Map<String, InputFile> mybatisMapperMap, Dialects sqlDialect) {
        stmts.forEach(stmt -> {
            if (null != stmt) {
                if (stmt.getSqlCommandType() == SqlCommandType.SELECT
                        || stmt.getSqlCommandType() == SqlCommandType.UPDATE
                        || stmt.getSqlCommandType() == SqlCommandType.DELETE) {
                    SqlSource sqlSource = stmt.getSqlSource();
                    BoundSql boundSql = null;
                    try {
                        boundSql = sqlSource.getBoundSql(null);
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage());
                    }
                    if (null != boundSql) {
                        String sql = boundSql.getSql();
                        String stmtId = stmt.getId();
                        if (!StringUtils.endsWith(stmtId, "!selectKey")) {
                            String reducedXmlFilePath = stmt.getResource();
                            // windows environment
                            if (!reducedXmlFilePath.startsWith(LEFT_SLASH)) {
                                reducedXmlFilePath = LEFT_SLASH + reducedXmlFilePath.replace("\\", LEFT_SLASH);
                            }
                            LOGGER.debug("reducedMapperFilePath: " + reducedXmlFilePath);

                            final InputFile sourceMapperFilePath = mybatisMapperMap.get(reducedXmlFilePath);

                            LOGGER.info("stmtId=" + stmtId);
                            LOGGER.info("sql=" + sql);

                            if (stmtIdExcludeList.contains(stmtId)) {
                                LOGGER.info("stmt id exclude:" + stmtId);
                            } else {
                                // get lineNumber by mapper file and keyWord
                                final String[] stmtIdSplit = stmtId.split("\\.");
                                final String stmtIdTail = stmtIdSplit[stmtIdSplit.length - 1];
                                final String sqlCmdType = stmt.getSqlCommandType().toString().toLowerCase();
                                LOGGER.debug("sourceMapperFilePath: " + sourceMapperFilePath + " stmtIdTail:  "
                                        + stmtIdTail + " sqlCmdType: " + sqlCmdType);
                                final int lineNumber;
                                try {
                                    InputStream inputStream = new ByteArrayInputStream(sourceMapperFilePath.contents().getBytes());
                                    lineNumber = getLineNumber(inputStream, stmtIdTail, sqlCmdType);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                final AntlrContext ctx = sqlDialect.parse(sql, new ArrayList<>());
                                filler.fill(sourceMapperFilePath, context, ctx, lineNumber);

                            }
                        }
                    }
                }
            }
        });
    }

    private void cleanFiles(List<File> files) {
        for (File file : files) {
            if (file.exists() && file.isFile()) {
                try {
                    Files.delete(Paths.get(new URI("file:///" + file.getAbsolutePath().replace("\\", LEFT_SLASH))));
                } catch (IOException | URISyntaxException e) {
                    LOGGER.warn(e.toString());
                }
            }
        }
    }

    private int getLineNumber(final InputStream fileStream, final String stmtIdTail, final String sqlCmdType) {
        return IOUtils.getLineNumber(fileStream, stmtIdTail, sqlCmdType);
    }

    @Override
    public String toString() {
        return "MyBatisLintSensor";
    }

}
