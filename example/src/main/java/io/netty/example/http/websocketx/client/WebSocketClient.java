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

import java.io.InputStreamReader;

/**
 * This is an example of a WebSocket client.
 * <p>
 * In order to run this example you need a compatible WebSocket server. Therefore you can either
 * start the WebSocket server from the examples by running
 * {@link io.netty.example.http.websocketx.server.WebSocketServer} or connect to an existing
 * WebSocket server such as
 * <a href="http://www.websocket.org/echo.html">ws://echo.websocket.org</a>.
 * <p>
 * The client will attempt to connect to the URI passed to it as the first argument. You don't have
 * to specify any arguments if you want to connect to the example WebSocket server, as this is the
 * default.
 */
public final class WebSocketClient {

  static final String URL = System.getProperty("url", "ws://127.0.0.1:8080/websocket");

  public static void main(String[] args) throws Exception {
    long count = 0;
    WsClientChannel client;
    new InputStreamReader(System.in).read();
    while (true) {
      System.out.println();
      System.out.println("Starting loop");
      client = new WsClientChannel();
      client.startClient(URL);
      System.out.println("Pass " + ++count);
      client.send(count);
      // Give some time to free resources
      Thread.sleep(20);
      client.cleanResources();
      client = null;
    }
  }

}
