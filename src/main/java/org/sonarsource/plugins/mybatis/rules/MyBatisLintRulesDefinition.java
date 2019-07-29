package org.sonarsource.plugins.mybatis.rules;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.plugins.xml.Xml;

public final class MyBatisLintRulesDefinition implements RulesDefinition {

    private static final String PATH_TO_RULES_XML = "/mybatis/mybatislint-rules.xml";

    public static final String REPO_KEY = "MyBatisLint";
    protected static final String REPO_NAME = REPO_KEY;

    protected String rulesDefinitionFilePath() {
        return PATH_TO_RULES_XML;
    }

    private void defineRulesForLanguage(Context context, String repositoryKey, String repositoryName,
        String languageKey) {
        NewRepository repository = context.createRepository(repositoryKey, languageKey).setName(repositoryName);

        InputStream rulesXml = this.getClass().getResourceAsStream(rulesDefinitionFilePath());
        if (rulesXml != null) {
            RulesDefinitionXmlLoader rulesLoader = new RulesDefinitionXmlLoader();
            rulesLoader.load(repository, rulesXml, StandardCharsets.UTF_8.name());
        }

        repository.done();
    }

    @Override
    public void define(Context context) {
        defineRulesForLanguage(context, REPO_KEY, REPO_NAME, Xml.KEY);
    }

}
