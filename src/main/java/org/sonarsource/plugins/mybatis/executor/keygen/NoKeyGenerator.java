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
package org.sonarsource.plugins.mybatis.executor.keygen;

import org.sonarsource.plugins.mybatis.executor.Executor;
import org.sonarsource.plugins.mybatis.executor.keygen.KeyGenerator;
import org.sonarsource.plugins.mybatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class NoKeyGenerator implements KeyGenerator {

  /**
   * A shared instance.
   *
   * @since 3.4.3
   */
  public static final NoKeyGenerator INSTANCE = new NoKeyGenerator();

  @Override
  public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    // Do Nothing
  }

  @Override
  public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
    // Do Nothing
  }

}
