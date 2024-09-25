package org.aoju.bus.ocr.toolkit;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.Point;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.opencv.OpenCVImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import org.aoju.bus.core.codec.Base64;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.ocr.factory.OcrV4DetFactory;
import org.aoju.bus.ocr.factory.OcrV4RecFactory;
import org.aoju.bus.ocr.entity.OcrResult;
import org.aoju.bus.ocr.entity.RotatedBox;
import org.aoju.bus.ocr.entity.RotatedBoxCompX;
import org.aoju.bus.ocr.entity.WordBlock;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class OcrV4Kit {
    public static OcrResult runOcr(InputStream inputStream)
            throws TranslateException, IOException, ModelNotFoundException, MalformedModelException {
        Map<String, Object> param = new HashMap<>();
        OcrV4DetFactory ocrV4Det = new OcrV4DetFactory(param);
        Criteria<Image, NDList> criteria = ocrV4Det.criteria();
        OcrV4RecFactory ocrV4Rec = new OcrV4RecFactory(param);
        Criteria<Image, WordBlock> criteria1 = ocrV4Rec.criteria();
        try (
                ZooModel<Image, NDList> detModel = ModelZoo.loadModel(criteria);
                ZooModel<Image, WordBlock> recModel = ModelZoo.loadModel(criteria1);
                NDManager manager = NDManager.newBaseManager()
        ) {
            ImageFactory factory = OpenCVImageFactory.getInstance();
            if(null == inputStream) {
                return new OcrResult(null,"输入流为空");
            }
            Image image = factory.fromInputStream(inputStream);
            List<RotatedBox> list = predict(manager, image, detModel, recModel);
            
            // put low Y value at the head of the queue.
            List<RotatedBox> initList = new ArrayList<>(list);
            Collections.sort(initList);
            List<List<RotatedBoxCompX>> lines = new ArrayList<>();
            List<RotatedBoxCompX> line = new ArrayList<>();
            RotatedBoxCompX firstBox = new RotatedBoxCompX(initList.get(0));
            line.add(firstBox);
            lines.add(line);
            for (int i = 1; i < initList.size(); i++) {
                RotatedBoxCompX tmpBox = new RotatedBoxCompX(initList.get(i));
                float y1 = firstBox.getBox().toFloatArray()[1];
                float y2 = tmpBox.getBox().toFloatArray()[1];
                float dis = Math.abs(y2 - y1);
                if (dis < 20) { // 认为是同 1 行  - Considered to be in the same line
                    line.add(tmpBox);
                } else { // 换行 - Line break
                    firstBox = tmpBox;
                    Collections.sort(line);
                    line = new ArrayList<>();
                    line.add(firstBox);
                    lines.add(line);
                }
            }

            StringBuilder fullText = new StringBuilder();
            for (List<RotatedBoxCompX> rotatedBoxCompXES : lines) {
                for (RotatedBoxCompX rotatedBoxCompX : rotatedBoxCompXES) {
                    String text = rotatedBoxCompX.getText();
                    if (text.trim().isEmpty())
                        continue;
                    fullText.append(text).append("    ");
                }
                fullText.append('\n');
            }
//            Mat mat = (Mat) image.getWrappedImage();
//            BufferedImage bufferedImage = OpenCVKit.mat2Image(mat);
//            for (RotatedBox result : list) {
//                ImageKit.drawImageRectWithText(bufferedImage, result.getBox(), result.getText());
//            }
//            String base64 = toBase64(bufferedImage);
            return new OcrResult(null, fullText.toString());
        }
    }

    private static NDList detPredict(ZooModel<Image, NDList> detModel, Image image) throws TranslateException {
        try (Predictor<Image, NDList> detector = detModel.newPredictor()) {
            long timeInferStart = System.currentTimeMillis();
            NDList boxes = detector.predict(image);
            long timeInferEnd = System.currentTimeMillis();
            Logger.info("检测时间：{}", timeInferEnd - timeInferStart);
            return boxes;
        }
    }

    /**
     * 图像推理
     *
     * @param image    图片
     * @param detModel 图像检测模型
     * @param recModel 图像识别
     * @return 文本块
     * @throws TranslateException 转换异常
     */
    private static List<RotatedBox> predict(NDManager manager, Image image, ZooModel<Image, NDList> detModel, ZooModel<Image, WordBlock> recModel)
            throws TranslateException {
        NDList boxes = detPredict(detModel, image);
        boxes.attach(manager);
        List<RotatedBox> result = new ArrayList<>();
        Mat mat = (Mat) image.getWrappedImage();
        try (Predictor<Image, WordBlock> rec = recModel.newPredictor()) {
            long timeInferStart = System.currentTimeMillis();
            boxes.parallelStream().map(box -> {
                float[] pointsArr = box.toFloatArray();
                float[] lt = java.util.Arrays.copyOfRange(pointsArr, 0, 2);
                float[] rt = java.util.Arrays.copyOfRange(pointsArr, 2, 4);
                float[] rb = java.util.Arrays.copyOfRange(pointsArr, 4, 6);
                float[] lb = java.util.Arrays.copyOfRange(pointsArr, 6, 8);
                int img_crop_width = (int) Math.max(distance(lt, rt), distance(rb, lb));
                int img_crop_height = (int) Math.max(distance(lt, lb), distance(rt, rb));
                List<Point> srcPoints = new ArrayList<>();
                srcPoints.add(new Point(lt[0], lt[1]));
                srcPoints.add(new Point(rt[0], rt[1]));
                srcPoints.add(new Point(rb[0], rb[1]));
                srcPoints.add(new Point(lb[0], lb[1]));
                List<Point> dstPoints = new ArrayList<>();
                dstPoints.add(new Point(0, 0));
                dstPoints.add(new Point(img_crop_width, 0));
                dstPoints.add(new Point(img_crop_width, img_crop_height));
                dstPoints.add(new Point(0, img_crop_height));

                Mat srcPoint2f = NDArrayKit.toMat(srcPoints);
                Mat dstPoint2f = NDArrayKit.toMat(dstPoints);
                //耗时
                Mat cvMat = OpenCVKit.perspectiveTransform(mat, srcPoint2f, dstPoint2f);
                Image subImg = OpenCVImageFactory.getInstance().fromImage(cvMat);
                subImg = subImg.getSubImage(0, 0, img_crop_width, img_crop_height);
                if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
                    subImg = rotateImg(manager, subImg);
                }
                //耗时
                WordBlock wordBlock;
                try {
                    wordBlock = rec.predict(subImg);
                } catch (TranslateException e) {
                    throw new RuntimeException(e);
                }
                wordBlock.setBoxPoint(dstPoints);
                wordBlock.setBox(box);
                cvMat.release();
                srcPoint2f.release();
                dstPoint2f.release();
                return wordBlock;
            }).forEach(wordBlock -> {
                RotatedBox rotatedBox = new RotatedBox(wordBlock);
                result.add(rotatedBox);
            });
            long timeInferEnd = System.currentTimeMillis();
            Logger.info("识别时间：{}", timeInferEnd - timeInferStart);
            return result;
        }
    }

    //欧式距离计算
    private static float distance(float[] point1, float[] point2) {
        float disX = point1[0] - point2[0];
        float disY = point1[1] - point2[1];
        return (float) Math.sqrt(disX * disX + disY * disY);
    }

    //图片旋转
    private static Image rotateImg(NDManager manager, Image image) {
        NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
        return ImageFactory.getInstance().fromNDArray(rotated);
    }

    public static Mat imageToMat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        MatOfByte matOfByte = new MatOfByte(byteArray);
        return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED);
    }

    private static String toBase64(BufferedImage img) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(img, "png", outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64.encode(bytes);
    }
}
