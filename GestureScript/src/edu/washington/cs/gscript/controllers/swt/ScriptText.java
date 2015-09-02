package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.swt.NotificationObserverFromUI;
import edu.washington.cs.gscript.helpers.ParserHelper;
import edu.washington.cs.gscript.models.Category;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ScriptText extends Composite {

    private class StatusArea extends Canvas {

        private int paddingWidth = 30;

        private int paddingHeight = 8;

        private Font font0;

        private Font font1;

        private String content = "Syntax: [rotate(X)] [repeat(N, Y)] draw(S)";

        public StatusArea(Composite parent, int style) {
            super(parent, style);

            font0 = Resources.FONT_COURIER_NEW_11_BOLD;
            font1 = Resources.FONT_ARIAL_11_NORMAL;

            addPaintListener(new PaintListener() {
                @Override
                public void paintControl(PaintEvent e) {
                    GC gc = e.gc;
                    gc.setTextAntialias(SWT.ON);
                    renderContent(gc);
                }
            });
        }

        private void renderContent(GC gc) {
            Rectangle r = getBounds();

            gc.setFont(font0);
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
            int sx = paddingWidth;
            int sy = paddingHeight;
            Point stringExtent = gc.stringExtent(content);
            gc.drawString(content, (r.width - stringExtent.x) / 2, sy);

//            String s = "Status: ";
//            stringExtent = gc.stringExtent(s);
//
//            gc.drawString(s, r.width - 24 - stringExtent.x, (r.height - stringExtent.y) / 2, true);

        }
    }

    private MainViewModel mainViewModel;

    private Category category;

    private Text text;

    private Canvas status;

    private TitleBar titleBar;

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

        titleBar = new TitleBar(this, SWT.BACKGROUND);
        final String title = "Script    ";
        titleBar.setTitle(title);

        titleBar.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                GC gc = e.gc;

                Point titleExtent = gc.stringExtent(title);
                Rectangle r = titleBar.getBounds();
                r.x = (r.width - titleExtent.x) / 2;
                r.width = titleExtent.x;

                if (valid) {
                    gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
                } else {
                    gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
                }
                gc.fillArc(r.x + r.width - 2, (r.height - 8) / 2, 8, 8, 0, 360);
            }
        });

        valid = true;
        text = new Text(this, SWT.MULTI | SWT.V_SCROLL);
        status = new StatusArea(this, SWT.BACKGROUND);

        status.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        titleBar.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(titleBar);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(status);
        text.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(100);
        fd.height = 24;
        status.setLayoutData(fd);

        text.addModifyListener(textModifyListener);

        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserverFromUI(this) {
                    @Override
                    public void onUINotified(Object arg) {
                        reloadData();
                    }
                },
                MainViewModel.CATEGORY_SELECTED_NOTIFICATION, mainViewModel);

    }

    private void updateValidity() {
        valid = true;
        if (ParserHelper.parseScript(text.getText()) == null) {
            valid = false;
        }
        titleBar.redraw();
    }

    private void reloadData() {
        category = mainViewModel.getSelectedCategory();

        if (category != null) {
            text.removeModifyListener(textModifyListener);
            text.setText(category.getScriptTextProperty().getValue());
            text.addModifyListener(textModifyListener);
        } else {
            text.removeModifyListener(textModifyListener);
            text.setText("");
            text.addModifyListener(textModifyListener);
        }

        updateValidity();
    }

    private void onChanged() {
        mainViewModel.setScript(category, text.getText());
        updateValidity();
    }
}
