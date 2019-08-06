package org.sonarsource.plugins.mybatis.rules;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

import static org.sonarsource.plugins.mybatis.MyBatisPlugin.STMTID_EXCLUDE_KEY;

/**
 * The goal of this Sensor is analysis mybatis mapper files and generate issues.
 */
public class MyBatisLintSensor implements Sensor {

    private static final Logger LOGGER = Loggers.get(MyBatisLintSensor.class);

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
        String[] stmtIdExclude = config.getStringArray(STMTID_EXCLUDE_KEY);
        Collections.addAll(stmtIdExcludeList, stmtIdExclude);
        LOGGER.info("stmtIdExcludeList: " + stmtIdExcludeList.toString());
        // analysis mybatis mapper files and generate issues
        Map mybatisMapperMap = new HashMap(16);

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
                    String dstXmlFilePath = xmlFilePath + "-reduced.xml";
                    File dstXmlFile = new File(dstXmlFilePath);
                    MyBatisMapperXmlHandler myBatisMapperXmlHandler = new MyBatisMapperXmlHandler();
                    myBatisMapperXmlHandler.handleMapperFile(xmlFile, dstXmlFile);
                    mybatisMapperMap.put(dstXmlFilePath, xmlFilePath);
                    // xmlMapperBuilder parse mapper resource
                    Resource mapperResource = new FileSystemResource(dstXmlFile);
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
                            String mapperResource = stmt.getResource();
                            String dstMapperFilePath =
                                mapperResource.substring(mapperResource.indexOf('[') + 1, mapperResource.indexOf(']'));
                            String sourceMapperFilePath = (String)mybatisMapperMap.get(dstMapperFilePath);

                            LOGGER.info("id=" + stmtId + ",");
                            LOGGER.info("sql=" + sql);

                            if (stmtIdExcludeList.contains(stmtId)) {
                                LOGGER.info("stmt id exclude:" + stmtId);
                            } else {
                                // get lineNumber by mapper file and keyWord
                                String[] stmtIdSplit = stmtId.split("\\.");
                                String stmtIdTail = stmtIdSplit[stmtIdSplit.length - 1];

                                String sqlCmdType = stmt.getSqlCommandType().toString().toLowerCase();

                                Integer lineNumber = getLineNumber(sourceMapperFilePath, stmtIdTail, sqlCmdType);

                                // match Rule And Save Issue
                                matchRuleAndSaveIssue(sql, sourceMapperFilePath, lineNumber);
                            }
                        }
                    }
                }
            }
        }

    }

    private Integer getLineNumber(String filePath, String stmtIdTail, String sqlCmdType) {
        String keyWordWithSingleQuot = stmtIdTail + "\'";
        Integer lineNumber = IOUtils.getLineNumber(filePath, keyWordWithSingleQuot, sqlCmdType);
        if (null == lineNumber) {
            String keyWordWithDoubleQuot = stmtIdTail + "\"";
            lineNumber = IOUtils.getLineNumber(filePath, keyWordWithDoubleQuot, sqlCmdType);
        }
        return lineNumber;
    }

    private void matchRuleAndSaveIssue(String sql, String sourceMapperFilePath, Integer lineNumber) {
        sql = sql.toLowerCase();
        String errorMessage = "";
        String ruleId = "";
        if (containsOneEqualsOne(sql)) {
            if (sql.startsWith("delete")) {
                // delete statement contains 1=1
                errorMessage = "delete statement should not include 1=1";
                ruleId = "MyBatisMapperCheckRule3";
            } else if (sql.startsWith("update")) {
                // update statement contains 1=1
                errorMessage = "update statement should not include 1=1";
                ruleId = "MyBatisMapperCheckRule2";
            } else if (sql.startsWith("select") && !containsFunctionOrLimit(sql)) {
                // select statement contains 1=1
                errorMessage = "select statement should not include 1=1";
                ruleId = "MyBatisMapperCheckRule1";
            }
        } else if (!sql.contains("where")) {
            if (sql.startsWith("delete")) {
                // delete statement may not has where condition
                errorMessage = "delete statement may not has where condition";
                ruleId = "MyBatisMapperCheckRule6";
            } else if (sql.startsWith("update")) {
                // update statement may not has where condition
                errorMessage = "update statement may not has where condition";
                ruleId = "MyBatisMapperCheckRule5";
            } else if (sql.startsWith("select") && !containsFunctionOrLimit(sql)) {
                // select statement may not has where condition
                errorMessage = "select statement may not has where condition";
                ruleId = "MyBatisMapperCheckRule4";
            }
        }

        if (!"".equals(ruleId)) {
            ErrorDataFromLinter mybatisError =
                new ErrorDataFromLinter(ruleId, errorMessage, sourceMapperFilePath, lineNumber);
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

        NewIssue newIssue = context.newIssue().forRule(ruleKey);

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

    private static class ErrorDataFromLinter {

        private final String externalRuleId;
        private final String issueMessage;
        private final String filePath;
        private final int line;

        public ErrorDataFromLinter(final String externalRuleId, final String issueMessage, final String filePath,
            final int line) {
            this.externalRuleId = externalRuleId;
            this.issueMessage = issueMessage;
            this.filePath = filePath;
            this.line = line;
        }

        public String getType() {
            return externalRuleId;
        }

        public String getDescription() {
            return issueMessage;
        }

        public String getFilePath() {
            return filePath;
        }

        public int getLine() {
            return line;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append(externalRuleId);
            s.append("|");
            s.append(issueMessage);
            s.append("|");
            s.append(filePath);
            s.append("(");
            s.append(line);
            s.append(")");
            return s.toString();
        }
    }

}
