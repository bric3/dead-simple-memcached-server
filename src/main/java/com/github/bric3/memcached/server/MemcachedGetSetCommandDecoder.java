package com.github.bric3.memcached.server;

import static io.netty.util.CharsetUtil.US_ASCII;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;

class MemcachedGetSetCommandDecoder extends ByteToMessageDecoder {
    private final AllCommands allCommands = new AllCommands()
            .register(new MemcachedGetCommand.GetParser())
            .register(new MemcachedSetCommand.SetParser());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        out.add(decode(in));
    }

    private MemcachedCommand decode(ByteBuf buffer) throws Exception {
        System.out.println("----decoder----\n");
        System.out.println("actual buffer hex : \n" + ByteBufUtil.hexDump(buffer));
        System.out.println("actual buffer : \n" + buffer.toString(US_ASCII));

        // command
        ByteBuf command = buffer.readSlice(buffer.bytesBefore((byte) ' '));
        System.out.println("command: " + command.toString(CharsetUtil.US_ASCII));

        MemcachedCommand memcachedCommand = allCommands.tryParse(command, buffer);
        System.out.println("----decoder----\n");

        // make sure there's nothing left to parse
        buffer.readerIndex(buffer.readerIndex() + buffer.readableBytes());

        return memcachedCommand;
    }

    private class AllCommands {
        private final Map<ByteBuf, MemcachedCommand.Parser> commands = new HashMap<>();

        private AllCommands register(MemcachedCommand.Parser value) {
            commands.put(value.command(), value);
            return this;
        }

        public MemcachedCommand tryParse(ByteBuf command, ByteBuf buffer) {
            return commands.getOrDefault(command, UnknownCommand.UNKNOWN_PARSER)
                           .parseToCommand(buffer);
        }
    }

}
