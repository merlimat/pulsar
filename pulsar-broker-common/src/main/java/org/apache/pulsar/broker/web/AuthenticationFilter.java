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
package org.apache.pulsar.broker.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.naming.AuthenticationException;
import org.apache.pulsar.broker.authentication.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet filter that hooks up with AuthenticationService to reject unauthenticated HTTP requests.
 */
public class AuthenticationFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final AuthenticationService authenticationService;

    public static final String AuthenticatedRoleAttributeName = AuthenticationFilter.class.getName() + "-role";
    public static final String AuthenticatedDataAttributeName = AuthenticationFilter.class.getName() + "-data";
    public static final String PULSAR_AUTH_METHOD_NAME = "X-Pulsar-Auth-Method-Name";


    public AuthenticationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        final boolean doFilter;
        try {
            doFilter = authenticationService.authenticateHttpRequest(httpRequest, httpResponse);
        } catch (Exception e) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            if (e instanceof AuthenticationException) {
                LOG.warn("[{}] Failed to authenticate HTTP request: {}", request.getRemoteAddr(), e.getMessage());
            } else {
                LOG.error("[{}] Error performing authentication for HTTP", request.getRemoteAddr(), e);
            }
            return;
        }

        if (doFilter) {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig arg) throws ServletException {
        // No init necessary.
    }

    @Override
    public void destroy() {
        // No state to clean up.
    }
}
