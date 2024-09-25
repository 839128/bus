package org.aoju.bus.ocr.entity;

import ai.djl.ndarray.NDArray;
import lombok.Data;

/**
 * 旋转检测框 - 支持左上角 X 坐标升序排序
 *
 * @author Calvin
 * @mail 179209347@qq.com
 * @website www.aias.top
 */
@Data
public class RotatedBoxCompX extends WordBlock implements Comparable<RotatedBoxCompX> {
   

    public RotatedBoxCompX( WordBlock wordBlock) {
        this.setBoxPoint(wordBlock.getBoxPoint());
        this.setText(wordBlock.getText());
        this.setCharScores(wordBlock.getCharScores());
        this.setBox(wordBlock.getBox());
    }

    /**
     * 将左上角 X 坐标升序排序
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(RotatedBoxCompX o) {
        NDArray leftBox = this.getBox();
        NDArray rightBox = o.getBox();
        float leftX = leftBox.toFloatArray()[0];
        float rightX = rightBox.toFloatArray()[0];
        return (leftX < rightX) ? -1 : 1;
    }
}
