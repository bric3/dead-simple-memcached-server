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
    public void processAndReply(Map<ByteBuf, CachedData> cache, Consumer<ByteBuf> replier) {
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
            // COMMAND LINE
            int remainingCommandLineSize = bufferToParse.bytesBefore((byte) '\r');
            ByteBuf remainingCommandLine = bufferToParse.readSlice(remainingCommandLineSize);

            remainingCommandLine.skipBytes(1); // whitespace

            // key
            ByteBuf key = remainingCommandLine.readRetainedSlice(remainingCommandLine.bytesBefore((byte) ' '));
            remainingCommandLine.skipBytes(1); // whitespace
            ByteBuf flags = remainingCommandLine.readRetainedSlice(remainingCommandLine.bytesBefore((byte) ' '));
            remainingCommandLine.skipBytes(1); // whitespace

            // ignore expiration time
            remainingCommandLine.skipBytes(remainingCommandLine.bytesBefore((byte) ' '));
            remainingCommandLine.skipBytes(1); // whitespace

            int bytesToReadBeforeSPACE = remainingCommandLine.bytesBefore((byte) ' ');
            int bytesToReadBeforeCROrSPACE = Math.max(bytesToReadBeforeSPACE, remainingCommandLine.readableBytes());
            ByteBuf payloadSize = remainingCommandLine.readSlice(bytesToReadBeforeCROrSPACE);

            // ignore noreply

            // LINE SEPARATOR
            bufferToParse.skipBytes(bufferToParse.bytesBefore((byte) '\r') + 1);
            bufferToParse.skipBytes(bufferToParse.bytesBefore((byte) '\n') + 1);

            // PAYLOAD
            int length = Integer.parseInt(payloadSize.toString(US_ASCII));
            ByteBuf payload = bufferToParse.retainedSlice(bufferToParse.readerIndex(), length);

            // advance buffer reader position after payload
            bufferToParse.readerIndex(bufferToParse.readerIndex() + payload.readableBytes() + CRLF.length);
            return new MemcachedSetCommand(key, flags, payload);
        }
    }
}
