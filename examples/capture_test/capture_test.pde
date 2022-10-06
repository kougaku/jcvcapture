import jcvcapture.*;

JcvCapture cam;

void setup() {
  size(640, 480);
  
  cam = new JcvCapture(this, 640, 480);  // 640x480, 0th device, use VideoInput capture

  // cam = new JcvCapture(this, 640, 480, 0);                        // 640x480, 0th device, use VideoInput capture
  // cam = new JcvCapture(this, 640, 480, 0, JcvCapture.VIDEOINPUT); // 640x480, 0th device, use VideoInput capture
  // cam = new JcvCapture(this, 640, 480, 0, JcvCapture.OPENCV);     // 640x480, 0th device, use OpenCV capture (Startup is slow)
  
  cam.frameRate(30);
  cam.start();
}

void draw() {
  cam.read();
  image(cam, 0, 0);
}
