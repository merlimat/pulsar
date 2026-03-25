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

# shell function library for Pulsar CI builds

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

set -e
set -o pipefail

ARTIFACT_RETENTION_DAYS="${ARTIFACT_RETENTION_DAYS:-3}"

# lists all available functions in this tool
function ci_list_functions() {
  declare -F | awk '{print $NF}' | sort | grep -E '^ci_' | sed 's/^ci_//'
}

# prints thread dumps for all running JVMs
# used in CI when a job gets cancelled because of a job timeout
function ci_print_thread_dumps() {
  for java_pid in $(jps -q -J-XX:+PerfDisableSharedMem); do
    echo "----------------------- pid $java_pid -----------------------"
    cat /proc/$java_pid/cmdline | xargs -0 echo
    jcmd $java_pid Thread.print -l
    jcmd $java_pid GC.heap_info
  done
  return 0
}

# installs a tool executable if it's not found on the PATH
function ci_install_tool() {
  local tool_executable=$1
  local tool_package=${2:-$1}
  if ! command -v $tool_executable &>/dev/null; then
    if [[ "$GITHUB_ACTIONS" == "true" ]]; then
      echo "::group::Installing ${tool_package}"
      sudo apt-get -y install ${tool_package} >/dev/null || {
        echo "Installing the package failed. Switching the ubuntu mirror and retrying..."
        # retry after picking the ubuntu mirror
        sudo apt-get -y install ${tool_package}
      }
      echo '::endgroup::'
    else
      fail "$tool_executable wasn't found on PATH. You should first install $tool_package with your package manager."
    fi
  fi
}

# outputs the given message to stderr and exits the shell script
function fail() {
  echo "$*" >&2
  exit 1
}

