package jwill392.nanobotsreplay.ui;

import java.awt.Dimension;
import java.awt.Rectangle;

import jwill392.nanobotsreplay.NBRV;
import jwill392.nanobotsreplay.assets.Assets;
import jwill392.nanobotsreplay.util.ImgUtil;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.gui.GUIContext;

public class Frame extends AbstractUIComponent {
	private final Image frameImg;
	private AbstractUIComponent content;
	private final Rectangle relFramedArea;

	private final Image beforeFrameDraw;
	private final Image insideFrameDraw;

	public Frame(Dimension drawSize, Vector2f drawPos) throws SlickException {
		super(drawSize, drawPos);

		Image frameTheme = Assets.getSheet("assets/spritesheet").getSprite("frame.gif");
		frameImg = ImgUtil.buildPanelImage(frameTheme, drawSize, 6, 6, 8, 8);

		relFramedArea = new Rectangle(
				6,
				6,
				- frameTheme.getWidth() + 2,
				- frameTheme.getHeight() + 2);

		beforeFrameDraw = new Image(NBRV.WIDTH, NBRV.HEIGHT);
		insideFrameDraw = new Image(
				drawSize.width + relFramedArea.width,
				drawSize.height + relFramedArea.height);
	}

	@Override
	protected void drawSelfThenChildren(GUIContext container, Graphics g) throws SlickException {
		if (isHidden()) {
			return;
		}

		frameImg.draw(getAbsPos().x, getAbsPos().y);

		g.copyArea(beforeFrameDraw, 0, 0);

		for (AbstractUIComponent child : this) {
			child.drawSelfThenChildren(container, g);
		}

		g.copyArea(insideFrameDraw,
				getAbsBounds().x + relFramedArea.x,
				getAbsBounds().y + relFramedArea.y);

		g.drawImage(beforeFrameDraw, 0, 0);
		g.drawImage(insideFrameDraw,
				getAbsBounds().x + relFramedArea.x,
				getAbsBounds().y + relFramedArea.y);
	}

	@Override
	protected void draw(GUIContext container, Graphics g) throws SlickException {
		throw new IllegalStateException();
	}

	public void setContents(AbstractUIComponent newContent) {
		if (content != null) {
			removeChild(content);
		}

		content = newContent;
		newContent.setSize(new Dimension(
				getSize().width + (int) relFramedArea.getWidth(),
				getSize().height + (int) relFramedArea.getHeight()));
		newContent.setRelPos(new Vector2f(relFramedArea.x, relFramedArea.y));

		addChild(newContent);
	}

	@Override
	protected void tick(GameContainer container, int delta) throws SlickException {
	}
}
