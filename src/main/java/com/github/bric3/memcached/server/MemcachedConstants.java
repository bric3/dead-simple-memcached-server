package com.github.bric3.memcached.server;

import static io.netty.util.CharsetUtil.US_ASCII;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

abstract class MemcachedConstants {
    private MemcachedConstants() { }

    static final ByteBuf CRLF = Unpooled.wrappedBuffer(new byte[]{'\r', '\n'}).retainedDuplicate();

    static final ByteBuf get = Unpooled.copiedBuffer("get", US_ASCII);
    static final ByteBuf VALUE = Unpooled.copiedBuffer("VALUE", US_ASCII);
    static final ByteBuf END = Unpooled.copiedBuffer("END", US_ASCII);

    static final ByteBuf set = Unpooled.copiedBuffer("set", US_ASCII).asReadOnly();
    static final ByteBuf STORED = Unpooled.copiedBuffer("STORED", US_ASCII).asReadOnly();
}
