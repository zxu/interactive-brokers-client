package org.zhuang.trading;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.zhuang.trading.api.IBActions;
import org.zhuang.trading.api.MarketDataEvent;
import org.zhuang.trading.api.MarketDataType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

@Component
public class IBClientMain {
    private final static Logger logger = LoggerFactory.getLogger(IBClientMain.class);

    private static final Display display = new Display();

    private static final String SYMBOL = "Symbol";
    private static final String MONTH = "Month";
    private static final String EXCHANGE = "Exchange";
    private static final String ACTION = "Action";
    private static final String PRICE = "Price";
    private static final String BID_PRICE = "Bid_Price";
    private static final String ASK_PRICE = "Ask_Price";
    private static final String TRAILING_STOP_AMOUNT = "TrailingStopAmount";
    private static final String NEXT_ORDER_ID = "NextOrderId";
    private static final String LINKED = "LINKED";
    private static final String MID = "MID";

    @Autowired
    private IBActions ibActions;

    @Autowired
    private EventBus marketDataEventBus;

    @Autowired
    private ExecutorService executor;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> checkConnection;

    private Group tradeGroup;
    private Label connectionStatusLabel;
    private Button connectionButton;
    private Text textLimitPrice;
    private Label labelPriceBid;
    private Label labelPriceAsk;

    private Map<String, String> data = new ConcurrentHashMap<>();
    private static Map<String, String> defaultValues = new ConcurrentHashMap<>();

    static {
        defaultValues.put(SYMBOL, "NQ");
        defaultValues.put(MONTH, new SimpleDateFormat("yyyyMM").format(Calendar.getInstance().getTime()));
        defaultValues.put(EXCHANGE, "GLOBEX");
    }

    private void startWatching() {
        final Runnable checker = () -> display.asyncExec(() -> {
            boolean connected = ibActions.isConnected();
            tradeGroup.setEnabled(connected);
            connectionStatusLabel.setText(connected ? "Connected" : "Disconnected");
            connectionButton.setText(connected ? "Disconnect" : "Connect");
        });
        checkConnection = scheduler.scheduleAtFixedRate(checker, 1, 1, SECONDS);
    }

    private void stopWatching() {
        ibActions.disconnect();
        checkConnection.cancel(true);
        scheduler.shutdown();
        executor.shutdown();
    }

    @Subscribe
    public void marketEventHandler(MarketDataEvent event) {
        MarketDataType dataType = event.type();
        if (dataType == MarketDataType.BID_PRICE
                || dataType == MarketDataType.ASK_PRICE
                || dataType == MarketDataType.UPDATE_UI_PRICE) {
            updateTickPrice(event);
        }
        if (dataType == MarketDataType.NEXT_ORDER_ID) {
            updateNextOrderId(event);
        }
    }

    private void updateNextOrderId(MarketDataEvent event) {
        MarketDataType dataType = event.type();

        logger.info("===========================");
        logger.info(String.format("%s: %d", dataType, (Integer) event.data()));
        logger.info("===========================");

        data.put(NEXT_ORDER_ID, event.data().toString());
    }

    private void updateTickPrice(final MarketDataEvent event) {
        MarketDataType dataType = event.type();

        logger.info("===========================");
        logger.info(String.format("%s: %f", dataType, (Double) event.data()));
        logger.info("===========================");

        if (dataType == MarketDataType.BID_PRICE) {
            data.put(BID_PRICE, event.data().toString());
            display.asyncExec(() -> {
                labelPriceBid.setText(String.format("Bid: %.4f", (Double) event.data()));
                tradeGroup.layout();
            });
        }

        if (dataType == MarketDataType.ASK_PRICE) {
            data.put(ASK_PRICE, event.data().toString());
            display.asyncExec(() -> {
                labelPriceAsk.setText(String.format("Ask: %.4f", (Double) event.data()));
                tradeGroup.layout();
            });
        }

        if (!Boolean.parseBoolean(data.get(LINKED))) {
            return;
        }

        String action = data.containsKey(ACTION) ? data.get(ACTION) : "NONE";

        if (action.equals("NONE")) {
            return;
        }

        if (Boolean.parseBoolean(data.get(MID))) {
            if (!(data.containsKey(BID_PRICE) && data.containsKey(ASK_PRICE))) {
                return;
            }
            double bidPrice = Double.parseDouble(data.get(BID_PRICE));
            double askPrice = Double.parseDouble(data.get(ASK_PRICE));
            double midPrice = (bidPrice + askPrice) / 2;

            display.asyncExec(() -> {
                textLimitPrice.setText(String.format("%.4f", midPrice));
                tradeGroup.layout();
            });
        } else {
            if (action.equals("BUY")) {
                if (!data.containsKey(ASK_PRICE)) {
                    return;
                }
                display.asyncExec(() -> {
                    textLimitPrice.setText(String.format("%.4f", Double.parseDouble(data.get(ASK_PRICE))));
                    tradeGroup.layout();
                });
            } else {
                if (!data.containsKey(BID_PRICE)) {
                    return;
                }
                display.asyncExec(() -> {
                    textLimitPrice.setText(String.format("%.4f", Double.parseDouble(data.get(BID_PRICE))));
                    tradeGroup.layout();
                });
            }
        }
    }

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(IBClientConfig.class);

