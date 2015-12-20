package jwill392.nanobotsreplay.ui;

import java.awt.Dimension;

import jwill392.nanobotsreplay.assets.Assets;
import jwill392.nanobotsreplay.util.ImgUtil;
import jwill392.nanobotsreplay.world.EntityModel;
import jwill392.nanobotsreplay.world.WorldModel.ModelTurnChange;
import jwill392.nanobotsreplay.world.display.WorldView.SelectedEntityChange;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheetFont;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.gui.GUIContext;

import replay.ReplayProto.Replay.Entity;
import com.google.common.eventbus.Subscribe;

public class WorldInfoPanel extends AbstractUIComponent{
	private int turn = -1;
	private EntityModel selected;
	private final Image mainPanel;
	private final Image turnPanel;
	private final SpriteSheetFont font;

	public WorldInfoPanel(Dimension drawSize, Vector2f drawPos) throws SlickException {
		super(drawSize, drawPos);
		Image panelBackgroundTheme = Assets.getSheet("assets/spritesheet").getSprite("panel.png");

		Dimension turnPanelSize = new Dimension(drawSize.width, 30);
		Dimension mainPanelSize = new Dimension(drawSize.width, drawSize.height - 31);
		turnPanel = ImgUtil.buildPanelImage(panelBackgroundTheme, turnPanelSize, 6, 6, 8, 8);
		mainPanel = ImgUtil.buildPanelImage(panelBackgroundTheme, mainPanelSize, 6, 6, 8, 8);

		font = Assets.getFont(2);
	}

	@Override
	protected void draw(GUIContext container, Graphics g) throws SlickException {
		turnPanel.draw(getAbsPos().x, getAbsPos().y);
		mainPanel.draw(getAbsPos().x, getAbsPos().y + 31);

		font.drawString(getAbsPos().x + 10, getAbsPos().y + 10, "Turn: " + turn);

		if (selected != null) {
			Entity turnInfo = selected.onTurn(turn);

			font.drawString(getAbsPos().x + 10, getAbsPos().y + 40, "ID: " + turnInfo.getEid());
			font.drawString(getAbsPos().x + 10, getAbsPos().y + 60, "Energy: " + turnInfo.getEnergy());

			font.drawString(getAbsPos().x + 10, getAbsPos().y + 80, "Msgs: " + turnInfo.getInboxCount());
			font.drawString(getAbsPos().x + 10, getAbsPos().y + 100, "Mem: " + Integer.toBinaryString(turnInfo.getMemory()));

			if (turnInfo.hasRunningAction()) {
				font.drawString(
						getAbsPos().x + 10, getAbsPos().y + 120,
						"Action: " + turnInfo.getRunningAction().getType());
				font.drawString(
						getAbsPos().x + 10, getAbsPos().y + 140,
						" > " + turnInfo.getRunningAction().getOutcome());
			}
		}
	}

	@Override
	public void tick(GameContainer container, int delta) throws SlickException {
	}

	private void setSelected(EntityModel ent) {
		selected = ent;
	}

	private void setTurn(int newTurn) {
		turn = newTurn;
	}

	@Subscribe
	public void turnChanged(ModelTurnChange e) {
		if (selected != null && !selected.hasTurn(e.newTurn)) {
			selected = null;
		}

		setTurn(e.newTurn);
	}

	@Subscribe
	public void selectedEntityChanged(SelectedEntityChange e) {
		setSelected(e.selected);

		if (e.selected == null) {
			return;
		}

		System.out.println(selected.onTurn(turn));
	}
}
