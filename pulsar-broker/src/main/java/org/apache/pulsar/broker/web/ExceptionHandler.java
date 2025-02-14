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

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.common.intercept.InterceptException;
import org.apache.pulsar.common.policies.data.ErrorData;
import org.apache.pulsar.common.util.ObjectMapperFactory;

/**
 *  Exception handler for handle exception.
 */
@Slf4j
public class ExceptionHandler {

    public void handle(ServletResponse response, Exception ex) throws IOException {
        if (ex instanceof InterceptException) {
            if (response instanceof org.eclipse.jetty.server.Response) {
                String errorData = ObjectMapperFactory
                        .getMapper().writer().writeValueAsString(new ErrorData(ex.getMessage()));
                int errorCode = ((InterceptException) ex).getErrorCode();
                ((org.eclipse.jetty.server.Response) response).setStatus(errorCode);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(errorData);
            } else {
                ((HttpServletResponse) response).sendError(((InterceptException) ex).getErrorCode(),
                        ex.getMessage());
            }
        } else {
            ((HttpServletResponse) response).sendError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    ex.getMessage());
        }
    }
}
