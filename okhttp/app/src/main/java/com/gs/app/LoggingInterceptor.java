package com.gs.app;

import com.gs.app.util.Logger;

import java.io.IOException;

import com.squareup.okhttp3.Interceptor;
import com.squareup.okhttp3.Request;
import com.squareup.okhttp3.Response;

@Deprecated
class LoggingInterceptor implements Interceptor {

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        Logger.info(String.format("Sending request %s on %s%n%s",
                request.url(), chain.connection(), request.headers()));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        Logger.info(String.format("Received response for %s in %.1fms%n%s",
                response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        return response;
    }
}