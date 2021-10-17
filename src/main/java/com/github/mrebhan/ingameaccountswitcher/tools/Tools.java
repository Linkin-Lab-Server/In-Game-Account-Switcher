package com.github.mrebhan.ingameaccountswitcher.tools;

import net.minecraft.client.gui.screen.Screen;

/**
 * @author MRebhan
 * @author The_Fireplace
 */

public class Tools {
	/**
	 * Draws a rectangle with a border
	 * @param x
	 * First corner x
	 * @param y
	 * First corner y
	 * @param x1
	 * Opposite corner x
	 * @param y1
	 * Opposite corner y
	 * @param size
	 * border width
	 * @param borderColor
	 * border color(ARGB format)
	 * @param insideColor
	 * inside color(ARGB format)
	 */
	public static void drawBorderedRect(int x, int y, int x1, int y1, int size, int borderColor, int insideColor) {
		Screen.fill(x + size, y + size, x1 - size, y1 - size, insideColor);
		Screen.fill(x + size, y + size, x1, y, borderColor);
		Screen.fill(x, y, x + size, y1, borderColor);
		Screen.fill(x1, y1, x1 - size, y + size, borderColor);
		Screen.fill(x, y1 - size, x1, y1, borderColor);
	}
}
