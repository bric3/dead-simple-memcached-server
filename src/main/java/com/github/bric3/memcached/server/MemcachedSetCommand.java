package com.github.bric3.memcached.server;

import java.util.Map;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

class MemcachedSetCommand implements MemcachedCommand {
    private ByteBuf payload;
    private ByteBuf key;

    public MemcachedSetCommand(ByteBuf key, ByteBuf payload) {
        this.payload = payload;
        this.key = key;
    }

    public ByteBuf payload() {
        return payload;
    }

    public ByteBuf key() {
        return key;
    }

    public String keyAsString() {
        return key().toString(CharsetUtil.UTF_8);
    }

    public static boolean isSetCommand(ByteBuf command) {
        return command.equals(Unpooled.wrappedBuffer(new byte[]{'s', 'e', 't'}));
    }

    @Override
    public void applyOn(Map<ByteBuf, ByteBuf> cache) {
        cache.put(key, payload);
    }
}
