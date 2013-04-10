package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;

import edu.washington.cs.gscript.models.Gesture;

public class SampleListItem extends ScrolledList.ListItem {

	private MainViewModel mainViewModel;

    private SampleScrolledList parentList;

	private IconWithButtonOverlay.IconWithRemoveButtonOverlay icon;

	private Composite iconContainer;

	private Gesture gesture;

    private Composite classTag;

	public SampleListItem(SampleScrolledList parent, MainViewModel viewModel) {
		super(parent, SWT.BACKGROUND);

        this.parentList = parent;
        this.mainViewModel = viewModel;

		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        FormLayout formLayout = new FormLayout();
		setLayout(formLayout);

		iconContainer = new Composite(this, SWT.BACKGROUND);

		icon = new IconWithButtonOverlay.IconWithRemoveButtonOverlay(iconContainer, SWT.BACKGROUND | SWT.BORDER) {
            @Override
            protected void mouseDownOnIcon() {
                mainViewModel.selectSample(gesture);
            }

            @Override
            protected void buttonClicked(int index) {
                mainViewModel.removeSample(parentList.getDataSource(), gesture);
            }
        };

        classTag = new Composite(this, SWT.BACKGROUND);
        classTag.setBackground(getDisplay().getSystemColor(SWT.COLOR_DARK_RED));

        classTag.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                if (mainViewModel.getRecognizedClass(gesture) != null) {
                    GC gc = e.gc;
                    gc.setTextAntialias(SWT.ON);
                    gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
                    gc.setFont(Resources.FONT_COURIER_NEW_10_BOLD);

                    String text = mainViewModel.getRecognizedClass(gesture).getNameProperty().getValue();
                    Point stringExtent = gc.stringExtent(text);
                    Rectangle rect = classTag.getClientArea();
                    gc.fillRectangle(rect);

                    gc.drawString(text, Math.max(0, (rect.width - stringExtent.x) / 2), (rect.height - stringExtent.y) / 2, true);
                }
            }
        });

        FormData fd = new FormData(80, 80);
		iconContainer.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(10);
        fd.right = new FormAttachment(90);
        fd.bottom = new FormAttachment(100);
        fd.height = 12;
        classTag.setLayoutData(fd);

        classTag.moveAbove(iconContainer);

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
		MainWindowController.drawSample(
                gesture, gc, padding, padding, width - padding * 2, height - padding * 2,
                getDisplay().getSystemColor(SWT.COLOR_BLUE),
                getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.dispose();

		icon.setThumbnail(thumbnail);

        if (mainViewModel.getRecognizedClass(gesture) == null || mainViewModel.getRecognizedClass(gesture) == parentList.getDataSource()) {
            classTag.setVisible(false);
        } else {
            classTag.setVisible(true);
            classTag.setToolTipText(mainViewModel.getRecognizedClass(gesture).getNameProperty().getValue());
        }
    }

	public Gesture getDataSource() {
		return gesture;
	}

    @Override
    protected void onSelectionChanged() {
        iconContainer.setBackground(
                getDisplay().getSystemColor(isSelected() ? SWT.COLOR_LIST_SELECTION : SWT.COLOR_WHITE));
    }
}
