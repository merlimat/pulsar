#!/usr/bin/env python2

import sys, csv, os
import xml.etree.ElementTree as ET

def writeCsv(xmlFile):
    tree = ET.parse(xmlFile)
    root = tree.getroot()
    writer = csv.writer(sys.stdout)
    project = os.environ["CI_PROJECT_NAME"] \
              if "CI_PROJECT_NAME" in os.environ else "unknown"
    branch = os.environ["CI_COMMIT_REF_NAME"] \
             if "CI_COMMIT_REF_NAME" in os.environ else "unknown"
    url = os.environ["CI_JOB_URL"] \
          if "CI_JOB_URL" in os.environ else "unknown"
    for test in root.iter("testcase"):
        failure = test.find("failure")
        time = (float(test.attrib["time"]) if "time" in test.attrib else 0.0)
        writer.writerow(["TESTRESULT",
                         project,
                         branch,
                         root.attrib["name"],
                         test.attrib["name"],
                         time,
                         "FAIL" if failure != None else "SUCCESS",
                         url])

alltests = []
for i in sys.argv[1:]:
    writeCsv(i)
