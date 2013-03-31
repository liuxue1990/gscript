package edu.washington.cs.gscript.controllers.swt;

import edu.washington.cs.gscript.controllers.MainViewModel;
import edu.washington.cs.gscript.framework.ReadWriteProperty;
import edu.washington.cs.gscript.framework.swt.NotificationObserverFromUI;
import edu.washington.cs.gscript.models.Project;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
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

        int lineWidth = 4;

        public ButtonRefresh(Composite parent, int style) {
            super(parent, style);
        }

        public void setLineWidth(int lineWidth) {
            this.lineWidth = lineWidth;
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

            gc.setLineWidth(lineWidth);

            gc.setLineCap(SWT.CAP_ROUND);
            gc.drawArc(8, 8, bounds.width - 16, bounds.height - 16, 45, 270);

            bg = gc.getBackground();
            gc.setBackground(gc.getForeground());
            int r = (bounds.width - 16) / 2;
            int xc = bounds.width / 2 + (int)(r / 1.414 + 4.5);
            int yc = bounds.height / 2 - (int)(r / 1.414 - 4.5);
            gc.fillPolygon(new int[] {xc, yc, xc - lineWidth * 2 - 1, yc, xc, yc - lineWidth * 2 - 1});
            gc.setBackground(bg);

            transform.dispose();
        }
    }

    public class ButtonRetrain extends ButtonRefresh {

        private String text;

        private Font font;

        public ButtonRetrain(Composite parent, int style) {
            super(parent, style);

            font = Resources.FONT_COURIER_NEW_10_BOLD;
        }

        public void setText(String text) {
            this.text = text;
            redraw();
        }

        @Override
        void paint(GC gc) {
            Rectangle bounds = getClientArea();

            Transform transform = new Transform(getDisplay());
            transform.translate(bounds.x, bounds.y);
            gc.setTransform(transform);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_BLUE);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isHover() && !isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
            } else if (isHover() && isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
            }

            gc.setBackground(bg);
            gc.setForeground(fg);

            gc.fillRoundRectangle(1, 1, bounds.width - 2, bounds.height - 2, 10, 10);

            if (text == null) {
                gc.setLineWidth(2);

                gc.setLineCap(SWT.CAP_ROUND);
                gc.drawArc(6, 6, bounds.width - 12, bounds.height - 12, 45, 270);

                bg = gc.getBackground();
                gc.setBackground(gc.getForeground());
                int r = (bounds.width - 16) / 2;
                int xc = bounds.width / 2 + (int)(r / 1.414 + 4.5);
                int yc = bounds.height / 2 - (int)(r / 1.414 - 4.5);
                gc.fillPolygon(new int[] {xc, yc, xc - lineWidth * 2 - 1, yc, xc, yc - lineWidth * 2 - 1});
                gc.setBackground(bg);

            } else {
                gc.setFont(font);
                Point ext = gc.stringExtent(text);
                gc.drawString(text, (bounds.width - ext.x) / 2, (bounds.height - ext.y) / 2, true);
            }

            transform.dispose();
        }

        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            return new Point(32, 32);
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
            font = Resources.FONT_ARIAL_12_BOLD;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        void paint(GC gc) {
            Rectangle bounds = getClientArea();

            gc.setFont(font);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
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
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    5, 5);

            gc.drawString(text, bounds.x + bounds.width / 2 - ext.x / 2, bounds.y + bounds.height / 2 - ext.y / 2, true);
        }

        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            GC gc = new GC(this);
            gc.setFont(font);
            Point stringExtent = gc.stringExtent(text);
            gc.dispose();

            return new Point(200, 50);
        }
    }

    public static class ButtonGreenYes extends SimpleButton {

        Font font;

        String text = "Add to Samples";

        public ButtonGreenYes(Composite parent, int style) {
            super(parent, style);
            font = Resources.FONT_ARIAL_12_NORMAL;
        }

        @Override
        void paint(GC gc) {
            Rectangle bounds = getClientArea();

            Transform transform = new Transform(getDisplay());
            transform.translate(bounds.x, bounds.y);
            gc.setTransform(transform);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isHover() && !isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_GREEN);
            } else if (isHover() && isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
            }

            gc.setBackground(bg);
            gc.setForeground(fg);

            gc.fillRoundRectangle(0, 0, bounds.width, bounds.height, 10, 10);

            int w = bounds.height;
            gc.setLineWidth(2);
            gc.drawLine(w / 4, bounds.height / 2, w * 3 / 4, bounds.height / 2);
            gc.drawLine(w / 2, bounds.height / 4, w / 2, bounds.height * 3/ 4);

            gc.setFont(font);
            gc.drawString(text, w, (bounds.height - gc.stringExtent(text).y) / 2, true);

            transform.dispose();
        }

        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            GC gc = new GC(this);
            gc.setFont(font);
            Point stringExtent = gc.stringExtent(text);
            gc.dispose();
            return new Point(stringExtent.x + stringExtent.y + 16, stringExtent.y + 8);
        }
    }

    public static class ButtonRedNo extends SimpleButton {

        Font font;

        String text = "Reject and Refresh";

        public ButtonRedNo(Composite parent, int style) {
            super(parent, style);
            font = Resources.FONT_ARIAL_12_NORMAL;
        }

        @Override
        void paint(GC gc) {
            Rectangle bounds = getClientArea();

            Transform transform = new Transform(getDisplay());
            transform.translate(bounds.x, bounds.y);
            gc.setTransform(transform);

            Color bg = getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
            Color fg = getDisplay().getSystemColor(SWT.COLOR_WHITE);

            if (isHover() && !isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_RED);
            } else if (isHover() && isPressed()) {
                bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
            }

            gc.setBackground(bg);
            gc.setForeground(fg);

            gc.fillRoundRectangle(0, 0, bounds.width, bounds.height, 10, 10);

            int w = bounds.height;
            gc.setLineWidth(2);
            gc.drawLine(w / 4, bounds.height / 4, w / 4 * 3, bounds.height / 4 * 3);
            gc.drawLine(w / 4 * 3, bounds.height / 4, w / 4, bounds.height / 4 * 3);

            gc.setFont(font);
            gc.drawString(text, w, (bounds.height - gc.stringExtent(text).y) / 2, true);

            transform.dispose();
        }

        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            GC gc = new GC(this);
            gc.setFont(font);
            Point stringExtent = gc.stringExtent(text);
            gc.dispose();
            return new Point(stringExtent.x + stringExtent.y + 16, stringExtent.y + 8);
        }
    }

    private NotificationObserver projectFileStatusObserver;

	private MainViewModel mainViewModel;

    private Shell shell;

	private CategoryScrolledList categoryScrolledList;

	private SampleScrolledList sampleScrolledList;

    private ScriptText scriptText;

    private ButtonRetrain btnRetrain;

    private ButtonRefresh btnAnalyze;

	public MainWindowController(Shell shell, MainViewModel mainViewModel) {
        this.shell = shell;
		this.mainViewModel = mainViewModel;

        shell.setSize(1280, 720);

        createMenu();
        createComponents();

        projectFileStatusObserver = new NotificationObserverFromUI(shell) {
            @Override
            public void onUINotified(Object arg) {
                updateShellText();
            }
        };

        NotificationCenter.getDefaultCenter().addObserver(
				new NotificationObserverFromUI(shell) {
					@Override
					public void onUINotified(Object arg) {
						reloadProject();
					}
				},
				MainViewModel.PROJECT_CHANGED_NOTIFICATION,
				mainViewModel);

        NotificationCenter.getDefaultCenter().addObserver(
                new NotificationObserverFromUI(shell) {
                    @Override
                    public void onUINotified(Object arg) {
                        if (MainWindowController.this.mainViewModel.getAccuracyProperty().getValue() == null) {
                            btnRetrain.setText(null);
                        } else {
                            btnRetrain.setText(
                                    "" + Math.round(
                                            MainWindowController.this.mainViewModel.getAccuracyProperty().getValue() * 100));
                        }
                    }
                },
                MainViewModel.RECOGNITION_CHANGED_NOTIFICATION,
                mainViewModel
        );

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                e.doit = confirmCloseProject();

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

    private boolean confirmCloseProject() {
        Project project = MainWindowController.this.mainViewModel.getProject();

        if (project != null && project.isDirty()) {
            MessageBox messageBox = new MessageBox(
                    MainWindowController.this.shell,
                    SWT.APPLICATION_MODAL | SWT.ICON_ERROR | SWT.YES | SWT.NO);
            messageBox.setText("Unsaved Change");
            messageBox.setMessage(
                    "The project has been modified. Are you sure you want to discard the change?");

            return messageBox.open() == SWT.YES;
        }

        return true;
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

        fileNewItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onUserActionNew();
            }
        });

        fileOpenItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
                onUserActionOpen();
			}
		});

        fileSaveItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onUserActionSave();
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

