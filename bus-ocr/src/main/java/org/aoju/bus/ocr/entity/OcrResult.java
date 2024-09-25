package org.aoju.bus.ocr.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Justubborn
 * @since 2024/7/24
 */
@Data
@AllArgsConstructor
public class OcrResult {
    private String img;
    private String text;
}
