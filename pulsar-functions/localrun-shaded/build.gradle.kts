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

plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":pulsar-functions:pulsar-functions-local-runner"))
}

val shadePrefix = "org.apache.pulsar.functions.runtime.shaded"

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()

    relocate("com.google", "$shadePrefix.com.google")
    relocate("org.apache.jute", "$shadePrefix.org.apache.jute")
    relocate("javax.servlet", "$shadePrefix.javax.servlet")
    relocate("javax.ws", "$shadePrefix.javax.ws")
    relocate("org.lz4", "$shadePrefix.org.lz4")
    relocate("org.reactivestreams", "$shadePrefix.org.reactivestreams")
    relocate("org.apache.commons", "$shadePrefix.org.apache.commons")
    relocate("io.swagger", "$shadePrefix.io.swagger")
    relocate("org.yaml", "$shadePrefix.org.yaml")
    relocate("io.grpc", "$shadePrefix.io.grpc")
    relocate("io.perfmark", "$shadePrefix.io.perfmark")
    relocate("io.prometheus", "$shadePrefix.io.prometheus")
    relocate("org.apache.zookeeper", "$shadePrefix.org.apache.zookeeper")
    relocate("org.apache.bookkeeper", "$shadePrefix.org.apache.bookkeeper")
    relocate("org.apache.distributedlog", "$shadePrefix.org.apache.distributedlog")
    relocate("org.apache.curator", "$shadePrefix.org.apache.curator")
    relocate("org.codehaus.jackson", "$shadePrefix.org.codehaus.jackson")
    relocate("org.asynchttpclient", "$shadePrefix.org.asynchttpclient")
    relocate("com.yahoo", "$shadePrefix.com.yahoo")
    relocate("com.typesafe", "$shadePrefix.com.typesafe")
    // NOTE: Do NOT shade log4j, otherwise logging won't work
}
