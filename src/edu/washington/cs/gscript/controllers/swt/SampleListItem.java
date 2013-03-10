package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

import edu.washington.cs.gscript.models.Gesture;

public class SampleListItem extends ScrolledList.ListItem {

	private MainViewModel mainViewModel;

	private Composite icon;

	private Composite iconContainer;

	private Gesture gesture;

	private Image thumbnail;

	public SampleListItem(SampleScrolledList parent, MainViewModel viewModel) {
		super(parent, SWT.BACKGROUND);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.spacing = 5;
		setLayout(rowLayout);

		iconContainer = new Composite(this, SWT.BACKGROUND);

		icon = new Composite(iconContainer, SWT.BACKGROUND | SWT.BORDER);
		icon.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		RowData rd = new RowData();
		rd.width = 80;
		rd.height = 80;
		iconContainer.setLayoutData(rd);

		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = 5;
		fillLayout.marginWidth = 5;
		iconContainer.setLayout(fillLayout);

		setSelected(false);

		this.mainViewModel = viewModel;

		icon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				mainViewModel.selectSample(gesture);
			}
		});

		icon.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				if (thumbnail != null) {
					e.gc.drawImage(thumbnail, 0, 0);
				}
			}
		});

        onSelectionChanged();
	}

	public void setDataSource(Gesture gesture) {
		this.gesture = gesture;

		int width = 70;
		int height = 70;
		int padding = 5;

		thumbnail = new Image(getDisplay(), width, height);
		GC gc = new GC(thumbnail);
		MainWindowController.drawSample(gesture, gc, padding, padding, width - padding * 2, height - padding * 2);
		gc.dispose();

		icon.redraw();
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
