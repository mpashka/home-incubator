#!/bin/bash

trap_handler() { sig=$1
  echo "[$sig] Trap handler $sig"
#  echo 'trap inside f1:'
#  trap
#  echo 'f1 exit'
  case $sig in
    SIGTERM)
      echo "[$sig] Sleep 20 seconds to do some work"
      sleep 20
      echo "[$sig] Sleep done. Exit"
      exit 0
    ;;
  esac
}

# SIGINT: Ctrl+C
# SIGTERM: kill
# EXIT

for sig in SIGINT SIGTERM SIGHUP SIGQUIT SIGKILL EXIT ERR RETURN; do
  trap "trap_handler $sig" $sig
done

while [[ true ]]; do
#    echo -n "."
    sleep 3
done