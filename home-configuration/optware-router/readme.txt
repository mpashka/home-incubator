pbr+luci-app-pbr

ethtool mc wireguard-tools curl
ethtool eth0: Speed: 1000Mb/s, Duplex: Full

https://docs.openwrt.melmac.net/pbr/
https://docs.openwrt.melmac.net/pbr/#how-to-install-dnsmasq-full

opkg remove kmod-nf-ipt kmod-ipt-core kmod-ipt-ipset --force-depends
modules: /lib/modules/5.10.161/

---
ip route show table vpn_ru0
ip route add table vpn_ru0 default dev vpn_ru0
ip r add 192.168.2.0/24 dev vpn_ru0 scope link  src 192.168.3.1
---
iperf3 -c speedtest.serverius.net -p 5002

---
export DISPLAY=":0.0"
(didn't help)
cp /home/pmukhataev/.Xauthority -> /tmp/
export XAUTHORITY=/tmp/.Xauthority
xhost +SI:localuser:m_pashka
