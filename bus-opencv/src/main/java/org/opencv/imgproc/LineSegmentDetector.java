/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org opencv.org and other contributors.         ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.opencv.imgproc;

import org.opencv.core.Algorithm;
import org.opencv.core.Mat;
import org.opencv.core.Size;

// C++: class LineSegmentDetector
/**
 * Line segment detector class
 *
 * following the algorithm described at CITE: Rafael12 .
 *
 * <b>Note:</b> Implementation has been removed from OpenCV version 3.4.6 to 3.4.15 and version 4.1.0 to 4.5.3 due
 * original code license conflict. restored again after [Computation of a
 * NFA](https://github.com/rafael-grompone-von-gioi/binomial_nfa) code published under the MIT license.
 */
public class LineSegmentDetector extends Algorithm {

    protected LineSegmentDetector(long addr) {
        super(addr);
    }

    // internal usage only
    public static LineSegmentDetector __fromPtr__(long addr) {
        return new LineSegmentDetector(addr);
    }

    //
    // C++: void cv::LineSegmentDetector::detect(Mat image, Mat& lines, Mat& width = Mat(), Mat& prec = Mat(), Mat& nfa
    // = Mat())
    //

    // C++: void cv::LineSegmentDetector::detect(Mat image, Mat& lines, Mat& width = Mat(), Mat& prec = Mat(), Mat& nfa
    // = Mat())
    private static native void detect_0(long nativeObj, long image_nativeObj, long lines_nativeObj,
            long width_nativeObj, long prec_nativeObj, long nfa_nativeObj);

    private static native void detect_1(long nativeObj, long image_nativeObj, long lines_nativeObj,
            long width_nativeObj, long prec_nativeObj);

    private static native void detect_2(long nativeObj, long image_nativeObj, long lines_nativeObj,
            long width_nativeObj);

    private static native void detect_3(long nativeObj, long image_nativeObj, long lines_nativeObj);

    //
    // C++: void cv::LineSegmentDetector::drawSegments(Mat& image, Mat lines)
    //

    // C++: void cv::LineSegmentDetector::drawSegments(Mat& image, Mat lines)
    private static native void drawSegments_0(long nativeObj, long image_nativeObj, long lines_nativeObj);

    //
    // C++: int cv::LineSegmentDetector::compareSegments(Size size, Mat lines1, Mat lines2, Mat& image = Mat())
    //

    // C++: int cv::LineSegmentDetector::compareSegments(Size size, Mat lines1, Mat lines2, Mat& image = Mat())
    private static native int compareSegments_0(long nativeObj, double size_width, double size_height,
            long lines1_nativeObj, long lines2_nativeObj, long image_nativeObj);

    private static native int compareSegments_1(long nativeObj, double size_width, double size_height,
            long lines1_nativeObj, long lines2_nativeObj);

    // native support for deleting native object
    private static native void delete(long nativeObj);

    /**
     * Finds lines in the input image.
     *
     * This is the output of the default parameters of the algorithm on the above shown image.
     *
     * ![image](pics/building_lsd.png)
     *
     * @param image A grayscale (CV_8UC1) input image. If only a roi needs to be selected, use:
     *              {@code lsd_ptr-&gt;detect(image(roi), lines, ...); lines += Scalar(roi.x, roi.y, roi.x, roi.y);}
     * @param lines A vector of Vec4f elements specifying the beginning and ending point of a line. Where Vec4f is (x1,
     *              y1, x2, y2), point 1 is the start, point 2 - end. Returned lines are strictly oriented depending on
     *              the gradient.
     * @param width Vector of widths of the regions, where the lines are found. E.g. Width of line.
     * @param prec  Vector of precisions with which the lines are found.
     * @param nfa   Vector containing number of false alarms in the line region, with precision of 10%. The bigger the
     *              value, logarithmically better the detection.
     *              <ul>
     *              <li>-1 corresponds to 10 mean false alarms</li>
     *              <li>0 corresponds to 1 mean false alarm</li>
     *              <li>1 corresponds to 0.1 mean false alarms This vector will be calculated only when the objects type
     *              is #LSD_REFINE_ADV.</li>
     *              </ul>
     */
    public void detect(Mat image, Mat lines, Mat width, Mat prec, Mat nfa) {
        detect_0(nativeObj, image.nativeObj, lines.nativeObj, width.nativeObj, prec.nativeObj, nfa.nativeObj);
    }

