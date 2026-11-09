#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# TEMPORARY, fork-only C1 A/B (never to be merged): once a second, appends the runner's CPU counters and the CPU
# time of every java process to the file given as the argument.
#   T <epoch> <busy jiffies> <idle+iowait jiffies>
#   P <epoch> <pid> <kind> <utime> <stime> <starttime>
# kind: W1 a Gradle test worker with -XX:TieredStopAtLevel=1, W4 one without, D the Gradle daemon, O any other
# java process (the containers' JVMs in the integration jobs). utime, stime and starttime are clock ticks.

out="$1"
declare -A kind
echo "# hz=$(getconf CLK_TCK) nproc=$(nproc)" > "$out"
while true; do
  now=$(date +%s.%N)
  read -r _ u n s i w irq sirq st _ < /proc/stat
  echo "T $now $((u + n + s + irq + sirq + st)) $((i + w))" >> "$out"
  for p in $(pgrep -x java); do
    if [ -z "${kind[$p]}" ]; then
      cmd=$(tr '\0' ' ' < "/proc/$p/cmdline" 2>/dev/null) || continue
      case "$cmd" in
        *"Gradle Test Executor"*TieredStopAtLevel=1*|*TieredStopAtLevel=1*"Gradle Test Executor"*) kind[$p]=W1 ;;
        *"Gradle Test Executor"*) kind[$p]=W4 ;;
        *GradleDaemon*) kind[$p]=D ;;
        *) kind[$p]=O ;;
      esac
    fi
    stat=$(cat "/proc/$p/stat" 2>/dev/null) || continue
    read -r -a f <<< "${stat##*) }"
    echo "P $now $p ${kind[$p]} ${f[11]} ${f[12]} ${f[19]}" >> "$out"
  done
  sleep 1
done
