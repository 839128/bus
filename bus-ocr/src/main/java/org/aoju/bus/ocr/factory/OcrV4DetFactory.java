package org.aoju.bus.ocr.factory;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Translator;
import org.aoju.bus.ocr.translator.OcrDetTranslator;

import java.util.Map;

public class OcrV4DetFactory extends AbstractModelFactory<NDList> {


    public OcrV4DetFactory(Map<String, Object> arguments) {
        super("ch_PP-OCRv4_det_infer.zip", arguments);
    }

    @Override
    protected Translator<Image, NDList> getTranslator(Map<String, Object> arguments) {
        return new OcrDetTranslator(arguments);
    }

    @Override
    protected Class<NDList> getClassOfT() {
        return NDList.class;
    }

    @Override
    protected String getEngine() {
        return "OnnxRuntime";
    }
}
