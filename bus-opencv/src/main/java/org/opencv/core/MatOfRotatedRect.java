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

import java.util.Arrays;
import java.util.List;

public class MatOfRotatedRect extends Mat {
    // 32FC5
    private static final int _depth = CvType.CV_32F;
    private static final int _channels = 5;

    public MatOfRotatedRect() {
        super();
    }

    protected MatOfRotatedRect(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfRotatedRect(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfRotatedRect(RotatedRect... a) {
        super();
        fromArray(a);
    }

    public static MatOfRotatedRect fromNativeAddr(long addr) {
        return new MatOfRotatedRect(addr);
    }

    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(RotatedRect... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length;
        alloc(num);
        float buff[] = new float[num * _channels];
        for (int i = 0; i < num; i++) {
            RotatedRect r = a[i];
            buff[_channels * i + 0] = (float) r.center.x;
            buff[_channels * i + 1] = (float) r.center.y;
            buff[_channels * i + 2] = (float) r.size.width;
            buff[_channels * i + 3] = (float) r.size.height;
            buff[_channels * i + 4] = (float) r.angle;
        }
        put(0, 0, buff); // TODO: check ret val!
    }

    public RotatedRect[] toArray() {
        int num = (int) total();
        RotatedRect[] a = new RotatedRect[num];
        if (num == 0)
            return a;
        float buff[] = new float[_channels];
        for (int i = 0; i < num; i++) {
            get(i, 0, buff); // TODO: check ret val!
            a[i] = new RotatedRect(new Point(buff[0], buff[1]), new Size(buff[2], buff[3]), buff[4]);
        }
        return a;
    }

    public void fromList(List<RotatedRect> lr) {
        RotatedRect ap[] = lr.toArray(new RotatedRect[0]);
        fromArray(ap);
    }

    public List<RotatedRect> toList() {
        RotatedRect[] ar = toArray();
        return Arrays.asList(ar);
    }

}
