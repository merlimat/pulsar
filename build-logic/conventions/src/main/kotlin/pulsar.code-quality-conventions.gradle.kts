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
    id("com.diffplug.spotless")
}

// ── Spotless (formatting + license headers) ───────────────────────────────
val asfLicenseHeader = rootProject.file("src/license-header.txt").readText()
val asfLicenseHeaderJava = "/*\n" + asfLicenseHeader.lines()
    .map { " * $it".trimEnd() }
    .joinToString("\n") + "/\n"
val asfLicenseHeaderJavadoc = asfLicenseHeaderJava.replaceFirst("/*", "/**")
configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        targetExclude(
            "**/AbstractCASReferenceCounted.java",
            "**/generated/**",
            "**/generated-lightproto/**",
            "**/generated-sources/**",
            "build/**",
        )
        eclipse().configFile(rootProject.file("buildtools/src/main/resources/pulsar/pulsar-formatter.xml"))
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        licenseHeader(asfLicenseHeaderJava, "(\\n|package|import|public|class|module) ?")
    }

    format("proto") {
        target("src/*/proto/**/*.proto")
        licenseHeader(asfLicenseHeaderJavadoc, "\\n|syntax")
    }
}
