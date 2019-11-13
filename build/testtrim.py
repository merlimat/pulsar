#!/usr/bin/env python2

import sys, json
import xml.etree.ElementTree as ET

def trimtest(xmlFile):
    tree = ET.parse(xmlFile)
    root = tree.getroot()
    tests = []
    sysout = root.find("system-out")
    if sysout is not None:
        sysout.clear()
        tree.write(xmlFile)

for i in sys.argv[1:]:
    trimtest(i)

