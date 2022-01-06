#!/bin/sh

### Custom user script for post-update iptables
### This script auto called after internal firewall restart
### First param is:
###  "start" (call at start optware),
###  "stop" (call before stop optware),
###  "update" (call after internal firewall restart).
### Include you custom rules for iptables below:

case "$1" in
start|update)
	# add iptables custom rules
	# route some traffic through tor
	iptables -t nat -A PREROUTING -i br0 -p tcp -m set --match-set unblock dst -j REDIRECT --to-port 9040
	iptables -t nat -A OUTPUT -p tcp -m set --match-set unblock dst -j REDIRECT --to-ports 9040
	# allow inbound wireguard traffic
	iptables -t filter -A INPUT -i wg0 -j ACCEPT
	iptables -t filter -A FORWARD -i wg0 -j ACCEPT
	echo "firewall started"
	;;
stop)
	# delete iptables custom rules
	# tor
	iptables -t nat -D PREROUTING -i br0 -p tcp -m set --match-set unblock dst -j REDIRECT --to-port 9040
	iptables -t nat -D OUTPUT -p tcp -m set --match-set unblock dst -j REDIRECT --to-ports 9040
	# wireguard
	iptables -t filter -D INPUT -i wg0 -j ACCEPT
	iptables -t filter -D FORWARD -i wg0 -j ACCEPT
	echo "firewall stopped"
	;;
*)
	echo "Usage: $0 {start|stop|update}"
	exit 1
	;;
esac
