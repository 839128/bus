package org.aoju.bus.ocr.detect;

import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Translator;
import org.aoju.bus.ocr.translator.AbstractDjlTranslator;
import org.aoju.bus.ocr.translator.OcrDetTranslator;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public  class OcrV4Det extends AbstractDjlTranslator<NDList> {


  public OcrV4Det(Map<String, Object> arguments)  {
    super("ch_PP-OCRv4_det_infer.zip",arguments);
  }
  @Override
  protected Translator<Image, NDList> getTranslator(Map<String, Object> arguments) {
    return new OcrDetTranslator(new ConcurrentHashMap<String, String>());
  }

  @Override
  protected Class<NDList> getClassOfT() {
    return NDList.class;
  }
  @Override
  protected String getEngine(){
    return "OnnxRuntime";
  }
}
