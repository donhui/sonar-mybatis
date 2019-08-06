## SonarQube MyBatis 插件
MyBatis SonarQube Plugin ：自定义规则用于检查 Mybatis Mapper XML 文件中的风险 SQL。

## What is Risk SQL?
风险 SQL 是指在 mybatis mapper 文件中，有一些动态 SQL，例如 `<if test="">`，
如果没有命中后出来的 SQL 有比较大的风险。

示例如下:

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

## MyBatis 规则
有6个内置的规则，select、update 以及 delete 语句分别有2个规则。

![mybatis-rules](images/mybatis-rules.png)

## 如何使用？
使用 maven 构建命令的示例如下：
```
mvn clean compile -U -Dmaven.test.skip=true -Dmaven.javadoc.skip=true sonar:sonar -Dsonar.host.url=http://127.0.0.1:9000/ -Dsonar.projectKey=demo -Dsonar.projectName=demo -Dsonar.sourceEncoding=UTF-8 -Dsonar.sources=. -Dsonar.inclusions=src/main/** -Dsonar.exclusions==src/main/webapp/**
```
要想分析 mybatis mapper 文件， `src/main/resources` 目录必须在 `sonar.sources` 中。

## 支持全局 Stmt ID 排除列表
对于一些特殊语句的问题，要想忽略它们，可以将附带 namespace 的 statement id 放入全局 stmt id 排除列表。

![stmt-id-exclude](images/stmt-id-exclude.png)

## 贡献
可以在 https://github.com/donhui/sonar-mybatis/issues 提交 bug 和新功能请求。

或者可以提交 pull requests 用于修复 bug 或提交新功能，欢迎任何形式的贡献。

