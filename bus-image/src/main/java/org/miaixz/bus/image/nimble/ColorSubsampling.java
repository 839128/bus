/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ColorSubsampling {
    YBR_XXX_422 {
        @Override
        public int frameLength(int w, int h) {
            return w * h * 2;
        }

        @Override
        public int indexOfY(int x, int y, int w) {
            return (w * y + x) * 2 - (x % 2);
        }

        @Override
        public int indexOfBR(int x, int y, int w) {
            return (w * y * 2) + ((x >> 1) << 2) + 2;
        }
    },
    YBR_XXX_420 {
        @Override
        public int frameLength(int w, int h) {
            return w * h / 2 * 3;
        }

        @Override
        public int indexOfY(int x, int y, int w) {
            int withoutBR = y / 2;
            int withBR = y - withoutBR;
            return w * (withBR * 2 + withoutBR) + ((y % 2 == 0) ? (x * 2 - (x % 2)) : x);
        }

        @Override
        public int indexOfBR(int x, int y, int w) {
            return w * (y / 2) * 3 + ((x >> 1) << 2) + 2;
        }
    };

    public abstract int frameLength(int w, int h);

    public abstract int indexOfY(int x, int y, int w);

    public abstract int indexOfBR(int x, int y, int w);

}
