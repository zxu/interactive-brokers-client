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

    private void startWatching() {
        final Runnable checker = new Runnable() {
            public void run() {
                System.out.println(ibActions.isConnected());

                display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        boolean connected = ibActions.isConnected();
                        tradeGroup.setEnabled(connected);
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

        GridLayout connectionGroupGridLayout = new GridLayout(1, false);
        connectionGroupGridLayout.marginWidth = 0;
        connectionGroupGridLayout.marginHeight = 0;
        connectionGroupGridLayout.verticalSpacing = 0;
        connectionGroupGridLayout.horizontalSpacing = 0;

        Group connectionGroup = new Group(shell, SWT.NONE);
        connectionGroup.setLayout(connectionGroupGridLayout);
        connectionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        connectionGroup.setText("Connection");

        Composite compositeConnection = new Composite(connectionGroup, SWT.NONE);
        compositeConnection.setLayout(createRowLayout());

        Button connectionButton = new Button(compositeConnection, SWT.PUSH);
        connectionButton.setText("Connect");

        connectionButton.addSelectionListener(widgetSelectedAdapter(e -> {
            ibActions.connect();
        }));

        GridLayout tradeGroupGridLayout = new GridLayout(2, false);
        tradeGroupGridLayout.marginWidth = 0;
        tradeGroupGridLayout.marginHeight = 0;
        tradeGroupGridLayout.verticalSpacing = 0;
        tradeGroupGridLayout.horizontalSpacing = 0;

        tradeGroup = new Group(shell, SWT.NONE);
        tradeGroup.setLayout(tradeGroupGridLayout);
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
