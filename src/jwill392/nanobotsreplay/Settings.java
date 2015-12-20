package jwill392.nanobotsreplay;

import org.newdawn.slick.Color;

public class Settings {
	public static Color getTeamColor(int tid) {
		if (tid == 0) {
			return new Color(0xBB8888);
		} else {
			return new Color(0x88BB88);
		}
	}
}
