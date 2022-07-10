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
package org.sonarsource.plugins.mybatis.scripting;

import org.sonarsource.plugins.mybatis.scripting.LanguageDriver;
import org.sonarsource.plugins.mybatis.scripting.ScriptingException;
import org.apache.ibatis.util.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frank D. Martinez [mnesarco]
 */
public class LanguageDriverRegistry {

  private final Map<Class<? extends org.sonarsource.plugins.mybatis.scripting.LanguageDriver>, org.sonarsource.plugins.mybatis.scripting.LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();

  private Class<? extends org.sonarsource.plugins.mybatis.scripting.LanguageDriver> defaultDriverClass;

  public void register(Class<? extends org.sonarsource.plugins.mybatis.scripting.LanguageDriver> cls) {
    if (cls == null) {
      throw new IllegalArgumentException("null is not a valid Language Driver");
    }
    MapUtil.computeIfAbsent(LANGUAGE_DRIVER_MAP, cls, k -> {
      try {
        return k.getDeclaredConstructor().newInstance();
      } catch (Exception ex) {
        throw new ScriptingException("Failed to load language driver for " + cls.getName(), ex);
      }
    });
  }

  public void register(org.sonarsource.plugins.mybatis.scripting.LanguageDriver instance) {
    if (instance == null) {
      throw new IllegalArgumentException("null is not a valid Language Driver");
    }
    Class<? extends org.sonarsource.plugins.mybatis.scripting.LanguageDriver> cls = instance.getClass();
    if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
      LANGUAGE_DRIVER_MAP.put(cls, instance);
    }
  }

  public org.sonarsource.plugins.mybatis.scripting.LanguageDriver getDriver(Class<? extends org.sonarsource.plugins.mybatis.scripting.LanguageDriver> cls) {
    return LANGUAGE_DRIVER_MAP.get(cls);
  }

  public org.sonarsource.plugins.mybatis.scripting.LanguageDriver getDefaultDriver() {
    return getDriver(getDefaultDriverClass());
  }

  public Class<? extends org.sonarsource.plugins.mybatis.scripting.LanguageDriver> getDefaultDriverClass() {
    return defaultDriverClass;
  }

  public void setDefaultDriverClass(Class<? extends LanguageDriver> defaultDriverClass) {
    register(defaultDriverClass);
    this.defaultDriverClass = defaultDriverClass;
  }

}
