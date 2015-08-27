package edu.washington.cs.gscript.controllers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;

public class ScrolledList extends ScrolledComposite {

    private final static int DEFAULT_SCROLL_BAR_INCREMENT = 10;

    public static class ListItem extends Composite {
        private boolean selected;

        public ListItem(final ScrolledList parent, int style) {
            super(parent.listContainer, style);
            parent.items.add(this);

            selected = false;

            addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    parent.onItemDisposed(ListItem.this);
                }
            });
        }

        public void setSelected(boolean flag) {
            if (selected != flag) {
                selected = flag;
                onSelectionChanged();
            }
        }

        public boolean isSelected() {
            return selected;
        }

        protected void onSelectionChanged() {
        }
    }

    private boolean vertical;

    private boolean multi;

    private Composite content;

    private Composite listContainer;

    private ArrayList<ListItem> items;

    private int selectedItemIndex;

    public ScrolledList(Composite parent, int style) {
        super(parent, style | SWT.BACKGROUND);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

        if (getHorizontalBar() != null) {
            getHorizontalBar().setIncrement(DEFAULT_SCROLL_BAR_INCREMENT);
        }

        if (getVerticalBar() != null) {
            getVerticalBar().setIncrement(DEFAULT_SCROLL_BAR_INCREMENT);
        }

        setMinSize(0, 0);

        vertical = (style & SWT.H_SCROLL) == 0;
        multi = (style & SWT.MULTI) != 0;

        setExpandHorizontal(vertical);
        setExpandVertical(!vertical);

        content = new Composite(this, SWT.BACKGROUND);
        content.setBackground(getBackground());
        setContent(content);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginTop = computeTopBarHeight();
        content.setLayout(gridLayout);

        listContainer = new Composite(content, SWT.BACKGROUND);
        listContainer.setBackground(getBackground());

        listContainer.setLayoutData(new GridData(
                vertical ? GridData.CENTER : GridData.BEGINNING,
                vertical ? GridData.BEGINNING : GridData.CENTER, true, true));

        RowLayout rowLayout = new RowLayout((vertical && !multi || !vertical && multi)  ? SWT.VERTICAL : SWT.HORIZONTAL);
        rowLayout.center = true;
        listContainer.setLayout(rowLayout);

        final Composite focusHelper = new Composite(content, SWT.NONE) {
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                return new Point(0, 0);
            }
        };
        GridData gd = new GridData();
        gd.widthHint = gd.heightHint = 0;
        focusHelper.setLayoutData(gd);

        addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                updateContentLayout();
            }
        });

        listContainer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                focusHelper.setFocus();
            }
        });

        content.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                focusHelper.setFocus();
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                focusHelper.setFocus();
            }
        });

        items = new ArrayList<ListItem>();
        selectedItemIndex = -1;
    }

    public void updateContentLayout() {
        content.setSize(content.computeSize(
                vertical ? getClientArea().width : SWT.DEFAULT,
                vertical ? SWT.DEFAULT : getClientArea().height, true));
        listContainer.layout();
        content.setSize(content.computeSize(
                vertical ? getClientArea().width : SWT.DEFAULT,
                vertical ? SWT.DEFAULT : getClientArea().height, true));
    }

    public ListItem[] getListItems() {
        return items.toArray(new ListItem[items.size()]);
    }

    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    protected void selectItem(int index) {
        if (selectedItemIndex == index) {
            return;
        }

        if (selectedItemIndex >= 0) {
            items.get(selectedItemIndex).setSelected(false);
        }

        if (index >= 0) {
            items.get(index).setSelected(true);
        }

        selectedItemIndex = index;
    }

    private void onItemDisposed(ListItem listItem) {
        int index = items.indexOf(listItem);
        if (index == selectedItemIndex) {
            selectedItemIndex = -1;
        } else if (index < selectedItemIndex) {
            --selectedItemIndex;
        }
        items.remove(index);
    }

    public void scrollItemIntoView(int index) {
        ListItem item = items.get(index);
        Rectangle bounds = item.getBounds();

        Point origin = getOrigin();
        Rectangle area = this.getClientArea();
        int x = origin.x;
        int y = origin.y;
        if (bounds.x < origin.x) {
            x = Math.max(0, bounds.x);
        }
        if (bounds.y < origin.y) {
            y = Math.max(0, bounds.y);
        }
        if (bounds.x + bounds.width > origin.x + area.width) {
            x = Math.max(0, bounds.x + bounds.width - area.width);
        }
        if (bounds.y + bounds.height > origin.y + area.height) {
            y = Math.max(0, bounds.y + bounds.height - area.height);
        }

        this.setOrigin(x, y);
    }

    protected int computeTopBarHeight() {
        return 0;
    }
}
