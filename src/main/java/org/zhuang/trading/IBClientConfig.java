package org.zhuang.trading;

import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zhuang.trading.IBClientMain;
import org.zhuang.trading.api.IBActions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ComponentScan(basePackageClasses = {IBClientConfig.class})
public class IBClientConfig {
    @Bean(name = "marketDataEventBus")
    public EventBus eventBus() {
        return new EventBus();
    }

    @Bean(name = "executor")
    public ExecutorService executor() {
        return Executors.newFixedThreadPool(2);
    }
}
