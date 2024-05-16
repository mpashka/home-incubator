#!/bin/bash

#    --logs_dir out \
venv/bin/py.test -svl --tb=long \
    my_py_test \
    --reruns 5 $*
