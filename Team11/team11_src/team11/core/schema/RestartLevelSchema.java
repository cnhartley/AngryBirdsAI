/*
 * <H1>Angry Birds AI</H1>
 * <H2>CPE 480 - Artificial Intelligence</H2>
 * <H3>Group Project - Team 11</H3>
 * 
 * <H2>Team Member(s):</H2>
 * <LI>Chris N. Hartley (cnhartle@calpoly.edu)
 * <LI>Brent Williams (brent.robert.williams@gmail.com)
 * <LI>Alex Bozarth (ajbando@gmail.com)
 * <LI>Taylor Nesheim (tnesheim@calpoly.edu)
 */
package team11.core.schema;

import team11.core.utils.ActionRobot;

import ab.vision.GameStateExtractor.GameState;

/**
 * Provides a general schema to be used to restart the current active level in
 * the Angry Birds game.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 */
public class RestartLevelSchema implements Schema {

	
	/**
	 * Restarts the current level and returns {@code true} if the game state
	 * after restarting the level is set to playing. Otherwise, this returns
	 * {@code false} because the game state is not set to playing.
	 * 
	 * @return  {@code true} after the level is restarted and the game state is
	 *          set to playing; otherwise, returns {@code false}.
	 */
	public boolean restartLevel() {
		GameState state = ActionRobot.getCurrentGameState();
		
		if (state == GameState.WON || state == GameState.LOST)
			ActionRobot.doClick(RESTART_BUTTON_ON_WON_OR_LOST_SCREEN);
		else if (state == GameState.PLAYING)
			ActionRobot.doClick(RESTART_BUTTON_ON_PLAYING_SCREEN);
		
		ActionRobot.waitForPlayingState();
		ActionRobot.zoomOut(15);
		
		return ActionRobot.isPlaying();
	}

}