        IBClientMain ibClientMain = context.getBean(IBClientMain.class);

        ibClientMain.startWatching();
        Shell shell = ibClientMain.open(display);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    public Shell open(Display display) {
        marketDataEventBus.register(this);

        Shell shell = new Shell(display, SWT.CLOSE | SWT.MIN | SWT.TITLE);

        shell.setLayout(new GridLayout());

        /**
         * Set up the "connection" region of the UI
         */
        {
            GridLayout gridLayout = new GridLayout(2, false);

            Group group = new Group(shell, SWT.NONE);
            group.setLayout(gridLayout);
            group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            group.setText("Connection");

            connectionButton = new Button(group, SWT.PUSH);
            connectionButton.setText("Connect");
            connectionButton.setLayoutData(new GridData(120, SWT.DEFAULT));

            connectionStatusLabel = new Label(group, SWT.NONE);
            connectionStatusLabel.setText("Disconnected");

            connectionButton.addSelectionListener(widgetSelectedAdapter(e -> {
                if (ibActions.isConnected()) {
                    ibActions.disconnect();
                } else {
                    ibActions.connect();
                }
            }));
        }

        /**
         * Set up the "trade" region of the UI
         */
        {
            GridLayout gridLayout = new GridLayout(4, false);

            tradeGroup = new Group(shell, SWT.NONE);
            tradeGroup.setLayout(gridLayout);
            tradeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            tradeGroup.setText("Trade");

            tradeGroup.setEnabled(false);

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Symbol: ");

                Text text = new Text(tradeGroup, SWT.BORDER);

                GridData gridData = new GridData(100, SWT.DEFAULT);
                gridData.horizontalSpan = 3;
                gridData.horizontalAlignment = SWT.LEFT;
                text.setLayoutData(gridData);
                addFocusListeners(text, SYMBOL);
                text.addModifyListener(getModifyListener(SYMBOL));
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Contract month: ");

                Text text = new Text(tradeGroup, SWT.BORDER);

                GridData gridData = new GridData(100, SWT.DEFAULT);
                gridData.horizontalSpan = 3;
                gridData.horizontalAlignment = SWT.LEFT;
                text.setLayoutData(gridData);
                addFocusListeners(text, MONTH);
                text.addModifyListener(getModifyListener(MONTH));
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Exchange: ");

                Text text = new Text(tradeGroup, SWT.BORDER);

                GridData gridData = new GridData(100, SWT.DEFAULT);
                gridData.horizontalAlignment = SWT.LEFT;
                text.setLayoutData(gridData);
                addFocusListeners(text, EXCHANGE);
                text.addModifyListener(getModifyListener(EXCHANGE));
            }

            {
                Button button = new Button(tradeGroup, SWT.PUSH);
                button.setText("Retrieve Price");

                GridData gridData = new GridData(160, SWT.DEFAULT);
                gridData.horizontalSpan = 2;
                gridData.horizontalAlignment = SWT.LEFT;
                button.setLayoutData(gridData);

                button.addSelectionListener(widgetSelectedAdapter(e -> {
                    ibActions.retrieveMarketData(data.get(SYMBOL),
                            data.get(MONTH),
                            data.get(EXCHANGE));
                }));
            }

            {
                Label separator = new Label(tradeGroup, SWT.HORIZONTAL | SWT.SEPARATOR);
                GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 4;
                separator.setLayoutData(gridData);
            }

            {
                Composite composite = new Composite(tradeGroup, SWT.NONE);
                GridData gridData = new GridData();
                gridData.horizontalSpan = 4;
                gridData.horizontalAlignment = SWT.LEFT;
                composite.setLayoutData(gridData);
                composite.setLayout(createRowLayout());

                Button buyButton = new Button(composite, SWT.RADIO);
                buyButton.setText(" Buy");
                buyButton.addSelectionListener(widgetSelectedAdapter(e -> {
                    data.put(ACTION, "BUY");
                    if (data.containsKey(ASK_PRICE)) {
                        marketDataEventBus.post(MarketDataEvent.updateUIPriceEvent(0));
                    }
                }));

                Button sellButton = new Button(composite, SWT.RADIO);
                sellButton.setText(" Sell");
                sellButton.addSelectionListener(widgetSelectedAdapter(e -> {
                    data.put(ACTION, "SELL");
                    if (data.containsKey(BID_PRICE)) {
                        marketDataEventBus.post(MarketDataEvent.updateUIPriceEvent(0));
                    }
                }));

                {
                    Label separator = new Label(composite, SWT.SPACE);
                    RowData rowData = new RowData(40, 20);
                    separator.setLayoutData(rowData);

                    FormLayout gridLayoutPrices = new FormLayout();
                    Composite compositePrices = new Composite(composite, SWT.NONE);
                    compositePrices.setLayout(gridLayoutPrices);

                    FormData formDataBid = new FormData();
                    formDataBid.left = new FormAttachment(0, 0);
                    formDataBid.top = new FormAttachment(0, 0);

                    labelPriceBid = new Label(compositePrices, SWT.NONE);
                    labelPriceBid.setText("");
                    labelPriceBid.setLayoutData(formDataBid);

                    FormData formDataAsk = new FormData();
                    formDataAsk.left = new FormAttachment(55, 0);

                    labelPriceAsk = new Label(compositePrices, SWT.NONE);
                    labelPriceAsk.setText("");
                    labelPriceAsk.setLayoutData(formDataAsk);
                }
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Limit price: ");

                textLimitPrice = new Text(tradeGroup, SWT.BORDER);

                GridData gridData = new GridData(100, SWT.DEFAULT);
                gridData.horizontalAlignment = SWT.LEFT;
                textLimitPrice.setLayoutData(gridData);
                addFocusListeners(textLimitPrice, PRICE);
                textLimitPrice.addModifyListener(getModifyListener(PRICE));
            }

            {
                Composite composite = new Composite(tradeGroup, SWT.NONE);
                GridData gridData = new GridData();
                gridData.horizontalSpan = 2;
                gridData.horizontalAlignment = SWT.LEFT;
                composite.setLayoutData(gridData);
                composite.setLayout(createRowLayout());
                {

                    Button checkBox = new Button(composite, SWT.CHECK);
                    checkBox.setText("Linked");
                    checkBox.addSelectionListener(widgetSelectedAdapter(e -> {
                        Button button = (Button) e.getSource();

                        data.put(LINKED, Boolean.toString(button.getSelection()));
                    }));

                    checkBox.setSelection(true);
                    data.put(LINKED, Boolean.toString(true));
                }

                {
                    Button checkBox = new Button(composite, SWT.CHECK);
                    checkBox.setText("Mid-point");
                    checkBox.addSelectionListener(widgetSelectedAdapter(e -> {
                        Button button = (Button) e.getSource();

                        data.put(MID, Boolean.toString(button.getSelection()));

                        marketDataEventBus.post(MarketDataEvent.updateUIPriceEvent(0));
                    }));

                    checkBox.setSelection(false);
                    data.put(MID, Boolean.toString(false));
                }
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Trailing amount: ");

                Text text = new Text(tradeGroup, SWT.BORDER);
                text.setLayoutData(new GridData(100, SWT.DEFAULT));
                addFocusListeners(text, TRAILING_STOP_AMOUNT);
                text.addModifyListener(getModifyListener(TRAILING_STOP_AMOUNT));
            }

            {
                Button button = new Button(tradeGroup, SWT.PUSH);
                button.setText("Place Order");
                button.setLayoutData(new GridData(160, SWT.DEFAULT));
                button.addSelectionListener(widgetSelectedAdapter(e -> {
                    logger.info(String.format("%s %s @ %s - %s",
                            data.get(SYMBOL),
                            data.get(MONTH),
                            data.get(EXCHANGE),
                            data.get(ACTION)));

                    try {
                        int orderId = Integer.parseInt(data.get(NEXT_ORDER_ID));

                        double price = NumberUtils.isCreatable(data.get(PRICE)) ?
                                NumberUtils.createDouble(data.get(PRICE)) : -1;
                        double trailingStopAmount = NumberUtils.isCreatable(data.get(TRAILING_STOP_AMOUNT)) ?
                                NumberUtils.createDouble(data.get(TRAILING_STOP_AMOUNT)) : -1;

                        ibActions.placeFutureOrder(orderId, data.get(SYMBOL),
                                data.get(MONTH),
                                data.get(EXCHANGE),
                                data.get(ACTION),
                                price,
                                trailingStopAmount);

                    } catch (Exception ignored) {

                    }
                }));
            }
        }

