package jwill392.nanobotsreplay;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import jwill392.nanobotsreplay.assets.Assets;
import jwill392.nanobotsreplay.ui.AbstractUIComponent;
import jwill392.nanobotsreplay.ui.Frame;
import jwill392.nanobotsreplay.ui.MinimapPanel;
import jwill392.nanobotsreplay.ui.WorldInfoPanel;
import jwill392.nanobotsreplay.world.WorldModel;
import jwill392.nanobotsreplay.world.display.WorldView;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

import replay.ReplayProto.Replay;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

public class NBRV extends BasicGame {
	public static final int HEIGHT = 720;
	public static final int WIDTH = 1280;
	public static final Dimension SCREEN = new Dimension(WIDTH, HEIGHT);

	public static final EventBus eventBus = new EventBus();
	private GameContainer container;

	private WorldView worldDisplay;
	private WorldInfoPanel infoPanel;
	private Frame worldFrame;
	private Frame minimapFrame;
	private MinimapPanel minimapPanel;

	private WorldModel worldModel;

	private int keyRepeatCounter = 1;
	private static final int KEY_REPEAT = 5;
	private static final int INITIAL_KEY_REPEAT = 20;
	private static final List<Integer> repeatKeys = ImmutableList.of(Input.KEY_UP, Input.KEY_DOWN, Input.KEY_RIGHT, Input.KEY_LEFT);

	public NBRV() {
		super("Nanobots Replay Viewer");
	}

	public static void main(String[] args) throws SlickException, FileNotFoundException, IOException {
		//checkArgument(args.length == 1);
		//checkArgument(new File(args[0]).exists());

		NBRV inst = new NBRV();
		AppGameContainer app = new AppGameContainer(inst);

		//app.setMouseGrabbed(true);
		app.setShowFPS(true);
		app.setSmoothDeltas(true);
		app.setVSync(true);
		app.setDisplayMode((int)SCREEN.getWidth(), (int)SCREEN.getHeight(), false);
		app.start();
	}

	@Override
	public void init(GameContainer container) throws SlickException {
		container.getGraphics().setBackground(new Color(0x9990AA));
		this.container = container;

		Assets.loadSheet("assets/spritesheet");
		Assets.loadFont(2);

		AbstractUIComponent.setRoot(SCREEN, container);

		worldFrame = new Frame(new Dimension(980, 718), new Vector2f(1, 1), "frame.gif");
		AbstractUIComponent.getRoot().addChild(worldFrame);

		worldDisplay = new WorldView(new Dimension(), new Vector2f());
		worldFrame.setContents(worldDisplay); //sets self as parent

		eventBus.register(worldDisplay);

		infoPanel = new WorldInfoPanel(new Dimension(298, 498), new Vector2f(982, 1));
		AbstractUIComponent.getRoot().addChild(infoPanel);
		eventBus.register(infoPanel);

		minimapFrame = new Frame(new Dimension(298, 219), new Vector2f(982,500), "panel.png");
		AbstractUIComponent.getRoot().addChild(minimapFrame);
		
		minimapPanel = new MinimapPanel(new Dimension(), new Vector2f());
		minimapFrame.setContents(minimapPanel);
		eventBus.register(minimapPanel);

		//Tooltip testTip = new Tooltip(new Dimension(100, 20), new Vector2f(100, 100));
		//AbstractUIComponent.getRoot().addChild(testTip);


		//Replay rep = Replay.parseFrom(new FileInputStream(args[0]));
		Replay rep = null;
		try {
			rep = Replay.parseFrom(new FileInputStream("C:\\Users\\jack\\Documents\\nanobots\\testreplay"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		load(rep);
	}

	@Override
	public void keyPressed(int key, char c) {
		super.keyPressed(key, c);
		keyRepeatCounter = INITIAL_KEY_REPEAT;

		doKeyPressedAction(key);
	}

	private void doKeyPressedAction(int key) {
		if (worldModel == null) {
			return;
		}

		if (key == Input.KEY_RIGHT) {
			if (worldModel.hasNextTurn()) {
				worldModel.nextTurn();
			}
		} else if (key == Input.KEY_LEFT) {
			if (worldModel.hasPrevTurn()) {
				worldModel.prevTurn();
			}
		} else if (key == Input.KEY_UP) {
			if (worldModel.hasTurn(worldModel.getTurn() + 9)) {
				worldModel.setTurn(worldModel.getTurn() + 9);
			}
		} else if (key == Input.KEY_DOWN) {
			if (worldModel.hasTurn(worldModel.getTurn() - 9)) {
				worldModel.setTurn(worldModel.getTurn() - 9);
			}
		} else if (key == Input.KEY_SPACE) {
			container.exit();
		}
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException {
		AbstractUIComponent.getRoot().render(container, g);
	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException {
		AbstractUIComponent.getRoot().update(container, delta);

		inputTick(container.getInput());
	}

	private void inputTick(Input input) {
		if (keyRepeatCounter > 0) {
			keyRepeatCounter--;
		}
		if (keyRepeatCounter != 0) {
			return;
		}

		for (Integer keyCode : repeatKeys) {
			if (input.isKeyDown(keyCode)) {
				keyRepeatCounter = KEY_REPEAT;
				doKeyPressedAction(keyCode);
				return;
			}
		}

	}

	public void load(Replay rep) {
		worldModel = new WorldModel(rep);
		worldDisplay.connectWorldModel(worldModel);
	}
}
