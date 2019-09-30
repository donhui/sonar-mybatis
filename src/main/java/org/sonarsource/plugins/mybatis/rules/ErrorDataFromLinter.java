package org.sonarsource.plugins.mybatis.rules;

import java.io.Serializable;

public class ErrorDataFromLinter implements Serializable {
    private static final long serialVersionUID = 1L;
    private String externalRuleId = "";
    private String issueMessage = "";
    private String filePath = "";
    private int line = 0;

    public ErrorDataFromLinter(final String externalRuleId, final String issueMessage, final String filePath,
            final int line) {
        this.externalRuleId = externalRuleId;
        this.issueMessage = issueMessage;
        this.filePath = filePath;
        this.line = line;
    }

    public String getType() {
        return externalRuleId;
    }

    public String getDescription() {
        return issueMessage;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(externalRuleId);
        s.append("|");
        s.append(issueMessage);
        s.append("|");
        s.append(filePath);
        s.append("(");
        s.append(line);
        s.append(")");
        return s.toString();
    }

}
