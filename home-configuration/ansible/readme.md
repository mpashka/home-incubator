

Например:
```
# проверить ping до хостов и убедиться, что ansible работает
$ ansible -m ping all -i hosts.yml

# запустить для настройки рабочего десктопа 
$ ansible-playbook -i hosts.yml site.yml -l localhost

# запустить для всех хостов
$ ansible-playbook -i hosts.yml site.yml

# запустить только для одного хоста
$ ansible-playbook -i hosts.yml site.yml --limit 192.168.2.1

$ export ANSIBLE_LIBRARY=~/.ansible/roles/gekmihesg.openwrt/library
$ export ANSIBLE_VARS_PLUGINS=~/.ansible/roles/gekmihesg.openwrt/vars_plugins
$ ansible -i openwrt-hosts -m setup all
```


```
Ansible options:
-i, --inventory
    specify inventory host path or comma separated host list

-l 'SUBSET', --limit 'SUBSET'
    further limit selected hosts to an additional pattern

-m 'MODULE_NAME', --module-name 'MODULE_NAME'
    Name of the action to execute (default=command)

--private-key 'PRIVATE_KEY_FILE', --key-file 'PRIVATE_KEY_FILE'
    use this file to authenticate the connection
```
