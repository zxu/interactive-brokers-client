package org.zhuang.trading;

import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@ComponentScan(basePackageClasses = {IBClientConfig.class})
public class IBClientConfig {
    private static final Map<String, String> data;
    private static final Map<String, String> defaultValues;

    static {
        data = new ConcurrentHashMap<>();
        defaultValues = new ConcurrentHashMap<>();

        defaultValues.put(Constants.SYMBOL, "NQ");
        defaultValues.put(Constants.MONTH, new SimpleDateFormat("yyyyMM").format(Calendar.getInstance().getTime()));
        defaultValues.put(Constants.EXCHANGE, "GLOBEX");
        defaultValues.put(Constants.QUANTITY, "1");
    }

    @Bean(name = "marketDataEventBus")
    public EventBus eventBus() {
        return new EventBus();
    }

    @Bean(name = "executor")
    public ExecutorService executor() {
        return Executors.newFixedThreadPool(2);
    }

    @Bean(name = "data")
    public Map<String, String> data() {
        return data;
    }

    @Bean(name = "defaultValues")
    public Map<String, String> defaultValues() {
        return defaultValues;
    }
}
