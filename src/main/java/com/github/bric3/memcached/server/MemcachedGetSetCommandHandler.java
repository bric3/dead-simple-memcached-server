package com.github.bric3.memcached.server;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.channel.ChannelFutureListener.CLOSE;
import java.util.Map;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
class MemcachedGetSetCommandHandler extends ChannelInboundHandlerAdapter {

    private Map<ByteBuf, ByteBuf> cache;

    public MemcachedGetSetCommandHandler(Map<ByteBuf, ByteBuf> cache) {
        this.cache = cache;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MemcachedCommand command = (MemcachedCommand) msg;

        command.applyOn(cache);

        ctx.write(EMPTY_BUFFER);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(EMPTY_BUFFER)
           .addListener(CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
