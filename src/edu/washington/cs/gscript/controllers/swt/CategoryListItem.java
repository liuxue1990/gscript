package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.widgets.Label;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Category;

public class CategoryListItem extends ScrolledList.ListItem {

	private MainViewModel mainViewModel;

	private Label name;

	private Composite icon;

	private Composite iconContainer;

	private Category category;

	private Image thumbnail;

	private NotificationObserver itemObserver = new NotificationObserver() {
		@Override
		public void onNotified(Object arg) {
			reloadData();
		}
	};

	public CategoryListItem(ScrolledList parent, MainViewModel viewModel) {
		super(parent, SWT.BACKGROUND);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.spacing = 5;
		setLayout(rowLayout);

		name = new Label(this, SWT.NONE);
		name.setAlignment(SWT.CENTER);

		iconContainer = new Composite(this, SWT.BACKGROUND);

		icon = new Composite(iconContainer, SWT.BACKGROUND | SWT.BORDER);
		icon.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		RowData rd = new RowData();
		rd.width = 120;
		name.setLayoutData(rd);

		rd = new RowData();
		rd.width = 120;
		rd.height = 120;
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
				mainViewModel.selectCategory(category);
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

		name.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
			}
		});

		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				NotificationCenter.getDefaultCenter().removeObserver(itemObserver);
			}
		});

        onSelectionChanged();
	}

	public void setDataSource(Category category) {
		this.category = category;

		reloadData();
	}

	public Category getDataSource() {
		return category;
	}

	private void reloadData() {
		NotificationCenter.getDefaultCenter().removeObserver(itemObserver);

		NotificationCenter.getDefaultCenter().addObserver(
				itemObserver, NotificationCenter.VALUE_CHANGED_NOTIFICATION, category.getNameProperty());

		NotificationCenter.getDefaultCenter().addObserver(
				itemObserver, NotificationCenter.ITEMS_ADDED_NOTIFICATION, category.getSamples());

		NotificationCenter.getDefaultCenter().addObserver(
				itemObserver, NotificationCenter.ITEMS_REMOVED_NOTIFICATION, category.getSamples());

		name.setText(category.getNameProperty().getValue());


		int width = 110;
		int height = 110;
		int padding = 5;

		thumbnail = new Image(getDisplay(), 110, 110);
		GC gc = new GC(thumbnail);
		if (category.getSamples().size() > 0) {
			MainWindowController.drawSample(
                    category.getSamples().get(0),
                    gc, padding, padding, width - padding * 2, height - padding * 2);
		}

		gc.dispose();

		redraw();
	}

    @Override
	protected void onSelectionChanged() {
		iconContainer.setBackground(
				getDisplay().getSystemColor(isSelected() ? SWT.COLOR_LIST_SELECTION : SWT.COLOR_WHITE));
	}
}
