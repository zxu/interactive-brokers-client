package org.zhuang.trading;

import com.google.common.eventbus.EventBus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IBClientConfig.class})
public class IBClientTest {
    @Autowired
    private EventBus marketDataEventBus;

    @Autowired
    private ExecutorService executor;

    @Test
    public void eventBusIsAvailable() {
        assertNotNull(marketDataEventBus);
    }

    @Test
    public void executorIsAvailable() {
        assertNotNull(executor);
    }

}
