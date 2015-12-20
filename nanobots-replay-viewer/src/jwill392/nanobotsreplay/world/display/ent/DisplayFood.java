package jwill392.nanobotsreplay.world.display.ent;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import jwill392.nanobotsreplay.assets.Assets;
import jwill392.nanobotsreplay.world.EntityModel;

public class DisplayFood extends WorldDisplayEntity {
	private final Image foodImg;

	public DisplayFood(Vector2f pos, EntityModel data) {
		super(pos, data);

		foodImg = Assets.getSheet("assets/spritesheet").getSprite("food.gif");
		foodImg.setCenterOfRotation(foodImg.getWidth()/2, foodImg.getHeight()/2);
	}

	@Override
	protected void draw(float x, float y) {
		foodImg.draw(x, y);
	}
}
