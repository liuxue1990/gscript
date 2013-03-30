package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.swt.NotificationObserverFromUI;
import edu.washington.cs.gscript.models.Project;
import edu.washington.cs.gscript.recognizers.Recognizer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Category;

public class CategoryScrolledList extends ScrolledList {

	private MainViewModel mainViewModel;

	private NotificationObserver listObserver = new NotificationObserverFromUI(this) {
		@Override
		public void onUINotified(Object arg) {
			reloadData();
		}
	};

	public CategoryScrolledList(Composite parent, int style, MainViewModel viewModel) {
		super(parent, style | SWT.V_SCROLL);

		this.mainViewModel = viewModel;

		NotificationCenter.getDefaultCenter().addObserver(
				new NotificationObserverFromUI(this) {
                    @Override
                    public void onUINotified(Object arg) {
                        reloadData();
                    }
                },
				MainViewModel.PROJECT_CHANGED_NOTIFICATION, mainViewModel);

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
				MainViewModel.CATEGORY_SELECTED_NOTIFICATION, mainViewModel);

        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserverFromUI(this) {
                    @Override
                    public void onUINotified(Object arg) {
                        updateRecalls();
                    }
                },
                MainViewModel.RECOGNITION_CHANGED_NOTIFICATION, mainViewModel);
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
                break;
            }
        }
	}

    private void updateRecalls() {
        for (ListItem item : getListItems()) {
            if (item instanceof CategoryListItem) {
                CategoryListItem listItem = (CategoryListItem) item;
                Recognizer.RecognitionInfo info = mainViewModel.getRecognitionInfo(listItem.getDataSource());

                if (info == null) {
                    listItem.setRecallInfo(null, null);
                } else {
                    String desc = null;

                    if (info.getConfusedCategories() != null && info.getConfusedCategories().length > 0) {
                        desc = "Most confused with ";
                        for (int i = 0; i < 2 && i < info.getConfusedCategories().length; ++i) {
                            if (i > 0) {
                                desc = desc + " & ";
                            }
                            desc = desc + info.getConfusedCategories()[i].getNameProperty().getValue();
                        }
                    }
                    listItem.setRecallInfo(info.getRecall(), desc);
                }
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

        updateRecalls();
		updateSelection();
        updateContentLayout();
	}

	private void addItem(Category category) {
		new CategoryListItem(this, mainViewModel).setDataSource(category);
	}

    protected int computeTopBarHeight() {
        return 0;
    }
}
