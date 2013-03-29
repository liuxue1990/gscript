package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.swt.NotificationObserverFromUI;
import edu.washington.cs.gscript.helpers.Parser;
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

        private double lineHeight = 1.3;

        private Font font0;

        private Font font1;

        private String content = "rotate(X) repeat(N, Y) draw(S)\n" +
                "        -- Rotate X then repeat N times drawing S\n" +
                "           and rotating Y between repetitions.";

        public StatusArea(Composite parent, int style) {
            super(parent, style);

            font0 = new Font(getDisplay(), "Menlo", 13, SWT.BOLD);
            font1 = new Font(getDisplay(), "Arial", 11, SWT.NORMAL);

            addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    font0.dispose();
                    font1.dispose();
                }
            });

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
            gc.setFont(font0);
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
            int sx = paddingWidth;
            int sy = paddingHeight;
            String[] lines = content.split("\n");
            for (String line : lines) {
                gc.drawString(line, sx, sy);
                sy += (int)(gc.stringExtent(line).y * lineHeight);
                gc.setFont(font1);
            }

            Rectangle r = getBounds();
            if (valid) {
                gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
            } else {
                gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
            }
            gc.fillArc(r.width - 24, r.height / 2 - 8, 16, 16, 0, 360);

            String s = "Status: ";
            Point stringExtent = gc.stringExtent(s);

            gc.drawString(s, r.width - 24 - stringExtent.x, (r.height - stringExtent.y) / 2, true);

        }
    }

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

        TitleBar titleBar = new TitleBar(this, SWT.BACKGROUND);
        titleBar.setTitle("Script");

        valid = true;
        text = new Text(this, SWT.MULTI | SWT.V_SCROLL);
        status = new StatusArea(this, SWT.BACKGROUND);

        final Color statusAreaColor = new Color(getDisplay(), 0xFF, 0xFF, 0xCC);
        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                statusAreaColor.dispose();
            }
        });
        status.setBackground(statusAreaColor);

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
        fd.height = 60;
        status.setLayoutData(fd);

        status.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                GC gc = e.gc;

            }
        });

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
