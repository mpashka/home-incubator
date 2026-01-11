мой файл /etc/postgresql/14/main/pg_hba.conf имеет следующее содержимое
```
local   all             postgres                                peer
local   all             all                                     peer
host    all             all             127.0.0.1/32            scram-sha-256
host    all             all             ::1/128                 scram-sha-256
local   replication     all                                     peer
host    replication     all             127.0.0.1/32            scram-sha-256
host    replication     all             ::1/128                 scram-sha-256
```

как мне запустить админку postgres и создать новую базу

https://www.postgresql.org/docs/14/reference-client.html
sudo -u postgres psql
pg_config
psql
\l - list databases
\c rs_receipt_qr - connect to <my>
\dn - list schemas
\dt - list tables

-- Change all tables owner
for tbl in `psql -qAt -c "select tablename from pg_tables where schemaname = 'public';" YOUR_DB` ; do  
    psql -c "alter table \"$tbl\" owner to NEW_OWNER" YOUR_DB
done
