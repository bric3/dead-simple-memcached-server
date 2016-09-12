[![Build Status](https://travis-ci.org/bric3/dead-simple-memcached-server.svg?branch=master)](https://travis-ci.org/bric3/dead-simple-memcached-server)

Simple memcached server (only handle get/set) 
 
 
This project requires java 8 to run. And the launch scripts expect a unix environment (Linux, OSX will do).
 
```sh
# Package it
mvn clean package

## packaged to a tar.gz file
ls -l target/dead-simple-memcached-server-0.1-SNAPSHOT-batch.tar.gz


# run it from target
./target/simple-memcached-server/bin/simple-memcached-server.sh 


# run it with a different port
./target/simple-memcached-server/bin/simple-memcached-server.sh 22000 


# run it with a different port and a different cache size
./target/simple-memcached-server/bin/simple-memcached-server.sh 22000 10000000
``` 
