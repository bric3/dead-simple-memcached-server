package com.github.bric3.memcached;

import static org.assertj.core.api.Assertions.assertThat;
import static net.spy.memcached.ConnectionFactoryBuilder.Protocol.TEXT;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import com.github.bric3.memcached.server.DeadSimpleTextMemcachedServer;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

public class SimpleTextMemcachedGoalsTest {
    private static final int IGNORED_EXPIRATION = 0;

    private static MemcachedClient textMC;
    private static DeadSimpleTextMemcachedServer server;

    @BeforeClass
    public static void init_text_protocol_memcached_client_and_server() throws Exception {
        init_memcached_server(TestConfiguration.PORT_NUMBER);
        init_memcached_client_with_text_protocol(TestConfiguration.IPV4_ADDR, TestConfiguration.PORT_NUMBER);
    }

    private static void init_memcached_server(Integer portNumber) {
        server = new DeadSimpleTextMemcachedServer(portNumber, 10_000);
        server.start();
    }

    private static void init_memcached_client_with_text_protocol(String ipv4Addr, Integer portNumber) throws IOException {
        textMC = new MemcachedClient(new ConnectionFactoryBuilder().setProtocol(TEXT)
                                                                   .build(),
                                     AddrUtil.getAddresses(ipv4Addr + ":" + portNumber));
    }

    @AfterClass
    public static void shutdown_server() throws Exception {
        textMC.shutdown();
        server.stop();
    }

    @Test
    public void can_set_and_get_with_serialized_value() throws IOException, ExecutionException, InterruptedException {
        // Given
        String key = UUID.randomUUID().toString();

        // When
        OperationFuture<Boolean> whateverSetFuture = textMC.set(key, IGNORED_EXPIRATION, new Value("whatever", 73));

        // Then
        assertThat(whateverSetFuture.get()).isTrue();
        assertThat(textMC.get(key)).isEqualTo(new Value("whatever", 73));
    }

    @Test
    public void can_set_and_get_with_string() throws IOException, ExecutionException, InterruptedException {
        // Given
        String key = UUID.randomUUID().toString();

        // When
        OperationFuture<Boolean> whateverSetFuture = textMC.set(key, IGNORED_EXPIRATION, "string");

        // Then
        assertThat(whateverSetFuture.get()).isTrue();
        assertThat(textMC.get(key)).isEqualTo("string");
    }

    @Test
    public void can_set_with_serialized_value() throws IOException, ExecutionException, InterruptedException {
        // Given
        String key = UUID.randomUUID().toString();

        // When
        OperationFuture<Boolean> whateverSetFuture = textMC.set(key, IGNORED_EXPIRATION, new Value("whatever", 1_556));


        // Then
        assertThat(whateverSetFuture.get()).isTrue();
    }

    @Test
    public void can_get_non_exiting_key() throws IOException, ExecutionException, InterruptedException {
        // Given
        String key = UUID.randomUUID().toString();

        // When
        Object value = textMC.get(key);

        // Then
        assertThat(value).isNull();
    }


    public static class Value implements Serializable {
        String a;
        Integer b;

        public Value(String a, Integer b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Value value = (Value) o;
            return Objects.equals(a, value.a) &&
                    Objects.equals(b, value.b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }
}
