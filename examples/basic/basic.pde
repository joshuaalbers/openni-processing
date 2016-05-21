import OpenNI2.*;

OpenNI2 sensor;

void setup() {
  size(640, 480);
  sensor = new OpenNI2(this);
  //sensor.run();
  println(sensor.getSensors());
  sensor.startDepth();
}

void draw() {
  //background(frameCount % 256);
  try {
    image(sensor.getDepth(), 0, 0);
  } 
  catch(Exception e) {
  }
}