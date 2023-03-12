#!/bin/bash

# -G, --dhcp-host=[<hwaddr>][,id:<client_id>|*][,set:<tag>][,tag:<tag>][,<ipaddr>][,<hostname>][,<lease_time>][,ignore]

add_dnsmasq_domains() { src_file="$1"
    vpn_domains=()

    while IFS=, read -r domain ; do
        if [[ ! -z "$domain" && "${domain:0:1}" != "#" ]] ; then
            #vpn_domains+=("${domain}")
	    #echo -n "$domain: "
	    if ! addr="$(dig +short @10.10.250.250 "$domain")" ; then
		echo "Bad domain: $domain"
	    fi
	    if [[ -z $addr ]]; then
		echo "No addr: $domain"
	    fi
        fi
    done <"$src_file"

}

add_dnsmasq_domains "vpn_domains-ru.txt.i"
add_dnsmasq_domains "vpn_domains-en.txt.i"
