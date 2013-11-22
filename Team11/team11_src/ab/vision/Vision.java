/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2013,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys, Kar-Wai Lim, Zain Mubashir,  Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/

package ab.vision;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import Jama.Matrix;

/* Vision ----------------------------------------------------------------- */

public class Vision {

	/**
	 * Specifies the minimal number of pixels a region should contain.
	 */
	private static final int regionThreshold = 10;

	
	private static final int[] stone3bitSet = { 365 };
	private static final int[] wood3bitSet = { 408, 417, 481 };
	private static final int[] traj3bitSet = { 365, 366, 438 };
	private static final int[] ice3bitSet = { 183, 247, 311 };
	private static final int[] sling3bitSet =
		{ 209, 273, 281, 282, 345, 346, 351, 354, 418 };
	
	
	// Member data.
	private int _nHeight; // height of the scene
	private int _nWidth; // width of the scene
	private int _scene[][]; // quantized scene colours
	private int _nSegments; // number of segments
	private int _segments[][]; // connected components (0 to _nSegments)
	private int _colours[]; // colour for each segment
	private Rectangle _boxes[]; // bounding box for each segment
	
	
	// create a vision object for processing a given screenshot
	public Vision(BufferedImage screenshot) {
		processScreenShot(screenshot);
	}

	//find sling-shot
	//only return one rectangle
	/*
	 * Update 11/20/2013 - Chris N. Hartley - optimized and reordered method to
	 *                     improve speed and readability.
	 */
	public Rectangle findSlingshot() {
		final int[] colorSet = { 511, 447 };
		
		List<Rectangle> slings = find(sling3bitSet);
		int[] hist;
		int[] histCol;
		Rectangle col;
		
		if (slings.isEmpty())
			return null;
		
		//return slings.isEmpty() ? null : slings.get(0);
		for (Rectangle sling : slings) {
			hist = histogram(sling);

			// abandon shelf underneath
			if (sling.height > 10) {
				col = new Rectangle(sling.x, sling.y, 1, sling.height);
				histCol = histogram(col);
				
				if (isValidColor(sling.x, sling.y, colorSet)) {
					for (int m = sling.y; m < sling.y + sling.height; m++) {
						if (isValidColor(sling.x, m, sling3bitSet)) {
							sling.setSize(sling.width, m - sling.y);
							break;
						}
					}
				}

				while (histCol[511] >= sling.height * 0.8) {
					sling.setBounds(sling.x + 1, sling.y,
							sling.width - 1, sling.height);
					col = new Rectangle(sling.x + 1, sling.y, 1, sling.height);
					histCol = histogram(col);
				}

				col = new Rectangle(sling.x + sling.width, sling.y,
						1, sling.height);
				histCol = histogram(col);
				
				while (histCol[511] >= sling.height * 0.8 && sling.height > 10)
				{
					sling.setSize(sling.width - 1, sling.height);
					col = new Rectangle(sling.x + sling.width, sling.y,
							1, sling.height);
					histCol = histogram(col);
				}
			}

			if (sling.width > sling.height)
				continue;

			if ((hist[345] > Math.max(32, 0.1 * sling.width * sling.height))
					&& (hist[64] != 0)) {
				sling.add(
						new Rectangle(sling.x - sling.width / 10,
								sling.y - sling.height / 3,
								sling.width / 10 * 12,
								sling.height / 3 * 4) );
				
				return sling;
			}
		}

		return null;
	}

	
	// find pigs in the current scene
	/*
	 * Update 11/20/2013 - Chris N. Hartley - optimized and reordered method to
	 *                     improve speed and readability.
	 */
	public List<Rectangle> findPigs() {
		final ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		Boolean ignore[] = createIgnoreSegmentSet();
		Rectangle bounds, bounds2, obj;

		for (int n = 0; n < _nSegments; n++) {
			if (!isValidColor(_colours[n], 376) || ignore[n])
				continue;

			// dilate bounding box of color 376
			bounds = VisionUtils.dialateRectangle(_boxes[n], _boxes[n].width / 2 + 1, _boxes[n].height / 2 + 1);
			obj = _boxes[n];

			// look for overlapping bounding boxes of color 376
			for (int m = n + 1; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], 376))
					continue;
				
