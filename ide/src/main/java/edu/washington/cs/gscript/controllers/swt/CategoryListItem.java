package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.swt.NotificationObserverFromUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
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

    private Composite recallTag;

    private Double recallValue;

    private StackLayout stackLayout;

	private IconWithButtonOverlay.IconWithRemoveButtonOverlay icon;

	private Composite iconContainer;

	private Category category;

    private Font recallFont;

	private NotificationObserver itemObserver = new NotificationObserverFromUI(this) {
		@Override
		public void onUINotified(Object arg) {
			reloadData();
		}
	};

	public CategoryListItem(ScrolledList parent, MainViewModel viewModel) {
		super(parent, SWT.BACKGROUND);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

        recallFont = Resources.FONT_COURIER_NEW_10_BOLD;

        FormLayout formLayout = new FormLayout();
		setLayout(formLayout);

        final Composite nameContainer = new Composite(this, SWT.BACKGROUND);
        nameContainer.setBackground(getBackground());

        stackLayout = new StackLayout();
        nameContainer.setLayout(stackLayout);

        recallTag = new Composite(this, SWT.BACKGROUND) {
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                return new Point(24, 16);
            }
        };
        recallTag.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLUE));

        nameLabel = new Label(nameContainer, SWT.BACKGROUND);
        nameLabel.setBackground(getBackground());
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

		FormData fd = new FormData();
        fd.top = new FormAttachment(0);
        fd.left = new FormAttachment(0);
		fd.width = 120;
		nameContainer.setLayoutData(fd);

		fd = new FormData();
        fd.top = new FormAttachment(nameContainer);
        fd.left = new FormAttachment(nameContainer, 0, SWT.CENTER);
		fd.width = 120;
		fd.height = 120;
		iconContainer.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(iconContainer, 0, SWT.TOP);
        fd.left = new FormAttachment(iconContainer, 0, SWT.LEFT);
        recallTag.setLayoutData(fd);

        recallTag.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {

                if (recallValue != null) {
                    GC gc = e.gc;

                    gc.setTextAntialias(SWT.ON);

                    gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));

                    gc.setFont(recallFont);
                    String text = String.format("%d", Math.round(recallValue * 100));
                    Point stringExtent = gc.stringExtent(text);
                    Rectangle rect = recallTag.getClientArea();
                    gc.fillRectangle(rect);

                    gc.drawString(text, (rect.width - stringExtent.x) / 2, (rect.height - stringExtent.y) / 2, true);
                }
            }
        });

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
                renameText.selectAll();
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

        renameText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String newName = renameText.getText();
                if (!newName.contains(" ") && !newName.equals(category.getNameProperty().getValue())) {
                    mainViewModel.getProject().renameCategory(category, renameText.getText());
                }
                stackLayout.topControl = nameLabel;
                nameContainer.layout();
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

    public void setRecallInfo(Double recall, String detail) {
        this.recallValue = recall;

        if (recall == null) {
            recallTag.setVisible(false);
            icon.setToolTipText(null);
        } else {
            recallTag.setVisible(true);
            recallTag.redraw();
            icon.setToolTipText(detail);
        }
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

		nameLabel.setText(category.getNameProperty().getValue() + "  (" + category.getNumOfSamples() + ")");


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
