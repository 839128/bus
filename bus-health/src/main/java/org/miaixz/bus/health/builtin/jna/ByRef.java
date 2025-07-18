/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.builtin.jna;

import org.miaixz.bus.health.Builder;

import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTRByReference;
import com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32;
import com.sun.jna.platform.win32.WinDef.LONGLONGByReference;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Wrapper classes for JNA clases which extend {@link com.sun.jna.ptr.ByReference} intended for use in
 * try-with-resources blocks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ByRef {

    class CloseableIntByReference extends IntByReference implements AutoCloseable {
        public CloseableIntByReference() {
            super();
        }

        public CloseableIntByReference(int value) {
            super(value);
        }

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableLongByReference extends LongByReference implements AutoCloseable {
        public CloseableLongByReference() {
            super();
        }

        public CloseableLongByReference(long value) {
            super(value);
        }

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableNativeLongByReference extends NativeLongByReference implements AutoCloseable {
        public CloseableNativeLongByReference() {
            super();
        }

        public CloseableNativeLongByReference(NativeLong nativeLong) {
            super(nativeLong);
        }

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseablePointerByReference extends PointerByReference implements AutoCloseable {
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableLONGLONGByReference extends LONGLONGByReference implements AutoCloseable {
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableULONGptrByReference extends ULONG_PTRByReference implements AutoCloseable {
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableHANDLEByReference extends HANDLEByReference implements AutoCloseable {
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableSizeTByReference extends size_t.ByReference implements AutoCloseable {
        public CloseableSizeTByReference() {
            super();
        }

        public CloseableSizeTByReference(long value) {
            super(value);
        }

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseablePROCESSENTRY32ByReference extends PROCESSENTRY32.ByReference implements AutoCloseable {
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

}
