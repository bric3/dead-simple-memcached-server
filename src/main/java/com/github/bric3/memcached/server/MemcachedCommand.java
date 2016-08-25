package com.github.bric3.memcached.server;

import java.util.Map;
import io.netty.buffer.ByteBuf;

interface MemcachedCommand {
    default void applyOn(Map<ByteBuf, ByteBuf> cache) {}
}
