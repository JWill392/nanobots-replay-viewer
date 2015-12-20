package jwill392.nanobotsreplay.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import jwill392.slickutil.SlickUtil;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.gui.GUIContext;

public abstract class AbstractUIComponent implements Iterable<AbstractUIComponent>, MouseListener {
	private final Vector2f drawPos;
	private final Dimension drawSize;
	private final PriorityQueue<AbstractUIComponent> children;
	private AbstractUIComponent parent;

	private boolean hidden;

	private enum MouseButton {
		LEFT,
		MIDDLE,
		RIGHT;

		public static MouseButton of(int code) {
			switch (code) {
			case Input.MOUSE_LEFT_BUTTON:
				return LEFT;
			case Input.MOUSE_MIDDLE_BUTTON:
				return MIDDLE;
			case Input.MOUSE_RIGHT_BUTTON:
				return RIGHT;
			default:
				throw new IllegalStateException();
			}
		}
	}

	private final EnumSet<MouseButton> downAndStartedInsideComponent;
	private boolean mouseHover;
	private boolean focused;

	private final Queue<InputNotificationCommand> inputNotifications;

	// root stuff
	private static UIRoot root;
	public static void setRoot(Dimension screen, GameContainer container) {
		assert root == null;
		root = new UIRoot(screen, container);
	}
	public static UIRoot getRoot() {
		return root;
	}

	public boolean isMouseDown(int button) {
		return downAndStartedInsideComponent.contains(MouseButton.of(button));
	}
	public boolean isMouseHover() {
		return mouseHover;
	}
	public boolean isFocused() {
		return focused;
	}

	public AbstractUIComponent getFocusedChild() {
		for (AbstractUIComponent child : this) {
			if (child.isFocused()) {
				return child;
			}
		}
		return null;
	}

	// can be overridden
	public void onChildAdded(AbstractUIComponent child) {
		parent.onChildAdded(child);
	}
	public void onChildRemoved(AbstractUIComponent child) {
		parent.onChildRemoved(child);
	}

	public List<AbstractUIComponent> getRecursiveChildren() {
		final List<AbstractUIComponent> ret = new ArrayList<>();

		// only add children; not root
		for (AbstractUIComponent child : this) {
			teampg.util.Util.addEachBranchAndLeaf(ret, child);
		}

		return ret;
	}

	public AbstractUIComponent(Dimension drawSize, Vector2f drawPos) {
		this.drawPos = drawPos;
		this.drawSize = drawSize;

		hidden = false;
		children = new PriorityQueue<>(5, new Comparator<AbstractUIComponent>() {
			@Override
			public int compare(AbstractUIComponent a, AbstractUIComponent b) {
				return b.getDrawOrder() - a.getDrawOrder();
			}
		});
		inputNotifications = new LinkedList<>();
		downAndStartedInsideComponent = EnumSet.noneOf(MouseButton.class);
	}

	public AbstractUIComponent getParent() {
		return parent;
	}

	public Vector2f getRelPos() {
		return drawPos.copy();
	}
	public Vector2f getAbsPos() {
		return parent.getAbsPos().add(drawPos);
	}
	public Rectangle getAbsBounds() {
		return new Rectangle(SlickUtil.asPoint(getAbsPos()), drawSize);
	}

	public void setRelPos(Vector2f drawPos) {
		this.drawPos.set(drawPos);
	}

	public void setSize(Dimension size) {
		drawSize.setSize(size);
	}
	public Dimension getSize() {
		return (Dimension) drawSize.clone();
	}

