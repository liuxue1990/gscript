package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TestingDialog extends Dialog {

    private MainViewModel mainViewModel;

    private TestGestureCanvas gestureCanvas;

    public TestingDialog(Shell parent, MainViewModel viewModel) {
        super(parent);
        this.mainViewModel = viewModel;
    }

    protected void createContents(Shell shell) {
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        shell.setLayout(gridLayout);

        gestureCanvas = new TestGestureCanvas(shell, mainViewModel);

        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.widthHint = 600;
        gd.heightHint = 600;

        gestureCanvas.setLayoutData(gd);
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
