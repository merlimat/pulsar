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
    alias(libs.plugins.nar)
}
dependencies {
    compileOnly(project(":managed-ledger"))
    compileOnly(libs.bookkeeper.server)
    compileOnly(libs.netty.buffer)
    implementation(project(":jclouds-shaded"))
    compileOnly(libs.jclouds.allblobstore)
    compileOnly(libs.jclouds.blobstore)
    implementation(libs.aws.java.sdk.core)
    implementation(libs.aws.java.sdk.sts)
    runtimeOnly(libs.jakarta.xml.bind.api)
    runtimeOnly(libs.jakarta.activation)

    testImplementation(project(":managed-ledger"))
    testImplementation(project(":testmocks"))
    testImplementation(libs.guava)
    testImplementation(libs.netty.buffer)
    testImplementation(libs.bookkeeper.server)
    testImplementation(libs.jclouds.blobstore)
    testImplementation(libs.jclouds.allblobstore)
    testImplementation(libs.simpleclient)
}
