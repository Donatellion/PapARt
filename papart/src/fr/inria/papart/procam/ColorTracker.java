/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import fr.inria.papart.calibration.PlanarTouchCalibration;
import fr.inria.papart.multitouch.TouchPointTracker;
import fr.inria.papart.multitouch.TrackedElement;
import fr.inria.papart.multitouch.detection.TouchDetectionColor;
import fr.inria.papart.procam.camera.TrackedView;
import fr.inria.papart.utils.MathUtils;
import fr.inria.papart.utils.SimpleSize;
import java.util.ArrayList;
import java.util.HashMap;
import processing.core.PConstants;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class ColorTracker {

    private final PaperScreen paperScreen;
    private final TrackedView trackedView;
    private PImage capturedImage;

//    private final HashMap<String, Integer> trackedColors;
    
    private final TouchDetectionColor touchDetectionColor;
    private final byte[] colorFoundArray;
    private final ArrayList<TrackedElement> trackedElements;
    
    private float brightness, saturation;
    private float hue;
    private float redThreshold;
    
    public ColorTracker(PaperScreen paperScreen){
        this(paperScreen, 1);
    }
    
    public ColorTracker(PaperScreen paperScreen, float scale) {
        this.paperScreen = paperScreen;
        this.trackedView = new TrackedView(paperScreen);
        this.trackedView.setScale(scale);
        trackedView.init();

//        this.trackedColors = new HashMap<>();

        SimpleSize size = new SimpleSize(paperScreen.drawingSize);
        touchDetectionColor = new TouchDetectionColor(size);

        PlanarTouchCalibration calib = Papart.getPapart().getDefaultColorTouchCalibration();
        calib.setMaximumDistance(calib.getMaximumDistance() * scale);
        calib.setMinimumComponentSize((int) (calib.getMinimumComponentSize()* scale * scale)); // Quadratic (area)
        calib.setSearchDepth((int) (calib.getSearchDepth() * scale));
        calib.setTrackingMaxDistance(calib.getTrackingMaxDistance() * scale);
        calib.setMaximumRecursion((int) (calib.getMaximumRecursion() * scale));
        
        touchDetectionColor.setCalibration(calib);
        trackedElements = new ArrayList<TrackedElement>();

        colorFoundArray = touchDetectionColor.createInputArray();
        hue = 40;
        saturation = 70;
        brightness = 80;
        redThreshold = 15;
    }

    
    /**
     * For now it only finds one color.
     *
     * @param name
     * @param reference Reference color found by the camera somewhere. -1 to
     * disable it.
     */
    public ArrayList<TrackedElement> findColor(String name, int reference, int time, int erosion) {

        capturedImage = trackedView.getViewOf(paperScreen.getCameraTracking());
        capturedImage.loadPixels();

        // Reset the colorFoundArray
        touchDetectionColor.resetInputArray();

        // Default to RGB 255 for now. 
        paperScreen.getGraphics().colorMode(PConstants.RGB, 255);

        // each pixels.
        byte id = 0;

        for (int x = 0; x < capturedImage.width; x++) {
            for (int y = 0; y < capturedImage.height; y++) {
                int offset = x + y * capturedImage.width;
                int c = capturedImage.pixels[offset];
                boolean good = MathUtils.colorFinderHSB(paperScreen.getGraphics(),
                        reference, c, hue, saturation, brightness);

                if (name == "red") {
                    boolean red = MathUtils.isRed(paperScreen.getGraphics(),
                            c, reference, redThreshold);
                    good = good && red;
                }
                if (good) {
                    colorFoundArray[offset] = id;
                }

            }
        }

        ArrayList<TrackedElement> newElements = touchDetectionColor.compute(time, erosion);
        TouchPointTracker.trackPoints(trackedElements, newElements, time);

        return trackedElements;
    }

    public PImage getTrackedImage(){
        return this.capturedImage;
    }
    
    public ArrayList<TrackedElement> getTrackedElements() {
        return trackedElements;
    }
//
//    /**
//     * Add a color to track, returns the associated ID to modifiy it.
//     *
//     * @param name Tag of the color to store, like "red", or "blue"
//     * @param initialValue initial value
//     */
//    public void addTrackedColor(String name, int initialValue) {
//        this.trackedColors.put(name, initialValue);
//    }
//
//    /**
//     * Update the value of a given color.
//     *
//     * @param name
//     * @param value
//     */
//    public void updateTrackedColor(String name, int value) {
//        this.trackedColors.replace(name, value);
//    }
//
//    /**
//     * Remove the tracking of a color
//     *
//     * @param name
//     */
//    public void removeTrackedColor(String name) {
//        this.trackedColors.remove(name);
//    }
//    
    
    public void setThresholds(float hue, float saturation, float brightness){
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }
    public void setRedThreshold(float red){
        this.redThreshold = red;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getHue() {
        return hue;
    }

    public float getRedThreshold() {
        return redThreshold;
    }

}
