#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cat > $SCRIPT_DIR/assets/config/build-info.yaml <<EOF
git_branch: $(git rev-parse --abbrev-ref HEAD)
git_commit: $(git describe --always --dirty)
build_date: $(date +%Y-%m-%d_%H:%M)
build_user: $USER
build_home: $HOSTNAME
build_info: "$USER $HOSTNAME $(date +%Y-%m-%d_%H:%M) $(git rev-parse --abbrev-ref HEAD)/$(git describe --always --dirty)"
EOF
