package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.framework.Property;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.Timer;
import java.util.TimerTask;

public class ProgressDialog extends Dialog {

    private Property<Integer> progress;

    private ProgressBar progressBar;

    private Timer checkProgressTimer;

    private String prompt;

    public ProgressDialog(Shell parent, Property<Integer> progress) {
        super(parent, SWT.APPLICATION_MODAL);
        this.progress = progress;
        prompt = "Working...";
    }

    public Object open () {
        Shell parent = getParent();
        final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText(getText());

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                if (progress.getValue() < 100) {
                    e.doit = false;
                }
            }
        });

        GridLayout gridLayout = new GridLayout(1, true);
        shell.setLayout(gridLayout);

        Label label = new Label(shell, SWT.NONE);
        label.setText(prompt);

        progressBar = new ProgressBar(shell, SWT.BORDER);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setSelection(0);

        GridData gd = new GridData();
        gd.widthHint = 300;
        progressBar.setLayoutData(gd);
        shell.pack();

        Rectangle rect = parent.getBounds();
        shell.setLocation(
                rect.x + rect.width / 2 - shell.getSize().x / 2,
                rect.y + rect.height / 2 - shell.getSize().y / 2);

        shell.open();

        checkProgressTimer = new Timer();
        checkProgressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ProgressDialog.this.getParent().getDisplay().asyncExec(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (shell.isDisposed()) {
                                    return;
                                }
                                progressBar.setSelection(progress.getValue());
                                if (progress.getValue() == 100) {
                                    checkProgressTimer.cancel();
                                    shell.close();
                                }
                            }
                        }
                );
            }
        }, 0, 100);

        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return null;
    }

    public void setPrompt(String text) {
        prompt = text;
    }

}
