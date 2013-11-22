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
package team11.core.utils;

import team11.core.other.Shot;
import team11.core.schema.LoadingLevelSchema;
import team11.core.schema.RestartLevelSchema;
import team11.core.schema.ShootingSchema;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import javax.imageio.ImageIO;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyClickMessage;
import ab.server.proxy.message.ProxyDragMessage;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.vision.GameStateExtractor;
import ab.vision.GameStateExtractor.GameState;

/**
 * This class provides an interface layer between the agents and the server
 * which is the extension enabled in the Google's Chrome web browser.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 */
public class ActionRobot {

	/**
	 * Integer that represents the mouse wheel action of scrolling upwards.
	 */
	public static final int MOUSE_WHEEL_UP = -1;
	
	
	/**
	 * Integer that represents the mouse wheel action of scrolling downwards.
	 */
	public static final int MOUSE_WHEEL_DOWN = 1;
	
	
	/**
	 * The {@link Proxy} connection for this implementation to communicate to 
	 * the Angry Birds server extension enabled in Google's Chrome web browser.
	 */
	public static Proxy proxy;
	
	
	/**
	 * The {@link Proxy}'s port number to connect through.
	 */
	public static int proxyPort = 9000;
	
	
	// Member data.
	private LoadingLevelSchema levelLoader = null;
	private RestartLevelSchema levelRestart = null;
	
