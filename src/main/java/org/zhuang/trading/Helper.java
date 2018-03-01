package org.zhuang.trading;

import org.zhuang.trading.api.Direction;

class Helper {
    public static double midPointPrice(double bidPrice, double askPrice, double priceIncrement, Direction direction) {
        double steps = Math.floor((askPrice - bidPrice) / priceIncrement);
        int increment = (int) Math.floor(steps / 2);

        double result = 0;
        if (direction == Direction.BUY) {
            result = askPrice - increment * priceIncrement;
        }
        if (direction == Direction.SELL) {
            result = bidPrice + increment * priceIncrement;
        }

        return result;
    }
}
