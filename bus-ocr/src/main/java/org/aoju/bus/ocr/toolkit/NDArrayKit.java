package org.aoju.bus.ocr.toolkit;

import ai.djl.ndarray.NDArray;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * NDArray工具类，提供多种数据结构转换功能。
 */
public class NDArrayKit {
    /**
     * 将Mat对象转换为MatOfPoint对象。
     * @param mat 要转换的Mat对象。
     * @return 转换后的MatOfPoint对象。
     */
    public static MatOfPoint matToMatOfPoint(Mat mat) {
        // 获取mat的行数。
        int rows = mat.rows();
        // 创建一个新的MatOfPoint对象。
        MatOfPoint matOfPoint = new MatOfPoint();

        // 创建一个Point对象的列表。
        List<Point> list = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            // 从Mat对象中获取点的坐标。
            Point point = new Point((float) mat.get(i, 0)[0], (float) mat.get(i, 1)[0]);
            // 将点添加到列表中。
            list.add(point);
        }
        // 将列表转换为MatOfPoint对象。
        matOfPoint.fromList(list);

        return matOfPoint;
    }

    /**
     * 将float类型的NDArray对象转换为float类型的二维数组。
     * @param ndArray 要转换的NDArray对象。
     * @return 转换后的float二维数组。
     */
    public static float[][] floatNDArrayToArray(NDArray ndArray) {
        // 获取NDArray的行数和列数。
        int rows = (int) (ndArray.getShape().get(0));
        int cols = (int) (ndArray.getShape().get(1));
        // 创建一个新的float二维数组。
        float[][] arr = new float[rows][cols];

        // 将NDArray转换为一维float数组。
        float[] arrs = ndArray.toFloatArray();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 将一维数组转换为二维数组。
                arr[i][j] = arrs[i * cols + j];
            }
        }
        return arr;
    }

    /**
     * 将Mat对象转换为double类型的二维数组。
     * @param mat 要转换的Mat对象。
     * @return 转换后的double二维数组。
     */
    public static double[][] matToDoubleArray(Mat mat) {
        // 获取mat的行数和列数。
        int rows = mat.rows();
        int cols = mat.cols();

        // 创建一个新的double二维数组。
        double[][] doubles = new double[rows][cols];

        // 遍历mat的每个元素，将其添加到二维数组中。
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                doubles[i][j] = mat.get(i, j)[0];
            }
        }

        return doubles;
    }

    /**
     * 将Mat对象转换为float类型的二维数组。
     * @param mat 要转换的Mat对象。
     * @return 转换后的float二维数组。
     */
    public static float[][] matToFloatArray(Mat mat) {
        // 获取mat的行数和列数。
        int rows = mat.rows();
        int cols = mat.cols();

        // 创建一个新的float二维数组。
        float[][] floats = new float[rows][cols];

        // 遍历mat的每个元素，将其转换为float并添加到二维数组中。
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                floats[i][j] = (float) mat.get(i, j)[0];
            }
        }

        return floats;
    }

    /**
     * 将Mat对象转换为byte类型的二维数组。
     * @param mat 要转换的Mat对象。
     * @return 转换后的byte二维数组。
     */
    public static byte[][] matToUint8Array(Mat mat) {
        // 获取mat的行数和列数。
        int rows = mat.rows();
        int cols = mat.cols();

        // 创建一个新的byte二维数组。
        byte[][] bytes = new byte[rows][cols];

        // 遍历mat的每个元素，将其转换为byte并添加到二维数组中。
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                bytes[i][j] = (byte) mat.get(i, j)[0];
            }
        }

        return bytes;
    }

    /**
     * 将float类型的NDArray对象转换为Mat对象。
     * @param ndArray 要转换的NDArray对象。
     * @param cvType 指定Mat对象的数据类型。
     * @return 转换后的Mat对象。
     */
    public static Mat floatNDArrayToMat(NDArray ndArray, int cvType) {
        // 获取NDArray的行数和列数。
        int rows = (int) (ndArray.getShape().get(0));
        int cols = (int) (ndArray.getShape().get(1));
        // 创建一个新的Mat对象。
        Mat mat = new Mat(rows, cols, cvType);

        // 将NDArray转换为一维float数组。
        float[] arrs = ndArray.toFloatArray();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 将一维数组的元素放入Mat对象中。
                mat.put(i, j, arrs[i * cols + j]);
            }
        }
        return mat;
    }

    /**
     * 将float类型的NDArray对象转换为Mat对象，数据类型为CV_32F。
     * @param ndArray 要转换的NDArray对象。
     * @return 转换后的Mat对象。
     */
    public static Mat floatNDArrayToMat(NDArray ndArray) {
        // 获取NDArray的行数和列数。
        int rows = (int) (ndArray.getShape().get(0));
        int cols = (int) (ndArray.getShape().get(1));
        // 创建一个新的Mat对象，数据类型为CV_32F。
        Mat mat = new Mat(rows, cols, CvType.CV_32F);

        // 将NDArray转换为一维float数组。
        float[] arrs = ndArray.toFloatArray();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // 将一维数组的元素放入Mat对象中。
                mat.put(i, j, arrs[i * cols + j]);
            }
        }

        return mat;
    }

    /**
     * 将uint8类型的NDArray对象转换为Mat对象，数据类型为CV_8U。
     * @param ndArray 要转换的NDArray对象。
     * @return 转换后的Mat对象。
     */
    public static Mat uint8NDArrayToMat(NDArray ndArray) {
        int rows = (int) (ndArray.getShape().get(0));
        int cols = (int) (ndArray.getShape().get(1));
        Mat mat = new Mat(rows, cols, CvType.CV_8U);

        byte[] arrs = ndArray.toByteArray();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mat.put(i, j, arrs[i * cols + j]);
            }
        }
        return mat;
    }

    /**
     * float[][] Array To Mat
     * @param arr
     * @return
     */
    public static Mat floatArrayToMat(float[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        Mat mat = new Mat(rows, cols, CvType.CV_32F);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mat.put(i, j, arr[i][j]);
            }
        }

        return mat;
    }

    /**
     * byte[][] Array To Mat
     * @param arr
     * @return
     */
    public static Mat uint8ArrayToMat(byte[][] arr) {
        int rows = arr.length;
        int cols = arr[0].length;
        Mat mat = new Mat(rows, cols, CvType.CV_8U);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mat.put(i, j, arr[i][j]);
            }
        }

        return mat;
    }

    /**
     * List To Mat
     * @param points
     * @return
     */
    public static Mat toMat(List<ai.djl.modality.cv.output.Point> points) {
        Mat mat = new Mat(points.size(), 2, CvType.CV_32F);
        for (int i = 0; i < points.size(); i++) {
            ai.djl.modality.cv.output.Point point = points.get(i);
            mat.put(i, 0, (float) point.getX());
            mat.put(i, 1, (float) point.getY());
        }

        return mat;
    }
}
