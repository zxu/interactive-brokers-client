package org.zhuang.trading.config;

import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zhuang.trading.IBClientMain;

@Configuration
@ComponentScan(basePackageClasses = {IBClientMain.class})
public class IBClientConfig {
    @Bean(name = "marketDataEventBus")
    public EventBus eventBus() {
        return new EventBus();
    }
}
