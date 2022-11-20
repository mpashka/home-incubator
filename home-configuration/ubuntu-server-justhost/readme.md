https://justhost.ru/services/vps
https://habr.com/ru/post/686238/ - обзор VPS
https://habr.com/ru/company/ruvds/blog/515758/ - wireguard для OpenWRT - для всего траффика
https://moonback.ru/page/keenetic-warp - Cloudflare warp VPN - уже не актуально
https://www.wisereport.ru/linux-routing-sites-vpn/ - маршрутизация ipset в интерфейс
    https://tldp.org/HOWTO/Adv-Routing-HOWTO/lartc.kernel.rpf.html - параметр
https://habr.com/ru/post/428992/ - общая инструкция - ipset для роутера (кинетика)
    https://timeweb.com/ru/community/articles/nastroyka-neskolkih-tablic-marshrutizacii-na-odnom-servere
https://upcloud.com/resources/tutorials/get-started-wireguard-vpn - wireguard

apt install ipset
# ipset-persistent

ipset create unblock hash:net

ipset create badips iphash family net
ipset create badips6 iphash family net6

#ipset create justhost6 hash:ip family inet6
ipset create justhost6 hash:net family inet6
dig @localhost -p 53 rutracker.org AAAA
