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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Provides an accessor to an external configuration file specifying varying
 * parameters and fields which can be accessed within our implementation of the
 * AI agent to play Angry Birds.
 * 
 * @author Chris N. Hartley (cnhartle@calpoly.edu)
 */
public class Env {


	private static final String configurationFilePath = "team11.conf";
	
	private static final String startCommentLine = "#";
	
	private static final String startFocusPoint = "focus_pt";
	
	private static final HashMap<Integer, Point> env =
			new HashMap<Integer, Point>();
	
	
	public static final boolean containsKey(Object obj) {
		return env.containsKey(obj);
	}
	
	
	public static final Point get(Object obj) {
		return env.get(obj);
	}
	
	
	public static final Point put(Integer key, Point value) {
		return env.put(key, value);
	}

	
	// TODO need to rewrite configuration file style...
	static {
		File file = new File(configurationFilePath);
		String line;
		
		if (file.exists()) {
			BufferedReader br = null;
			try {
				br = new BufferedReader( new FileReader(file) );
				while (br.ready()) {
					line = br.readLine().trim();
					
				    if (line.contains(startCommentLine))
				    	processComment(line);
				    else if (line.contains(startFocusPoint))
				    	processFocusPoint(line);
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException ignore) { }
				}
			}
	    }
	}
	
	
	private static final void processComment(String ln) {
		// TODO add code if comments are needed to be kept track of.
	}
	
	
	private static final void processFocusPoint(String ln) {
		String[] xyz = ln.substring(ln.lastIndexOf(":") + 1).trim().split(",");
		int x, y, z;
		
		if (xyz.length >= 3) {
			x = Integer.parseInt(xyz[0]);
			y = Integer.parseInt(xyz[1]);
			z = Integer.parseInt(xyz[2]);
			env.put(z, new Point(x, y) );
	    }
	}
	
	
}
