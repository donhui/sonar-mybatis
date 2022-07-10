package org.antlr.sql.sca.matchers;

import org.antlr.sql.sca.nodes.IParsedNode;
import org.sonarsource.plugins.mybatis.rules.RuleDistanceIndexMatchType;
import org.sonarsource.plugins.mybatis.rules.RuleImplementation;

public class DistanceMatcher implements IMatcher {

	@Override
	public boolean match(IParsedNode item, RuleImplementation rule) {
		if (rule.getDistance() == 0) {
			return true;
		}
		int val = item.getDistance();
		if (rule.getDistanceCheckType() == RuleDistanceIndexMatchType.LESS) {
			if (val > rule.getDistance()) {
				return false;
			}
		}
		if (rule.getDistanceCheckType() == RuleDistanceIndexMatchType.MORE) {
			if (val < rule.getDistance()) {
				return false;
			}
		}
		if (rule.getDistanceCheckType() == RuleDistanceIndexMatchType.EQUALS) {
			if (rule.getDistance() != val) {
				return false;
			}
		}
		return true;
	}

}
