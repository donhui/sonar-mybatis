package org.sonarsource.plugins.mybatis.rules;

import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.impl.utils.JUnitTempFolder;
import org.sonarsource.plugins.mybatis.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * <p>Title: MyBatisLintSensorTest</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: www.tul.com.cn</p>
 *
 * @author cuijing
 * @version 1.0
 * @date 2022/7/10 03:25
 */
public class MyBatisLintSensorTest {
    @Rule
    public TemporaryFolder folder = TemporaryFolder.builder().assureDeletion().build();
    @org.junit.Rule
    public JUnitTempFolder temp = new org.sonar.api.impl.utils.JUnitTempFolder();
    @Test
    public void testMySQL() throws IOException {
        SensorContextTester ctxTester = SensorContextTester.create(folder.getRoot());
        ctxTester.settings().setProperty(Constants.PLUGIN_SQL_DIALECT, "tsql");
        File baseFile = folder.newFile("test.xml");
        FileUtils.copyURLToFile(getClass().getResource("/mysql/OrderMapper.xml"), baseFile);

        String contents = new String(Files.readAllBytes(baseFile.toPath()));
        DefaultInputFile ti = new TestInputFileBuilder("test", "test.xml").initMetadata(contents).setContents(contents)
                .setLanguage(Constants.languageKey).build();
        ctxTester.fileSystem().add(ti);

        MyBatisLintSensor s = new MyBatisLintSensor();
        s.execute(ctxTester);

        Assert.assertEquals(5, ctxTester.allIssues().size());
        Assert.assertEquals(0, ctxTester.allExternalIssues().size());
        Assert.assertEquals(0, ctxTester.allAdHocRules().size());

    }
}