/**
 * 
 */
package team11.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import team11.core.utils.ActionRobot;

/**
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 *
 */
public class AgentFrame extends JFrame implements Runnable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1516322014221528655L;


	// Member data.
	private int screenshotWidth = -1;
	private int screenshotHeight = -1;
    private double framePerSecond = 0d;
    private StatusBar statusBar = null;
	private GameViewPanel gameView;
	
    private RenderThread renderer = null;
    
	
	/**
	 * 
	 */
	public AgentFrame(String agentName) {
		super("Angry Birds - AI Agent: " + agentName);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) { }

			@Override
			public void windowClosed(WindowEvent arg0) { }

			@Override
			public void windowClosing(WindowEvent arg0) {
				close();
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) { }

			@Override
			public void windowDeiconified(WindowEvent arg0) { }

			@Override
			public void windowIconified(WindowEvent arg0) { }

			@Override
			public void windowOpened(WindowEvent arg0) { }
			
		});
		
		setContentPane( buildGUI() );
		setResizable(false);
		
		setVisible(true);
	}
	
    
    /**
     * Returns the current value of the frames per second that this window is
     * updating its view of the game at.
     * 
     * @return  the {@code double} value of the frames per second the screen is 
     *          updating in.
     */
    public final double getFPS() {
        return framePerSecond;
    }
    
    
	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		BufferedImage screenshot = ActionRobot.doScreenShot();
		screenshotWidth = screenshot.getWidth();
		screenshotHeight = screenshot.getHeight();
		
		final Insets ins = getInsets();
		final int width = screenshotWidth + ins.left + ins.right;
		final int height = screenshotHeight + ins.top + ins.bottom;
		setSize(width, height + statusBar.getHeight());

		if (renderer != null)
			renderer.interrupt();
		
		renderer = new RenderThread(gameView);
		renderer.setMaximumRefreshRate(30);
		renderer.start();
	}
	
	
	public final void close() {
		if (renderer != null) {
			renderer.interrupt();
			renderer = null;
		}
		
		dispose();
	}


	/**
	 * Returns the {@link Container} for the content pane of this frame.
	 * 
	 * @return  the {@link Container} of the GUI for this frame.
	 */
	private final Container buildGUI() {
		JPanel main = new JPanel( new BorderLayout() );
		
		main.add(statusBar = new StatusBar(), BorderLayout.SOUTH);
		main.add(gameView = new GameViewPanel(), BorderLayout.CENTER);
		
		gameView.addMouseMotionListener(statusBar);
		
		return main;
	}
	
	
	private class StatusBar extends JPanel implements MouseMotionListener {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2918469745017936080L;
		
		
		// Member data.
		private JLabel mouseLocationLabel = null;
		private String mouseLocationFormat = "Mouse at (%d, %d)";
		
		 

		
		/**
		 * 
		 */
		public StatusBar() {
			super(null);
			
			build();
		}
		
		
		public final void build() {
			BoxLayout box = new BoxLayout(this, BoxLayout.X_AXIS);
			setLayout(box);
			
			final JLabel fpsLabel = new JLabel("FPS: [unknown]");
			
			add(mouseLocationLabel = new JLabel());
			add(Box.createHorizontalGlue());
			add(fpsLabel);
			
			Timer fpsClock = new Timer(1000, new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent ae) {
					fpsLabel.setText("" + renderer);
				}
				
			});
			fpsClock.setInitialDelay(2000);
			fpsClock.start();
		}
		
		
		@Override
		public void mouseDragged(MouseEvent e) {
			updateMouseLocation(e.getX(), e.getY());
		}


		@Override
		public void mouseMoved(MouseEvent e) {
			updateMouseLocation(e.getX(), e.getY());
		}
		
		
		private final void updateMouseLocation(int x, int y) {
			mouseLocationLabel.setText(String.format(mouseLocationFormat, x, y));
		}
	}

}
