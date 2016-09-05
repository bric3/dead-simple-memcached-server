package com.github.bric3.memcached.server;

import static io.netty.util.CharsetUtil.US_ASCII;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

abstract class MemcachedConstants {
    private MemcachedConstants() { }

    static final byte[] CRLF = new byte[]{'\r', '\n'};

    static final ByteBuf get = Unpooled.copiedBuffer("get", US_ASCII).asReadOnly();
    static final byte[] VALUE = "VALUE".getBytes(US_ASCII);
    static final byte[] END = "END".getBytes(US_ASCII);

    static final ByteBuf set = Unpooled.copiedBuffer("set", US_ASCII).asReadOnly();
    static final byte[] STORED = "STORED".getBytes(US_ASCII);
}
