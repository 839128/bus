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
package org.opencv.core;

// C++: class Algorithm
/**
 * This is a base class for all more or less complex algorithms in OpenCV
 *
 * especially for classes of algorithms, for which there can be multiple implementations. The examples are stereo
 * correspondence (for which there are algorithms like block matching, semi-global block matching, graph-cut etc.),
 * background subtraction (which can be done using mixture-of-gaussians models, codebook-based algorithm etc.), optical
 * flow (block matching, Lucas-Kanade, Horn-Schunck etc.).
 *
 * Here is example of SimpleBlobDetector use in your application via Algorithm interface: SNIPPET:
 * snippets/core_various.cpp Algorithm
 */
public class Algorithm {

    protected final long nativeObj;

    protected Algorithm(long addr) {
        nativeObj = addr;
        long nativeObjCopy = nativeObj;
        org.opencv.core.Mat.cleaner.register(this, () -> delete(nativeObjCopy));
    }

    // internal usage only
    public static Algorithm __fromPtr__(long addr) {
        return new Algorithm(addr);
    }

    // C++: void cv::Algorithm::clear()
    private static native void clear_0(long nativeObj);

    //
    // C++: void cv::Algorithm::clear()
    //

    // C++: bool cv::Algorithm::empty()
    private static native boolean empty_0(long nativeObj);

    //
    // C++: void cv::Algorithm::write(FileStorage fs)
    //

    // Unknown type 'FileStorage' (I), skipping the function

    //
    // C++: void cv::Algorithm::write(FileStorage fs, String name)
    //

    // Unknown type 'FileStorage' (I), skipping the function

    //
    // C++: void cv::Algorithm::read(FileNode fn)
    //

    // Unknown type 'FileNode' (I), skipping the function

    //
    // C++: bool cv::Algorithm::empty()
    //

    // C++: void cv::Algorithm::save(String filename)
    private static native void save_0(long nativeObj, String filename);

    //
    // C++: void cv::Algorithm::save(String filename)
    //

    // C++: String cv::Algorithm::getDefaultName()
    private static native String getDefaultName_0(long nativeObj);

    //
    // C++: String cv::Algorithm::getDefaultName()
    //

    // native support for deleting native object
    private static native void delete(long nativeObj);

    public long getNativeObjAddr() {
        return nativeObj;
    }

    /**
     * Clears the algorithm state
     */
    public void clear() {
        clear_0(nativeObj);
    }

    /**
     * Returns true if the Algorithm is empty (e.g. in the very beginning or after unsuccessful read
     *
     * @return automatically generated
     */
    public boolean empty() {
        return empty_0(nativeObj);
    }

    /**
     * Saves the algorithm to a file. In order to make this method work, the derived class must implement
     * Algorithm::write(FileStorage&amp; fs).
     *
     * @param filename automatically generated
     */
    public void save(String filename) {
        save_0(nativeObj, filename);
    }

    /**
     * Returns the algorithm string identifier. This string is used as top level xml/yml node tag when the object is
     * saved to a file or string.
     *
     * @return automatically generated
     */
    public String getDefaultName() {
        return getDefaultName_0(nativeObj);
    }

}
