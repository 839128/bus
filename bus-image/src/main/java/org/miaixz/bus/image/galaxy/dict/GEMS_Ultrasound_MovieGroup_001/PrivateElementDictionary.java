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
package org.miaixz.bus.image.galaxy.dict.GEMS_Ultrasound_MovieGroup_001;

import org.miaixz.bus.image.galaxy.data.ElementDictionary;
import org.miaixz.bus.image.galaxy.data.VR;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateElementDictionary extends ElementDictionary {

    public static final String PrivateCreator = "";

    public PrivateElementDictionary() {
        super("", PrivateTag.class);
    }

    @Override
    public String keywordOf(int tag) {
        return PrivateKeyword.valueOf(tag);
    }

    @Override
    public VR vrOf(int tag) {

        switch (tag & 0xFFFF00FF) {

        case PrivateTag._7FE1_xx51_:
            return VR.FL;
        case PrivateTag._7FE1_xx3C_:
        case PrivateTag._7FE1_xx48_:
        case PrivateTag._7FE1_xx52_:
        case PrivateTag._7FE1_xx77_:
        case PrivateTag._7FE1_xx87_:
        case PrivateTag._7FE1_xx88_:
            return VR.FD;
        case PrivateTag._7FE1_xx02_:
        case PrivateTag._7FE1_xx12_:
        case PrivateTag._7FE1_xx30_:
        case PrivateTag._7FE1_xx72_:
        case PrivateTag._7FE1_xx74_:
        case PrivateTag._7FE1_xx84_:
            return VR.LO;
        case PrivateTag._7FE1_xx57_:
            return VR.LT;
        case PrivateTag._7FE1_xx43_:
        case PrivateTag._7FE1_xx55_:
        case PrivateTag._7FE1_xx60_:
            return VR.OB;
        case PrivateTag._7FE1_xx61_:
        case PrivateTag._7FE1_xx69_:
            return VR.OW;
        case PrivateTag._7FE1_xx24_:
            return VR.SH;
        case PrivateTag._7FE1_xx54_:
        case PrivateTag._7FE1_xx79_:
        case PrivateTag._7FE1_xx86_:
            return VR.SL;
        case PrivateTag._7FE1_xx01_:
        case PrivateTag._7FE1_xx08_:
        case PrivateTag._7FE1_xx10_:
        case PrivateTag._7FE1_xx18_:
        case PrivateTag._7FE1_xx20_:
        case PrivateTag._7FE1_xx26_:
        case PrivateTag._7FE1_xx36_:
        case PrivateTag._7FE1_xx3A_:
        case PrivateTag._7FE1_xx62_:
        case PrivateTag._7FE1_xx70_:
        case PrivateTag._7FE1_xx73_:
        case PrivateTag._7FE1_xx75_:
        case PrivateTag._7FE1_xx83_:
        case PrivateTag._7FE1_xx85_:
            return VR.SQ;
        case PrivateTag._7FE1_xx03_:
        case PrivateTag._7FE1_xx32_:
        case PrivateTag._7FE1_xx37_:
        case PrivateTag._7FE1_xx49_:
        case PrivateTag._7FE1_xx53_:
        case PrivateTag._7FE1_xx71_:
            return VR.UL;
        }
        return VR.UN;
    }

}
