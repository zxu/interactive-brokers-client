package org.zhuang.trading;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.zhuang.trading.api.IBActions;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

public class IBClientMain {
    private static final Display display = new Display();

    private IBActions ibActions = new IBActions();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> checkConnection;

    private Group tradeGroup;
    private Label connectionStatusLabel;
    private Button connectionButton;

    private void startWatching() {
        final Runnable checker = new Runnable() {
            public void run() {
                System.out.println(ibActions.isConnected());

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
            GridLayout gridLayout = new GridLayout(2, false);

            tradeGroup = new Group(shell, SWT.NONE);
            tradeGroup.setLayout(gridLayout);
            tradeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            tradeGroup.setText("Trade");

            tradeGroup.setEnabled(false);

            Composite composite = new Composite(tradeGroup, SWT.NONE);
            GridData gridData = new GridData();
            gridData.horizontalSpan = 2;
            gridData.horizontalAlignment = SWT.LEFT;
            composite.setLayoutData(gridData);
            composite.setLayout(createRowLayout());

            Button buyButton = new Button(composite, SWT.RADIO);
            buyButton.setText(" Buy");

            Button sellButton = new Button(composite, SWT.RADIO);
            sellButton.setText(" Sell");

            Composite composite1 = new Composite(tradeGroup, SWT.NONE);
            composite1.setLayout(createRowLayout());

            Label label = new Label(composite1, SWT.NONE);
            label.setText("Trailing amount: ");

            Text text = new Text(composite1, SWT.BORDER);
            text.setLayoutData(new RowData(100, SWT.DEFAULT));

            new Button(composite1, SWT.PUSH).setText("Place Order");
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
}
