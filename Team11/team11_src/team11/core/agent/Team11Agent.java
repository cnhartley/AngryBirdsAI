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
package team11.core.agent;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import team11.core.birds.Bird;
import team11.core.birds.BlackBird;
import team11.core.birds.BlueBird;
import team11.core.birds.RedBird;
import team11.core.birds.WhiteBird;
import team11.core.birds.YellowBird;

import team11.core.other.Env;
import team11.core.other.Shot;
import team11.core.utils.ActionRobot;

import ab.planner.TrajectoryPlanner;
import ab.vision.Vision;
import ab.vision.GameStateExtractor.GameState;


/**
 * <P>This is an abstract Angry Birds AI agent which provides the basic
 * framework for various implementations of AI's to play the Angry Birds game.
 * Implementation note; do not call {@code super()} in any implementing class
 * constructors as there are no constructors for this abstract class.
 * 
 * <H3>Usage and Implementation:</H3>
 * Required method(s) to implement in a super class:<UL>
 * <LI>{@code doTurn(Vision)}
 * </UL>
 * <P>Optional methods that can be implemented in a super class:<UL>
 * <LI>{@code onLost()}
 * <LI>{@code onWin()}
 * </UL>
 * <P>Additional helper methods for outputs:<UL>
 * <LI>{@code error(String)}
 * <LI>{@code debug(String)}
 * <LI>{@code log(String)}
 * </UL>
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 * @see java.lang.Runnable
 */
public abstract class Team11Agent implements Runnable {

	/**
	 * Specification of the maximum level for this implementation of the agent
	 * to play to. Once the {@link #MAX_LEVEL} is reached, the agent will start
	 * back from level 1 and continue playing.
	 */
	public static final int MAX_LEVEL = 21;
	
	
	/**
	 * Specifies whether this class has the additional debugging messages 
	 * printed out to the debug print stream or not.
	 */
	public static boolean DEBUGGING = false;
	
	
	/**
	 * The {@link PrintStream} to print the debug messages to. Defaults to
	 * {@code System.out}. Calling the {@link Team11Agent#debug(String)} method
	 * automatically prints the specified {@code String} to the stream using
	 * {@code println(String)}.
	 */
	protected static PrintStream debugPrintStream = System.out;
	
	
	/**
	 * The {@link PrintStream} to print the error messages to. Defaults to
	 * {@code System.err}. Calling the {@link Team11Agent#error(String)} method
	 * automatically prints the specified {@code String} to the stream using
	 * {@code println(String)}.
	 */
	protected static PrintStream errorPrintStream = System.err;
	
	
	/**
	 * The {@link PrintStream} to print the log messages to. Defaults to
	 * {@code System.out}. Calling the {@link Team11Agent#log(String)} method
	 * automatically prints the specified {@code String} to the stream using
	 * {@code println(String)}.
	 */
	protected static PrintStream logPrintStream = System.out;
	
	
	// Private member data
	private ActionRobot actionRobot = new ActionRobot();
	private TrajectoryPlanner trajectoryPlanner = new TrajectoryPlanner();

	private List<Bird> birdLineUp = null;
	
	private final int[] highScorePerLevel = new int[21];
	private int currentBirdLineUpIndex = 0;
	private int currentLevel = 1;
	private int currentLevelAttempts = 1;
	
