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

    private IconWithButtonOverlay.IconWithRemoveButtonOverlay icon;

    private Composite iconContainer;

    private Gesture gesture;

    public CandidateListItem(CandidateScrolledList parent) {
        super(parent, SWT.BACKGROUND);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        parentList = parent;

        RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
        rowLayout.spacing = 5;
        setLayout(rowLayout);

        iconContainer = new Composite(this, SWT.BACKGROUND);

        icon = new IconWithButtonOverlay.IconWithRemoveButtonOverlay(iconContainer, SWT.BACKGROUND | SWT.BORDER);

        RowData rd = new RowData();
        rd.width = 80;
        rd.height = 80;
        iconContainer.setLayoutData(rd);

        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight = 5;
        fillLayout.marginWidth = 5;
        iconContainer.setLayout(fillLayout);

        setSelected(false);
        onSelectionChanged();
    }

    public void setDataSource(Gesture gesture) {
        this.gesture = gesture;

        int width = 70;
        int height = 70;
        int padding = 5;

        Image thumbnail = new Image(getDisplay(), width, height);
        GC gc = new GC(thumbnail);
        MainWindowController.drawSample(gesture, gc, padding, padding, width - padding * 2, height - padding * 2);
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
    }}
