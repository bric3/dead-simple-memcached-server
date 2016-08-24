package com.github.bric3.memcached.server;

import java.net.InetSocketAddress;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class DeadSimpleTextMemcachedServer {

    private final int port;
    private EventLoopGroup eventExecutors;
    private ChannelFuture channelFuture;

    public DeadSimpleTextMemcachedServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Requires port number");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        new DeadSimpleTextMemcachedServer(port).startBlocking();
    }

    private void startBlocking() {
        try {
            start();
        } finally {
            stop();
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
        MemcachedGetSetHandler echoHandler = new MemcachedGetSetHandler();
        eventExecutors = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(eventExecutors)
                       .channel(NioServerSocketChannel.class)
                       .localAddress(new InetSocketAddress(port))
                       .childHandler(new ChannelInitializer<SocketChannel>() {
                           @Override
                           protected void initChannel(SocketChannel ch) {
                               ch.pipeline().addLast(echoHandler);
                           }
                       });
        channelFuture = serverBootstrap.bind().sync();
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
