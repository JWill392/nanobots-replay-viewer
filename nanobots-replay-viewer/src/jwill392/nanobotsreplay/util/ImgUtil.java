package jwill392.nanobotsreplay.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.ImageBuffer;
import org.newdawn.slick.SlickException;


public class ImgUtil {
	public static Image getScaled(Image img, float scale) throws SlickException {
		img.setFilter(Image.FILTER_NEAREST);
		return img.getScaledCopy(scale);
	}

	public static Image getSubImage(Image img, Rectangle dim) {
		return img.getSubImage(dim.x, dim.y, dim.width, dim.height);
	}

	public static Rectangle getRectangle(org.newdawn.slick.geom.Rectangle badRect) {
		return new Rectangle(
				(int)badRect.getX(),
				(int)badRect.getY(),
				(int)badRect.getWidth(),
				(int)badRect.getHeight()
				);
	}
	public static Rectangle getRectangle(Image img) {
		return new Rectangle(0, 0, img.getWidth(), img.getHeight());
	}
	public static Dimension getDim(Image img) {
		return new Dimension(img.getWidth(), img.getHeight());
	}

	public static void tileImgToBuffer(ImageBuffer target, Image src, Rectangle fillArea) {
		for (int destX = fillArea.x; destX < fillArea.x + fillArea.width; destX++) {
			for (int destY = fillArea.y; destY < fillArea.y + fillArea.height; destY++) {
				Color pxl = src.getColor(
						(destX - fillArea.x)%src.getWidth(),
						(destY - fillArea.y)%src.getHeight());
				target.setRGBA(destX, destY,
						pxl.getRed(), pxl.getGreen(), pxl.getBlue(), pxl.getAlpha());
			}
		}
	}


	public static void drawImgToBuffer(ImageBuffer target, Image src, final int destX, final int destY) {
		int currTarX = destX;
		int currTarY = destY;

		for (int srcX = 0; srcX < src.getWidth(); srcX++) {
			for (int srcY = 0; srcY < src.getHeight(); srcY++) {
				Color pxl = src.getColor(srcX, srcY);
				target.setRGBA(currTarX, currTarY,
						pxl.getRed(), pxl.getGreen(), pxl.getBlue(), pxl.getAlpha());

				currTarY++;
			}
			currTarY = destY;
			currTarX++;
		}
	}

	/**
	 * Given an image split into 9 sections, draws a panel using these sections as 4 corners, 4 edges, and center fill.
	 * @param theme Composed of 9 segments; top left corner, top side (etc...) and middle.
	 * 			Should look like a mini version of the panel you want to draw.
	 * lx and ty are top left point inside middle section
	 * rx and by are top left point inside bottom right corner
	 * @return
	 */
	public static Image buildPanelImage(Image theme, Dimension panelDim, int lx, int ty, int rx, int by) {
		checkArgument(panelDim.width > theme.getWidth());
		checkArgument(panelDim.height > theme.getHeight());
		Segment[] segValues = Segment.values();

		Rectangle themeRect = getRectangle(theme);
		Rectangle[] segmentRects = getSegments(themeRect, lx, ty, rx, by);

		// load image sections into array accessible by Segment enum ordinal
		Image[] segments = new Image[segValues.length];
		for (int i = 0; i < segValues.length; i++) {
			segments[i] = getSubImage(theme, segmentRects[i]);
		}

		//convenience variables
		Image cornerTopLeft     = segments[Segment.TOP_LEFT.ordinal()];
		Image cornerTopRight    = segments[Segment.TOP_RIGHT.ordinal()];
		Image cornerBottomLeft  = segments[Segment.BOTTOM_LEFT.ordinal()];
		Image cornerBottomRight = segments[Segment.BOTTOM_RIGHT.ordinal()];

		Image fillCentre = segments[Segment.CENTRE.ordinal()];

		Image sideTop    = segments[Segment.TOP.ordinal()];
		Image sideBottom = segments[Segment.BOTTOM.ordinal()];
		Image sideLeft   = segments[Segment.LEFT.ordinal()];
		Image sideRight  = segments[Segment.RIGHT.ordinal()];

		int w = panelDim.width;
		int h = panelDim.height;

		int hTop = sideTop.getHeight();
		int hBottom = sideBottom.getHeight();

		int wLeft = sideLeft.getWidth();
		int wRight = sideRight.getWidth();

		ImageBuffer bldr = new ImageBuffer(panelDim.width, panelDim.height);

		// TODO properly draw panels when the bits we have to draw don't fit in evenly (mainly a problem with big theme images)

		// fill centre
		tileImgToBuffer(bldr, fillCentre,
				new Rectangle(wLeft,hTop,w - wRight - wLeft, h - hBottom - hTop));

		// draw edges -- width for horizontal must subtract width of both corners (likewise for vertical)
		tileImgToBuffer(bldr, sideTop,    new Rectangle(wLeft     , 0          , w - wLeft - wRight, hTop));
		tileImgToBuffer(bldr, sideBottom, new Rectangle(wLeft     , h - hBottom, w - wLeft - wRight, hBottom));
		tileImgToBuffer(bldr, sideLeft,   new Rectangle(0         , hTop       , wLeft             ,  h - hTop - hBottom));
		tileImgToBuffer(bldr, sideRight,  new Rectangle(w - wRight, hTop       , wRight            ,  h - hTop - hBottom));

		// draw corners
		drawImgToBuffer(bldr, cornerTopLeft,     0         , 0);
		drawImgToBuffer(bldr, cornerTopRight,    w - wRight, 0);
		drawImgToBuffer(bldr, cornerBottomLeft,  0         , h - hBottom);
		drawImgToBuffer(bldr, cornerBottomRight, w - wRight, h - hBottom);

		return bldr.getImage();
	}


