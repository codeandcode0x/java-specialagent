package io.opentracing.contrib.specialagent.rule.okhttp2;

import com.squareup.okhttp.*;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TracingCallFactory {
    static final String COMPONENT_NAME = "okhttp";
    private OkHttpClient okHttpClient;
    private Tracer tracer;
    private List<OkHttpClientSpanDecorator> decorators;

    public TracingCallFactory(OkHttpClient okHttpClient, Tracer tracer) {
        this(okHttpClient, tracer, Collections.singletonList(OkHttpClientSpanDecorator.STANDARD_TAGS));
    }

    public TracingCallFactory(OkHttpClient okHttpClient, Tracer tracer, List<OkHttpClientSpanDecorator> decorators) {
        this.okHttpClient = okHttpClient;
        this.tracer = tracer;
        this.decorators = new ArrayList(decorators);
    }
//
//    public Call newCall(Request request) {
//        final Span span = this.tracer.buildSpan(request.method()).withTag(Tags.COMPONENT.getKey(), "okhttp").start();
//
//        try {
//            Builder okBuilder = this.okHttpClient.newBuilder();
//            okBuilder.networkInterceptors().add(0, new TracingCallFactory.NetworkInterceptor(this.tracer, span.context(), this.decorators));
//            okBuilder.interceptors().add(0, new Interceptor() {
//                public Response intercept(Chain chain) throws IOException {
//                    Object var28;
//                    try {
//                        Scope activeInterceptorSpan = TracingCallFactory.this.tracer.activateSpan(span);
//                        Throwable var27 = null;
//
//                        try {
//                            var28 = chain.proceed(chain.request());
//                        } catch (Throwable var22) {
//                            var28 = var22;
//                            var27 = var22;
//                            throw var22;
//                        } finally {
//                            if (activeInterceptorSpan != null) {
//                                if (var27 != null) {
//                                    try {
//                                        activeInterceptorSpan.close();
//                                    } catch (Throwable var21) {
//                                        var27.addSuppressed(var21);
//                                    }
//                                } else {
//                                    activeInterceptorSpan.close();
//                                }
//                            }
//
//                        }
//                    } catch (Exception var24) {
//                        Exception ex = var24;
//                        Iterator var3 = TracingCallFactory.this.decorators.iterator();
//
//                        while(var3.hasNext()) {
//                            OkHttpClientSpanDecorator spanDecorator = (OkHttpClientSpanDecorator)var3.next();
//                            spanDecorator.onError(ex, span);
//                        }
//
//                        throw ex;
//                    } finally {
//                        span.finish();
//                    }
//
//                    return (Response)var28;
//                }
//            });
//            return okBuilder.build().newCall(request);
//        } catch (Exception var6) {
//            Exception ex = var6;
//            Iterator var4 = this.decorators.iterator();
//
//            while(var4.hasNext()) {
//                OkHttpClientSpanDecorator spanDecorator = (OkHttpClientSpanDecorator)var4.next();
//                spanDecorator.onError(ex, span);
//            }
//
//            throw ex;
//        }
//    }

    static class NetworkInterceptor implements Interceptor {
        public SpanContext parentContext;
        public Tracer tracer;
        public List<OkHttpClientSpanDecorator> decorators;

        NetworkInterceptor(Tracer tracer, SpanContext spanContext, List<OkHttpClientSpanDecorator> decorators) {
            this.parentContext = spanContext;
            this.tracer = tracer;
            this.decorators = decorators;
        }

        public Response intercept(Chain chain) throws IOException {
            Span networkSpan = this.tracer.buildSpan(chain.request().method()).withTag(Tags.SPAN_KIND.getKey(), "client").asChildOf(this.parentContext).start();
            Iterator var3 = this.decorators.iterator();

            while(var3.hasNext()) {
                OkHttpClientSpanDecorator spanDecorator = (OkHttpClientSpanDecorator)var3.next();
                spanDecorator.onRequest(chain.request(), networkSpan);
            }

            Request.Builder requestBuilder = chain.request().newBuilder();
//            this.tracer.inject(networkSpan.context(), Format.Builtin.HTTP_HEADERS, new RequestBuilderInjectAdapter(requestBuilder));

            try {
                Scope scope = this.tracer.activateSpan(networkSpan);
                Throwable var5 = null;

                try {
                    Response response = chain.proceed(requestBuilder.build());
                    Iterator var7 = this.decorators.iterator();

                    while(var7.hasNext()) {
                        OkHttpClientSpanDecorator spanDecorator = (OkHttpClientSpanDecorator)var7.next();
                        spanDecorator.onResponse(chain.connection(), response, networkSpan);
                    }

                    Response var28 = response;
                    return var28;
                } catch (Throwable var23) {
                    var5 = var23;
                    throw var23;
                } finally {
                    if (scope != null) {
                        if (var5 != null) {
                            try {
                                scope.close();
                            } catch (Throwable var22) {
                                var5.addSuppressed(var22);
                            }
                        } else {
                            scope.close();
                        }
                    }

                }
            } finally {
                networkSpan.finish();
            }
        }
    }
}
