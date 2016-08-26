package com.github.bric3.memcached.server;

import java.util.Map;
import java.util.function.Consumer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

interface MemcachedCommand {
    ByteBuf CRLF = Unpooled.wrappedBuffer(new byte[]{'\r', '\n'}).retainedDuplicate();

    default void applyOn(Map<ByteBuf, ByteBuf> cache, Consumer<ByteBuf> replier) {}
}
