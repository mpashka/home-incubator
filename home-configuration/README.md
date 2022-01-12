## These are configuration files for my LANs.
In one LAN I have a router that can't be configured. It is GPON router from my Internet provider.
But it has not bad WiFi 802.11ac and is located in the center of my appartments.
So my idea was to continue using provider's router with WiFi but introduce my own server
in LAN, setup set of different services on my server (VPN, tor) and allow transparent access to
VPN and tor resources for the clients in my LAN.

* LAN - 192.168.2.0/24
  * LAN Router - 192.168.2.1 (I can only disable DHCP here and enable port forwarding)
  * LAN Server - 192.168.2.4 (OS: ubuntu, ethernet interface name: enp1s0)
  * LAN tor
    * tor private network (VirtualAddrNetwork) - 10.254.0.0/16
    * tor dns server (DNSPort) - 127.0.0.1:9053
    * tor transparent proxy address (TransPort) - 0.0.0.0:9040
  * dnscrypt-proxy (DoH) address - 127.0.0.1:9153
* LAN2 - 192.168.3.0/24
  * LAN2 router - 192.168.3.1 (xiaomi mi 3g + padavan firmware + entware installed)
* Wireguard
  * wireguard device network (private IP network used to assign wireguard interface address) - 10.253.1.0/24
  * LAN server wireguard address - 10.253.1.2
  * LAN2 router vpn address - 10.253.1.3
  * Android client wireguard address - 10.253.1.10
