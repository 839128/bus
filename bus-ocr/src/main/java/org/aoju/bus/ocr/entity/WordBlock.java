package org.aoju.bus.ocr.entity;

import ai.djl.modality.cv.output.Point;
import ai.djl.ndarray.NDArray;
import lombok.Data;

import java.util.List;

@Data
public class WordBlock {
    private String text;
    private float[] charScores;
    private List<Point> boxPoint;
    private NDArray box;
    
    
}
