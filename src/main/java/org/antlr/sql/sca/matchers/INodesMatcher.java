package org.antlr.sql.sca.matchers;

import org.antlr.sql.sca.nodes.IParsedNode;
import org.sonarsource.plugins.mybatis.rules.RuleImplementation;

public interface INodesMatcher {
	boolean matches(IParsedNode item, IParsedNode parent, RuleImplementation rule);
}
