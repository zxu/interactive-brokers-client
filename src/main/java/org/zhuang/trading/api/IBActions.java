package org.zhuang.trading.api;

import com.ib.client.*;

import java.util.ArrayList;
import java.util.List;

public class IBActions {
    private final EWrapperImpl wrapper = new EWrapperImpl();

    public boolean isConnected() {
        return wrapper.getClient().isConnected();
    }

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
                        System.out.println("Exception: "+e.getMessage());
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

    public void disconnect() {
        final EClientSocket client = wrapper.getClient();
        client.eDisconnect();
    }

    public void placeFutureOrder(String symbol,
                                 String contractMonth,
                                 String exchange,
                                 String action,
                                 double trailingStopAmount) {
        final EClientSocket client = wrapper.getClient();

        Contract contract = Contracts.simpleFuture(symbol, contractMonth, exchange);

        client.reqIds(-1);
        int parentOrderId = wrapper.getCurrentOrderId() + 1;

        Order parentOrder = Orders.limitOrder(action, 1, 100);
        parentOrder.orderId(parentOrderId);
        parentOrder.transmit(false);

        Order trailingStopOrder = Orders.trailingStop(action, trailingStopAmount, 1);
        trailingStopOrder.orderId(parentOrder.orderId() + 1);
        trailingStopOrder.parentId(parentOrderId);
        trailingStopOrder.transmit(true);

        List<Order> orders = new ArrayList<>();
        orders.add(parentOrder);
        orders.add(trailingStopOrder);

        orders.forEach(order -> {
            client.placeOrder(order.orderId(), contract, order);
        });
//
//
//        client.placeOrder(parentOrderId,
//                contract,
//                parentOrder);

//        client.reqExecutions(10001, new ExecutionFilter());

//        try {
//            Thread.sleep(1000 * 10);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println("================================");
//        System.out.println("================================");
//        System.out.println("================================");
//
//
//        int childOrderId = parentOrderId++;
//        client.placeOrder(childOrderId,
//                contract,
//                trailingStopOrder);

//
//        client.reqExecutions(10001, new ExecutionFilter());
    }

    private String reverse(String action) {
        return action.equals("BUY") ? "SELL" : "BUY";
    }
}
