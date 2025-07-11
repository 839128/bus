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
package org.miaixz.bus.image.galaxy.dict.GEMS_CT_CARDIAC_001;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.CTCardiacSequence:
            return "CTCardiacSequence";
        case PrivateTag.HeartRateAtConfirm:
            return "HeartRateAtConfirm";
        case PrivateTag.AvgHeartRatePriorToConfirm:
            return "AvgHeartRatePriorToConfirm";
        case PrivateTag.MinHeartRatePriorToConfirm:
            return "MinHeartRatePriorToConfirm";
        case PrivateTag.MaxHeartRatePriorToConfirm:
            return "MaxHeartRatePriorToConfirm";
        case PrivateTag.StdDevHeartRatePriorToConfirm:
            return "StdDevHeartRatePriorToConfirm";
        case PrivateTag.NumHeartRateSamplesPriorToConfirm:
            return "NumHeartRateSamplesPriorToConfirm";
        case PrivateTag.AutoHeartRateDetectPredict:
            return "AutoHeartRateDetectPredict";
        case PrivateTag.SystemOptimizedHeartRate:
            return "SystemOptimizedHeartRate";
        case PrivateTag.EkgMonitorType:
            return "EkgMonitorType";
        case PrivateTag.NumReconSectors:
            return "NumReconSectors";
        case PrivateTag.RpeakTimeStamps:
            return "RpeakTimeStamps";
        case PrivateTag.EkgGatingType:
            return "EkgGatingType";
        case PrivateTag.EkgWaveTimeOffFirstDataPoint:
            return "EkgWaveTimeOffFirstDataPoint";
        case PrivateTag.TemporalAlg:
            return "TemporalAlg";
        case PrivateTag.PhaseLocation:
            return "PhaseLocation";
        case PrivateTag.PreBlendedCycle1:
            return "PreBlendedCycle1";
        case PrivateTag.PreBlendedCycle2:
            return "PreBlendedCycle2";
        case PrivateTag.CompressionAlg:
            return "CompressionAlg";
        }
        return "";
    }

}
