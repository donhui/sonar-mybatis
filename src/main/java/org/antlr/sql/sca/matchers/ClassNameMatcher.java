package org.antlr.sql.sca.matchers;

import java.util.List;

import org.antlr.sql.sca.nodes.IParsedNode;
import org.sonarsource.plugins.mybatis.rules.RuleImplementation;
import org.sonarsource.plugins.mybatis.rules.RuleMatchType;

public class ClassNameMatcher implements IMatcher {

	@Override
	public boolean match(IParsedNode item, RuleImplementation ruleImplementation) {
		if (ruleImplementation.getRuleMatchType() == RuleMatchType.TEXT_ONLY) {
			return true;
		}
		final List<String> names = ruleImplementation.getNames().getTextItem();
		boolean result =  names.contains(item.getClassName());
		return result;
	}

}
