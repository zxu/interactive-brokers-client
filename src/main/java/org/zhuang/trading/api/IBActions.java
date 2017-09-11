package org.zhuang.trading.api;

public interface IBActions {
    boolean isConnected();

    void connect();

    void disconnect();

    void placeFutureOrder(String symbol,
                          String contractMonth,
                          String exchange,
                          String action,
                          double price,
                          double trailingStopAmount);

    void retrieveMarketData(String symbol, String contractMonth, String exchange);
}