	private static final GameStateExtractor extractor =
			new GameStateExtractor();

	
	/**
	 * Constructor for an instance of this {@link ActionRobot}. If the 
	 * {@link Proxy}, static {@code proxy}, connection has not been initialized
	 * this will initialize it on the port number, static {@code proxyPort}.
	 */
	public ActionRobot() {
		if (proxy == null)
			initializeProxy();
		
		levelLoader = new LoadingLevelSchema();
		levelRestart = new RestartLevelSchema();
	}
	
	
	/**
	 * Called to initialize the {@link Proxy} connection for all instances of 
	 * the {@link ActionRobot} class.
	 */
	private final void initializeProxy() {
		try {
			proxy = new Proxy(proxyPort) {
				
				@Override
				public void onOpen() {
					System.out.println(" <O> Client connected");
				}

				@Override
				public void onClose() {
					System.out.println(" <!> Client disconnected");
				}
				
			};
			proxy.start();

			System.out.println(" --> Server started on port: " + proxy.getPort());
			System.out.println(" <-- Waiting for client to connect");
			
			proxy.waitForClients(1);
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Replaced with {@code doClick()}.
	 */
	@Deprecated
	public void click() {
		doClick();
	}
	
	
	/**
	 * Sends a click message through the proxy to the game server to simulate a
	 * mouse click at (0, 0). And returns the {@link Object} returned from the
	 * {@code sendProxyClickMessage} method.
	 * 
	 * @return  the {@link Object} returned from sending the click message to
	 *          the proxy.
	 * 
	 * @see #sendProxyClickMessage(int, int)
	 */
	public static final Object doClick() {
		return sendProxyClickMessage(0, 0);
	}
	
	
	/**
	 * Sends a click message through the proxy to the game server to simulate a
	 * mouse click at ({@code x}, {@code y}). And returns the {@link Object}
	 * returned from the {@code sendProxyClickMessage} method.
	 * 
	 * @param x the X-coordinate to click at.
	 * @param y the Y-coordinate to click at.
	 * 
	 * @return  the {@link Object} returned from sending the click message to
	 *          the proxy.
	 * 
	 * @see #sendProxyClickMessage(int, int)
	 */
	public static final Object doClick(int x, int y) {
		return sendProxyClickMessage(x, y);
	}
	
	
	/**
	 * Sends a click message through the proxy to the game server to simulate a
	 * mouse click at the specified {@code Point}. And returns the
	 * {@link Object} returned from the {@code sendProxyClickMessage} method.
	 * 
	 * @param pt the {@link Point} to click at.
	 * 
	 * @return  the {@link Object} returned from sending the click message to
	 *          the proxy.
	 * 
	 * @see #sendProxyClickMessage(int, int)
	 */
	public static final Object doClick(Point pt) {
		return pt != null ? doClick(pt.x, pt.y): doClick(); 
	}
	
	
	/**
	 * Replaced with {@code doDrag()}.
	 */
	@Deprecated
	public void drag() {
		proxy.send( new ProxyDragMessage(0, 0, 0, 0) );
	}
	
	
	/**
	 * Sends a mouse drag message through the proxy to the game server to
	 * simulate a mouse being dragged from (0, 0) to (0, 0). And returns the
	 * {@link Object} returned from the {@code sendProxyDragMessage} method.
	 * 
	 * @return  the {@link Object} returned from sending the drag message to
	 *          the proxy.
	 * 
	 * @see #sendProxyDragMessage(int, int, int, int)
	 */
	public static final Object doDrag() {
		return sendProxyDragMessage(0, 0, 0, 0);
	}
	
	
	/**
	 * Sends a mouse drag message through the proxy to the game server to
	 * simulate a mouse being dragged from ({@code x}, {@code y}) to
	 * ({@code x + dx}, {@code y + dy}). And returns the {@link Object} returned
	 *  from the {@code sendProxyDragMessage} method.
	 * 
	 * @param x  the X-coordinate to start dragging from.
	 * @param y  the Y-coordinate to start dragging from.
	 * @param dx the change in the X-coordinates to drag to.
	 * @param dy the change in the Y-coordinates to drag to.
	 * 
	 * @return  the {@link Object} returned from sending the drag message to
	 *          the proxy.
	 * 
	 * @see #sendProxyDragMessage(int, int, int, int)
	 */
	public static final Object doDrag(int x, int y, int dx, int dy) {
		return sendProxyDragMessage(x, y, dx, dy);
	}
	
	
	/**
	 * Sends a mouse drag message through the proxy to the game server to
	 * simulate a mouse being dragged from {@link Point} {@code p1} to
	 * {@link Point} {@code p2}. And returns the {@link Object} returned from
	 * the {@code sendProxyDragMessage} method.
	 * 
	 * @param p1  the {@link Point} to starting dragging from.
	 * @param p2  the {@link Point} to drag to.
	 * 
	 * @return  the {@link Object} returned from sending the drag message to
	 *          the proxy.
	 * 
	 * @see #sendProxyDragMessage(int, int, int, int)
	 */
	public static final Object doDrag(Point p1, Point p2) {
		if (p1 == null || p2 == null)
			throw new NullPointerException(
					"Unable to drag, point(s) are null.");
		
		return sendProxyDragMessage(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
	}
	
	
	/**
	 * Returns the {@link Object} returned from sending the a mouse wheel
	 * message through to the game server to simulate the mouse wheel scrolling
	 * in the "up" direction.
	 * 
	 * @return  the {@link Object} returned from the proxy for the mouse wheel
	 *          message requested.
	 * 
	 * @see #MOUSE_WHEEL_UP
	 * @see #sendProxyMouseWheelMessage(int)
	 */
	public static final Object doMouseWheelUp() {
		return sendProxyMouseWheelMessage(MOUSE_WHEEL_UP);
	}
	
	
	/**
	 * Returns the {@link Object} returned from sending the a mouse wheel
	 * message through to the game server to simulate the mouse wheel scrolling
	 * in the "down" direction.
	 * 
	 * @return  the {@link Object} returned from the proxy for the mouse wheel
	 *          message requested.
	 * 
	 * @see #MOUSE_WHEEL_DOWN
	 * @see #sendProxyMouseWheelMessage(int)
	 */
	public static final Object doMouseWheelDown() {
		return sendProxyMouseWheelMessage(MOUSE_WHEEL_DOWN);
	}

	
	/**
	 * Returns a {@link BufferedImage} of the screenshot for the Angry Birds
	 * game by calling the {@code sendProxyScreenshotMessage()} and building
	 * the image based on the byte array returned.
	 * 
	 * @return  a {@link BufferedImage} of the current screenshot of the game.
	 * 
	 * @see #sendProxyScreenshotMessage()
	 */
	public static final synchronized BufferedImage doScreenShot() {
		byte[] imageBytes = sendProxyScreenshotMessage();
		BufferedImage image = null;
		
		try {
			image = ImageIO.read( new ByteArrayInputStream(imageBytes) );
		}
		catch (IOException ignore) { }

		return image;
	}
	
	
	/**
	 * Returns the {@link GameState} of the game after shooting the specified
	 * {@link Shot}s contained in the {@link java.util.List List}<{@link Shot}>
	 * parameter.
	 * 
	 * @param shots  the {@link java.util.List List} of type {@link Shot} to be
	 *               fired.
	 * 
	 * @return  the {@link GameState} after firing the specified {@link Shot}s.
	 */
	public GameState shootWithStateInfoReturned(List<Shot> shots) {
		new ShootingSchema().shoot(shots);
		return getCurrentGameState();
	}


	/**
	 * Attempts to load the specified level through the load level schema.
	 * 
	 * @param level  the {@code int} specifying the level to attempt to load.
	 * 
	 * @see team11.core.schema.LoadingLevelSchema#loadLevel(int)
	 * 
	 */
	public void loadLevel(int level) {
		if (levelLoader != null)
			levelLoader.loadLevel(level);
	}
	
	
	/**
	 * Returns {@code true} when the current game state is set to "playing".
	 * This is the same as calling: {@code isGameState(GameState.PLAYING)}.
	 * 
	 * @return  {@code true} when the current game state is set to "playing";
	 *          otherwise, returns {@code false}.
	 * 
	 * @see ab.vision.GameStateExtractor.GameState#PLAYING
	 */
	public static final boolean isPlaying() {
		return isGameState(GameState.PLAYING);
	}
	
	
	/**
	 * Returns {@code true} if the specified {@link GameState} is equal to the
	 * game state "playing", {@code GameState.PLAYING}.
	 * 
	 * @param state  the game state to be checked if "playing".
	 * 
	 * @return  {@code true} if the specified game state is "playing".
	 * 
	 * @see ab.vision.GameStateExtractor.GameState#PLAYING
	 */
	public static final boolean isPlaying(GameState state) {
		return state == GameState.PLAYING;
	}
	
	
	/**
	 * Returns {@code true} when the current game state is set to "won". This is
	 * the same as calling: {@code isGameState(GameState.WON)}.
	 * 
	 * @return  {@code true} when the current game state is set to "won";
	 *          otherwise, returns {@code false}.
	 * 
	 * @see ab.vision.GameStateExtractor.GameState#WON
	 */
	public static final boolean isWon() {
		return isGameState(GameState.WON);
	}
	
	
	/**
	 * Returns a {@code boolean} value if the game state is "playing" after
	 * waiting for three attempts and sleeping between each for roughly three
	 * seconds. If the game state is set to "playing" initially this will return
	 * immediately with the value of {@code true}.
	 * 
	 * @return  {@code true} when the game state is set to "playing" after
	 *          waiting for multiple attempts.
	 */
	public static final synchronized boolean waitForPlayingState() {
		for (int i = 0; !isPlaying() && i < 3; i++)
			sleep(3000);
		
		return isPlaying();
	}

	
	/**
	 * Replaced with {@code doDrag()}.
	 */
	@Deprecated
	public static synchronized GameState checkState() {
		// Old way: return StateUtils.checkCurrentState(proxy);
		return getCurrentGameState();
	}
	
	
	/**
	 * Returns the current game state after grabbing a new screenshot to
	 * analyze from the {@link GameStateExtractor}. This method will create a
	 * new instance of the {@link GameStateExtractor} and capture a new instance
	 * of the screen with {@code doScreenShot()}.
	 * 
	 * @return  the current {@link GameState}.
	 */
	public static final synchronized GameState getCurrentGameState() {
		return extractor.getGameState( doScreenShot() );
	}
	
	
	/**
	 * Returns the current game score from either the game "playing" or "won"
	 * states. If the game state does not contain a current score, then
	 * {@code -1} will be returned.
	 * 
	 * @return  the current score of the game from either the "playing" or "won"
	 *          game states.
	 */
	public static final synchronized int getCurrentScore() {
		BufferedImage screenshot = doScreenShot();
		int score = -1;
		
		GameState state = extractor.getGameState(screenshot);
		
		if (state == GameState.PLAYING)
			score = extractor.getScoreInGame(screenshot);
		else if (state == GameState.WON)
			score = extractor.getScoreEndGame(screenshot);
		
		if (score == -1)
			// TODO need to remove or re-work...
			;//System.out.println(" the game score is unavailable "); 

		return score;
	}

	
	/**
	 * Attempts to zoom the game view out so that the entire game is visible.
	 * This method is equivalent to calling: {@code zoomOut(10)}.
	 * 
	 * @see #zoomOut(int)
	 */
	public static final void fullyZoomOut() {
		zoomOut(10);
	}
	
	
	/**
	 * Attempts to zoom out by invoking the {@code #doMouseWheelUp()} action
	 * repetitively by the specified {@code scrollCount}.
	 * 
	 * @param scrollCount the {@code int} number of times to call the
	 *                    {@code #doMouseWheelUp()} method.
	 *                    
	 * @see #doMouseWheelUp()
	 */
	public static final void zoomOut(int scrollCount) {
		if (isPlaying()) {
			for (int zoom = 0; zoom < scrollCount; zoom++)
				doMouseWheelUp();
			
			sleep(3000);
		}
	}


	/**
	 * Attempts to restart the current level in the Angry Birds game.
	 */
	public void restartLevel() {
		if (levelRestart != null)
			levelRestart.restartLevel();
	}
	
	
	/**
	 * Returns {@code true} if the current game state of the Angry Birds game
	 * equals the specified game state. If the game state passed is {@code null}
	 * or any other game state than the current game state of the game.
	 * 
	 * @param state  the game state to compare against the current game state.
	 * 
	 * @return  {@code true} if the current game state equals the specified game
	 *          state; otherwise, returns {@code false}.
	 * 
	 * @see ab.vision.GameStateExtractor.GameState
	 */
	public static final boolean isGameState(GameState state) {
		return getCurrentGameState() == state;
	}

	
	/**
	 * Method to move from the main menu in the Angry Birds game to the episode
	 * selection menu and proceed to the level selection menu.
	 */
	public static final void GoFromMainMenuToLevelSelection() {
		while (isGameState(GameState.MAIN_MENU)) {
			doClick(305, 277);
			sleep(1000);
		}

		while (isGameState(GameState.EPISODE_MENU)) {
			doClick(150, 300);
			sleep(1000);
		}
	}
	
	
	/**
	 * Sends a mouse click message through to the game server to simulate a
	 * mouse being clicked at the specified point, ({@code x}, {@code y}).
	 * 
	 * @param x  the X-coordinate to click at.
	 * @param y  the Y-coordinate to click at.
	 * 
	 * @return  the {@link Object} returned from the proxy for the mouse click
	 *          message requested.
	 * 
	 * @throws NullPointException if the proxy is not defined.
	 * @see ab.server.proxy.message.ProxyClickMessage
	 */
	public static final Object sendProxyClickMessage(int x, int y) {
		if (proxy == null)
			throw new NullPointerException("Proxy not defined.");
		
		return proxy.send( new ProxyClickMessage(x, y) );
	}
	
	
	/**
	 * Sends a mouse drag message through to the game server to simulate a mouse
	 * click and drag action. The mouse should be pressed at the specified
	 * ({@code x}, {@code y}) coordinates and dragged by the change in the X
	 * direction, {@code dx}, and Y direction, {@code dy}.
	 * 
	 * @param x   the initial X-coordinate to drag from.
	 * @param y   the initial Y-coordinate to drag from.
	 * @param dx  the change in the X direction from the initial point.
	 * @param dy  the change in the Y direction from the initial point.
	 * 
	 * @return  the {@link Object} returned from the proxy for the mouse drag
	 *          message requested.
	 * 
	 * @throws NullPointException if the proxy is not defined.
	 * @see ab.server.proxy.message.ProxyClickMessage
	 */
	public static final Object sendProxyDragMessage(int x, int y, int dx,
			int dy)
	{
		if (proxy == null)
			throw new NullPointerException("Proxy not defined.");
		
		return proxy.send( new ProxyDragMessage(x, y, dx, dy) );
	}
	
	
	/**
	 * Sends the a mouse wheel message through to the game server to simulate 
	 * the mouse wheel scrolling in the direction specified. The direction is
	 * an {@code int} specifying which direction to scroll the mouse wheel. As
	 * long as the value is greater than zero, the {@link #MOUSE_WHEEL_DOWN}
	 * field is used. And if the value is less than zero, the
	 * {@link #MOUSE_WHEEL_UP} field is used. If the value equals zero, nothing 
	 * is sent to the proxy server and {@code null} is returned.
	 * 
	 * @param direction an {@code int} specifying the direction to scroll the
	 *                  mouse wheel.
	 * 
	 * @return  the {@link Object} returned from the proxy for the mouse wheel
	 *          message requested.
	 * 
	 * @throws NullPointException if the proxy is not defined.
	 * @see ab.server.proxy.message.ProxyMouseWheelMessage
	 */
	public static final Object sendProxyMouseWheelMessage(int direction) {
		if (proxy == null)
			throw new NullPointerException("Proxy not defined.");
		
		if (direction > 0)
			direction = MOUSE_WHEEL_DOWN;
		else if (direction < 0)
			direction = MOUSE_WHEEL_UP;
		else
			return null;
		
		return proxy.send( new ProxyMouseWheelMessage(direction) );
	}
	
	
	/**
	 * Sends a screenshot message request through to the game server to take a
	 * screenshot of the current game viewable area. This returns the
	 * {@code byte} array for the screenshot.
	 * 
	 * @return  the array of {@code byte}s which makes up the screenshot image.
	 * 
	 * @throws NullPointException if the proxy is not defined.
	 * @see ab.server.proxy.message.ProxyScreenshotMessage
	 */
	public static final byte[] sendProxyScreenshotMessage() {
		if (proxy == null)
			throw new NullPointerException("Proxy not defined.");
		
		return proxy.send( new ProxyScreenshotMessage() );
	}
	
	
	/**
	 * Attempts to sleep the current {@code Thread} for the specified number of
	 * milliseconds. No exceptions are thrown if interrupted and if the number
	 * of milliseconds is less than or equal to zero.
	 * 
	 * @param millis  the number of milliseconds to sleep for.
	 */
	public static final void sleep(long millis) {
		try {
			if (millis > 0)
				Thread.sleep(millis);
		}
		catch (InterruptedException ignore) { }
	}

}
