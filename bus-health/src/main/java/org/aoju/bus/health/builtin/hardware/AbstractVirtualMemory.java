/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.builtin.hardware;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;

/**
 * Virtual Memory info.
 *
 * @author Kimi Liu
 * @version 5.9.1
 * @since JDK 1.8+
 */
@ThreadSafe
public abstract class AbstractVirtualMemory implements VirtualMemory {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Swap Used/Avail: ");
        sb.append(Builder.formatBytes(getSwapUsed()));
        sb.append(Symbol.SLASH);
        sb.append(Builder.formatBytes(getSwapTotal()));
        sb.append(", Virtual Memory In Use/Max=");
        sb.append(Builder.formatBytes(getVirtualInUse()));
        sb.append(Symbol.SLASH);
        sb.append(Builder.formatBytes(getVirtualMax()));
        return sb.toString();
    }

}