/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.okhttp3;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import com.squareup.okhttp3.internal.NamedRunnable;
import com.squareup.okhttp3.internal.cache.CacheInterceptor;
import com.squareup.okhttp3.internal.connection.ConnectInterceptor;
import com.squareup.okhttp3.internal.connection.Transmitter;
import com.squareup.okhttp3.internal.http.BridgeInterceptor;
import com.squareup.okhttp3.internal.http.CallServerInterceptor;
import com.squareup.okhttp3.internal.http.RealInterceptorChain;
import com.squareup.okhttp3.internal.http.RetryAndFollowUpInterceptor;
import com.squareup.okhttp3.internal.platform.Platform;
import okio.Timeout;

import static com.squareup.okhttp3.internal.Util.closeQuietly;
import static com.squareup.okhttp3.internal.platform.Platform.INFO;

final class RealCall implements Call {
  final OkHttpClient client;

  /**
   * There is a cycle between the {@link Call} and {@link Transmitter} that makes this awkward.
   * This is set after immediately after creating the call instance.
   */
  private Transmitter transmitter;

  /** The application's original request unadulterated by redirects or auth headers. */
  final Request originalRequest;
  final boolean forWebSocket;

  // Guarded by this.
  private boolean executed;

  private RealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
    this.client = client;
    this.originalRequest = originalRequest;
    this.forWebSocket = forWebSocket;
  }

  static RealCall newRealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
    // Safely publish the Call instance to the EventListener.
    RealCall call = new RealCall(client, originalRequest, forWebSocket);
    call.transmitter = new Transmitter(client, call);
    return call;
  }

  @Override public Request request() {
    return originalRequest;
  }

  @Override public Response execute() throws IOException {
    synchronized (this) {
      if (executed) throw new IllegalStateException("Already Executed");
      executed = true;
    }
    transmitter.timeoutEnter();
    transmitter.callStart();
    try {
      client.dispatcher().executed(this);
      return getResponseWithInterceptorChain();
    } finally {
      client.dispatcher().finished(this);
    }
  }

  @Override public void enqueue(Callback responseCallback) {
    synchronized (this) {
      if (executed) throw new IllegalStateException("Already Executed");
      executed = true;
    }
    transmitter.callStart();
    client.dispatcher().enqueue(new AsyncCall(responseCallback));
  }

  @Override public void cancel() {
    transmitter.cancel();
  }

  @Override public Timeout timeout() {
    return transmitter.timeout();
  }

  @Override public synchronized boolean isExecuted() {
    return executed;
  }

  @Override public boolean isCanceled() {
    return transmitter.isCanceled();
  }

  @SuppressWarnings("CloneDoesntCallSuperClone") // We are a final type & this saves clearing state.
  @Override public RealCall clone() {
    return RealCall.newRealCall(client, originalRequest, forWebSocket);
  }

  final class AsyncCall extends NamedRunnable {
    private final Callback responseCallback;
    private volatile AtomicInteger callsPerHost = new AtomicInteger(0);

    AsyncCall(Callback responseCallback) {
      super("OkHttp %s", redactedUrl());
      this.responseCallback = responseCallback;
    }

    AtomicInteger callsPerHost() {
      return callsPerHost;
    }

    void reuseCallsPerHostFrom(AsyncCall other) {
      this.callsPerHost = other.callsPerHost;
    }

    String host() {
      return originalRequest.url().host();
    }

    Request request() {
      return originalRequest;
    }

    RealCall get() {
      return RealCall.this;
    }

    /**
     * Attempt to enqueue this async call on {@code executorService}. This will attempt to clean up
     * if the executor has been shut down by reporting the call as failed.
     */
    void executeOn(ExecutorService executorService) {
      assert (!Thread.holdsLock(client.dispatcher()));
      boolean success = false;
      try {
        executorService.execute(this);
        success = true;
      } catch (RejectedExecutionException e) {
        InterruptedIOException ioException = new InterruptedIOException("executor rejected");
        ioException.initCause(e);
        transmitter.noMoreExchanges(ioException);
        responseCallback.onFailure(RealCall.this, ioException);
      } finally {
        if (!success) {
          client.dispatcher().finished(this); // This call is no longer running!
        }
      }
    }

    @Override protected void execute() {
      boolean signalledCallback = false;
      transmitter.timeoutEnter();
      try {
        Response response = getResponseWithInterceptorChain();
        signalledCallback = true;
        responseCallback.onResponse(RealCall.this, response);
      } catch (IOException e) {
        if (signalledCallback) {
          // Do not signal the callback twice!
          Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
        } else {
          responseCallback.onFailure(RealCall.this, e);
        }
      } finally {
        client.dispatcher().finished(this);
      }
    }
  }

  /**
   * Returns a string that describes this call. Doesn't include a full URL as that might contain
   * sensitive information.
   */
  String toLoggableString() {
    return (isCanceled() ? "canceled " : "")
        + (forWebSocket ? "web socket" : "call")
        + " to " + redactedUrl();
  }

  String redactedUrl() {
    return originalRequest.url().redact();
  }

  Response getResponseWithInterceptorChain() throws IOException {
    // Build a full stack of interceptors.
    List<Interceptor> interceptors = new ArrayList<>();

    interceptors.addAll(client.interceptors());  //添加用户自己的拦截器

    //TODO 负责处理失败后的重试与重定向
    interceptors.add(new RetryAndFollowUpInterceptor(client));

    //TODO 负责把用户构造的请求转换为发送到服务器的请求 、把服务器返回的响应转换为用户友好的响应 处理 配置请求头等信息
    //TODO 从应用程序代码到网络代码的桥梁。首先，它根据用户请求构建网络请求。然后它继续呼叫网络。最后，它根据网络响应构建用户响应。
    interceptors.add(new BridgeInterceptor(client.cookieJar()));

    //TODO 处理 缓存配置 根据条件(存在响应缓存并被设置为不变的或者响应在有效期内)返回缓存响应
    //TODO 设置请求头(If-None-Match、If-Modified-Since等) 服务器可能返回304(未修改)
    //TODO 可配置用户自己设置的缓存拦截器
    interceptors.add(new CacheInterceptor(client.internalCache()));

    //TODO 连接服务器 负责和服务器建立连接 这里才是真正的请求网络
    interceptors.add(new ConnectInterceptor(client));

    if (!forWebSocket) {
      //TODO 配置okhttpClient 时设置的networkInterceptors
      //TODO 返回观察单个网络请求和响应的不可变拦截器列表。
      interceptors.addAll(client.networkInterceptors());
    }

    //TODO 执行流操作(写出请求体、获得响应数据) 负责向服务器发送请求数据、从服务器读取响应数据
    //TODO 进行http请求报文的封装与请求报文的解析
    interceptors.add(new CallServerInterceptor(forWebSocket));

    //TODO 创建责任链
    Interceptor.Chain chain = new RealInterceptorChain(interceptors, transmitter, null, 0,
        originalRequest, this, client.connectTimeoutMillis(),
        client.readTimeoutMillis(), client.writeTimeoutMillis());

    boolean calledNoMoreExchanges = false;
    try {
      //TODO 执行责任链
      Response response = chain.proceed(originalRequest);
      if (transmitter.isCanceled()) {
        closeQuietly(response);
        throw new IOException("Canceled");
      }
      return response;
    } catch (IOException e) {
      calledNoMoreExchanges = true;
      throw transmitter.noMoreExchanges(e);
    } finally {
      if (!calledNoMoreExchanges) {
        transmitter.noMoreExchanges(null);
      }
    }
  }
}
