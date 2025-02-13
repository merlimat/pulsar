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

import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;

public class WebSocketMultiTopicConsumerServlet extends JettyWebSocketServlet {
    private static final long serialVersionUID = 1L;

    public static final String SERVLET_PATH = "/ws/v3/consumer";

    private final transient WebSocketService service;

    public WebSocketMultiTopicConsumerServlet(WebSocketService service) {
        super();
        this.service = service;
    }

    @Override
    public void configure(JettyWebSocketServletFactory factory) {
        factory.getPolicy().setMaxTextMessageSize(service.getConfig().getWebSocketMaxTextFrameSize());
        if (service.getConfig().getWebSocketSessionIdleTimeoutMillis() > 0) {
            factory.getPolicy().setIdleTimeout(service.getConfig().getWebSocketSessionIdleTimeoutMillis());
        }
        factory.setCreator((request, response) ->
                new MultiTopicConsumerHandler(service, request.getHttpServletRequest(), response));
    }
}