	/**
	 * Can call remove; it's equivalent to calling removeChild.
	 */
	@Override
	public final Iterator<AbstractUIComponent> iterator() {
		final Iterator<AbstractUIComponent> iter = children.iterator();
		return new Iterator<AbstractUIComponent>() {
			private AbstractUIComponent curr;

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public AbstractUIComponent next() {
				curr = iter.next();
				return curr;
			}

			@Override
			public void remove() {
				if (curr == null) {
					throw new IllegalStateException();
				}

				onChildRemoved(curr);
				iter.remove();
			}
		};
	}
	public final void addChild(AbstractUIComponent child) {
		children.add(child);
		child.parent = this;
		onChildAdded(child);
	}
	public final void removeChild(AbstractUIComponent child) {
		children.remove(child);
		child.parent = null;
		onChildRemoved(child);
	}
	public final boolean hasChild(AbstractUIComponent child) {
		return children.contains(child);
	}
	public final void removeAllChildren() {
		for (Iterator<AbstractUIComponent> iter = iterator(); iter.hasNext();) {
			iter.next();
			iter.remove();
		}
	}

	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hid) {
		hidden = hid;
	}

	public void render(GUIContext container, Graphics g) throws SlickException {
		throw new IllegalStateException("Should not render UIComponents directly; call root.render");
	}

	protected void drawSelfThenChildren(GUIContext container, Graphics g) throws SlickException {
		if (hidden) {
			return;
		}

		draw(container, g);

		for (AbstractUIComponent child : children) {
			child.drawSelfThenChildren(container, g);
		}
	}

	/**
	 * Lowest is drawn last (on top of everything else)
	 */
	protected int getDrawOrder() {
		return 0;
	}
	protected abstract void draw(GUIContext container, Graphics g) throws SlickException;

	public void update(GameContainer container, int delta)
			throws SlickException {
		throw new IllegalStateException("Should not update UIComponents directly; call root.update");
	}

	protected final void tickSelfThenChildren(GameContainer container, int delta) throws SlickException {
		if (hidden) {
			return;
		}

		tick(container, delta);

		for (AbstractUIComponent child : children) {
			child.tickSelfThenChildren(container, delta);
		}
	}
	protected abstract void tick(GameContainer container, int delta) throws SlickException;


	public void onHoverStart() {
	}
	public void onHoverEnd() {
	}
	public void onPressed(int x, int y, int button) {
	}
	public void onClick(int x, int y, int button) {
	}
	public void onFocus() {
	}
	public void onBlur() {
	}


	@Override
	public void setInput(Input input) {
	}
	@Override
	public boolean isAcceptingInput() {
		return true;
	}
	@Override
	public void inputEnded() {
		while (!inputNotifications.isEmpty()) {
			InputNotificationCommand cmd = inputNotifications.remove();
			cmd.execute();
		}
	}
	@Override
	public void inputStarted() {
	}
	@Override
	public void mouseWheelMoved(int change) {
	}
	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
	}
	@Override
	public void mousePressed(int button, int x, int y) {
		assert !downAndStartedInsideComponent.contains(MouseButton.of(button));
		if (getAbsBounds().contains(x, y)) {
			downAndStartedInsideComponent.add(MouseButton.of(button));
			inputNotifications.add(new OnClick(true, x, y, button));

			if (!focused && button == Input.MOUSE_LEFT_BUTTON) {
				focused = true;
				inputNotifications.add(new OnFocus(true));
				onFocus();
			}

		} else {
			if (focused && button == Input.MOUSE_LEFT_BUTTON) {
				focused = false;
				inputNotifications.add(new OnFocus(false));
			}

		}
	}
	@Override
	public void mouseReleased(int button, int x, int y) {
		if (getAbsBounds().contains(x, y) && downAndStartedInsideComponent.contains(MouseButton.of(button))) {
			inputNotifications.add(new OnClick(false, x, y, button));
		}
		downAndStartedInsideComponent.remove(MouseButton.of(button));
	}
	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		// moved outside component
		if (!getAbsBounds().contains(newx, newy) && mouseHover) {
			mouseHover = false;
			inputNotifications.add(new OnHover(false));
			return;
		}

		// moved onto component
		if (!mouseHover) {
			inputNotifications.add(new OnHover(true));
			mouseHover = true;
			return;
		}

		// already hovering, moving within component.  Don't care.
	}
	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		mouseMoved(oldx, oldy, newx, newy);
	}

	// HERE BE DRAGONS
	private abstract class InputNotificationCommand {
		final boolean start;
		public InputNotificationCommand(boolean start) {
			this.start = start;
		}
		abstract void execute();
	}
	private class OnHover extends InputNotificationCommand {
		public OnHover(boolean start) {
			super(start);
		}

		@Override
		void execute() {
			if (start) {
				onHoverStart();
			} else {
				onHoverEnd();
			}
		}
	}
	private class OnFocus extends InputNotificationCommand {
		public OnFocus(boolean start) {
			super(start);
		}

		@Override
		void execute() {
			if (start) {
				onFocus();
			} else {
				onBlur();
			}
		}
	}
	private class OnClick extends InputNotificationCommand {
		final int x;
		final int y;
		final int button;
		public OnClick(boolean start, int x, int y, int button) {
			super(start);
			this.x = x;
			this.y = y;
			this.button = button;
		}

		@Override
		void execute() {
			if (start) {
				onPressed(x, y, button);
			} else {
				onClick(x, y, button);
			}
		}
	}
}
