package com.github.bric3.memcached;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.Rule;
import org.junit.Test;
import net.spy.memcached.internal.OperationFuture;

public class SimpleTextMemcachedGoalsTest {
    private static final int IGNORED_EXPIRATION = 0;

    @Rule
    public MemcachedClientAndServerRule serverRule = new MemcachedClientAndServerRule();


    @Test
    public void can_set_and_get_with_serialized_value() throws IOException, ExecutionException, InterruptedException {
        // Given
        String key = UUID.randomUUID().toString();

        // When
        OperationFuture<Boolean> whateverSetFuture = serverRule.textMC().set(key, IGNORED_EXPIRATION, new Value("whatever", 73));

        // Then
        assertThat(whateverSetFuture.get()).isTrue();
        assertThat(serverRule.textMC().get(key)).isEqualTo(new Value("whatever", 73));
    }

    @Test
    public void can_set_and_get_with_string() throws IOException, ExecutionException, InterruptedException {
        // Given
        String key = UUID.randomUUID().toString();

        // When
        OperationFuture<Boolean> whateverSetFuture = serverRule.textMC().set(key, IGNORED_EXPIRATION, "string");

        // Then
        assertThat(whateverSetFuture.get()).isTrue();
        assertThat(serverRule.textMC().get(key)).isEqualTo("string");
    }

    @Test
    public void can_set_with_serialized_value() throws IOException, ExecutionException, InterruptedException {
        // Given
        String key = UUID.randomUUID().toString();

        // When
        OperationFuture<Boolean> whateverSetFuture = serverRule.textMC().set(key, IGNORED_EXPIRATION, new Value("whatever", 1_556));


        // Then
        assertThat(whateverSetFuture.get()).isTrue();
    }

    @Test
    public void can_get_non_exiting_key() throws IOException, ExecutionException, InterruptedException {
        // Given
        String key = UUID.randomUUID().toString();

        // When
        Object value = serverRule.textMC().get(key);

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
