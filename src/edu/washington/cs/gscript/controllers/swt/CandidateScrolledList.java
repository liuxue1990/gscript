package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.helpers.PartInstance;
import edu.washington.cs.gscript.helpers.SampleGenerator;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;

public class CandidateScrolledList extends ScrolledList {

    private MainViewModel mainViewModel;

    private Category category;

    private NotificationObserver listObserver = new NotificationObserver() {
        @Override
        public void onNotified(Object arg) {
            CandidateScrolledList.this.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });
        }
    };

    public CandidateScrolledList(Composite parent, int style, MainViewModel viewModel) {
        super(parent, style | SWT.V_SCROLL | SWT.MULTI);

        this.mainViewModel = viewModel;

        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserver() {
                    @Override
                    public void onNotified(Object arg) {
                        reloadData();
                    }
                },
                MainViewModel.CATEGORY_SELECTED_NOTIFICATION, mainViewModel);
    }

    public Category getDataSource() {
        return category;
    }

    private void reloadData() {
        NotificationCenter.getDefaultCenter().removeObserver(listObserver);

        category = mainViewModel.getSelectedCategory();

        if (category != null) {
            NotificationCenter.getDefaultCenter().addObserver(
                    listObserver, NotificationCenter.VALUE_CHANGED_NOTIFICATION, category.getPartsProperty());
        }

        for (ListItem item : getListItems()) {
            item.dispose();
        }

        if (category != null) {
            for (ArrayList<PartInstance> instanceList : category.getGenerated()) {
                addSample(SampleGenerator.stitch(instanceList));
            }
        }

        updateContentLayout();
    }

    private void addSample(Gesture gesture) {
        new CandidateListItem(this).setDataSource(gesture);
    }
}
