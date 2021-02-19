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

import io.opentracing.*;
import io.opentracing.contrib.specialagent.LocalSpanContext;
import io.opentracing.contrib.specialagent.OpenTracingApiUtil;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class QuartzjobAgentIntercept {
  static final String COMPONENT_NAME = "quartz-job";

  public static void enter(final Object thiz, final Object arg) {
    final JobExecutionContext jobExecutionContext = (JobExecutionContext) arg;
    final QuartzJobBean quartzJobBean = (QuartzJobBean) thiz;
    final Tracer tracer = GlobalTracer.get();
    final Span span = tracer
            .buildSpan(quartzJobBean.getClass().getName())
            .withTag(Tags.COMPONENT.getKey(), COMPONENT_NAME)
            .withTag("class", quartzJobBean.getClass().getName())
            .withTag("instanceId", jobExecutionContext.getFireInstanceId())
            .start();

    final Scope scope = tracer.activateSpan(span);
    LocalSpanContext.set(COMPONENT_NAME, span, scope);
  }

  public static void exit(final Throwable thrown) {
    final LocalSpanContext context = LocalSpanContext.get(COMPONENT_NAME);
    if (context == null)
      return;

    if (thrown != null)
      OpenTracingApiUtil.setErrorTag(context.getSpan(), thrown);

    context.closeAndFinish();
  }

}