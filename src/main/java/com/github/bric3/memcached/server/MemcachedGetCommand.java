package com.github.bric3.memcached.server;

import static io.netty.util.CharsetUtil.US_ASCII;
import java.util.Map;
import java.util.function.Consumer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

class MemcachedGetCommand implements MemcachedCommand {
    private static final ByteBuf GET = Unpooled.copiedBuffer("get", US_ASCII);
    private static final ByteBuf VALUE = Unpooled.copiedBuffer("VALUE", US_ASCII);
    private static final ByteBuf END = Unpooled.copiedBuffer("END", US_ASCII);
    private ByteBuf key;

    public MemcachedGetCommand(ByteBuf key) {
        this.key = key;
    }

    public ByteBuf key() {
        return key;
    }

    public String keyAsString() {
        return key().toString(CharsetUtil.UTF_8);
    }

    public static boolean isGetCommand(ByteBuf command) {
        return command.equals(GET);
    }

    @Override
    public void applyOn(Map<ByteBuf, ByteBuf> cache, Consumer<ByteBuf> replier) {
        if (!cache.containsKey(key)) {
            replier.accept(Unpooled.buffer(3 + 2)
                                   .writeBytes(END.retainedDuplicate().readerIndex(0))
                                   .writeBytes(CRLF.retainedDuplicate().readerIndex(0)));
            return;
        }


        ByteBuf value = cache.get(key);

        System.out.println("found : " + ByteBufUtil.hexDump(value));

        ByteBuf response = Unpooled.buffer(
                key.readerIndex(0).readableBytes() +
                        value.readerIndex(0).readableBytes() +
                        17) // protocol overhead
                                   .writeBytes(VALUE.retainedDuplicate().readerIndex(0)) // VALUE
                                   .writeByte(' ')
                                   .writeBytes(key.retainedDuplicate().readerIndex(0)) // key
                                   .writeByte(' ')
                                   .writeByte('0') // flags
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