# copies test reports into test-reports and test-results directories
# subsequent runs of tests might overwrite previous reports. This ensures that all test runs get reported.
function ci_move_test_reports() {
  (
    if [ -n "${GITHUB_WORKSPACE}" ]; then
      cd "${GITHUB_WORKSPACE}"
      mkdir -p test-reports
      mkdir -p test-results
    fi
    # aggregate all junit xml reports in a single directory
    if [ -d test-reports ]; then
      # copy test reports to single directory, rename duplicates
      # Gradle test reports
      find . -path '*/build/test-results/*/TEST-*.xml' -print0 | xargs -0 -r -n 1 mv -t test-reports --backup=numbered
      # rename possible duplicates to have ".xml" extension
      (
        for f in test-reports/*~; do
          mv -- "$f" "${f}.xml"
        done 2>/dev/null
      ) || true
    fi
    # aggregate all test-results in a single directory
    if [ -d test-results ]; then
      (
        # Gradle test-results directories
        find . -type d -path '*/build/test-results/test' -not -path './test-results/*' |
          while IFS=$'\n' read -r directory; do
            echo "Copying Gradle reports from $directory"
            target_dir="test-results/${directory}"
            if [ -d "$target_dir" ]; then
              ( command ls -vr1d "${target_dir}~"* 2> /dev/null | awk '{print "mv "$0" "substr($0,0,length-1)substr($0,length,1)+1}' | sh ) || true
              mv "$target_dir" "${target_dir}~1"
            fi
            cp -R --parents "$directory" test-results
            rm -rf "$directory"
          done
      )
    fi
  )
}

function ci_check_ready_to_test() {
  if [[ -z "$GITHUB_EVENT_PATH" ]]; then
    >&2 echo "GITHUB_EVENT_PATH isn't set"
    return 1
  fi

  PR_JSON_URL=$(jq -r '.pull_request.url' "${GITHUB_EVENT_PATH}")
  echo "Refreshing $PR_JSON_URL..."
  PR_JSON=$(curl -s -H "Authorization: Bearer $GITHUB_TOKEN" "${PR_JSON_URL}")

  if printf "%s" "${PR_JSON}" | jq -e '.draft | select(. == true)' &> /dev/null; then
    echo "PR is draft."
  elif ! ( printf "%s" "${PR_JSON}" | jq -e '.mergeable | select(. == true)' &> /dev/null ); then
    echo "PR isn't mergeable."
  else
    # check ready-to-test label
    if printf "%s" "${PR_JSON}" | jq -e '.labels[] | .name | select(. == "ready-to-test")' &> /dev/null; then
      echo "Found ready-to-test label."
      return 0
    else
      echo "There is no ready-to-test label on the PR."
    fi

    # check if the PR has been approved
    PR_NUM=$(jq -r '.pull_request.number' "${GITHUB_EVENT_PATH}")
    REPO_FULL_NAME=$(jq -r '.repository.full_name' "${GITHUB_EVENT_PATH}")
    REPO_NAME=$(basename "${REPO_FULL_NAME}")
    REPO_OWNER=$(dirname "${REPO_FULL_NAME}")
    # use graphql query to find out reviewDecision
    PR_REVIEW_DECISION=$(curl -s -H "Authorization: Bearer $GITHUB_TOKEN" -X POST -d '{"query": "query { repository(name: \"'${REPO_NAME}'\", owner: \"'${REPO_OWNER}'\") { pullRequest(number: '${PR_NUM}') { reviewDecision } } }"}' https://api.github.com/graphql |jq -r '.data.repository.pullRequest.reviewDecision')
    echo "Review decision for PR #${PR_NUM} in repository ${REPO_OWNER}/${REPO_NAME} is ${PR_REVIEW_DECISION}"
    if [[ "$PR_REVIEW_DECISION" == "APPROVED" ]]; then
      return 0
    fi
  fi

  FORK_REPO_URL=$(jq -r '.pull_request.head.repo.html_url' "$GITHUB_EVENT_PATH")
  PR_BRANCH_LABEL=$(jq -r '.pull_request.head.label' "$GITHUB_EVENT_PATH")
  PR_BASE_BRANCH=$(jq -r '.pull_request.base.ref' "$GITHUB_EVENT_PATH")
  PR_URL=$(jq -r '.pull_request.html_url' "$GITHUB_EVENT_PATH")
  FORK_PR_TITLE_URL_ENCODED=$(printf "%s" "${PR_JSON}" | jq -r '"[run-tests] " + .title | @uri')
  FORK_PR_BODY_URL_ENCODED=$(jq -n -r "\"This PR is for running tests for upstream PR ${PR_URL}.\n\n<!-- Before creating this PR, please ensure that the fork $FORK_REPO_URL is up to date with https://github.com/apache/pulsar -->\" | @uri")
  if [[ "$PR_BASE_BRANCH" != "master" ]]; then
    sync_non_master_fork_docs=$(cat <<EOF
 \\$('\n')
   If ${FORK_REPO_URL}/tree/${PR_BASE_BRANCH} is missing, you must sync the branch ${PR_BASE_BRANCH} on the command line.
   \`\`\`
   git fetch https://github.com/apache/pulsar ${PR_BASE_BRANCH}
   git push ${FORK_REPO_URL} FETCH_HEAD:refs/heads/${PR_BASE_BRANCH}
   \`\`\`
EOF
)
  else
    sync_non_master_fork_docs=""
  fi

  >&2 tee -a "$GITHUB_STEP_SUMMARY" <<EOF

# Instructions for proceeding with the pull request:

apache/pulsar pull requests should be first tested in your own fork since the apache/pulsar CI based on
GitHub Actions has constrained resources and quota. GitHub Actions provides separate quota for
pull requests that are executed in a forked repository.

1. Go to ${FORK_REPO_URL}/tree/${PR_BASE_BRANCH} and ensure that your ${PR_BASE_BRANCH} branch is up to date
   with https://github.com/apache/pulsar \\
   [Sync your fork if it's behind.](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/syncing-a-fork)${sync_non_master_fork_docs}
2. Open a pull request to your own fork. You can use this link to create the pull request in
   your own fork:
   [Create PR in fork for running tests](${FORK_REPO_URL}/compare/${PR_BASE_BRANCH}...${PR_BRANCH_LABEL}?expand=1&title=${FORK_PR_TITLE_URL_ENCODED}&body=${FORK_PR_BODY_URL_ENCODED})
3. Edit the description of the pull request ${PR_URL} and add the link to the PR that you opened to your own fork
   so that the reviewer can verify that tests pass in your own fork.
4. Ensure that tests pass in your own fork. Your own fork will be used to run the tests during the PR review
   and any changes made during the review. You as a PR author are responsible for following up on test failures.
   Please report any flaky tests as new issues at https://github.com/apache/pulsar/issues
   after checking that the flaky test isn't already reported.
5. When the PR is approved, it will be possible to restart the Pulsar CI workflow within apache/pulsar
   repository by adding a comment "/pulsarbot rerun-failure-checks" to the PR.
   An alternative for the PR approval is to add a ready-to-test label to the PR. This can be done
   by Apache Pulsar committers.
6. When tests pass on the apache/pulsar side, the PR can be merged by a Apache Pulsar Committer.

If you have any trouble you can get support in multiple ways:
* by sending email to the [dev mailing list](mailto:dev@pulsar.apache.org) ([subscribe](mailto:dev-subscribe@pulsar.apache.org))
* on the [#contributors channel on Pulsar Slack](https://apache-pulsar.slack.com/channels/contributors) ([join](https://pulsar.apache.org/community#section-discussions))
* in apache/pulsar [GitHub discussions Q&A](https://github.com/apache/pulsar/discussions/categories/q-a)

EOF
  return 1
}

ci_report_netty_leaks() {
  if [ -z "$NETTY_LEAK_DUMP_DIR" ]; then
    echo "NETTY_LEAK_DUMP_DIR isn't set"
    return 0
  fi
  local temp_file=$(mktemp -t netty_leak.XXXX)

  # concat all netty_leak_*.txt files in the dump directory to a temp file
  if [ -d "$NETTY_LEAK_DUMP_DIR" ]; then
    find "$NETTY_LEAK_DUMP_DIR" -maxdepth 1 -type f -name "netty_leak_*.txt" -exec cat {} \; >> $temp_file
  fi

  # check if there are any netty_leak_*.txt files in the container logs
  local container_logs_dir="tests/integration/target/container-logs"
  if [ -d "$container_logs_dir" ]; then
    local container_netty_leak_dump_dir="$NETTY_LEAK_DUMP_DIR/container-logs"
    mkdir -p "$container_netty_leak_dump_dir"
    while read -r file; do
      # example file name "tests/integration/target/container-logs/ltnizrzm-standalone/var-log-pulsar.tar.gz"
      # take ltnizrzm-standalone part
      container_name=$(basename "$(dirname "$file")")
      target_dir="$container_netty_leak_dump_dir/$container_name"
      mkdir -p "$target_dir"
      tar -C "$target_dir" -zxf "$file" --strip-components=1 --wildcards --wildcards-match-slash '*/netty_leak_*.txt' >/dev/null 2>&1 || true
    done < <(find "$container_logs_dir" -type f -name "*.tar.gz")
    # remove all empty directories
    find "$container_netty_leak_dump_dir" -type d -empty -delete
    # print all netty_leak_*.txt files in the container logs dump directory to the temp file
    if [ -d "$container_netty_leak_dump_dir" ]; then
      find "$container_netty_leak_dump_dir" -type f -name "netty_leak_*.txt" -exec cat {} \; >> $temp_file
    fi
  fi

  if [ -s $temp_file ]; then
    local leak_found_log_message
    if [[ "$NETTY_LEAK_DETECTION" == "fail_on_leak" ]]; then
      leak_found_log_message="::error::Netty leaks found. Failing the build since Netty leak detection is set to 'fail_on_leak'."
    else
      leak_found_log_message="::warning::Netty leaks found."
    fi
    {
      echo "${leak_found_log_message}"
      local test_file_locations=$(grep -h -i test $temp_file | grep org.apache | sed 's/^[[:space:]]*//;s/[[:space:]]*$//;s/^Hint: //' | sort -u || true)
      if [[ -n "$test_file_locations" ]]; then
        echo "Test file locations in stack traces:"
        echo
        echo "$test_file_locations"
      fi
      echo "Details:"
      cat $temp_file
    } | tee $NETTY_LEAK_DUMP_DIR/leak_report.txt
    touch pulsar-build/netty_leaks_found
    if [[ "$NETTY_LEAK_DETECTION" == "fail_on_leak" ]]; then
      exit 1
    fi
  else
    echo "No netty leaks found."
    touch pulsar-build/netty_leaks_not_found
  fi
  rm $temp_file
}

if [ -z "$1" ]; then
  echo "usage: $0 [ci_tool_function_name]"
  echo "Available ci tool functions:"
  ci_list_functions
  exit 1
fi
ci_function_name="ci_$1"
shift

if [[ "$(LC_ALL=C type -t "${ci_function_name}")" == "function" ]]; then
  eval "$ci_function_name" "$@"
else
  echo "Invalid ci tool function"
  echo "Available ci tool functions:"
  ci_list_functions
  exit 1
fi
