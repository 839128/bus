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
package org.miaixz.bus.image.builtin;

import org.miaixz.bus.core.lang.Normal;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Metaphone implements FuzzyString {

    private static boolean vowel(char ch) {
        return ch == 'A' || ch == 'E' || ch == 'I' || ch == 'O' || ch == 'U';
    }

    @Override
    public String toFuzzy(String s) {
        if (null == s || s.length() == 0)
            return Normal.EMPTY;

        char[] in = s.toUpperCase().toCharArray();
        int countX = 0;
        for (char c : in)
            if (c == 'X')
                countX++;
        char[] out = countX > 0 ? new char[in.length + countX] : in;
        int i = 0;
        int j = 0;
        char prev = 0;
        char cur = 0;
        char next1 = in[0];
        char next2 = in.length > 1 ? in[1] : 0;

        // Initial kn-, gn- pn, ae- or wr- -> drop first letter
        if (next2 == 'N' && (next1 == 'K' || next1 == 'G' || next1 == 'P') || next1 == 'A' && next2 == 'E'
                || next1 == 'W' && next2 == 'R') {
            next1 = next2;
            next2 = in.length > 2 ? in[2] : 0;
            i++;
            // Initial x- -> change to "s"
        } else if (next1 == 'X') {
            next1 = 'S';
            // Initial wh- -> change to "w"
        } else if (next1 == 'W' && next2 == 'H') {
            next2 = in.length > 2 ? in[2] : 0;
            i++;
        }
        for (; i < in.length; i++) {
            prev = cur;
            cur = next1;
            next1 = next2;
            next2 = i + 2 < in.length ? in[i + 2] : 0;
            // Doubled letters except "g" -> drop 2nd letter.
            if (cur == prev && cur != 'C')
                continue;

            switch (cur) {
            // Vowels are only kept when they are the first letter.
            case 'A':
            case 'E':
            case 'I':
            case 'O':
            case 'U':
                if (j == 0)
                    out[j++] = cur;
                break;
            // B -> B unless at the end of a word after "m" as in "dumb"
            case 'B':
                if (!(next1 == 0 && prev == 'M'))
                    out[j++] = cur;
                break;
            // C -> X (sh) if -cia- or -ch-
            // S if -ci-, -ce- or -cy-
            // SILENT if "-sci-", "-sce-", or "-scy-"
            // K otherwise, including -sch-
            case 'C':
                if (next1 == 'I' || next1 == 'E' || next1 == 'Y') {
                    if (prev != 'S')
                        out[j++] = next1 == 'I' && next2 == 'A' ? 'X' : 'S';
                } else
                    out[j++] = next1 == 'H' && prev != 'S' ? 'X' : 'K';
                break;
            // D -> J if in -dge-, -dgy- or -dgi-
            // T otherwise
            case 'D':
                out[j++] = next1 == 'G' && (next2 == 'I' || next2 == 'E' || next2 == 'Y') ? 'J' : 'T';
                break;
            // F -> F
            // J -> J
            // L -> L
            // M -> M
            // N -> N
            // R -> R
            case 'F':
            case 'J':
            case 'L':
            case 'M':
            case 'N':
            case 'R':
                out[j++] = cur;
                break;
            // G -> silent if in -gh- and not at end or before a vowel
            // in -gn or -gned (also see dge etc. above)
            // J if before i or e or y if not double gg
            // K otherwise
            case 'G':
                if (next1 == 'H' && next2 != 0 && !vowel(next2)
                        || next1 == 'N' && (next2 == 0 || next2 == 'E' && in.length == (i + 4) && in[3] == 'D')
                        || prev == 'D' && (next1 == 'I' || next1 == 'E' || next1 == 'Y'))
                    continue;
                // if double gg, next1 == 'G' -> K
                out[j++] = (next1 == 'I' || next1 == 'E' || next1 == 'Y') ? 'J' : 'K';
                break;
            // H -> silent if after vowel and no vowel follows
            // or in "-ch-", "-sh-", "-ph-", "-th-", "-gh-"
            // H otherwise
            case 'H':
                switch (prev) {
                case 'A':
                case 'E':
                case 'I':
                case 'O':
                case 'U':
                    if (!vowel(next1))
                        continue;
                    break;
                case 'C':
                case 'S':
                case 'P':
                case 'T':
                case 'G':
                    continue;
                }
                out[j++] = cur;
                break;
            // K -> silent if after "c"
            // K otherwise
            case 'K':
                if (prev != 'C')
                    out[j++] = cur;
                break;
            // P -> F if before "h"
            // P otherwise
            case 'P':
                out[j++] = (next1 == 'H') ? 'F' : 'P';
                break;
            // Q -> K
            case 'Q':
                out[j++] = 'K';
                break;
            // S -> X (sh) if before "h" or in -sio- or -sia-
            // S otherwise
            case 'S':
                out[j++] = next1 == 'H' || next1 == 'I' && (next2 == 'O' || next2 == 'A') ? 'X' : 'S';
                break;
            // T -> X (sh) if -tia- or -tio-
            // 0 (th) if before "h"
            // silent if in -tch-
            // T otherwise
            case 'T':
                if (!(next1 == 'C' || next2 == 'H'))
                    out[j++] = next1 == 'I' && (next2 == 'A' || next2 == 'O') ? 'X' : (next1 == 'H') ? '0' : 'T';
                break;
            // V -> F
            case 'V':
                out[j++] = 'F';
                break;
            // W -> silent if not followed by a vowel
            // W if followed by a vowel
            // Y -> silent if not followed by a vowel
            // Y if followed by a vowel
            case 'W':
            case 'Y':
                if (vowel(next1))
                    out[j++] = cur;
                break;
            // X -> KS
            case 'X':
                out[j++] = 'K';
                out[j++] = 'S';
                break;
            // Z -> S
            case 'Z':
                out[j++] = 'S';
                break;
            default:
                continue;
            }
        }
        return new String(out, 0, j);
    }

}
