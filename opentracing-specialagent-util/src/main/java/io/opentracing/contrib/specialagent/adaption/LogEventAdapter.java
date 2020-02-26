package io.opentracing.contrib.specialagent.adaption;

import io.opentracing.Span;

public class LogEventAdapter extends Adapter {
  private final long timestampMicroseconds;
  private final Span target;
  private final Adaptive source;

  LogEventAdapter(final AdaptionRules rules, final long timestampMicroseconds, final Adaptive source, final Span target) {
    super(rules);
    this.timestampMicroseconds = timestampMicroseconds;
    this.source = source;
    this.target = target;
  }

  final void processLog(final String event) {
    if (!processRules(AdaptionRuleType.LOG, null, event))
      log(event);
  }

  void log(final Object value) {
    if (timestampMicroseconds > 0)
      target.log(timestampMicroseconds, value.toString());
    else
      target.log(value.toString());
  }

  @Override
  void adaptLogField(final String key, final Object value) {
    log(value);
  }

  @Override
  void adaptTag(final String key, final Object value) {
    source.adaptTag(key, value);
  }

  @Override
  void adaptOperationName(final String name) {
    source.adaptOperationName(name);
  }
}