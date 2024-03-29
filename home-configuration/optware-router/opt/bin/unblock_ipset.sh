#!/bin/sh

# Delay after dnsmasq restart
# sleep 3

# Wait for google.com resolving
until ADDRS=$(dig +short google.com @localhost) && [ -n "$ADDRS" ] > /dev/null 2>&1; do sleep 5; done

ipset flush unblock

while read line || [ -n "$line" ]; do

  [ -z "$line" ] && continue
  [ "${line:0:1}" = "#" ] && continue

  cidr=$(echo $line | grep -Eo '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}/[0-9]{1,2}')

  if [ ! -z "$cidr" ]; then
    ipset -exist add unblock $cidr
    continue
  fi

  range=$(echo $line | grep -Eo '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}-[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')

  if [ ! -z "$range" ]; then
    ipset -exist add unblock $range
    continue
  fi

  addr=$(echo $line | grep -Eo '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')

  if [ ! -z "$addr" ]; then
    ipset -exist add unblock $addr
    continue
  fi

  dig +short $line @localhost | grep -Eo '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}' | awk '{system("ipset -exist add unblock "$1)}'

done < /opt/etc/unblock.txt
