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
    implementation(project(":pulsar-functions:pulsar-functions-local-runner-original"))
}

val shadePrefix = "org.apache.pulsar.functions.runtime.shaded"

tasks.shadowJar {
    archiveClassifier.set("")
    isZip64 = true
    mergeServiceFiles()

    dependencies {
        include(dependency("org.apache.pulsar:.*"))
        include(project(":pulsar-functions:pulsar-functions-local-runner-original"))
        include(project(":pulsar-client-original"))
        include(project(":pulsar-common"))
        include(project(":pulsar-client-admin-original"))
        include(dependency("org.apache.bookkeeper:.*"))
        include(dependency("commons-.*:.*"))
        include(dependency("org.apache.commons:.*"))
        include(dependency("com.fasterxml.jackson.*:.*"))
        include(dependency("io.netty:.*"))
        include(dependency("io.netty.incubator:.*"))
        include(dependency("com.google.*:.*"))
        include(dependency("javax.servlet:.*"))
        include(dependency("org.reactivestreams:reactive-streams"))
        include(dependency("io.swagger:.*"))
        include(dependency("org.yaml:snakeyaml"))
        include(dependency("io.perfmark:.*"))
        include(dependency("io.prometheus:.*"))
        include(dependency("io.prometheus.jmx:.*"))
        include(dependency("javax.ws.rs:.*"))
        include(dependency("org.tukaani:xz"))
        include(dependency("com.github.zafarkhaja:java-semver"))
        include(dependency("net.java.dev.jna:.*"))
        include(dependency("org.apache.zookeeper:.*"))
        include(dependency("com.thoughtworks.paranamer:paranamer"))
        include(dependency("jline:.*"))
        include(dependency("org.rocksdb:.*"))
        include(dependency("org.eclipse.jetty.*:.*"))
        include(dependency("org.apache.avro:avro"))
        include(dependency("info.picocli:.*"))
        include(dependency("net.jodah:.*"))
        include(dependency("io.airlift:.*"))
        include(dependency("com.yahoo.datasketches:.*"))
    }

    // Exclude bouncycastle from pulsar-client (signatures would break if shaded)
    exclude("org/bouncycastle/**")

    relocate("com.google", "$shadePrefix.com.google")
    relocate("org.apache.jute", "$shadePrefix.org.apache.jute")
    relocate("javax.servlet", "$shadePrefix.javax.servlet")
    relocate("net.jodah", "$shadePrefix.net.jodah")
    relocate("javax.ws.rs", "$shadePrefix.javax.ws.rs")
    relocate("org.lz4", "$shadePrefix.org.lz4")
    relocate("org.reactivestreams", "$shadePrefix.org.reactivestreams")
    relocate("org.apache.commons", "$shadePrefix.org.apache.commons")
    relocate("io.swagger", "$shadePrefix.io.swagger")
    relocate("org.yaml", "$shadePrefix.org.yaml")
    relocate("org.jctools", "$shadePrefix.org.jctools")
    relocate("com.squareup.okhttp", "$shadePrefix.com.squareup.okhttp")
    relocate("io.grpc", "$shadePrefix.io.grpc")
    relocate("io.perfmark", "$shadePrefix.io.perfmark")
    relocate("org.joda", "$shadePrefix.org.joda")
    relocate("io.kubernetes", "$shadePrefix.io.kubernetes")
    relocate("io.opencensus", "$shadePrefix.io.opencensus")
    relocate("net.jpountz", "$shadePrefix.net.jpountz")
    relocate("org.tukaani", "$shadePrefix.org.tukaani")
    relocate("com.github", "$shadePrefix.com.github")
    relocate("commons-io", "$shadePrefix.commons-io")
    relocate("org.apache.distributedlog", "$shadePrefix.org.apache.distributedlog")
    relocate("com.fasterxml", "$shadePrefix.com.fasterxml")
    relocate("org.inferred", "$shadePrefix.org.inferred")
    relocate("org.apache.bookkeeper", "$shadePrefix.org.apache.bookkeeper")
    relocate("org.bookkeeper", "$shadePrefix.org.bookkeeper")
    relocate("dlshade", "$shadePrefix.dlshade")
    relocate("org.codehaus.jackson", "$shadePrefix.org.codehaus.jackson")
    relocate("net.java.dev.jna", "$shadePrefix.net.java.dev.jna")
    relocate("org.apache.curator", "$shadePrefix.org.apache.curator")
    relocate("javax.validation", "$shadePrefix.javax.validation")
    relocate("javax.activation", "$shadePrefix.javax.activation")
    relocate("io.prometheus", "$shadePrefix.io.prometheus")
    relocate("org.apache.zookeeper", "$shadePrefix.org.apache.zookeeper")
    relocate("io.jsonwebtoken", "$shadePrefix.io.jsonwebtoken")
    relocate("commons-codec", "$shadePrefix.commons-codec")
    relocate("com.thoughtworks.paranamer", "$shadePrefix.com.thoughtworks.paranamer")
    relocate("org.codehaus.mojo", "$shadePrefix.org.codehaus.mojo")
    relocate("com.github.luben", "$shadePrefix.com.github.luben")
    relocate("jline", "$shadePrefix.jline")
    relocate("org.xerial.snappy", "$shadePrefix.org.xerial.snappy")
    relocate("javax.annotation", "$shadePrefix.javax.annotation")
    relocate("org.checkerframework", "$shadePrefix.org.checkerframework")
    relocate("org.apache.yetus", "$shadePrefix.org.apache.yetus")
    relocate("commons-cli", "$shadePrefix.commons-cli")
    relocate("commons-lang", "$shadePrefix.commons-lang")
    relocate("com.squareup.okio", "$shadePrefix.com.squareup.okio")
    relocate("org.rocksdb", "$shadePrefix.org.rocksdb")
    relocate("org.objenesis", "$shadePrefix.org.objenesis")
    relocate("org.eclipse.jetty", "$shadePrefix.org.eclipse.jetty")
    relocate("org.apache.avro", "$shadePrefix.org.apache.avro")
    relocate("avro.shaded", "$shadePrefix.avro.shaded")
    relocate("com.yahoo.datasketches", "org.apache.pulsar.shaded.com.yahoo.datasketches")
    relocate("com.yahoo.sketches", "org.apache.pulsar.shaded.com.yahoo.sketches")
    relocate("info.picocli", "$shadePrefix.info.picocli")
    relocate("io.netty", "$shadePrefix.io.netty")
    relocate("org.hamcrest", "$shadePrefix.org.hamcrest")
    relocate("aj.org", "$shadePrefix.aj.org")
    relocate("com.scurrilous", "$shadePrefix.com.scurrilous")
    relocate("okio", "$shadePrefix.okio")
    relocate("org.asynchttpclient", "$shadePrefix.org.asynchttpclient")
    relocate("io.airlift", "$shadePrefix.io.airlift")
    // NOTE: Do NOT shade log4j, otherwise logging won't work

    // The shadow plugin's relocate() doesn't handle property file VALUES.
    // AsyncHttpClient's ahc-default.properties contains property names prefixed with
    // "org.asynchttpclient." which must be updated to the relocated package name.
    filesMatching("org/asynchttpclient/config/ahc-default.properties") {
        filter { line -> line.replace("org.asynchttpclient.", "org.apache.pulsar.shade.org.asynchttpclient.") }
    }
}
