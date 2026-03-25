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
    implementation(project(":pulsar-client-api"))
    implementation(project(":pulsar-client-original")) {
        exclude(group = "it.unimi.dsi", module = "fastutil")
    }
    implementation(project(":pulsar-client-dependencies-minimized"))
    implementation(project(":pulsar-client-admin-original"))
    implementation(project(":pulsar-client-messagecrypto-bc"))

    testImplementation(libs.log4j.api)
    testImplementation(libs.log4j.core)
    testImplementation(libs.log4j.slf4j2.impl)
}

val shadePrefix = "org.apache.pulsar.shade"

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()

    dependencies {
        include(project(":pulsar-client-original"))
        include(project(":pulsar-client-admin-original"))
        include(project(":pulsar-common"))
        include(project(":pulsar-client-messagecrypto-bc"))
        include(project(":pulsar-client-dependencies-minimized"))
        include(dependency("com.fasterxml.jackson.*:.*"))
        include(dependency("com.google.*:.*"))
        include(dependency("com.google.auth:.*"))
        include(dependency("com.google.code.findbugs:.*"))
        include(dependency("com.google.code.gson:gson"))
        include(dependency("com.google.errorprone:.*"))
        include(dependency("com.google.guava:.*"))
        include(dependency("com.google.j2objc:.*"))
        include(dependency("com.google.re2j:re2j"))
        include(dependency("com.spotify:completable-futures"))
        include(dependency("com.squareup.*:.*"))
        include(dependency("com.sun.activation:jakarta.activation"))
        include(dependency("com.thoughtworks.paranamer:paranamer"))
        include(dependency("com.typesafe.netty:netty-reactive-streams"))
        include(dependency("com.yahoo.datasketches:.*"))
        include(dependency("commons-.*:.*"))
        include(dependency("io.airlift:.*"))
        include(dependency("io.grpc:.*"))
        include(dependency("io.netty.incubator:.*"))
        include(dependency("io.netty:.*"))
        include(dependency("io.opencensus:.*"))
        include(dependency("io.perfmark:.*"))
        include(dependency("io.swagger:.*"))
        include(dependency("jakarta.activation:jakarta.activation-api"))
        include(dependency("jakarta.annotation:jakarta.annotation-api"))
        include(dependency("jakarta.ws.rs:jakarta.ws.rs-api"))
        include(dependency("jakarta.xml.bind:jakarta.xml.bind-api"))
        include(dependency("javax.ws.rs:.*"))
        include(dependency("javax.xml.bind:jaxb-api"))
        include(dependency("org.apache.avro:.*"))
        include(dependency("org.apache.bookkeeper:.*"))
        include(dependency("org.apache.commons:commons-compress"))
        include(dependency("org.apache.commons:commons-lang3"))
        include(dependency("org.asynchttpclient:.*"))
        include(dependency("org.checkerframework:.*"))
        include(dependency("org.eclipse.jetty:.*"))
        include(dependency("org.glassfish.hk2.*:.*"))
        include(dependency("org.glassfish.jersey.*:.*"))
        include(dependency("org.javassist:javassist"))
        include(dependency("org.jvnet.mimepull:.*"))
        include(dependency("org.objenesis:.*"))
        include(dependency("org.reactivestreams:reactive-streams"))
        include(dependency("org.tukaani:xz"))
        include(dependency("org.yaml:snakeyaml"))
        include(dependency("org.roaringbitmap:RoaringBitmap"))
        exclude(dependency("com.fasterxml.jackson.core:jackson-annotations"))
        exclude(dependency("com.google.protobuf:protobuf-java"))
        exclude(dependency("io.netty:netty-transport-native-kqueue"))
    }

    // Exclude bouncycastle from pulsar-client (signatures would break if shaded)
    exclude("org/bouncycastle/**")
    // Exclude common META-INF noise
    exclude("**/module-info.class")
    exclude("findbugsExclude.xml")
    exclude("META-INF/*-LICENSE")
    exclude("META-INF/*-NOTICE")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/*.SF")
    exclude("META-INF/DEPENDENCIES*")
    exclude("META-INF/io.netty.versions.properties")
    exclude("META-INF/LICENSE*")
    exclude("META-INF/license/**")
    exclude("META-INF/maven/**")
    exclude("META-INF/native-image/**")
    exclude("META-INF/NOTICE*")
    exclude("META-INF/proguard/**")

    relocate("com.fasterxml.jackson", "$shadePrefix.com.fasterxml.jackson") {
        exclude("com.fasterxml.jackson.annotation.*")
    }
    relocate("com.google", "$shadePrefix.com.google") {
        exclude("com.google.protobuf.**")
    }
    relocate("com.spotify.futures", "$shadePrefix.com.spotify.futures")
    relocate("com.squareup", "$shadePrefix.com.squareup")
    relocate("com.sun.activation", "$shadePrefix.com.sun.activation")
    relocate("com.thoughtworks.paranamer", "$shadePrefix.com.thoughtworks.paranamer")
    relocate("com.typesafe", "$shadePrefix.com.typesafe")
    relocate("com.yahoo", "$shadePrefix.com.yahoo")
    relocate("com.github.benmanes", "$shadePrefix.com.github.benmanes")
    relocate("io.airlift", "$shadePrefix.io.airlift")
    relocate("io.grpc", "$shadePrefix.io.grpc")
    relocate("io.netty", "$shadePrefix.io.netty")
    relocate("io.opencensus", "$shadePrefix.io.opencensus")
    relocate("io.swagger", "$shadePrefix.io.swagger")
    relocate("it.unimi.dsi.fastutil", "$shadePrefix.it.unimi.dsi.fastutil")
    relocate("javassist", "$shadePrefix.javassist")
    relocate("javax.activation", "$shadePrefix.javax.activation")
    relocate("javax.annotation", "$shadePrefix.javax.annotation")
    relocate("javax.inject", "$shadePrefix.javax.inject")
    relocate("javax.ws", "$shadePrefix.javax.ws")
    relocate("javax.xml.bind", "$shadePrefix.javax.xml.bind")
    relocate("jersey", "$shadePrefix.jersey")
    relocate("okio", "$shadePrefix.okio")
    relocate("org.aopalliance", "$shadePrefix.org.aopalliance")
    relocate("org.apache.avro", "$shadePrefix.org.apache.avro") {
        exclude("org.apache.avro.reflect.AvroAlias")
        exclude("org.apache.avro.reflect.AvroDefault")
        exclude("org.apache.avro.reflect.AvroEncode")
        exclude("org.apache.avro.reflect.AvroIgnore")
        exclude("org.apache.avro.reflect.AvroMeta")
        exclude("org.apache.avro.reflect.AvroName")
        exclude("org.apache.avro.reflect.AvroSchema")
        exclude("org.apache.avro.reflect.Nullable")
        exclude("org.apache.avro.reflect.Stringable")
        exclude("org.apache.avro.reflect.Union")
    }
    relocate("org.apache.bookkeeper", "$shadePrefix.org.apache.bookkeeper")
    relocate("org.apache.commons", "$shadePrefix.org.apache.commons")
    relocate("org.apache.pulsar.policies", "$shadePrefix.org.apache.pulsar.policies") {
        exclude("org.apache.pulsar.policies.data.loadbalancer.LoadManagerReport")
        exclude("org.apache.pulsar.policies.data.loadbalancer.NamespaceBundleStats")
        exclude("org.apache.pulsar.policies.data.loadbalancer.ResourceUsage")
        exclude("org.apache.pulsar.policies.data.loadbalancer.ServiceLookupData")
    }
    relocate("org.asynchttpclient", "$shadePrefix.org.asynchttpclient")
    relocate("org.checkerframework", "$shadePrefix.org.checkerframework")
    relocate("org.codehaus.jackson", "$shadePrefix.org.codehaus.jackson")
    relocate("org.eclipse.jetty", "$shadePrefix.org.eclipse")
    relocate("org.glassfish", "$shadePrefix.org.glassfish")
    relocate("org.jvnet", "$shadePrefix.org.jvnet")
    relocate("org.objenesis", "$shadePrefix.org.objenesis")
    relocate("org.reactivestreams", "$shadePrefix.org.reactivestreams")
    relocate("org.roaringbitmap", "$shadePrefix.org.roaringbitmap")
    relocate("org.tukaani", "$shadePrefix.org.tukaani")
    relocate("org.yaml", "$shadePrefix.org.yaml")
    // NOTE: Do NOT shade log4j, otherwise logging won't work

    // The shadow plugin's relocate() doesn't handle property file VALUES.
    // AsyncHttpClient's ahc-default.properties contains property names prefixed with
    // "org.asynchttpclient." which must be updated to the relocated package name.
    filesMatching("org/asynchttpclient/config/ahc-default.properties") {
        filter { line -> line.replace("org.asynchttpclient.", "org.apache.pulsar.shade.org.asynchttpclient.") }
    }

    // Relocate Netty native library filenames to avoid conflicts with unshaded Netty
    filesMatching("META-INF/native/**") {
        if (name.matches(Regex("netty.+\\.(so|jnilib|dll)"))) {
            path = path.replace(name, "org_apache_pulsar_shade_$name")
        }
    }
}
