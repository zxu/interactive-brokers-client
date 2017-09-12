package org.zhuang.trading.api;

import com.google.common.eventbus.EventBus;
import com.ib.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EWrapperImpl implements EWrapper {
    @Autowired
    private EventBus marketDataEventBus;

    //! [socket_declare]
    private EReaderSignal readerSignal;
    private EClientSocket clientSocket;
    protected int currentOrderId = -1;
    //! [socket_declare]

    //! [socket_init]
    public EWrapperImpl() {
        readerSignal = new EJavaSignal();
        clientSocket = new EClientSocket(this, readerSignal);
    }

    //! [socket_init]
    public EClientSocket getClient() {
        return clientSocket;
    }

    public EReaderSignal getSignal() {
        return readerSignal;
    }

    public int getCurrentOrderId() {
        return currentOrderId;
    }

    //! [tickprice]
    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
        System.out.println("Tick Price. Ticker Id:" + tickerId + ", Field: " + TickType.getField(field) + ", Price: " + price + ", CanAutoExecute: " + canAutoExecute);

        switch (TickType.getField(field)) {
            case "bidPrice":
                marketDataEventBus.post(MarketDataEvent.bidPriceEvent(price));
                break;
            case "askPrice":
                marketDataEventBus.post(MarketDataEvent.askPriceEvent(price));
                break;
        }
    }
    //! [tickprice]

    //! [ticksize]
    public void tickSize(int tickerId, int field, int size) {
        System.out.println("Tick Size. Ticker Id:" + tickerId + ", Field: " + TickType.getField(field) + ", Size: " + size);
    }
    //! [ticksize]

    //! [tickoptioncomputation]
    public void tickOptionComputation(int tickerId, int field,
                                      double impliedVol, double delta, double optPrice,
                                      double pvDividend, double gamma, double vega, double theta,
                                      double undPrice) {
        System.out.println("TickOptionComputation. TickerId: " + tickerId + ", field: " + field + ", ImpliedVolatility: " + impliedVol + ", Delta: " + delta
                + ", OptionPrice: " + optPrice + ", pvDividend: " + pvDividend + ", Gamma: " + gamma + ", Vega: " + vega + ", Theta: " + theta + ", UnderlyingPrice: " + undPrice);
    }
    //! [tickoptioncomputation]

    //! [tickgeneric]
    public void tickGeneric(int tickerId, int tickType, double value) {
        System.out.println("Tick Generic. Ticker Id:" + tickerId + ", Field: " + TickType.getField(tickType) + ", Value: " + value);
    }
    //! [tickgeneric]

    //! [tickstring]
    public void tickString(int tickerId, int tickType, String value) {
        System.out.println("Tick string. Ticker Id:" + tickerId + ", Type: " + tickType + ", Value: " + value);
    }

    //! [tickstring]
    public void tickEFP(int tickerId, int tickType, double basisPoints,
                        String formattedBasisPoints, double impliedFuture, int holdDays,
                        String futureLastTradeDate, double dividendImpact,
                        double dividendsToLastTradeDate) {
        System.out.println("TickEFP. " + tickerId + ", Type: " + tickType + ", BasisPoints: " + basisPoints + ", FormattedBasisPoints: " +
                formattedBasisPoints + ", ImpliedFuture: " + impliedFuture + ", HoldDays: " + holdDays + ", FutureLastTradeDate: " + futureLastTradeDate +
                ", DividendImpact: " + dividendImpact + ", DividendsToLastTradeDate: " + dividendsToLastTradeDate);
    }

    //! [orderstatus]
    public void orderStatus(int orderId, String status, double filled,
                            double remaining, double avgFillPrice, int permId, int parentId,
                            double lastFillPrice, int clientId, String whyHeld) {
        System.out.println("OrderStatus. Id: " + orderId + ", Status: " + status + ", Filled" + filled + ", Remaining: " + remaining
                + ", AvgFillPrice: " + avgFillPrice + ", PermId: " + permId + ", ParentId: " + parentId + ", LastFillPrice: " + lastFillPrice +
                ", ClientId: " + clientId + ", WhyHeld: " + whyHeld);
    }
    //! [orderstatus]

    //! [openorder]
    public void openOrder(int orderId, Contract contract, Order order,
                          OrderState orderState) {
        System.out.println("OpenOrder. ID: " + orderId + ", " + contract.symbol() + ", " + contract.secType() + " @ " + contract.exchange() + ": " +
                order.action() + ", " + order.orderType() + " " + order.totalQuantity() + ", " + orderState.status());
    }
    //! [openorder]

    //! [openorderend]
    public void openOrderEnd() {
        System.out.println("OpenOrderEnd");
    }
    //! [openorderend]

    //! [updateaccountvalue]
    public void updateAccountValue(String key, String value, String currency,
                                   String accountName) {
        System.out.println("UpdateAccountValue. Key: " + key + ", Value: " + value + ", Currency: " + currency + ", AccountName: " + accountName);
    }
    //! [updateaccountvalue]

    //! [updateportfolio]
    public void updatePortfolio(Contract contract, double position,
                                double marketPrice, double marketValue, double averageCost,
                                double unrealizedPNL, double realizedPNL, String accountName) {
        System.out.println("UpdatePortfolio. " + contract.symbol() + ", " + contract.secType() + " @ " + contract.exchange()
                + ": Position: " + position + ", MarketPrice: " + marketPrice + ", MarketValue: " + marketValue + ", AverageCost: " + averageCost
                + ", UnrealisedPNL: " + unrealizedPNL + ", RealisedPNL: " + realizedPNL + ", AccountName: " + accountName);
    }
    //! [updateportfolio]

    //! [updateaccounttime]
    public void updateAccountTime(String timeStamp) {
        System.out.println("UpdateAccountTime. Time: " + timeStamp + "\n");
    }
    //! [updateaccounttime]

    //! [accountdownloadend]
    public void accountDownloadEnd(String accountName) {
        System.out.println("Account download finished: " + accountName + "\n");
    }
    //! [accountdownloadend]

    //! [nextvalidid]
    public void nextValidId(int orderId) {
        System.out.println("Next Valid Id: [" + orderId + "]");
        currentOrderId = orderId;

        marketDataEventBus.post(MarketDataEvent.nextOrderIdEvent(orderId));
    }
    //! [nextvalidid]

    //! [contractdetails]
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        System.out.println("ContractDetails. ReqId: [" + reqId + "] - [" + contractDetails.contract().symbol() + "], [" + contractDetails.contract().secType() + "], ConId: [" + contractDetails.contract().conid() + "] @ [" + contractDetails.contract().exchange() + "]");
    }

    //! [contractdetails]
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        System.out.println("bondContractDetails");
    }

    //! [contractdetailsend]
    public void contractDetailsEnd(int reqId) {
        System.out.println("ContractDetailsEnd. " + reqId + "\n");
    }
    //! [contractdetailsend]

    //! [execdetails]
    public void execDetails(int reqId, Contract contract, Execution execution) {
        System.out.println("ExecDetails. " + reqId + " - [" + contract.symbol() + "], [" + contract.secType() + "], [" + contract.currency() + "], [" + execution.execId() + "], [" + execution.orderId() + "], [" + execution.shares() + "]");
    }
    //! [execdetails]

    //! [execdetailsend]
    public void execDetailsEnd(int reqId) {
        System.out.println("ExecDetailsEnd. " + reqId + "\n");
    }
    //! [execdetailsend]

    //! [updatemktdepth]
    public void updateMktDepth(int tickerId, int position, int operation,
                               int side, double price, int size) {
        System.out.println("UpdateMarketDepth. " + tickerId + " - Position: " + position + ", Operation: " + operation + ", Side: " + side + ", Price: " + price + ", Size: " + size + "");
    }

    //! [updatemktdepth]
    public void updateMktDepthL2(int tickerId, int position,
                                 String marketMaker, int operation, int side, double price, int size) {
        System.out.println("updateMktDepthL2");
    }

    //! [updatenewsbulletin]
    public void updateNewsBulletin(int msgId, int msgType, String message,
                                   String origExchange) {
        System.out.println("News Bulletins. " + msgId + " - Type: " + msgType + ", Message: " + message + ", Exchange of Origin: " + origExchange + "\n");
    }
    //! [updatenewsbulletin]

    //! [managedaccounts]
    public void managedAccounts(String accountsList) {
        System.out.println("Account list: " + accountsList);
    }
    //! [managedaccounts]

    //! [receivefa]
    public void receiveFA(int faDataType, String xml) {
        System.out.println("Receing FA: " + faDataType + " - " + xml);
    }
    //! [receivefa]

    //! [historicaldata]
    public void historicalData(int reqId, String date, double open,
                               double high, double low, double close, int volume, int count,
                               double WAP, boolean hasGaps) {
        System.out.println("HistoricalData. " + reqId + " - Date: " + date + ", Open: " + open + ", High: " + high + ", Low: " + low + ", Close: " + close + ", Volume: " + volume + ", Count: " + count + ", WAP: " + WAP + ", HasGaps: " + hasGaps);
    }
    //! [historicaldata]

    //! [scannerparameters]
    public void scannerParameters(String xml) {
        System.out.println("ScannerParameters. " + xml + "\n");
    }
    //! [scannerparameters]

    //! [scannerdata]
    public void scannerData(int reqId, int rank,
                            ContractDetails contractDetails, String distance, String benchmark,
                            String projection, String legsStr) {
        System.out.println("ScannerData. " + reqId + " - Rank: " + rank + ", Symbol: " + contractDetails.contract().symbol() + ", SecType: " + contractDetails.contract().secType() + ", Currency: " + contractDetails.contract().currency()
                + ", Distance: " + distance + ", Benchmark: " + benchmark + ", Projection: " + projection + ", Legs String: " + legsStr);
    }
    //! [scannerdata]

    //! [scannerdataend]
    public void scannerDataEnd(int reqId) {
        System.out.println("ScannerDataEnd. " + reqId);
    }
    //! [scannerdataend]

    //! [realtimebar]
    public void realtimeBar(int reqId, long time, double open, double high,
                            double low, double close, long volume, double wap, int count) {
        System.out.println("RealTimeBars. " + reqId + " - Time: " + time + ", Open: " + open + ", High: " + high + ", Low: " + low + ", Close: " + close + ", Volume: " + volume + ", Count: " + count + ", WAP: " + wap);
    }

    //! [realtimebar]
    public void currentTime(long time) {
        System.out.println("currentTime");
    }

    //! [fundamentaldata]
    public void fundamentalData(int reqId, String data) {
        System.out.println("FundamentalData. ReqId: [" + reqId + "] - Data: [" + data + "]");
    }

    //! [fundamentaldata]
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract underComp) {
        System.out.println("deltaNeutralValidation");
    }

    //! [ticksnapshotend]
    public void tickSnapshotEnd(int reqId) {
        System.out.println("TickSnapshotEnd: " + reqId);
    }
    //! [ticksnapshotend]

    //! [marketdatatype]
    public void marketDataType(int reqId, int marketDataType) {
        System.out.println("MarketDataType. [" + reqId + "], Type: [" + marketDataType + "]\n");
    }
    //! [marketdatatype]

    //! [commissionreport]
    public void commissionReport(CommissionReport commissionReport) {
        System.out.println("CommissionReport. [" + commissionReport.m_execId + "] - [" + commissionReport.m_commission + "] [" + commissionReport.m_currency + "] RPNL [" + commissionReport.m_realizedPNL + "]");
    }
    //! [commissionreport]

    //! [position]
    public void position(String account, Contract contract, double pos,
                         double avgCost) {
        System.out.println("Position. " + account + " - Symbol: " + contract.symbol() + ", SecType: " + contract.secType() + ", Currency: " + contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost);
    }
    //! [position]

    //! [positionend]
    public void positionEnd() {
        System.out.println("PositionEnd \n");
    }
    //! [positionend]

    //! [accountsummary]
    public void accountSummary(int reqId, String account, String tag,
                               String value, String currency) {
        System.out.println("Acct Summary. ReqId: " + reqId + ", Acct: " + account + ", Tag: " + tag + ", Value: " + value + ", Currency: " + currency);
    }
    //! [accountsummary]

    //! [accountsummaryend]
    public void accountSummaryEnd(int reqId) {
        System.out.println("AccountSummaryEnd. Req Id: " + reqId + "\n");
    }

    //! [accountsummaryend]
    public void verifyMessageAPI(String apiData) {
        System.out.println("verifyMessageAPI");
    }

    public void verifyCompleted(boolean isSuccessful, String errorText) {
        System.out.println("verifyCompleted");
    }

    public void verifyAndAuthMessageAPI(String apiData, String xyzChallange) {
        System.out.println("verifyAndAuthMessageAPI");
    }

    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
        System.out.println("verifyAndAuthCompleted");
    }

    //! [displaygrouplist]
    public void displayGroupList(int reqId, String groups) {
        System.out.println("Display Group List. ReqId: " + reqId + ", Groups: " + groups + "\n");
    }
    //! [displaygrouplist]

    //! [displaygroupupdated]
    public void displayGroupUpdated(int reqId, String contractInfo) {
        System.out.println("Display Group Updated. ReqId: " + reqId + ", Contract info: " + contractInfo + "\n");
    }

    //! [displaygroupupdated]
    public void error(Exception e) {
        System.out.println("Exception in EWrapper: " + e.getMessage());
    }

    public void error(String str) {
        System.out.println("Error STR");
    }

    //! [error]
    public void error(int id, int errorCode, String errorMsg) {
        System.out.println("Error. Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg + "\n");
    }

    //! [error]
    public void connectionClosed() {
        System.out.println("Connection closed");
    }

    //! [connectack]
    public void connectAck() {
        if (clientSocket.isAsyncEConnect()) {
            System.out.println("Acknowledging connection");
            clientSocket.startAPI();
        }
    }
    //! [connectack]

    //! [positionmulti]
    public void positionMulti(int reqId, String account, String modelCode,
                              Contract contract, double pos, double avgCost) {
        System.out.println("Position Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode + ", Symbol: " + contract.symbol() + ", SecType: " + contract.secType() + ", Currency: " + contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost + "\n");
    }
    //! [positionmulti]

    //! [positionmultiend]
    public void positionMultiEnd(int reqId) {
        System.out.println("Position Multi End. Request: " + reqId + "\n");
    }
    //! [positionmultiend]

    //! [accountupdatemulti]
    public void accountUpdateMulti(int reqId, String account, String modelCode,
                                   String key, String value, String currency) {
        System.out.println("Account Update Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode + ", Key: " + key + ", Value: " + value + ", Currency: " + currency + "\n");
    }
    //! [accountupdatemulti]

    //! [accountupdatemultiend]
    public void accountUpdateMultiEnd(int reqId) {
        System.out.println("Account Update Multi End. Request: " + reqId + "\n");
    }
    //! [accountupdatemultiend]

    //! [securityDefinitionOptionParameter]
    public void securityDefinitionOptionalParameter(int reqId, String exchange,
                                                    int underlyingConId, String tradingClass, String multiplier,
                                                    Set<String> expirations, Set<Double> strikes) {
        System.out.println("Security Definition Optional Parameter. Request: " + reqId + ", Trading Class: " + tradingClass + ", Multiplier: " + multiplier + " \n");
    }

    //! [securityDefinitionOptionParameter]
    public void securityDefinitionOptionalParameterEnd(int reqId) {
        // TODO Auto-generated method stub

    }

    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
        for (SoftDollarTier tier : tiers) {
            System.out.print("tier: " + tier + ", ");
        }

        System.out.println();
    }

}
