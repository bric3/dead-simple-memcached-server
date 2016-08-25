package com.github.bric3.memcached;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class TestUtils {
    private TestUtils() {
    }

    public static ByteBuf byteBufFromHexString(String s) {
        ByteBuf buffer = Unpooled.buffer(s.length() / 2);
        for (int i = 0; i < s.length(); i += 2) {
            buffer.writeByte((byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16)));
        }
        return buffer;
    }
}
