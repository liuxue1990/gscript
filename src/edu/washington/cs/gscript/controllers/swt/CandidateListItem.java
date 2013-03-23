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

    private IconWithButtonOverlay.IconWithYesNoCancelButtonOverlay icon;

    private Composite iconContainer;

    private MainViewModel mainViewModel;

    private SynthesizedGestureSample sample;

    private Gesture stitched;

    private int label;

    private NotificationObserver labelObserver = new NotificationObserver() {
        @Override
        public void onNotified(Object arg) {
            updateLabel();
        }
    };

    public CandidateListItem(CandidateScrolledList parent, MainViewModel viewModel) {
        super(parent, SWT.BACKGROUND);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                NotificationCenter.getDefaultCenter().removeObserver(labelObserver);
            }
        });

        mainViewModel = viewModel;

        parentList = parent;

        RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
        rowLayout.spacing = 4;
        setLayout(rowLayout);

        iconContainer = new Composite(this, SWT.BACKGROUND);

        icon = new IconWithButtonOverlay.IconWithYesNoCancelButtonOverlay(iconContainer, SWT.BACKGROUND) {
            @Override
            protected void buttonClicked(int index) {
                if (index == 0) {
                    mainViewModel.setLabelOfSynthesizedSample(sample, 1);
                } else if (index == 1) {
                    mainViewModel.setLabelOfSynthesizedSample(sample, -1);
                } else {
                    mainViewModel.setLabelOfSynthesizedSample(sample, 0);
                }
            }

            @Override
            protected void mouseDownOnIcon() {
                if (label != 0) {
                    mainViewModel.setLabelOfSynthesizedSample(sample, 0);
                } else {
                    mainViewModel.setLabelOfSynthesizedSample(sample, -1);
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

    public void setDataSource(SynthesizedGestureSample sample) {
        NotificationCenter.getDefaultCenter().removeObserver(labelObserver);

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

        updateLabel();

        NotificationCenter.getDefaultCenter().addObserver(
                labelObserver, NotificationCenter.VALUE_CHANGED_NOTIFICATION, sample.getUserLabelProperty());
    }

    private void updateLabel() {
        this.label = sample.getUserLabelProperty().getValue();

        if (label < 0) {
            iconContainer.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
        } else if (label > 0) {
            iconContainer.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
        } else {
            iconContainer.setBackground(getBackground());
        }
    }
}
