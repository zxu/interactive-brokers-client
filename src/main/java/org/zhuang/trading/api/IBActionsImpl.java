package org.zhuang.trading.api;

import com.ib.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IBActionsImpl implements IBActions {
    @Autowired
    private EWrapperImpl wrapper;

    private Map<String, String> data = new ConcurrentHashMap<>();

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
        new Thread() {
            public void run() {
                while (client.isConnected()) {
                    signal.waitForSignal();
                    try {
                        reader.processMsgs();
                    } catch (Exception e) {
                        System.out.println("Exception: " + e.getMessage());
                    }
                }
            }
        }.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.reqIds(-1);
        System.out.println(wrapper.getCurrentOrderId());
    }

    @Override
    public void disconnect() {
        final EClientSocket client = wrapper.getClient();
        client.eDisconnect();
    }

    @Override
    public void placeFutureOrder(String symbol,
                                 String contractMonth,
                                 String exchange,
                                 String action,
                                 double price,
                                 double trailingStopAmount) {
        final EClientSocket client = wrapper.getClient();

        Contract contract = Contracts.simpleFuture(symbol, contractMonth, exchange);

        client.reqIds(-1);
        int parentOrderId = wrapper.getCurrentOrderId() + 1;

        Order parentOrder = Orders.limitOrder(action, 1, price);
        parentOrder.orderId(parentOrderId);
        parentOrder.transmit(false);

        Order trailingStopOrder = Orders.trailingStop(reverse(action), trailingStopAmount, 1);
        trailingStopOrder.orderId(parentOrder.orderId() + 1);
        trailingStopOrder.parentId(parentOrderId);
        trailingStopOrder.transmit(true);

        List<Order> orders = new ArrayList<>();
        orders.add(parentOrder);
        orders.add(trailingStopOrder);

        orders.forEach(order -> {
            client.placeOrder(order.orderId(), contract, order);
        });
    }

    private String reverse(String action) {
        return action.equals("BUY") ? "SELL" : "BUY";
    }

    @Override
    public void retrieveMarketData(String symbol, String contractMonth, String exchange) {
        final EClientSocket client = wrapper.getClient();

        client.reqMktData(1001,
                Contracts.simpleFuture(symbol, contractMonth, exchange),
                "",
                false,
                null);
    }

    public Map<String, String> getData() {
        return data;
    }
}
