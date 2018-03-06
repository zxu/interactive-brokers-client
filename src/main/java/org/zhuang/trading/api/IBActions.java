package org.zhuang.trading.api;

public interface IBActions {
    boolean isConnected();

    void connect();

    void disconnect();

    void placeFutureOrder(int orderId, String symbol,
                          String contractMonth,
                          String exchange,
                          String action,
                          double price,
                          double trailingStopAmount,
                          int quantity);

    void retrieveMarketData(String symbol, String contractMonth, String exchange);

    void retrieveMarketRules(String marketRulesIds);

    void retrievePositions();
}
