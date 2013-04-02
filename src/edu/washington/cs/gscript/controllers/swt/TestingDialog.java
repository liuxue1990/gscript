package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

public class TestingDialog extends Dialog {

    private MainViewModel mainViewModel;

    private TestGestureCanvas gestureCanvas;

    private Combo combo;

    private Button btnAdd;

    public TestingDialog(Shell parent, MainViewModel viewModel) {
        super(parent);
        this.mainViewModel = viewModel;
    }

    protected void createContents(Shell shell) {
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        shell.setLayout(gridLayout);

        gestureCanvas = new TestGestureCanvas(shell, mainViewModel) {
            @Override
            protected void gesturePerformed() {
                super.gesturePerformed();

                boolean enabled = (getGesture() != null);
                combo.setEnabled(enabled);
                btnAdd.setEnabled(combo.getSelectionIndex() != -1);
            }
        };

        Composite bottomBar = new Composite(shell, SWT.NONE);
        RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
        bottomBar.setLayout(rowLayout);
        combo = new Combo(bottomBar, SWT.NONE);
        int numOfCategories = mainViewModel.getProject().getNumOfCategories();
        for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
            combo.add(mainViewModel.getProject().getCategory(categoryIndex).getNameProperty().getValue());
        }
        combo.setEnabled(false);
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                btnAdd.setEnabled(combo.getSelectionIndex() != -1);
            }
        });

        btnAdd = new Button(bottomBar, SWT.PUSH);
        btnAdd.setText("Add");
        btnAdd.setEnabled(false);

        btnAdd.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int selIndex = combo.getSelectionIndex();
                if (selIndex != -1 && gestureCanvas.getGesture() != null) {
                    mainViewModel.getProject().addSample(
                            mainViewModel.getProject().getCategory(selIndex), gestureCanvas.getGesture());
                    gestureCanvas.reset();
                }
            }
        });

        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.widthHint = 600;
        gd.heightHint = 600;

        gestureCanvas.setLayoutData(gd);

        gd = new GridData();
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalAlignment = SWT.RIGHT;
        bottomBar.setLayoutData(gd);
        shell.pack();
    }

    public Object open () {
        Shell parent = getParent();
        final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.MAX);

        createContents(shell);

        Rectangle rect = parent.getBounds();
        shell.setLocation(
                rect.x + rect.width / 2 - shell.getSize().x / 2,
                rect.y + rect.height / 2 - shell.getSize().y / 2);

        shell.open();

        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return null;
    }

}
