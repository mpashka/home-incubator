#!/bin/bash

#    --capture=method      per-test capturing method: one of fd|sys|no.
#    --capture=no == -s
#    -l, --showlocals      show locals in tracebacks (disabled by default).
#    --tb=style            traceback print mode (auto/long/short/line/native/no).

#    --log_dir out \
#    my_py_test \
#    my_py_test/my_test.py::test_4 \
../venv/bin/py.test -vl --tb=long \
    --capture=no \
    --log_dir out \
    my_py_test/my_test.py::test_grpc \
    $*

#    --reruns 1 $*
