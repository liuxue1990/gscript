package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Category;

public class CategoryScrolledList extends ScrolledList {

	private MainViewModel mainViewModel;

	private NotificationObserver listObserver = new NotificationObserver() {
		@Override
		public void onNotified(Object arg) {
			reloadData();
		}
	};

	public CategoryScrolledList(Composite parent, int style, MainViewModel viewModel) {
		super(parent, style | SWT.V_SCROLL);

		this.mainViewModel = viewModel;

		NotificationCenter.getDefaultCenter().addObserver(
				listObserver,
				MainViewModel.PROJECT_CHANGED_NOTIFICATION, mainViewModel);

		NotificationCenter.getDefaultCenter().addObserver(
				new NotificationObserver() {
					@Override
					public void onNotified(Object arg) {
						updateSelection();
					}
				},
				MainViewModel.CATEGORY_SELECTED_NOTIFICATION, mainViewModel);
	}

	private void updateSelection() {
		Category selectedCategory = mainViewModel.getSelectedCategory();
        if (selectedCategory == null) {
            selectItem(-1);
            return;
        }

        ListItem[] items = getListItems();
        for (int i = 0; i < items.length; ++i) {
            if (selectedCategory == ((CategoryListItem)items[i]).getDataSource()) {
                selectItem(i);
                scrollItemIntoView(i);
                break;
            }
        }
	}

	private void reloadData() {
		NotificationCenter.getDefaultCenter().removeObserver(listObserver);

		NotificationCenter.getDefaultCenter().addObserver(listObserver,
				NotificationCenter.ITEMS_ADDED_NOTIFICATION, mainViewModel.getProject().getCategoriesProperty());

		NotificationCenter.getDefaultCenter().addObserver(listObserver,
				NotificationCenter.ITEMS_REMOVED_NOTIFICATION, mainViewModel.getProject().getCategoriesProperty());

		for (ListItem item : getListItems()) {
			item.dispose();
		}

        for (int i = 0, n = mainViewModel.getProject().getNumOfCategories(); i < n; ++i) {
            addItem(mainViewModel.getProject().getCategory(i));
        }

		updateSelection();
        updateContentLayout();
	}

	private void addItem(Category category) {
		new CategoryListItem(this, mainViewModel).setDataSource(category);
	}

}
