package OpenNI2;

import org.openni.*;

import processing.core.PApplet;
import processing.core.PImage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import java.lang.reflect.Method;

public class OpenNI2 implements VideoStream.NewFrameListener {
  static PApplet p5parent;
  public Device mDevice;
  private ArrayList<SensorType> mDeviceSensors;
  private HashMap<SensorType, VideoStream> mVideoStreams;
	private HashMap<SensorType, VideoFrameRef> mVideoFrames;

	private VideoStream colorStream;
	private VideoStream depthStream;
	private VideoStream irStream;

	private VideoFrameRef depthFrame;

	private int[] depthPixels;
	PImage depthImage;

	//Method frameEventMethod;

  /**
   * @param _p parent (usually "this")
   */

  public OpenNI2(PApplet _p) {
    p5parent = _p;
    //System.out.println(System.getProperty("java.library.path"));

		// try {
		// 			frameEventMethod = p5parent.getClass().getMethod("onFrameReady",new Class[] { OpenNI2.class });
		// 		} catch (Exception e) {
		// 			//System.out.println("You are missing the depthEventMethod() method. " + e);
		// 			frameEventMethod = null;
		// 		}

    try {
      OpenNI.initialize();

      List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
      if (devicesInfo.isEmpty()) {
        //System.out.showMessageDialog(null, "No device is connected", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      mDevice = Device.open(devicesInfo.get(0).getUri());

      mDeviceSensors = new ArrayList<SensorType>();
      mVideoStreams = new HashMap<SensorType, VideoStream>();

      if (mDevice.getSensorInfo(SensorType.COLOR) != null) {
				System.out.println("Color stream detected");
        mDeviceSensors.add(SensorType.COLOR);
				List<VideoMode> modes = mDevice.getSensorInfo(SensorType.COLOR).getSupportedVideoModes();
				for (int i = 0; i<modes.size(); i++) {
					VideoMode mode = modes.get(i);
					int fps = mode.getFps();
					PixelFormat format = mode.getPixelFormat();
					int width = mode.getResolutionX();
					int height = mode.getResolutionY();
					System.out.println(width + "x" + height + ", " + fps + "fps, " + format);
				}
      }

      if (mDevice.getSensorInfo(SensorType.DEPTH) != null) {
				System.out.println("DEPTH stream detected");
        mDeviceSensors.add(SensorType.DEPTH);
				List<VideoMode> modes = mDevice.getSensorInfo(SensorType.DEPTH).getSupportedVideoModes();
				for (int i = 0; i<modes.size(); i++) {
					VideoMode mode = modes.get(i);
					int fps = mode.getFps();
					PixelFormat format = mode.getPixelFormat();
					int width = mode.getResolutionX();
					int height = mode.getResolutionY();
					System.out.println(width + "x" + height + ", " + fps + "fps, " + format);
				}
      }

      if (mDevice.getSensorInfo(SensorType.IR) != null) {
				System.out.println("IR stream detected");
        mDeviceSensors.add(SensorType.IR);

				List<VideoMode> modes = mDevice.getSensorInfo(SensorType.IR).getSupportedVideoModes();
				for (int i = 0; i<modes.size(); i++) {
					VideoMode mode = modes.get(i);
					int fps = mode.getFps();
					PixelFormat format = mode.getPixelFormat();
					int width = mode.getResolutionX();
					int height = mode.getResolutionY();
					System.out.println(width + "x" + height + ", " + fps + "fps, " + format);
				}
      }
    }
    catch(Exception e) {
      System.err.println(e);
    }

    p5parent.registerMethod("dispose", this);
  }

  public ArrayList<SensorType> getSensors() {
    return mDeviceSensors;
  }

	@Override
	public void onFrameReady(VideoStream stream) {
		//System.out.println(depthStream);
		if (stream.getSensorInfo().getSensorType() == SensorType.DEPTH) {
			if (depthFrame != null) {
					depthFrame.release();
					depthFrame = null;
			}
			depthFrame = stream.readFrame();
			ByteBuffer frameData = depthFrame.getData().order(ByteOrder.LITTLE_ENDIAN);

			depthPixels = new int[depthFrame.getWidth() * depthFrame.getHeight()];
			//System.out.println(depthPixels.length);
			//System.out.println(frameData.remaining());

			switch (depthFrame.getVideoMode().getPixelFormat())
			{
					case DEPTH_1_MM:
					case DEPTH_100_UM:
					case SHIFT_9_2:
					case SHIFT_9_3:
							frameData.rewind();
							int pos = 0;
							//System.out.println(frameData.remaining());
							while(frameData.remaining() > 0) {
								int depth = (int)frameData.getShort() & 0xFFFF;

									depthPixels[pos] = depth;
									//short pixel = (short)mHistogram[depth];
									//depthPixels[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8);
									pos++;
							}
							break;
					case RGB888:
							pos = 0;
							while (frameData.remaining() > 0) {
									int red = frameData.get() & 0xFF;
									int green = frameData.get() & 0xFF;
									int blue = frameData.get() & 0xFF;
									depthPixels[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;
									pos++;
							}
							break;
					default:
							// don't know how to draw
							depthFrame.release();
							depthFrame = null;
			}

      depthImage = p5parent.createImage(depthFrame.getWidth(), depthFrame.getHeight(), PApplet.RGB);
      depthImage.loadPixels();
      depthImage.pixels = depthPixels;
      depthImage.updatePixels();

		}
	}

	public void startDepth() {
		depthStream = VideoStream.create(mDevice, SensorType.DEPTH);
		List<VideoMode> modes = mDevice.getSensorInfo(SensorType.DEPTH).getSupportedVideoModes();
		VideoMode mode = modes.get(0);
		depthStream.setVideoMode(mode);
		if(depthStream != null) {
				depthStream.addNewFrameListener(this);
				VideoMode streamMode = depthStream.getVideoMode();
				int fps = streamMode.getFps();
				PixelFormat format = streamMode.getPixelFormat();
				int width = streamMode.getResolutionX();
				int height = streamMode.getResolutionY();
				System.out.println(width + "x" + height + ", " + fps + "fps, " + format);

		} else {
			System.out.println("huh. that stream is null");
		}

		depthStream.start();
		//System.out.println(depthStream);
	}

	public PImage getDepth() {
		//System.out.println(depthFrame);
		// System.out.println(depthPixels.length);

		return depthImage;
	}

  /**
   * The dispose method is registered in the constructor and will run when the parent sketch closes.
   *
   */
  public void dispose() {
    OpenNI.shutdown();
  }

	// public void run() {
	// 		while (true) {
	// 					//System.out.println(depthStream);
	// 					//System.out.println("running");
	// 					//onFrameReady(depthStream);
	// 				try {
	// 						Thread.sleep(200);
	// 				} catch (InterruptedException e) {
	// 						e.printStackTrace();
	// 				}
	// 		}
	// 		//mFrame.dispose();
	// }
}
