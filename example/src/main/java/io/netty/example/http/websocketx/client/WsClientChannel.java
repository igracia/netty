/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.example.http.websocketx.client;

import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class WsClientChannel {

  private EventLoopGroup group;

  private Channel ch;

  void cleanResources() throws InterruptedException {
    System.out.println("Cleaning resources");
    ch.close().sync();
    ch = null;
    group.shutdownGracefully();
    group = null;
  }

  void startClient(String url) throws URISyntaxException, SSLException, InterruptedException {

    URI uri = new URI(url);
    String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
    final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
    final int port;
    if (uri.getPort() == -1) {
      if ("ws".equalsIgnoreCase(scheme)) {
        port = 80;
      } else if ("wss".equalsIgnoreCase(scheme)) {
        port = 443;
      } else {
        port = -1;
      }
    } else {
      port = uri.getPort();
    }

    if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
      System.err.println("Only WS(S) is supported.");
      return;
    }

    final boolean ssl = "wss".equalsIgnoreCase(scheme);
    final SslContext sslCtx;
    if (ssl) {
      sslCtx =
          SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
    } else {
      sslCtx = null;
    }

    group = new NioEventLoopGroup(5);

    // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
    // If you change it to V00, ping is not supported and remember to change
    // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
    final WebSocketClientHandler handler =
        new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri,
            WebSocketVersion.V13, null, false, new DefaultHttpHeaders()));

    Bootstrap b = new Bootstrap();
    b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
      @Override
      protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
          p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
        }
        p.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192),
            WebSocketClientCompressionHandler.INSTANCE, handler);
      }
    });

    ch = b.connect(uri.getHost(), port).sync().channel();
    handler.handshakeFuture().sync();
  }

  public void send(long count) {
    ch.writeAndFlush(count);
  }
}
