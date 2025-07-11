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
package org.miaixz.bus.image.nimble.codec;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.metric.Property;
import org.miaixz.bus.image.nimble.codec.jpeg.PatchJPEGLS;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ImageReaderFactory implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852288677583L;

    private static volatile ImageReaderFactory defaultFactory;
    private final TreeMap<String, ImageReaderParam> map = new TreeMap<>();

    private static String nullify(String s) {
        return null == s || s.isEmpty() || s.equals(Symbol.STAR) ? null : s;
    }

    public static ImageReaderFactory getDefault() {
        if (defaultFactory == null)
            defaultFactory = init();

        return defaultFactory;
    }

    public static void setDefault(ImageReaderFactory factory) {
        if (factory == null)
            throw new NullPointerException();

        defaultFactory = factory;
    }

    public static void resetDefault() {
        defaultFactory = null;
    }

    public static ImageReaderParam getImageReaderParam(String tsuid) {
        return getDefault().get(tsuid);
    }

    public static boolean canDecompress(String tsuid) {
        return getDefault().contains(tsuid);
    }

    public static ImageReader getImageReader(ImageReaderParam param) {
        return Boolean.getBoolean("org.miaixz.bus.image.nimble.codec.useServiceLoader")
                ? getImageReaderFromServiceLoader(param)
                : getImageReaderFromImageIOServiceRegistry(param);
    }

    public static ImageReader getImageReaderFromImageIOServiceRegistry(ImageReaderParam param) {
        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName(param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Reader for format: " + param.formatName + " registered");

        ImageReader reader = iter.next();
        if (param.className != null) {
            while (!param.className.equals(reader.getClass().getName())) {
                if (iter.hasNext())
                    reader = iter.next();
                else {
                    Logger.warn("No preferred Reader {} for format: {} - use {}", param.className, param.formatName,
                            reader.getClass().getName());
                    break;
                }
            }
        }
        return reader;
    }

    public static ImageReader getImageReaderFromServiceLoader(ImageReaderParam param) {
        try {
            return getImageReaderSpi(param).createReaderInstance();
        } catch (IOException e) {
            throw new RuntimeException("Error instantiating Reader for format: " + param.formatName, e);
        }
    }

    private static ImageReaderSpi getImageReaderSpi(ImageReaderParam param) {
        Iterator<ImageReaderSpi> iter = new FormatNameFilterIterator<>(
                ServiceLoader.load(ImageReaderSpi.class).iterator(), param.formatName);
        if (!iter.hasNext())
            throw new RuntimeException("No Reader for format: " + param.formatName + " registered");

        ImageReaderSpi spi = iter.next();
        if (param.className != null) {
            while (!param.className.equals(spi.getPluginClassName())) {
                if (iter.hasNext())
                    spi = iter.next();
                else {
                    Logger.warn("No preferred Reader {} for format: {} - use {}", param.className, param.formatName,
                            spi.getPluginClassName());
                    break;
                }
            }
        }
        return spi;
    }

    public static ImageReaderFactory init() {
        ImageReaderFactory factory = new ImageReaderFactory();
        URL url = ResourceKit.getResourceUrl("ImageReaderFactory.properties", ImageReaderFactory.class);
        try {
            Properties props = new Properties();
            props.load(url.openStream());
            for (Entry<Object, Object> entry : props.entrySet()) {
                String[] ss = Builder.split((String) entry.getValue(), ':');
                factory.map.put((String) entry.getKey(), new ImageReaderParam(ss[0], ss[1], ss[2],
                        ss.length > 3 ? Builder.split(ss[3], ';') : Normal.EMPTY_STRING_ARRAY));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Image Reader Factory configuration from: " + url.toString(), e);
        }
        return factory;
    }

    public ImageReaderParam get(String tsuid) {
        return map.get(tsuid);
    }

    public boolean contains(String tsuid) {
        return map.containsKey(tsuid);
    }

    public ImageReaderParam put(String tsuid, ImageReaderParam param) {
        return map.put(tsuid, param);
    }

    public ImageReaderParam remove(String tsuid) {
        return map.remove(tsuid);
    }

    public Set<Entry<String, ImageReaderParam>> getEntries() {
        return Collections.unmodifiableMap(map).entrySet();
    }

    public void clear() {
        map.clear();
    }

    public static class ImageReaderParam implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852288722533L;

        public final String formatName;
        public final String className;
        public final PatchJPEGLS patchJPEGLS;
        public final Property[] imageReadParams;

        public ImageReaderParam(String formatName, String className, PatchJPEGLS patchJPEGLS,
                Property[] imageReadParams) {
            this.formatName = formatName;
            this.className = nullify(className);
            this.patchJPEGLS = patchJPEGLS;
            this.imageReadParams = imageReadParams;
        }

        public ImageReaderParam(String formatName, String className, String patchJPEGLS, String... imageWriteParams) {
            this(formatName, className,
                    patchJPEGLS != null && !patchJPEGLS.isEmpty() ? PatchJPEGLS.valueOf(patchJPEGLS) : null,
                    Property.valueOf(imageWriteParams));
        }

        public Property[] getImageReadParams() {
            return imageReadParams;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            ImageReaderParam that = (ImageReaderParam) o;

            if (!formatName.equals(that.formatName))
                return false;
            if (!Objects.equals(className, that.className))
                return false;
            if (patchJPEGLS != that.patchJPEGLS)
                return false;
            return Arrays.equals(imageReadParams, that.imageReadParams);

        }

        @Override
        public int hashCode() {
            int result = formatName.hashCode();
            result = 31 * result + (className != null ? className.hashCode() : 0);
            result = 31 * result + (patchJPEGLS != null ? patchJPEGLS.hashCode() : 0);
            result = 31 * result + Arrays.hashCode(imageReadParams);
            return result;
        }

        @Override
        public String toString() {
            return "ImageReaderParam{" + "formatName='" + formatName + Symbol.C_SINGLE_QUOTE + ", className='"
                    + className + Symbol.C_SINGLE_QUOTE + ", patchJPEGLS=" + patchJPEGLS + ", imageReadParams="
                    + Arrays.toString(imageReadParams) + '}';
        }
    }

}
