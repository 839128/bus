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

public class MatOfRect2d extends Mat {
    // 64FC4
    private static final int _depth = CvType.CV_64F;
    private static final int _channels = 4;

    public MatOfRect2d() {
        super();
    }

    protected MatOfRect2d(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfRect2d(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat");
        // FIXME: do we need release() here?
    }

    public MatOfRect2d(Rect2d... a) {
        super();
        fromArray(a);
    }

    public static MatOfRect2d fromNativeAddr(long addr) {
        return new MatOfRect2d(addr);
    }

    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(Rect2d... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length;
        alloc(num);
        double buff[] = new double[num * _channels];
        for (int i = 0; i < num; i++) {
            Rect2d r = a[i];
            buff[_channels * i + 0] = (double) r.x;
            buff[_channels * i + 1] = (double) r.y;
            buff[_channels * i + 2] = (double) r.width;
            buff[_channels * i + 3] = (double) r.height;
        }
        put(0, 0, buff); // TODO: check ret val!
    }

    public Rect2d[] toArray() {
        int num = (int) total();
        Rect2d[] a = new Rect2d[num];
        if (num == 0)
            return a;
        double buff[] = new double[num * _channels];
        get(0, 0, buff); // TODO: check ret val!
        for (int i = 0; i < num; i++)
            a[i] = new Rect2d(buff[i * _channels], buff[i * _channels + 1], buff[i * _channels + 2],
                    buff[i * _channels + 3]);
        return a;
    }

    public void fromList(List<Rect2d> lr) {
        Rect2d ap[] = lr.toArray(new Rect2d[0]);
        fromArray(ap);
    }

    public List<Rect2d> toList() {
        Rect2d[] ar = toArray();
        return Arrays.asList(ar);
    }

}
