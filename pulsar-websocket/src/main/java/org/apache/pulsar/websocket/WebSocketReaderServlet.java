/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.websocket;

import java.time.Duration;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;

public class WebSocketReaderServlet extends JettyWebSocketServlet {
    private static final transient long serialVersionUID = 1L;

    public static final String SERVLET_PATH = "/ws/reader";
    public static final String SERVLET_PATH_V2 = "/ws/v2/reader";

    private final transient WebSocketService service;

    public WebSocketReaderServlet(WebSocketService service) {
        super();
        this.service = service;
    }

    @Override
    public void configure(JettyWebSocketServletFactory factory) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        JettyWebSocketServletContainerInitializer.configure(context, (servletContext, container) -> {
            container.setMaxTextMessageSize(service.getConfig().getWebSocketMaxTextFrameSize());
            if (service.getConfig().getWebSocketSessionIdleTimeoutMillis() > 0) {
                container.setIdleTimeout(Duration.ofMillis(service.getConfig().getWebSocketSessionIdleTimeoutMillis()));
            }
        });

        factory.setCreator(
                (request, response) -> new ReaderHandler(service, request.getHttpServletRequest(), response));
    }
}
