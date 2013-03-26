package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.recognizers.Learner;
import edu.washington.cs.gscript.recognizers.Recognizer;
import org.eclipse.swt.widgets.Shell;

public class TestWindowController {

    MainViewModel mainViewModel;

    public TestWindowController(Shell shell, MainViewModel viewModel) {
        mainViewModel = viewModel;

        mainViewModel.getProject().learnProject();
        Recognizer recognizer = Recognizer.train(mainViewModel.getProject());
    }
}
