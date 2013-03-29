package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.*;
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
import org.eclipse.swt.widgets.Text;

public class CategoryListItem extends ScrolledList.ListItem {

	private MainViewModel mainViewModel;

	private Label nameLabel;

    private Text renameText;

    private StackLayout stackLayout;

	private IconWithButtonOverlay.IconWithRemoveButtonOverlay icon;

	private Composite iconContainer;

	private Category category;

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

        final Composite nameContainer = new Composite(this, SWT.BACKGROUND);
        nameContainer.setBackground(getBackground());

        stackLayout = new StackLayout();
        nameContainer.setLayout(stackLayout);

		nameLabel = new Label(nameContainer, SWT.NONE);
		nameLabel.setAlignment(SWT.CENTER);

        renameText = new Text(nameContainer, SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        stackLayout.topControl = nameLabel;

		iconContainer = new Composite(this, SWT.BACKGROUND);

		icon = new IconWithButtonOverlay.IconWithRemoveButtonOverlay(iconContainer, SWT.BACKGROUND | SWT.BORDER) {
            @Override
            protected void mouseDownOnIcon() {
                mainViewModel.selectCategory(category);
            }

            @Override
            protected void buttonClicked(int index) {
                mainViewModel.removeCategory(category);
            }
        };

		icon.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

		RowData rd = new RowData();
		rd.width = 120;
		nameContainer.setLayoutData(rd);

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

		nameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent e) {
                stackLayout.topControl = renameText;
                renameText.setText(category.getNameProperty().getValue());
                nameContainer.layout();
                renameText.setFocus();
            }
        });

        renameText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ESC) {
                    stackLayout.topControl = nameLabel;
                    nameContainer.layout();
                } else if (e.keyCode == SWT.CR) {
                    String newName = renameText.getText();

                    if (!newName.contains(" ") && !newName.equals(category.getNameProperty().getValue())) {
                        if (mainViewModel.getProject().renameCategory(category, renameText.getText())) {
                            stackLayout.topControl = nameLabel;
                            nameContainer.layout();
                        }
                    }
                }
            }
        });

		addDisposeListener(new DisposeListener() {
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
				itemObserver, NotificationCenter.ITEMS_ADDED_NOTIFICATION, category.getSamplesProperty());

		NotificationCenter.getDefaultCenter().addObserver(
				itemObserver, NotificationCenter.ITEMS_REMOVED_NOTIFICATION, category.getSamplesProperty());

		nameLabel.setText(category.getNameProperty().getValue());


		int width = 110;
		int height = 110;
		int padding = 5;

		Image thumbnail = new Image(getDisplay(), 110, 110);
		GC gc = new GC(thumbnail);
		if (category.getNumOfSamples() > 0) {
			MainWindowController.drawSample(
                    category.getSample(0),
                    gc, padding, padding, width - padding * 2, height - padding * 2,
                    getDisplay().getSystemColor(SWT.COLOR_BLUE),
                    getDisplay().getSystemColor(SWT.COLOR_WHITE));
		}

		gc.dispose();

        icon.setThumbnail(thumbnail);
	}

    @Override
	protected void onSelectionChanged() {
		iconContainer.setBackground(
				getDisplay().getSystemColor(isSelected() ? SWT.COLOR_LIST_SELECTION : SWT.COLOR_WHITE));
	}
}
