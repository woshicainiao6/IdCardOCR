package org.zlt.utils;

import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.zlt.bean.Image;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;

public class RotateImage {
    public static Image rotateImage(Mat originalImage, MatOfPoint largestContour) throws InterruptedException, IOException, TesseractException {
        if (checkIsRect(largestContour, originalImage.size())) {
//            Scalar contourColor = new Scalar(0, 255, 0); // 绿色的轮廓颜色
//            Imgproc.drawContours(originalImage, Collections.singletonList(largestContour), -1, contourColor, 2);

            // 使用最小外接矩形来获取旋转角度
            RotatedRect minAreaRect = Imgproc.minAreaRect(new MatOfPoint2f(largestContour.toArray()));
            double angle = minAreaRect.angle;

            // 确保角度在-90到+90之间，以便选择最小的旋转角度
            if (angle < -45) {
                angle += 90; // 如果角度小于-45，则旋转90度使得角度最小
            } else if (angle > 45) {
                angle -= 90; // 如果角度大于45，则旋转90度使得角度最小
            }
//            System.out.println("旋转角度：" + angle);

            // 获取旋转矩阵
            Point center = new Point(originalImage.cols() / 2.0, originalImage.rows() / 2.0);
            Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);

            // 旋转图像
            Mat rotatedImage = new Mat();
            Imgproc.warpAffine(originalImage, rotatedImage, rotationMatrix, originalImage.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar(0, 0, 0));

            // 旋转 largestContour
            MatOfPoint2f contour2f = new MatOfPoint2f(largestContour.toArray());
            MatOfPoint2f rotatedContour2f = new MatOfPoint2f();

            // 将轮廓的每个点根据旋转矩阵进行旋转
            Core.transform(contour2f, rotatedContour2f, rotationMatrix);

            // 将旋转后的点转换回 MatOfPoint
            MatOfPoint rotatedLargestContour = new MatOfPoint(rotatedContour2f.toArray());

            // 返回旋转后的图像和旋转后的 largestContour
            return new Image(rotatedImage, rotatedLargestContour);
        } else {
            // 如果不是矩形，返回原始图像和轮廓
            return new Image(originalImage, largestContour);
        }
    }

    // 确保图片是水平的
    public static Mat horizontalImage(Mat originalImage) {
        // 检查是否需要旋转
        if (originalImage.cols() < originalImage.rows()) {
            // 获取中心点
            Point center = new Point(originalImage.cols() / 2.0, originalImage.rows() / 2.0);

            // 获取旋转矩阵，旋转 90 度
            Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, 90, 1.0);

            // 计算旋转后图片的新边界
            int newWidth = originalImage.rows();
            int newHeight = originalImage.cols();

            // 调整旋转矩阵的平移部分以适配新的边界
            rotationMatrix.put(0, 2, rotationMatrix.get(0, 2)[0] + (newWidth - originalImage.cols()) / 2.0);
            rotationMatrix.put(1, 2, rotationMatrix.get(1, 2)[0] + (newHeight - originalImage.rows()) / 2.0);

            // 创建新的目标矩阵
            Mat rotatedImage = new Mat();

            // 执行仿射变换，输出完整的图像
            Imgproc.warpAffine(originalImage, rotatedImage, rotationMatrix, new Size(newWidth, newHeight));
            return rotatedImage;
        }
        return originalImage; // 如果宽度大于高度，则不做旋转
    }

    // 如果未封闭找到边缘轮廓
    public static MatOfPoint findMaxContours(Mat originalImage, List<MatOfPoint> contours) throws
            IOException, TesseractException {
        // 如果不是矩形，生成最外层轮廓线组成的矩形
        MatOfPoint2f allPoints = new MatOfPoint2f();
        for (MatOfPoint contour : contours) {
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            allPoints.push_back(contour2f); // 合并所有轮廓点
        }

        // 找到最外层的极值点
        Point[] pointsArray = allPoints.toArray();
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (Point point : pointsArray) {
            if (point.x < minX) minX = point.x;
            if (point.y < minY) minY = point.y;
            if (point.x > maxX) maxX = point.x;
            if (point.y > maxY) maxY = point.y;
        }

        // 创建闭合的矩形轮廓
        MatOfPoint rectangleContour = new MatOfPoint(new Point(minX, minY), new Point(maxX, minY), new Point(maxX, maxY), new Point(minX, maxY));

//        // 在原图上绘制矩形轮廓
//        Scalar rectColor = new Scalar(0, 250, 78); // 蓝色的矩形颜色
//        Imgproc.drawContours(originalImage, Collections.singletonList(rectangleContour), -1, rectColor, 2); // 绘制矩形
        return rectangleContour;
    }

    public static boolean checkIsRect(MatOfPoint largestContour, Size originalImageSize) {
        // 转换轮廓为浮点类型
        MatOfPoint2f contour2f = new MatOfPoint2f(largestContour.toArray());
        MatOfPoint2f approx = new MatOfPoint2f();

        // 设置近似精度
        double epsilon = 0.04 * Imgproc.arcLength(contour2f, true);
        Imgproc.approxPolyDP(contour2f, approx, epsilon, true);

//        System.out.println("近似后的顶点数：" + approx.total());

        // 如果轮廓面积过小，直接排除
        double contourArea = Imgproc.contourArea(largestContour);
        double imageArea = originalImageSize.width * originalImageSize.height;
        if (contourArea < imageArea * 0.1) { // 面积小于图像面积的 2%，不判定为矩形
            return false;
        }

        // 如果近似后的轮廓顶点数为 4，则可能是矩形
        if (approx.total() == 4) {
            // 转换顶点为 MatOfPoint 格式
            MatOfPoint approxPoints = new MatOfPoint(approx.toArray());

            // 获取外接矩形
            Rect boundingRect = Imgproc.boundingRect(approxPoints);

            // 计算长宽比
            double aspectRatio = (double) boundingRect.width / boundingRect.height;

            // 判定为矩形或正方形
            if (aspectRatio >= 0.8 && aspectRatio <= 1.2) {
                return true; // 正方形
            } else return aspectRatio > 1.2 || aspectRatio < 0.8; // 长方形
        }

        // 不符合条件，不是矩形
        return false;
    }

    public static List<MatOfPoint> getOriginalImgEdge(Mat originalImage) throws InterruptedException {
        // 1. 转为灰度图像
        Mat grayImage = new Mat();
        Imgproc.cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY);

        // 2. 增强对比度（使用 CLAHE）
        Mat enhancedImage = enhanceContrast(grayImage);

        // 3. 降噪处理（结合双边滤波和高斯模糊）
        Mat filteredImage = denoiseImage(enhancedImage);

        // 6. 自动阈值计算（Otsu 方法）
        double otsuThreshVal = Imgproc.threshold(filteredImage, new Mat(), 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        double lowerThresh = otsuThreshVal * 0.4;

        // 7. Canny 边缘检测（结合 Otsu 阈值）
        Mat edges = detectCannyEdges(filteredImage, lowerThresh, otsuThreshVal);

        // 8. 膨胀操作（修复边缘）
        Mat dilatedEdges = dilateEdges(edges);

        // 9. 查找轮廓
        List<MatOfPoint> contours = findContours(dilatedEdges);

        return contours; // 返回符合条件的轮廓
    }

    public static MatOfPoint getMaximumContour(Mat originalImage, List<MatOfPoint> contours) throws
            InterruptedException, TesseractException, IOException {
        // 创建一个黑色图像用于绘制轮廓
        Mat contourImage = new Mat(originalImage.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));

        double maxArea = 0;
        MatOfPoint largestContour = null;

        // 遍历所有轮廓
        for (MatOfPoint contour : contours) {
            // 计算轮廓的面积
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                largestContour = contour;  // 记录最大面积的轮廓
            }
        }
        // 判断最大轮廓是否为矩形，若是则返回该轮廓，否则递归处理
        if (largestContour != null && checkIsRect(largestContour, originalImage.size())) {
            return largestContour;
        } else {
            return findMaxContours(originalImage, contours);  // 如果不是矩形，继续寻找最大轮廓
        }
    }

    // 增强对比度（使用 CLAHE）
    public static Mat enhanceContrast(Mat grayImage) {
        Mat enhancedImage = new Mat();
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(3.0); // 增强对比度的限制值
        clahe.apply(grayImage, enhancedImage);
        return enhancedImage;
    }

    // 降噪处理（结合双边滤波和高斯模糊）
    public static Mat denoiseImage(Mat enhancedImage) {
        Mat filteredImage = new Mat();
        Imgproc.bilateralFilter(enhancedImage, filteredImage, 9, 150, 150); // 双边滤波，保留边缘
        Imgproc.GaussianBlur(filteredImage, filteredImage, new Size(5, 5), 1); // 轻度模糊
        return filteredImage;
    }

    public static Mat detectCannyEdges(Mat filteredImage, double lowerThresh, double otsuThreshVal) {
        Mat edges = new Mat();
        Imgproc.Canny(filteredImage, edges, lowerThresh, otsuThreshVal);
        return edges;
    }

    // 膨胀操作（修复边缘）
    public static Mat dilateEdges(Mat edges) {
        Mat dilatedEdges = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(edges, dilatedEdges, kernel);
        return dilatedEdges;
    }

    // 查找轮廓
    public static List<MatOfPoint> findContours(Mat dilatedEdges) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(dilatedEdges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }
    // static {
    //     // 加载OpenCV动态库
    //     URL url = ClassLoader.getSystemResource("opencv/opencv_java4100.dll");
    //     System.load(url.getPath());
    // }
    // public static void main(String[] args) {
    //     Mat originalImage = Imgcodecs.imread("D:\\Develop\\Java\\JavaProject\\OCR_Final\\images\\test11.jpg");
    //     horizontalImage(originalImage);
    //     waitKey();
    // }
}
