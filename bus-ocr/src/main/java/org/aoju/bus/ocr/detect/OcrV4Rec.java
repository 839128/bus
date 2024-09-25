package org.aoju.bus.ocr.detect;

import ai.djl.modality.cv.Image;
import ai.djl.translate.Translator;
import org.aoju.bus.ocr.entity.WordBlock;
import org.aoju.bus.ocr.translator.AbstractDjlTranslator;
import org.aoju.bus.ocr.translator.OcrRecTranslator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文字识别
 */
public  class OcrV4Rec extends AbstractDjlTranslator<WordBlock> {

    public OcrV4Rec(Map<String, Object> arguments) {
        super("ch_PP-OCRv4_rec_infer.zip", arguments);
    }
    @Override
    protected Translator<Image, WordBlock> getTranslator(Map<String, Object> arguments) {
        return new OcrRecTranslator(new ConcurrentHashMap<String, String>());
    }

    @Override
    protected Class<WordBlock> getClassOfT() {
        return WordBlock.class;
    }
    @Override
    protected String getEngine(){
        return "OnnxRuntime";
    }
}
