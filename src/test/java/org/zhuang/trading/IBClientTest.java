package org.zhuang.trading;

import com.google.common.eventbus.EventBus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zhuang.trading.api.Direction;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IBClientConfig.class})
public class IBClientTest {
    @Autowired
    private EventBus marketDataEventBus;

    @Autowired
    private ExecutorService executor;

    @Autowired
    private Map<String, String> data;

    @Autowired
    private Map<String, String> defaultValues;

    @Test
    public void eventBusIsAvailable() {
        assertNotNull(marketDataEventBus);
    }

    @Test
    public void executorIsAvailable() {
        assertNotNull(executor);
    }

    @Test
    public void dataIsAvailable() {
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    @Test
    public void defaultValuesIsAvailable() {
        assertNotNull(defaultValues);
        assertFalse(defaultValues.isEmpty());
    }

    @Test
    public void midPointCalculationIsCorrect() {
        assertEquals(Helper.midPointPrice(6, 6, 0.25, Direction.BUY), 6, 0);
        assertEquals(Helper.midPointPrice(6, 6, 0.25, Direction.SELL), 6, 0);
        assertEquals(Helper.midPointPrice(6, 6.25, 0.25, Direction.BUY), 6.25, 0);
        assertEquals(Helper.midPointPrice(6, 6.25, 0.25, Direction.SELL), 6, 0);
        assertEquals(Helper.midPointPrice(6, 6.50, 0.25, Direction.BUY), 6.25, 0);
        assertEquals(Helper.midPointPrice(6, 6.50, 0.25, Direction.SELL), 6.25, 0);
        assertEquals(Helper.midPointPrice(6, 6.75, 0.25, Direction.BUY), 6.5, 0);
        assertEquals(Helper.midPointPrice(6, 6.75, 0.25, Direction.SELL), 6.25, 0);
        assertEquals(Helper.midPointPrice(6, 7, 0.25, Direction.BUY), 6.5, 0);
        assertEquals(Helper.midPointPrice(6, 7, 0.25, Direction.SELL), 6.5, 0);
    }
}
