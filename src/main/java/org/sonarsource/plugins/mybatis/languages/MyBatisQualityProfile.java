package org.sonarsource.plugins.mybatis.languages;

import org.sonar.api.rule.Severity;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.xml.Xml;

import static org.sonarsource.plugins.mybatis.rules.MyBatisLintRulesDefinition.REPO_KEY;

/**
 * Default, BuiltIn Quality Profile for the projects having files of the language "xml"
 */
public final class MyBatisQualityProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("MyBatisLint Rules", Xml.KEY);
        profile.setDefault(true);

        NewBuiltInActiveRule rule1 = profile.activateRule(REPO_KEY, "MyBatisMapperCheckRule01");
        rule1.overrideSeverity(Severity.MINOR);

        NewBuiltInActiveRule rule2 = profile.activateRule(REPO_KEY, "MyBatisMapperCheckRule02");
        rule2.overrideSeverity(Severity.MAJOR);

        NewBuiltInActiveRule rule3 = profile.activateRule(REPO_KEY, "MyBatisMapperCheckRule03");
        rule3.overrideSeverity(Severity.CRITICAL);

        NewBuiltInActiveRule rule4 = profile.activateRule(REPO_KEY, "MyBatisMapperCheckRule04");
        rule4.overrideSeverity(Severity.MINOR);

        NewBuiltInActiveRule rule5 = profile.activateRule(REPO_KEY, "MyBatisMapperCheckRule05");
        rule5.overrideSeverity(Severity.MAJOR);

        NewBuiltInActiveRule rule6 = profile.activateRule(REPO_KEY, "MyBatisMapperCheckRule06");
        rule6.overrideSeverity(Severity.CRITICAL);

        NewBuiltInActiveRule rule7 = profile.activateRule(REPO_KEY, "MyBatisMapperCheckRule07");
        rule7.overrideSeverity(Severity.MINOR);

        profile.done();
    }

}
