package com.github.bric3.memcached;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Rule;
import org.junit.Test;

public class WorkInProgressMultithreadedTest {

    private static final int IGNORED_EXPIRATION = 0;
    @Rule
    public MemcachedClientAndServerRule serverRule = new MemcachedClientAndServerRule(10_000);

    @Test
    public void exercise_server_with_multiple_client_iterating_over_a_finite_shuffled_set_of_keys() {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<String> keys = prepareKeys(10_000);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            executorService.submit(new MyRunnable(keys));
        }
    }

    private class MyRunnable implements Runnable {
        private List<String> keys;

        public MyRunnable(List<String> keys) {
            this.keys = new ArrayList<>(keys);
            Collections.shuffle(keys);
        }

        @Override
        public void run() {
            // TODO latency measures, e.g. HdrHistogram
            for (int i = 0; i < keys.size(); i++) {
                if (i % 2 == 0) {
                    serverRule.textMC().set(keys.get(i), IGNORED_EXPIRATION, keys.get(i));
                } else {
                    Object o = serverRule.textMC().get(keys.get(i));
                    assertThat(o).isIn(null, keys.get(i));
                }
            }
        }

    }

    private List<String> prepareKeys(int count) {
        List<String> keys = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            keys.add(UUID.randomUUID().toString());
        }
        return keys;
    }
}
