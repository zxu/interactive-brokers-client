package org.zhuang.trading;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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
    private static final String TRAILING_STOP_AMOUNT = "TrailingStopAmount";
    private static final String NEXT_ORDER_ID = "NextOrderId";
    private static final String LINKED = "LINKED";

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

    private Map<String, String> data = new ConcurrentHashMap<>();
    private Text textLimitPrice;

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
        if (dataType == MarketDataType.BID_PRICE || dataType == MarketDataType.ASK_PRICE) {
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
        if (!Boolean.parseBoolean(data.get(LINKED))) {
            return;
        }

        MarketDataType dataType = event.type();

        logger.info("===========================");
        logger.info(String.format("%s: %f", dataType, ((Double) event.data()).doubleValue()));
        logger.info("===========================");

        String action = data.containsKey(ACTION) ? data.get(ACTION) : "NONE";
        if (action.equals("NONE") ||
                (action.equals("BUY") && dataType == MarketDataType.ASK_PRICE) ||
                (action.equals("SELL") && dataType == MarketDataType.BID_PRICE)) {
            display.asyncExec(() -> textLimitPrice.setText(String.format("%.6f", (Double) event.data())));
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
            GridLayout gridLayout = new GridLayout(3, false);

            tradeGroup = new Group(shell, SWT.NONE);
            tradeGroup.setLayout(gridLayout);
            tradeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            tradeGroup.setText("Trade");

            tradeGroup.setEnabled(false);

            {
                Composite composite = new Composite(tradeGroup, SWT.NONE);
                GridData gridData = new GridData();
                gridData.horizontalSpan = 3;
                gridData.horizontalAlignment = SWT.LEFT;
                composite.setLayoutData(gridData);
                composite.setLayout(createRowLayout());

                Button buyButton = new Button(composite, SWT.RADIO);
                buyButton.setText(" Buy");
                buyButton.addSelectionListener(widgetSelectedAdapter(e -> {
                    data.put(ACTION, "BUY");
                }));

                Button sellButton = new Button(composite, SWT.RADIO);
                sellButton.setText(" Sell");
                sellButton.addSelectionListener(widgetSelectedAdapter(e -> {
                    data.put(ACTION, "SELL");
                }));
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Symbol: ");

                Text text = new Text(tradeGroup, SWT.BORDER);

                GridData gridData = new GridData(100, SWT.DEFAULT);
                gridData.horizontalSpan = 2;
                gridData.horizontalAlignment = SWT.LEFT;
                text.setLayoutData(gridData);
                text.addModifyListener(getModifyListener(SYMBOL));
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Contract month: ");

                Text text = new Text(tradeGroup, SWT.BORDER);

                GridData gridData = new GridData(100, SWT.DEFAULT);
                gridData.horizontalSpan = 2;
                gridData.horizontalAlignment = SWT.LEFT;
                text.setLayoutData(gridData);
                text.addModifyListener(getModifyListener(MONTH));
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Exchange: ");

                Text text = new Text(tradeGroup, SWT.BORDER);

                GridData gridData = new GridData(100, SWT.DEFAULT);
                gridData.horizontalAlignment = SWT.LEFT;
                text.setLayoutData(gridData);
                text.addModifyListener(getModifyListener(EXCHANGE));
            }

            {
                Button button = new Button(tradeGroup, SWT.PUSH);
                button.setText("Retrieve Price");
                button.setLayoutData(new GridData(120, SWT.DEFAULT));

                button.addSelectionListener(widgetSelectedAdapter(e -> {
                    ibActions.retrieveMarketData(data.get(SYMBOL),
                            data.get(MONTH),
                            data.get(EXCHANGE));
                }));
            }

            {
                Label separator = new Label(tradeGroup, SWT.HORIZONTAL | SWT.SEPARATOR);
                GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                gridData.horizontalSpan = 3;
                separator.setLayoutData(gridData);
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Limit price: ");

                textLimitPrice = new Text(tradeGroup, SWT.BORDER);

                GridData gridData = new GridData(100, SWT.DEFAULT);
                gridData.horizontalAlignment = SWT.LEFT;
                textLimitPrice.setLayoutData(gridData);
                textLimitPrice.addModifyListener(getModifyListener(PRICE));
            }

            {
                Button checkBox = new Button(tradeGroup,SWT.CHECK);
                checkBox.setText("Linked");
                GridData gridData = new GridData();
                gridData.horizontalIndent = 5;
                checkBox.setLayoutData(gridData);
                checkBox.addSelectionListener(widgetSelectedAdapter(e -> {
                    Button button = (Button) e.getSource();

                    data.put(LINKED, Boolean.toString(button.getSelection()));
                }));

                checkBox.setSelection(true);
                data.put(LINKED, Boolean.toString(true));
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Trailing amount: ");

                Text text = new Text(tradeGroup, SWT.BORDER);
                text.setLayoutData(new GridData(100, SWT.DEFAULT));
                text.addModifyListener(getModifyListener(TRAILING_STOP_AMOUNT));
            }

            {
                Button button = new Button(tradeGroup, SWT.PUSH);
                button.setText("Place Order");
                button.setLayoutData(new GridData(120, SWT.DEFAULT));
                button.addSelectionListener(widgetSelectedAdapter(e -> {
                    logger.info(String.format("%s %s @ %s - %s",
                            data.get(SYMBOL),
                            data.get(MONTH),
                            data.get(EXCHANGE),
                            data.get(ACTION)));

                    try {
                        int orderId = Integer.parseInt(data.get(NEXT_ORDER_ID));
                        ibActions.placeFutureOrder(orderId, data.get(SYMBOL),
                                data.get(MONTH),
                                data.get(EXCHANGE),
                                data.get(ACTION),
                                Double.parseDouble(data.get(PRICE)),
                                Double.parseDouble(data.get(TRAILING_STOP_AMOUNT)));

                        data.put(NEXT_ORDER_ID, String.valueOf(orderId + 2));
                    } catch (Exception ex) {

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
}
