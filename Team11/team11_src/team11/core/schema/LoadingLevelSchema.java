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

import java.awt.Point;
import java.util.LinkedList;

import team11.core.utils.ActionRobot;

import ab.vision.GameStateExtractor.GameState;


/**
 * Provides a general level loading schema to click through the different menus
 * in the Angry Birds game and load specific levels to be attempted by an AI
 * agent.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 */
public class LoadingLevelSchema implements Schema {

	// Member data.
	private int currentLevel = -1;
	
	
	/**
	 * Attempts to load the specified level and returns {@code true} if the 
	 * game state is set to playing. This method returns {@code false} if the 
	 * game state is anything other than playing.
	 * 
	 * @param level the {@code int} level number to attempt to load.
	 * 
	 * @return  {@code true} if the specified level was loaded and the game
	 *          state is set to playing; otherwise, returns {@code false}.
	 */
	public boolean loadLevel(int level) {
		if (level > 21)
			level = ((level % 21) == 0) ? 21 : level % 21;
		
		while (!loadLevel(ActionRobot.getCurrentGameState(), level))
			ActionRobot.sleep(12000);
		
		return ActionRobot.isPlaying();
	}


	/**
	 * Attempts to load the specific level given the current game state and 
	 * returns {@code true} if the game state is set to playing. This method 
	 * returns {@code false} if the game state is anything other than playing.
	 * 
	 * @param state  the {@link GameState} for the current state of the game.
	 * @param level  the {@code int} level number to attempt to load.
	 * 
	 * @return  {@code true} if the specified level was loaded and the game
	 *          state is set to playing; otherwise, returns {@code false}.
	 */
	private boolean loadLevel(GameState state, int level) {
		ActionRobot.GoFromMainMenuToLevelSelection();
		
		LinkedList<Point> clicks = new LinkedList<Point>();
		int tileX = 54 + ((level - 1) % 7) * 86;
		int tileY = 110 + ((level - 1) / 7) * 100;
		Point tileLevel = new Point(tileX, tileY);
		
		if (state == GameState.WON || state == GameState.LOST) {
			if (state == GameState.WON && level > currentLevel)
				clicks.add(NEXT_LEVEL_BUTTON_ON_WON_SCREEN);
			else {
				clicks.add(LEVEL_SELECTION_BUTTON_ON_WON_OR_LOST_SCREEN);
				clicks.add(tileLevel);
			}
		}
		else if (state == GameState.PLAYING) {
			clicks.add(PAUSE_BUTTON_ON_PLAYING_SCREEN);
			clicks.add(LEVEL_SELECTION_BUTTON_ON_PLAYING_SCREEN);
			clicks.add(tileLevel);
		}
		else
			clicks.add(tileLevel);
		
		if (level == 1)
			clicks.add(SKIP_ANIMATION_BUTTON);
		
		for (Point click : clicks) {
			ActionRobot.doClick(click);
			ActionRobot.sleep(1000);
		}
		
		ActionRobot.waitForPlayingState();
		ActionRobot.zoomOut(15);
		
		currentLevel = level;
		return ActionRobot.isPlaying();
	}

}
