package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import edu.washington.cs.gscript.framework.NotificationCenter;
import edu.washington.cs.gscript.framework.NotificationObserver;
import edu.washington.cs.gscript.models.Rect;
import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.XYT;

import java.io.IOException;

public class MainWindowController {

    public static void errorMessage(Shell shell, String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        messageBox.setText("Error");
        messageBox.setMessage(message);
        messageBox.open();
    }


	private MainViewModel mainViewModel;

    private Shell shell;

	private CategoryScrolledList categoryScrolledList;

	private SampleScrolledList sampleScrolledList;

	public MainWindowController(Shell shell, MainViewModel mainViewModel) {
        this.shell = shell;
		this.mainViewModel = mainViewModel;

        createMenu();
        createComponents();

		NotificationCenter.getDefaultCenter().addObserver(
				new NotificationObserver() {
					@Override
					public void onNotified(Object arg) {
						reloadProject();
					}
				},
				MainViewModel.PROJECT_CHANGED_NOTIFICATION,
				mainViewModel);
	}

	private void reloadProject() {
	}

	private void createMenu() {
		Menu mainMenu = new Menu(shell, SWT.BAR);

        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        MenuItem fileMenuHeader = new MenuItem(mainMenu, SWT.CASCADE);
        fileMenuHeader.setText("&File");
        fileMenuHeader.setMenu(fileMenu);

        Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
        MenuItem editMenuHeader = new MenuItem(mainMenu, SWT.CASCADE);
        editMenuHeader.setText("&Edit");
        editMenuHeader.setMenu(editMenu);


		MenuItem fileNewItem = new MenuItem(fileMenu, SWT.PUSH);
		fileNewItem.setText("&New");

		MenuItem fileOpenItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenItem.setText("&Open File...");
		fileOpenItem.setAccelerator(SWT.MOD1 + 'O');

		new MenuItem(fileMenu, SWT.SEPARATOR);

		MenuItem fileSaveItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveItem.setText("&Save");

		MenuItem fileSaveAsItem = new MenuItem(fileMenu, SWT.PUSH);
		fileSaveAsItem.setText("Save &As...");

        new MenuItem(fileMenu, SWT.SEPARATOR);

        MenuItem fileImportOneDollarData = new MenuItem(fileMenu, SWT.PUSH);
        fileImportOneDollarData.setText("Import $1 data...");


        fileOpenItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
                onUserActionOpen();
			}
		});

        fileSaveAsItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onUserActionSaveAs();
            }
        });

        fileImportOneDollarData.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onUserActionImportOneDollarData();
            }
        });

        MenuItem fileLoadTestData = new MenuItem(fileMenu, SWT.PUSH);
        fileLoadTestData.setText("Load test data");
        fileLoadTestData.setAccelerator(SWT.MOD1 + '0');
        fileLoadTestData.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mainViewModel.loadTestData();
            }
        });

		shell.setMenuBar(mainMenu);
	}

    private void onUserActionOpen() {
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterPath("~/Desktop");
        String fileName = dialog.open();
        if (fileName != null) {
            try {
                mainViewModel.openProject(fileName);
            } catch (IOException e) {
                errorMessage(shell, "Cannot open file");
            } catch (ClassNotFoundException e) {
                errorMessage(shell, "Unknown file format");
            } catch (ClassCastException e) {
                errorMessage(shell, "Not a valid project file");
            }
        }
    }

    private void onUserActionSaveAs() {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setOverwrite(true);
        dialog.setFilterPath("~/Desktop");
        String fileName = dialog.open();
        if (fileName != null) {
            try {
                mainViewModel.saveProject(fileName);
            } catch (IOException e) {
                errorMessage(shell, "Cannot save to file");
            }
        }
    }

    private void onUserActionImportOneDollarData() {
        DirectoryDialog dialog = new DirectoryDialog(shell);
        dialog.setFilterPath("/Users/hlv/repos/gscript/data/one_dollar");
        String dirName = dialog.open();

        if (dirName != null) {
            mainViewModel.importOneDollarGestures(dirName);
        }
    }

    private void createComponents() {
		shell.setLayout(new FillLayout());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		shell.setLayout(gridLayout);

		ToolBar toolbar = createGlobalToolBar();
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        toolbar.setLayoutData(gd);

        SashForm clientArea = new SashForm(shell, SWT.HORIZONTAL);
        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        clientArea.setLayoutData(gd);


		categoryScrolledList = new CategoryScrolledList(clientArea, SWT.NONE, mainViewModel);
		createMidPanel(clientArea);
		createRightPanel(clientArea);

		clientArea.setWeights(new int[]{20, 60, 40});
		clientArea.setFocus();
	}

	private ToolBar createGlobalToolBar() {
		ToolBar toolbar = new ToolBar(shell, SWT.HORIZONTAL | SWT.FLAT | SWT.BORDER);

		ToolItem itemAdd = new ToolItem(toolbar, SWT.PUSH);
		itemAdd.setText("Add");
		itemAdd.setToolTipText("Add a new gesture category");
		itemAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mainViewModel.addNewCategory();
			}
		});

        ToolItem itemAnalyze = new ToolItem(toolbar, SWT.PUSH);
        itemAnalyze.setText("Analyze");
        itemAnalyze.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                mainViewModel.analyze();
            }
        });

		return toolbar;
	}

	private Composite createMidPanel(Composite parent) {
		Composite midPanel = new Composite(parent, SWT.NONE);

		FormLayout formLayout = new FormLayout();
		formLayout.marginLeft = formLayout.marginRight = formLayout.marginTop = formLayout.marginBottom = 0;
		midPanel.setLayout(formLayout);

		GestureCanvas gestureCanvas = new GestureCanvas(midPanel, mainViewModel);
		sampleScrolledList = new SampleScrolledList(midPanel, SWT.NONE, mainViewModel);

		FormData fd = new FormData();
		fd.top = new FormAttachment(0);
		fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(sampleScrolledList);

		gestureCanvas.setLayoutData(fd);

		fd = new FormData();
		fd.left = new FormAttachment(0);
		fd.right = new FormAttachment(100);
		fd.bottom = new FormAttachment(100);
		fd.height = 100;

		sampleScrolledList.setLayoutData(fd);

		return midPanel;
	}

	private Composite createRightPanel(Composite parent) {
		SashForm rightPanel = new SashForm(parent, SWT.VERTICAL);

		new ScriptText(rightPanel, SWT.MULTI, mainViewModel);

		Composite outputComposite = new Composite(rightPanel, SWT.BACKGROUND);
		outputComposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		rightPanel.setWeights(new int[]{50, 50});

		return rightPanel;
	}

	public static void drawSample(Gesture gesture, GC gc, int x, int y, int width, int height) {
		gc.setAntialias(SWT.ON);

		Rect bounds = gesture.getBounds();

		gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLUE));
		gc.fillRectangle(x, y, width, height);

		double scale;

		if (bounds.getHeight() == 0) {
			scale = width / bounds.getWidth();
		} else if (bounds.getWidth() == 0) {
			scale = height / bounds.getHeight();
		} else {
			scale = Math.min(width / bounds.getWidth(), height / bounds.getHeight());
		}

		x += (int)((width - bounds.getWidth() * scale) / 2);
		y += (int)((height - bounds.getHeight() * scale) / 2);

		for (int i = 1, n = gesture.size(); i < n; ++i) {
            XYT p1 = gesture.get(i - 1);
            XYT p2 = gesture.get(i);

			int x1 = (int)((p1.getX() - bounds.getLeft()) * scale) + x;
			int y1 = (int)((p1.getY() - bounds.getTop()) * scale) + y;
			int x2 = (int)((p2.getX() - bounds.getLeft()) * scale) + x;
			int y2 = (int)((p2.getY() - bounds.getTop()) * scale) + y;

			gc.drawLine(x1, y1, x2, y2);
		}
	}
}
