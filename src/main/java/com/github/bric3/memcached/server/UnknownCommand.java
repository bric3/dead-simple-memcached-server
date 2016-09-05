package com.github.bric3.memcached.server;

class UnknownCommand implements MemcachedCommand {
    static final UnknownCommand UNKNOWN_COMMAND = new UnknownCommand();
    static Parser UNKNOWN_PARSER = buffer -> {
        // trash buffer content
        buffer.skipBytes(buffer.readableBytes());
        return null;
    };

}
