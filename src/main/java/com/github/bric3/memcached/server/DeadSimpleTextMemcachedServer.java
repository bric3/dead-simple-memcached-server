package com.github.bric3.memcached.server;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class DeadSimpleTextMemcachedServer {
    public static final int DEFAULT_PORT = 11211;

    private final int port;
    private EventLoopGroup eventExecutors;
    private ChannelFuture channelFuture;

    public DeadSimpleTextMemcachedServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        int port = portFromArgsOrDefault(args);
        new DeadSimpleTextMemcachedServer(port).startBlocking();
    }

    private static int portFromArgsOrDefault(String[] args) {
        if (args.length != 1) {
            System.out.format("Using default port : %d%n", DEFAULT_PORT);
            return DEFAULT_PORT;
        }
        return Integer.parseInt(args[0]);
    }

    private void startBlocking() throws InterruptedException {
        try {
            internalStart();
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventExecutors.shutdownGracefully().sync();
        }
    }

    public void start() {
        try {
            internalStart();
        } catch (InterruptedException e) {
            Thread.interrupted();
            System.out.println("Thread interrupted, exiting.");
        }
    }

    private void internalStart() throws InterruptedException {
        MemcachedGetSetCommandHandler sharedEchoHandler = new MemcachedGetSetCommandHandler(cache());
        eventExecutors = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(eventExecutors)
                       .localAddress(new InetSocketAddress(port))
                       .channel(NioServerSocketChannel.class)
                       .handler(new LoggingHandler(LogLevel.INFO))
//                       .option(ChannelOption.SO_BACKLOG, 128)
//                       .childOption(ChannelOption.SO_KEEPALIVE, true)
                       .childHandler(new ChannelInitializer<SocketChannel>() {
                           @Override
                           protected void initChannel(SocketChannel ch) {
                               ch.pipeline()
                                 .addLast(new MemcachedGetSetCommandDecoder())
                                 .addLast(sharedEchoHandler);
                           }
                       });
        channelFuture = serverBootstrap.bind().sync();
    }

    private ConcurrentHashMap<ByteBuf, ByteBuf> cache() {
        return new ConcurrentHashMap<>();
    }

    public void stop() {
        if (channelFuture != null) {
            try {
                eventExecutors.shutdownGracefully().sync();
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.interrupted();
                System.out.println("Thread interrupted while shutting down, exiting.");
            }
        }
    }
}
