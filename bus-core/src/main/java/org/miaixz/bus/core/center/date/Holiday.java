/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;

/**
 * 法定假日（自2001-12-29起）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Holiday extends Loops {

    /**
     * 法定假日名称
     */
    public static final String[] NAMES = { "元旦节", "春节", "清明节", "劳动节", "端午节", "中秋节", "国庆节", "国庆中秋", "抗战胜利日" };

    /**
     * 默认节假日数据 完整格式:日期+下标+调休+节日 日期：YYYYMMDD 节日：{@link Holiday#NAMES} 下标位置，例如，元旦-0，抗战胜利日-8 调休：0工作日,1调休 节日：YYYYMMDD
     */
    private static final String DATA = "200112290020020101" + "200112300020020101" + "200201010120020101"
            + "200201020120020101" + "200201030120020101" + "200202091020020212" + "200202101020020212"
            + "200202121120020212" + "200202131120020212" + "200202141120020212" + "200202151120020212"
            + "200202161120020212" + "200202171120020212" + "200202181120020212" + "200204273020020501"
            + "200204283020020501" + "200205013120020501" + "200205023120020501" + "200205033120020501"
            + "200205043120020501" + "200205053120020501" + "200205063120020501" + "200205073120021001"
            + "200209286020021001" + "200209296020021001" + "200210016120021001" + "200210026120021001"
            + "200210036120021001" + "200210046120021001" + "200210056120021001" + "200210066120021001"
            + "200210076120021001" + "200301010120030101" + "200302011120030201" + "200302021120030201"
            + "200302031120030201" + "200302041120030201" + "200302051120030201" + "200302061120030201"
            + "200302071120030201" + "200302081020030201" + "200302091020030201" + "200304263020030501"
            + "200304273020030501" + "200305013120030501" + "200305023120030501" + "200305033120030501"
            + "200305043120030501" + "200305053120030501" + "200305063120030501" + "200305073120031001"
            + "200309276020031001" + "200309286020031001" + "200310016120031001" + "200310026120031001"
            + "200310036120031001" + "200310046120031001" + "200310056120031001" + "200310066120031001"
            + "200310076120031001" + "200401010120040101" + "200401171020040122" + "200401181020040122"
            + "200401221120040122" + "200401231120040122" + "200401241120040122" + "200401251120040122"
            + "200401261120040122" + "200401271120040122" + "200401281120040122" + "200405013120040501"
            + "200405023120040501" + "200405033120040501" + "200405043120040501" + "200405053120040501"
            + "200405063120040501" + "200405073120041001" + "200405083020040501" + "200405093020040501"
            + "200410016120041001" + "200410026120041001" + "200410036120041001" + "200410046120041001"
            + "200410056120041001" + "200410066120041001" + "200410076120041001" + "200410096020041001"
            + "200410106020041001" + "200501010120050101" + "200501020120050101" + "200501030120050101"
            + "200502051020050209" + "200502061020050209" + "200502091120050209" + "200502101120050209"
            + "200502111120050209" + "200502121120050209" + "200502131120050209" + "200502141120050209"
            + "200502151120050209" + "200504303020050501" + "200505013120050501" + "200505023120050501"
            + "200505033120050501" + "200505043120050501" + "200505053120050501" + "200505063120050501"
            + "200505073120051001" + "200505083020050501" + "200510016120051001" + "200510026120051001"
            + "200510036120051001" + "200510046120051001" + "200510056120051001" + "200510066120051001"
            + "200510076120051001" + "200510086020051001" + "200510096020051001" + "200512310020060101"
            + "200601010120060101" + "200601020120060101" + "200601030120060101" + "200601281020060129"
            + "200601291120060129" + "200601301120060129" + "200601311120060129" + "200602011120060129"
            + "200602021120060129" + "200602031120060129" + "200602041120060129" + "200602051020060129"
            + "200604293020060501" + "200604303020060501" + "200605013120060501" + "200605023120060501"
            + "200605033120060501" + "200605043120060501" + "200605053120060501" + "200605063120060501"
            + "200605073120061001" + "200609306020061001" + "200610016120061001" + "200610026120061001"
            + "200610036120061001" + "200610046120061001" + "200610056120061001" + "200610066120061001"
            + "200610076120061001" + "200610086020061001" + "200612300020070101" + "200612310020070101"
            + "200701010120070101" + "200701020120070101" + "200701030120070101" + "200702171020070218"
            + "200702181120070218" + "200702191120070218" + "200702201120070218" + "200702211120070218"
            + "200702221120070218" + "200702231120070218" + "200702241120070218" + "200702251020070218"
            + "200704283020070501" + "200704293020070501" + "200705013120070501" + "200705023120070501"
            + "200705033120070501" + "200705043120070501" + "200705053120070501" + "200705063120070501"
            + "200705073120070501" + "200709296020071001" + "200709306020071001" + "200710016120071001"
            + "200710026120071001" + "200710036120071001" + "200710046120071001" + "200710056120071001"
            + "200710066120071001" + "200710076120071001" + "200712290020080101" + "200712300120080101"
            + "200712310120080101" + "200801010120080101" + "200802021020080206" + "200802031020080206"
            + "200802061120080206" + "200802071120080206" + "200802081120080206" + "200802091120080206"
            + "200802101120080206" + "200802111120080206" + "200802121120080206" + "200804042120080404"
            + "200804052120080404" + "200804062120080404" + "200805013120080501" + "200805023120080501"
            + "200805033120080501" + "200805043020080501" + "200806074120080608" + "200806084120080608"
            + "200806094120080608" + "200809135120080914" + "200809145120080914" + "200809155120080914"
            + "200809276020081001" + "200809286020081001" + "200809296120081001" + "200809306120081001"
            + "200810016120081001" + "200810026120081001" + "200810036120081001" + "200810046120081001"
            + "200810056120081001" + "200901010120090101" + "200901020120090101" + "200901030120090101"
            + "200901040020090101" + "200901241020090125" + "200901251120090125" + "200901261120090125"
            + "200901271120090125" + "200901281120090125" + "200901291120090125" + "200901301120090125"
            + "200901311120090125" + "200902011020090125" + "200904042120090404" + "200904052120090404"
            + "200904062120090404" + "200905013120090501" + "200905023120090501" + "200905033120090501"
            + "200905284120090528" + "200905294120090528" + "200905304120090528" + "200905314020090528"
            + "200909276020091001" + "200910016120091001" + "200910026120091001" + "200910036120091001"
            + "200910046120091001" + "200910055120091003" + "200910065120091003" + "200910075120091003"
            + "200910085120091003" + "200910105020091003" + "201001010120100101" + "201001020120100101"
            + "201001030120100101" + "201002131120100213" + "201002141120100213" + "201002151120100213"
            + "201002161120100213" + "201002171120100213" + "201002181120100213" + "201002191120100213"
            + "201002201020100213" + "201002211020100213" + "201004032120100405" + "201004042120100405"
            + "201004052120100405" + "201005013120100501" + "201005023120100501" + "201005033120100501"
            + "201006124020100616" + "201006134020100616" + "201006144120100616" + "201006154120100616"
            + "201006164120100616" + "201009195020100922" + "201009225120100922" + "201009235120100922"
            + "201009245120100922" + "201009255020100922" + "201009266020101001" + "201010016120101001"
            + "201010026120101001" + "201010036120101001" + "201010046120101001" + "201010056120101001"
            + "201010066120101001" + "201010076120101001" + "201010096020101001" + "201101010120110101"
            + "201101020120110101" + "201101030120110101" + "201101301020110203" + "201102021120110203"
            + "201102031120110203" + "201102041120110203" + "201102051120110203" + "201102061120110203"
            + "201102071120110203" + "201102081120110203" + "201102121020110203" + "201104022020110405"
            + "201104032120110405" + "201104042120110405" + "201104052120110405" + "201104303120110501"
            + "201105013120110501" + "201105023120110501" + "201106044120110606" + "201106054120110606"
            + "201106064120110606" + "201109105120110912" + "201109115120110912" + "201109125120110912"
            + "201110016120111001" + "201110026120111001" + "201110036120111001" + "201110046120111001"
            + "201110056120111001" + "201110066120111001" + "201110076120111001" + "201110086020111001"
            + "201110096020111001" + "201112310020120101" + "201201010120120101" + "201201020120120101"
            + "201201030120120101" + "201201211020120123" + "201201221120120123" + "201201231120120123"
            + "201201241120120123" + "201201251120120123" + "201201261120120123" + "201201271120120123"
            + "201201281120120123" + "201201291020120123" + "201203312020120404" + "201204012020120404"
            + "201204022120120404" + "201204032120120404" + "201204042120120404" + "201204283020120501"
            + "201204293120120501" + "201204303120120501" + "201205013120120501" + "201205023020120501"
            + "201206224120120623" + "201206234120120623" + "201206244120120623" + "201209295020120930"
            + "201209305120120930" + "201210016120121001" + "201210026120121001" + "201210036120121001"
            + "201210046120121001" + "201210056120121001" + "201210066120121001" + "201210076120121001"
            + "201210086020121001" + "201301010120130101" + "201301020120130101" + "201301030120130101"
            + "201301050020130101" + "201301060020130101" + "201302091120130210" + "201302101120130210"
            + "201302111120130210" + "201302121120130210" + "201302131120130210" + "201302141120130210"
            + "201302151120130210" + "201302161020130210" + "201302171020130210" + "201304042120130404"
            + "201304052120130404" + "201304062120130404" + "201304273020130501" + "201304283020130501"
            + "201304293120130501" + "201304303120130501" + "201305013120130501" + "201306084020130612"
            + "201306094020130612" + "201306104120130612" + "201306114120130612" + "201306124120130612"
            + "201309195120130919" + "201309205120130919" + "201309215120130919" + "201309225020130919"
            + "201309296020131001" + "201310016120131001" + "201310026120131001" + "201310036120131001"
            + "201310046120131001" + "201310056120131001" + "201310066120131001" + "201310076120131001"
            + "201401010120140101" + "201401261020140131" + "201401311120140131" + "201402011120140131"
            + "201402021120140131" + "201402031120140131" + "201402041120140131" + "201402051120140131"
            + "201402061120140131" + "201402081020140131" + "201404052120140405" + "201404062120140405"
            + "201404072120140405" + "201405013120140501" + "201405023120140501" + "201405033120140501"
            + "201405043020140501" + "201405314120140602" + "201406014120140602" + "201406024120140602"
            + "201409065120140908" + "201409075120140908" + "201409085120140908" + "201409286020141001"
            + "201410016120141001" + "201410026120141001" + "201410036120141001" + "201410046120141004"
            + "201410056120141001" + "201410066120141001" + "201410076120141001" + "201410116020141001"
            + "201501010120150101" + "201501020120150101" + "201501030120150101" + "201501040020150101"
            + "201502151020150219" + "201502181120150219" + "201502191120150219" + "201502201120150219"
            + "201502211120150219" + "201502221120150219" + "201502231120150219" + "201502241120150219"
            + "201502281020150219" + "201504042120150405" + "201504052120150405" + "201504062120150405"
            + "201505013120150501" + "201505023120150501" + "201505033120150501" + "201506204120150620"
            + "201506214120150620" + "201506224120150620" + "201509038120150903" + "201509048120150903"
            + "201509058120150903" + "201509068020150903" + "201509265120150927" + "201509275120150927"
            + "201510016120151001" + "201510026120151001" + "201510036120151001" + "201510046120151004"
            + "201510056120151001" + "201510066120151001" + "201510076120151001" + "201510106020151001"
            + "201601010120160101" + "201601020120160101" + "201601030120160101" + "201602061020160208"
            + "201602071120160208" + "201602081120160208" + "201602091120160208" + "201602101120160208"
            + "201602111120160208" + "201602121120160208" + "201602131120160208" + "201602141020160208"
            + "201604022120160404" + "201604032120160404" + "201604042120160404" + "201604303120160501"
            + "201605013120160501" + "201605023120160501" + "201606094120160609" + "201606104120160609"
            + "201606114120160609" + "201606124020160609" + "201609155120160915" + "201609165120160915"
            + "201609175120160915" + "201609185020160915" + "201610016120161001" + "201610026120161001"
            + "201610036120161001" + "201610046120161001" + "201610056120161001" + "201610066120161001"
            + "201610076120161001" + "201610086020161001" + "201610096020161001" + "201612310120170101"
            + "201701010120170101" + "201701020120170101" + "201701221020170128" + "201701271120170128"
            + "201701281120170128" + "201701291120170128" + "201701301120170128" + "201701311120170128"
            + "201702011120170128" + "201702021120170128" + "201702041020170128" + "201704012020170404"
            + "201704022120170404" + "201704032120170404" + "201704042120170404" + "201704293120170501"
            + "201704303120170501" + "201705013120170501" + "201705274020170530" + "201705284120170530"
            + "201705294120170530" + "201705304120170530" + "201709306020171001" + "201710016120171001"
            + "201710026120171001" + "201710036120171001" + "201710045120171004" + "201710056120171001"
            + "201710066120171001" + "201710076120171001" + "201710086120171001" + "201712300120180101"
            + "201712310120180101" + "201801010120180101" + "201802111020180216" + "201802151120180216"
            + "201802161120180216" + "201802171120180216" + "201802181120180216" + "201802191120180216"
            + "201802201120180216" + "201802211120180216" + "201802241020180216" + "201804052120180405"
            + "201804062120180405" + "201804072120180405" + "201804082020180405" + "201804283020180501"
            + "201804293120180501" + "201804303120180501" + "201805013120180501" + "201806164120180618"
            + "201806174120180618" + "201806184120180618" + "201809225120180924" + "201809235120180924"
            + "201809245120180924" + "201809296020181001" + "201809306020181001" + "201810016120181001"
            + "201810026120181001" + "201810036120181001" + "201810046120181001" + "201810056120181001"
            + "201810066120181001" + "201810076120181001" + "201812290020190101" + "201812300120190101"
            + "201812310120190101" + "201901010120190101" + "201902021020190205" + "201902031020190205"
            + "201902041120190205" + "201902051120190205" + "201902061120190205" + "201902071120190205"
            + "201902081120190205" + "201902091120190205" + "201902101120190205" + "201904052120190405"
            + "201904062120190405" + "201904072120190405" + "201904283020190501" + "201905013120190501"
            + "201905023120190501" + "201905033120190501" + "201905043120190501" + "201905053020190501"
            + "201906074120190607" + "201906084120190607" + "201906094120190607" + "201909135120190913"
            + "201909145120190913" + "201909155120190913" + "201909296020191001" + "201910016120191001"
            + "201910026120191001" + "201910036120191001" + "201910046120191001" + "201910056120191001"
            + "201910066120191001" + "201910076120191001" + "201910126020191001" + "202001010120200101"
            + "202001191020200125" + "202001241120200125" + "202001251120200125" + "202001261120200125"
            + "202001271120200125" + "202001281120200125" + "202001291120200125" + "202001301120200125"
            + "202001311120200125" + "202002011120200125" + "202002021120200125" + "202004042120200404"
            + "202004052120200404" + "202004062120200404" + "202004263020200501" + "202005013120200501"
            + "202005023120200501" + "202005033120200501" + "202005043120200501" + "202005053120200501"
            + "202005093020200501" + "202006254120200625" + "202006264120200625" + "202006274120200625"
            + "202006284020200625" + "202009277020201001" + "202010017120201001" + "202010026120201001"
            + "202010036120201001" + "202010046120201001" + "202010056120201001" + "202010066120201001"
            + "202010076120201001" + "202010086120201001" + "202010106020201001" + "202101010120210101"
            + "202101020120210101" + "202101030120210101" + "202102071020210212" + "202102111120210212"
            + "202102121120210212" + "202102131120210212" + "202102141120210212" + "202102151120210212"
            + "202102161120210212" + "202102171120210212" + "202102201020210212" + "202104032120210404"
            + "202104042120210404" + "202104052120210404" + "202104253020210501" + "202105013120210501"
            + "202105023120210501" + "202105033120210501" + "202105043120210501" + "202105053120210501"
            + "202105083020210501" + "202106124120210614" + "202106134120210614" + "202106144120210614"
            + "202109185020210921" + "202109195120210921" + "202109205120210921" + "202109215120210921"
            + "202109266020211001" + "202110016120211001" + "202110026120211001" + "202110036120211001"
            + "202110046120211001" + "202110056120211001" + "202110066120211001" + "202110076120211001"
            + "202110096020211001" + "202201010120220101" + "202201020120220101" + "202201030120220101"
            + "202201291020220201" + "202201301020220201" + "202201311120220201" + "202202011120220201"
            + "202202021120220201" + "202202031120220201" + "202202041120220201" + "202202051120220201"
            + "202202061120220201" + "202204022020220405" + "202204032120220405" + "202204042120220405"
            + "202204052120220405" + "202204243020220501" + "202204303120220501" + "202205013120220501"
            + "202205023120220501" + "202205033120220501" + "202205043120220501" + "202205073020220501"
            + "202206034120220603" + "202206044120220603" + "202206054120220603" + "202209105120220910"
            + "202209115120220910" + "202209125120220910" + "202210016120221001" + "202210026120221001"
            + "202210036120221001" + "202210046120221001" + "202210056120221001" + "202210066120221001"
            + "202210076120221001" + "202210086020221001" + "202210096020221001" + "202210096020221001"
            + "202212310120230101" + "202301010120230101" + "202301020120230101" + "202301211120230122"
            + "202301221120230122" + "202301231120230122" + "202301241120230122" + "202301251120230122"
            + "202301261120230122" + "202301271120230122" + "202301281020230122" + "202301291020230122"
            + "202304052120230405" + "202304233020230501" + "202304293120230501" + "202304303120230501"
            + "202305013120230501" + "202305023120230501" + "202305033120230501" + "202305063020230501"
            + "202306224120230622" + "202306234120230622" + "202306244120230622" + "202306254020230622"
            + "202309295120230929" + "202309306120231001" + "202310016120231001" + "202310026120231001"
            + "202310036120231001" + "202310046120231001" + "202310056120231001" + "202310066120231001"
            + "202310076020231001" + "202310086020231001" + "202312300120240101" + "202312310120240101"
            + "202401010120240101" + "202402041020240210" + "202402101120240210" + "202402111120240210"
            + "202402121120240210" + "202402131120240210" + "202402141120240210" + "202402151120240210"
            + "202402161120240210" + "202402171120240210" + "202402181020240210" + "202404042120240404"
            + "202404052120240404" + "202404062120240404" + "202404072020240404" + "202404283020240501"
            + "202405013120240501" + "202405023120240501" + "202405033120240501" + "202405043120240501"
            + "202405053120240501" + "202405113020240501" + "202406084120240610" + "202406094120240610"
            + "202406104120240610" + "202409145020240917" + "202409155120240917" + "202409165120240917"
            + "202409175120240917" + "202409296020241001" + "202410016120241001" + "202410026120241001"
            + "202410036120241001" + "202410046120241001" + "202410056120241001" + "202410066120241001"
            + "202410076120241001" + "202410126020241001";
    /**
     * 公历日
     */
    protected SolarDay day;

    /**
     * 名称
     */
    protected String name;

    /**
     * 是否上班
     */
    protected boolean work;

    public Holiday(int year, int month, int day, String data) {
        this.day = SolarDay.fromYmd(year, month, day);
        work = '0' == data.charAt(8);
        name = NAMES[data.charAt(9) - '0'];
    }

    public static Holiday fromYmd(int year, int month, int day) {
        Matcher matcher = Pattern.compile(String.format("%04d%02d%02d[0-1][0-8][\\+|-]\\d{2}", year, month, day))
                .matcher(DATA);
        return !matcher.find() ? null : new Holiday(year, month, day, matcher.group());
    }

    public Holiday next(int n) {
        int year = day.getYear();
        int month = day.getMonth();
        if (n == 0) {
            return fromYmd(year, month, day.getDay());
        }
        List<String> data = new ArrayList<>();
        String reg = "%04d\\d{4}[0-1][0-8][\\+|-]\\d{2}";
        String today = String.format("%04d%02d%02d", year, month, day.getDay());
        Matcher matcher = Pattern.compile(String.format(reg, year)).matcher(DATA);
        while (matcher.find()) {
            data.add(matcher.group());
        }
        int index = -1;
        int size = data.size();
        for (int i = 0; i < size; i++) {
            if (data.get(i).startsWith(today)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return null;
        }
        index += n;
        int y = year;
        if (n > 0) {
            while (index >= size) {
                index -= size;
                y += 1;
                data.clear();
                matcher = Pattern.compile(String.format(reg, y)).matcher(DATA);
                while (matcher.find()) {
                    data.add(matcher.group());
                }
                size = data.size();
                if (size < 1) {
                    return null;
                }
            }
        } else {
            while (index < 0) {
                y -= 1;
                data.clear();
                matcher = Pattern.compile(String.format(reg, y)).matcher(DATA);
                while (matcher.find()) {
                    data.add(matcher.group());
                }
                size = data.size();
                if (size < 1) {
                    return null;
                }
                index += size;
            }
        }
        String d = data.get(index);
        return new Holiday(Integer.parseInt(d.substring(0, 4)), Integer.parseInt(d.substring(4, 6)),
                Integer.parseInt(d.substring(6, 8)), d);
    }

    @Override
    public String toString() {
        return String.format("%s %s(%s)", day, name, work ? "班" : "休");
    }

    public SolarDay getDay() {
        return day;
    }

    public String getName() {
        return name;
    }

    /**
     * 是否上班
     *
     * @return true/false
     */
    public boolean isWork() {
        return work;
    }

}