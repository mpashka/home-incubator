#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd $SCRIPT_DIR/quarkus-be
$SCRIPT_DIR/quarkus-be/mvnw package -Dquarkus.container-image.build=true && \
  docker push mpashka/totemftc-be:1.0.0-SNAPSHOT
