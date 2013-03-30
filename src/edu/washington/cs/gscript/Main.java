package edu.washington.cs.gscript;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.controllers.swt.MainWindowController;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Main {

	public static void main(String[] args) {
		Display.setAppName("Gesture Script");
        MainViewModel mainViewModel = new MainViewModel();

        Display display = new Display();
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
        } catch (Exception e) {
            e.printStackTrace();
            shell.dispose();
        }

        display.dispose();
	}

}
