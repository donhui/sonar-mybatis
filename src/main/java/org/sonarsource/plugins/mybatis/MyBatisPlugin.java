package org.sonarsource.plugins.mybatis;

import org.sonar.api.Plugin;
import org.sonarsource.plugins.mybatis.hooks.DisplayIssuesInScanner;
import org.sonarsource.plugins.mybatis.hooks.DisplayQualityGateStatus;
import org.sonarsource.plugins.mybatis.languages.MyBatisQualityProfile;
import org.sonarsource.plugins.mybatis.rules.MyBatisLintSensor;
import org.sonarsource.plugins.mybatis.rules.MyBatisLintRulesDefinition;

/**
 * This class is the entry point for all extensions. It is referenced in pom.xml.
 */
public class MyBatisPlugin implements Plugin {

    @Override
    public void define(Context context) {
        // hooks
        // http://docs.sonarqube.org/display/DEV/Adding+Hooks
        context.addExtensions(DisplayIssuesInScanner.class, DisplayQualityGateStatus.class);

        // qualityprofile
        context.addExtension(MyBatisQualityProfile.class);

        // rules
        context.addExtensions(MyBatisLintRulesDefinition.class, MyBatisLintSensor.class);
    }
}
