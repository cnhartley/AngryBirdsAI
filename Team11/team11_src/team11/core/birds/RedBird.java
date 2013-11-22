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

import java.awt.Rectangle;

/**
 * Provides an object representing a red bird found in the Angry Birds game.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 * 
 * @see team11.core.birds.Bird
 */
public class RedBird extends Bird {

	/**
	 * Specifies the specific serial version id as part of the
	 * {@link Serializable} interface.
	 */
	private static final long serialVersionUID = 7885939156484791152L;


	/**
	 * Constructor for a new instance of a red bird found in game with the
	 * specified bounding {@link Rectangle}.
	 * 
	 * @param rect  the {@link Rectangle} boundary for this instance of the
	 *              red bird.
	 */
	public RedBird(Rectangle rect) {
		super(RED, rect);
	}

}
