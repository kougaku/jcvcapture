package jcvcapture;

//VideoライブラリのCaptureとのAPIの互換性はないです。
//captureEvent()によるコールバックはないです。

import java.awt.image.BufferedImage;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;


public class JcvCapture extends PImage implements PConstants {

	public static final int OPENCV = 0;
	public static final int VIDEOINPUT = 1;

	FrameGrabber grabber;
	Java2DFrameConverter converter;

	public int sourceWidth = 0;
	public int sourceHeight = 0;

	protected int[] copyPixels = null;
	protected boolean capturing = false;
	protected boolean firstFrame = true;
	protected boolean outdatedPixels = true;

	public JcvCapture(PApplet parent) {
		this(parent, 640, 480, 0, VIDEOINPUT);
	}

	public JcvCapture(PApplet parent, int width, int height) {
		this(parent, width, height, 0, VIDEOINPUT);
	}

	public JcvCapture(PApplet parent, int width, int height, int device_index) {
		this(parent, width, height, device_index, VIDEOINPUT);
	}

	public JcvCapture(PApplet parent, int width, int height, int device_index, int grabber_mode) {
		super(width, height, RGB);
		this.parent = parent;

		if ( grabber_mode == OPENCV ) {
			grabber = new OpenCVFrameGrabber(device_index);
		} else {
			grabber = new VideoInputFrameGrabber(device_index);
		}
		grabber.setImageWidth(width);
		grabber.setImageHeight(height);
		converter = new Java2DFrameConverter();

		try {
			parent.registerMethod("dispose", this);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void frameRate(float ifps) {
		grabber.setFrameRate((double)ifps);
	}

	public void start() {
		try {
			grabber.start();
			sourceWidth = grabber.getImageWidth();
			sourceHeight = grabber.getImageHeight();

			if ( this.width != sourceWidth || this.height != sourceHeight ) {
				PApplet.println("[JcvCapture] The requested size (" + this.width + "x" + this.height + ") could not be set. New size is " + sourceWidth + "x" + sourceHeight + ".");
			}
		}
		catch (Exception e) {
		}
		capturing = true;
	}


	public void stop() {
		capturing = false;
		try {
			grabber.stop();
		}
		catch (Exception e) {
		}
	}

	public void dispose() {
		pixels = null;
		parent.g.removeCache(this);
		parent.unregisterMethod("dispose", this);
	}

	protected void finalize() throws Throwable {
		try {
			dispose();
		}
		finally {
			super.finalize();
		}
	}

	public synchronized void read() {
		if ( !capturing ) return;

		if (firstFrame) {
			super.init(sourceWidth, sourceHeight, RGB, 1);
			firstFrame = false;
		}
		if (copyPixels == null) {
			copyPixels = new int[sourceWidth * sourceHeight];
		}

		try {
			org.bytedeco.javacv.Frame frame = grabber.grab();
			BufferedImage bufImg = converter.getBufferedImage(frame);
			for (int y=0; y<this.height; y++ ) {
				for (int x=0; x<this.width; x++ ) {
					copyPixels[y * this.width + x] = bufImg.getRGB(x, y);
				}
			}
		}
		catch (Exception e) {
		}
		int[] temp = pixels;
		pixels = copyPixels;
		updatePixels();
		copyPixels = temp;
	}


	/////////////////////////////////////////////////////////////
	// for PImage

	@Override
	public synchronized void loadPixels() {
		super.loadPixels();
		if ( true ) {
			outdatedPixels = false;
		}
	}

	@Override
	public int get(int x, int y) {
		if (outdatedPixels) loadPixels();
		return super.get(x, y);
	}

	protected void getImpl(int sourceX, int sourceY,
			int sourceWidth, int sourceHeight,
			PImage target, int targetX, int targetY) {
		if (outdatedPixels) loadPixels();
		super.getImpl(sourceX, sourceY, sourceWidth, sourceHeight,
				target, targetX, targetY);
	}
}