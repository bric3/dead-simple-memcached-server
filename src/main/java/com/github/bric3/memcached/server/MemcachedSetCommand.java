package com.github.bric3.memcached.server;

import java.util.Map;
import java.util.function.Consumer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

class MemcachedSetCommand implements MemcachedCommand {
    private static final ByteBuf SET = Unpooled.copiedBuffer("set", CharsetUtil.US_ASCII);
    private static final ByteBuf STORED = Unpooled.copiedBuffer("STORED", CharsetUtil.US_ASCII);
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
        return command.equals(SET);
    }

    @Override
    public void applyOn(Map<ByteBuf, ByteBuf> cache, Consumer<ByteBuf> replier) {
        cache.put(key, payload);

        replier.accept(Unpooled.buffer(6 + 2)
                               .writeBytes(STORED.retainedDuplicate().readerIndex(0))
                               .writeBytes(CRLF.retainedDuplicate().readerIndex(0)));
    }
}
