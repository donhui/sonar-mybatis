package org.antlr.sql.sca.matchers;

import org.antlr.sql.sca.nodes.IParsedNode;
import org.sonarsource.plugins.mybatis.rules.RuleImplementation;

public interface IMatcher {
	boolean match(IParsedNode item, RuleImplementation ruleImplementation);
}
