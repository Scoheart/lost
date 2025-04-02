docker run -d \
    --name mysql-server \
    -e MYSQL_ROOT_PASSWORD=88888888 \
    -p 3306:3306 \
    mysql
