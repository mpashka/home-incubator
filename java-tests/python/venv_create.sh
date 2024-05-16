#!/bin/bash

virtualenv --python /usr/bin/python2.7 venv
source venv/bin/activate
pip install -r venv.txt
