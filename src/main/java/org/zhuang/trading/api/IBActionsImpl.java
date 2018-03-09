package org.zhuang.trading.api;

import com.google.common.eventbus.EventBus;
import com.ib.client.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class IBActionsImpl implements IBActions {
    private final static Logger logger = LoggerFactory.getLogger(IBActionsImpl.class);

    @Autowired
    private EWrapperImpl wrapper;

    @Autowired
    private ExecutorService executor;

    @Autowired
    private EventBus eventBus;

    @Override
    public boolean isConnected() {
        return wrapper.getClient().isConnected();
    }

    @Override
    public void connect() {
        final EClientSocket client = wrapper.getClient();
        final EReaderSignal signal = wrapper.getSignal();

        client.eConnect("127.0.0.1", 7497, 0);

        final EReader reader = new EReader(client, signal);

        reader.start();
        executor.execute(() -> {
            while (client.isConnected()) {
                logger.info("Waiting for a signal...");
                signal.waitForSignal();
                logger.info("A signal has arrived");
                try {
                    reader.processMsgs();
                } catch (Exception e) {
                    logger.error("Exception in IB message loop: " + e.getMessage());
                }
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        final EClientSocket client = wrapper.getClient();
        client.eDisconnect();

        wrapper.getSignal().issueSignal();
    }

    @Override
    public void placeFutureOrder(int orderId, String symbol,
                                 String contractMonth,
                                 String exchange,
                                 String action,
                                 double price,
                                 double trailingStopAmount,
                                 double quantity) {
        final EClientSocket client = wrapper.getClient();

        Contract contract = Contracts.simpleFuture(symbol, contractMonth, exchange);


        List<Order> orders = new ArrayList<>();

        int parentOrderId = orderId;
        Order parentOrder = Orders.limitOrder(action, quantity, price);
        parentOrder.orderId(parentOrderId);
        parentOrder.transmit(true);

        orders.add(parentOrder);

        if (trailingStopAmount > 0) {
            Order trailingStopOrder = Orders.trailingStop(reverse(action), trailingStopAmount, quantity);
            trailingStopOrder.orderId(parentOrder.orderId() + 1);
            trailingStopOrder.parentId(parentOrderId);
            trailingStopOrder.transmit(true);

            parentOrder.transmit(false);

            orders.add(trailingStopOrder);
        }

        orders.forEach(order -> {
            client.placeOrder(order.orderId(), contract, order);
        });

        eventBus.post(MarketDataEvent.nextOrderIdEvent(orderId + orders.size()));
    }

    @Override
    public void placeFutureOrderMarket(int orderId, String symbol, String contractMonth, String exchange, String action, double quantity) {
        final EClientSocket client = wrapper.getClient();

        Contract contract = Contracts.simpleFuture(symbol, contractMonth, exchange);

        Order order = Orders.marketOrder(action, quantity);
        order.orderId(orderId);
        order.transmit(true);

        client.placeOrder(order.orderId(), contract, order);

        eventBus.post(MarketDataEvent.nextOrderIdEvent(orderId + 1));
    }

    @Override
    public void cancelAllOrders() {
        final EClientSocket client = wrapper.getClient();

        client.reqGlobalCancel();
    }

    private String reverse(String action) {
        return action.equals("BUY") ? "SELL" : "BUY";
    }

    @Override
    public void retrieveMarketData(String symbol, String contractMonth, String exchange) {
        final EClientSocket client = wrapper.getClient();

        client.cancelMktData(1001);

        Contract contract = Contracts.simpleFuture(symbol, contractMonth, exchange);

        client.reqContractDetails(100101, contract);

        client.reqMktData(1001,
                contract,
                "",
                false,
                false,
                null);
    }

    @Override
    public void retrieveMarketRules(String marketRulesIds) {
        final EClientSocket client = wrapper.getClient();

        String[] ruleIds = StringUtils.split(marketRulesIds, ',');
        for (String ruleId : ruleIds) {
            client.reqMarketRule(Integer.parseInt(ruleId));
        }
    }

    @Override
    public void retrievePositions() {
        final EClientSocket client = wrapper.getClient();

        client.reqPositions();
    }
}
