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
package org.miaixz.bus.extra.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ColorKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 图片背景识别处理、背景替换、背景设置为矢量图，根据一定规则算出图片背景色的RGB值，进行替换
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageRemoval {

    /**
     * 目前暂时支持的图片类型数组 其他格式的不保证结果
     */
    public static String[] IMAGES_TYPE = { ImageKit.IMAGE_TYPE_JPG, ImageKit.IMAGE_TYPE_JPEG, ImageKit.IMAGE_TYPE_PNG };

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定地容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param inputPath  要处理图片的路径
     * @param outputPath 输出图片的路径
     * @param tolerance  容差值[根据图片的主题色,加入容差值,值的范围在0~255之间]
     */
    public static void backgroundRemoval(final String inputPath, final String outputPath, final int tolerance) {
        backgroundRemoval(new File(inputPath), new File(outputPath), tolerance);
    }

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定地容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param input     需要进行操作的图片
     * @param output    最后输出的文件
     * @param tolerance 容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     */
    public static void backgroundRemoval(final File input, final File output, final int tolerance) {
        backgroundRemoval(input, output, null, tolerance);
    }

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定地容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param input     需要进行操作的图片
     * @param output    最后输出的文件，必须为.png
     * @param override  指定替换成的背景颜色 为null时背景为透明
     * @param tolerance 容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     */
    public static void backgroundRemoval(final File input, final File output, final Color override,
            final int tolerance) {
        fileTypeValidation(input, IMAGES_TYPE);
        BufferedImage bufferedImage = null;
        try {
            // 获取图片左上、中上、右上、右中、右下、下中、左下、左中、8个像素点rgb的16进制值
            bufferedImage = ImageKit.read(input);
            // 图片输出的格式为 png
            ImageKit.write(backgroundRemoval(bufferedImage, override, tolerance), output);
        } finally {
            ImageKit.flush(bufferedImage);
        }
    }

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定地容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param bufferedImage 需要进行处理的图片流
     * @param override      指定替换成的背景颜色 为null时背景为透明
     * @param tolerance     容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     * @return 返回处理好的图片流
     */
    public static BufferedImage backgroundRemoval(final BufferedImage bufferedImage, final Color override,
            int tolerance) {
        // 容差值 最大255 最小0
        tolerance = Math.min(255, Math.max(tolerance, 0));
        // 绘制icon
        final ImageIcon imageIcon = new ImageIcon(bufferedImage);
        final BufferedImage image = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
        // 绘图工具
        final Graphics graphics = image.getGraphics();
        graphics.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
        // 需要删除的RGB元素
        final String[] removeRgb = getRemoveRgb(bufferedImage);
        // 获取图片的大概主色调
        final String mainColor = getMainColor(bufferedImage);
        final int alpha = 0;
        for (int y = image.getMinY(); y < image.getHeight(); y++) {
            for (int x = image.getMinX(); x < image.getWidth(); x++) {
                // 获取像素的16进制
                int rgb = image.getRGB(x, y);
                final String hex = ColorKit.toHex((rgb & 0xff0000) >> 16, (rgb & 0xff00) >> 8, (rgb & 0xff));
                final boolean isTrue = ArrayKit.contains(removeRgb, hex) || areColorsWithinTolerance(
                        hexToRgb(mainColor), new Color(Integer.parseInt(hex.substring(1), 16)), tolerance);
                if (isTrue) {
                    rgb = override == null ? ((alpha + 1) << 24) | (rgb & 0x00ffffff) : override.getRGB();
                }
                image.setRGB(x, y, rgb);
            }
        }
        graphics.drawImage(image, 0, 0, imageIcon.getImageObserver());
        return image;
    }

    /**
     * 背景移除 图片去底工具 将 "纯色背景的图片" 还原成 "透明背景的图片" 将纯色背景的图片转成矢量图 取图片边缘的像素点和获取到的图片主题色作为要替换的背景色 再加入一定地容差值,然后将所有像素点与该颜色进行比较
     * 发现相同则将颜色不透明度设置为0,使颜色完全透明.
     *
     * @param outputStream 需要进行处理的图片字节数组流
     * @param override     指定替换成的背景颜色 为null时背景为透明
     * @param tolerance    容差值[根据图片的主题色,加入容差值,值的取值范围在0~255之间]
     * @return 返回处理好的图片流
     */
    public static BufferedImage backgroundRemoval(final ByteArrayOutputStream outputStream, final Color override,
            final int tolerance) {
        try {
            return backgroundRemoval(ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray())), override,
                    tolerance);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取要删除的 RGB 元素 分别获取图片左上、中上、右上、右中、右下、下中、左下、左中、8个像素点rgb的16进制值
     *
     * @param image 图片流
     * @return String数组 包含 各个位置的rgb数值
     */
    private static String[] getRemoveRgb(final BufferedImage image) {
        // 获取图片流的宽和高
        final int width = image.getWidth() - 1;
        final int height = image.getHeight() - 1;
        // 左上
        final int leftUpPixel = image.getRGB(1, 1);
        final String leftUp = ColorKit.toHex((leftUpPixel & 0xff0000) >> 16, (leftUpPixel & 0xff00) >> 8,
                (leftUpPixel & 0xff));
        // 上中
        final int upMiddlePixel = image.getRGB(width / 2, 1);
        final String upMiddle = ColorKit.toHex((upMiddlePixel & 0xff0000) >> 16, (upMiddlePixel & 0xff00) >> 8,
                (upMiddlePixel & 0xff));
        // 右上
        final int rightUpPixel = image.getRGB(width, 1);
        final String rightUp = ColorKit.toHex((rightUpPixel & 0xff0000) >> 16, (rightUpPixel & 0xff00) >> 8,
                (rightUpPixel & 0xff));
        // 右中
        final int rightMiddlePixel = image.getRGB(width, height / 2);
        final String rightMiddle = ColorKit.toHex((rightMiddlePixel & 0xff0000) >> 16, (rightMiddlePixel & 0xff00) >> 8,
                (rightMiddlePixel & 0xff));
        // 右下
        final int lowerRightPixel = image.getRGB(width, height);
        final String lowerRight = ColorKit.toHex((lowerRightPixel & 0xff0000) >> 16, (lowerRightPixel & 0xff00) >> 8,
                (lowerRightPixel & 0xff));
        // 下中
        final int lowerMiddlePixel = image.getRGB(width / 2, height);
        final String lowerMiddle = ColorKit.toHex((lowerMiddlePixel & 0xff0000) >> 16, (lowerMiddlePixel & 0xff00) >> 8,
                (lowerMiddlePixel & 0xff));
        // 左下
        final int leftLowerPixel = image.getRGB(1, height);
        final String leftLower = ColorKit.toHex((leftLowerPixel & 0xff0000) >> 16, (leftLowerPixel & 0xff00) >> 8,
                (leftLowerPixel & 0xff));
        // 左中
        final int leftMiddlePixel = image.getRGB(1, height / 2);
        final String leftMiddle = ColorKit.toHex((leftMiddlePixel & 0xff0000) >> 16, (leftMiddlePixel & 0xff00) >> 8,
                (leftMiddlePixel & 0xff));
        // 需要删除的RGB元素
        return new String[] { leftUp, upMiddle, rightUp, rightMiddle, lowerRight, lowerMiddle, leftLower, leftMiddle };
    }

    /**
     * 十六进制颜色码转RGB颜色值
     *
     * @param hex 十六进制颜色码
     * @return 返回 RGB颜色值
     */
    public static Color hexToRgb(final String hex) {
        return new Color(Integer.parseInt(hex.substring(1), 16));
    }

    /**
     * 判断颜色是否在容差范围内 对比两个颜色的相似度，判断这个相似度是否小于 tolerance 容差值
     *
     * @param color1    颜色1
     * @param color2    颜色2
     * @param tolerance 容差值
     * @return 返回true:两个颜色在容差值之内 false: 不在
     */
    public static boolean areColorsWithinTolerance(final Color color1, final Color color2, final int tolerance) {
        return areColorsWithinTolerance(color1, color2, new Color(tolerance, tolerance, tolerance));
    }

    /**
     * 判断颜色是否在容差范围内 对比两个颜色的相似度，判断这个相似度是否小于 tolerance 容差值
     *
     * @param color1    颜色1
     * @param color2    颜色2
     * @param tolerance 容差色值
     * @return 返回true:两个颜色在容差值之内 false: 不在
     */
    public static boolean areColorsWithinTolerance(final Color color1, final Color color2, final Color tolerance) {
        return (color1.getRed() - color2.getRed() < tolerance.getRed()
                && color1.getRed() - color2.getRed() > -tolerance.getRed())
                && (color1.getBlue() - color2.getBlue() < tolerance.getBlue()
                        && color1.getBlue() - color2.getBlue() > -tolerance.getBlue())
                && (color1.getGreen() - color2.getGreen() < tolerance.getGreen()
                        && color1.getGreen() - color2.getGreen() > -tolerance.getGreen());
    }

    /**
     * 获取图片大概的主题色 循环所有的像素点,取出出现次数最多的一个像素点的RGB值
     *
     * @param input 图片文件路径
     * @return 返回一个图片的大概的色值 一个16进制的颜色码
     */
    public static String getMainColor(final String input) {
        return getMainColor(new File(input));
    }

    /**
     * 获取图片大概的主题色 循环所有的像素点,取出出现次数最多的一个像素点的RGB值
     *
     * @param input 图片文件
     * @return 返回一个图片的大概的色值 一个16进制的颜色码
     */
    public static String getMainColor(final File input) {
        try {
            return getMainColor(ImageIO.read(input));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取图片大概的主题色 循环所有的像素点,取出出现次数最多的一个像素点的RGB值
     *
     * @param bufferedImage 图片流
     * @return 返回一个图片的大概的色值 一个16进制的颜色码
     */
    public static String getMainColor(final BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("图片流是空的");
        }

        // 存储图片的所有RGB元素
        final List<String> list = new ArrayList<>();
        for (int y = bufferedImage.getMinY(); y < bufferedImage.getHeight(); y++) {
            for (int x = bufferedImage.getMinX(); x < bufferedImage.getWidth(); x++) {
                final int pixel = bufferedImage.getRGB(x, y);
                list.add(((pixel & 0xff0000) >> 16) + Symbol.MINUS + ((pixel & 0xff00) >> 8) + Symbol.MINUS
                        + (pixel & 0xff));
            }
        }

        final Map<String, Integer> map = new HashMap<>(list.size(), 1);
        for (final String string : list) {
            Integer integer = map.get(string);
            if (integer == null) {
                integer = 1;
            } else {
                integer++;
            }
            map.put(string, integer);
        }
        String max = Normal.EMPTY;
        long num = 0;
        for (final Map.Entry<String, Integer> entry : map.entrySet()) {
            final String key = entry.getKey();
            final Integer temp = entry.getValue();
            if (StringKit.isBlank(max) || temp > num) {
                max = key;
                num = temp;
            }
        }
        final String[] strings = max.split(Symbol.MINUS);
        // rgb 的数量只有3个
        final int rgbLength = 3;
        if (strings.length == rgbLength) {
            return ColorKit.toHex(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]),
                    Integer.parseInt(strings[2]));
        }
        return Normal.EMPTY;
    }

    /**
     * 文件类型验证 根据给定文件类型数据，验证给定文件类型.
     *
     * @param input      需要进行验证的文件
     * @param imagesType 文件包含的类型数组
     */
    private static void fileTypeValidation(final File input, final String[] imagesType) {
        Assert.isTrue(input.exists(), "File {} not exist!", input);
        // 获取图片类型
        final String type = FileType.getType(input);
        // 类型对比
        if (!ArrayKit.contains(imagesType, type)) {
            throw new IllegalArgumentException(StringKit.format("Format {} of File not supported!", type));
        }
    }

}
