package org.zhuang.trading.api;

public class MarketDataEvent {
    public MarketDataEvent(MarketDataType type, Object data) {
        this.type = type;
        this.data = data;
    }

    private MarketDataType type;
    private Object data;

    public static MarketDataEvent bidPriceEvent(double price) {
        return new MarketDataEvent(MarketDataType.BID_PRICE, Double.valueOf(price));
    }

    public static MarketDataEvent askPriceEvent(double price) {
        return new MarketDataEvent(MarketDataType.ASK_PRICE, Double.valueOf(price));
    }

    public MarketDataType type() {
        return type;
    }

    public Object data() {
        return data;
    }
}
