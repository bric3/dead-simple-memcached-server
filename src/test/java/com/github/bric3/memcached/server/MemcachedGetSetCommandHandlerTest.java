package com.github.bric3.memcached.server;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static com.github.bric3.memcached.TestUtils.byteBufFromHexString;
import static io.netty.util.CharsetUtil.US_ASCII;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.github.bric3.memcached.server.cache.CachedData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

public class MemcachedGetSetCommandHandlerTest {

    private final static String PAYLOAD = "aced00057372002a636f6d2e6769746875622e62726963332e6d656d6361636865642e476f616c73546573742456616c7565f88ba03a193b5c3c0200024c0001617400124c6a6176612f6c616e672f537472696e673b4c0001627400134c6a6176612f6c616e672f496e74656765723b78707400087768617465766572737200116a6176612e6c616e672e496e746567657212e2a0a4f781873802000149000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b020000787000000614";
    private ByteBuf payload = byteBufFromHexString(PAYLOAD);
    private final String string_key = "cdc40be0-ea6b-4ebf-bf9a-b635cfb0af8f";
    private ByteBuf key = Unpooled.copiedBuffer(string_key, US_ASCII);
    private String string_flags = "1";
    private ByteBuf flags = Unpooled.copiedBuffer(string_flags, US_ASCII);

    @Test
    public void should_handle_set_command_and_reply_STORED() {
        // Given
        Map<ByteBuf, CachedData> cache = new HashMap<>();
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MemcachedGetSetCommandHandler(cache));

        // When
        embeddedChannel.writeInbound(new MemcachedSetCommand(key, flags, payload));
        embeddedChannel.finish();

        // Then
        assertThat(embeddedChannel.<ByteBuf>readOutbound()).isEqualTo(Unpooled.copiedBuffer("STORED\r\n", US_ASCII));
        assertThat(cache).contains(entry(key, new CachedData(flags, payload)));
    }


    @Test
    public void should_handle_get_command_and_reply_VALUE_followed_by_END_when_cache_has_key() {
        // Given
        Map<ByteBuf, CachedData> cache = Collections.singletonMap(key, new CachedData(flags, payload));
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MemcachedGetSetCommandHandler(cache));

        // When
        embeddedChannel.writeInbound(new MemcachedGetCommand(key));
        embeddedChannel.finish();

        // Then
        assertThat(embeddedChannel.<ByteBuf>readOutbound())
                .isEqualTo(Unpooled.copiedBuffer(format("VALUE %s %s 202\r\n", string_key, string_flags), US_ASCII)
                                   .writeBytes(payload)
                                   .writeBytes(Unpooled.copiedBuffer("\r\nEND\r\n", US_ASCII)));
    }

    @Test
    public void should_handle_get_command_and_reply_END_when_cache_dont_have_key() {
        // Given
        Map<ByteBuf, CachedData> cache = new HashMap<>();
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new MemcachedGetSetCommandHandler(cache));

        // When
        embeddedChannel.writeInbound(new MemcachedGetCommand(key));
        embeddedChannel.finish();

        // Then
        assertThat(embeddedChannel.<ByteBuf>readOutbound())
                .isEqualTo(Unpooled.copiedBuffer("END\r\n", US_ASCII));
    }
}
