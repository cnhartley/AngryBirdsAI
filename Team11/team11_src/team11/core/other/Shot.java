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
package team11.core.other;

import java.awt.Point;
import java.util.Comparator;

/**
 * Provides a shot data structure to hold the initial focus point, the change 
 * in the X and Y coordinates to drag to, and the times to both shot the bird
 * and tap the screen.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 */
public class Shot {
	
	// Member data.
	private int x;
	private int y;
	
	private int dx;
	private int dy;
	
	private long shootTime;
	private long tapTime;
	
	
	/**
	 * Constructor for an instance of the shot class with all values set to
	 * zero. This is the same as calling the {@code Shot(0, 0, 0, 0, 0, 0)}
	 * constructor.
	 */
	public Shot() {
		this(0, 0, 0, 0, 0l, 0l);
	}
	
	
	/**
	 * Constructor for an instance of the shot based on the specified shot. 
	 * This effectively clones all of the parameters of the {@code anotherShot}
	 * into this new instance.
	 * 
	 * @param anotherShot  another {@link Shot} to capture the parameters from.
	 */
	public Shot(Shot anotherShot) {
		this(anotherShot.x, anotherShot.y, anotherShot.dx, anotherShot.dy,
				anotherShot.shootTime, anotherShot.tapTime);
	}


