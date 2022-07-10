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
package org.sonarsource.plugins.mybatis.mapping;

import org.sonarsource.plugins.mybatis.mapping.ParameterMapping;
import org.sonarsource.plugins.mybatis.session.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class ParameterMap {

  private String id;
  private Class<?> type;
  private List<org.sonarsource.plugins.mybatis.mapping.ParameterMapping> parameterMappings;

  private ParameterMap() {
  }

  public static class Builder {
    private ParameterMap parameterMap = new ParameterMap();

    public Builder(Configuration configuration, String id, Class<?> type, List<org.sonarsource.plugins.mybatis.mapping.ParameterMapping> parameterMappings) {
      parameterMap.id = id;
      parameterMap.type = type;
      parameterMap.parameterMappings = parameterMappings;
    }

    public Class<?> type() {
      return parameterMap.type;
    }

    public ParameterMap build() {
      //lock down collections
      parameterMap.parameterMappings = Collections.unmodifiableList(parameterMap.parameterMappings);
      return parameterMap;
    }
  }

  public String getId() {
    return id;
  }

  public Class<?> getType() {
    return type;
  }

  public List<ParameterMapping> getParameterMappings() {
    return parameterMappings;
  }

}
