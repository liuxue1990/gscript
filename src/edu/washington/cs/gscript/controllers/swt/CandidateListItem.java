package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.helpers.SampleGenerator;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.SynthesizedGestureSample;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

public class CandidateListItem extends ScrolledList.ListItem {
    private CandidateScrolledList parentList;

    private IconWithButtonOverlay.IconWithEmptyButtonOverlay icon;

    private Composite iconContainer;

    private MainViewModel mainViewModel;

    private SynthesizedGestureSample sample;

    private Gesture stitched;

    public CandidateListItem(CandidateScrolledList parent, MainViewModel viewModel) {
        super(parent, SWT.BACKGROUND);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        mainViewModel = viewModel;

        parentList = parent;

        RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
        rowLayout.spacing = 4;
        setLayout(rowLayout);

        iconContainer = new Composite(this, SWT.BACKGROUND);

        icon = new IconWithButtonOverlay.IconWithEmptyButtonOverlay(iconContainer, SWT.BACKGROUND) {
            @Override
            protected void mouseDownOnIcon() {
                mainViewModel.toggleSelectionOnSynthesizedSample(sample);
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

    public SynthesizedGestureSample getDataSource() {
        return sample;
    }

    public void setDataSource(SynthesizedGestureSample sample) {
        this.sample = sample;
        this.stitched = SampleGenerator.stitch(sample);

        int width = 56;
        int height = 56;
        int padding = 2;

        Image thumbnail = new Image(getDisplay(), width, height);
        GC gc = new GC(thumbnail);
        MainWindowController.drawSample(
                stitched, gc, padding, padding, width - padding * 2, height - padding * 2,
                getDisplay().getSystemColor(SWT.COLOR_BLACK),
                getDisplay().getSystemColor(SWT.COLOR_WHITE));
        gc.dispose();

        icon.setThumbnail(thumbnail);
    }

    @Override
    protected void onSelectionChanged() {
        iconContainer.setBackground(
                getDisplay().getSystemColor(isSelected() ? SWT.COLOR_LIST_SELECTION : SWT.COLOR_WHITE));
    }
}
