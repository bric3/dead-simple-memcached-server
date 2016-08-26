package com.github.bric3.memcached.server;

import java.net.InetSocketAddress;
import java.util.Map;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.bric3.memcached.server.cache.CachedData;
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
    private static final int DEFAULT_PORT = 11211;
    private static final int DEFAULT_CACHE_MAX_SIZE = 10_000;

    private final int port;
    private final int cacheMaxSize;
    private EventLoopGroup eventExecutors;
    private ChannelFuture channelFuture;
    private Cache<ByteBuf, CachedData> cache;

    public DeadSimpleTextMemcachedServer(int port, int cacheMaxSize) {
        this.port = port;
        System.out.format("Server will bind to 0.0.0.0:%d%n", port);
        this.cacheMaxSize = cacheMaxSize;
        System.out.format("Server can store %d elements%n", cacheMaxSize);
    }

    public static void main(String[] args) throws InterruptedException {
        new DeadSimpleTextMemcachedServer(portFromArgsOrDefault(args),
                                          cacheMaxSizeFromArgsOrDefault(args))
                .startBlocking();
    }

    private static int cacheMaxSizeFromArgsOrDefault(String[] args) {
        if (args.length != 2) {
            System.out.format("Using default cache max size : %d%n", DEFAULT_CACHE_MAX_SIZE);
            return DEFAULT_CACHE_MAX_SIZE;
        }
        return Integer.parseInt(args[1]);
    }

    private static int portFromArgsOrDefault(String[] args) {
        if (args.length < 1) {
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
                                 .addLast(sharedEchoHandler); // possibly schedule on DefaultEventExecutorGroup
                           }
                       });
        channelFuture = serverBootstrap.bind().sync();
    }

    private Map<ByteBuf, CachedData> cache() {
        cache = Caffeine.newBuilder()
                        .maximumSize(cacheMaxSize)
                        .recordStats()
                        .build();
        return cache.asMap();
    }

    public void stop() {
        if (channelFuture != null) {
            try {
                eventExecutors.shutdownGracefully().sync();
                channelFuture.channel().closeFuture().sync();
                cache = null;
            } catch (InterruptedException e) {
                Thread.interrupted();
                System.out.println("Thread interrupted while shutting down, exiting.");
            }
        }
    }
}
