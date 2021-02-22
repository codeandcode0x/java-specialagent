/* Copyright 2019 The OpenTracing Authors
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

package io.opentracing.contrib.specialagent.rule.quartz;

import static net.bytebuddy.matcher.ElementMatchers.*;

import io.opentracing.contrib.specialagent.AgentRule;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.utility.JavaModule;

public class QuartzJobBeanAgentRule extends AgentRule {
    @Override
    public AgentBuilder buildAgentChainedGlobal1(final AgentBuilder builder) {
        return builder
//                .type(named("net.coding.infra.cci.scheduler.CiTaskQueuingChecker"))
                .type(hasSuperType(named("org.quartz.Job")).and(not(isInterface())))
                .transform(new Transformer() {
                    @Override
                    public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
                        return builder.visit(advice(typeDescription).to(QuartzJobBeanAgentRule.class).on(named("executeInternal")));
                    }
                });
    }

    @Advice.OnMethodEnter
    public static void enter(final @ClassName String className, final @Advice.Origin String origin, final @Advice.This Object thiz, final @Advice.Argument(value = 0) Object arg) {
        if (isAllowed(className, origin))
            QuartzjobAgentIntercept.enter(thiz, arg);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void exit(final @ClassName String className, final @Advice.Origin String origin, final @Advice.Thrown Throwable thrown) {
        if (isAllowed(className, origin))
            QuartzjobAgentIntercept.exit(thrown);
    }
}