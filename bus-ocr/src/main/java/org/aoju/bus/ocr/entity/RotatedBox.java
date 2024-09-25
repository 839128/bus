package org.aoju.bus.ocr.entity;

import ai.djl.ndarray.NDArray;
import lombok.Data;

/**
 * 旋转检测框
 */
@Data
public class RotatedBox extends WordBlock implements Comparable<RotatedBox> {
    public RotatedBox(WordBlock wordBlock) {
        this.setBoxPoint(wordBlock.getBoxPoint());
        this.setText(wordBlock.getText());
        this.setCharScores(wordBlock.getCharScores());
        this.setBox(wordBlock.getBox());
    }

    /**
     * 将左上角 Y 坐标升序排序
     *
     * @param o 入参
     * @return 结果
     */
    @Override
    public int compareTo(RotatedBox o) {
        NDArray lowBox = this.getBox();
        NDArray highBox = o.getBox();
        float lowY = lowBox.toFloatArray()[1];
        float highY = highBox.toFloatArray()[1];
        return (lowY < highY) ? -1 : 1;
    }
}
