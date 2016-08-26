A Memcached Server
The goal of this exercise is to create a high­performance, asynchronous memcached server. 
For the scope of this exercise, we consider the following simplifications:

* Supports only the memcached text protocol
* Supports only the `set` and `get` operations
* Supports none of the configuration of the original memcached, the server is always bound 
  to `TCP 0.0.0.0:11211`
* In the protocol, `flags`, `exptime` and `noreply` are expected by the server but ignored 
  in the `set` command
* Only one key can be provided to the `get` command

In order to keep the focus on the initial purpose of this exercise, which is to write 
high­performance, concurrent code, the following constraints must be followed:

* The server should be written ideally in Java, but feel free to use the language you feel 
  the most comfortable with for such a task
* Performance matters, so the server should be designed to minimize its latency and maximize 
  its throughput
* Robustness matters as well, it should remain stable under a heavy load

Building such a server could obviously be endless, therefore it’s important to bound this 
exercise to a reasonable duration, probably in the 5­6h. The additional improvements that 
couldn’t be made on time can be discussed along with the solution.




My TODO list : 


* [x] pom structure  
* [x] Maven assembly
* [x] make a simple tcp server that binds to 0.0.0.0 on a given port
* [x] make main method
* [x] implement set decoder (partial)
* [x] implement set correct block decoder
* [x] implement set handler
* [x] implement get decoder
* [x] implement get handler
* [x] handles store flags as well
* [x] investigate cache
* [ ] performance tests 

Future works :

* [ ] Cache reached limit (set operation)
* [ ] Experiments with socket options, like connection backlog
* [ ] Delegate handler to another event executor
* [ ] Network packet fragmentation under high load?
* [ ] Experiment on ByteBuf direct memory
* [ ] Flush all data
* [x] Make a JUnit rule for the server
* [ ] Allow to pass more options via the command line
