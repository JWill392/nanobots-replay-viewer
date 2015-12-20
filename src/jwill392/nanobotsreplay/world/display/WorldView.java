package jwill392.nanobotsreplay.world.display;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

import jwill392.nanobotsreplay.NBRV;
import jwill392.nanobotsreplay.assets.Assets;
import jwill392.nanobotsreplay.ui.AbstractUIComponent;
import jwill392.nanobotsreplay.ui.MinimapPanel.MinimapCellClicked;
import jwill392.nanobotsreplay.util.ImgUtil;
import jwill392.nanobotsreplay.world.EntityModel;
import jwill392.nanobotsreplay.world.WorldModel;
import jwill392.nanobotsreplay.world.WorldModel.ModelTurnChange;
import jwill392.nanobotsreplay.world.display.ent.WorldDisplayEntity;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.gui.GUIContext;

import com.google.common.collect.Iterators;
import com.google.common.eventbus.Subscribe;

import replay.Util;
import teampg.grid2d.point.AbsPos;

public class WorldView extends AbstractUIComponent {
	private static final Dimension CELL_SIZE = new Dimension(34, 34);
	private static final Dimension CELL_PADDING = new Dimension(2, 2);

	private WorldModel worldData;

	//private Image worldGrid;
	private final Image gridTheme;

	private Vector2f viewOffset;


	public WorldView(Dimension drawSize, Vector2f drawPos) {
		super(drawSize, drawPos);

		gridTheme = Assets.getSheet("assets/spritesheet").getSprite("grid.gif");
	}

	public void connectWorldModel(WorldModel model) {
		viewOffset = new Vector2f();
		worldData = model;


		NBRV.eventBus.post(new ViewAreaChange(getWorldViewArea()));
		setTurn(0);
	}
	public WorldModel getModel() {
		return worldData;
	}

	private Rectangle getGridArea() {
		Dimension sizeInTiles = worldData.getSize();
		return new Rectangle(
				(int)(getAbsPos().x + viewOffset.x),
				(int)(getAbsPos().y + viewOffset.y),
				gridTheme.getWidth() * sizeInTiles.width,
				gridTheme.getHeight() * sizeInTiles.height);

	}

	/**
	 * Gets drawing offset from world view pos, given a grid pos
	 */
	private Vector2f getDrawPosFromGridPos(AbsPos gridPos) {
		return new Vector2f(
				(CELL_SIZE.width * gridPos.x) + CELL_PADDING.width + viewOffset.x,
				(CELL_SIZE.height * gridPos.y) + CELL_PADDING.height + viewOffset.y);
	}

	/**
	 * Gets rectangle of map currently in viewport, <em>measured in tiles</em>.
	 * @return
	 */
	public Rectangle getWorldViewArea() {
		Rectangle pixelRect = getGridArea().intersection(getAbsBounds());

		return new Rectangle(
				Math.round((pixelRect.x - viewOffset.x - getAbsPos().x) / CELL_SIZE.width),
				Math.round((pixelRect.y - viewOffset.y - getAbsPos().y) / CELL_SIZE.height),
				Math.round((float)pixelRect.width / (float)CELL_SIZE.width - 0.001f),
				Math.round((float)pixelRect.height / (float)CELL_SIZE.height - 0.001f)
				);
	}



	@Override
	protected void draw(GUIContext container, Graphics g) throws SlickException {
		if (worldData == null) {
			return;
		}
		Rectangle drawArea = getGridArea().intersection(getAbsBounds());
		Point relTileOffset =  new Point(
				getGridArea().x - drawArea.x,
				getGridArea().y - drawArea.y
				);

		ImgUtil.drawTiled(gridTheme, relTileOffset, drawArea);
	}

	@Override
	public void tick(GameContainer container, int delta) throws SlickException {
		/* OLD FAST-PANNING... might switch back to this method if maps are too big
		 * if (panDown != null) {
			// TO/DO check if we'd have scrolled off map.  If so, only scroll to edge of map.
			Vector2f adjustedPanVec = panVector.copy().scale(delta);

			// FIX/ME temp measure to stop accidentally scrolling off screen
			if (!gridSize.contains(-(viewOffset.x + adjustedPanVec.x), -(viewOffset.y + adjustedPanVec.y))) {
				return;
			}

			viewOffset.add(adjustedPanVec);
		}*/

		int turnIndex = worldData.getTurn();

		for (AbstractUIComponent child : this) {
			WorldDisplayEntity dispEnt = (WorldDisplayEntity)child;

			child.setRelPos(getDrawPosFromGridPos(dispEnt.getGridPos(turnIndex)));
		}

	}

	@Subscribe
	public void turnChanged(ModelTurnChange e) {
		if (worldData != e.model) {
			return;
		}

		setTurn(e.newTurn);
	}

	private void setTurn(int turn) {
		// persist living ents; don't remake every turn
		for (Iterator<AbstractUIComponent> iter = iterator(); iter.hasNext();) {
			AbstractUIComponent childComponent = iter.next();

			WorldDisplayEntity entity = (WorldDisplayEntity) childComponent;

			if (!Iterators.contains(worldData.iterator(), entity.getData())) {
				iter.remove();
			}
		}
		int turnIndex = worldData.getTurn();

		for (EntityModel ent : worldData) {
			Vector2f displayEntDrawPos = getDrawPosFromGridPos(Util.of(ent.onTurn(turnIndex).getPos()));
			WorldDisplayEntity displayEnt = WorldDisplayEntity.getEnt(displayEntDrawPos, ent);

			if (!hasChild(displayEnt)) {
				addChild(displayEnt);
			}
		}
	}

	@Subscribe
	public void setViewArea(MinimapCellClicked e) {
		viewOffset.set(
				- teampg.util.Util.ensureRange((CELL_SIZE.width * e.cell.x) + CELL_PADDING.width 
						- getAbsBounds().width/2, 0, getGridArea().width-getAbsBounds().width),
				- teampg.util.Util.ensureRange((CELL_SIZE.height * e.cell.y) + CELL_PADDING.height 
						- getAbsBounds().height/2, 0, getGridArea().height-getAbsBounds().height));
		NBRV.eventBus.post(new ViewAreaChange(getWorldViewArea()));
	}
	

	@Override
	public void onPressed(int x, int y, int button) {
		super.onPressed(x, y, button);

		switch (button) {
		case Input.MOUSE_LEFT_BUTTON:
			if (getFocusedChild() instanceof WorldDisplayEntity) {
				NBRV.eventBus.post(new SelectedEntityChange(((WorldDisplayEntity)getFocusedChild()).getData()));
			} else {
				NBRV.eventBus.post(new SelectedEntityChange(null));
			}
			break;
		}
	}

	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
		super.mouseMoved(oldx, oldy, newx, newy);
		if (isMouseDown(Input.MOUSE_MIDDLE_BUTTON)) {
			viewOffset.add(new Vector2f(newx, newy).sub(new Vector2f(oldx, oldy)));
			NBRV.eventBus.post(new ViewAreaChange(getWorldViewArea()));
		}
	}



	public static class SelectedEntityChange {
		public final EntityModel selected;
		public SelectedEntityChange(EntityModel selected) {
			this.selected = selected;
		}
	}

	public static class ViewAreaChange {
		public final Rectangle viewArea;
		public ViewAreaChange(Rectangle newViewArea) {
			viewArea = newViewArea;
		}
	}
}
