package org.sonarsource.plugins.mybatis.languages;

import org.antlr.sql.dialects.SQLDialectRules;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.plugins.mybatis.Constants;
import org.sonarsource.plugins.mybatis.rules.Rule;
import org.sonarsource.plugins.mybatis.rules.SqlRules;

import java.util.List;

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
