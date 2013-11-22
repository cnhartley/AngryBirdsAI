/**
 * 
 */
package team11.core.schema;

import java.awt.Point;

/**
 * @author Chris Hartley
 *
 */
public interface Schema {

	/**
	 * The center of the restart button location on the "playing" game screen
	 * in the Angry Birds game for Google's Chrome web browser.
	 */
	public static final Point PAUSE_BUTTON_ON_PLAYING_SCREEN =
			new Point(30, 30);
	
	/**
	 * The center of the restart button location on the "playing" game screen
	 * in the Angry Birds game for Google's Chrome web browser.
	 */
	public static final Point RESTART_BUTTON_ON_PLAYING_SCREEN =
			new Point(100, 30);
	
	
	/**
	 * The center of the level selection button location on the "playing" game
	 * screen, once the "pause" button has been pressed, in the Angry Birds game
	 * for Google's Chrome web browser.
	 */
	public static final Point LEVEL_SELECTION_BUTTON_ON_PLAYING_SCREEN =
			new Point(168, 30);
	
	
	/**
	 * The center of the level selection button location on the "won" or "lost"
	 * game screens in the Angry Birds game for Google's Chrome web browser.
	 */
	public static final Point LEVEL_SELECTION_BUTTON_ON_WON_OR_LOST_SCREEN =
			new Point(340, 385);
	
	/**
	 * The center of the restart button location on the "won" or "lost" game
	 * screen in the Angry Birds game for Google's Chrome web browser.
	 */
	public static final Point RESTART_BUTTON_ON_WON_OR_LOST_SCREEN =
			new Point(420, 380);
	
	
	/**
	 * The center of the next level button location on the "won" game screen in
	 * the Angry Birds game for Google's Chrome web browser.
	 */
	public static final Point NEXT_LEVEL_BUTTON_ON_WON_SCREEN = 
			new Point(500, 375);
	
	
	/**
	 * The center of the hint check button in the lower-right of the hit pop-up
	 * that appears for hints in the Angry Birds game for Google's Chrome web
	 * browser.
	 * <P><B>Note:</B> This pop-up can randomly show or when a user clicks on
	 * the help question mark on the side bar.
	 */
	public static final Point HINT_CHECK_BUTTON_ON_PLAYING_SCREEN =
			new Point(510, 330);
	
	
	/**
	 * The center of the skip animation button location in the Angry Birds game
	 * for Google's Chrome web browser. The animation that plays before level 1
	 * loads can be skipped by pressing this location.
	 * <P><B>Note:</B> The animation does not appear in the SD mode.
	 */
	public static final Point SKIP_ANIMATION_BUTTON =
			new Point(1176, 704);

}
