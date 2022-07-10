/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.sonarsource.plugins.mybatis.builder;

import org.sonarsource.plugins.mybatis.builder.BuilderException;
import org.sonarsource.plugins.mybatis.mapping.ParameterMode;
import org.sonarsource.plugins.mybatis.mapping.ResultSetType;
import org.sonarsource.plugins.mybatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Clinton Begin
 */
public abstract class BaseBuilder {
  protected final Configuration configuration;
  protected final TypeAliasRegistry typeAliasRegistry;

  public BaseBuilder(Configuration configuration) {
    this.configuration = configuration;
    this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  protected Pattern parseExpression(String regex, String defaultValue) {
    return Pattern.compile(regex == null ? defaultValue : regex);
  }

  protected Boolean booleanValueOf(String value, Boolean defaultValue) {
    return value == null ? defaultValue : Boolean.valueOf(value);
  }

  protected Integer integerValueOf(String value, Integer defaultValue) {
    return value == null ? defaultValue : Integer.valueOf(value);
  }

  protected Set<String> stringSetValueOf(String value, String defaultValue) {
    value = value == null ? defaultValue : value;
    return new HashSet<>(Arrays.asList(value.split(",")));
  }

  protected JdbcType resolveJdbcType(String alias) {
    if (alias == null) {
      return null;
    }
    try {
      return JdbcType.valueOf(alias);
    } catch (IllegalArgumentException e) {
      throw new org.sonarsource.plugins.mybatis.builder.BuilderException("Error resolving JdbcType. Cause: " + e, e);
    }
  }

  protected ResultSetType resolveResultSetType(String alias) {
    if (alias == null) {
      return null;
    }
    try {
      return ResultSetType.valueOf(alias);
    } catch (IllegalArgumentException e) {
      throw new org.sonarsource.plugins.mybatis.builder.BuilderException("Error resolving ResultSetType. Cause: " + e, e);
    }
  }

  protected ParameterMode resolveParameterMode(String alias) {
    if (alias == null) {
      return null;
    }
    try {
      return ParameterMode.valueOf(alias);
    } catch (IllegalArgumentException e) {
      throw new org.sonarsource.plugins.mybatis.builder.BuilderException("Error resolving ParameterMode. Cause: " + e, e);
    }
  }

  protected Object createInstance(String alias) {
    Class<?> clazz = resolveClass(alias);
    if (clazz == null) {
      return null;
    }
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new org.sonarsource.plugins.mybatis.builder.BuilderException("Error creating instance. Cause: " + e, e);
    }
  }

  protected <T> Class<? extends T> resolveClass(String alias) {
    if (alias == null) {
      return null;
    }
    try {
      return resolveAlias(alias);
    } catch (Exception e) {
      throw new org.sonarsource.plugins.mybatis.builder.BuilderException("Error resolving class. Cause: " + e, e);
    }
  }


  protected <T> Class<? extends T> resolveAlias(String alias) {
    return typeAliasRegistry.resolveAlias(alias);
  }
}
