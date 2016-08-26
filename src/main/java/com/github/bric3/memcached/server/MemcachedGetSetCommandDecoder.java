package com.github.bric3.memcached.server;

import static io.netty.util.CharsetUtil.US_ASCII;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.CharsetUtil;

class MemcachedGetSetCommandDecoder extends DelimiterBasedFrameDecoder {
    /*
     * Max text frame
     * 3 char (set/get)
     * 1 whitespace
     * 250 char (key)
     * 1 whitespace
     * 2 flags (16 bits)
     * 1 whitespace
     * 4 int (exptime)
     * 1 whitespace
     * 8 bytes of the payload size
     */
    private static final int MAX_COMMAND_LENGTH = 3 + 1 + 250 + 1 + 2 + 1 + Integer.BYTES + 1 + Long.BYTES;

    public MemcachedGetSetCommandDecoder() {
        super(MAX_COMMAND_LENGTH, true, Unpooled.wrappedBuffer(new byte[]{'\r', '\n'}), Unpooled.wrappedBuffer(new byte[]{'\n'}));
    }


    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        System.out.println("----decoder----\n");
        System.out.println("actual buffer hex : \n" + ByteBufUtil.hexDump(buffer));
        System.out.println("actual buffer : \n" + buffer.toString(US_ASCII));
        ByteBuf textFrame = (ByteBuf) super.decode(ctx, buffer);
        // if frame does not end by CRLF
        if (textFrame == null) {
            return UnknownCommand.INSTANCE;
        }

        // command
        ByteBuf command = textFrame.readSlice(textFrame.bytesBefore((byte) ' '));
        System.out.println("command: " + command.toString(CharsetUtil.US_ASCII));

        if (MemcachedSetCommand.isSetCommand(command)) {
            textFrame.skipBytes(1); // whitespace

            // key
            ByteBuf key = textFrame.readSlice(textFrame.bytesBefore((byte) ' '));
            System.out.println("key    : " + key.toString(CharsetUtil.US_ASCII));

            textFrame.skipBytes(1); // whitespace

            ByteBuf flags = textFrame.readSlice(textFrame.bytesBefore((byte) ' '));
            System.out.println("flags  : " + flags.toString(CharsetUtil.US_ASCII));

            // ignore expiration time
            // TODO do not ignore byte size
            // ignore noreply

            // payload
            ByteBuf payload = (ByteBuf) super.decode(ctx, buffer);
            System.out.println("value  : " + ByteBufUtil.hexDump(payload));
            return new MemcachedSetCommand(key, flags, payload);
        }
        if (MemcachedGetCommand.isGetCommand(command)) {
            textFrame.skipBytes(1); // whitespace

            // key
            ByteBuf key = textFrame.slice();
            System.out.println("key    : " + key.toString(CharsetUtil.US_ASCII));

            return new MemcachedGetCommand(key);
        }

        System.out.println("----decoder----\n");
        return UnknownCommand.INSTANCE;
    }


}
