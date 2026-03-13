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
    implementation(project(":pulsar-client-admin")) {
        exclude(group = "it.unimi.dsi", module = "fastutil")
    }
    implementation(project(":pulsar-client-dependencies-minimized"))
    implementation(project(":pulsar-client-messagecrypto-bc"))
}

val shadePrefix = "org.apache.pulsar.shade"

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()

    relocate("com.fasterxml.jackson", "$shadePrefix.com.fasterxml.jackson")
    relocate("com.google", "$shadePrefix.com.google") {
        exclude("com.google.protobuf.**")
    }
    relocate("com.spotify.futures", "$shadePrefix.com.spotify.futures")
    relocate("com.sun.activation", "$shadePrefix.com.sun.activation")
    relocate("com.thoughtworks.paranamer", "$shadePrefix.com.thoughtworks.paranamer")
    relocate("com.typesafe", "$shadePrefix.com.typesafe")
    relocate("com.yahoo.datasketches", "$shadePrefix.com.yahoo.datasketches")
    relocate("com.github.benmanes", "$shadePrefix.com.github.benmanes")
    relocate("io.airlift", "$shadePrefix.io.airlift")
    relocate("io.netty", "$shadePrefix.io.netty")
    relocate("io.swagger", "$shadePrefix.io.swagger")
    relocate("io.prometheus.client", "$shadePrefix.io.prometheus.client")
    relocate("it.unimi.dsi.fastutil", "$shadePrefix.it.unimi.dsi.fastutil")
    relocate("javax.activation", "$shadePrefix.javax.activation")
    relocate("javax.annotation", "$shadePrefix.javax.annotation")
    relocate("javax.ws", "$shadePrefix.javax.ws")
    relocate("org.apache.avro", "$shadePrefix.org.apache.avro")
    relocate("org.apache.bookkeeper", "$shadePrefix.org.apache.bookkeeper")
    relocate("org.apache.commons", "$shadePrefix.org.apache.commons")
    relocate("org.apache.pulsar.checksum", "$shadePrefix.org.apache.pulsar.checksum")
    relocate("org.asynchttpclient", "$shadePrefix.org.asynchttpclient")
    relocate("org.checkerframework", "$shadePrefix.org.checkerframework")
    relocate("org.codehaus.jackson", "$shadePrefix.org.codehaus.jackson")
    relocate("org.eclipse.jetty", "$shadePrefix.org.eclipse.jetty")
    relocate("org.objenesis", "$shadePrefix.org.objenesis")
    relocate("org.reactivestreams", "$shadePrefix.org.reactivestreams")
    relocate("org.roaringbitmap", "$shadePrefix.org.roaringbitmap")
    relocate("org.tukaani", "$shadePrefix.org.tukaani")
    relocate("org.yaml", "$shadePrefix.org.yaml")
}
