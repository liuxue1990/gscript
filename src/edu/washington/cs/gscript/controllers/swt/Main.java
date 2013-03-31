package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.controllers.swt.MainWindowController;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		Display.setAppName("Gesture Script");
        MainViewModel mainViewModel = new MainViewModel();

        Display display = new Display();
        Resources.initResources(display);

		Shell shell = new Shell(display);
		new MainWindowController(shell, mainViewModel);

        shell.open();

        mainViewModel.newProject();

        try {
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();

            try {
                mainViewModel.backupProject();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        shell.dispose();
        Resources.disposeResources();
        display.dispose();
	}

}
