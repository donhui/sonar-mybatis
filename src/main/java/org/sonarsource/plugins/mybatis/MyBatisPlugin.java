package org.sonarsource.plugins.mybatis;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonarsource.plugins.mybatis.hooks.DisplayIssuesInScanner;
import org.sonarsource.plugins.mybatis.hooks.DisplayQualityGateStatus;
import org.sonarsource.plugins.mybatis.languages.MyBatisQualityProfile;
import org.sonarsource.plugins.mybatis.rules.MyBatisLintSensor;
import org.sonarsource.plugins.mybatis.rules.MyBatisLintRulesDefinition;

/**
 * This class is the entry point for all extensions. It is referenced in pom.xml.
 */
public class MyBatisPlugin implements Plugin {
    public static final String STMTID_EXCLUDE_KEY = "sonar.mybatis.stmtid.exclude";
    private static final String MYBATIS_CATEGORY = "MyBatis";
    private static final String GENERAL_SUBCATEGORY = "General";


    @Override
    public void define(Context context) {
        // hooks
        // http://docs.sonarqube.org/display/DEV/Adding+Hooks
        context.addExtensions(DisplayIssuesInScanner.class, DisplayQualityGateStatus.class);

        // qualityprofile
        context.addExtension(MyBatisQualityProfile.class);

        // rules
        context.addExtensions(MyBatisLintRulesDefinition.class, MyBatisLintSensor.class);

        // property
        context.addExtension(PropertyDefinition.builder(STMTID_EXCLUDE_KEY)
                .name("Statement ID Exclude")
                .description("Comma-separated list of statement id exclude.")
                .subCategory(GENERAL_SUBCATEGORY)
                .category(MYBATIS_CATEGORY)
                .onQualifiers(Qualifiers.PROJECT)
                .multiValues(true)
                .build());
    }
}
