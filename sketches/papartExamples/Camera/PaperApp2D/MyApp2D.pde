public class MyApp extends PaperScreen {

  void setup() {
    setDrawingSize(297, 210);
    loadMarkerBoard(sketchPath() + "/data/big.cfg", 297, 210);
  }

  void draw() {
    beginDraw2D();
    background(100, 0, 0);
    fill(200, 100, 20);
    rect(10, 10, 100, 30);
    endDraw();
  }
 }
