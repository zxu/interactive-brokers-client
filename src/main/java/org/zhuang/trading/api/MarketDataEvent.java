package org.zhuang.trading.api;

import com.ib.client.ContractDetails;

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

    public static MarketDataEvent contractDetailsEvent(ContractDetails contractDetails) {
        return new MarketDataEvent(MarketDataType.CONTRACT_DETAILS, contractDetails);
    }

    public static MarketDataEvent nextOrderIdEvent(int id) {
        return new MarketDataEvent(MarketDataType.NEXT_ORDER_ID, Integer.valueOf(id));
    }

    public static MarketDataEvent updateUIPriceEvent(double price) {
        return new MarketDataEvent(MarketDataType.UPDATE_UI_PRICE, Double.valueOf(price));
    }

    public static MarketDataEvent priceIncrementEvent(double price) {
        return new MarketDataEvent(MarketDataType.PRICE_INCREMENT, price);
    }

    public MarketDataType type() {
        return type;
    }

    public Object data() {
        return data;
    }
}
