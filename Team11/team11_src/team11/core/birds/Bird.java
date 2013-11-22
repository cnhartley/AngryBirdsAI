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
package team11.core.birds;

import java.awt.Color;
import java.awt.Rectangle;

/**
 * Provides an abstract class for all {@link Bird}s that can be used in the 
 * Angry Birds game.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 * 
 * @see java.awt.Rectangle
 */
public abstract class Bird extends Rectangle {

	// Member data.
	public static final Color BLACK = Color.black;
	public static final Color BLUE = Color.blue;
	public static final Color RED = Color.red;
	public static final Color WHITE = Color.white;
	public static final Color YELLOW = Color.yellow;
	
	public static double defaultTapPercentage = 0.75;
	
	
	/**
	 * Specifies the specific serial version id as part of the
	 * {@link Serializable} interface.
	 */
	private static final long serialVersionUID = 3133113157405199086L;

	
	// Member data.
	private final Color color;
	private boolean isFired = false;
	public double tapPercentage = defaultTapPercentage;
	
	
	/**
	 * Constructor for this instance of a bird of the specified color.
	 * 
	 * @param color  the {@link Color} of the bird.
	 */
	public Bird(Color color) {
		this.color = color;
	}

	/**
	 * Constructor for this instance of a bird of the specified color and bounds
	 * as defined by the {@link Rectangle} parameter.
	 * 
	 * @param color  the {@link Color} of the bird.
	 * @param rect   the {@link Rectangle} bounds of the bird.
	 */
	public Bird(Color color, Rectangle rect) {
		super(rect);
		this.color = color;
	}
	
	
	/**
	 * Returns the {@link Color} of this instance of the bird class.
	 * 
	 * @return  the {@link Color} of this instance of the bird class.
	 */
	public Color getColor() {
		return color;
	}
	
	
	/**
	 * Returns {@code true} if this instance of the bird has been fired already;
	 * otherwise, returns {@code false}.
	 * 
	 * @return  {@code true} when this bird has been fired.
	 */
	public boolean isFired() {
		return isFired;
	}

	
	/**
	 * Sets this instance of the bird to be fired already.
	 */
	@Deprecated
	public void fire() {
		isFired = true;
	}
	
	
	/**
	 * Sets this instance of the bird to be fired already.
	 */
	public void fired() {
		isFired = true;
	}
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[fired=" + isFired + ",x="
				+ getCenterX() + ",y=" + getCenterY() + "]";
	}

}
