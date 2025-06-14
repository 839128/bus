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
package org.miaixz.bus.image.builtin;

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.image.galaxy.data.Code;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class AcquisitionModality {

    public static final Code Autorefraction = new Code("AR", "DCM", null, "Autorefraction");
    public static final Code BoneMineralDensitometry = new Code("BMD", "DCM", null, "Bone Mineral Densitometry");
    public static final Code UltrasoundBoneDensitometry = new Code("BDUS", "DCM", null, "Ultrasound Bone Densitometry");
    public static final Code CardiacElectrophysiology = new Code("EPS", "DCM", null, "Cardiac Electrophysiology");
    public static final Code ComputedRadiography = new Code("CR", "DCM", null, "Computed Radiography");
    public static final Code ComputedTomography = new Code("CT", "DCM", null, "Computed Tomography");
    public static final Code DigitalRadiography = new Code("DX", "DCM", null, "Digital Radiography");
    public static final Code Electrocardiography = new Code("ECG", "DCM", null, "Electrocardiography");
    public static final Code Endoscopy = new Code("ES", "DCM", null, "Endoscopy");
    public static final Code ExternalCameraPhotography = new Code("XC", "DCM", null, "External-camera Photography");
    public static final Code GeneralMicroscopy = new Code("GM", "DCM", null, "General Microscopy");
    public static final Code HemodynamicWaveform = new Code("HD", "DCM", null, "Hemodynamic Waveform");
    public static final Code IntraOralRadiography = new Code("IO", "DCM", null, "Intra-oral Radiography");
    public static final Code IntravascularOpticalCoherence = new Code("IVOCT", "DCM", null,
            "Intravascular Optical Coherence Tomography");
    public static final Code IntravascularUltrasound = new Code("IVUS", "DCM", null, "Intravascular Ultrasound");
    public static final Code Keratometry = new Code("KER", "DCM", null, "Keratometry");
    public static final Code Lensometry = new Code("LEN", "DCM", null, "Lensometry");
    public static final Code MagneticResonance = new Code("MR", "DCM", null, "Magnetic Resonance");
    public static final Code Mammography = new Code("MG", "DCM", null, "Mammography");
    public static final Code NuclearMedicine = new Code("NM", "DCM", null, "Nuclear Medicine");
    public static final Code OphthalmicAxialMeasurements = new Code("OAM", "DCM", null,
            "Ophthalmic Axial Measurements");
    public static final Code OpticalCoherenceTomography = new Code("OCT", "DCM", null, "Optical Coherence Tomography");
    public static final Code OphthalmicMapping = new Code("OPM", "DCM", null, "Ophthalmic Mapping");
    public static final Code OphthalmicPhotography = new Code("OP", "DCM", null, "Ophthalmic Photography");
    public static final Code OphthalmicRefraction = new Code("OPR", "DCM", null, "Ophthalmic Refraction");
    public static final Code OphthalmicTomography = new Code("OPT", "DCM", null, "Ophthalmic Tomography");
    public static final Code OphthalmicVisualField = new Code("OPV", "DCM", null, "Ophthalmic Visual Field");
    public static final Code OpticalSurfaceScanner = new Code("OSS", "DCM", null, "Optical Surface Scanner");
    public static final Code PanoramicXRay = new Code("PX", "DCM", null, "Panoramic X-Ray");
    public static final Code PositronEmissionTomography = new Code("PT", "DCM", null, "Positron emission tomography");
    public static final Code Radiofluoroscopy = new Code("RF", "DCM", null, "Radiofluoroscopy");
    public static final Code RadiographicImaging = new Code("RG", "DCM", null, "Radiographic imaging");
    public static final Code SlideMicroscopy = new Code("SM", "DCM", null, "Slide Microscopy");
    public static final Code SubjectiveRefraction = new Code("SRF", "DCM", null, "Subjective Refraction");
    public static final Code Ultrasound = new Code("US", "DCM", null, "Ultrasound");
    public static final Code VisualAcuity = new Code("VA", "DCM", null, "Visual Acuity");
    public static final Code XRayAngiography = new Code("XA", "DCM", null, "X-Ray Angiography");

    private static final Map<String, Code> MODALITIES = new HashMap<>(50);

    static {
        Code[] codes = { Autorefraction, BoneMineralDensitometry, UltrasoundBoneDensitometry, CardiacElectrophysiology,
                ComputedRadiography, ComputedTomography, DigitalRadiography, Electrocardiography, Endoscopy,
                ExternalCameraPhotography, GeneralMicroscopy, HemodynamicWaveform, IntraOralRadiography,
                IntravascularOpticalCoherence, IntravascularUltrasound, Keratometry, Lensometry, MagneticResonance,
                Mammography, NuclearMedicine, OphthalmicAxialMeasurements, OpticalCoherenceTomography,
                OphthalmicMapping, OphthalmicPhotography, OphthalmicRefraction, OphthalmicTomography,
                OphthalmicVisualField, OpticalSurfaceScanner, PanoramicXRay, PositronEmissionTomography,
                Radiofluoroscopy, RadiographicImaging, SlideMicroscopy, SubjectiveRefraction, Ultrasound, VisualAcuity,
                XRayAngiography };
        for (Code code : codes) {
            MODALITIES.put(code.getCodeValue(), code);
        }

    }

    public static Code codeOf(String modality) {
        return MODALITIES.get(modality);
    }

    public static Code addCode(Code code) {
        return MODALITIES.put(code.getCodeValue(), code);
    }

    public static Code removeCode(String modality) {
        return MODALITIES.remove(modality);
    }

}
