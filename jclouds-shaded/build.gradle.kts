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
    implementation(libs.jclouds.allblobstore)
    implementation("org.apache.jclouds.driver:jclouds-apachehc:${libs.versions.jclouds.get()}")
    implementation("org.apache.jclouds.driver:jclouds-okhttp:${libs.versions.jclouds.get()}") {
        exclude(group = "com.squareup.okhttp")
    }
    implementation("org.apache.jclouds.driver:jclouds-slf4j:${libs.versions.jclouds.get()}")
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("com.google.common", "org.apache.pulsar.shade.com.google.common")
    relocate("com.google.errorprone", "org.apache.pulsar.shade.com.google.errorprone")
    relocate("com.jamesmurty.utils", "org.apache.pulsar.shade.com.jamesmurty.utils")
    relocate("aopalliance", "org.apache.pulsar.shade.aopalliance")
    relocate("net.iharder", "org.apache.pulsar.shade.net.iharder")
    relocate("jakarta", "org.apache.pulsar.shade.jakarta")
    relocate("org.aopalliance", "org.apache.pulsar.shade.org.aopalliance")
}
