package org.sonarsource.plugins.mybatis.fillers;



import org.antlr.sql.dialects.Dialects;
import org.antlr.sql.models.AntlrContext;
import org.antlr.sql.tools.PrettyPrinter;
import org.junit.Assert;
import org.junit.Test;
import org.sonarsource.plugins.mybatis.issues.SqlIssuesList;

import java.io.IOException;

/**
 * <p>Title: IssuesFillerTest</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: www.tul.com.cn</p>
 *
 * @author cuijing
 * @version 1.0
 * @date 2022/7/10 11:51
 */
public class IssuesFillerTest {

    @Test
    public void testIssues() throws IOException {

        IssuesFiller filler = new IssuesFiller();

        AntlrContext antlrContext = Dialects.TSQL
                .parse("update dbo.test set name = null  where 1 > 0 ;;");

        PrettyPrinter.print(antlrContext.root, 0, antlrContext.stream);

        SqlIssuesList issues = filler.getIssues(antlrContext);
        Assert.assertEquals(1, issues.getaLLIssues().size());

    }

}