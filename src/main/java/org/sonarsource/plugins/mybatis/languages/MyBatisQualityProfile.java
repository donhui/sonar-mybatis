package org.sonarsource.plugins.mybatis.languages;

import org.antlr.sql.dialects.SQLDialectRules;
import org.sonar.api.rule.Severity;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.xml.Xml;
import org.sonarsource.plugins.mybatis.Constant;
import org.sonarsource.plugins.mybatis.Constants;
import org.sonarsource.plugins.mybatis.rules.Rule;
import org.sonarsource.plugins.mybatis.rules.SqlRules;

import java.util.List;

import static org.sonarsource.plugins.mybatis.rules.MyBatisLintRulesDefinition.REPO_KEY;

/**
 * Default, BuiltIn Quality Profile for the projects having files of the language "xml"
 */
public final class MyBatisQualityProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(Context context) {
        final NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("SQL rules", Constants.languageKey)
                .setDefault(true);
        final List<SqlRules> rules = SQLDialectRules.INSTANCE.getGroupedRules();

        for (SqlRules sqlRules : rules) {
            for (Rule rule : sqlRules.getRule()) {
                profile.activateRule(sqlRules.getRepoKey(), rule.getKey());
            }
        }
        profile.done();
    }

}