	private static int xMax(Rectangle rect) {
		return rect.x + rect.width;
	}
	private static int yMax(Rectangle rect) {
		return rect.y + rect.height;
	}

	public static void drawTiled(Image theme, Point relTileOffset, Rectangle fillArea) {
		int startX = relTileOffset.x % theme.getWidth();
		int startY = relTileOffset.y % theme.getHeight();
		if (startX > 0) {
			startX -= theme.getWidth();
		}
		if (startY > 0) {
			startY -= theme.getHeight();
		}

		Point start = new Point(startX, startY);

		List<Rectangle> tileRects = tileWithRects(fillArea, getDim(theme), start);

		for (Rectangle drawRect : tileRects) {
			/*
			 * draws subsections of images to avoid drawing outside bounds -- not needed due to improvements to Frame.
			int srcLeft = 0;
			int srcTop = 0;

			int srcRight = theme.getWidth();
			int srcBottom = theme.getHeight();

			if (drawRect.x == fillArea.x && drawRect.y == fillArea.y) {
				srcLeft = theme.getWidth() - drawRect.width;
				srcTop = theme.getHeight() - drawRect.height;
			} else if (drawRect.y == fillArea.y) {
				// remaining tops can be drawn as if top right
				srcRight = drawRect.width;
				srcTop = theme.getHeight() - drawRect.height;

			} else if (xMax(drawRect) == xMax(fillArea)) {
				// remaining rights can be drawn as if bottom right
				srcRight = drawRect.width;
				srcBottom = drawRect.height;


			} else if (yMax(drawRect) == yMax(fillArea)) {
				// remaining bottoms can be drawn as if bottom left
				srcLeft = theme.getWidth() - drawRect.width;
				srcBottom = drawRect.height;
			} else if (drawRect.x == fillArea.x){
				srcLeft = theme.getWidth() - drawRect.width;
			}

			Image subsect = theme.getSubImage(srcLeft, srcTop, srcRight, srcBottom);
			subsect.draw(drawRect.x, drawRect.y);
			*/
			theme.draw(drawRect.x, drawRect.y);
		}
	}

	public enum Segment {
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_RIGHT,
		BOTTOM_LEFT,

		TOP,
		RIGHT,
		BOTTOM,
		LEFT,

		CENTRE;
	}
	/**
	 *
	 * @param main
	 * @param lx First x pixel in middle column
	 * @param rx
	 * @param ty First y pixel in middle row
	 * @param by
	 * @return
	 */
	public static Rectangle[] getSegments(Rectangle main, int lx, int ty, int rx, int by) {
		checkArgument(main.contains(lx, ty) && main.contains(rx, by));
		Segment[] values = Segment.values();
		Rectangle[] ret = new Rectangle[values.length];

		int w =  main.width;
		int h =  main.height;

		int xLeft = 0;
		int xMid = lx;
		int xRight = rx;

		int yTop = 0;
		int yMid = ty;
		int yBottom = by;

		int hTop = ty;
		int hMid = by - ty;
		int hBottom = h - by;

		int wLeft = lx;
		int wMid = rx - lx;
		int wRight = w - rx;

		ret[Segment.TOP_LEFT.ordinal()]     = new Rectangle(xLeft , yTop   , wLeft , hTop);
		ret[Segment.TOP_RIGHT.ordinal()]    = new Rectangle(xRight, yTop   , wRight, hTop);
		ret[Segment.BOTTOM_RIGHT.ordinal()] = new Rectangle(xRight, yBottom, wRight, hBottom);
		ret[Segment.BOTTOM_LEFT.ordinal()]  = new Rectangle(xLeft , yBottom, wLeft , hBottom);

		ret[Segment.TOP.ordinal()]          = new Rectangle(xMid  , yTop   , wMid  , hTop);
		ret[Segment.RIGHT.ordinal()]        = new Rectangle(xRight, yMid   , wRight, hMid);
		ret[Segment.BOTTOM.ordinal()]       = new Rectangle(xMid  , yBottom, wMid  , hBottom);
		ret[Segment.LEFT.ordinal()]         = new Rectangle(xLeft , yMid   , wLeft , hMid);

		ret[Segment.CENTRE.ordinal()]       = new Rectangle(xMid  , yMid   , wMid  , hMid);

		return ret;
	}

	/**
	 * Given a big rectangle and a small rectangle, fill tiles small rect onto big rect.
	 * If rects aren't perfect multiples of each other (or are offset), some tiled rects
	 * will be subsections of small rect.
	 *
	 * @param relTileStart The point where the first (top left) tile rect will be layed.  Must be on top left of toFill, or within one tile dim of top left.
	 */
	public static List<Rectangle> tileWithRects(Rectangle toFill, Dimension tile, Point relTileStart) {
		List<Rectangle> ret = new ArrayList<>();

		int toFillMaxX = toFill.x + toFill.width;
		int toFillMaxY = toFill.y + toFill.height;

		Point tileStart = new Point(relTileStart.x + toFill.x, relTileStart.y + toFill.y);

		for (int x = tileStart.x; x < toFillMaxX; x += tile.width) {
			for (int y = tileStart.y; y < toFillMaxY; y += tile.height) {
				ret.add(toFill.intersection(new Rectangle(x, y, tile.width, tile.height)));
			}
		}

		return ret;
	}
}
