package com.github.bric3.memcached.server;

import static io.netty.util.CharsetUtil.US_ASCII;
import java.util.Map;
import java.util.function.Consumer;
import com.github.bric3.memcached.server.cache.CachedData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

class MemcachedGetCommand implements MemcachedCommand {
    private static final ByteBuf GET = Unpooled.copiedBuffer("get", US_ASCII);
    private static final ByteBuf VALUE = Unpooled.copiedBuffer("VALUE", US_ASCII);
    private static final ByteBuf END = Unpooled.copiedBuffer("END", US_ASCII);
    private static final int GET_WITH_SINGLE_VALUE_RESULT_OVERHEAD = 17;
    private static final int GET_WITHOUT_RESULT_OVERHEAD = 5;
    private ByteBuf key;

    public MemcachedGetCommand(ByteBuf key) {
        this.key = key.asReadOnly();
    }

    String keyAsString() {
        return key.toString(CharsetUtil.UTF_8);
    }

    public static boolean isGetCommand(ByteBuf command) {
        return command.equals(GET);
    }

    @Override
    public void applyOn(Map<ByteBuf, CachedData> cache, Consumer<ByteBuf> replier) {
        if (!cache.containsKey(key)) {
            replier.accept(Unpooled.buffer(GET_WITHOUT_RESULT_OVERHEAD)
                                   .writeBytes(END.retainedDuplicate().readerIndex(0))
                                   .writeBytes(CRLF.retainedDuplicate().readerIndex(0)));
            return;
        }


        CachedData cachedData = cache.get(key);
        ByteBuf value = cachedData.payload;
        ByteBuf flags = cachedData.flags;

        System.out.println("found : " + ByteBufUtil.hexDump(value));

        int bufferResponseInitialCapacity = key.readerIndex(0).readableBytes() +
                value.readerIndex(0).readableBytes() +
                flags.readerIndex(0).readableBytes() +
                GET_WITH_SINGLE_VALUE_RESULT_OVERHEAD;
        ByteBuf response = Unpooled.buffer(bufferResponseInitialCapacity)
                                   .writeBytes(VALUE.retainedDuplicate().readerIndex(0)) // VALUE
                                   .writeByte(' ')
                                   .writeBytes(key.retainedDuplicate().readerIndex(0)) // key
                                   .writeByte(' ')
                                   .writeBytes(flags.retainedDuplicate().readerIndex(0)) // flags
                                   .writeByte(' ')
                                   .writeBytes(String.valueOf(value.readableBytes()).getBytes(US_ASCII)) // size
                                   .writeBytes(CRLF.retainedDuplicate().readerIndex(0))
                                   .writeBytes(value.slice().readerIndex(0)) // payload
                                   .writeBytes(CRLF.retainedDuplicate().readerIndex(0))
                                   .writeBytes(END.retainedDuplicate().readerIndex(0))
                                   .writeBytes(CRLF.retainedDuplicate().readerIndex(0));

        System.out.println("returning :\n" + response.readerIndex(0).toString(US_ASCII));
        replier.accept(response);

    }
}
