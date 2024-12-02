package org.zlt;

import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.zlt.bean.Image;
import org.zlt.bean.User;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.zlt.utils.CheckAccuracy.checkAllAccuracy;
import static org.zlt.utils.CheckAccuracy.checkSingleAccuracy;
import static org.zlt.utils.FormatResult.extractInfo;
import static org.zlt.utils.OCR.performOCR;
import static org.zlt.utils.ProcessImg.*;
import static org.zlt.utils.RotateImage.*;
import static org.zlt.utils.RotateImage.rotateImage;

// 按两次 Shift 打开“随处搜索”对话框并输入 `show whitespaces`，
// 然后按 Enter 键。现在，您可以在代码中看到空格字符。
public class Main {
    static {
        // 加载OpenCV动态库
        URL url = ClassLoader.getSystemResource("opencv/opencv_java4100.dll");
        System.load(url.getPath());
    }

    public static void main(String[] args) throws InterruptedException, TesseractException, IOException {

        List<User> userList = new ArrayList<>();
        int totalNum =14;
        for (int i =1; i <= totalNum; i++) {
            String path = "D:\\Develop\\Java\\JavaProject\\OCR_Final\\images\\test" + i + ".jpg";
            User user = getUserInfo(path);
            // waitKey();
            System.out.println("识别的用户信息：" + user);
            double accuracy = checkSingleAccuracy(user, i) * 100;
            System.out.println("第" + i + "张图片的准确率：" + accuracy + "%");
            userList.add(user);
        }
        double allImageAccuracy = checkAllAccuracy(userList, totalNum) * 100;
        System.out.println("识别的准确率：" + allImageAccuracy + "%");
    }

    public static User getUserInfo(String inputImagePath) throws InterruptedException, TesseractException, IOException {
        Mat originalImage = Imgcodecs.imread(inputImagePath);
        // 处理图片的纵横比
        originalImage=comHorizontalRatio(originalImage);
        // 再次旋转确保图像水平放置
        originalImage = horizontalImage(originalImage);

        // 调整图像大小
        Mat resizeImage = resizeImg(originalImage, 800);

        // 获取原图像的所有边缘轮廓（为后续的图像旋转做准备）
        List<MatOfPoint> ImgEdgePoints = getOriginalImgEdge(resizeImage);

        // 获取原始图像最大的轮廓（为了查找出身份证的位置，从而进行图像旋转）
        MatOfPoint largestContour = getMaximumContour(resizeImage, ImgEdgePoints);

        // 执行图像旋转
        Image rotateImage = rotateImage(resizeImage, largestContour);

        // 执行原图像裁剪（如果有需要）
        Mat croppingImg = croppingImg(rotateImage.getRotatedImage(), rotateImage.getRotatedContours());

        // 获取裁切图像的所有边缘轮廓
        List<Rect> croppingImgEdgePoints = getAllContours(croppingImg);

        // 对裁剪后的图继续进行裁剪和拼接
        Mat montageNewImage = cuttingSplicingImg(croppingImg, croppingImgEdgePoints);

        // 处理拼接后的图像得到二值化的图像清晰度影响就在此处
        Mat processRresult = montageNewImage;

        // 设置拼接后的图像大小
        processRresult = resizeImg(processRresult, 500);

        // 将图像重新进行放缩，得到一个边缘比拼接后的图像大1.5倍的图像，增加识别准确率
        processRresult = regenerateImg(processRresult, 1.5);

        // 进行ORC识别
        String result = performOCR(processRresult);

        User user = new User();
        // 提取信息
        extractInfo(result, user);
        return user;
    }

}