	private boolean running = false;
	
	
	/**
	 * Returns {@code true} if this implementation of the agent is currently 
	 * running, playing the game; otherwise, returns {@code false} if the agent
	 * has not been started or and exception was caught while running.
	 * 
	 * @return {@code true} if the agent is running; otherwise, returns 
	 *         {@code false}.
	 */
	public final boolean isRunning() {
		return running;
	}
	
	
	/**
	 * Returns the current level this instance of the agent is playing.
	 * 
	 * @return  the current level this instance is playing.
	 */
	public final int getCurrentLevel() {
		return currentLevel;
	}

	
	/**
	 * Sets the current level for the agent to play to the specified level.
	 * 
	 * @param level the level to set to the current level to.
	 */
	public final void setCurrentLevel(int level) {
		currentLevel = level;
		currentLevelAttempts = 1;
		birdLineUp = null;
		currentBirdLineUpIndex = 0;
		actionRobot.loadLevel(level);
	}
	
	
	/**
	 * Returns the {@link ActionRobot} for this implementation of the agent.
	 * 
	 * @return the {@link ActionRobot} for this implementation of the agent.
	 */
	protected final ActionRobot getActionRobot() {
		return actionRobot;
	}
	
	
	/**
	 * Returns the {@link TrajectoryPlanner} used for the current level for this
	 * implementation of the agent.
	 * 
	 * @return the {@link TrajectoryPlanner} for the current level.
	 */
	protected final TrajectoryPlanner getTrajectoryPlanner() {
		return trajectoryPlanner;
	}

	
	/**
	 * Returns the focus point for the sling or center of the active bird. This
	 * focus point is used to create new {@link Shot}s as the {@code x} and
	 * {@code y} parameters.
	 * 
	 * @param sling the {@link Rectangle} of the location for the sling in the
	 *              game vision.
	 * 
	 * @return the focus point for the sling or center of the active bird.
	 */
	protected final Point getFocusPoint(Rectangle sling) {
		int currentLevel = getCurrentLevel();
		Point focus;
		
		if (Env.containsKey(currentLevel))
			focus = new Point( Env.get(currentLevel) );
		else
			focus = getTrajectoryPlanner().getReferencePoint(sling);
		
		return focus;
	}
	
	
	/**
	 * Returns the line up of the birds in order that they will be fired in.
	 * 
	 * @return a {@link List} of {@link Bird}s in their correct firing order.
	 */
	protected final List<Bird> getBirdLineUp() {
		return birdLineUp;
	}
	
	
	/**
	 * Returns the next {@link Bird} to be fire or {@code null} if there
	 * are no birds left.
	 * 
	 * @return the next {@link Bird} to be fired.
	 */
	protected final Bird getNextBird() {
		if (birdLineUp == null || currentBirdLineUpIndex >= birdLineUp.size())
			return null;
		else
			return birdLineUp.get(currentBirdLineUpIndex);
	}
	
	
	/**
	 * Returns a list of the line up of birds to fire/shoot in the order they 
	 * will become available for the current level. This list will always 
	 * contain the list of all birds even after they are shot but will have a 
	 * flag specifying that they have been shot already.
	 * 
	 * @param vision the {@link Vision} of the current level being played.
	 * 
	 * @return the {@link java.util.List} of {@link Bird}s in order they can be
	 *         shot at the target.
	 */
	private final synchronized List<Bird> getBirdLineUp(Vision vision) {
		List<Bird> birds = Collections.synchronizedList(new LinkedList<Bird>());
		Rectangle sling = vision.findSlingshot();
		if (sling == null)
			return birds;
		
		// TODO need to fix and only find birds relative to the sling shot and
		//      determine which side of the sling shot the birds are lined up on
		//      to determine the ordering of birds in the list.
		
		for (Rectangle rect : vision.findRedBirds())
			birds.add( new RedBird(rect) );
		
		for (Rectangle rect : vision.findBlueBirds())
			birds.add( new BlueBird(rect) );
		
		for (Rectangle rect : vision.findYellowBirds())
			birds.add( new YellowBird(rect) );
		
		for (Rectangle rect : vision.findBlackBirds())
			birds.add( new BlackBird(rect) );
		
		for (Rectangle rect : vision.findWhiteBirds())
			birds.add( new WhiteBird(rect) );
		
		// Remove any outside of the relative proximity of the sling...
		double tolerance = 10d;
		Iterator<Bird> it = birds.iterator();
		Bird bird = null;
		while (it.hasNext()) {
			bird = it.next();
			if (bird.getMaxY() > sling.getMaxY() + tolerance
					|| bird.getMinY() < sling.getMinY() - tolerance)
				it.remove();//birds.remove(bird);
			
			// if lined up on the left of the sling...
			else if (bird.getMaxX() > sling.getMaxX() + tolerance)
				it.remove();//birds.remove(bird);
			
			// TODO if lined up on the right of the sling...
			//if (bird.getMinX() < sling.getMinX() - tolerance)
			//	birds.remove(bird);
		}
		
		Collections.sort(birds, new Comparator<Bird>() {

			@Override
			public int compare(Bird b1, Bird b2) {
				assert(b1 != null && b2 != null);
				return (int)b2.getCenterX() - (int)b1.getCenterX();
			}
			
		});
		
		return birds;
	}
	
	
	/**
	 * Returns the current {@link GameState} from the {@link ActionRobot} of
	 * this implementation of the agent.
	 * 
	 * @return the current {@link GameState} of the game.
	 * 
	 * @see ab.vision.GameStateExtractor.GameState
	 * @see team11.core.utils.ActionRobot#checkState()
	 */
	protected final GameState getState() {
		return ActionRobot.getCurrentGameState();
	}

	
	/**
	 * The run method is called to start the agent and connect it to the server
	 * to begin playing the game. This should loop continuously as long as the
	 * AI is playing the game, checking the game state.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();
		
		running = true;
		setCurrentLevel(currentLevel);
		
		try {
			while (true) {
				GameState state = processTurn();
				if (state == GameState.WON)
					processGameStateWon();
				else if (state == GameState.LOST)
					processGameStateLost();
				else if (state == GameState.LEVEL_SELECTION)
					processLevelSelection();
				else if (state == GameState.MAIN_MENU)
					processMainMenu();
				else if (state == GameState.EPISODE_MENU)
					processEpisodeMenu();
			}
		}
		catch (Exception ex) {
			debug(" !> Exception caught while running: " + ex);
			ex.printStackTrace(debugPrintStream);
			running = false;
		}
	}
	
	
	/**
	 * Processes the turn for this implementation of the agent and calls the
	 * {@code doTurn(Vision)} method that should be overwritten.
	 * 
	 * @return the {@link GameState} after the turn has been completed.
	 * 
	 * @see #doTurn(Vision)
	 */
	private final GameState processTurn() {
		BufferedImage screenshot = ActionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);