	/**
	 * Constructor for an instance of the shot with the specified focus point,
	 * {@code pt}, the offset time to shoot this particular shot at, and the 
	 * delay offset from the shot time to simulate a tap on the screen.
	 * 
	 * @param pt        the focus {@link Point} to start the shot at.
	 * @param shootTime the offset time in milliseconds to shoot at.
	 * @param tapTime   the delay from the shoot time in milliseconds to tap at.
	 * 
	 * @see java.awt.Point
	 */
	public Shot(Point pt, long shootTime, long tapTime) {
		this(pt.x, pt.y, 0, 0, shootTime, tapTime);
	}
	
	
	/**
	 * Constructor for an instance of the shot with the specified focus point,
	 * at ({@code x}, {@code y}), the offset time to shoot this particular shot
	 * at, and the delay offset from the shot time to simulate a tap on the
	 * screen.
	 * 
	 * @param x         the {@code int} X-coordinate for the focus point to 
	 *                  start the shot at.
	 * @param y         the {@code int} Y-coordinate for the focus point to 
	 *                  start the shot at.
	 * @param shootTime the offset time in milliseconds to shoot at.
	 * @param tapTime   the delay from the shoot time in milliseconds to tap at.
	 */
	public Shot(int x, int y, long shootTime, long tapTime) {
		this(x, y, 0, 0, shootTime, tapTime);
	}
	
	
	/**
	 * Constructor for an instance of the shot with the specified focus point,
	 * {@code pt}, the change in the X-coordinate to drag to, the change in the
	 * Y-coordinate to drag to, and the offset time to shoot this particular
	 * shot at.
	 * 
	 * @param pt        the focus {@link Point} to start the shot at.
	 * @param dx        the {@code int} change in X-coordinates to release the
	 *                  shot from. 
	 * @param dy        the {@code int} change in Y-coordinates to release the
	 *                  shot from.
	 * @param shootTime the offset time in milliseconds to shoot at.
	 * 
	 * @see java.awt.Point
	 */
	public Shot(Point pt, int dx, int dy, long shootTime) {
		this(pt.x, pt.y, 0, 0, shootTime, 0l);
	}
	
	
	/**
	 * Constructor for an instance of the shot with the specified focus point,
	 * at ({@code x}, {@code y}), the change in the X-coordinate to drag to, 
	 * the change in the Y-coordinate to drag to, and the offset time to shoot
	 * this particular shot at.
	 * 
	 * @param x         the {@code int} X-coordinate for the focus point to 
	 *                  start the shot at.
	 * @param y         the {@code int} Y-coordinate for the focus point to 
	 *                  start the shot at.
	 * @param dx        the {@code int} change in X-coordinates to release the
	 *                  shot from. 
	 * @param dy        the {@code int} change in Y-coordinates to release the
	 *                  shot from.
	 * @param shootTime the offset time in milliseconds to shoot at.
	 */
	public Shot(int x, int y, int dx, int dy, long shootTime) {
		this(x, y, dx, dy, shootTime, 0l);
	}
	
	
	/**
	 * Constructor for an instance of the shot with the specified focus point,
	 * {@code pt},the change in the X-coordinate to drag to, the change in the
	 * Y-coordinate to drag to, the offset time to shoot this particular shot
	 * at, and the delay offset from the shot time to simulate a tap on the
	 * screen.
	 * 
	 * @param pt        the focus {@link Point} to start the shot at.
	 * @param dx        the {@code int} change in X-coordinates to release the
	 *                  shot from. 
	 * @param dy        the {@code int} change in Y-coordinates to release the
	 *                  shot from.
	 * @param shootTime the offset time in milliseconds to shoot at.
	 * @param tapTime   the delay from the shoot time in milliseconds to tap at.
	 * 
	 * @see java.awt.Point
	 */
	public Shot(Point pt, int dx, int dy, long shootTime, long tapTime) {
		this(pt.x, pt.y, dx, dy, shootTime, tapTime);
	}
	
	
	/**
	 * Constructor for an instance of the shot with the specified focus point,
	 * at ({@code x}, {@code y}), the change in the X-coordinate to drag to, 
	 * the change in the Y-coordinate to drag to, the offset time to shoot this
	 * particular shot at, and the delay offset from the shot time to simulate
	 * a tap on the screen.
	 * 
	 * @param x         the {@code int} X-coordinate for the focus point to 
	 *                  start the shot at.
	 * @param y         the {@code int} Y-coordinate for the focus point to 
	 *                  start the shot at.
	 * @param dx        the {@code int} change in X-coordinates to release the
	 *                  shot from. 
	 * @param dy        the {@code int} change in Y-coordinates to release the
	 *                  shot from.
	 * @param shootTime the offset time in milliseconds to shoot at.
	 * @param tapTime   the delay from the shoot time in milliseconds to tap at.
	 */
	public Shot(int x, int y, int dx, int dy, long shootTime, long tapTime) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.shootTime = shootTime;
		this.tapTime = tapTime;
	}
	
	
	/**
	 * Returns the change in the X-coordinates from the focus point to 
	 * calculate the release point from.
	 * 
	 * @return the {@code int} change in the X-coordinates from the focus point.
	 */
	public int getDeltaX() {
		return dx;
	}


	/**
	 * Sets the change in the X-coordinates from the focus point to calculate
	 * the release point.
	 * 
	 * @param dx  the {@code int} change in X-coordinates to release the shot 
	 *            from.
	 */
	public void setDeltaX(int dx) {
		this.dx = dx;
	}


	/**
	 * Returns the change in the Y-coordinates from the focus point to 
	 * calculate the release point from.
	 * 
	 * @return the {@code int} change in the Y-coordinates from the focus point.
	 */
	public int getDeltaY() {
		return dy;
	}

	
	/**
	 * Sets the change in the Y-coordinates from the focus point to calculate
	 * the release point.
	 * 
	 * @param dy  the {@code int} change in Y-coordinates to release the shot 
	 *            from.
	 */
	public void setDeltaY(int dy) {
		this.dy = dy;
	}
	
	
	/**
	 * Returns the X-coordinate of the focus point which denotes the starting
	 * mouse position in game to begin the mouse dragging action for the shot.
	 * 
	 * @return  the {@code int} X-coordinate of the focus point.
	 */
	public int getX() {
		return x;
	}


	/**
	 * Sets the X-coordinate of the focus point which denotes the starting
	 * mouse position in game to begin the mouse dragging action for the shot.
	 * 
	 * @param x  the {@code int} X-coordinate for the focus point to start the 
	 *           shot at.
	 */
	public void setX(int x) {
		this.x = x;
	}


	/**
	 * Returns the Y-coordinate of the focus point which denotes the starting
	 * mouse position in game to begin the mouse dragging action for the shot.
	 * 
	 * @return  the {@code int} Y-coordinate of the focus point.
	 */
	public int getY() {
		return y;
	}


	/**
	 * Sets the Y-coordinate of the focus point which denotes the starting
	 * mouse position in game to begin the mouse dragging action for the shot.
	 * 
	 * @param y  the {@code int} Y-coordinate for the focus point to start the 
	 *           shot at.
	 */
	public void setY(int y) {
		this.y = y;
	}


	/**
	 * Returns the focus point which denotes the starting mouse position in 
	 * game to begin the mouse dragging action for the shot.
	 * 
	 * @return  the {@link Point} for the location of the focus point to start
	 *          the shot at.
	 * 
	 * @see java.awt.Point
	 */
	public Point getFocusPoint() {
		return new Point(x, y);
	}
	
	
	/**
	 * Returns the release point based on the focus point and the changes in 
	 * both X- and Y-coordinates. Effectively, this is equivalent to:
	 * {@code Point release = new Point(focus.x + deltaX, focus.y + deltaY)}
	 * 
	 * @return  the {@link Point} for the location of the release point.
	 * 
	 * @see java.awt.Point
	 */
	public Point getReleasePoint() {
		return new Point(x + dx, y + dy);
	}
	
	
	/**
	 * Returns the time to perform the shot at in milliseconds.
	 * 
	 * @return  the time, in milliseconds, to fire the shot at.
	 */
	public long getShootTime() {
		return shootTime;
	}


	/**
	 * Sets the time to perform the shot at in milliseconds.
	 * 
	 * @param shootTime  the {@code long} time in milliseconds to shot at.
	 */
	public void setShootTime(long shootTime) {
		this.shootTime = shootTime;
	}


	/**
	 * Returns the time, in milliseconds, to simulate the tap action at in game
	 * while firing a bird.
	 * 
	 * @return the {@code long} time in milliseconds to tap at.
	 */
	public long getTapTime() {
		return tapTime;
	}


	/**
	 * Sets the time, in milliseconds, to simulate the tap action at in game
	 * while firing a bird.
	 * 
	 * @param tapTime  the delay from the shoot time in milliseconds to tap at.
	 */
	public void setTapTime(long tapTime) {
		this.tapTime = tapTime;
	}


	@Override
	public String toString() {
		String result = getClass().getSimpleName() + ":";
		if (x == 0 && y == 0) {
			if (tapTime != 0)
				result += "tap="  + tapTime;
		}
		else
			result += "from (x="+ (x + dx) + ",y=" + (y + dy) +" )"
					+ " at time  " + shootTime ;	
	
		return result;
	}
	
	
	/**
	 * Returns a {@link Comparator} for the type {@link Shot} used to order the
	 * shots based on their shooting times.
	 * 
	 * @return  a {@link Comparator}<{@link Shot}> to compare shot based on 
	 *          their shooting times.
	 *          
	 * @see java.util.Comparator
	 */
	public static final Comparator<Shot> getShootTimeComparator() {
		Comparator<Shot> cmp = new Comparator<Shot>() {

			@Override
			public int compare(Shot s1, Shot s2) {
				Long i1 = s1 != null ? s1.getShootTime() : null;
				Long i2 = s2 != null ? s2.getShootTime() : null;
				if (i1 != null)
					return i1.compareTo(i2);
				else if (i2 != null)
					return i2.compareTo(i1);
				else
					return 0;
			}
			
		};
		return cmp;
	}
	
}
