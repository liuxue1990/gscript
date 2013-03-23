package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.helpers.Parser;
import edu.washington.cs.gscript.models.Category;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ScriptText extends Composite {

    private MainViewModel mainViewModel;

    private Category category;

    private Text text;

    private Canvas status;

    private boolean valid;

    private ModifyListener textModifyListener = new ModifyListener() {
        @Override
        public void modifyText(ModifyEvent e) {
            onChanged();
        }
    };

    public ScriptText(Composite parent, int style, MainViewModel viewModel) {
        super(parent, style);
        FormLayout formLayout = new FormLayout();
        setLayout(formLayout);

        this.mainViewModel = viewModel;

        valid = true;
        text = new Text(this, SWT.MULTI | SWT.V_SCROLL);
        status = new Canvas(this, SWT.BACKGROUND);
        status.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(status);
        text.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(100);
        fd.height = 20;
        status.setLayoutData(fd);

        status.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                GC gc = e.gc;

                Rectangle r = status.getBounds();
                if (valid) {
                    gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
                } else {
                    gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
                }
                gc.fillArc(r.width - 24, r.height / 2 - 8, 16, 16, 0, 360);
            }
        });

        text.addModifyListener(textModifyListener);

        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserver() {
                    @Override
                    public void onNotified(Object arg) {
                        reloadData();
                    }
                },
                MainViewModel.CATEGORY_SELECTED_NOTIFICATION, mainViewModel);

    }

    private void updateValidity() {
        valid = true;
        if (Parser.parseScript(text.getText()) == null) {
            valid = false;
        }
        status.redraw();
    }

    private void reloadData() {
        category = mainViewModel.getSelectedCategory();

        if (category != null) {
            text.removeModifyListener(textModifyListener);
            text.setText(category.getScriptTextProperty().getValue());
            text.addModifyListener(textModifyListener);
        }

        updateValidity();
    }

    private void onChanged() {
        mainViewModel.setScript(category, text.getText());
        updateValidity();
    }
}
