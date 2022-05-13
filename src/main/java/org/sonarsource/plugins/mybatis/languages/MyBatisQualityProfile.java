package org.sonarsource.plugins.mybatis.languages;

import org.sonar.api.rule.Severity;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.xml.Xml;
import org.sonarsource.plugins.mybatis.Constant;

import static org.sonarsource.plugins.mybatis.rules.MyBatisLintRulesDefinition.REPO_KEY;

/**
 * Default, BuiltIn Quality Profile for the projects having files of the language "xml"
 */
public final class MyBatisQualityProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("MyBatisLint Rules", Xml.KEY);
        profile.setDefault(true);

        NewBuiltInActiveRule rule01 = profile.activateRule(REPO_KEY, Constant.MYBATIS_MAPPER_CHECK_RULE_01);
        rule01.overrideSeverity(Severity.MINOR);

        NewBuiltInActiveRule rule02 = profile.activateRule(REPO_KEY, Constant.MYBATIS_MAPPER_CHECK_RULE_02);
        rule02.overrideSeverity(Severity.MAJOR);

        NewBuiltInActiveRule rule03 = profile.activateRule(REPO_KEY, Constant.MYBATIS_MAPPER_CHECK_RULE_03);
        rule03.overrideSeverity(Severity.CRITICAL);

        NewBuiltInActiveRule rule04 = profile.activateRule(REPO_KEY, Constant.MYBATIS_MAPPER_CHECK_RULE_04);
        rule04.overrideSeverity(Severity.MINOR);

        NewBuiltInActiveRule rule05 = profile.activateRule(REPO_KEY, Constant.MYBATIS_MAPPER_CHECK_RULE_05);
        rule05.overrideSeverity(Severity.MAJOR);

        NewBuiltInActiveRule rule06 = profile.activateRule(REPO_KEY, Constant.MYBATIS_MAPPER_CHECK_RULE_06);
        rule06.overrideSeverity(Severity.CRITICAL);

        NewBuiltInActiveRule rule07 = profile.activateRule(REPO_KEY, Constant.MYBATIS_MAPPER_CHECK_RULE_07);
        rule07.overrideSeverity(Severity.MINOR);

        profile.done();
    }

}
