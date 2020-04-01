/* Copyright 2020 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentracing.contrib.specialagent.rule.dubbo27;

public class GreeterServiceImpl implements GreeterService {
  public static volatile boolean isThrowExecption = false;
  public static final String errorMesg = "fail to call GreeterService";

  @Override
  public String sayHello(final String name) {
    if (isThrowExecption)
      throw new RuntimeException(errorMesg);

    return "hello " + name;
  }

  @Override
  public String sayGoodbye(final String name) {
    return "goodbye " + name;
  }
}