#!/bin/bash

TOR_DNS=127.0.0.1:9053
SRC_HOSTS=/etc/dnsmasq.d/src/lan_hosts.csv.i
DHCP_HOSTS=/etc/dnsmasq.d/lan_hosts.cfg.i
SRC_DOMAINS=/etc/dnsmasq.d/src/dns_tor_domains.txt.i
DNSMASQ_TOR_DOMAINS=/etc/dnsmasq.d/dns_tor.conf
TOR_DOMAINS=/etc/tor/hosts_suffixes.cfg

echo "# Auto generated from $SRC_HOSTS" > $DHCP_HOSTS

while IFS=, read -r ip name mac class ; do
    if [[ ! -z "$ip" && "${ip:0:1}" != "#" ]] ; then
        #echo "$ip $name $mac ($class)"
        mac="$(echo "$mac" | tr 'ABCDEF' 'abcdef')"
        echo "# $name" >> $DHCP_HOSTS
        if [[ -z "$class" ]]; then
          echo "${mac},${ip},${name// /}" >> $DHCP_HOSTS
        else
          echo "${mac},net:${class},${ip},${name// /}" >> $DHCP_HOSTS
        fi
        echo "" >> $DHCP_HOSTS
    fi
done <"$SRC_HOSTS"

domains_tor=""
domains_dnsmasq=""

while IFS=, read -r domain ; do
    if [[ ! -z "$domain" && "${domain:0:1}" != "#" ]] ; then
        domains_tor="${domains_tor},${domain}"
        domains_dnsmasq="${domains_dnsmasq}/${domain}"
    fi
done <"$SRC_DOMAINS"

echo "# Auto generated from $SRC_DOMAINS
AutomapHostsSuffixes $domains_tor
" > $TOR_DOMAINS

echo "# Auto generated from $SRC_DOMAINS
server=${domains_dnsmasq}/${TOR_DNS/:/#}
"> $DNSMASQ_TOR_DOMAINS

#/opt/etc/init.d/S35tor reconfigure
#/opt/etc/init.d/S56dnsmasq reconfigure
systemctl reload tor

# Restart dnsmasq in order to flush caches
#systemctl reload dnsmasq
systemctl restart dnsmasq
#ipconfig /flushdns
