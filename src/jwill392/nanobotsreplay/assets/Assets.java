package jwill392.nanobotsreplay.assets;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;

import org.newdawn.slick.Image;
import org.newdawn.slick.PackedSpriteSheet;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.SpriteSheetFont;

public class Assets {
	private static Map<String, PackedSpriteSheet> sheetMap = new HashMap<>();
	private static Map<Integer, SpriteSheetFont> fontMap = new HashMap<>();

	public static void loadSheet(String path) throws SlickException {
		if (!sheetMap.containsKey(path)) {
			sheetMap.put(path, new PackedSpriteSheet(path));
		}
	}

	public static PackedSpriteSheet getSheet(String path) {
		checkArgument(sheetMap.containsKey(path));
		return sheetMap.get(path);
	}

	public static void loadFont(int scale) throws SlickException {
		if (!fontMap.containsKey(scale)) {
			Image fontImg = new Image("/assets/font.png");
			fontImg.setFilter(Image.FILTER_NEAREST);
			fontImg = fontImg.getScaledCopy(scale);

			fontMap.put(scale, new SpriteSheetFont(new SpriteSheet(fontImg, 5*scale, 7*scale, scale), ' '));
		}
	}

	public static SpriteSheetFont getFont(int scale) {
		return fontMap.get(scale);
	}

	public static int getFontNormHeight(int scale) {
		return 5*scale;
	}

}
