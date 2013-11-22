/**
 * 
 */
package team11.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import team11.core.utils.ActionRobot;

import Jama.Matrix;
import ab.vision.GameStateExtractor;
import ab.vision.Vision;
import ab.vision.VisionUtils;
import ab.vision.GameStateExtractor.GameState;

/**
 * @author Chris Hartley
 *
 */
public final class GameViewPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2152925257806151616L;

	
	// Member data.
	private Image image = null;
	private int[][] metaData = null;
	
	private int highlightIndex = -1;
	private boolean drawTrajectory = true;
	private boolean includeHigherLevelObjects = false;
	
	private final GameStateExtractor extractor = new GameStateExtractor();
	
	//private BufferedImage screenshotOrignal = null;
	//private BufferedImage screenshotGrayScale = null;
	private BufferedImage screenshotHighLights = null;
	
	/**
	 * Constructor for an instance of this game view panel.
	 */
	public GameViewPanel() {
		super(null);
		
		setDoubleBuffered(true);
	}
	
	
	/**
	 * 
	 * @param img
	 * @param meta
	 */
    public final void update(Image img, int[][] meta) {
    	image = img;
        metaData = meta;
        
        //repaint();
    }

    class ScreenShotAnalyzer extends SwingWorker<BufferedImage, BufferedImage> {

    	private final BufferedImage screenshotColor;
    	private final int screanshotWidth;
    	private final int screenshotHeight;
    	
    	public ScreenShotAnalyzer(BufferedImage screenshot) {
    		screenshotColor = screenshot;
    		
    		if (screenshotColor != null) {
    			screanshotWidth = screenshot.getWidth();
    			screenshotHeight = screenshot.getHeight();
    		}
    		else {
    			screanshotWidth = -1;
    			screenshotHeight = -1;
    		}
    	}
    	
		@Override
		protected BufferedImage doInBackground() throws Exception {
			if (screenshotColor == null)
				return null;
			
			BufferedImage screenshotGreyScale =
					new BufferedImage(screanshotWidth, screenshotHeight,
							BufferedImage.TYPE_BYTE_GRAY);
			
			Graphics g = screenshotGreyScale.getGraphics();
			g.drawImage(screenshotColor, 0, 0, null);
			g.dispose();
			publish(screenshotGreyScale);
			//BufferedImage screenshot = ActionRobot.doScreenShot();
			
			return analyseScreenShot2(screenshotColor);
		}
		
		@Override
		protected void done() {
			try {
				//update(get(), null);
				screenshotHighLights = get();
			} catch (Exception ignore) { }
		}
		
		@Override
		protected void process(List<BufferedImage> images) {
			for (BufferedImage image : images) {
				update(image, null);
			}
		}
    	
    }
    
    ScreenShotAnalyzer analyzer = null;
    @Override
    public void repaint() {
    	//int[][] meta = null;
    	if (analyzer == null || analyzer.isDone()) {
    		BufferedImage screenshot = ActionRobot.doScreenShot();
    		analyzer = new ScreenShotAnalyzer(screenshot);
    		analyzer.execute();
    	}
    	//update(VisionUtils.convert2grey(screenshot), meta);
    	
    	//meta = computeMetaInformation(screenshot);
		//screenshot = analyseScreenShot(screenshot);
		
		//update(screenshot, meta);
		
    	super.repaint();
    }
    
    
    @Override
    public void paint(Graphics g) {
    	Graphics2D g2d = (Graphics2D)g;
    	BufferedImage canvas;
    	
        if (image != null) {
            if ((metaData != null) && (highlightIndex != -1)) {
                canvas = VisionUtils.highlightRegions(image, metaData,
                		highlightIndex, Color.RED);
                g2d.drawImage(canvas, 0, 0, null);
            }
            else {
                g2d.drawImage(image, 0, 0, null);
                if (screenshotHighLights != null)
                	g2d.drawImage(screenshotHighLights, 0, 0, null);
            }
        }
        else {
        	g2d.setColor(Color.black);
        	g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    

    protected abstract class VisionFindWorker
    		extends SwingWorker<List<Rectangle>,Rectangle>
    {

    	protected final Graphics2D g2d;
    	private final Color fgColor;
    	private final Color bgColor;
    	
    	public VisionFindWorker(Graphics2D g2d, Color fgColor, Color bgColor) {
    		this.g2d = g2d;
    		this.fgColor = fgColor;
    		this.bgColor = bgColor;
    	}
		
		@Override
		protected void done() {
			try {
				for (Rectangle rect : get())
					drawBoundingBox(g2d, rect, fgColor, bgColor);
			}
			catch (InterruptedException | ExecutionException ignore) { }
		}
    	
    }
	
	
	/**
	 * Analyzes the specified screenshot finding the objects and drawing 
	 * rectangular regions around them in various colors. This will also attempt
	 * to find and plot a path of the trajectory for the last bird fired. All of
	 * the drawing is done on the screenshot image and then returned.
	 * 
	 * @param screenshot  the {@link BufferedImage} of the screenshot to analyze
	 *                    and update.
	 *                    
	 * @return  the updated {@link BufferedImage} after completing the analysis.
	 *
    private final BufferedImage analyseScreenShot(BufferedImage screenshot) {
		BufferedImage highlights =
				new BufferedImage(screenshot.getWidth(), screenshot.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
		
		if (screenshot == null || extractor == null)
			return highlights;
		
		GameState state = extractor.getGameState(screenshot);
		if (!ActionRobot.isPlaying(state))
			return highlights;//VisionUtils.convert2grey(screenshot);

		// process image
		Vision vision = new Vision(screenshot);
		List<Rectangle> pigs = vision.findPigs();
		List<Rectangle> redBirds = vision.findRedBirds();
		List<Rectangle> blueBirds = vision.findBlueBirds();
		List<Rectangle> yellowBirds = vision.findYellowBirds();
		List<Rectangle> woodBlocks = vision.findWood();
		List<Rectangle> stoneBlocks = vision.findStones();
		List<Rectangle> iceBlocks = vision.findIce();
		List<Rectangle> whiteBirds = vision.findWhiteBirds();
		List<Rectangle> blackBirds = vision.findBlackBirds();
		List<Rectangle> TNTs = vision.findTNTs();
		List<Point> trajPoints = vision.findTrajPoints();
		Rectangle sling = vision.findSlingshot();

		//screenshot = VisionUtils.convert2grey(screenshot);
		Graphics2D g2d = highlights.createGraphics();//screenshot.createGraphics();
		
		drawBoundingBoxes(g2d, pigs, Color.GREEN);
		drawBoundingBoxes(g2d, redBirds, Color.RED);
		drawBoundingBoxes(g2d, blueBirds, Color.BLUE);
		drawBoundingBoxes(g2d, yellowBirds, Color.YELLOW);
		drawBoundingBoxes(g2d, woodBlocks, Color.WHITE, Color.ORANGE);
		drawBoundingBoxes(g2d, stoneBlocks, Color.WHITE, Color.GRAY);
		drawBoundingBoxes(g2d, iceBlocks, Color.WHITE, Color.CYAN);
		drawBoundingBoxes(g2d, whiteBirds, Color.WHITE, Color.lightGray);
		drawBoundingBoxes(g2d, TNTs, Color.WHITE, Color.PINK);
		drawBoundingBoxes(g2d, blackBirds, Color.BLACK);
		
		if (sling != null && drawTrajectory) {
			drawBoundingBox(g2d, sling, Color.ORANGE, Color.BLACK);

			// generate traj points using estimated parameters
			Matrix W = vision.fitParabola(trajPoints);
			int p[][] = new int[2][100];
			int startx = (int) sling.getCenterX();
			for (int i = 0; i < 100; i++) {
				p[0][i] = startx;
				double tem = W.get(0, 0) * Math.pow(p[0][i], 2) + W.get(1, 0)
						* p[0][i] + W.get(2, 0);
				p[1][i] = (int) tem;
				startx += 10;
			}
			if (W.get(0, 0) > 0)
				drawTrajectory(g2d, p, Color.RED);

		}

		return highlights;
	}//*/
    
    private final BufferedImage analyseScreenShot2(BufferedImage screenshot) {
    	long lapseTime = System.currentTimeMillis();
    	
		BufferedImage highlights =
				new BufferedImage(screenshot.getWidth(), screenshot.getHeight(),
						BufferedImage.TYPE_INT_ARGB);
		
		if (screenshot == null || extractor == null)
			return highlights;
		
		GameState state = extractor.getGameState(screenshot);
		if (!ActionRobot.isPlaying(state))
			return highlights;
		
		Graphics2D g2d = highlights.createGraphics();
		final Vision vision = new Vision(screenshot);
		
		LinkedList<VisionFindWorker> finders = new LinkedList<VisionFindWorker>();
		
		//List<Rectangle> pigs = vision.findPigs();
		finders.add( new VisionFindWorker(g2d, Color.GREEN, Color.WHITE) {
			protected List<Rectangle> doInBackground() throws Exception {
				return vision.findPigs();
			}
		} );
		finders.getLast().execute();
		
		//List<Rectangle> redBirds = vision.findRedBirds();
		finders.add( new VisionFindWorker(g2d, Color.RED, Color.WHITE) {
			protected List<Rectangle> doInBackground() throws Exception {
				return vision.findRedBirds();
			}
		} );
		finders.getLast().execute();
		
		//List<Rectangle> blueBirds = vision.findBlueBirds();
		finders.add( new VisionFindWorker(g2d, Color.BLUE, Color.WHITE) {
			protected List<Rectangle> doInBackground() throws Exception {
				return vision.findBlueBirds();
			}
		} );
		finders.getLast().execute();
		
		//List<Rectangle> yellowBirds = vision.findYellowBirds();
		finders.add( new VisionFindWorker(g2d, Color.YELLOW, Color.WHITE) {
			protected List<Rectangle> doInBackground() throws Exception {
				return vision.findYellowBirds();
			}
		} );
		finders.getLast().execute();
		
		//List<Rectangle> woodBlocks = vision.findWood();
		finders.add( new VisionFindWorker(g2d, Color.WHITE, Color.ORANGE) {
			protected List<Rectangle> doInBackground() throws Exception {
				return vision.findWood();
			}
		} );
		finders.getLast().execute();
		
		//List<Rectangle> stoneBlocks = vision.findStones();
		finders.add( new VisionFindWorker(g2d, Color.WHITE, Color.GRAY) {
			protected List<Rectangle> doInBackground() throws Exception {
				return vision.findStones();
			}
		} );
		finders.getLast().execute();
		
		//List<Rectangle> iceBlocks = vision.findIce();
		finders.add( new VisionFindWorker(g2d, Color.WHITE, Color.CYAN) {
			protected List<Rectangle> doInBackground() throws Exception {
				return vision.findIce();
			}
		} );
		finders.getLast().execute();
		
		if (includeHigherLevelObjects) {
			//List<Rectangle> whiteBirds = vision.findWhiteBirds();
			finders.add( new VisionFindWorker(g2d, Color.WHITE, Color.LIGHT_GRAY) {
				protected List<Rectangle> doInBackground() throws Exception {
					return vision.findWhiteBirds();
				}
			} );
			finders.getLast().execute();
			
			//List<Rectangle> blackBirds = vision.findBlackBirds();
			finders.add( new VisionFindWorker(g2d, Color.BLACK, Color.WHITE) {
				protected List<Rectangle> doInBackground() throws Exception {
					return vision.findBlackBirds();
				}
			} );
			finders.getLast().execute();
			
			//List<Rectangle> TNTs = vision.findTNTs();
			finders.add( new VisionFindWorker(g2d, Color.WHITE, Color.PINK) {
				protected List<Rectangle> doInBackground() throws Exception {
					return vision.findTNTs();
				}
			} );
			finders.getLast().execute();
		}
		
		VisionFindWorker slingWorker;
		//Rectangle sling = vision.findSlingshot();
		finders.add(slingWorker = new VisionFindWorker(g2d, Color.ORANGE, Color.BLACK) {
			protected List<Rectangle> doInBackground() throws Exception {
				List<Rectangle> slings = new ArrayList<Rectangle>(1);
				slings.add(vision.findSlingshot());
				return slings;
			}
		} );
		finders.getLast().execute();

		try {
			Rectangle sling = slingWorker.get().get(0);
			if (sling != null && drawTrajectory) {
				List<Point> trajPoints = vision.findTrajPoints();

				Matrix W = vision.fitParabola(trajPoints);
				int p[][] = new int[2][100];
				int startx = (int) sling.getCenterX();
				double tem;
				
				for (int i = 0; i < 100; i++) {
					p[0][i] = startx;
					tem = W.get(0, 0) * Math.pow(p[0][i], 2) + W.get(1, 0)
							* p[0][i] + W.get(2, 0);
					p[1][i] = (int) tem;
					startx += 10;
				}
				if (W.get(0, 0) > 0)
					drawTrajectory(g2d, p, Color.RED);

			}
		}
		catch (InterruptedException | ExecutionException e1) { }
		
		for (SwingWorker<?,?> finder : finders) {
			try {
				finder.get();
			}
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		lapseTime = System.currentTimeMillis() - lapseTime;
		drawAnalysisTime(g2d, highlights.getWidth() >> 1,
				highlights.getHeight() - 2, lapseTime);
		
		g2d.dispose();
		
		return highlights;
	}
    
    
    private final void drawAnalysisTime(Graphics2D g2d, int x, int y, long lapse) {
    	final String text = "[ Analysis took: " + lapse + "ms ]";
    	int left = g2d.getFontMetrics().stringWidth(text) >> 1;
    	g2d.setColor(Color.orange);
    	g2d.drawString(text, x - left, y - g2d.getFontMetrics().getMaxDescent());
    }
	
	
	
	
	protected static final void drawTrajectory(Graphics2D g2d, int parabola[][], Color fgColor) {
		g2d.setColor(fgColor);
		g2d.drawPolyline(parabola[0], parabola[1], parabola[0].length);
	}
	
	protected static final void drawBoundingBox(Graphics2D g2d, Rectangle box, Color fgColor) {
		drawBoundingBoxes(g2d, Collections.singletonList(box), fgColor);
	}
	
	protected static final void drawBoundingBox(Graphics2D g2d, Rectangle box, Color fgColor, Color bgColor) {
		drawBoundingBoxes(g2d, Collections.singletonList(box), fgColor, bgColor);
	}
	
	protected static final void drawBoundingBoxes(Graphics2D g2d, List<Rectangle> boxes, Color fgColor) {
		drawBoundingBoxes(g2d, boxes, fgColor, Color.WHITE);
	}
	
	protected static final synchronized void drawBoundingBoxes(Graphics2D g2d, List<Rectangle> boxes, Color fgColor, Color bgColor) {
		for (Rectangle box : boxes) {
			if (box != null) {
				g2d.setColor(bgColor);
				g2d.drawRect(box.x - 1, box.y - 1, box.width + 2, box.height + 2);
				g2d.drawRect(box.x + 1, box.y + 1, box.width - 2, box.height - 2);
				g2d.setColor(fgColor);
				g2d.drawRect(box.x, box.y, box.width, box.height);
			}
		}
	}


	/**
	 * Computes the meta information from the specified screenshot and returns
	 * the information as an {@code int[][]}.
	 * 
	 * @param screenshot  the {@link BufferedImage} of the screenshot to compute
	 *                    the mate information from.
	 * @return  the double {@code int} array containing the computed meta 
	 *          information from the screenshot.
	 *
	private static final int[][] computeMetaInfo(BufferedImage screenshot) {
		final int screenshotHeight = screenshot.getHeight();
		final int screenshotWidth = screenshot.getWidth();
		final int[][] meta = new int[screenshotHeight][screenshotWidth];
		int color, x, y;
		
		for (y = 0; y < screenshotHeight; y++) {
			for (x = 0; x < screenshotWidth; x++) {
				color = screenshot.getRGB(x, y);
				meta[y][x] = ((color & 0x00e00000) >> 15)
						| ((color & 0x0000e000) >> 10)
						| ((color & 0x000000e0) >> 5);
			}
		}

		return meta;
	}//*/
	
}
