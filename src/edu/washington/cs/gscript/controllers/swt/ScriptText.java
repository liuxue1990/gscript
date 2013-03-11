package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Category;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ScriptText extends Composite {

    private MainViewModel mainViewModel;

    private Category category;

    private Text text;

    public ScriptText(Composite parent, int style, MainViewModel viewModel) {
        super(parent, style);
        setLayout(new FillLayout());

        text = new Text(this, SWT.MULTI);

        this.mainViewModel = viewModel;

        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserver() {
                    @Override
                    public void onNotified(Object arg) {
                        reloadData();
                    }
                },
                MainViewModel.CATEGORY_SELECTED_NOTIFICATION, mainViewModel);

        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                onChanged();
            }
        });
    }

    private void reloadData() {
        category = mainViewModel.getSelectedCategory();

        if (category != null) {
            text.setText(category.getScriptTextProperty().getValue());
        }
    }

    private void onChanged() {
        mainViewModel.setScript(category, text.getText());
    }
}
