package com.github.bric3.memcached.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.github.bric3.memcached.TestUtils.byteBufFromHexString;
import org.junit.Ignore;
import org.junit.Test;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.util.CharsetUtil;

public class MemcachedGetSetCommandDecoderTest {

    private static final String SET_OPERATION_HEX_DUMP = "7365742063646334306265302d656136622d346562662d626639612d62363335636662306166386620312030203230320d0aaced00057372002a636f6d2e6769746875622e62726963332e6d656d6361636865642e476f616c73546573742456616c7565f88ba03a193b5c3c0200024c0001617400124c6a6176612f6c616e672f537472696e673b4c0001627400134c6a6176612f6c616e672f496e74656765723b78707400087768617465766572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b0200007870000006140d0a";
    private ByteBuf set_operation = byteBufFromHexString(SET_OPERATION_HEX_DUMP);

    private static final String GET_OPERATION_HEX_DUMP = "6765742037626631303933392d643130382d343966382d623533302d6437623066623037623731610d0a";
    private ByteBuf get_operation = byteBufFromHexString(GET_OPERATION_HEX_DUMP);

    private ByteBuf malformed = Unpooled.copiedBuffer("sdjvbnwdjsavbn", CharsetUtil.UTF_8);
    private ByteBuf unknown_operation = Unpooled.copiedBuffer("garbage sdjvbnwdjsavbn\r\n", CharsetUtil.UTF_8);

    /*
     * - <bytes> is the number of bytes in the data block to follow, *not*
     *   including the delimiting \r\n. <bytes> may be zero (in which case
     *   it's followed by an empty data block).
     *
     * * [x] parse the datablock
     * * [ ] handle \r\n within the datablock with the size in bytes
     */

    @Test
    public void can_decode_set_operation_command() {
        // Given
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MemcachedGetSetCommandDecoder());

        // When
        embeddedChannel.writeInbound(set_operation);
        embeddedChannel.finish();

        // Then
        Object decoded = embeddedChannel.readInbound();
        assertThat(decoded).isInstanceOf(MemcachedSetCommand.class);
        assertThat(((MemcachedSetCommand) decoded).keyAsString()).isEqualTo("cdc40be0-ea6b-4ebf-bf9a-b635cfb0af8f");
        assertThat(((MemcachedSetCommand) decoded).payload().readableBytes()).isEqualTo(202);
    }

    @Test
    public void can_decode_get_operation_command() {
        // Given
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MemcachedGetSetCommandDecoder());

        // When
        embeddedChannel.writeInbound(get_operation);
        embeddedChannel.finish();

        // Then
        Object decoded = embeddedChannel.readInbound();
        assertThat(decoded).isInstanceOf(MemcachedGetCommand.class);
        assertThat(((MemcachedGetCommand) decoded).keyAsString()).isEqualTo("7bf10939-d108-49f8-b530-d7b0fb07b71a");
    }


    @Test
    public void cannot_decode_malformed() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MemcachedGetSetCommandDecoder());

        assertThatThrownBy(() -> embeddedChannel.writeInbound(malformed)).isInstanceOf(DecoderException.class);
    }

    @Test
    @Ignore("handler may fail on non supported commands")
    public void cannot_decode_non_get_or_set_command() {
        // Given
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MemcachedGetSetCommandDecoder());

        // When
        embeddedChannel.writeInbound(unknown_operation);

        // Then
        assertThat(embeddedChannel.<Object>readInbound()).isInstanceOf(UnknownCommand.class);
    }

    @Test
    public void ensure_hex_decoding_ok() {
        assertThat(ByteBufUtil.hexDump(byteBufFromHexString(SET_OPERATION_HEX_DUMP))).isEqualTo(SET_OPERATION_HEX_DUMP);
    }
}