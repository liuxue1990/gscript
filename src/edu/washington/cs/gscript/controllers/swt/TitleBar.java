package edu.washington.cs.gscript.controllers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class TitleBar extends Canvas {

    private int paddingWidth = 10;

    private int paddingHeight = 2;

    private int minWidth = 0;

    private Font font;

    private String title;

    public TitleBar(Composite parent, int style) {
        super(parent, style);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        font = Resources.FONT_HELVETICA_10_BOLD;

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                font.dispose();
            }
        });

        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                GC gc = e.gc;
                gc.setTextAntialias(SWT.ON);
                renderTitle(gc);
            }
        });
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private void renderTitle(GC gc) {
        if (title == null) {
            return;
        }

        gc.setFont(font);

        Rectangle rect = getClientArea();
        Point titleExtent = gc.stringExtent(title);

        int w = Math.max(titleExtent.x + paddingWidth * 2, minWidth);
        int sx = (rect.width - titleExtent.x) / 2;
        int arcSize = 8;

        gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
        gc.fillRoundRectangle(
                (rect.width - w) / 2, -arcSize,
                w, titleExtent.y + paddingHeight * 2 + arcSize,
                arcSize, arcSize);

        gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        gc.drawString(title, sx, paddingHeight, true);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        GC gc = new GC(this);
        gc.setFont(font);
        Point stringExtent = gc.stringExtent("|");
        gc.dispose();
        return new Point(Math.max(stringExtent.x + paddingWidth * 2, minWidth), stringExtent.y + paddingHeight * 2);
    }
}
