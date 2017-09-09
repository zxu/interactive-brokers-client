package org.zhuang.trading;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.zhuang.trading.api.IBActions;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

public class IBClientMain {
    private static final Display display = new Display();
    private static final String MONTH = "Month";
    private static final String SYMBOL = "Symbol";
    private static final String EXCHANGE = "Exchange";
    private static final String ACTION = "Action";
    private static final String PRICE = "Price";
    private static final String TRAILING_STOP_AMOUNT = "TrailingStopAmount";

    private IBActions ibActions = new IBActions();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> checkConnection;

    private Group tradeGroup;
    private Label connectionStatusLabel;
    private Button connectionButton;

    private HashMap<String, String> data = new HashMap<>();

    private void startWatching() {
        final Runnable checker = new Runnable() {
            public void run() {
                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        boolean connected = ibActions.isConnected();
                        tradeGroup.setEnabled(connected);
                        connectionStatusLabel.setText(connected ? "Connected" : "Disconnected");
                        connectionButton.setText(connected ? "Disconnect" : "Connect");
                    }
                });
            }
        };
        checkConnection = scheduler.scheduleAtFixedRate(checker, 1, 1, SECONDS);
    }

    private void stopWatching() {
        ibActions.disconnect();
        checkConnection.cancel(true);
        scheduler.shutdown();
    }

    public static void main(String[] args) {
        IBClientMain ibClientMain = new IBClientMain();
        ibClientMain.startWatching();
        Shell shell = ibClientMain.open(display);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    public Shell open(Display display) {
        Shell shell = new Shell(display);

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
                gridData.horizontalSpan = 2;
                gridData.horizontalAlignment = SWT.LEFT;
                text.setLayoutData(gridData);
                text.addModifyListener(getModifyListener(EXCHANGE));
            }

            {
                Label label = new Label(tradeGroup, SWT.NONE);
                label.setText("Limit price: ");

                Text text = new Text(tradeGroup, SWT.BORDER);

                GridData gridData = new GridData(100, SWT.DEFAULT);
                gridData.horizontalSpan = 2;
                gridData.horizontalAlignment = SWT.LEFT;
                text.setLayoutData(gridData);
                text.addModifyListener(getModifyListener(PRICE));
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
                button.addSelectionListener(widgetSelectedAdapter(e -> {
                    System.out.println(String.format("%s %s @ %s - %s",
                            data.get(SYMBOL),
                            data.get(MONTH),
                            data.get(EXCHANGE),
                            data.get(ACTION)));

                    ibActions.placeFutureOrder(data.get(SYMBOL),
                            data.get(MONTH),
                            data.get(EXCHANGE),
                            data.get(ACTION),
                            Double.parseDouble(data.get(PRICE)),
                            Double.parseDouble(data.get(TRAILING_STOP_AMOUNT)));
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
