package org.zlt.utils;

import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.opencv.highgui.HighGui.imshow;

// 处理图像的方法
public class ProcessImg {
    // Mat图像进行格式转换
    public static BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();

        BufferedImage image;

        // 如果是单通道图像（灰度图）
        if (channels == 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            mat.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        } else {
            // 转换为RGB图像
            Mat matRgb = new Mat();
            Imgproc.cvtColor(mat, matRgb, Imgproc.COLOR_BGR2RGB);  // 将BGR转换为RGB
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            matRgb.get(0, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData());
        }

        return image;
    }

    // 检查图片的纵横比
    public static Mat comHorizontalRatio(Mat originalImage) {
        // 获取图像宽度和高度
        int width = originalImage.cols();
        int height = originalImage.rows();

        // 计算纵横比
        double aspectRatio = (double) width / height;
        // System.out.println("当前图片的纵横比: " + aspectRatio);

        // 设置合理的纵横比范围（这里假设合理范围是 1:3 到 3:1）
        double minAspectRatio = 0.8;
        double maxAspectRatio = 1.6;

        // 如果纵横比过大或过小，需要裁剪
        if (aspectRatio > maxAspectRatio) {
            // 宽度过大，从左右两侧裁剪
            int newWidth = (int) (height * maxAspectRatio); // 计算目标宽度
            int cropStartX = (width - newWidth) / 2; // 从中间裁剪
            Rect cropRect = new Rect(cropStartX, 0, newWidth, height);

            Mat croppedImage = new Mat(originalImage, cropRect);

            // 计算裁剪后纵横比
            double newAspectRatio = (double) croppedImage.cols() / croppedImage.rows();

            return croppedImage;
        } else if (aspectRatio < minAspectRatio) {
            // 高度过大，从上下两侧裁剪
            int newHeight = (int) (width / minAspectRatio); // 计算目标高度
            int cropStartY = (height - newHeight) / 2; // 从中间裁剪
            Rect cropRect = new Rect(0, cropStartY, width, newHeight);

            Mat croppedImage = new Mat(originalImage, cropRect);

            // 计算裁剪后纵横比
            double newAspectRatio = (double) croppedImage.cols() / croppedImage.rows();
            return croppedImage;
        }

        return originalImage;
    }

    // 重新设置图像的大小，根据设置的指定宽度进行放缩
    public static Mat resizeImg(Mat originalImage, int targetWidth) {
        // 调整图像大小，保持宽高比
        Mat resizedImage = new Mat();
        double aspectRatio = (double) originalImage.height() / originalImage.width();
        int targetHeight = (int) (targetWidth * aspectRatio); // 计算目标高度
        Imgproc.resize(originalImage, resizedImage, new Size(targetWidth, targetHeight));
        return resizedImage;
    }

    // 图像的切割
    public static Mat croppingImg(Mat originalImage, MatOfPoint largestContour) throws InterruptedException, IOException {
        // 创建一个与原始图像相同大小的全黑图像作为掩模
        Mat mask = Mat.zeros(originalImage.size(), CvType.CV_8UC1);

//        // 绘制轮廓到掩模上，轮廓区域为白色
        Imgproc.drawContours(mask, Collections.singletonList(largestContour), -1, new Scalar(255), Imgproc.FILLED);

        // 创建一个新的图像来保存裁剪结果
        Mat croppedImage = new Mat(originalImage.size(), originalImage.type());

        // 使用掩模裁剪图像
        originalImage.copyTo(croppedImage, mask);

        // 获取轮廓的边界矩形
        Rect boundingRect = Imgproc.boundingRect(largestContour);

        // 裁剪到边界矩形大小
        return resizeImg(new Mat(croppedImage, boundingRect), 800);
    }

    public static Mat processCroppedImage(Mat croppedImage) throws InterruptedException, TesseractException, IOException {
        // 检查输入图像是否为空
        if (croppedImage.empty()) {
            throw new IOException("Input image is empty!");
        }

        Mat grayImage = new Mat();

        // 检查图像是否是灰度图（单通道）
        if (croppedImage.channels() == 1) {
            grayImage = croppedImage.clone(); // 如果是灰度图，直接复制
        } else {
            Imgproc.cvtColor(croppedImage, grayImage, Imgproc.COLOR_BGR2GRAY); // 转为灰度图像
        }

        // 应用自适应阈值化（大津算法）
        Mat binary = new Mat();
        Imgproc.adaptiveThreshold(grayImage, binary, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 59, 23);

        // 显示处理后的二值图像（调试用）
        // imshow("Binary Images", binary);

        return binary;
    }


    public static Mat regenerateImg(Mat originalImage, double scaleFactor) {
        // 检查输入图像是否为空
        if (originalImage.empty()) {
            throw new IllegalArgumentException("输入图像是空的");
        }

        // 原图像的宽高
        int originalWidth = originalImage.cols();
        int originalHeight = originalImage.rows();

        // 白色背景的尺寸（设为原图的1.5倍）
        int newWidth = (int) (originalWidth * scaleFactor);
        int newHeight = (int) (originalHeight * scaleFactor);

        // 创建白色背景的图像
        Mat whiteBackground = Mat.ones(newHeight, newWidth, originalImage.type());
        whiteBackground.setTo(new Scalar(255, 255, 255)); // 设置为白色 (BGR)

        // 计算将原图像放在白色背景中的起始位置 (居中)
        int startX = (newWidth - originalWidth) / 2;
        int startY = (newHeight - originalHeight) / 2;

        // 定义 ROI (Region of Interest) 区域
        Rect roi = new Rect(startX, startY, originalWidth, originalHeight);

        // 将原图像复制到白色背景中
        Mat region = whiteBackground.submat(roi);
        originalImage.copyTo(region);
        imshow("whiteBackground", whiteBackground);
        return whiteBackground;
    }

    // 获取轮廓的边界矩形
    public static List<Rect> getAllContours(Mat inputImage) throws InterruptedException, TesseractException, IOException {

        // 1. 转为灰度图像
        Mat grayImage = new Mat();
        Imgproc.cvtColor(inputImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        Mat filteredImage = new Mat();
        Imgproc.bilateralFilter(grayImage, filteredImage, 3, 70, 80); // 双边滤波，保留边缘
        // 2. 应用阈值化（大津算法）
        Mat binary = new Mat();
        Imgproc.threshold(filteredImage, binary, 127, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Mat edges = new Mat();
        Imgproc.Canny(binary, edges, 10, 20);

        // 膨胀操作增强边缘
        Mat new_dilatedEdges = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15, 15));
        Imgproc.dilate(edges, new_dilatedEdges, kernel);

        // 再次应用 Canny 边缘检测
        Mat newEdges = new Mat();
        Imgproc.Canny(new_dilatedEdges, newEdges, 10, 20);

        // 膨胀操作增强边缘
        Mat newDilatedEdges = new Mat();
        Mat newKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(newEdges, newDilatedEdges, newKernel);
        Core.bitwise_not(newDilatedEdges, newDilatedEdges);
        // 查找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(newDilatedEdges, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // 创建一个拷贝的图像用于绘制结果
        Mat resultImage = inputImage.clone();
        List<Rect> rects = new ArrayList<>();
        // 遍历轮廓
        for (int i = 0; i < contours.size(); i++) {
            // 检查是否为封闭边界
            if (hierarchy.get(0, i)[3] < 0) {
                Rect boundingRect = Imgproc.boundingRect(contours.get(i));
                if (boundingRect.area() >= 800) {
                    rects.add(boundingRect);
                }
                // 绘制矩形边框
                Imgproc.rectangle(resultImage, boundingRect.tl(), boundingRect.br(), new Scalar(0, 255, 0), 1);
            }
        }
        imshow("New Image with Bounding Rect", resultImage);
        // 返回处理后的图像
        return rects;
    }

    // 裁剪和拼接图像
    public static Mat cuttingSplicingImg(Mat image, List<Rect> rects) throws InterruptedException, TesseractException, IOException {
        // 定义裁剪区域的结果变量
        Mat upperCroppedImage = null;
        Mat lowerCroppedImage = null;

        // 裁剪左上区域
        Rect upperBoundingRect = getBoundingRect(rects, rect -> rect.x < image.width() / 2 && rect.x > image.width() / 7.8 && rect.y < image.height() * 0.7 && (rect.x + rect.width) < image.width() * 0.7);

        if (upperBoundingRect != null) {
            upperCroppedImage = new Mat(image, upperBoundingRect);
            upperCroppedImage = processCroppedImage(upperCroppedImage);

            upperCroppedImage = resizeImg(upperCroppedImage, 500);
        }
        // 裁剪左下区域
        Rect lowerBoundingRect = getBoundingRect(rects, rect -> rect.x < image.width() / 2 && rect.x > image.width() / 3.5 && rect.y > image.height() / 3 * 2);

        if (lowerBoundingRect != null) {
            lowerCroppedImage = new Mat(image, lowerBoundingRect);
            lowerCroppedImage = processCroppedImage(lowerCroppedImage);

            lowerCroppedImage = resizeImg(lowerCroppedImage, 500);

        }
        // 如果两个裁剪区域都存在，进行拼接
        if (upperCroppedImage != null && lowerCroppedImage != null) {
            // 拼接图像（上下拼接）
            Mat concatenatedImage = new Mat();
            Core.vconcat(Arrays.asList(upperCroppedImage, lowerCroppedImage), concatenatedImage);

            // 显示拼接后的图像
            imshow("Concatenated Image", concatenatedImage);
            return concatenatedImage;
        }

        return null;
    }

    /**
     * 获取符合条件的矩形的边界框
     *
     * @param rects     矩形列表
     * @param condition 筛选条件 (Predicate)
     * @return 包含所有符合条件矩形的边界框 (Rect)，若无符合条件的矩形则返回 null
     */
    private static Rect getBoundingRect(List<Rect> rects, Predicate<Rect> condition) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Rect rect : rects) {
            if (condition.test(rect)) {
                minX = Math.min(minX, rect.x);
                minY = Math.min(minY, rect.y);
                maxX = Math.max(maxX, rect.x + rect.width);
                maxY = Math.max(maxY, rect.y + rect.height);
            }
        }

        // 如果没有找到符合条件的矩形，则返回 null
        if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE || maxX == Integer.MIN_VALUE || maxY == Integer.MIN_VALUE) {
            return null;
        }

        // 返回合并后的边界框
        return new Rect(minX, minY, maxX - minX, maxY - minY);
    }
}
