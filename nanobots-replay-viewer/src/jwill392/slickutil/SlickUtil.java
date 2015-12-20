package jwill392.slickutil;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Comparator;

import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import teampg.grid2d.point.AbsPos;
import teampg.grid2d.point.RelPos;

public class SlickUtil {
	/**
	 * Two rects of same size are NOT considered to contain each other
	 */
	public static boolean contains(Rectangle a, Rectangle b) {
		float ax = a.getX();
		float ay = a.getY();
		float aw = a.getWidth();
		float ah = a.getHeight();

		float bx = b.getX();
		float by = b.getY();
		float bw = b.getWidth();
		float bh = b.getHeight();

		return ax < bx && (bx + bw) < (ax + aw) &&
				ay < by && (by + bh) < (ay + ah);
	}

	/**
	 * Two rects of same size ARE considered to contain each other
	 */
	public static boolean containsNotStrict(Rectangle a, Rectangle b) {
		float ax = a.getX();
		float ay = a.getY();
		float aw = a.getWidth();
		float ah = a.getHeight();

		float bx = b.getX();
		float by = b.getY();
		float bw = b.getWidth();
		float bh = b.getHeight();

		return ax <= bx && (bx + bw) <= (ax + aw) &&
				ay <= by && (by + bh) <= (ay + ah);
	}

	public static Vector2f getPos(Rectangle r) {
		return new Vector2f(r.getX(), r.getY());
	}

	public static Rectangle copy(Rectangle r) {
		return new Rectangle(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public static Rectangle newRect(Vector2f topLeft, float width, float height) {
		return new Rectangle(topLeft.x, topLeft.y, width, height);
	}

	public static Rectangle newRect(float lx, float ty, float rx, float by) {
		return new Rectangle(lx, ty, rx - lx, by - ty);
	}

	public static Dimension getRectDim(Rectangle rect) {
		return new Dimension((int)rect.getWidth(), (int)rect.getHeight());
	}

	public static Vector2f of(RelPos pos) {
		return new Vector2f(pos.x, pos.y);
	}

	/**
	 * Closer to angle is better (smaller).
	 */
	public static class ByAngleLikeness implements Comparator<Vector2f> {
		private final double angle;

		public ByAngleLikeness(double angle) {
			this.angle = angle;
		}

		public ByAngleLikeness(Vector2f closeTo) {
			this(closeTo.getTheta());
		}

		@Override
		public int compare(Vector2f a, Vector2f b) {
			double adiff = a.getTheta() - angle;
			double aDist = Math.min(Math.abs(adiff), Math.abs(adiff - 360));

			double bdiff = b.getTheta() - angle;
			double bDist = Math.min(Math.abs(bdiff), Math.abs(bdiff - 360));

			return Double.compare(aDist, bDist);
		}

	}

	public static void setSize(Rectangle drawArea, Dimension size) {
		drawArea.setWidth(size.width);
		drawArea.setHeight(size.height);
	}

	public static Rectangle newRect(Vector2f drawPos, Dimension drawSize) {
		return new Rectangle(drawPos.x, drawPos.y, drawSize.width, drawSize.height);
	}

	public static AbsPos of(Vector2f viewOffset) {
		return AbsPos.of((int) viewOffset.x, (int) viewOffset.y);
	}

	public static Point asPoint(Vector2f vec) {
		return new Point((int) vec.x, (int) vec.y);
	}
}
