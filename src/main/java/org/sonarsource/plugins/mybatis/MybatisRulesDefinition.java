package org.sonarsource.plugins.mybatis;

import org.antlr.sql.dialects.SQLDialectRules;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.debt.internal.DefaultDebtRemediationFunction;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.plugins.mybatis.rules.SqlRules;

import java.util.List;
import java.util.Objects;

public class MybatisRulesDefinition implements RulesDefinition {

    @Override
    public void define(Context context) {

        List<SqlRules> rules = SQLDialectRules.INSTANCE.getGroupedRules();

        for (SqlRules rulesDef : rules) {
            NewRepository repository = context.createRepository(rulesDef.getRepoKey(), Constants.languageKey)
                    .setName(rulesDef.getRepoName());

            for (org.sonarsource.plugins.mybatis.rules.Rule rule : rulesDef.getRule()) {
                NewRule x1Rule = repository.createRule(rule.getKey()).setName(rule.getName())
                        .setHtmlDescription(rule.getDescription()).addTags(rule.getTag())
                        .setSeverity(rule.getSeverity())
                        .setType(RuleType.valueOf(rule.getRuleType()));
                String gapMultiplier = rule.getDebtRemediationFunctionCoefficient();
                String baseEffort = rule.getRemediationFunctionBaseEffort();
                DebtRemediationFunction func = new DefaultDebtRemediationFunction(
                        DebtRemediationFunction.Type.valueOf(rule.getRemediationFunction()),
                        (Objects.equals(gapMultiplier, "")) ? null : gapMultiplier,
                        (Objects.equals(baseEffort, "")) ? null : baseEffort);
                x1Rule.setDebtRemediationFunction(func);
                x1Rule.setActivatedByDefault(true);

            }
            repository.done();

        }

    }

}
