package com.github.bric3.memcached.server.cache;

import java.util.Objects;
import io.netty.buffer.ByteBuf;

public class CachedData {
    public final ByteBuf flags;
    public final ByteBuf payload;

    public CachedData(ByteBuf flags, ByteBuf payload) {
        this.flags = flags;
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedData that = (CachedData) o;
        return Objects.equals(flags, that.flags) &&
                Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flags, payload);
    }
}