				bounds2 = VisionUtils.dialateRectangle(
						_boxes[m], _boxes[m].width / 2 + 1,
						_boxes[m].height / 2 + 1);
				
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of color 250
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], 250))
					continue;
				
				if (bounds.intersects(_boxes[m])) {
					bValidObject = true;
					break;
				}
			}

			// add object if valid
			if (bValidObject) {
				obj = VisionUtils.dialateRectangle(obj, obj.width / 2 + 1, obj.height / 2 + 1);
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	// find birds in the current scene
	/*
	 * Update 11/20/2013 - Chris N. Hartley - optimized and reordered method to
	 *                     improve speed and readability.
	 */
	public List<Rectangle> findRedBirds() {
		final ArrayList<Rectangle> objects = new ArrayList<Rectangle>();
		final int[] colorSet = { 488, 501 };
		
		Boolean ignore[] = createIgnoreSegmentSet();
		Rectangle bounds, bounds2, obj;

		for (int n = 0; n < _nSegments; n++) {
			if (!isValidColor(_colours[n], 385) || ignore[n])
				continue;

			// dilate bounding box around color 385
			bounds = VisionUtils.dialateRectangle(_boxes[n], 1, _boxes[n].height / 2 + 1);
			obj = _boxes[n];

			// look for overlapping bounding boxes of color 385
			for (int m = n + 1; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], 385))
					continue;
				
				bounds2 = VisionUtils.dialateRectangle(_boxes[m], 1, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// look for overlapping bounding boxes of colors 488 and 501
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], colorSet))
					continue;
				
				if (bounds.intersects(_boxes[m])) {
					obj.add(_boxes[m]);
					bValidObject = true;
				}
			}

			if (bValidObject) {
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}

	
	/*
	 * Update 11/20/2013 - Chris N. Hartley - optimized and reordered method to
	 *                     improve speed and readability.
	 */
	public List<Rectangle> findBlueBirds() {
		final ArrayList<Rectangle> objects = new ArrayList<Rectangle>();
		final int[] colorSet = { 238, 165, 280, 344, 488, 416 };
		
		Boolean ignore[] = createIgnoreSegmentSet();
		Rectangle bounds, bounds2, obj;
		
		for (int n = 0; n < _nSegments; n++) {
			if (!isValidColor(_colours[n], 238) || ignore[n])
				continue;

			// dilate bounding box around color 238
			bounds = VisionUtils.dialateRectangle(_boxes[n], 1,	_boxes[n].height / 2 + 1);
			obj = _boxes[n];

			// look for overlapping bounding boxes of colors 238, 165, 280,
			// 344, 488, 416
			for (int m = n + 1; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], colorSet))
					continue;
				
				bounds2 = VisionUtils.dialateRectangle(_boxes[m], 2, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			for (int m = n + 1; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], 238))
					continue;
				
				bounds2 = VisionUtils.dialateRectangle(_boxes[m], 2, _boxes[m].height / 2 + 1);
				if (bounds.intersects(bounds2))
					ignore[m] = true;
			}

			// look for overlapping bounding boxes of colors 488
			Boolean bValidObject = false;
			for (int m = 0; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], 488))
					continue;
				
				if (bounds.intersects(_boxes[m])) {
					obj.add(_boxes[m]);
					bValidObject = true;
				}
			}

			if (bValidObject && (obj.width > 3)) {
				obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
				objects.add(obj);
			}
		}

		return objects;
	}
	

	/*
	 * Update 11/20/2013 - Chris N. Hartley - optimized and reordered method to
	 *                     improve speed and readability.
	 */
	public List<Rectangle> findYellowBirds() {
		final ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		Boolean ignore[] = createIgnoreSegmentSet();
		Rectangle bounds, bounds2, obj;
		int[] hist;
		
		for (int n = 0; n < _nSegments; n++) {
			if (!isValidColor(_colours[n], 497) || ignore[n])
				continue;

			// dilate bounding box around color 497
			bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			obj = _boxes[n];

			// look for overlapping bounding boxes of colors 497
			for (int m = n + 1; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], 497))
					continue;
				
				bounds2 = VisionUtils.dialateRectangle(_boxes[m], 2, 2);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary colors 288
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			hist = histogram(obj);
			if (hist[288] > 0)
				objects.add(obj);
		}

		return objects;
	}

	
	/*
	 * Update 11/20/2013 - Chris N. Hartley - optimized and reordered method to
	 *                     improve speed and readability.
	 */
	public List<Rectangle> findWhiteBirds() {
		final ArrayList<Rectangle> objects = new ArrayList<Rectangle>();
		final int[] colorSet = { 490, 508, 510 };

		Boolean ignore[] = createIgnoreSegmentSet();
		Rectangle bounds, bounds2, obj;
		int[] hist;
		
		for (int n = 0; n < _nSegments; n++) {
			if (!isValidColor(_colours[n], 490) || ignore[n])
				continue;

			// dilate bounding box around color 490
			bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			obj = _boxes[n];

			// look for overlapping bounding boxes of color 490,508,510
			for (int m = n + 1; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], colorSet))
					continue;
				
				bounds2 = VisionUtils.dialateRectangle(_boxes[m], 2, 2);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary color 510
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			
			// remove objects too high or too low in the image 
			// (probably false positives)
			if ((obj.y < 60) || (obj.y > 385))
				continue;
				
			hist = histogram(obj);
			if (hist[510] > 0 && hist[508] > 0)
				objects.add(obj);
		}

		return objects;
	}

	
	/*
	 * Update 11/20/2013 - Chris N. Hartley - optimized and reordered method to
	 *                     improve speed and readability.
	 */
	public List<Rectangle> findBlackBirds() {
		final ArrayList<Rectangle> objects = new ArrayList<Rectangle>();
		final int[] colorSet = { 488, 146, 64, 0 };
		
		Boolean ignore[] = createIgnoreSegmentSet();
		Rectangle bounds, bounds2, obj;
		int[] hist;
		
		for (int n = 0; n < _nSegments; n++) {
			if (!isValidColor(_colours[n], 488) || ignore[n])
				continue;

			// dilate bounding box around color 488
			bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			obj = _boxes[n];

			// look for overlapping bounding boxes of color 488
			for (int m = n + 1; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], colorSet))
					continue;
				
				bounds2 = VisionUtils.dialateRectangle(_boxes[m], 2, 2);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			// confirm secondary color
			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			
			hist = histogram(obj);
			if ((hist[0] > Math.max(32, 0.1 * obj.width * obj.height))
					&& hist[64] > 0 && hist[385] == 0)
				objects.add(obj);
		}

		return objects;
	}

	
	/**
	 * 
	 * @return
	 */
	public List<Rectangle> findStones() {
		return find(stone3bitSet);
	}

	
	/**
	 * 
	 * @return
	 */
	public List<Rectangle> findIce() {
		return find(ice3bitSet);
	}

	
	/**
	 * 
	 * @return
	 */
	public List<Rectangle> findWood() {
		return find(wood3bitSet);
	}

	
	/*
	 * Update 11/20/2013 - Chris N. Hartley - optimized and reordered method to
	 *                     improve speed and readability.
	 */
	public List<Rectangle> findTNTs() {
		final ArrayList<Rectangle> objects = new ArrayList<Rectangle>();
		final int[] colorSet = { 410, 418 };
		
		Boolean ignore[] = createIgnoreSegmentSet();
		Rectangle bounds, bounds2, obj;
		int[] hist;

		for (int n = 0; n < _nSegments; n++) {
			if (!isValidColor(_colours[n], 410) || ignore[n])
				continue;

			// dilate bounding box around color 410
			bounds = VisionUtils.dialateRectangle(_boxes[n], 2, 2);
			obj = _boxes[n];

			// look for overlapping bounding boxes of color 410
			for (int m = n + 1; m < _nSegments; m++) {
				if (!isValidColor(_colours[m], colorSet))
					continue;
				
				bounds2 = VisionUtils.dialateRectangle(_boxes[m], 2, 2);
				if (bounds.intersects(bounds2)) {
					bounds.add(bounds2);
					obj.add(_boxes[m]);
					ignore[m] = true;
				}
			}

			obj = VisionUtils.dialateRectangle(obj, 2, 2);
			obj = VisionUtils.cropBoundingBox(obj, _nWidth, _nHeight);
			
			//check secondary color
			hist = histogram(obj);
			if (hist[457] > 0 && hist[511] > 0)
				objects.add(obj);
		}
		
		return objects;
	}
	
	
	// find trajectory points
	@SuppressWarnings("unchecked")
	public ArrayList<Point> findTrajPoints() {
		ArrayList<Point> objects = new ArrayList<Point>();
		ArrayList<Point> objectsRemovedNoise;
		
		for (Rectangle traj : find(traj3bitSet, new Tolerance() {

			@Override
			public boolean verify(Rectangle region) {
				return region.height * region.width <= 25;
			}
			
		}))
		{
			objects.add( new Point(	(int)traj.getCenterX(),
									(int)traj.getCenterY()) );
		}

		objectsRemovedNoise = (ArrayList<Point>) objects.clone();

		// remove noise points
		Matrix W = fitParabola(objects);
		double maxError = 10;
		Rectangle menu = new Rectangle(0, 0, 205, 60);

		for (Point o : objects) {
			if (Math.abs(W.get(0, 0) * Math.pow(o.x, 2) + W.get(1, 0) * o.x
					+ W.get(2, 0) - o.y) > maxError) {
				objectsRemovedNoise.remove(o);
			}

			if (menu.contains(o)) {
				objectsRemovedNoise.remove(o);
			}
		}

		return objectsRemovedNoise;
	}

	//fit parabola using maximum likelihood
	// vector W = (w0,w1,w2)T , y = w0*x^2 + w1*x + w2
	public Matrix fitParabola(List<Point> objects) {
		int trainingSize = 60;
		double arrayPhiX[][] = new double[trainingSize][3]; // Training set
		double arrayY[][] = new double[trainingSize][1];

		Rectangle sling = this.findSlingshot();

		Matrix PhiX, Y;
		Matrix W = new Matrix(new double[] { 0, 0, 0 }, 3);
		int i = 0;
		for (Point p : objects) {
			
			//if sling-shot not detected, abandon side noises 
			if (sling == null) {
				if (Math.abs(p.x - _nWidth / 2) <= _nWidth / 6
						&& p.y <= _nHeight / 5 * 3 && i < trainingSize) {
					arrayPhiX[i][0] = Math.pow(p.x, 2);
					arrayPhiX[i][1] = p.x;
					arrayPhiX[i][2] = 1;
					arrayY[i][0] = p.y;
					i++;
				}
			} 
			
			// if sling-shot detected, abandon noises to the left of sling-shot
			else {
				if (p.x >= sling.getCenterX() + sling.width * 2
						&& p.x <= sling.getCenterX() + _nWidth / 3
						&& p.y <= sling.getCenterY() && i < trainingSize) {
					arrayPhiX[i][0] = Math.pow(p.x, 2);
					arrayPhiX[i][1] = p.x;
					arrayPhiX[i][2] = 1;
					arrayY[i][0] = p.y;
					i++;
				}
			}
		}

		PhiX = new Matrix(arrayPhiX);
		Y = new Matrix(arrayY);

		// Maximum likelihood
		try {
			W = PhiX.transpose().times(PhiX).inverse().times(PhiX.transpose())
					.times(Y);
		} catch (Exception e) {
			// if Matrix is singular
			// do nothing
		}
		return W;
	}

	// train parabola using gradient descent
	public Matrix trainParabola(ArrayList<Rectangle> objects) {

		double points[][] = new double[objects.size()][2];
		double alpha = 1e-10;
		int trainingSize = 100;

		double trainingSet[][] = new double[trainingSize][2];
		double SquareError;
		Matrix deltaError;

		int i = 0, j = 0;
		for (Rectangle p : objects) {
			points[i][0] = p.getCenterX();
			points[i][1] = p.getCenterY();
			if (Math.abs(p.getCenterX() - _nWidth / 2) <= _nWidth / 4
					&& Math.abs(p.getCenterY() - _nHeight / 2) <= _nHeight / 5
					&& j < trainingSize) {
				trainingSet[j][0] = points[i][0];
				trainingSet[j][1] = points[i][1];
				j++;
			}
			i++;
		}

	
		Matrix W = new Matrix(new double[] { 0, 0, 0 }, 3);// parabola
															// parameters
		
		Matrix phiX;
		for (int x = -50; x < 50; x++) {
			if (x + 50 < trainingSize) {
				trainingSet[x + 50][0] = x;
				trainingSet[x + 50][1] = -x * x + 20 * x + 1;
			}
		}

		double xn, yn;
		for (int it = 0; it < 50000; it++) {
			SquareError = 0.;
			for (int n = 0; n < trainingSize; n++) {
				if (trainingSet[n][0] > 0) {
					xn = trainingSet[n][0];
					yn = trainingSet[n][1];
					phiX = new Matrix(new double[] { Math.pow(xn, 2), xn, 1. },
							3);

					deltaError = phiX.times((yn - W.transpose().times(phiX)
							.get(0, 0)));
					

					W = W.plus(deltaError.times(alpha));
					SquareError += Math.pow(
							yn - phiX.transpose().times(W).get(0, 0), 2);

				}
			}
			if (it % 1000 == 0) {
				System.out.print(SquareError + "\n");
				W.print(1, 30);
			}
		}

		return W;
	}

	// find bounding boxes around an arbitrary color code
	public List<Rectangle> findColour(int colorCode) {
		ArrayList<Rectangle> objects = new ArrayList<Rectangle>();

		for (int n = 0; n < _nSegments; n++)
			if (_colours[n] == colorCode)
				objects.add(_boxes[n]);

		return objects;
	}

	// query the color at given pixel
	public Integer query(Point p) {
		if ((p.x >= _nWidth) || (p.y >= _nHeight)) {
			System.err.println("pixel (" + p.x + ", " + p.y
					+ ") is out of range");
			return null;
		}

		return _colours[_segments[p.y][p.x]];
	}

	// query colors within given bounding box
	public Set<Integer> query(Rectangle r) {
		Set<Integer> s = new HashSet<Integer>();
		
		for (int n = 0; n < _nSegments; n++)
			if (r.contains(_boxes[n]))
				s.add(_colours[n]);
			
		return s;
	}

	// compute a histogram of colors within a given bounding box
	public int[] histogram(Rectangle r) {
		int[] h = new int[512];
		Arrays.fill(h, 0);

		for (int y = r.y; y < r.y + r.height; y++) {
			if (y >= 0 && y < _nHeight)
				for (int x = r.x; x < r.x + r.width; x++)
					if (x >= 0 && x < _nWidth)
						h[_colours[_segments[y][x]]] += 1;
		}

		return h;
	}

	// perform preprocessing of a new screenshot
	private void processScreenShot(BufferedImage screenshot) {
		// extract width and height
		_nHeight = screenshot.getHeight();
		_nWidth = screenshot.getWidth();
		
		if ((_nHeight != 480) && (_nWidth != 840)) {
			System.err.println("ERROR: expecting 840-by-480 image");
			System.exit(1);
		}

		// quantize to 3-bit color
		_scene = new int[_nHeight][_nWidth];
		int color;
		for (int y = 0; y < _nHeight; y++) {
			for (int x = 0; x < _nWidth; x++) {
				color = screenshot.getRGB(x, y);
				_scene[y][x] = ((color & 0x00e00000) >> 15)
						| ((color & 0x0000e000) >> 10)
						| ((color & 0x000000e0) >> 5);
			}
		}

		// find connected components
		_segments = VisionUtils.findConnectedComponents(_scene);
		_nSegments = VisionUtils.countComponents(_segments);
		// System.out.println("...found " + _nSegments + " components");

		_colours = new int[_nSegments];
		for (int y = 0; y < _nHeight; y++) {
			for (int x = 0; x < _nWidth; x++) {
				_colours[_segments[y][x]] = _scene[y][x];
			}
		}

		// find bounding boxes and segment colors
		_boxes = VisionUtils.findBoundingBoxes(_segments);
	}

	
	/**
	 * 
	 * @return
	 */
	private final Boolean[][] createIgnorePixelSet() {
		Boolean pixels[][] = new Boolean[_nHeight][_nWidth];
		for (int i = 0; i < _nHeight; i++)
			for (int j = 0; j < _nWidth; j++)
				pixels[i][j] = false;
		
		return pixels;
	}
	
	private final Boolean[] createIgnoreSegmentSet() {
		Boolean segs[] = new Boolean[_nSegments];
		Arrays.fill(segs, false);
		
		return segs;
	}

	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param type
	 * @return
	 */
	private final boolean isValidColor(int x, int y, int[] type) {
		return isValidColor(_scene[y][x], type);
	}
	
	
	/**
	 * 
	 * @param color
	 * @param type
	 * @return
	 */
	private static final boolean isValidColor(int color, int[] type) {
		for (int typeColor : type)
			if (typeColor == color) return true;
		
		return false;
	}
	
	
	/**
	 * 
	 * @param color
	 * @param type
	 * @return
	 */
	private static final boolean isValidColor(int color, int type) {
		return color == type;
	}
	
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param colorSet
	 * @param trajPts
	 * @param bounds
	 * @param ignorePxs
	 */
	private final void addPointIfValidColor(int x, int y, int[] colorSet,
			Collection<Point> points, Rectangle bounds, Boolean[][] ignorePxs)
	{
		if (y < 0 || y > ignorePxs.length - 1)
			return;
		if (x < 0 || x > ignorePxs[0].length - 1)
			return;
		if (ignorePxs[y][x])
			return;
		if (isValidColor(x, y, colorSet)) {
			points.add( new Point(x, y) );
			bounds.add(x, y);
			ignorePxs[y][x] = true;
		}
	}
	
	
	/**
	 * Returns a {@link List} of type {@link Rectangle} for all regions which
	 * which are comprised of the specified set, {@code int[][]}, of 3-bit color
	 * values. This also require that the regions found must be over the 
	 * threshold specified by {@link regionThreshold}.
	 * 
	 * @param colorSet
	 * 
	 * @return
	 * 
	 * @see java.awt.Rectangle
	 * @see java.util.List
	 */
	protected final List<Rectangle> find(int[] colorSet) {
		return find(colorSet, new Tolerance() {
			final Rectangle ignoreArea = new Rectangle(0, 0, 190, 55);
		
			@Override
			public boolean verify(Rectangle region) {
				return region.width * region.height > regionThreshold
						&& !ignoreArea.contains(region);
			}
			
		} );
	}
	
	
	/**
	 * Returns a {@link List} of type {@link Rectangle} for all regions which
	 * which are comprised of the specified set, {@code int[][]}, of 3-bit color
	 * values. Each region must satisfy the {@code verify(Rectangle)} of the
	 * specified {@link Tolerance} interface.
	 * 
	 * @param colorSet
	 * @param tolerance
	 * 
	 * @return
	 */
	protected final List<Rectangle> find(int[] colorSet, Tolerance tolerance) {
		final ArrayList<Rectangle> regions = new ArrayList<Rectangle>();
		final LinkedList<Point> pointList = new LinkedList<Point>();
		
		Boolean ignorePixel[][] = createIgnorePixelSet();
		Rectangle rect = null;
		
		for (int i = 0; i < _nHeight; i++) {
			for (int j = 0; j < _nWidth; j++) {
				if (ignorePixel[i][j] || !isValidColor(j, i, colorSet))
					continue;
				
				pointList.clear();
				pointList.add( new Point(j, i) );
				ignorePixel[i][j] = true;
				rect = new Rectangle(j, i, 0, 0);
				
				while (!pointList.isEmpty()) {
					Point p = pointList.pop();
					addPointIfValidColor(p.x, p.y + 1, colorSet, pointList, rect, ignorePixel);
					addPointIfValidColor(p.x, p.y - 1, colorSet, pointList, rect, ignorePixel);
					addPointIfValidColor(p.x + 1, p.y, colorSet, pointList, rect, ignorePixel);
					addPointIfValidColor(p.x - 1, p.y, colorSet, pointList, rect, ignorePixel);
					addPointIfValidColor(p.x - 1, p.y - 1, colorSet, pointList, rect, ignorePixel);
					addPointIfValidColor(p.x + 1, p.y - 1, colorSet, pointList, rect, ignorePixel);
					addPointIfValidColor(p.x + 1, p.y + 1, colorSet, pointList, rect, ignorePixel);
					addPointIfValidColor(p.x - 1, p.y + 1, colorSet, pointList, rect, ignorePixel);
				}
				
				if (tolerance.verify(rect))
					regions.add(rect);
			}
		}
		
		return regions;
	}
	
	
	/**
	 * 
	 * 
	 * @author Chris Hartley
	 */
	protected static interface Tolerance {
		
		/**
		 * 
		 * @param region
		 * @return
		 */
		public boolean verify(Rectangle region);
		
	}

}
