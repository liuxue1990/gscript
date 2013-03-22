package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.models.Gesture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

public class CandidateListItem extends ScrolledList.ListItem {
    private CandidateScrolledList parentList;

    private IconWithButtonOverlay.IconWithYesNoCancelButtonOverlay icon;

    private Composite iconContainer;

    private Gesture gesture;

    private int label;

    public CandidateListItem(CandidateScrolledList parent) {
        super(parent, SWT.BACKGROUND);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        parentList = parent;

        RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
        rowLayout.spacing = 4;
        setLayout(rowLayout);

        iconContainer = new Composite(this, SWT.BACKGROUND);
        setLabel(0);

        icon = new IconWithButtonOverlay.IconWithYesNoCancelButtonOverlay(iconContainer, SWT.BACKGROUND) {
            @Override
            protected void buttonClicked(int index) {
                if (index == 0) {
                    setLabel(1);
                } else if (index == 1) {
                    setLabel(-1);
                } else {
                    setLabel(0);
                }
            }

            @Override
            protected void mouseDownOnIcon() {
                if (label != 0) {
                    setLabel(0);
                } else {
                    setLabel(-1);
                }
            }
        };

        RowData rd = new RowData();
        rd.width = 60;
        rd.height = 60;
        iconContainer.setLayoutData(rd);

        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight = 2;
        fillLayout.marginWidth = 2;
        iconContainer.setLayout(fillLayout);

        setSelected(false);
        onSelectionChanged();
    }

    public void setDataSource(Gesture gesture) {
        this.gesture = gesture;

        int width = 56;
        int height = 56;
        int padding = 2;

        Image thumbnail = new Image(getDisplay(), width, height);
        GC gc = new GC(thumbnail);
        MainWindowController.drawSample(
                gesture, gc, padding, padding, width - padding * 2, height - padding * 2,
                getDisplay().getSystemColor(SWT.COLOR_BLACK),
                getDisplay().getSystemColor(SWT.COLOR_WHITE));
        gc.dispose();

        icon.setThumbnail(thumbnail);
    }

    public Gesture getDataSource() {
        return gesture;
    }

    @Override
    protected void onSelectionChanged() {
        iconContainer.setBackground(
                getDisplay().getSystemColor(isSelected() ? SWT.COLOR_LIST_SELECTION : SWT.COLOR_WHITE));
    }

    private void setLabel(int label) {
        this.label = label;

        if (label < 0) {
            iconContainer.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
        } else if (label > 0) {
            iconContainer.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
        } else {
            iconContainer.setBackground(getBackground());
        }
    }
}
