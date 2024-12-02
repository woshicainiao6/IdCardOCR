package org.zlt.bean;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

public class Image {
    private Mat rotatedImage;
    private MatOfPoint rotatedContours;

    public Image(Mat rotatedImage, MatOfPoint rotatedContours) {
        this.rotatedImage = rotatedImage;
        this.rotatedContours = rotatedContours;
    }

    public Mat getRotatedImage() {
        return rotatedImage;
    }

    public MatOfPoint getRotatedContours() {
        return rotatedContours;
    }
}
