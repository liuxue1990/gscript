package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.swt.NotificationObserverFromUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;

public class SampleScrolledList extends ScrolledList {

	private MainViewModel mainViewModel;

	private Category category;

	private NotificationObserver listObserver = new NotificationObserverFromUI(this) {
		@Override
		public void onUINotified(Object arg) {
			reloadData();
		}
	};

	public SampleScrolledList(Composite parent, int style, MainViewModel mainViewModel) {
		super(parent, style | SWT.H_SCROLL);

		this.mainViewModel = mainViewModel;

		NotificationCenter.getDefaultCenter().addObserver(
				new NotificationObserverFromUI(this) {
					@Override
					public void onUINotified(Object arg) {
						reloadData();
					}
				},
				MainViewModel.CATEGORY_SELECTED_NOTIFICATION, mainViewModel);

        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserverFromUI(this) {
                    @Override
                    public void onUINotified(Object arg) {
                        reloadData();
                    }
                },
                MainViewModel.RECOGNITION_CHANGED_NOTIFICATION, mainViewModel);

        NotificationCenter.getDefaultCenter().addObserver(
				new NotificationObserverFromUI(this) {
					@Override
					public void onUINotified(Object arg) {
						updateSelection();

                        if (getSelectedItemIndex() >= 0) {
                            scrollItemIntoView(getSelectedItemIndex());
                        }
					}
				},
				MainViewModel.SAMPLE_SELECTED_NOTIFICATION, mainViewModel);
	}

    public Category getDataSource() {
        return category;
    }

	private void reloadData() {
		NotificationCenter.getDefaultCenter().removeObserver(listObserver);

		category = mainViewModel.getSelectedCategory();

		if (category != null) {
			NotificationCenter.getDefaultCenter().addObserver(
					listObserver, NotificationCenter.ITEMS_ADDED_NOTIFICATION, category.getSamplesProperty());

			NotificationCenter.getDefaultCenter().addObserver(
					listObserver, NotificationCenter.ITEMS_REMOVED_NOTIFICATION, category.getSamplesProperty());
		}

		for (ListItem item : getListItems()) {
			item.dispose();
		}

		if (category != null) {
            for (int i = 0, n = category.getNumOfSamples(); i < n; ++i) {
				addSample(category.getSample(i));
			}
		}

		updateSelection();
        updateContentLayout();
	}

	private void updateSelection() {
		Gesture selectedSample = mainViewModel.getSelectedSample();

        if (selectedSample == null) {
            selectItem(-1);
            return;
        }

        ListItem[] listItems = getListItems();
        for (int i = 0; i < listItems.length; ++i) {
            if (((SampleListItem) listItems[i]).getDataSource() == selectedSample) {
                selectItem(i);
                break;
            }
        }
	}

	private void addSample(Gesture gesture) {
		new SampleListItem(this, mainViewModel).setDataSource(gesture);
	}
}
