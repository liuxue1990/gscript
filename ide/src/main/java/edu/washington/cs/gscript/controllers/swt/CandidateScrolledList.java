package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.framework.swt.NotificationObserverFromUI;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.SynthesizedGestureSample;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class CandidateScrolledList extends ScrolledList {

    private MainViewModel mainViewModel;

    private Category category;

    private NotificationObserver listObserver = new NotificationObserverFromUI(this) {
        @Override
        public void onUINotified(Object arg) {
            reloadData();
        }
    };

    public CandidateScrolledList(Composite parent, int style, MainViewModel viewModel) {
        super(parent, style | SWT.V_SCROLL | SWT.MULTI);

        this.mainViewModel = viewModel;

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
                        updateSelection();
                    }
                },
                MainViewModel.SYNTHESIZED_SAMPLE_SELECTED_NOTIFICATION, mainViewModel);

    }

    private void reloadData() {
        NotificationCenter.getDefaultCenter().removeObserver(listObserver);

        category = mainViewModel.getSelectedCategory();

        if (category != null) {
            NotificationCenter.getDefaultCenter().addObserver(
                    listObserver,
                    NotificationCenter.VALUE_CHANGED_NOTIFICATION,
                    category.getSynthesizedSamplesProperty());
        }

        for (ListItem item : getListItems()) {
            item.dispose();
        }

        if (category != null) {
            for (SynthesizedGestureSample sample : category.getSynthesizedSamples()) {
                addSample(sample);
            }
        }

        updateContentLayout();
        updateSelection();
    }

    private void updateSelection() {
        for (ListItem item : getListItems()) {
            if (item instanceof CandidateListItem) {
                CandidateListItem listItem = (CandidateListItem) item;
                listItem.setSelected(mainViewModel.isSynthesizedSampleSelected(listItem.getDataSource()));
            }
        }
    }

    private void addSample(SynthesizedGestureSample sample) {
        new CandidateListItem(this, mainViewModel).setDataSource(sample);
    }
}
