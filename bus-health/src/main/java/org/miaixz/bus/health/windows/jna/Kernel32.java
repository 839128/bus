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
package org.miaixz.bus.health.windows.jna;

import com.sun.jna.Native;

/**
 * Kernel32. This class should be considered non-API as it may be removed if/when its code is incorporated into the JNA
 * project.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {

    /**
     * Constant <code>INSTANCE</code>
     */
    Kernel32 INSTANCE = Native.load("Kernel32", Kernel32.class);

    enum ProcessorFeature {
        PF_FLOATING_POINT_PRECISION_ERRATA(0), PF_FLOATING_POINT_EMULATED(1), PF_COMPARE_EXCHANGE_DOUBLE(2),
        PF_MMX_INSTRUCTIONS_AVAILABLE(3), PF_PPC_MOVEMEM_64BIT_OK(4), PF_ALPHA_BYTE_INSTRUCTIONS(5),
        PF_XMMI_INSTRUCTIONS_AVAILABLE(6), PF_3DNOW_INSTRUCTIONS_AVAILABLE(7), PF_RDTSC_INSTRUCTION_AVAILABLE(8),
        PF_PAE_ENABLED(9), PF_XMMI64_INSTRUCTIONS_AVAILABLE(10), PF_SSE_DAZ_MODE_AVAILABLE(11), PF_NX_ENABLED(12),
        PF_SSE3_INSTRUCTIONS_AVAILABLE(13), PF_COMPARE_EXCHANGE128(14), PF_COMPARE64_EXCHANGE128(15),
        PF_CHANNELS_ENABLED(16), PF_XSAVE_ENABLED(17), PF_ARM_VFP_32_REGISTERS_AVAILABLE(18),
        PF_ARM_NEON_INSTRUCTIONS_AVAILABLE(19), PF_SECOND_LEVEL_ADDRESS_TRANSLATION(20), PF_VIRT_FIRMWARE_ENABLED(21),
        PF_RDWRFSGSBASE_AVAILABLE(22), PF_FASTFAIL_AVAILABLE(23), PF_ARM_DIVIDE_INSTRUCTION_AVAILABLE(24),
        PF_ARM_64BIT_LOADSTORE_ATOMIC(25), PF_ARM_EXTERNAL_CACHE_AVAILABLE(26), PF_ARM_FMAC_INSTRUCTIONS_AVAILABLE(27),
        PF_RDRAND_INSTRUCTION_AVAILABLE(28), PF_ARM_V8_INSTRUCTIONS_AVAILABLE(29),
        PF_ARM_V8_CRYPTO_INSTRUCTIONS_AVAILABLE(30), PF_ARM_V8_CRC32_INSTRUCTIONS_AVAILABLE(31),
        PF_RDTSCP_INSTRUCTION_AVAILABLE(32), PF_RDPID_INSTRUCTION_AVAILABLE(33),
        PF_ARM_V81_ATOMIC_INSTRUCTIONS_AVAILABLE(34), PF_SSSE3_INSTRUCTIONS_AVAILABLE(36),
        PF_SSE4_1_INSTRUCTIONS_AVAILABLE(37), PF_SSE4_2_INSTRUCTIONS_AVAILABLE(38), PF_AVX_INSTRUCTIONS_AVAILABLE(39),
        PF_AVX2_INSTRUCTIONS_AVAILABLE(40), PF_AVX512F_INSTRUCTIONS_AVAILABLE(41),
        PF_ARM_V82_DP_INSTRUCTIONS_AVAILABLE(43), PF_ARM_V83_JSCVT_INSTRUCTIONS_AVAILABLE(44),
        PF_ARM_V83_LRCPC_INSTRUCTIONS_AVAILABLE(45);

        private final int value;

        ProcessorFeature(int value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public int value() {
            return value;
        }
    }

}