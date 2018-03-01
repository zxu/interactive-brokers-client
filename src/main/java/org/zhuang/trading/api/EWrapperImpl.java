package org.zhuang.trading.api;

import com.google.common.eventbus.EventBus;
import com.ib.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class EWrapperImpl implements EWrapper {
    private final static Logger logger = LoggerFactory.getLogger(EWrapperImpl.class);

    @Autowired
    private EventBus eventBus;

    private EReaderSignal readerSignal;
    private EClientSocket clientSocket;
    protected int currentOrderId = -1;

    public EWrapperImpl() {
        readerSignal = new EJavaSignal();
        clientSocket = new EClientSocket(this, readerSignal);
    }

    public EClientSocket getClient() {
        return clientSocket;
    }

    public EReaderSignal getSignal() {
        return readerSignal;
    }

    public int getCurrentOrderId() {
        return currentOrderId;
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttr tickAttr) {
        logger.info(String.format("%12s[%4d] %12s: %12f%16s: %b",
                "Tick Price",
                tickerId,
                TickType.getField(field),
                price,
                "CanAutoExecute",
                tickAttr.canAutoExecute()));

        switch (TickType.getField(field)) {
            case "bidPrice":
                eventBus.post(MarketDataEvent.bidPriceEvent(price));
                break;
            case "askPrice":
                eventBus.post(MarketDataEvent.askPriceEvent(price));
                break;
        }
    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
        logger.info(String.format("%12s[%4d] %12s: %12d",
                "Tick Size",
                tickerId,
                TickType.getField(field),
                size));
    }

    @Override
    public void tickOptionComputation(int tickerId, int field,
                                      double impliedVol, double delta, double optPrice,
                                      double pvDividend, double gamma, double vega, double theta,
                                      double undPrice) {
        logger.info(String.format("TickOptionComputation. TickerId: %d, " +
                        "field: %d, ImpliedVolatility: %f, Delta: %f, " +
                        "OptionPrice: %f, pvDividend: %f, Gamma: %f, " +
                        "Vega: %f, Theta: %f, UnderlyingPrice: %f",
                tickerId,
                field,
                impliedVol,
                delta,
                optPrice,
                pvDividend,
                gamma,
                vega,
                theta,
                undPrice));
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
        logger.info(String.format("Tick Generic. Ticker Id: %d, Field: %s, Value: %f",
                tickerId,
                TickType.getField(tickType),
                value));
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
        logger.info(String.format("Tick string. Ticker Id: %d, Type: %d, Value: %s",
                tickerId,
                tickType,
                value));
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints,
                        String formattedBasisPoints, double impliedFuture, int holdDays,
                        String futureLastTradeDate, double dividendImpact,
                        double dividendsToLastTradeDate) {
        logger.info(String.format("TickEFP. %d, Type: %d, BasisPoints: %f, FormattedBasisPoints: %s, " +
                        "ImpliedFuture: %f, HoldDays: %d, FutureLastTradeDate: %s, DividendImpact: %f, " +
                        "DividendsToLastTradeDate: %f",
                tickerId,
                tickType,
                basisPoints,
                formattedBasisPoints,
                impliedFuture,
                holdDays,
                futureLastTradeDate,
                dividendImpact,
                dividendsToLastTradeDate));
    }

    @Override
    public void orderStatus(int orderId, String status, double filled,
                            double remaining, double avgFillPrice, int permId, int parentId,
                            double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        logger.info(String.format("OrderStatus. Id: %d, Status: %s, Filled: %f, Remaining: %f, " +
                        "AvgFillPrice: %f, PermId: %d, ParentId: %d, LastFillPrice: %f, ClientId: %d, WhyHeld: %s",
                orderId,
                status,
                filled,
                remaining,
                avgFillPrice,
                permId,
                parentId,
                lastFillPrice,
                clientId,
                whyHeld));

        eventBus.post(MarketDataEvent.orderStatusEvent(status));
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order,
                          OrderState orderState) {
        logger.info(String.format("OpenOrder. ID: %d, %s, %s @ %s: %s, %s %d, %s",
                orderId,
                contract.symbol(),
                contract.secType(),
                contract.exchange(),
                order.action(),
                order.orderType(),
                order.totalQuantity(),
                orderState.status()));
    }

    @Override
    public void openOrderEnd() {
        logger.info("OpenOrderEnd");
    }

    @Override
    public void updateAccountValue(String key, String value, String currency,
                                   String accountName) {
        logger.info("UpdateAccountValue. Key: " + key + ", Value: " + value +
                ", Currency: " + currency + ", AccountName: " + accountName);
    }

    @Override
    public void updatePortfolio(Contract contract, double position,
                                double marketPrice, double marketValue, double averageCost,
                                double unrealizedPNL, double realizedPNL, String accountName) {
        logger.info("UpdatePortfolio. " + contract.symbol() + ", " + contract.secType() + " @ " + contract.exchange()
                + ": Position: " + position + ", MarketPrice: " + marketPrice + ", MarketValue: " + marketValue +
                ", AverageCost: " + averageCost + ", UnrealisedPNL: " + unrealizedPNL + ", RealisedPNL: " +
                realizedPNL + ", AccountName: " + accountName);
    }

    @Override
    public void updateAccountTime(String timeStamp) {
        logger.info("UpdateAccountTime. Time: " + timeStamp + "\n");
    }

    @Override
    public void accountDownloadEnd(String accountName) {
        logger.info("Account download finished: " + accountName + "\n");
    }

    @Override
    public void nextValidId(int orderId) {
        logger.info(String.format("Next Valid Id: [%d]", orderId));
        currentOrderId = orderId;

        eventBus.post(MarketDataEvent.nextOrderIdEvent(orderId));
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        logger.info("ContractDetails. ReqId: [" + reqId + "] - [" + contractDetails.contract().symbol() + "], [" +
                contractDetails.contract().secType() + "], ConId: [" + contractDetails.contract().conid() +
                "] @ [" + contractDetails.contract().exchange() +
                "], MarketRulesIds: [" + contractDetails.marketRuleIds() + "]");

        eventBus.post(MarketDataEvent.contractDetailsEvent(contractDetails));
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        logger.info("bondContractDetails");
    }

    @Override
    public void contractDetailsEnd(int reqId) {
        logger.info("ContractDetailsEnd. " + reqId + "\n");
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        logger.info(String.format("ExecDetails. %d - [%s], [%s], [%s], [%d], [%d], [%f]",
                reqId,
                contract.symbol(),
                contract.secType(),
                contract.currency(),
                execution.execId(),
                execution.orderId(),
                execution.shares()));
    }

    @Override
    public void execDetailsEnd(int reqId) {
        logger.info("ExecDetailsEnd. " + reqId + "\n");
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation,
                               int side, double price, int size) {
        logger.info("UpdateMarketDepth. " + tickerId + " - Position: " + position + ", Operation: " +
                operation + ", Side: " + side + ", Price: " + price + ", Size: " + size + "");
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position,
                                 String marketMaker, int operation, int side, double price, int size) {
        logger.info("updateMktDepthL2");
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message,
                                   String origExchange) {
        logger.info("News Bulletins. " + msgId + " - Type: " + msgType + ", Message: " + message +
                ", Exchange of Origin: " + origExchange + "\n");
    }

    @Override
    public void managedAccounts(String accountsList) {
        logger.info(String.format("Account list: %s", accountsList));
    }

    @Override
    public void receiveFA(int faDataType, String xml) {
        logger.info("Receing FA: " + faDataType + " - " + xml);
    }

    @Override
    public void historicalData(int i, Bar bar) {
        logger.info("HistoricalData. " + i + " - Date: " + bar.time() + ", Open: " + bar.open() + ", High: " +
                bar.high() + ", Low: " + bar.low() + ", Close: " + bar.close() + ", Volume: " + bar.volume() +
                ", Count: " + bar.count() + ", WAP: " + bar.wap());
    }

    @Override
    public void scannerParameters(String xml) {
        logger.info("ScannerParameters. " + xml + "\n");
    }

    @Override
    public void scannerData(int reqId, int rank,
                            ContractDetails contractDetails, String distance, String benchmark,
                            String projection, String legsStr) {
        logger.info("ScannerData. " + reqId + " - Rank: " + rank + ", Symbol: " +
                contractDetails.contract().symbol() + ", SecType: " + contractDetails.contract().secType() +
                ", Currency: " + contractDetails.contract().currency() + ", Distance: " + distance +
                ", Benchmark: " + benchmark + ", Projection: " + projection + ", Legs String: " + legsStr);
    }

    @Override
    public void scannerDataEnd(int reqId) {
        logger.info("ScannerDataEnd. " + reqId);
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high,
                            double low, double close, long volume, double wap, int count) {
        logger.info("RealTimeBars. " + reqId + " - Time: " + time + ", Open: " + open + ", High: " +
                high + ", Low: " + low + ", Close: " + close + ", Volume: " + volume + ", Count: " +
                count + ", WAP: " + wap);
    }

    @Override
    public void currentTime(long time) {
        logger.info("currentTime");
    }

    @Override
    public void fundamentalData(int reqId, String data) {
        logger.info("FundamentalData. ReqId: [" + reqId + "] - Data: [" + data + "]");
    }

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract underComp) {
        logger.info("deltaNeutralValidation");
    }

    @Override
    public void tickSnapshotEnd(int reqId) {
        logger.info("TickSnapshotEnd: " + reqId);
    }

    @Override
    public void marketDataType(int reqId, int marketDataType) {
        logger.info("MarketDataType. [" + reqId + "], Type: [" + marketDataType + "]\n");
    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {
        logger.info(String.format("CommissionReport. [%d] - [%f] [%s] RPNL [%f]",
                commissionReport.m_execId,
                commissionReport.m_commission,
                commissionReport.m_currency,
                commissionReport.m_realizedPNL));
    }

    @Override
    public void position(String account, Contract contract, double pos,
                         double avgCost) {
        logger.info("Position. " + account + " - Symbol: " + contract.symbol() + ", SecType: " +
                contract.secType() + ", Currency: " + contract.currency() + ", Position: " +
                pos + ", Avg cost: " + avgCost);
    }

    @Override
    public void positionEnd() {
        logger.info("PositionEnd \n");
    }

    @Override
    public void accountSummary(int reqId, String account, String tag,
                               String value, String currency) {
        logger.info("Acct Summary. ReqId: " + reqId + ", Acct: " + account + ", Tag: " + tag +
                ", Value: " + value + ", Currency: " + currency);
    }

    @Override
    public void accountSummaryEnd(int reqId) {
        logger.info("AccountSummaryEnd. Req Id: " + reqId + "\n");
    }

    @Override
    public void verifyMessageAPI(String apiData) {
        logger.info("verifyMessageAPI");
    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
        logger.info("verifyCompleted");
    }

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallange) {
        logger.info("verifyAndAuthMessageAPI");
    }

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
        logger.info("verifyAndAuthCompleted");
    }

    @Override
    public void displayGroupList(int reqId, String groups) {
        logger.info("Display Group List. ReqId: " + reqId + ", Groups: " + groups + "\n");
    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
        logger.info("Display Group Updated. ReqId: " + reqId + ", Contract info: " + contractInfo + "\n");
    }

    @Override
    public void error(Exception e) {
        logger.info("Exception in EWrapper: " + e.getMessage());
    }

    @Override
    public void error(String str) {
        logger.info("Error STR");
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        logger.error(String.format("ID: %d, Code: %4d, Message: %s",
                id, errorCode, errorMsg));
    }

    @Override
    public void connectionClosed() {
        logger.info("Connection closed");
    }

    @Override
    public void connectAck() {
        if (clientSocket.isAsyncEConnect()) {
            logger.info("Acknowledging connection");
            clientSocket.startAPI();
        }
    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode,
                              Contract contract, double pos, double avgCost) {
        logger.info("Position Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " +
                modelCode + ", Symbol: " + contract.symbol() + ", SecType: " + contract.secType() + ", Currency: " +
                contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost + "\n");
    }

    @Override
    public void positionMultiEnd(int reqId) {
        logger.info("Position Multi End. Request: " + reqId + "\n");
    }

    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode,
                                   String key, String value, String currency) {
        logger.info("Account Update Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " +
                modelCode + ", Key: " + key + ", Value: " + value + ", Currency: " + currency + "\n");
    }

    @Override
    public void accountUpdateMultiEnd(int reqId) {
        logger.info("Account Update Multi End. Request: " + reqId + "\n");
    }

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange,
                                                    int underlyingConId, String tradingClass, String multiplier,
                                                    Set<String> expirations, Set<Double> strikes) {
        logger.info("Security Definition Optional Parameter. Request: " + reqId + ", Trading Class: " +
                tradingClass + ", Multiplier: " + multiplier + " \n");
    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {
    }

    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
        for (SoftDollarTier tier : tiers) {
            logger.info("tier: " + tier + ", ");
        }
    }

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {

    }

    @Override
    public void symbolSamples(int i, ContractDescription[] contractDescriptions) {

    }

    @Override
    public void historicalDataEnd(int i, String s, String s1) {

    }

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {

    }

    @Override
    public void tickNews(int i, long l, String s, String s1, String s2, String s3) {

    }

    @Override
    public void smartComponents(int i, Map<Integer, Map.Entry<String, Character>> map) {

    }

    @Override
    public void tickReqParams(int i, double v, String s, int i1) {

    }

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {

    }

    @Override
    public void newsArticle(int i, int i1, String s) {

    }

    @Override
    public void historicalNews(int i, String s, String s1, String s2, String s3) {

    }

    @Override
    public void historicalNewsEnd(int i, boolean b) {

    }

    @Override
    public void headTimestamp(int i, String s) {

    }

    @Override
    public void histogramData(int i, List<HistogramEntry> list) {

    }

    @Override
    public void historicalDataUpdate(int i, Bar bar) {

    }

    @Override
    public void rerouteMktDataReq(int i, int i1, String s) {

    }

    @Override
    public void rerouteMktDepthReq(int i, int i1, String s) {

    }

    @Override
    public void marketRule(int i, PriceIncrement[] priceIncrements) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(340);
        logger.info(String.format("Market Rule Id: %d", i));
        for (PriceIncrement pi : priceIncrements) {
            logger.info("Price Increment. Low Edge: " + df.format(pi.lowEdge()) + ", Increment: " + df.format(pi.increment()));
            eventBus.post(MarketDataEvent.priceIncrementEvent(pi.increment()));
        }
    }

    @Override
    public void pnl(int i, double v, double v1, double v2) {

    }

    @Override
    public void pnlSingle(int i, int i1, double v, double v1, double v2, double v3) {

    }

    @Override
    public void historicalTicks(int i, List<HistoricalTick> list, boolean b) {

    }

    @Override
    public void historicalTicksBidAsk(int i, List<HistoricalTickBidAsk> list, boolean b) {

    }

    @Override
    public void historicalTicksLast(int i, List<HistoricalTickLast> list, boolean b) {

    }

    @Override
    public void tickByTickAllLast(int i, int i1, long l, double v, int i2, TickAttr tickAttr, String s, String s1) {

    }

    @Override
    public void tickByTickBidAsk(int i, long l, double v, double v1, int i1, int i2, TickAttr tickAttr) {

    }

    @Override
    public void tickByTickMidPoint(int i, long l, double v) {

    }
}
