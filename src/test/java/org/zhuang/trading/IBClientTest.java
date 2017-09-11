package org.zhuang.trading;

import com.google.common.eventbus.EventBus;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zhuang.trading.config.IBClientConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IBClientConfig.class})
public class IBClientTest {
    @Autowired
    private EventBus marketDataEventBus;

    @Test
    public void eventBusIsAvailable() {
        assertNotNull(marketDataEventBus);
    }
}
