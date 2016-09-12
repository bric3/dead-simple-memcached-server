package com.github.bric3.memcached.server;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import java.util.Map;
import java.util.function.Consumer;
import com.github.bric3.memcached.server.cache.CachedData;
import io.netty.buffer.ByteBuf;

interface MemcachedCommand {

    default void processAndReply(Map<ByteBuf, CachedData> cache, Consumer<ByteBuf> replier) {}

    interface Parser {
        MemcachedCommand parseToCommand(ByteBuf buffer);
        default ByteBuf command() { return EMPTY_BUFFER; }
    }
}
