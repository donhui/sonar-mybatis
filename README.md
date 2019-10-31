[![Build Status](https://travis-ci.com/donhui/sonar-mybatis.svg?branch=master)](https://travis-ci.com/donhui/sonar-mybatis)
[![SonarCloud Status](https://sonarcloud.io/api/project_badges/measure?project=donhui_sonar-mybatis&metric=alert_status)](https://sonarcloud.io/dashboard?id=donhui_sonar-mybatis)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/donhui/sonar-mybatis)](https://github.com/donhui/sonar-mybatis/releases/)
[![GitHub All Releases](https://img.shields.io/github/downloads/donhui/sonar-mybatis/total)](https://github.com/donhui/sonar-mybatis/releases/)

[README 中文版](README.zh.md)

## SonarQube MyBatis Plugin
MyBatis Plugin for SonarQube: Rules to check SQL statements in MyBatis Mapper XML files.

## What is Risk SQL?
Risk SQL is that in the mybatis mapper file, there are some dynamic SQL, such as `<if test=""></if>` elements of Mapper file, 
if all parameters in the SQL statement elements of Mapper XML file are null , the SQL may at great risk.

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
There are 7 built-in mybatis rules, which select statement has three rules, update and delete statement has two rules.

![mybatis-rules](images/mybatis-rules.png)

## How to install it?
There are two options to install a plugin into SonarQube:
- Marketplace - Installs plugins automatically, from the SonarQube UI.
- Manual Installation - You'll use this method if your SonarQube instance doesn't have access to the Internet.

### Marketplace
If you have access to the Internet and you are connected with a SonarQube user having the Global Permission "Administer System", you can go to Administration > Marketplace.
- Find the plugin by search `mybatis`
- Click on Install and wait for the download to be processed

Once download is complete, a "Restart" button will be available to restart your instance.

### Manual Installation
The plugin can be downloaded from [github release](https://github.com/donhui/sonar-mybatis/releases/) .

Put the downloaded jar in `$SONARQUBE_HOME/extensions/plugins`, removing any previous versions of the same plugins.

Once done, you will need to restart your SonarQube Server.

## How to Use it?
There is an example of using maven build command:
```
mvn clean compile -U -Dmaven.test.skip=true -Dmaven.javadoc.skip=true sonar:sonar -Dsonar.host.url=http://127.0.0.1:9000/ -Dsonar.projectKey=demo -Dsonar.projectName=demo -Dsonar.sourceEncoding=UTF-8 -Dsonar.sources=. -Dsonar.inclusions=src/main/** -Dsonar.exclusions==src/main/webapp/**
```
To analysis mybatis mapper file, `src/main/resources` dir must be in `sonar.sources`.

## Support Global Stmt ID Exclude
For special issues of statements, if you want ignore them, you can put the statement id with namespace into the global stmt id exclude list.

![stmt-id-exclude](images/stmt-id-exclude.png)

## Skip MyBatis Sensor
If you want to skip mybatis sensor sometimes, you can change the global properties `sonar.mybatis.skip` to `true` or add the parameter in the command:
`-Dsonar.mybatis.skip=true` .

## Contribute
Please report bugs and feature requests at https://github.com/donhui/sonar-mybatis/issues.

Or you can submit pull requests for fix bugs or create new features, any contribution is welcome.

# Stargazers over time

[![Stargazers over time](https://starchart.cc/donhui/sonar-mybatis.svg)](https://starchart.cc/donhui/sonar-mybatis)
