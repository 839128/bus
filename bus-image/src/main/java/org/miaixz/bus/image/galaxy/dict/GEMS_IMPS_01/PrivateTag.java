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
package org.miaixz.bus.image.galaxy.dict.GEMS_IMPS_01;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateTag {

    public static final String PrivateCreator = "GEMS_IMPS_01";

    /** (0029,xx04) VR=SL VM=1 Lower Range Of Pixels */
    public static final int LowerRangeOfPixels = 0x00290004;

    /** (0029,xx15) VR=SL VM=1 Lower Range Of Pixels1 */
    public static final int LowerRangeOfPixels1 = 0x00290015;

    /** (0029,xx16) VR=SL VM=1 Upper Range Of Pixels1 */
    public static final int UpperRangeOfPixels1 = 0x00290016;

    /** (0029,xx17) VR=SL VM=1 Lower Range Of Pixels2 */
    public static final int LowerRangeOfPixels2 = 0x00290017;

    /** (0029,xx18) VR=SL VM=1 Upper Range Of Pixels2 */
    public static final int UpperRangeOfPixels2 = 0x00290018;

    /** (0029,xx1A) VR=SL VM=1 Length Of Total Header In Bytes */
    public static final int LengthOfTotalHeaderInBytes = 0x0029001A;

    /** (0029,xx26) VR=SS VM=1 Version Of Header Structure */
    public static final int VersionOfHeaderStructure = 0x00290026;

    /** (0029,xx34) VR=SL VM=1 Advantage Comp Overflow */
    public static final int AdvantageCompOverflow = 0x00290034;

    /** (0029,xx35) VR=SL VM=1 Advantage Comp Underflow */
    public static final int AdvantageCompUnderflow = 0x00290035;

}
