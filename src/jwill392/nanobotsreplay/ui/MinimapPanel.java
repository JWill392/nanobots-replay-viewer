package jwill392.nanobotsreplay.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Iterator;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.gui.GUIContext;

import com.google.common.eventbus.Subscribe;

import jwill392.nanobotsreplay.NBRV;
import jwill392.nanobotsreplay.Settings;
import jwill392.nanobotsreplay.world.EntityModel;
import jwill392.nanobotsreplay.world.WorldModel;
import jwill392.nanobotsreplay.world.WorldModel.ModelTurnChange;
import jwill392.nanobotsreplay.world.display.WorldView.ViewAreaChange;
import teampg.grid2d.point.AbsPos;
import teampg.grid2d.point.RelPos;

public class MinimapPanel extends AbstractUIComponent {
	private int _pixelsPerCell = 4;
	private static final Color BACKGROUND_COLOR = Color.black;
	
	private Image mapImage;
	private Rectangle viewArea;
	
	private boolean initialized = false;
	
	public MinimapPanel(Dimension drawSize, Vector2f drawPos) throws SlickException {
		super(drawSize, drawPos);
		
	}


	@Override
	protected void draw(GUIContext container, Graphics g) throws SlickException {
		// TODO Auto-generated method stub
		if (!initialized) { return; }
		
		g.drawImage(mapImage, getAbsPos().x, getAbsPos().y);
		g.drawRect(getAbsPos().x + (viewArea.x * _pixelsPerCell), 
				   getAbsPos().y + (viewArea.y * _pixelsPerCell), 
				   viewArea.width * _pixelsPerCell, 
				   viewArea.height * _pixelsPerCell);
	}

	@Override
	protected void tick(GameContainer container, int delta) throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPressed(int x, int y, int button) {
		super.onPressed(x, y, button);
		
		RelPos clickPos = asRelPos(x, y);
		RelPos cell = RelPos.of(clickPos.x / _pixelsPerCell, clickPos.y / _pixelsPerCell);
		
		switch (button) {
		case Input.MOUSE_LEFT_BUTTON:
			NBRV.eventBus.post(new MinimapCellClicked(cell));
		}
	}
	
	@Subscribe
	public void turnChanged(ModelTurnChange e) throws SlickException {
		this._pixelsPerCell = Math.min((int)(this.getSize().getWidth() / e.model.getSize().getWidth()),
									(int)(this.getSize().getHeight() / e.model.getSize().getHeight()));
		
		if (mapImage != null) {mapImage.destroy();}
		mapImage = buildMinimapImage(e.model, e.newTurn);
		
		this.initialized = true;
	}
	
	@Subscribe
	public void viewAreaChange(ViewAreaChange e) throws SlickException {
		this.viewArea = e.viewArea;
	}
	
	private Image buildMinimapImage(WorldModel model, int turnIndex) throws SlickException {
		ImageBuffer img = new ImageBuffer(model.getSize().width * _pixelsPerCell, 
							  model.getSize().height * _pixelsPerCell);
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				img.setRGBA(x, y, BACKGROUND_COLOR.getRed(), BACKGROUND_COLOR.getGreen(), 
						BACKGROUND_COLOR.getBlue(), BACKGROUND_COLOR.getAlpha());
			}
		}
		
		
		for (EntityModel ent : model) {
			Color entColor;
			
			switch (ent.onTurn(turnIndex).getType()) {
			case BOT:
				entColor = Settings.getTeamColor(ent.onTurn(turnIndex).getTid());
				break;
			case FOOD:
				entColor = Color.cyan;
				break;
			case WALL:
				entColor = Color.lightGray;
				break;
			default:
				throw new IllegalStateException();
			}

			int cellY = ent.onTurn(turnIndex).getPos().getY();
			int cellX = ent.onTurn(turnIndex).getPos().getX();
			for (int y = cellY * _pixelsPerCell; y < cellY * _pixelsPerCell + _pixelsPerCell; y++) {
				for (int x = cellX * _pixelsPerCell; x < cellX * _pixelsPerCell + _pixelsPerCell; x++) {
					img.setRGBA(x, y, 
							entColor.getRed(), entColor.getGreen(), 
							entColor.getBlue(), entColor.getAlpha());					
				}
			}
		}
		
		
		
		return img.getImage();
	}

	private RelPos asRelPos(int x, int y) {
		Vector2f absPos = this.getAbsPos();
		
		return RelPos.offsetVector(
				AbsPos.of((int)absPos.x, (int)absPos.y), 
				AbsPos.of(x, y));		
	}
	
	public static class MinimapCellClicked {
		public final RelPos cell;
		
		public MinimapCellClicked(RelPos cell) {
			this.cell = cell;
		}
	}
}
