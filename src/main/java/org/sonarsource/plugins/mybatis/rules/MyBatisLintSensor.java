package org.sonarsource.plugins.mybatis.rules;

import static org.sonarsource.plugins.mybatis.MyBatisPlugin.SONAR_MYBATIS_SKIP;
import static org.sonarsource.plugins.mybatis.MyBatisPlugin.STMTID_EXCLUDE_KEY;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
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
import org.sonarsource.plugins.mybatis.utils.IOUtils;
import org.sonarsource.plugins.mybatis.xml.MyBatisMapperXmlHandler;
import org.sonarsource.plugins.mybatis.xml.XmlParser;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * The goal of this Sensor is analysis mybatis mapper files and generate issues.
 */
public class MyBatisLintSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(MyBatisLintSensor.class);

    private static final String LEFT_SLASH = "/";
    private static final String SELECT = "select";
    private static final String UPDATE = "update";
    private static final String DELETE = "delete";
    private static final String WHERE = "where";
    private static final String COUNT_STAR = "count(*)";
    private static final String STAR = "*";

    protected final Configuration config;
    protected final FileSystem fileSystem;
    protected SensorContext context;
    private List<String> stmtIdExcludeList = new ArrayList<>();

    /**
     * Use of IoC to get Settings, FileSystem, RuleFinder and ResourcePerspectives
     */
    public MyBatisLintSensor(final Configuration config, final FileSystem fileSystem) {
        this.config = config;
        this.fileSystem = fileSystem;
    }

    @Override
    public void describe(final SensorDescriptor descriptor) {
        descriptor.name("MyBatisLint Sensor");
        descriptor.onlyOnLanguage(Xml.KEY);
    }

    @Override
    public void execute(final SensorContext context) {
        this.context = context;
        Boolean sonarMyBatisSkipBooleanValue = Boolean.valueOf(false);
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
        Map mybatisMapperMap = new HashMap(16);
        List<File> reducedFileList = new ArrayList<>();

        org.apache.ibatis.session.Configuration mybatisConfiguration = new org.apache.ibatis.session.Configuration();

        // handle mybatis mapper file and add it to mybatisConfiguration
        FileSystem fs = context.fileSystem();
        Iterable<InputFile> xmlInputFiles = fs.inputFiles(fs.predicates().hasLanguage("xml"));
        for (InputFile xmlInputFile : xmlInputFiles) {
            String xmlFilePath = xmlInputFile.uri().getPath();
            File xmlFile = new File(xmlFilePath);
            try {
                XmlParser xmlParser = new XmlParser();
                Document document = xmlParser.parse(xmlFile);
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
                    String reducedXmlFilePath = xmlFilePath + "-reduced.xml";
                    File reducedXmlFile = new File(reducedXmlFilePath);
                    reducedFileList.add(reducedXmlFile);
                    MyBatisMapperXmlHandler myBatisMapperXmlHandler = new MyBatisMapperXmlHandler();
                    myBatisMapperXmlHandler.handleMapperFile(xmlFile, reducedXmlFile);
                    mybatisMapperMap.put(reducedXmlFilePath, xmlFilePath);
                    // xmlMapperBuilder parse mapper resource
                    Resource mapperResource = new FileSystemResource(reducedXmlFile);
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperResource.getInputStream(),
                        mybatisConfiguration, mapperResource.toString(), mybatisConfiguration.getSqlFragments());
                    xmlMapperBuilder.parse();
                }
            } catch (DocumentException | IOException e) {
                LOGGER.warn(e.toString());
            }
        }

        // parse MappedStatements
        Set<MappedStatement> stmts = new HashSet<>(mybatisConfiguration.getMappedStatements());
        parseStatement(stmts, mybatisMapperMap);

        // clean reduced.xml
        cleanFiles(reducedFileList);
    }

    private void parseStatement(Set<MappedStatement> stmts, Map mybatisMapperMap) {
        for (Iterator<MappedStatement> iter = stmts.iterator(); iter.hasNext();) {
            MappedStatement stmt = null;
            try {
                stmt = iter.next();
            } catch (Exception e) {
                LOGGER.warn(e.toString());
            }
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
                            sql = sql.replaceAll("\\n", "");
                            sql = sql.replaceAll("\\s{2,}", " ");
                            final String mapperResource = stmt.getResource();
                            String reducedXmlFilePath = mapperResource.substring(mapperResource.indexOf('[') + 1,
                                    mapperResource.indexOf(']'));

                            // windows environment
                            if (!reducedXmlFilePath.startsWith(LEFT_SLASH)) {
                                reducedXmlFilePath = LEFT_SLASH + reducedXmlFilePath.replace("\\", LEFT_SLASH);
                            }
                            LOGGER.debug("reducedMapperFilePath: " + reducedXmlFilePath);

                            final String sourceMapperFilePath = (String) mybatisMapperMap.get(reducedXmlFilePath);

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
                                final int lineNumber = getLineNumber(sourceMapperFilePath, stmtIdTail, sqlCmdType);
                                // match Rule And Save Issue
                                matchRuleAndSaveIssue(sql, sourceMapperFilePath, lineNumber);
                            }
                        }
                    }
                }
            }
        }

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

    private int getLineNumber(final String filePath, final String stmtIdTail, final String sqlCmdType) {
        return IOUtils.getLineNumber(filePath, stmtIdTail, sqlCmdType);
    }

    private void matchRuleAndSaveIssue(String sql, String sourceMapperFilePath, Integer lineNumber) {
        sql = sql.toLowerCase();
        String errorMessage = "";
        String ruleId = "";
        if (containsOneEqualsOne(sql)) {
            if (sql.startsWith(DELETE)) {
                // delete statement contains 1=1
                errorMessage = "delete statement should not include 1=1";
                ruleId = "MyBatisMapperCheckRule3";
            } else if (sql.startsWith(UPDATE)) {
                // update statement contains 1=1
                errorMessage = "update statement should not include 1=1";
                ruleId = "MyBatisMapperCheckRule2";
            } else if (sql.startsWith(SELECT) && !containsFunctionOrLimit(sql)) {
                // select statement contains 1=1
                errorMessage = "select statement should not include 1=1";
                ruleId = "MyBatisMapperCheckRule1";
            }
        } else if (!sql.contains(WHERE)) {
            if (sql.startsWith(DELETE)) {
                // Where condition not found in delete statement
                errorMessage = "where condition not found in delete statement";
                ruleId = "MyBatisMapperCheckRule6";
            } else if (sql.startsWith(UPDATE)) {
                // Where condition not found in update statement
                errorMessage = "where condition not found in update statement";
                ruleId = "MyBatisMapperCheckRule5";
            } else if (sql.startsWith(SELECT) && !containsFunctionOrLimit(sql)) {
                // Where condition not found in select statement
                errorMessage = "where condition not found in select statement";
                ruleId = "MyBatisMapperCheckRule4";
            }
        }

        if (sql.startsWith(SELECT) && sql.contains(STAR)) {
            sql = sql.replace(" ", "");
            if (!sql.contains(COUNT_STAR)) {
                errorMessage = "select statement should not include *";
                ruleId = "MyBatisMapperCheckRule7";
            }
        }

        if (!"".equals(ruleId)) {
            LOGGER.debug("ruleId=" + ruleId + " errorMessage=" + errorMessage + " filePath=" + sourceMapperFilePath + " line="
                    + lineNumber);
            ErrorDataFromLinter mybatisError = new ErrorDataFromLinter(ruleId, errorMessage, sourceMapperFilePath,
                    lineNumber);
            getResourceAndSaveIssue(mybatisError);
        }
    }

    private boolean containsOneEqualsOne(String sql) {
        return sql.contains("1=1") || sql.contains("1 = 1") || sql.contains("1= 1") || sql.contains("1 =1");
    }

    private boolean containsFunctionOrLimit(String sql) {
        return sql.contains("sum(") || sql.contains("count(") || sql.contains("max(") || sql.contains("min(")
            || sql.contains("limit");
    }

    private void getResourceAndSaveIssue(final ErrorDataFromLinter error) {
        LOGGER.debug(error.toString());

        final FileSystem fs = context.fileSystem();
        final InputFile inputFile = fs.inputFile(fs.predicates().hasAbsolutePath(error.getFilePath()));

        LOGGER.debug("inputFile null ? " + (inputFile == null));

        if (inputFile != null) {
            saveIssue(inputFile, error.getLine(), error.getType(), error.getDescription());
        } else {
            LOGGER.error("Not able to find a InputFile with " + error.getFilePath());
        }
    }

    private void saveIssue(final InputFile inputFile, int line, final String externalRuleKey, final String message) {
        RuleKey ruleKey = RuleKey.of(getRepositoryKeyForLanguage(), externalRuleKey);

        NewIssue newIssue = context.newIssue().forRule(ruleKey).gap(2.0);

        NewIssueLocation primaryLocation = newIssue.newLocation().on(inputFile).message(message);
        if (line > 0) {
            primaryLocation.at(inputFile.selectLine(line));
        }
        newIssue.at(primaryLocation);

        newIssue.save();
    }

    private static String getRepositoryKeyForLanguage() {
        return MyBatisLintRulesDefinition.REPO_KEY;
    }

    @Override
    public String toString() {
        return "MyBatisLintSensor";
    }

}
