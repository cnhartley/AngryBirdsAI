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


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import team11.core.other.Shot;
import team11.core.utils.ActionRobot;


/**
 * Provides a general shooting schema to use when firing birds in the Angry
 * Birds game.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 */
public class ShootingSchema implements Schema {

	
	/**
	 * Specifies the delay between shots when firing multiple shots or when a
	 * tap is needed.
	 */
	private static final int delayBetweenShots = 5000;
	
	
	/**
	 * Shoots the specified {@link List} of type {@link Shot}s based on their
	 * values for the starting point, ({@code x}, {@code y}), change in X and Y
	 * coordinates to the drag to point, specified by ({@code dx}, {@code dy}),
	 * and the times to release and tap, if applicable.
	 * 
	 * @param shotList the {@link List}<{@code Shot}> for all shots to be fired.
	 * 
	 * @see team11.core.other.Shot
	 */
	public final void shoot(List<Shot> shotList) {
		List<Shot> shots = refactorShots(shotList);
		
		if (!shots.isEmpty()) {
			fireShots(shots);
			ActionRobot.sleep(10000);
		}
	}
	
	
	/**
	 * Shoots the specified {@link Shot} based on its values for the starting
	 * point, ({@code x}, {@code y}), change in X and Y coordinates to the drag
	 * to point, specified by ({@code dx}, {@code dy}), and the times to release
	 * and tap, if applicable.
	 * 
	 * @param singleShot the {@code Shot} to be fired.
	 * 
	 * @see team11.core.other.Shot
	 */
	public final void shoot(Shot singleShot) {
		shoot( Collections.singletonList(singleShot) );
	}
	
	
	/**
	 * Refactors the specified {@link List} of type {@link Shot}s by breaking
	 * each shot into a shot containing the starting point and the drag to point
	 * while also creating a separate shot containing the tap information when
	 * present. This method will also arrange the shots in chronological order
	 * and add any needed delays between the separate shots.
	 * 
	 * @param shotList the {@link List}<{@code Shot}> for all shots to be fired.
	 * 
	 * @return  the {@link List}<{@code Shot}> for all shots to be fired after
	 *          refactoring and prioritizing.
	 * 
	 * @see team11.core.other.Shot
	 */
	private final List<Shot> refactorShots(List<Shot> shotList) {
		LinkedList<Shot> shots = new LinkedList<Shot>();
		boolean shootImmediately = true;
		long shotDelay = 0l;
		int count = 0;
		
		for (Shot shot : shotList) {
			shotDelay = delayBetweenShots * count++;
			
			if (shot.getShootTime() != 0)
				shootImmediately = false;
			
			if (!shootImmediately)
	    		shotDelay = shot.getShootTime();
			
			shots.add( new Shot(shot.getX(),shot.getY(), shot.getDeltaX(),
					shot.getDeltaY(), shotDelay) );
			
			if (shot.getTapTime() > 0)
				shots.add( new Shot(0,0,0,0, shot.getTapTime() + shotDelay) );
	    }
		
		Collections.sort(shots, Shot.getShootTimeComparator());
		return shots;
	}
	
	
	/**
	 * Fires the {@link List} of {@link Shot}s in order with the required delay
	 * between each shot in the list.
	 * 
	 * @param shots the {@link List}<{@code Shot}> for all shots to be fired.
	 * 
	 * @see team11.core.other.Shot
	 */
	private final void fireShots(List<Shot> shots) {
		long lastShotTime = 0l;
		long gap = 0l;
		
		if (!shots.isEmpty())
			lastShotTime = shots.get(0).getShootTime();
		
		for (Shot s : shots) {
			ActionRobot.sleep(s.getShootTime() - lastShotTime - gap);
			lastShotTime = s.getShootTime();
			
			gap = System.currentTimeMillis();
			ActionRobot.doDrag(s.getX(),s.getY(), s.getDeltaX(),s.getDeltaY());
			gap = System.currentTimeMillis() - gap;
		}
	}

}
