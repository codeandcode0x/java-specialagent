package io.opentracing.contrib.specialagent.rule.okhttp2;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class TracingInterceptor implements Interceptor {

    private static final Logger log = Logger.getLogger(TracingInterceptor.class.getName());
    private Tracer tracer;
    private List<OkHttpClientSpanDecorator> decorators;

    public TracingInterceptor(Tracer tracer) {
        this(tracer, Collections.singletonList(OkHttpClientSpanDecorator.STANDARD_TAGS));
    }

    public TracingInterceptor(Tracer tracer, List<OkHttpClientSpanDecorator> decorators) {
        this.tracer = tracer;
        this.decorators = new ArrayList(decorators);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = null;
        if (chain.connection() == null) {
            log.info(">>>>>>> TracingInterceptor.intercept with chain.connection");
            Span span = this.tracer.buildSpan(chain.request().method()).withTag(Tags.COMPONENT.getKey(), "okhttp").start();

            Request.Builder requestBuilder = chain.request().newBuilder();
            Object tag = chain.request().tag();
            TagWrapper tagWrapper = tag instanceof TagWrapper ? (TagWrapper)tag : new TagWrapper(tag);
            requestBuilder.tag(new TagWrapper(tagWrapper, span));

            try {
                response = chain.proceed(requestBuilder.build());
            } catch (IOException var13) {
                IOException ex = var13;
                Iterator var8 = this.decorators.iterator();

                while(var8.hasNext()) {
                    OkHttpClientSpanDecorator spanDecorator = (OkHttpClientSpanDecorator)var8.next();
                    spanDecorator.onError(ex, span);
                }

                throw ex;
            } finally {
                span.finish();
            }
        } else {
            log.info(">>>>>>> TracingInterceptor.intercept without chain.connection");
            Object tag = chain.request().tag();
            if (tag instanceof TagWrapper) {
                TagWrapper tagWrapper = (TagWrapper)tag;
                response = (new TracingCallFactory.NetworkInterceptor(this.tracer, tagWrapper.getSpan().context(), this.decorators)).intercept(chain);
            } else {
                log.severe("tag is null or not an instance of TagWrapper, skipping decorator onResponse()");
            }
        }

        return response;
    }

}
