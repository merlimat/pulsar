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
val dockerOrganization = providers.gradleProperty("docker.organization").getOrElse("apachepulsar")
val dockerImage = providers.gradleProperty("docker.image").getOrElse("pulsar")
val dockerTag = providers.gradleProperty("docker.tag").getOrElse("latest")
val dockerPlatforms = providers.gradleProperty("docker.platforms").getOrElse("")
val useWolfi = providers.gradleProperty("docker.wolfi").isPresent
val kinesisKplImage = providers.gradleProperty("docker.kinesisKplImage")
    .getOrElse("apachepulsar/pulsar-io-kinesis-sink-kinesis_producer:1.0.4")

val ioDistTask = project(":distribution:pulsar-io-distribution").tasks.named("ioDistDir")
val offloaderDistTask = project(":distribution:pulsar-offloader-distribution").tasks.named("offloaderDistTar")

// Copy IO connectors into build context
val copyConnectors by tasks.registering(Sync::class) {
    dependsOn(ioDistTask)
    from(project(":distribution:pulsar-io-distribution").layout.buildDirectory
        .dir("apache-pulsar-io-connectors-${pulsarVersion}-bin"))
    into(layout.buildDirectory.dir("target/apache-pulsar-io-connectors-${pulsarVersion}-bin"))
}

// Copy offloader tarball into build context
val copyOffloaderTarball by tasks.registering(Copy::class) {
    dependsOn(offloaderDistTask)
    from(offloaderDistTask.map { (it as Tar).archiveFile })
    into(layout.buildDirectory.dir("target"))
}

val dockerBuild by tasks.registering(Exec::class) {
    group = "docker"
    description = "Build the Pulsar All-in-One Docker image"

    dependsOn(":docker:pulsar-docker-image:dockerBuild")
    dependsOn(copyConnectors)
    dependsOn(copyOffloaderTarball)

    val dockerfile = if (useWolfi) "Dockerfile.wolfi" else "Dockerfile"
    val imageName = "${dockerOrganization}/${dockerImage}-all:${dockerTag}"
    val pulsarImageName = "${dockerOrganization}/${dockerImage}:${dockerTag}"
    val offloaderTarballName = "apache-pulsar-offloaders-${pulsarVersion}-bin.tar.gz"
    val ioConnectorsDir = "build/target/apache-pulsar-io-connectors-${pulsarVersion}-bin"

    // Docker build context is the project directory
    workingDir = projectDir

    val args = mutableListOf(
        "docker", "build",
        "-f", dockerfile,
        "-t", imageName,
        "--build-arg", "PULSAR_IMAGE=${pulsarImageName}",
        "--build-arg", "PULSAR_IO_DIR=${ioConnectorsDir}",
        "--build-arg", "PULSAR_OFFLOADER_TARBALL=build/target/${offloaderTarballName}",
        "--build-arg", "PULSAR_IO_KINESIS_KPL_IMAGE=${kinesisKplImage}",
    )

    if (dockerPlatforms.isNotEmpty()) {
        args.addAll(listOf("--platform", dockerPlatforms))
    }

    args.add(".")

    commandLine(args)
}

tasks.named("assemble") {
    dependsOn(dockerBuild)
}
