Simple memcached server (only handle get/set) 
 
 
```sh
# Package it
mvn clean package

# run it
./target/simple-memcached-server/bin/simple-memcached-server.sh 


# run it with a different port
./target/simple-memcached-server/bin/simple-memcached-server.sh 22000 


# run it with a different port and a different cache size
./target/simple-memcached-server/bin/simple-memcached-server.sh 22000 10000000
``` 
