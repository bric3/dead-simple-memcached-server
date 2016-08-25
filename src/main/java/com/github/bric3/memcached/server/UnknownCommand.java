package com.github.bric3.memcached.server;

class UnknownCommand implements MemcachedCommand {
    static UnknownCommand INSTANCE = new UnknownCommand();
}
