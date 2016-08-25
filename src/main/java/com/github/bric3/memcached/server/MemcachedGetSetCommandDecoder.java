package com.github.bric3.memcached.server;

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
        super(MAX_COMMAND_LENGTH, true, Unpooled.wrappedBuffer(new byte[]{'\r', '\n'}));
    }


    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        try {
            ByteBuf textFrame = (ByteBuf) super.decode(ctx, buffer);
            // if frame does not end by CRLF
            if (textFrame == null) {
                return UnknownCommand.INSTANCE;
            }
            System.out.println(ByteBufUtil.hexDump(textFrame));

            // command
            ByteBuf command = textFrame.readSlice(textFrame.bytesBefore((byte) ' '));
            System.out.println(command.toString(CharsetUtil.UTF_8));

            textFrame.skipBytes(1); // whitespace

            // key
            ByteBuf key = textFrame.readSlice(textFrame.bytesBefore((byte) ' '));
            System.out.println(key.toString(CharsetUtil.UTF_8));

            // ignore the rest of the line (for now, as there is still the size)

            if(MemcachedSetCommand.isSetCommand(command)) {
                // payload
                ByteBuf payload = (ByteBuf) super.decode(ctx, buffer);
                System.out.println(ByteBufUtil.hexDump(payload));
                return new MemcachedSetCommand(key, payload);
            }

            return UnknownCommand.INSTANCE;
        } finally {
            buffer.discardReadBytes();
        }
    }



}
