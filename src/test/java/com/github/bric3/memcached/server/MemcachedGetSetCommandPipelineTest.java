package com.github.bric3.memcached.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static com.github.bric3.memcached.TestUtils.byteBufFromHexString;
import static io.netty.util.CharsetUtil.UTF_8;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class MemcachedGetSetCommandPipelineTest {

    private static final String SET_OPERATION_HEX_DUMP = "7365742063646334306265302d656136622d346562662d626639612d62363335636662306166386620312030203230320d0aaced00057372002a636f6d2e6769746875622e62726963332e6d656d6361636865642e476f616c73546573742456616c7565f88ba03a193b5c3c0200024c0001617400124c6a6176612f6c616e672f537472696e673b4c0001627400134c6a6176612f6c616e672f496e74656765723b78707400087768617465766572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b0200007870000006140d0a";
    private ByteBuf set_operation = byteBufFromHexString(SET_OPERATION_HEX_DUMP);
    private final static String PAYLOAD = "aced00057372002a636f6d2e6769746875622e62726963332e6d656d6361636865642e476f616c73546573742456616c7565f88ba03a193b5c3c0200024c0001617400124c6a6176612f6c616e672f537472696e673b4c0001627400134c6a6176612f6c616e672f496e74656765723b78707400087768617465766572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b020000787000000614";
    private ByteBuf payload = byteBufFromHexString(PAYLOAD);

    @Before
    public void init_handler() throws Exception {

    }

    @Test
    public void can_decode_set_operation_command() {
        // Given
        HashMap<ByteBuf, ByteBuf> cache = new HashMap<>();
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new MemcachedGetSetCommandDecoder(),
                new MemcachedGetSetCommandHandler(cache));

        // When
        embeddedChannel.writeInbound(set_operation);
        embeddedChannel.finish();

        // Then
        assertThat(cache).contains(entry(Unpooled.copiedBuffer("cdc40be0-ea6b-4ebf-bf9a-b635cfb0af8f", UTF_8), payload));
    }
}