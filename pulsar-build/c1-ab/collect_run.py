#!/usr/bin/env python3
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

# TEMPORARY, fork-only C1 A/B (never to be merged): moves the JUnit XML reports of one run of a test group into the
# directory given as the argument, named <module>__<task>__TEST-<class>.xml. The output of the suites whose tests all
# passed on the first attempt is dropped to keep the artifact small; suites with a failure, an error or a skipped
# (retried) attempt keep theirs.

import glob
import os
import shutil
import sys
import xml.etree.ElementTree as ET

dest = sys.argv[1]
os.makedirs(dest, exist_ok=True)
moved = stripped = 0
for path in glob.glob("**/build/test-results/*/TEST-*.xml", recursive=True):
    if path.startswith("test-reports/"):
        continue
    module, rest = path.split("/build/test-results/", 1)
    task = rest.split("/", 1)[0]
    target = os.path.join(dest, "%s__%s__%s" % (module.replace("/", "_"), task, os.path.basename(path)))
    try:
        tree = ET.parse(path)
        root = tree.getroot()
        if all(int(root.get(a, "0")) == 0 for a in ("failures", "errors", "skipped")):
            for parent in [root] + root.findall("testcase"):
                for tag in ("system-out", "system-err"):
                    for element in parent.findall(tag):
                        element.text = None
            tree.write(target, encoding="UTF-8", xml_declaration=True)
            os.remove(path)
            stripped += 1
            moved += 1
            continue
    except (ET.ParseError, ValueError) as e:
        print("Keeping %s as is: %s" % (path, e))
    shutil.move(path, target)
    moved += 1
print("Moved %d JUnit reports to %s (%d without output)" % (moved, dest, stripped))
