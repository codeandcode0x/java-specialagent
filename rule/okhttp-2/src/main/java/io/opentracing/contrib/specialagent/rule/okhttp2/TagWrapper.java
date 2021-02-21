package io.opentracing.contrib.specialagent.rule.okhttp2;

import io.opentracing.Span;

public class TagWrapper {
    private Span span;
    private Object tag;

    public TagWrapper(Object tag) {
        this.tag = tag;
    }

    TagWrapper(TagWrapper wrapper, Span span) {
        this.span = span;
        this.tag = wrapper.tag;
    }

    public Object getTag() {
        return this.tag;
    }

    Span getSpan() {
        return this.span;
    }
}
