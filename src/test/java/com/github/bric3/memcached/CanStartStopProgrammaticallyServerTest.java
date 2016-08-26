package com.github.bric3.memcached;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.net.Socket;
import org.junit.Test;
import com.github.bric3.memcached.server.DeadSimpleTextMemcachedServer;

public class CanStartStopProgrammaticallyServerTest {
    @Test
    public void can_stop_the_server() {
        // given
        DeadSimpleTextMemcachedServer server = new DeadSimpleTextMemcachedServer(TestConfiguration.PORT_NUMBER, 10_000);
        server.start();

        // when
        server.stop();

        // then
        assertThat(new SimpleSocketConnector().canConnect(TestConfiguration.PORT_NUMBER)).isFalse();
    }

    @Test
    public void can_connect_to_a_started_server_before_stop() {
        // given
        DeadSimpleTextMemcachedServer server = new DeadSimpleTextMemcachedServer(TestConfiguration.PORT_NUMBER, 10_000);


        // when
        server.start();

        // then
        assertThat(new SimpleSocketConnector().canConnect(TestConfiguration.PORT_NUMBER)).isTrue();
        server.stop();
    }

    private class SimpleSocketConnector {

        boolean canConnect(Integer portNumber) {
            try {
                Socket clientSocket = new Socket("localhost", portNumber);
                clientSocket.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
