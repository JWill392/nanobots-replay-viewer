package jwill392.nanobotsreplay.world.display.ent;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import jwill392.nanobotsreplay.assets.Assets;
import jwill392.nanobotsreplay.world.EntityModel;

public class DisplayWall extends WorldDisplayEntity {
	private final Image wallImg;

	public DisplayWall(Vector2f pos, EntityModel data) {
		super(pos, data);

		wallImg = Assets.getSheet("assets/spritesheet").getSprite("wall.png");
		wallImg.setCenterOfRotation(wallImg.getWidth()/2, wallImg.getHeight()/2);
	}

	@Override
	protected void draw(float x, float y) {
		wallImg.draw(x, y);
	}
}
