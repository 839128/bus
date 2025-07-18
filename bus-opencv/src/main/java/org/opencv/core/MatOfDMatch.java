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

public class MatOfDMatch extends Mat {
    // 32FC4
    private static final int _depth = CvType.CV_32F;
    private static final int _channels = 4;

    public MatOfDMatch() {
        super();
    }

    protected MatOfDMatch(long addr) {
        super(addr);
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat: " + toString());
        // FIXME: do we need release() here?
    }

    public MatOfDMatch(Mat m) {
        super(m, Range.all());
        if (!empty() && checkVector(_channels, _depth) < 0)
            throw new IllegalArgumentException("Incompatible Mat: " + toString());
        // FIXME: do we need release() here?
    }

    public MatOfDMatch(DMatch... ap) {
        super();
        fromArray(ap);
    }

    public static MatOfDMatch fromNativeAddr(long addr) {
        return new MatOfDMatch(addr);
    }

    public void alloc(int elemNumber) {
        if (elemNumber > 0)
            super.create(elemNumber, 1, CvType.makeType(_depth, _channels));
    }

    public void fromArray(DMatch... a) {
        if (a == null || a.length == 0)
            return;
        int num = a.length;
        alloc(num);
        float buff[] = new float[num * _channels];
        for (int i = 0; i < num; i++) {
            DMatch m = a[i];
            buff[_channels * i + 0] = m.queryIdx;
            buff[_channels * i + 1] = m.trainIdx;
            buff[_channels * i + 2] = m.imgIdx;
            buff[_channels * i + 3] = m.distance;
        }
        put(0, 0, buff); // TODO: check ret val!
    }

    public DMatch[] toArray() {
        int num = (int) total();
        DMatch[] a = new DMatch[num];
        if (num == 0)
            return a;
        float buff[] = new float[num * _channels];
        get(0, 0, buff); // TODO: check ret val!
        for (int i = 0; i < num; i++)
            a[i] = new DMatch((int) buff[_channels * i + 0], (int) buff[_channels * i + 1],
                    (int) buff[_channels * i + 2], buff[_channels * i + 3]);
        return a;
    }

    public void fromList(List<DMatch> ldm) {
        DMatch adm[] = ldm.toArray(new DMatch[0]);
        fromArray(adm);
    }

    public List<DMatch> toList() {
        DMatch[] adm = toArray();
        return Arrays.asList(adm);
    }

}
