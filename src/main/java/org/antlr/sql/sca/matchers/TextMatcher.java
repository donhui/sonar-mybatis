package org.antlr.sql.sca.matchers;

import java.util.List;

import org.antlr.sql.sca.nodes.IParsedNode;
import org.apache.commons.lang3.StringUtils;
import org.sonarsource.plugins.mybatis.rules.RuleImplementation;
import org.sonarsource.plugins.mybatis.rules.RuleMatchType;

public class TextMatcher implements IMatcher {

    @Override
    public boolean match(IParsedNode item, RuleImplementation ruleImplementation) {

        if (ruleImplementation.getRuleMatchType() == RuleMatchType.CLASS_ONLY) {
            return true;
        }
        final org.sonarsource.plugins.mybatis.rules.TextCheckType type = ruleImplementation.getTextCheckType();

        final String text = item.getText();
        final List<String> names = ruleImplementation.getTextToFind().getTextItem();
        if (names.isEmpty()) {
            return true;
        }

        for (final String searchItem : names) {
            switch (type) {
            case DEFAULT:
            case CONTAINS:
                if (StringUtils.containsIgnoreCase(text, searchItem)) {
                    return true;
                }
                break;

            case REGEXP:
                if (text.matches(searchItem)) {
                    return true;
                }
                break;
            case STRICT:
                if (text.equalsIgnoreCase(searchItem)) {
                    return true;
                }
                break;
            default:
                break;
            }
        }
        return false;

    }

}
