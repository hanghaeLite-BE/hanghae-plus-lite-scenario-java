#!/bin/bash
set -e

# master가 뜰 때까지 대기
echo "[slave-init] Waiting for master to be ready..."
until mysql -hmysql-test-master -utest -ptest -e "select 1"; do
  sleep 2
done

echo "[slave-init] Setting up replication..."
mysql -uroot -ptest -e "\
CHANGE MASTER TO \
  MASTER_HOST='mysql-test-master',\
  MASTER_USER='root',\
  MASTER_PASSWORD='test',\
  MASTER_AUTO_POSITION=1,\
  GET_MASTER_PUBLIC_KEY=1;\
START SLAVE;\
"
echo "[slave-init] Done."
