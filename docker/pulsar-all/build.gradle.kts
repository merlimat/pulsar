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

// Docker image module — no Java compilation needed
tasks.named("compileJava") { enabled = false }
tasks.named("compileTestJava") { enabled = false }
tasks.named("jar") { enabled = false }

val pulsarVersion = project.version.toString()
val dockerOrganization = findProperty("docker.organization") as String? ?: "apachepulsar"
val dockerImage = findProperty("docker.image") as String? ?: "pulsar"
val dockerTag = findProperty("docker.tag") as String? ?: "latest"
val dockerPlatforms = findProperty("docker.platforms") as String? ?: ""
val useWolfi = project.hasProperty("docker.wolfi")

// Copy offloader tarball into target/
val copyOffloaderTarball by tasks.registering(Copy::class) {
    dependsOn(":distribution:pulsar-offloader-distribution:offloaderDistTar")
    from(project(":distribution:pulsar-offloader-distribution").tasks.named("offloaderDistTar").map {
        (it as Tar).archiveFile
    })
    into(layout.buildDirectory.dir("target"))
}

val dockerBuild by tasks.registering(Exec::class) {
    group = "docker"
    description = "Build the Pulsar All-in-One Docker image"

    dependsOn(":docker:pulsar-docker-image:dockerBuild")
    dependsOn(":distribution:pulsar-io-distribution:ioDistDir")
    dependsOn(copyOffloaderTarball)

    val dockerfile = if (useWolfi) "Dockerfile.wolfi" else "Dockerfile"
    val imageName = "${dockerOrganization}/${dockerImage}-all:${dockerTag}"
    val pulsarImageName = "${dockerOrganization}/${dockerImage}:${dockerTag}"
    val offloaderTarballName = "apache-pulsar-offloaders-${pulsarVersion}-bin.tar.gz"

    // IO connectors directory from the IO distribution
    val ioDistDir = project(":distribution:pulsar-io-distribution").layout.buildDirectory
        .dir("apache-pulsar-io-connectors-${pulsarVersion}-bin").get().asFile

    workingDir = projectDir

    doFirst {
        val args = mutableListOf(
            "docker", "build",
            "-f", dockerfile,
            "-t", imageName,
            "--build-arg", "PULSAR_IMAGE=${pulsarImageName}",
            "--build-arg", "PULSAR_IO_DIR=${ioDistDir.absolutePath}",
            "--build-arg", "PULSAR_OFFLOADER_TARBALL=build/target/${offloaderTarballName}",
        )

        if (dockerPlatforms.isNotEmpty()) {
            args.addAll(listOf("--platform", dockerPlatforms))
        }

        args.add(".")

        commandLine(args)
    }
}

tasks.named("assemble") {
    dependsOn(dockerBuild)
}