		Rectangle sling = vision.findSlingshot();

		while (sling == null && ActionRobot.isPlaying()) {
			debug("  !> No slingshot detected. Please remove any pop-ups!");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshot();
		}
		
		if (birdLineUp == null)
			birdLineUp = getBirdLineUp(vision);

		ActionRobot.sleep(1000);
		List<Shot> shots = doTurn(vision);
		
		return fireShots(sling, shots);
	}
	
	
	/**
	 * Fires the specified {@link Shot}s and uses the current sling reference
	 * point to adjust the trajectory planner with the actual trajectory path
	 * the bird that was fired took.
	 * 
	 * @param sling the {@link Rectangle} specifying the bounds for the 
	 *              sling shot in the current vision.
	 * @param shots the {@link java.util.List List} of {@link Shot}s to be fired
	 *              at targets.
	 *              
	 * @return the {@link GameState} of the game after the shots were fired.
	 * 
	 * @see ab.demo.other.Shot
	 * @see ab.vision.GameStateExtractor.GameState
	 */
	private final GameState fireShots(Rectangle sling, List<Shot> shots) {
		GameState state = getState();
		
		ActionRobot.fullyZoomOut();
		
		BufferedImage screenshot = ActionRobot.doScreenShot();
		Vision vision = new Vision(screenshot);
		Rectangle slingAfterShot = vision.findSlingshot();

		if (slingAfterShot != null && slingAfterShot.equals(sling)) {
			debug(" -> *** Shoot! ***");
			state = getActionRobot().shootWithStateInfoReturned(shots);
			
			if (state == GameState.PLAYING) {
				List<Point> traj = vision.findTrajPoints();
				
				if (traj != null && traj.size() > 0)
					getTrajectoryPlanner()
						.adjustTrajectory(traj, sling, traj.get(0));
			}
		}
		//else
		//	debug(" -- Scale changed, will re-segment the image.");
		
		return state;
	}
	
	
	/**
	 * This method is used to provide the AI implementation for each turn, time
	 * to fire a bird at some target, that returns a {@link java.util.List List}
	 * of {@link Shot}s to fire. If there are no shots that 
	 * can be taken, this must return a {@link java.util.List List} with size 0.
	 * 
	 * @param vision the {@link ab.vision.Vision Vision} of the current level
	 *               for the game.
	 *               
	 * @return the {@link List} of {@link Shot}s to fire.
	 * 
	 * @see team11.core.other.Shot
	 * @see ab.vision.Vision
	 */
	protected abstract List<Shot> doTurn(Vision vision);

	
	/**
	 * Called when the game state registers as being {@code WON}. This method
	 * captures the final score, calls to print the winning statistics to the
	 * log, calls the {@code onWin()} method, and continues to the next level.
	 * If the maximum level was reached, this will start at the first level
	 * again.
	 */
	private final void processGameStateWon() {
		int score = -2;
		ActionRobot.sleep(3000);
		
		while (score != ActionRobot.getCurrentScore()) {
			ActionRobot.sleep(500);
			score = ActionRobot.getCurrentScore();
		}
		
		if (highScorePerLevel[currentLevel - 1] < score)
			highScorePerLevel[currentLevel - 1] = score;
		
		printStatsToLog(true, score);
		onWin();
		
		if (currentLevel < MAX_LEVEL)
			setCurrentLevel(++currentLevel);
		else
			setCurrentLevel(currentLevel = 1);
		
		trajectoryPlanner = new TrajectoryPlanner();
	}
	
	
	/**
	 * This method is called once the score has been fully counted when the 
	 * agent successfully completes a level and after the log has been appended
	 * to. This method maybe overridden by a super class to perform custom 
	 * implementations on winning a level.
	 */
	public void onWin() { }

	
	/**
	 * Creates a log {@code String} to write out to the log print stream once a
	 * level is completed successfully.
	 * 
	 * @param score  the final score of the level completed.
	 */
	private final void printStatsToLog(boolean won, int score) {
		String msg = "'" + getClass().getSimpleName() + "',"
				+ currentLevel + ","
				+ currentLevelAttempts + ","
				+ score + ","
				+ won + ","
				+ System.currentTimeMillis();
		
		debug(" -> " + (won ? "WON" : "LOST") + ": " + msg);
		log(msg);
	}
	
	
	/**
	 * Called when the game state registers as being {@code LOST}. This method
	 * re-initializes the per level parameters, increments the number of 
	 * attempts for the current level, and requests from the 
	 * {@code ActionRobot} to restart the current level. This will also call
	 * the {@code onLost()} method.
	 */
	private final void processGameStateLost() {
		printStatsToLog(false, -1);
		debug(" -> LOST: Restarting level " + currentLevel + "...");
		
		onLost();
		
		birdLineUp = null;
		currentBirdLineUpIndex = 0;
		currentLevelAttempts++;
		actionRobot.restartLevel();
	}
	
	
	/**
	 * This method is called when the agent fails to complete a level and 
	 * before the level is attempted again. This method maybe overridden by a 
	 * super class to perform custom implementations on losing a level.
	 */
	public void onLost() { }
	
	
	/**
	 * Called when the game state registers as being the current screen at the
	 * episode menu in the game. This method attempts to set the current level
	 * and continue playing.
	 */
	private final void processEpisodeMenu() {
		debug(" !> Unexpected episode menu page, resetting current level to "
				+ currentLevel + "...");
		ActionRobot.GoFromMainMenuToLevelSelection();
		setCurrentLevel(currentLevel);
	}


	/**
	 * Called when the game state registers as being the current screen at the
	 * main menu of the game. This method goes to the level selection menu and
	 * sets the current level.
	 */
	private final void processMainMenu() {
		debug(" !> Unexpected main menu page, resettng current level to "
				+ currentLevel + "...");
		ActionRobot.GoFromMainMenuToLevelSelection();
		setCurrentLevel(currentLevel);
	}


	/**
	 * Called when the game state registers as being the current screen at the
	 * level selection menu. This method selects the current level and continues
	 * playing.
	 */
	private final void processLevelSelection() {
		debug(" !> Unexpected level selection page, resetting current level to "
				+ currentLevel + "...");
		setCurrentLevel(currentLevel);
	}
	

	/**
	 * Print the specified {@link String} to the error {@link PrintStream} 
	 * defined by the {@code errorPrintStream} field. This will automatically
	 * include the appropriate newline character at the end of the string.
	 * 
	 * <P><B>Note:</B> This is the same as calling;
	 * <PRE>errorPrintStream.println(String);</PRE>
	 * 
	 * @param err the {@link String} message to print to the error print stream.
	 * 
	 * @see #errorPrintStream
	 */
	protected static final void error(String err) {
		errorPrintStream.println(err + "");
	}
	

	/**
	 * Print the specified {@link String} to the debug {@link PrintStream} 
	 * defined by the {@code debugPrintStream} field. This will automatically
	 * include the appropriate newline character at the end of the string.
	 * 
	 * <P><B>Note:</B> This is the same as calling;
	 * <PRE>debugPrintStream.println(String);</PRE>
	 * 
	 * @param msg the {@link String} message to print to the debug print stream.
	 * 
	 * @see #debugPrintStream
	 */
	protected static final void debug(String msg) {
		debugPrintStream.println(msg + "");
	}
	
	
	/**
	 * Print the specified {@link String} to the log {@link PrintStream} 
	 * defined by the {@code logPrintStream} field. This will automatically
	 * include the appropriate newline character at the end of the string.
	 * 
	 * <P><B>Note:</B> This is the same as calling;
	 * <PRE>logPrintStream.println(String);</PRE>
	 * 
	 * @param msg the {@link String} message to print to the log print stream.
	 * 
	 * @see #logPrintStream
	 */
	protected static final void log(String msg) {
		logPrintStream.println(msg + "");
	}
	
	
	/**
	 * Sets the log to the specified file to append any calls to the
	 * {@code log(String)} method.
	 * 
	 * @param logFile  the {@link File} for the log data to be appended to.
	 */
	public static final void setLogFile(File logFile) {
		if (logFile == null)
			return;

		try {
			FileOutputStream fos = new FileOutputStream(logFile, true);
			logPrintStream = new PrintStream(fos);
		}
		catch (FileNotFoundException e) {
			error("Unable to set the log output to the file specified: " + e);
			e.printStackTrace(debugPrintStream);
		}
	}
	
	
	/**
	 * Ensures that the log print stream was flushed and closed properly and 
	 * continues by calling the {@code super.finalize()} method.
	 * 
	 * @throws Throwable any {@code Exception} raised by this method.
	 */
	@Override
	protected void finalize() throws Throwable {
		if (logPrintStream != null && logPrintStream != System.out) {
			logPrintStream.flush();
			logPrintStream.close();
			logPrintStream = System.out;
		}
		super.finalize();
	}
	
}
