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
package org.miaixz.bus.image.galaxy.dict.agfa_ag_hpstate;

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

        case PrivateTag._0019_xxA1_:
        case PrivateTag._0019_xxA2_:
        case PrivateTag._0019_xxA3_:
        case PrivateTag._0019_xxA4_:
        case PrivateTag._0071_xx20_:
        case PrivateTag._0073_xx80_:
            return VR.FL;
        case PrivateTag._0071_xx21_:
        case PrivateTag._0071_xx22_:
        case PrivateTag._0071_xx23_:
        case PrivateTag._0071_xx24_:
        case PrivateTag._0071_xx2B_:
        case PrivateTag._0071_xx2C_:
        case PrivateTag._0071_xx2D_:
        case PrivateTag._0087_xx05_:
        case PrivateTag._0087_xx06_:
        case PrivateTag._0087_xx07_:
        case PrivateTag._0087_xx08_:
            return VR.FD;
        case PrivateTag._0075_xx10_:
        case PrivateTag._0087_xx01_:
        case PrivateTag._0087_xx02_:
            return VR.LO;
        case PrivateTag._0011_xx11_:
        case PrivateTag._0073_xx23_:
            return VR.SH;
        case PrivateTag._0087_xx03_:
        case PrivateTag._0087_xx04_:
            return VR.SL;
        case PrivateTag._0019_xxA0_:
        case PrivateTag._0071_xx18_:
        case PrivateTag._0071_xx19_:
        case PrivateTag._0071_xx1A_:
        case PrivateTag._0071_xx1C_:
        case PrivateTag._0071_xx1E_:
        case PrivateTag._0073_xx24_:
        case PrivateTag._0073_xx28_:
            return VR.SQ;
        }
        return VR.UN;
    }

}
