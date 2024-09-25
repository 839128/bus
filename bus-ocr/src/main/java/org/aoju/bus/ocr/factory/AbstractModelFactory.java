package org.aoju.bus.ocr.factory;

import ai.djl.Device;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public abstract class AbstractModelFactory<T> {

    public String modelName;

    public Map<String, Object> arguments;

    public AbstractModelFactory(String modelName, Map<String, Object> arguments) {
        this.modelName = modelName;
        this.arguments = arguments;
    }

    public Criteria<Image, T> criteria() {
        Translator<Image, T> translator = getTranslator(arguments);
        String modelPath = "jar:///" + "model/onnx/" + modelName;
        return Criteria.builder()
                .setTypes(Image.class, getClassOfT())
                .optModelUrls(modelPath)
                .optTranslator(translator)
                .optEngine(getEngine()) // Use PyTorch engine
                .optProgress(new ProgressBar())
                .optDevice(Device.cpu())
                .build();
    }

    protected abstract Translator<Image, T> getTranslator(Map<String, Object> arguments);

    // 获取 T 类型的函数
    protected abstract Class<T> getClassOfT();

    protected abstract String getEngine();

}
