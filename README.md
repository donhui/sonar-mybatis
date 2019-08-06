[README 中文版](README.zh.md)

## SonarQube MyBatis Plugin
MyBatis Plugin for SonarQube: Custom Rule for Check Risk SQL of Mybatis Mapper XML.

## What is Risk SQL?
Risk SQL is that in the mybatis mapper file, there are some dynamic SQL, such as `<if test="">`, 
if not matched the SQL may at great risk.

an example as follows:

```
DELETE FROM table_name
WHERE 1=1
<if test="startTime != null">
    AND start_time <![CDATA[=]]> #{startTime}
</if>
<if test="endTime != null">
    AND end_time <![CDATA[=]]> #{endTime}
</if>
```

## MyBatis Rules
There are 6 built-in mybatis rules, which select, update and delete statement has two rules.

![mybatis-rules](images/mybatis-rules.png)

## How to Use it?
There is an example of using maven build command:
```
mvn clean compile -U -Dmaven.test.skip=true -Dmaven.javadoc.skip=true sonar:sonar -Dsonar.host.url=http://127.0.0.1:9000/ -Dsonar.projectKey=demo -Dsonar.projectName=demo -Dsonar.sourceEncoding=UTF-8 -Dsonar.sources=. -Dsonar.inclusions=src/main/** -Dsonar.exclusions==src/main/webapp/**
```
To analysis mybatis mapper file, `src/main/resources` dir must be in `sonar.sources`.

## Support Global Stmt ID Exclude
For special issues of statements, if you want ignore them, you can put the statement id with namespace into the global stmt id exclude list.

![stmt-id-exclude](images/stmt-id-exclude.png)

## Contribute
Please report bugs and feature requests at https://github.com/donhui/sonar-mybatis/issues.

Or you can submit pull requests for fix bugs or create new features, any contribution is welcome.

