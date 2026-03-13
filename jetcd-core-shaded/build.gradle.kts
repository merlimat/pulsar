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
    implementation(libs.jetcd.core) {
        exclude(group = "io.grpc", module = "grpc-netty")
        exclude(group = "io.netty")
        exclude(group = "javax.annotation", module = "javax.annotation-api")
    }
    implementation(libs.grpc.netty.shaded)
    implementation("dev.failsafe:failsafe:${libs.versions.failsafe.get()}")
    implementation("io.grpc:grpc-protobuf:${libs.versions.grpc.get()}")
    implementation(libs.grpc.stub)
    implementation("io.grpc:grpc-grpclb:${libs.versions.grpc.get()}")
    implementation("io.grpc:grpc-util:1.60.0")
}

tasks.shadowJar {
    archiveClassifier.set("shaded")
    relocate("io.vertx", "org.apache.pulsar.shade.io.vertx")
    relocate("io.grpc.netty", "org.apache.pulsar.shade.io.grpc.netty")
    relocate("io.netty", "org.apache.pulsar.shade.io.netty")
}

// Replace the default jar with the shadow jar for downstream consumers
tasks.jar {
    archiveClassifier.set("original")
}

// Expose shadow jar as the primary artifact
configurations {
    runtimeElements {
        outgoing {
            artifacts.clear()
            artifact(tasks.shadowJar)
        }
    }
    apiElements {
        outgoing {
            artifacts.clear()
            artifact(tasks.shadowJar)
        }
    }
}
