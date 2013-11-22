/**
 * 
 */
package team11.gui;

import java.awt.Component;

/**
 * Provides a thread incharge of repainting the specified component. If the 
 * {@code #setMaximumRefreshRate(int)} has been set to a value greater than 
 * zero, then this renderer will repaint up to the specified value.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 *
 * @see java.lang.Thread
 */
public class RenderThread extends Thread {

	// Member data.
	private int maxFrameRate;  // In terms of frames per second
    private final Component renderComponent;
    
    private int frameCount = 0;
    private long frameStartTime = 0l;
    private double framePerSecond = 0d;
    
    private final String infoFormat =
    		getClass().getCanonicalName() + ": running on %s at FPS: %.2f";
    
    
	/**
	 * 
	 * @param component
	 */
    public RenderThread(final Component component) {
		renderComponent = component;
        maxFrameRate = 0;
	}

    
    /**
     * 
     * @param maxFramesPerSecond
     */
    public final void setMaximumRefreshRate(int maxFramesPerSecond) {
		maxFrameRate = maxFramesPerSecond;
	}
    
    
    /**
     * 
     * @return
     */
    public final double getFPS() {
    	return framePerSecond;
    }
    
    
    @Override
    public void run() {
        boolean continueRendering = true;
        long repaintTime;
        long lapse;
        int maxRate = 0;
        
        if (maxFrameRate > 0)
            maxRate = (int)(1000.0 / maxFrameRate);
        
        while (continueRendering && renderComponent != null) {
            repaintTime = System.currentTimeMillis();
            
            if (frameCount == 8) {
                lapse = repaintTime - frameStartTime;
                framePerSecond = 8000.0 / lapse;
                frameCount = 0;
            }
            
            if (frameCount == 0)
                frameStartTime = repaintTime;
            
            frameCount++;
            renderComponent.repaint();
            
            repaintTime = System.currentTimeMillis() - repaintTime;
            if (repaintTime < maxRate) {
                try {
                    sleep(maxRate - repaintTime);
                }
                catch (InterruptedException ie) {
                    continueRendering = false;
                }
            }
        }
    }
    
    
    @Override
    public String toString() {
        return String.format( infoFormat,
                              renderComponent.getClass().getCanonicalName(),
                              getFPS() );
    }

}
