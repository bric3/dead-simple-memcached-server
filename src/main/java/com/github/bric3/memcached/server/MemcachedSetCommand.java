package com.github.bric3.memcached.server;

import java.util.Map;
import java.util.function.Consumer;
import com.github.bric3.memcached.server.cache.CachedData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

class MemcachedSetCommand implements MemcachedCommand {
    private static final ByteBuf SET = Unpooled.copiedBuffer("set", CharsetUtil.US_ASCII);
    private static final ByteBuf STORED = Unpooled.copiedBuffer("STORED", CharsetUtil.US_ASCII);
    public final ByteBuf flags;
    public final ByteBuf payload;
    public final ByteBuf key;

    public MemcachedSetCommand(ByteBuf key, ByteBuf flags, ByteBuf payload) {
        this.key = key.asReadOnly();
        this.flags = flags.asReadOnly();
        this.payload = payload.asReadOnly();
    }

    String keyAsString() {
        return key.toString(CharsetUtil.UTF_8);
    }

    public static boolean isSetCommand(ByteBuf command) {
        return command.equals(SET);
    }

    @Override
    public void applyOn(Map<ByteBuf, CachedData> cache, Consumer<ByteBuf> replier) {
        cache.put(key, new CachedData(flags, payload));

        replier.accept(Unpooled.buffer(6 + 2)
                               .writeBytes(STORED.retainedDuplicate().readerIndex(0))
                               .writeBytes(CRLF.retainedDuplicate().readerIndex(0)));
    }
}
