package org.zhuang.trading.api;

import com.ib.client.Order;

import java.util.ArrayList;
import java.util.List;

public class Orders {
    public static Order marketOrder(String action, double quantity) {
        //! [market]
        Order order = new Order();
        order.action(action);
        order.orderType("MKT");
        order.totalQuantity(quantity);
        //! [market]
        return order;
    }

    public static Order limitOrder(String action, double quantity, double limitPrice) {
        // ! [limitorder]
        Order order = new Order();
        order.action(action);
        order.orderType("LMT");
        order.totalQuantity(quantity);
        order.lmtPrice(limitPrice);
        // ! [limitorder]
        return order;
    }

    //! [bracket]
    public static List<Order> bracketOrder(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, double stopLossPrice) {
        //This will be our main or "parent" order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action);
        parent.orderType("LMT");
        parent.totalQuantity(quantity);
        parent.lmtPrice(limitPrice);
        //The parent and children com.trading.test.orders will need this attribute set to false to prevent accidental executions.
        //The LAST CHILD will have it set to true.
        parent.transmit(false);

        Order takeProfit = new Order();
        takeProfit.orderId(parent.orderId() + 1);
        takeProfit.action(action.equals("BUY") ? "SELL" : "BUY");
        takeProfit.orderType("LMT");
        takeProfit.totalQuantity(quantity);
        takeProfit.lmtPrice(takeProfitLimitPrice);
        takeProfit.parentId(parentOrderId);
        takeProfit.transmit(false);

        Order stopLoss = new Order();
        stopLoss.orderId(parent.orderId() + 2);
        stopLoss.action(action.equals("BUY") ? "SELL" : "BUY");
        stopLoss.orderType("STP");
        //Stop trigger price
        stopLoss.auxPrice(stopLossPrice);
        stopLoss.totalQuantity(quantity);
        stopLoss.parentId(parentOrderId);
        //In this case, the low side order will be the last child being sent. Therefore, it needs to set this attribute to true
        //to activate all its predecessors
        stopLoss.transmit(true);

        List<Order> bracketOrder = new ArrayList<Order>();
        bracketOrder.add(parent);
        bracketOrder.add(takeProfit);
        bracketOrder.add(stopLoss);

        return bracketOrder;
    }
    //! [bracket]

    public static Order stop(String action, double quantity, double stopPrice) {
        // ! [stop]
        Order order = new Order();
        order.action(action);
        order.orderType("STP");
        order.auxPrice(stopPrice);
        order.totalQuantity(quantity);
        // ! [stop]
        return order;
    }

    public static Order stopLimit(String action, double quantity, double limitPrice, double stopPrice) {
        // ! [stoplimit]
        Order order = new Order();
        order.action(action);
        order.orderType("STP LMT");
        order.lmtPrice(limitPrice);
        order.auxPrice(stopPrice);
        order.totalQuantity(quantity);
        // ! [stoplimit]
        return order;
    }

    public static Order trailingStop(String action, double trailingAmount, double quantity) {
        // ! [trailingstop]
        Order order = new Order();
        order.action(action);
        order.orderType("TRAIL");
//        order.trailingPercent(trailingPercent);
//        order.trailStopPrice(trailStopPrice);
        order.auxPrice(trailingAmount);
        order.totalQuantity(quantity);
        // ! [trailingstop]
        return order;
    }

    public static Order trailingStopLimit(String action, double quantity, double trailingAmount, double trailStopPrice, double limitPrice) {
        // ! [trailingstoplimit]
        Order order = new Order();
        order.action(action);
        order.orderType("TRAIL LIMIT");
        order.lmtPrice(limitPrice);
        order.auxPrice(trailingAmount);
        order.trailStopPrice(trailStopPrice);
        order.totalQuantity(quantity);
        // ! [trailingstoplimit]
        return order;
    }
}
