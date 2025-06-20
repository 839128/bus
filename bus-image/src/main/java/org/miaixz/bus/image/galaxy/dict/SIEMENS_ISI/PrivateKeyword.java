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
package org.miaixz.bus.image.galaxy.dict.SIEMENS_ISI;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrivateKeyword {

    public static final String PrivateCreator = "";

    public static String valueOf(int tag) {

        switch (tag & 0xFFFF00FF) {
        case PrivateTag.ISICommandField:
            return "ISICommandField";
        case PrivateTag.AttachIDApplicationCode:
            return "AttachIDApplicationCode";
        case PrivateTag.AttachIDMessageCount:
            return "AttachIDMessageCount";
        case PrivateTag.AttachIDDate:
            return "AttachIDDate";
        case PrivateTag.AttachIDTime:
            return "AttachIDTime";
        case PrivateTag.MessageType:
            return "MessageType";
        case PrivateTag.MaxWaitingDate:
            return "MaxWaitingDate";
        case PrivateTag.MaxWaitingTime:
            return "MaxWaitingTime";
        case PrivateTag.RISPatientInfoIMGEF:
            return "RISPatientInfoIMGEF";
        case PrivateTag.PatientUID:
            return "PatientUID";
        case PrivateTag.PatientID:
            return "PatientID";
        case PrivateTag.CaseID:
            return "CaseID";
        case PrivateTag.RequestID:
            return "RequestID";
        case PrivateTag.ExaminationUID:
            return "ExaminationUID";
        case PrivateTag.PatientRegistrationDate:
            return "PatientRegistrationDate";
        case PrivateTag.PatientRegistrationTime:
            return "PatientRegistrationTime";
        case PrivateTag.PatientLastName:
            return "PatientLastName";
        case PrivateTag.PatientFirstName:
            return "PatientFirstName";
        case PrivateTag.PatientHospitalStatus:
            return "PatientHospitalStatus";
        case PrivateTag.CurrentLocationTime:
            return "CurrentLocationTime";
        case PrivateTag.PatientInsuranceStatus:
            return "PatientInsuranceStatus";
        case PrivateTag.PatientBillingType:
            return "PatientBillingType";
        case PrivateTag.PatientBillingAddress:
            return "PatientBillingAddress";
        case PrivateTag.ExaminationReason:
            return "ExaminationReason";
        case PrivateTag.RequestedDate:
            return "RequestedDate";
        case PrivateTag.WorklistRequestStartTime:
            return "WorklistRequestStartTime";
        case PrivateTag.WorklistRequestEndTime:
            return "WorklistRequestEndTime";
        case PrivateTag.RequestedTime:
            return "RequestedTime";
        case PrivateTag.RequestedLocation:
            return "RequestedLocation";
        case PrivateTag.CurrentWard:
            return "CurrentWard";
        case PrivateTag.RISKey:
            return "RISKey";
        case PrivateTag.RISWorklistIMGEF:
            return "RISWorklistIMGEF";
        case PrivateTag.RISReportIMGEF:
            return "RISReportIMGEF";
        case PrivateTag.ReportID:
            return "ReportID";
        case PrivateTag.ReportStatus:
            return "ReportStatus";
        case PrivateTag.ReportCreationDate:
            return "ReportCreationDate";
        case PrivateTag.ReportApprovingPhysician:
            return "ReportApprovingPhysician";
        case PrivateTag.ReportText:
            return "ReportText";
        case PrivateTag.ReportAuthor:
            return "ReportAuthor";
        case PrivateTag.ReportingRadiologist:
            return "ReportingRadiologist";
        }
        return "";
    }

}