//        MenuItem fileLoadTestData = new MenuItem(fileMenu, SWT.PUSH);
//        fileLoadTestData.setText("Load test data");
//        fileLoadTestData.setAccelerator(SWT.MOD1 + '0');
//        fileLoadTestData.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                mainViewModel.loadTestData();
//            }
//        });

        toolTestRecognizer.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onUserActionTestRecognizer();
            }
        });

		shell.setMenuBar(mainMenu);
	}

    private void onUserActionNew() {
        if (!confirmCloseProject()) {
            return;
        }
        mainViewModel.newProject();
    }

    private void onUserActionOpen() {
        if (!confirmCloseProject()) {
            return;
        }
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

    private void doSave(String fileName) {
        if (fileName != null) {
            try {
                mainViewModel.saveProject(fileName);
            } catch (IOException e) {
                errorMessage(shell, "Cannot save to file");
            }
        }
    }

    private void onUserActionSave() {
        String fileName = mainViewModel.getProject().getFileNameProperty().getValue();
        if (fileName == null) {
            onUserActionSaveAs();
        } else {
            doSave(fileName);
        }
    }

    private void onUserActionSaveAs() {
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setOverwrite(true);
        dialog.setFilterPath("~/Desktop");
        String fileName = dialog.open();
        if (fileName != null) {
            doSave(fileName);
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

    private void onUserActionAnalyze() {
        if (!mainViewModel.isLearningNeeded()) {
            return;
        }

        onUserActionForceAnalyze();
    }

    private void onUserActionForceAnalyze() {
        final ReadWriteProperty<Integer> progress = new ReadWriteProperty<Integer>(0);
        ProgressDialog dialog = new ProgressDialog(this.shell, progress);
        dialog.setText("Progress");
        dialog.setPrompt("Analyzing parts...");
        Thread learningThread = new Thread() {
            @Override
            public void run() {
                mainViewModel.analyze(progress);
            }
        };
        learningThread.start();
        dialog.open();
    }

    private void onUserActionTestRecognizer() {
        final ReadWriteProperty<Integer> progress = new ReadWriteProperty<Integer>(0);
        ProgressDialog dialog = new ProgressDialog(shell, progress);
        dialog.setText("Progress");
        dialog.setPrompt("Training the recognizer...");
        Thread trainingThread = new Thread() {
            @Override
            public void run() {
                mainViewModel.trainRecognizer(progress);
            }
        };
        trainingThread.start();
        dialog.open();

        if (mainViewModel.getRecognizer() != null) {
            new TestingDialog(shell, mainViewModel).open();
        }
    }

    private void onUserActionAccept() {
        mainViewModel.setLabelOfSelectedSynthesizedSamples(1);
    }

    private void onUserActionReject() {
        mainViewModel.setLabelOfSelectedSynthesizedSamples(-1);
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

        btnAnalyze = new ButtonRefresh(clientContainer, SWT.BACKGROUND) {
            @Override
            protected void buttonClicked(MouseEvent e) {
                if ((e.stateMask & SWT.CTRL) != 0) {
                    onUserActionForceAnalyze();
                } else {
                    onUserActionAnalyze();
                }
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

        TitleBar titleBar = new TitleBar(leftPanel, SWT.BACKGROUND);
        titleBar.setTitle("Category List");

        btnRetrain = new ButtonRetrain(leftPanel, SWT.BACKGROUND) {
            @Override
            protected void buttonClicked(MouseEvent e) {
                final ReadWriteProperty<Integer> progress = new ReadWriteProperty<Integer>(0);
                ProgressDialog dialog = new ProgressDialog(shell, progress);
                dialog.setText("Progress");
                dialog.setPrompt("Training the recognizer...");
                Thread trainingThread = new Thread() {
                    @Override
                    public void run() {
                        mainViewModel.validateRecognition(progress);
                    }
                };
                trainingThread.start();
                dialog.open();
            }
        };
        btnRetrain.setBackground(leftPanel.getBackground());
        btnRetrain.setSize(32, 32);

        Composite buttonContainer = new Composite(leftPanel, SWT.BACKGROUND);
        buttonContainer.setBackground(leftPanel.getBackground());
        buttonContainer.setLayout(new RowLayout());

        SimpleTextButton itemAdd = new SimpleTextButton(buttonContainer, SWT.BACKGROUND) {
            @Override
            protected void buttonClicked(MouseEvent e) {
                mainViewModel.addNewCategory();
            }
        };
        itemAdd.setBackground(leftPanel.getBackground());
        itemAdd.setText("+ Add Category");

        categoryScrolledList = new CategoryScrolledList(leftPanel, SWT.NONE, mainViewModel);

        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.top = new FormAttachment(0);
        titleBar.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0);
        btnRetrain.setLayoutData(fd);
        btnRetrain.moveAbove(titleBar);

        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.top = new FormAttachment(titleBar);
        fd.bottom = new FormAttachment(buttonContainer);
        categoryScrolledList.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(categoryScrolledList, 0, SWT.CENTER);
        fd.bottom = new FormAttachment(100, -5);
        buttonContainer.setLayoutData(fd);

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

        Composite candidatesContainer = new Composite(rightPanel, SWT.BACKGROUND);
        candidatesContainer.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        FormLayout formLayout = new FormLayout();
        candidatesContainer.setLayout(formLayout);

        TitleBar titleBar = new TitleBar(candidatesContainer, SWT.BACKGROUND);
        titleBar.setTitle("Generated Samples");

        CandidateScrolledList outputComposite = new CandidateScrolledList(
                candidatesContainer, SWT.BACKGROUND, mainViewModel);
		outputComposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        Composite buttonContainer = new Composite(candidatesContainer, SWT.BACKGROUND);
        buttonContainer.setBackground(candidatesContainer.getBackground());
        buttonContainer.setLayout(new RowLayout());

        ButtonGreenYes btnAccept = new ButtonGreenYes(buttonContainer, SWT.BACKGROUND) {
            @Override
            protected void buttonClicked(MouseEvent e) {
                onUserActionAccept();
            }
        };
        btnAccept.setBackground(candidatesContainer.getBackground());

        ButtonRedNo btnReject = new ButtonRedNo(buttonContainer, SWT.NONE) {
            @Override
            protected void buttonClicked(MouseEvent e) {
                onUserActionReject();
            }
        };
        btnReject.setBackground(candidatesContainer.getBackground());

        FormData fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(0, 0);
        fd.right = new FormAttachment(100);
        titleBar.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.top = new FormAttachment(titleBar);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(buttonContainer);
        outputComposite.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(outputComposite, 0, SWT.CENTER);
        fd.bottom = new FormAttachment(100, -5);
        buttonContainer.setLayoutData(fd);

        buttonContainer.moveAbove(outputComposite);
        buttonContainer.moveAbove(titleBar);

		rightPanel.setWeights(new int[]{40, 60});

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
