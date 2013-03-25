package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.models.Project;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
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

    public static class ButtonRefresh extends SimpleButton {

        public ButtonRefresh(Composite parent, int style) {
            super(parent, style);
        }

        @Override
        void paint(GC gc) {
            Rectangle bounds = getClientArea();

            Transform transform = new Transform(getDisplay());
            transform.translate(bounds.x, bounds.y);
            gc.setTransform(transform);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isHover() && !isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
            } else if (isHover() && isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
            }

            gc.setBackground(bg);
            gc.setForeground(fg);

            gc.fillArc(0, 0, bounds.width, bounds.height, 0, 360);

            gc.setLineWidth(4);

            gc.setLineCap(SWT.CAP_ROUND);
            gc.drawArc(8, 8, bounds.width - 16, bounds.height - 16, 45, 270);

            bg = gc.getBackground();
            gc.setBackground(gc.getForeground());
            int r = (bounds.width - 16) / 2;
            int xc = bounds.width / 2 + (int)(r / 1.414 + 4.5);
            int yc = bounds.height / 2 - (int)(r / 1.414 - 4.5);
            gc.fillPolygon(new int[] {xc, yc, xc - 9, yc, xc, yc - 9});
            gc.setBackground(bg);

            transform.dispose();
        }
    }

    public static class SimpleTextButton extends SimpleButton {

        private String text;

        private Font font;

        private int paddingHeight = 2;

        private int paddingWidth = 4;

        public SimpleTextButton(Composite parent, int style) {
            super(parent, style);

            text = "";
            font = new Font(getDisplay(),"Arial", 10, SWT.BOLD );
            this.addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    font.dispose();
                }
            });
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        void paint(GC gc) {
            Rectangle bounds = getClientArea();

            gc.setFont(font);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isHover() && !isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
            } else if (isHover() && isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
            }

            gc.setBackground(bg);
            gc.setForeground(fg);

            Point ext = gc.stringExtent(text);

            gc.fillRoundRectangle(
                    bounds.x + bounds.width / 2 - ext.x / 2 - paddingWidth,
                    bounds.y + bounds.height / 2 - ext.y / 2 - paddingHeight,
                    ext.x + paddingWidth * 2,
                    ext.y + paddingHeight * 2,
                    5, 5);

            gc.drawString(text, bounds.x + bounds.width / 2 - ext.x / 2, bounds.y + bounds.height / 2 - ext.y / 2, true);
        }

        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            GC gc = new GC(this);
            gc.setFont(font);
            Point stringExtent = gc.stringExtent(text);
            gc.dispose();

            return new Point(stringExtent.x + paddingWidth * 2, stringExtent.y + paddingHeight * 2);
        }
    }

    private NotificationObserver projectFileStatusObserver = new NotificationObserver() {
        @Override
        public void onNotified(Object arg) {
            updateShellText();
        }
    };

	private MainViewModel mainViewModel;

    private Shell shell;

	private CategoryScrolledList categoryScrolledList;

	private SampleScrolledList sampleScrolledList;

    private ScriptText scriptText;

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

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                Project project = MainWindowController.this.mainViewModel.getProject();

                if (project != null && project.isDirty()) {
                    MessageBox messageBox = new MessageBox(
                            MainWindowController.this.shell,
                            SWT.APPLICATION_MODAL | SWT.ICON_ERROR | SWT.YES | SWT.NO);
                    messageBox.setText("Unsaved Change");
                    messageBox.setMessage(
                            "The project has been modified. Are you sure you want to discard the change and quit?");
                    e.doit = messageBox.open() == SWT.YES;
                }

                if (e.doit) {
                    MainWindowController.this.mainViewModel.newProject();
                }
            }
        });

        shell.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent event) {
                Project project = MainWindowController.this.mainViewModel.getProject();
                if (project != null && project.isDirty()) {
                    try {
                        MainWindowController.this.mainViewModel.backupProject();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
	}

	private void reloadProject() {
        NotificationCenter.getDefaultCenter().removeObserver(projectFileStatusObserver);
        Project project = mainViewModel.getProject();
        if (project != null) {
            NotificationCenter.getDefaultCenter().addObserver(
                    projectFileStatusObserver,
                    NotificationCenter.VALUE_CHANGED_NOTIFICATION,
                    project.getFileNameProperty());

            NotificationCenter.getDefaultCenter().addObserver(
                    projectFileStatusObserver,
                    NotificationCenter.VALUE_CHANGED_NOTIFICATION,
                    project.getDirtyProperty());
        }

        updateShellText();
	}

    private void updateShellText() {
        shell.getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                Project project = mainViewModel.getProject();

                String title = "";

                if (project != null) {
                    String fileName = project.getFileNameProperty().getValue();
                    if (fileName == null) {
                        fileName = "New Project";
                    } else {
                        fileName = String.format("[%s]", fileName);
                    }

                    String status = "";
                    if (project.getDirtyProperty().getValue()) {
                        status = " - Modified";
                    }

                    title = fileName + status;
                }

                shell.setText(title);
            }
        });
    }

	private void createMenu() {
		Menu mainMenu = new Menu(shell, SWT.BAR);

        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        MenuItem fileMenuHeader = new MenuItem(mainMenu, SWT.CASCADE);
        fileMenuHeader.setText("&File");
        fileMenuHeader.setMenu(fileMenu);

        Menu toolMenu = new Menu(shell, SWT.DROP_DOWN);
        MenuItem editMenuHeader = new MenuItem(mainMenu, SWT.CASCADE);
        editMenuHeader.setText("&Tool");
        editMenuHeader.setMenu(toolMenu);


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

        MenuItem toolTestRecognizer = new MenuItem(toolMenu, SWT.PUSH);
        toolTestRecognizer.setText("Test recognizer...");
        toolTestRecognizer.setAccelerator(SWT.MOD1 + 'T');

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

        toolTestRecognizer.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Shell testShell = new Shell(shell, SWT.APPLICATION_MODAL | SWT.CLOSE);
                testShell.setSize(400, 400);

                new TestWindowController(testShell, mainViewModel);
                testShell.open();
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

        GridData gd;

        final Composite clientContainer = new Composite(shell, SWT.NONE);

        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        clientContainer.setLayoutData(gd);


        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = formLayout.marginHeight = 0;
        clientContainer.setLayout(formLayout);

        SashForm clientArea = new SashForm(clientContainer, SWT.HORIZONTAL);
        createLeftPanel(clientArea);
		createMidPanel(clientArea);
		createRightPanel(clientArea);

		clientArea.setWeights(new int[]{20, 60, 40});
		clientArea.setFocus();

        final SimpleButton btnAnalyze = new ButtonRefresh(clientContainer, SWT.BACKGROUND) {
            @Override
            protected void buttonClicked() {
                mainViewModel.analyze();
            }
        };
        btnAnalyze.setBackground(clientContainer.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        btnAnalyze.setSize(40, 40);

        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(100);
        clientArea.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        btnAnalyze.setLayoutData(fd);
        btnAnalyze.moveAbove(clientArea);

        scriptText.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                Rectangle bounds = scriptText.getBounds();
                Point loc = clientContainer.toControl(scriptText.toDisplay(bounds.x, bounds.y + bounds.height));
                FormData fd = new FormData();
                fd.left = new FormAttachment(0, loc.x - btnAnalyze.getSize().x / 2);
                fd.top = new FormAttachment(0, loc.y - btnAnalyze.getSize().y / 2);
                btnAnalyze.setLayoutData(fd);
                clientContainer.layout();
            }
        });
	}

    private Composite createLeftPanel(Composite parent) {
        Composite leftPanel = new Composite(parent, SWT.BACKGROUND);
        leftPanel.setBackground(leftPanel.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = formLayout.marginHeight = 0;
        leftPanel.setLayout(formLayout);

        SimpleTextButton itemAdd = new SimpleTextButton(leftPanel, SWT.BACKGROUND) {
            @Override
            protected void buttonClicked() {
                mainViewModel.addNewCategory();
            }
        };
        itemAdd.setBackground(leftPanel.getBackground());
        itemAdd.setText("+ Add Category");

        categoryScrolledList = new CategoryScrolledList(leftPanel, SWT.NONE, mainViewModel);

        FormData fd = new FormData();
        fd.right = new FormAttachment(100, -20);
        fd.top = new FormAttachment(0, 2);
        itemAdd.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.top = new FormAttachment(0);
        fd.bottom = new FormAttachment(100);
        categoryScrolledList.setLayoutData(fd);
        itemAdd.moveAbove(categoryScrolledList);

        return leftPanel;
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

		scriptText = new ScriptText(rightPanel, SWT.MULTI, mainViewModel);

		CandidateScrolledList outputComposite = new CandidateScrolledList(rightPanel, SWT.BACKGROUND, mainViewModel);
		outputComposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		rightPanel.setWeights(new int[]{50, 50});

		return rightPanel;
	}

	public static void drawSample(Gesture gesture, GC gc, int x, int y, int width, int height, Color fg, Color bg) {
		gc.setAntialias(SWT.ON);

		Rect bounds = gesture.getBounds();

		gc.setBackground(bg);
		gc.setForeground(fg);
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
