package io.opentracing.contrib.specialagent.rule.okhttp2;

import com.squareup.okhttp.Connection;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.opentracing.Span;
import io.opentracing.tag.Tags;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public interface OkHttpClientSpanDecorator {
    OkHttpClientSpanDecorator STANDARD_TAGS = new OkHttpClientSpanDecorator() {
        public void onRequest(Request request, Span span) {
            Tags.COMPONENT.set(span, "okhttp");
            Tags.HTTP_METHOD.set(span, request.method());
            Tags.HTTP_URL.set(span, request.url().toString());
        }

        public void onError(Throwable throwable, Span span) {
            Tags.ERROR.set(span, Boolean.TRUE);
            span.log(this.errorLogs(throwable));
        }

        public void onResponse(Connection connection, Response response, Span span) {
            Tags.HTTP_STATUS.set(span, response.code());
            InetAddress inetAddress = connection.getSocket().getInetAddress();
            Tags.PEER_HOSTNAME.set(span, inetAddress.getHostName());
            Tags.PEER_PORT.set(span, connection.getSocket().getPort());
            if (inetAddress instanceof Inet4Address) {
                byte[] address = inetAddress.getAddress();
                Tags.PEER_HOST_IPV4.set(span, ByteBuffer.wrap(address).getInt());
            } else {
                Tags.PEER_HOST_IPV6.set(span, inetAddress.toString());
            }

        }

        protected Map<String, Object> errorLogs(Throwable throwable) {
            Map<String, Object> errorLogs = new HashMap(2);
            errorLogs.put("event", Tags.ERROR.getKey());
            errorLogs.put("error.object", throwable);
            return errorLogs;
        }
    };

    void onRequest(Request var1, Span var2);

    void onError(Throwable var1, Span var2);

    void onResponse(Connection var1, Response var2, Span var3);
}
