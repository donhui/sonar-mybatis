package org.sonarsource.plugins.mybatis;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonarsource.plugins.mybatis.languages.MyBatisQualityProfile;
import org.sonarsource.plugins.mybatis.rules.MyBatisLintSensor;
import org.sonarsource.plugins.mybatis.rules.MyBatisLintRulesDefinition;

/**
 * This class is the entry point for all extensions. It is referenced in pom.xml.
 */
public class MyBatisPlugin implements Plugin {
    public static final String STMTID_EXCLUDE_KEY = "sonar.mybatis.stmtid.exclude";
    public static final String SONAR_MYBATIS_SKIP = "sonar.mybatis.skip";
    private static final String MYBATIS_CATEGORY = "MyBatis";
    private static final String GENERAL_SUBCATEGORY = "General";

    @Override
    public void define(Context context) {

        // qualityprofile
        context.addExtension(MyBatisQualityProfile.class);

        // rules
        context.addExtensions(MyBatisLintRulesDefinition.class, MyBatisLintSensor.class);

        // property
        context.addExtension(PropertyDefinition.builder(STMTID_EXCLUDE_KEY).name("Statement ID Exclude")
            .description("Comma-separated list of statement id exclude.").subCategory(GENERAL_SUBCATEGORY)
            .category(MYBATIS_CATEGORY).onQualifiers(Qualifiers.PROJECT).multiValues(true).build());
        context.addExtension(
            PropertyDefinition.builder(SONAR_MYBATIS_SKIP).name("Sonar MyBatis Skip").defaultValue("false")
                .subCategory(GENERAL_SUBCATEGORY).category(MYBATIS_CATEGORY).onQualifiers(Qualifiers.PROJECT).build());
    }
}