        shell.pack();
        shell.open();

        shell.addListener(SWT.Close, event -> {
            stopWatching();
        });

        return shell;
    }

    private RowLayout createRowLayout() {
        RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = true;
        rowLayout.center = true;
        rowLayout.pack = true;
        rowLayout.justify = true;
        rowLayout.type = SWT.HORIZONTAL;
        rowLayout.marginLeft = 5;
        rowLayout.marginTop = 5;
        rowLayout.marginRight = 5;
        rowLayout.marginBottom = 5;
        rowLayout.spacing = 5;
        return rowLayout;
    }

    private ModifyListener getModifyListener(String label) {
        return new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                String text = ((Text) modifyEvent.widget).getText();
                data.put(label, text);
            }
        };
    }

    // See https://stackoverflow.com/a/10048884
    private void addFocusListeners(Widget widget, String field) {
        if (!(widget instanceof Text)) {
            return;
        }

        Text text = (Text) widget;

        Listener listener = getFocusListener(field);

        text.addListener(SWT.FocusIn, listener);
        text.addListener(SWT.FocusOut, listener);
        text.addListener(SWT.MouseDown, listener);
        text.addListener(SWT.MouseUp, listener);
    }

    private Listener getFocusListener(String field) {
        return new Listener() {
            private boolean hasFocus = false;
            private boolean hadFocusOnMousedown = false;

            @Override
            public void handleEvent(Event e) {
                switch (e.type) {
                    case SWT.FocusIn: {
                        Text t = (Text) e.widget;

                        if (StringUtils.isAllEmpty(t.getText()) &&
                                defaultValues.get(field) != null) {
                            t.setText(defaultValues.get(field));
                        }

                        // Covers the case where the user focuses by keyboard.
                        t.selectAll();

                        // The case where the user focuses by mouse click is special because Eclipse,
                        // for some reason, fires SWT.FocusIn before SWT.MouseDown, and on mouse down
                        // it cancels the selection. So we set a variable to keep track of whether the
                        // control is focused (can't rely on isFocusControl() because sometimes it's wrong),
                        // and we make it asynchronous so it will get set AFTER SWT.MouseDown is fired.
                        t.getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                hasFocus = true;
                            }
                        });

                        break;
                    }
                    case SWT.FocusOut: {
                        hasFocus = false;
                        ((Text) e.widget).clearSelection();

                        break;
                    }
                    case SWT.MouseDown: {
                        // Set the variable which is used in SWT.MouseUp.
                        hadFocusOnMousedown = hasFocus;

                        break;
                    }
                    case SWT.MouseUp: {
                        Text t = (Text) e.widget;
                        if (t.getSelectionCount() == 0 && !hadFocusOnMousedown) {
                            ((Text) e.widget).selectAll();
                        }

                        break;
                    }
                }
            }
        };
    }
}
