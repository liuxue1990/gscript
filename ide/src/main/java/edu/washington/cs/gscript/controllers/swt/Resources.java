package edu.washington.cs.gscript.controllers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class Resources {

    public static Font FONT_COURIER_NEW_11_BOLD;

    public static Font FONT_COURIER_NEW_10_BOLD;

    public static Font FONT_ARIAL_12_BOLD;

    public static Font FONT_ARIAL_12_NORMAL;

    public static Font FONT_ARIAL_11_NORMAL;

    public static Font FONT_HELVETICA_10_BOLD;

    public static void initResources(Display display) {
        FONT_COURIER_NEW_11_BOLD = new Font(display, "Courier New", 11, SWT.BOLD);
        FONT_COURIER_NEW_10_BOLD = new Font(display, "Courier New", 10, SWT.BOLD);
        FONT_ARIAL_12_BOLD = new Font(display, "Arial", 12, SWT.BOLD);
        FONT_ARIAL_12_NORMAL = new Font(display, "Arial", 12, SWT.NORMAL);
        FONT_ARIAL_11_NORMAL = new Font(display, "Arial", 11, SWT.NORMAL);
        FONT_HELVETICA_10_BOLD = new Font(display, "Helvetica", 10, SWT.BOLD);
    }

    public static void disposeResources() {
        FONT_COURIER_NEW_11_BOLD.dispose();
        FONT_COURIER_NEW_10_BOLD.dispose();
        FONT_ARIAL_12_BOLD.dispose();
        FONT_ARIAL_12_NORMAL.dispose();
        FONT_ARIAL_11_NORMAL.dispose();
        FONT_HELVETICA_10_BOLD.dispose();
    }
}
