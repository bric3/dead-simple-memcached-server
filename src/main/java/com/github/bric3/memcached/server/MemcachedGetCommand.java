package com.github.bric3.memcached.server;

import static com.github.bric3.memcached.server.MemcachedConstants.CRLF;
import static com.github.bric3.memcached.server.MemcachedConstants.END;
import static com.github.bric3.memcached.server.MemcachedConstants.VALUE;
import static com.github.bric3.memcached.server.MemcachedConstants.get;
import static io.netty.util.CharsetUtil.US_ASCII;
import java.util.Map;
import java.util.function.Consumer;
import com.github.bric3.memcached.server.cache.CachedData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class MemcachedGetCommand implements MemcachedCommand {
    /**
     * VALUE (5) + whitespace (1) + key + whitespace (1) + flags + whitespace (1) + size + CRLF (2) + payload + CRLF (2) + END (3) + CRLF (2)
     */
    private static final int GET_WITH_SINGLE_VALUE_RESULT_OVERHEAD = 17;
    /**
     * END (3) + CRLF (2)
     */
    private static final int GET_WITHOUT_RESULT_OVERHEAD = 5;
    private ByteBuf key;

    MemcachedGetCommand(ByteBuf key) {
        this.key = key.asReadOnly();
    }

    String keyAsString() {
        return key.toString(US_ASCII);
    }


    @Override
    public void applyOn(Map<ByteBuf, CachedData> cache, Consumer<ByteBuf> replier) {
        if (!cache.containsKey(key)) {
            replier.accept(Unpooled.buffer(GET_WITHOUT_RESULT_OVERHEAD)
                                   .writeBytes(END)
                                   .writeBytes(CRLF));
            return;
        }


        CachedData cachedData = cache.get(key);
        ByteBuf value = cachedData.payload;
        ByteBuf flags = cachedData.flags;

        int bufferResponseInitialCapacity =
                key.readerIndex(0).readableBytes() +
                value.readerIndex(0).readableBytes() +
                flags.readerIndex(0).readableBytes() +
                GET_WITH_SINGLE_VALUE_RESULT_OVERHEAD;
        ByteBuf response = Unpooled.buffer(bufferResponseInitialCapacity)
                                   .writeBytes(VALUE)
                                   .writeByte(' ')
                                   .writeBytes(key.retainedDuplicate().readerIndex(0))
                                   .writeByte(' ')
                                   .writeBytes(flags.retainedDuplicate().readerIndex(0))
                                   .writeByte(' ')
                                   .writeBytes(String.valueOf(value.readableBytes()).getBytes(US_ASCII))
                                   .writeBytes(CRLF)
                                   .writeBytes(value.slice().readerIndex(0))
                                   .writeBytes(CRLF)
                                   .writeBytes(END)
                                   .writeBytes(CRLF);

        replier.accept(response);
    }


    static class GetParser implements Parser {
        public ByteBuf command() {
            return get;
        }

        @Override
        public MemcachedCommand parseToCommand(ByteBuf bufferToParse) {
            // command already read
            bufferToParse.skipBytes(1); // whitespace

            // key
            ByteBuf key = bufferToParse.readRetainedSlice(bufferToParse.bytesBefore((byte) '\r'));

            // advance buffer position for next command
            bufferToParse.readerIndex(bufferToParse.readerIndex() + CRLF.length);
            return new MemcachedGetCommand(key);
        }
    }
}
