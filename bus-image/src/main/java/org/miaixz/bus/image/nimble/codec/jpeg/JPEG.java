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
package org.miaixz.bus.image.nimble.codec.jpeg;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JPEG {

    /**
     * For temporary use in arithmetic coding
     */
    public static final int TEM = 0x01;
    /**
     * Start of codestream
     */
    public static final int FF_SOC = 0xFF4F;
    public static final int SOC = 0x4F;
    /**
     * Image and tile size
     */
    public static final int SIZ = 0x51;
    /**
     * Coding style default
     */
    public static final int COD = 0x52;
    /**
     * Tile-part lengths
     */
    public static final int TLM = 0x55;
    /**
     * Start of tile-part
     */
    public static final int SOT = 0x90;
    /**
     * Start of data
     */
    public static final int SOD = 0x93;
    /**
     * Baseline DCT
     */
    public static final int SOF0 = 0xC0;
    /**
     * Extended Sequential DCT
     */
    public static final int SOF1 = 0xC1;
    /**
     * Progressive DCT
     */
    public static final int SOF2 = 0xC2;
    /**
     * Lossless Sequential
     */
    public static final int SOF3 = 0xC3;
    /**
     * Define Huffman Tables
     */
    public static final int DHT = 0xC4;
    /**
     * Differential Sequential DCT
     */
    public static final int SOF5 = 0xC5;
    /**
     * Differential Progressive DCT
     */
    public static final int SOF6 = 0xC6;
    /**
     * Differential Lossless
     */
    public static final int SOF7 = 0xC7;
    /**
     * Reserved for JPEG extensions
     */
    public static final int JPG = 0xC8;
    /**
     * Extended Sequential DCT, Arithmetic coding
     */
    public static final int SOF9 = 0xC9;
    /**
     * Progressive DCT, Arithmetic coding
     */
    public static final int SOF10 = 0xCA;
    /**
     * Lossless Sequential, Arithmetic coding
     */
    public static final int SOF11 = 0xCB;
    /**
     * Define Arithmetic conditioning tables
     */
    public static final int DAC = 0xCC;
    /**
     * Differential Sequential DCT, Arithmetic coding
     */
    public static final int SOF13 = 0xCD;
    /**
     * Differential Progressive DCT, Arithmetic coding
     */
    public static final int SOF14 = 0xCE;
    /**
     * Differential Lossless, Arithmetic coding
     */
    public static final int SOF15 = 0xCF;
    // Restart Markers
    public static final int RST0 = 0xD0;
    public static final int RST1 = 0xD1;
    public static final int RST2 = 0xD2;
    public static final int RST3 = 0xD3;
    public static final int RST4 = 0xD4;
    public static final int RST5 = 0xD5;
    public static final int RST6 = 0xD6;
    public static final int RST7 = 0xD7;
    /**
     * Number of restart markers
     */
    public static final int RESTART_RANGE = 8;
    /**
     * Start of Image
     */
    public static final int FF_SOI = 0xFFD8;
    public static final int SOI = 0xD8;
    /**
     * End of Image
     */
    public static final int EOI = 0xD9;
    /**
     * Start of Scan
     */
    public static final int SOS = 0xDA;
    /**
     * Define Quantization Tables
     */
    public static final int DQT = 0xDB;
    /**
     * Define Number of lines
     */
    public static final int DNL = 0xDC;
    /**
     * Define Restart Interval
     */
    public static final int DRI = 0xDD;
    /**
     * Define Hierarchical progression
     */
    public static final int DHP = 0xDE;
    /**
     * Expand reference image(s)
     */
    public static final int EXP = 0xDF;
    /**
     * APP0 used by JFIF
     */
    public static final int APP0 = 0xE0;

    // Application markers
    public static final int APP1 = 0xE1;
    public static final int APP2 = 0xE2;
    public static final int APP3 = 0xE3;
    public static final int APP4 = 0xE4;
    public static final int APP5 = 0xE5;
    public static final int APP6 = 0xE6;
    public static final int APP7 = 0xE7;
    public static final int APP8 = 0xE8;
    public static final int APP9 = 0xE9;
    public static final int APP10 = 0xEA;
    public static final int APP11 = 0xEB;
    public static final int APP12 = 0xEC;
    public static final int APP13 = 0xED;
    /**
     * APP14 used by Adobe
     */
    public static final int APP14 = 0xEE;
    public static final int APP15 = 0xEF;
    /**
     * JPEG-LS coding
     */
    public static final int SOF55 = 0xF7;
    /**
     * JPEG-LS parameters
     */
    public static final int LSE = 0xF8;
    /**
     * Comment marker
     */
    public static final int COM = 0xFE;
    // JPEG 2000 markers
    private static final int JPEG2000_STANDALONE = 0x30;

    public static boolean isStandalone(int marker) {
        switch (marker) {
        case TEM:
        case RST0:
        case RST1:
        case RST2:
        case RST3:
        case RST4:
        case RST5:
        case RST6:
        case RST7:
        case SOI:
        case EOI:
            return true;
        }
        return (marker & 0xF0) == JPEG2000_STANDALONE;
    }

    public static boolean isSOF(int marker) {
        switch (marker) {
        case SOF0:
        case SOF1:
        case SOF2:
        case SOF3:
        case SOF5:
        case SOF6:
        case SOF7:
        case SOF9:
        case SOF10:
        case SOF11:
        case SOF13:
        case SOF14:
        case SOF15:
        case SOF55:
            return true;
        }
        return false;
    }

    public static boolean isAPP(int marker) {
        return (marker & 0xF0) == APP0;
    }

}
