package com.github.bric3.memcached.server;

import static com.github.bric3.memcached.server.MemcachedConstants.CRLF;
import static com.github.bric3.memcached.server.MemcachedConstants.STORED;
import static com.github.bric3.memcached.server.MemcachedConstants.set;
import static io.netty.util.CharsetUtil.US_ASCII;
import java.util.Map;
import java.util.function.Consumer;
import com.github.bric3.memcached.server.cache.CachedData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

class MemcachedSetCommand implements MemcachedCommand {
    public final ByteBuf flags;
    public final ByteBuf payload;
    public final ByteBuf key;

    public MemcachedSetCommand(ByteBuf key, ByteBuf flags, ByteBuf payload) {
        this.key = key.asReadOnly();
        this.flags = flags.asReadOnly();
        this.payload = payload.asReadOnly();
    }

    String keyAsString() {
        return key.toString(CharsetUtil.US_ASCII);
    }


    @Override
    public void applyOn(Map<ByteBuf, CachedData> cache, Consumer<ByteBuf> replier) {
        cache.put(key, new CachedData(flags, payload));

        replier.accept(Unpooled.buffer(6 + 2)
                               .writeBytes(STORED)
                               .writeBytes(CRLF));
    }

    public static class SetParser implements Parser {
        @Override
        public ByteBuf command() {
            return set;
        }

        @Override
        public MemcachedCommand parseToCommand(ByteBuf bufferToParse) {
            bufferToParse.skipBytes(1); // whitespace

            // key
            ByteBuf key = bufferToParse.readRetainedSlice(bufferToParse.bytesBefore((byte) ' '));
            bufferToParse.skipBytes(1); // whitespace
            ByteBuf flags = bufferToParse.readRetainedSlice(bufferToParse.bytesBefore((byte) ' '));
            bufferToParse.skipBytes(1); // whitespace

            // ignore expiration time
            bufferToParse.skipBytes(bufferToParse.bytesBefore((byte) ' '));
            bufferToParse.skipBytes(1); // whitespace

            int bytesToReadBeforeSPACE = bufferToParse.bytesBefore((byte) ' ');
            int bytesToReadBeforeCR = bufferToParse.bytesBefore((byte) '\r');
            int bytesToReadBeforeCROrSPACE = Math.max(Math.min(bytesToReadBeforeCR, bytesToReadBeforeSPACE), bytesToReadBeforeCR);
            ByteBuf payloadSize = bufferToParse.readRetainedSlice(bytesToReadBeforeCROrSPACE);

            // ignore noreply

            // skip CRLF
            bufferToParse.skipBytes(bufferToParse.bytesBefore((byte) '\r') + 1);
            bufferToParse.skipBytes(bufferToParse.bytesBefore((byte) '\n') + 1);

            // payload
            int length = Integer.parseInt(payloadSize.toString(US_ASCII));
            ByteBuf payload = bufferToParse.retainedSlice(bufferToParse.readerIndex(), length);
            return new MemcachedSetCommand(key, flags, payload);
        }
    }
}
