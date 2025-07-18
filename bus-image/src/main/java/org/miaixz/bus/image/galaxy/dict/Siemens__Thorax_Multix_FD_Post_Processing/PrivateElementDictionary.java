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
package org.miaixz.bus.image.galaxy.dict.Siemens__Thorax_Multix_FD_Post_Processing;

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

        case PrivateTag.AutoWindowExpansion:
            return VR.DS;
        case PrivateTag._0021_xx02_:
        case PrivateTag._0021_xx03_:
        case PrivateTag._0021_xx06_:
        case PrivateTag._0021_xx07_:
        case PrivateTag._0021_xx0C_:
            return VR.FL;
        case PrivateTag.SystemType:
        case PrivateTag.DetectorType:
            return VR.LO;
        case PrivateTag.AutoWindowCenter:
        case PrivateTag.AutoWindowWidth:
            return VR.SL;
        case PrivateTag._0021_xx01_:
        case PrivateTag._0021_xx05_:
        case PrivateTag.FilterID:
        case PrivateTag._0021_xx0D_:
        case PrivateTag._0021_xx11_:
        case PrivateTag._0021_xx12_:
        case PrivateTag._0021_xx13_:
        case PrivateTag.AutoWindowShift:
            return VR.SS;
        case PrivateTag._0021_xx00_:
        case PrivateTag._0021_xx04_:
        case PrivateTag.AutoWindowFlag:
        case PrivateTag.DoseControlValue:
        case PrivateTag._0021_xx0F_:
        case PrivateTag._0021_xx10_:
        case PrivateTag.AnatomicCorrectView:
        case PrivateTag.AnatomicSortNumber:
        case PrivateTag.AcquisitionSortNumber:
            return VR.US;
        }
        return VR.UN;
    }

}
