package jwill392.nanobotsreplay.ui;

import java.awt.Dimension;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.gui.GUIContext;

public class UIRoot extends AbstractUIComponent {
	private final GameContainer container;

	UIRoot(Dimension drawSize, GameContainer container) {
		super(drawSize, new Vector2f());
		this.container = container;
		container.getInput().addMouseListener(this);
	}

	@Override
	public void onChildAdded(AbstractUIComponent child) {
		container.getInput().addMouseListener(child);
	}

	@Override
	public void onChildRemoved(AbstractUIComponent child) {
		container.getInput().removeMouseListener(child);
	}

	@Override
	public Vector2f getAbsPos() {
		return new Vector2f();
	}

	@Override
	public void render(GUIContext container, Graphics g) throws SlickException {
		for (AbstractUIComponent child : this) {
			child.drawSelfThenChildren(container, g);
		}
	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException {
		for (AbstractUIComponent child : this) {
			child.tickSelfThenChildren(container, delta);
		}
	}

	@Override
	protected void draw(GUIContext container, Graphics g) throws SlickException {
	}

	@Override
	protected void tick(GameContainer container, int delta) throws SlickException {
	}
}
