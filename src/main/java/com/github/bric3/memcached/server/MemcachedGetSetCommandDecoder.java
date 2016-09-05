package com.github.bric3.memcached.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

class MemcachedGetSetCommandDecoder extends ReplayingDecoder {
    private final AllCommands allCommands = new AllCommands()
            .register(new MemcachedGetCommand.GetParser())
            .register(new MemcachedSetCommand.SetParser());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        decode(in).ifPresent(out::add);
    }

    private Optional<MemcachedCommand> decode(ByteBuf buffer) throws Exception {
        // command
        int length = buffer.bytesBefore((byte) ' ');
        ByteBuf command = buffer.readSlice(length);

        return allCommands.tryParse(command, buffer);
    }

    private class AllCommands {
        private final Map<ByteBuf, MemcachedCommand.Parser> commands = new HashMap<>();

        private AllCommands register(MemcachedCommand.Parser value) {
            commands.put(value.command(), value);
            return this;
        }

        public Optional<MemcachedCommand> tryParse(ByteBuf command, ByteBuf buffer) {
            return Optional.ofNullable(commands.getOrDefault(command, UnknownCommand.UNKNOWN_PARSER)
                                               .parseToCommand(buffer));
        }
    }

}