    /**
     * Finds lines in the input image.
     *
     * This is the output of the default parameters of the algorithm on the above shown image.
     *
     * ![image](pics/building_lsd.png)
     *
     * @param image A grayscale (CV_8UC1) input image. If only a roi needs to be selected, use:
     *              {@code lsd_ptr-&gt;detect(image(roi), lines, ...); lines += Scalar(roi.x, roi.y, roi.x, roi.y);}
     * @param lines A vector of Vec4f elements specifying the beginning and ending point of a line. Where Vec4f is (x1,
     *              y1, x2, y2), point 1 is the start, point 2 - end. Returned lines are strictly oriented depending on
     *              the gradient.
     * @param width Vector of widths of the regions, where the lines are found. E.g. Width of line.
     * @param prec  Vector of precisions with which the lines are found. bigger the value, logarithmically better the
     *              detection.
     *              <ul>
     *              <li>-1 corresponds to 10 mean false alarms</li>
     *              <li>0 corresponds to 1 mean false alarm</li>
     *              <li>1 corresponds to 0.1 mean false alarms This vector will be calculated only when the objects type
     *              is #LSD_REFINE_ADV.</li>
     *              </ul>
     */
    public void detect(Mat image, Mat lines, Mat width, Mat prec) {
        detect_1(nativeObj, image.nativeObj, lines.nativeObj, width.nativeObj, prec.nativeObj);
    }

    /**
     * Finds lines in the input image.
     *
     * This is the output of the default parameters of the algorithm on the above shown image.
     *
     * ![image](pics/building_lsd.png)
     *
     * @param image A grayscale (CV_8UC1) input image. If only a roi needs to be selected, use:
     *              {@code lsd_ptr-&gt;detect(image(roi), lines, ...); lines += Scalar(roi.x, roi.y, roi.x, roi.y);}
     * @param lines A vector of Vec4f elements specifying the beginning and ending point of a line. Where Vec4f is (x1,
     *              y1, x2, y2), point 1 is the start, point 2 - end. Returned lines are strictly oriented depending on
     *              the gradient.
     * @param width Vector of widths of the regions, where the lines are found. E.g. Width of line. bigger the value,
     *              logarithmically better the detection.
     *              <ul>
     *              <li>-1 corresponds to 10 mean false alarms</li>
     *              <li>0 corresponds to 1 mean false alarm</li>
     *              <li>1 corresponds to 0.1 mean false alarms This vector will be calculated only when the objects type
     *              is #LSD_REFINE_ADV.</li>
     *              </ul>
     */
    public void detect(Mat image, Mat lines, Mat width) {
        detect_2(nativeObj, image.nativeObj, lines.nativeObj, width.nativeObj);
    }

    /**
     * Finds lines in the input image.
     *
     * This is the output of the default parameters of the algorithm on the above shown image.
     *
     * ![image](pics/building_lsd.png)
     *
     * @param image A grayscale (CV_8UC1) input image. If only a roi needs to be selected, use:
     *              {@code lsd_ptr-&gt;detect(image(roi), lines, ...); lines += Scalar(roi.x, roi.y, roi.x, roi.y);}
     * @param lines A vector of Vec4f elements specifying the beginning and ending point of a line. Where Vec4f is (x1,
     *              y1, x2, y2), point 1 is the start, point 2 - end. Returned lines are strictly oriented depending on
     *              the gradient. bigger the value, logarithmically better the detection.
     *              <ul>
     *              <li>-1 corresponds to 10 mean false alarms</li>
     *              <li>0 corresponds to 1 mean false alarm</li>
     *              <li>1 corresponds to 0.1 mean false alarms This vector will be calculated only when the objects type
     *              is #LSD_REFINE_ADV.</li>
     *              </ul>
     */
    public void detect(Mat image, Mat lines) {
        detect_3(nativeObj, image.nativeObj, lines.nativeObj);
    }

    /**
     * Draws the line segments on a given image.
     *
     * @param image The image, where the lines will be drawn. Should be bigger or equal to the image, where the lines
     *              were found.
     * @param lines A vector of the lines that needed to be drawn.
     */
    public void drawSegments(Mat image, Mat lines) {
        drawSegments_0(nativeObj, image.nativeObj, lines.nativeObj);
    }

    /**
     * Draws two groups of lines in blue and red, counting the non overlapping (mismatching) pixels.
     *
     * @param size   The size of the image, where lines1 and lines2 were found.
     * @param lines1 The first group of lines that needs to be drawn. It is visualized in blue color.
     * @param lines2 The second group of lines. They visualized in red color.
     * @param image  Optional image, where the lines will be drawn. The image should be color(3-channel) in order for
     *               lines1 and lines2 to be drawn in the above mentioned colors.
     * @return automatically generated
     */
    public int compareSegments(Size size, Mat lines1, Mat lines2, Mat image) {
        return compareSegments_0(nativeObj, size.width, size.height, lines1.nativeObj, lines2.nativeObj,
                image.nativeObj);
    }

    /**
     * Draws two groups of lines in blue and red, counting the non overlapping (mismatching) pixels.
     *
     * @param size   The size of the image, where lines1 and lines2 were found.
     * @param lines1 The first group of lines that needs to be drawn. It is visualized in blue color.
     * @param lines2 The second group of lines. They visualized in red color. in order for lines1 and lines2 to be drawn
     *               in the above mentioned colors.
     * @return automatically generated
     */
    public int compareSegments(Size size, Mat lines1, Mat lines2) {
        return compareSegments_1(nativeObj, size.width, size.height, lines1.nativeObj, lines2.nativeObj);
    }

}
