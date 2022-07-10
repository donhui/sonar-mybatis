package org.antlr.sql.dialects.rules;

import org.sonarsource.plugins.mybatis.rules.Rule;

public interface IBaseRules {

	/**
	 * 让等待规则
	 *
	 * @return {@link Rule}
	 */
	Rule getWaitForRule();

	/**
	 * 得到选择所有规则
	 *
	 * @return {@link Rule}
	 */
	Rule getSelectAllRule();

	/**
	 * 得到插入规则
	 *
	 * @return {@link Rule}
	 */
	Rule getInsertRule();

	/**
	 * 被统治阶
	 *
	 * @return {@link Rule}
	 */
	Rule getOrderByRule();

	/**
	 * 得到执行规则
	 *
	 * @return {@link Rule}
	 */
	Rule getExecRule();

	/**
	 * 得到模式规则
	 *
	 * @return {@link Rule}
	 */
	Rule getSchemaRule();

	/**
	 * 没有锁规则
	 *
	 * @return {@link Rule}
	 */
	Rule getNoLockRule();

	/**
	 * 让光标规则
	 *
	 * @return {@link Rule}
	 */
	Rule getCursorRule();

	/**
	 * 得到多个声明
	 *
	 * @return {@link Rule}
	 */
	Rule getMultipleDeclarations();

	/**
	 * 得到相同流
	 *
	 * @return {@link Rule}
	 */
	Rule getSameFlow();

	/**
	 * 得到pkrule
	 *
	 * @return {@link Rule}
	 */
	Rule getPKRule();

	/**
	 * 得到fkrule
	 *
	 * @return {@link Rule}
	 */
	Rule getFKRule();

	/**
	 * 得到指数命名规则
	 *
	 * @return {@link Rule}
	 */
	Rule getIndexNamingRule();

	/**
	 * 得到零比较规则
	 *
	 * @return {@link Rule}
	 */
	Rule getNullComparisonRule();

	/**
	 * 与或vs联盟规则地方
	 *
	 * @return {@link Rule}
	 */
	Rule getWhereWithOrVsUnionRule();

	/**
	 * 联盟与联盟allrule
	 *
	 * @return {@link Rule}
	 */
	Rule getUnionVsUnionALLRule();

	/**
	 * 对存在于规则
	 *
	 * @return {@link Rule}
	 */
	Rule getExistsVsInRule();

	/**
	 * 没有asc desc被统治秩序
	 *
	 * @return {@link Rule}
	 */
	Rule getOrderByRuleWithoutAscDesc();

	/**
	 * 文件太大规则
	 *
	 * @return {@link Rule}
	 */
	Rule getFileTooLargeRule();

	/**
	 * 得到sarg规则
	 *
	 * @return {@link Rule}
	 */
	Rule getSargRule();

	/**
	 * 得到声明规则
	 *
	 * @return {@link Rule}
	 */
	Rule getDeclareRule();

	/**
	 * 得到数等于规则
	 *
	 * @return {@link Rule}
	 */
	Rule getNumberEqualsRule();

}