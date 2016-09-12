package com.github.bric3.memcached.server;

import java.util.Map;
import com.github.bric3.memcached.server.cache.CachedData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
class MemcachedGetSetCommandHandler extends ChannelInboundHandlerAdapter {

    private Map<ByteBuf, CachedData> cache;

    public MemcachedGetSetCommandHandler(Map<ByteBuf, CachedData> cache) {
        this.cache = cache;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MemcachedCommand command = (MemcachedCommand) msg;

        command.processAndReply(cache, ctx::write);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
