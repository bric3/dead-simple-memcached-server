package com.github.bric3.memcached;

import static net.spy.memcached.ConnectionFactoryBuilder.Protocol.TEXT;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import com.github.bric3.memcached.server.DeadSimpleTextMemcachedServer;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;

public class MemcachedClientAndServerRule implements MethodRule {
    private DeadSimpleTextMemcachedServer server;
    private MemcachedClient textMC;

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                server = new DeadSimpleTextMemcachedServer(TestConfiguration.PORT_NUMBER, 10_000);
                server.start();

                textMC = new MemcachedClient(new ConnectionFactoryBuilder().setProtocol(TEXT)
                                                                           .build(),
                                             AddrUtil.getAddresses(TestConfiguration.IPV4_ADDR + ":" + TestConfiguration.PORT_NUMBER));

                base.evaluate();

                textMC.shutdown();
                server.stop();
            }
        };
    }

    public DeadSimpleTextMemcachedServer server() {
        return server;
    }

    public MemcachedClient textMC() {
        return textMC;
    }
}
