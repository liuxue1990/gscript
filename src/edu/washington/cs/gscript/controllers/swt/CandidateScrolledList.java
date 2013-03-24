package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.SynthesizedGestureSample;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

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
                    listObserver, NotificationCenter.VALUE_CHANGED_NOTIFICATION, category.getSynthesizedSamplesProperty());
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
    }

    private void addSample(SynthesizedGestureSample sample) {
        new CandidateListItem(this, mainViewModel).setDataSource(sample);
    }
}
