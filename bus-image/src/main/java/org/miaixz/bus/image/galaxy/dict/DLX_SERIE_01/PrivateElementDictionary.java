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
package org.miaixz.bus.image.galaxy.dict.DLX_SERIE_01;

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

        case PrivateTag.AngleLabelLArm:
        case PrivateTag.AngleLabelPArm:
        case PrivateTag.AngleLabelCArm:
        case PrivateTag.AutoInject:
        case PrivateTag.CameraRotationEnabled:
        case PrivateTag.ReverseSweep:
        case PrivateTag.Dose:
            return VR.CS;
        case PrivateTag.AngleValueLArm:
        case PrivateTag.AngleValuePArm:
        case PrivateTag.AngleValueCArm:
        case PrivateTag.InjectorDelay:
        case PrivateTag.Focus:
        case PrivateTag.ExposureDuration:
        case PrivateTag.TablePositionZ:
        case PrivateTag.TablePositionX:
        case PrivateTag.TablePositionY:
        case PrivateTag.Lambda:
        case PrivateTag.RegressionSlope:
        case PrivateTag.RegressionIntercept:
        case PrivateTag.ImageChainFWHMPsfMmMin:
        case PrivateTag.ImageChainFWHMPsfMmMax:
            return VR.DS;
        case PrivateTag.RecordView:
        case PrivateTag.AcquisitionMode:
        case PrivateTag.UserSpatialFilterStrength:
        case PrivateTag.UserZoomFactor:
        case PrivateTag.XZoomCenter:
        case PrivateTag.YZoomCenter:
        case PrivateTag.SideMark:
        case PrivateTag.PercentageLandscape:
            return VR.IS;
        case PrivateTag.IpAddress:
            return VR.LO;
        case PrivateTag.PatientSize:
            return VR.SH;
        case PrivateTag.ProcedureName:
        case PrivateTag.ExamName:
            return VR.ST;
        }
        return VR.UN;
    }

}
