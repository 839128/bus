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
package org.miaixz.bus.core.text;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Ant风格的路径匹配器。 来自Spring-core和Ant
 *
 * <p>
 * 匹配URL的规则如下：
 * <ul>
 * <li>{@code ?} 匹配单个字符</li>
 * <li>{@code *} 匹配0个或多个字符</li>
 * <li>{@code **} 0个或多个路径中的<em>目录节点</em></li>
 * <li>{@code {bus:[a-z]+}} 匹配以"bus"命名的正则 {@code [a-z]+}</li>
 * </ul>
 *
 * <p>
 * 示例:
 * </p>
 * <ul>
 * <li>{@code com/t?st.jsp} &mdash; 匹配 {@code com/test.jsp} 或 {@code com/tast.jsp} 或 {@code com/txst.jsp}</li>
 * <li>{@code com/*.jsp} &mdash; 匹配{@code com}目录下全部 {@code .jsp}文件</li>
 * <li>{@code com/&#42;&#42;/test.jsp} &mdash; 匹配{@code com}目录下全部 {@code test.jsp}文件</li>
 * <li>{@code org/bus/&#42;&#42;/*.jsp} &mdash; 匹配{@code org/bus}路径下全部{@code .jsp} 文件</li>
 * <li>{@code org/&#42;&#42;/servlet/bla.jsp} &mdash; 匹配{@code org/bus/servlet/bla.jsp}
 * 或{@code org/bus/testing/servlet/bla.jsp} 或 {@code org/servlet/bla.jsp}</li>
 * <li>{@code com/{filename:\\w+}.jsp} 匹配 {@code com/test.jsp} 并将 {@code test} 关联到 {@code filename} 变量</li>
 * </ul>
 *
 * <p>
 * <strong>注意:</strong> 表达式和路径必须都为绝对路径或都为相对路径。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AntPathMatcher {

    /**
     * Default path separator: "/".
     */
    public static final String DEFAULT_PATH_SEPARATOR = Symbol.SLASH;

    private static final int CACHE_TURNOFF_THRESHOLD = 65536;

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?}");

    private static final char[] WILDCARD_CHARS = { Symbol.C_STAR, Symbol.C_QUESTION_MARK, '{' };
    private final Map<String, String[]> tokenizedPatternCache = new ConcurrentHashMap<>(256);
    private final Map<String, AntPathStringMatcher> stringMatcherCache = new ConcurrentHashMap<>(256);
    private String pathSeparator;
    private PathSeparatorPatternCache pathSeparatorPatternCache;
    private boolean caseSensitive = true;
    private boolean trimTokens = false;
    private volatile Boolean cachePatterns;

    /**
     * 使用 {@link #DEFAULT_PATH_SEPARATOR} 作为分隔符构造
     */
    public AntPathMatcher() {
        this(DEFAULT_PATH_SEPARATOR);
    }

    /**
     * 使用自定义的分隔符构造
     *
     * @param pathSeparator the path separator to use, must not be {@code null}.
     */
    public AntPathMatcher(String pathSeparator) {
        if (null == pathSeparator) {
            pathSeparator = DEFAULT_PATH_SEPARATOR;
        }
        setPathSeparator(pathSeparator);
    }

    /**
     * 设置路径分隔符
     *
     * @param pathSeparator 分隔符，{@code null}表示使用默认分隔符{@link #DEFAULT_PATH_SEPARATOR}
     * @return this
     */
    public AntPathMatcher setPathSeparator(String pathSeparator) {
        if (null == pathSeparator) {
            pathSeparator = DEFAULT_PATH_SEPARATOR;
        }
        this.pathSeparator = pathSeparator;
        this.pathSeparatorPatternCache = new PathSeparatorPatternCache(this.pathSeparator);
        return this;
    }

    /**
     * 设置是否大小写敏感，默认为{@code true}
     *
     * @param caseSensitive 是否大小写敏感
     * @return this
     */
    public AntPathMatcher setCaseSensitive(final boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    /**
     * 设置是否去除路径节点两边的空白符，默认为{@code false}
     *
     * @param trimTokens 是否去除路径节点两边的空白符
     * @return this
     */
    public AntPathMatcher setTrimTokens(final boolean trimTokens) {
        this.trimTokens = trimTokens;
        return this;
    }

    /**
     * Specify whether to cache parsed pattern metadata for patterns passed into this matcher's {@link #match} method. A
     * value of {@code true} activates an unlimited pattern cache; a value of {@code false} turns the pattern cache off
     * completely.
     * <p>
     * Default is for the cache to be on, but with the variant to automatically turn it off when encountering too many
     * patterns to cache at runtime (the threshold is 65536), assuming that arbitrary permutations of patterns are
     * coming in, with little chance for encountering a recurring pattern.
     *
     * @param cachePatterns 是否缓存表达式
     * @return this
     * @see #getStringMatcher(String)
     */
    public AntPathMatcher setCachePatterns(final boolean cachePatterns) {
        this.cachePatterns = cachePatterns;
        return this;
    }

    /**
     * 判断给定路径是否是表达式
     *
     * @param path 路径
     * @return 是否为表达式
     */
    public boolean isPattern(final String path) {
        if (path == null) {
            return false;
        }
        boolean uriVar = false;
        final int length = path.length();
        char c;
        for (int i = 0; i < length; i++) {
            c = path.charAt(i);
            // 含有通配符
            if (c == Symbol.C_STAR || c == Symbol.C_QUESTION_MARK) {
                return true;
            }
            if (c == Symbol.C_BRACE_LEFT) {
                uriVar = true;
                continue;
            }
            if (c == Symbol.C_BRACE_RIGHT && uriVar) {
                return true;
            }
        }
        return false;
    }

    /**
     * 给定路径是否匹配表达式
     *
     * @param pattern 表达式
     * @param path    路径
     * @return 是否匹配
     */
    public boolean match(final String pattern, final String path) {
        return doMatch(pattern, path, true, null);
    }

    /**
     * 前置部分匹配
     *
     * @param pattern 表达式
     * @param path    路径
     * @return 是否匹配
     */
    public boolean matchStart(final String pattern, final String path) {
        return doMatch(pattern, path, false, null);
    }

    /**
     * 执行匹配，判断给定的{@code path}是否匹配{@code pattern}
     *
     * @param pattern              表达式
     * @param path                 路径
     * @param fullMatch            是否全匹配。{@code true} 表示全路径匹配，{@code false}表示只匹配开始
     * @param uriTemplateVariables 变量映射
     * @return {@code true} 表示提供的 {@code path} 匹配, {@code false} 表示不匹配
     */
    protected boolean doMatch(final String pattern, final String path, final boolean fullMatch,
            final Map<String, String> uriTemplateVariables) {
        if (path == null || path.startsWith(this.pathSeparator) != pattern.startsWith(this.pathSeparator)) {
            return false;
        }

        final String[] pattDirs = tokenizePattern(pattern);
        if (fullMatch && this.caseSensitive && !isPotentialMatch(path, pattDirs)) {
            return false;
        }

        final String[] pathDirs = tokenizePath(path);
        int pattIdxStart = 0;
        int pattIdxEnd = pattDirs.length - 1;
        int pathIdxStart = 0;
        int pathIdxEnd = pathDirs.length - 1;

        // Match all elements up to the first **
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            final String pattDir = pattDirs[pattIdxStart];
            if ("**".equals(pattDir)) {
                break;
            }
            if (notMatchStrings(pattDir, pathDirs[pathIdxStart], uriTemplateVariables)) {
                return false;
            }
            pattIdxStart++;
            pathIdxStart++;
        }

        if (pathIdxStart > pathIdxEnd) {
            // Path is exhausted, only match if rest of pattern is * or **'s
            if (pattIdxStart > pattIdxEnd) {
                return (pattern.endsWith(this.pathSeparator) == path.endsWith(this.pathSeparator));
            }
            if (!fullMatch) {
                return true;
            }
            if (pattIdxStart == pattIdxEnd && pattDirs[pattIdxStart].equals(Symbol.STAR)
                    && path.endsWith(this.pathSeparator)) {
                return true;
            }
            for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
                if (!pattDirs[i].equals("**")) {
                    return false;
                }
            }
            return true;
        } else if (pattIdxStart > pattIdxEnd) {
            // String not exhausted, but pattern is. Failure.
            return false;
        } else if (!fullMatch && "**".equals(pattDirs[pattIdxStart])) {
            // Path start definitely matches due to "**" part in pattern.
            return true;
        }

        // up to last '**'
        while (pattIdxStart <= pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            final String pattDir = pattDirs[pattIdxEnd];
            if (pattDir.equals("**")) {
                break;
            }
            if (notMatchStrings(pattDir, pathDirs[pathIdxEnd], uriTemplateVariables)) {
                return false;
            }
            pattIdxEnd--;
            pathIdxEnd--;
        }
        if (pathIdxStart > pathIdxEnd) {
            // String is exhausted
            for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
                if (!pattDirs[i].equals("**")) {
                    return false;
                }
            }
            return true;
        }

        while (pattIdxStart != pattIdxEnd && pathIdxStart <= pathIdxEnd) {
            int patIdxTmp = -1;
            for (int i = pattIdxStart + 1; i <= pattIdxEnd; i++) {
                if (pattDirs[i].equals("**")) {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == pattIdxStart + 1) {
                // '**/**' situation, so skip one
                pattIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in text between
            // strIdxStart & strIdxEnd
            final int patLength = (patIdxTmp - pattIdxStart - 1);
            final int strLength = (pathIdxEnd - pathIdxStart + 1);
            int foundIdx = -1;

            strLoop: for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    final String subPat = pattDirs[pattIdxStart + j + 1];
                    final String subStr = pathDirs[pathIdxStart + i + j];
                    if (notMatchStrings(subPat, subStr, uriTemplateVariables)) {
                        continue strLoop;
                    }
                }
                foundIdx = pathIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            pattIdxStart = patIdxTmp;
            pathIdxStart = foundIdx + patLength;
        }

        for (int i = pattIdxStart; i <= pattIdxEnd; i++) {
            if (!pattDirs[i].equals("**")) {
                return false;
            }
        }

        return true;
    }

    private boolean isPotentialMatch(final String path, final String[] pattDirs) {
        if (!this.trimTokens) {
            int pos = 0;
            for (final String pattDir : pattDirs) {
                int skipped = skipSeparator(path, pos, this.pathSeparator);
                pos += skipped;
                skipped = skipSegment(path, pos, pattDir);
                if (skipped < pattDir.length()) {
                    return (skipped > 0 || (!pattDir.isEmpty() && isWildcardChar(pattDir.charAt(0))));
                }
                pos += skipped;
            }
        }
        return true;
    }

    private int skipSegment(final String path, final int pos, final String prefix) {
        int skipped = 0;
        for (int i = 0; i < prefix.length(); i++) {
            final char c = prefix.charAt(i);
            if (isWildcardChar(c)) {
                return skipped;
            }
            final int currPos = pos + skipped;
            if (currPos >= path.length()) {
                return 0;
            }
            if (c == path.charAt(currPos)) {
                skipped++;
            }
        }
        return skipped;
    }

    private int skipSeparator(final String path, final int pos, final String separator) {
        int skipped = 0;
        while (path.startsWith(separator, pos + skipped)) {
            skipped += separator.length();
        }
        return skipped;
    }

    private boolean isWildcardChar(final char c) {
        for (final char candidate : WILDCARD_CHARS) {
            if (c == candidate) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tokenize the given path pattern into parts, based on this matcher's settings.
     * <p>
     * Performs caching based on {@link #setCachePatterns}, delegating to {@link #tokenizePath(String)} for the actual
     * tokenization algorithm.
     *
     * @param pattern the pattern to tokenize
     * @return the tokenized pattern parts
     */
    protected String[] tokenizePattern(final String pattern) {
        String[] tokenized = null;
        final Boolean cachePatterns = this.cachePatterns;
        if (cachePatterns == null || cachePatterns) {
            tokenized = this.tokenizedPatternCache.get(pattern);
        }
        if (tokenized == null) {
            tokenized = tokenizePath(pattern);
            if (cachePatterns == null && this.tokenizedPatternCache.size() >= CACHE_TURNOFF_THRESHOLD) {
                deactivatePatternCache();
                return tokenized;
            }
            if (cachePatterns == null || cachePatterns) {
                this.tokenizedPatternCache.put(pattern, tokenized);
            }
        }
        return tokenized;
    }

    private void deactivatePatternCache() {
        this.cachePatterns = false;
        this.tokenizedPatternCache.clear();
        this.stringMatcherCache.clear();
    }

    /**
     * Tokenize the given path into parts, based on this matcher's settings.
     *
     * @param path the path to tokenize
     * @return the tokenized path parts
     */
    protected String[] tokenizePath(final String path) {
        return CharsBacker.split(path, this.pathSeparator, this.trimTokens, true).toArray(new String[0]);
    }

    /**
     * Test whether a string matches against a pattern.
     *
     * @param pattern the pattern to match against (never {@code null})
     * @param text    the String which must be matched against the pattern (never {@code null})
     * @return {@code true} if the string matches against the pattern, or {@code false} otherwise
     */
    private boolean notMatchStrings(final String pattern, final String text,
            final Map<String, String> uriTemplateVariables) {
        return !getStringMatcher(pattern).matchStrings(text, uriTemplateVariables);
    }

    /**
     * Build or retrieve an {@link AntPathStringMatcher} for the given pattern.
     * <p>
     * The default implementation checks this AntPathMatcher's internal cache (see {@link #setCachePatterns}), creating
     * a new AntPathStringMatcher instance if no cached copier is found.
     * <p>
     * When encountering too many patterns to cache at runtime (the threshold is 65536), it turns the default cache off,
     * assuming that arbitrary permutations of patterns are coming in, with little chance for encountering a recurring
     * pattern.
     * <p>
     * This method may be overridden to implement a custom cache strategy.
     *
     * @param pattern the pattern to match against (never {@code null})
     * @return a corresponding AntPathStringMatcher (never {@code null})
     * @see #setCachePatterns
     */
    protected AntPathStringMatcher getStringMatcher(final String pattern) {
        AntPathStringMatcher matcher = null;
        final Boolean cachePatterns = this.cachePatterns;
        if (cachePatterns == null || cachePatterns) {
            matcher = this.stringMatcherCache.get(pattern);
        }
        if (matcher == null) {
            matcher = new AntPathStringMatcher(pattern, this.caseSensitive);
            if (cachePatterns == null && this.stringMatcherCache.size() >= CACHE_TURNOFF_THRESHOLD) {
                deactivatePatternCache();
                return matcher;
            }
            if (cachePatterns == null || cachePatterns) {
                this.stringMatcherCache.put(pattern, matcher);
            }
        }
        return matcher;
    }

    /**
     * Given a pattern and a full path, determine the pattern-mapped part.
     * <p>
     * For example:
     * <ul>
     * <li>'{@code /docs/cvs/commit.html}' and '{@code /docs/cvs/commit.html} &rarr; ''</li>
     * <li>'{@code /docs/*}' and '{@code /docs/cvs/commit} &rarr; '{@code cvs/commit}'</li>
     * <li>'{@code /docs/cvs/*.html}' and '{@code /docs/cvs/commit.html} &rarr; '{@code commit.html}'</li>
     * <li>'{@code /docs/**}' and '{@code /docs/cvs/commit} &rarr; '{@code cvs/commit}'</li>
     * <li>'{@code /docs/**\/*.html}' and '{@code /docs/cvs/commit.html} &rarr; '{@code cvs/commit.html}'</li>
     * <li>'{@code /*.html}' and '{@code /docs/cvs/commit.html} &rarr; '{@code docs/cvs/commit.html}'</li>
     * <li>'{@code *.html}' and '{@code /docs/cvs/commit.html} &rarr; '{@code /docs/cvs/commit.html}'</li>
     * <li>'{@code *}' and '{@code /docs/cvs/commit.html} &rarr; '{@code /docs/cvs/commit.html}'</li>
     * </ul>
     * <p>
     * Assumes that {@link #match} returns {@code true} for '{@code pattern}' and '{@code path}', but does
     * <strong>not</strong> enforce this.
     *
     * @param pattern 表达式
     * @param path    路径
     * @return 表达式匹配到的部分
     */
    public String extractPathWithinPattern(final String pattern, final String path) {
        final String[] patternParts = tokenizePath(pattern);
        final String[] pathParts = tokenizePath(path);
        final StringBuilder builder = new StringBuilder();
        boolean pathStarted = false;

        for (int segment = 0; segment < patternParts.length; segment++) {
            final String patternPart = patternParts[segment];
            if (patternPart.indexOf(Symbol.C_STAR) > -1 || patternPart.indexOf(Symbol.C_QUESTION_MARK) > -1) {
                for (; segment < pathParts.length; segment++) {
                    if (pathStarted || (segment == 0 && !pattern.startsWith(this.pathSeparator))) {
                        builder.append(this.pathSeparator);
                    }
                    builder.append(pathParts[segment]);
                    pathStarted = true;
                }
            }
        }

        return builder.toString();
    }

    /**
     * 提取参数
     *
     * @param pattern 模式
     * @param path    路径
     * @return 参数
     */
    public Map<String, String> extractUriTemplateVariables(final String pattern, final String path) {
        final Map<String, String> variables = new LinkedHashMap<>();
        final boolean result = doMatch(pattern, path, true, variables);
        if (!result) {
            throw new IllegalStateException("Pattern \"" + pattern + "\" is not a match for \"" + path + "\"");
        }
        return variables;
    }

    /**
     * Combine two patterns into a new pattern. This implementation simply concatenates the two patterns, unless the
     * first pattern contains a file extension match (e.g., {@code *.html}). In that case, the second pattern will be
     * merged into the first. Otherwise, an {@code IllegalArgumentException} will be thrown.
     *
     * @param pattern1 the first pattern
     * @param pattern2 the second pattern
     * @return the combination of the two patterns
     * @throws IllegalArgumentException if the two patterns cannot be combined
     */
    public String combine(final String pattern1, final String pattern2) {
        if (StringKit.isEmpty(pattern1) && StringKit.isEmpty(pattern2)) {
            return Normal.EMPTY;
        }
        if (StringKit.isEmpty(pattern1)) {
            return pattern2;
        }
        if (StringKit.isEmpty(pattern2)) {
            return pattern1;
        }

        final boolean pattern1ContainsUriVar = (pattern1.indexOf('{') != -1);
        if (!pattern1.equals(pattern2) && !pattern1ContainsUriVar && match(pattern1, pattern2)) {
            // /* + /hotel -> /hotel ; "/*.*" + "/*.html" -> /*.html
            // However /user + /user -> /usr/user ; /{foo} + /bar -> /{foo}/bar
            return pattern2;
        }

        // /hotels/* + /booking -> /hotels/booking
        // /hotels/* + booking -> /hotels/booking
        if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnWildCard())) {
            return concat(pattern1.substring(0, pattern1.length() - 2), pattern2);
        }

        // /hotels/** + /booking -> /hotels/**/booking
        // /hotels/** + booking -> /hotels/**/booking
        if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnDoubleWildCard())) {
            return concat(pattern1, pattern2);
        }

        final int starDotPos1 = pattern1.indexOf("*.");
        if (pattern1ContainsUriVar || starDotPos1 == -1 || this.pathSeparator.equals(".")) {
            // simply concatenate the two patterns
            return concat(pattern1, pattern2);
        }

        final String ext1 = pattern1.substring(starDotPos1 + 1);
        final int dotPos2 = pattern2.indexOf('.');
        final String file2 = (dotPos2 == -1 ? pattern2 : pattern2.substring(0, dotPos2));
        final String ext2 = (dotPos2 == -1 ? "" : pattern2.substring(dotPos2));
        final boolean ext1All = (ext1.equals(".*") || ext1.isEmpty());
        final boolean ext2All = (ext2.equals(".*") || ext2.isEmpty());
        if (!ext1All && !ext2All) {
            throw new IllegalArgumentException("Cannot combine patterns: " + pattern1 + " vs " + pattern2);
        }
        final String ext = (ext1All ? ext2 : ext1);
        return file2 + ext;
    }

    private String concat(final String path1, final String path2) {
        final boolean path1EndsWithSeparator = path1.endsWith(this.pathSeparator);
        final boolean path2StartsWithSeparator = path2.startsWith(this.pathSeparator);

        if (path1EndsWithSeparator && path2StartsWithSeparator) {
            return path1 + path2.substring(1);
        } else if (path1EndsWithSeparator || path2StartsWithSeparator) {
            return path1 + path2;
        } else {
            return path1 + this.pathSeparator + path2;
        }
    }

    /**
     * Given a full path, returns a {@link Comparator} suitable for sorting patterns in order of explicitness.
     * <p>
     * This {@code Comparator} will {@linkplain List#sort(Comparator) sort} a list so that more specific patterns
     * (without URI templates or wild cards) come before generic patterns. So given a list with the following patterns,
     * the returned compare will sort this list so that the order will be as indicated.
     * <ol>
     * <li>{@code /hotels/new}</li>
     * <li>{@code /hotels/{hotel}}</li>
     * <li>{@code /hotels/*}</li>
     * </ol>
     * <p>
     * The full path given as parameter is used to test for exact matches. So when the given path is {@code /hotels/2},
     * the pattern {@code /hotels/2} will be sorted before {@code /hotels/1}.
     *
     * @param path the full path to use for comparison
     * @return a compare capable of sorting patterns in order of explicitness
     */
    public Comparator<String> getPatternComparator(final String path) {
        return new AntPatternComparator(path);
    }

    /**
     * Tests whether a string matches against a pattern via a {@link Pattern}.
     * <p>
     * The pattern may contain special characters: '*' means zero or more characters; '?' means one and only one
     * character; '{' and '}' indicate a URI template pattern. For example {@code /users/{user}}.
     */
    protected static class AntPathStringMatcher {

        private static final Pattern GLOB_PATTERN = Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?}|[^/{}]|\\\\[{}])+?)}");

        private static final String DEFAULT_VARIABLE_PATTERN = "((?s).*)";

        private final String rawPattern;

        private final boolean caseSensitive;

        private final boolean exactMatch;

        private final Pattern pattern;

        private final List<String> variableNames = new ArrayList<>();

        /**
         * Create a new {@code AntPathStringMatcher} that will match the supplied {@code pattern}
         *
         * @param pattern       the pattern to match against
         * @param caseSensitive 是否大小写不敏感
         */
        public AntPathStringMatcher(final String pattern, final boolean caseSensitive) {
            this.rawPattern = pattern;
            this.caseSensitive = caseSensitive;
            final StringBuilder patternBuilder = new StringBuilder();
            final Matcher matcher = GLOB_PATTERN.matcher(pattern);
            int end = 0;
            while (matcher.find()) {
                patternBuilder.append(quote(pattern, end, matcher.start()));
                final String match = matcher.group();
                if ("?".equals(match)) {
                    patternBuilder.append('.');
                } else if (Symbol.STAR.equals(match)) {
                    patternBuilder.append(".*");
                } else if (match.startsWith("{") && match.endsWith("}")) {
                    final int colonIdx = match.indexOf(Symbol.C_COLON);
                    if (colonIdx == -1) {
                        patternBuilder.append(DEFAULT_VARIABLE_PATTERN);
                        this.variableNames.add(matcher.group(1));
                    } else {
                        final String variablePattern = match.substring(colonIdx + 1, match.length() - 1);
                        patternBuilder.append(Symbol.C_PARENTHESE_LEFT);
                        patternBuilder.append(variablePattern);
                        patternBuilder.append(Symbol.C_PARENTHESE_RIGHT);
                        final String variableName = match.substring(1, colonIdx);
                        this.variableNames.add(variableName);
                    }
                }
                end = matcher.end();
            }
            // No glob pattern was found, this is an exact String match
            if (end == 0) {
                this.exactMatch = true;
                this.pattern = null;
            } else {
                this.exactMatch = false;
                patternBuilder.append(quote(pattern, end, pattern.length()));
                this.pattern = (this.caseSensitive ? Pattern.compile(patternBuilder.toString())
                        : Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE));
            }
        }

        private String quote(final String s, final int start, final int end) {
            if (start == end) {
                return "";
            }
            return Pattern.quote(s.substring(start, end));
        }

        /**
         * Main entry point.
         *
         * @param text                 text
         * @param uriTemplateVariables uri template vars
         * @return {@code true} if the string matches against the pattern, or {@code false} otherwise.
         */
        public boolean matchStrings(final String text, final Map<String, String> uriTemplateVariables) {
            if (this.exactMatch) {
                return this.caseSensitive ? this.rawPattern.equals(text) : this.rawPattern.equalsIgnoreCase(text);
            } else if (this.pattern != null) {
                final Matcher matcher = this.pattern.matcher(text);
                if (matcher.matches()) {
                    if (uriTemplateVariables != null) {
                        if (this.variableNames.size() != matcher.groupCount()) {
                            throw new IllegalArgumentException("The number of capturing groups in the pattern segment "
                                    + this.pattern + " does not match the number of URI template variables it defines, "
                                    + "which can occur if capturing groups are used in a URI template regex. "
                                    + "Use non-capturing groups instead.");
                        }
                        for (int i = 1; i <= matcher.groupCount(); i++) {
                            final String name = this.variableNames.get(i - 1);
                            if (name.startsWith(Symbol.STAR)) {
                                throw new IllegalArgumentException("Capturing patterns (" + name + ") are not "
                                        + "supported by the AntPathMatcher. Use the PathPatternParser instead.");
                            }
                            final String value = matcher.group(i);
                            uriTemplateVariables.put(name, value);
                        }
                    }
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * The default {@link Comparator} implementation returned by {@link #getPatternComparator(String)}.
     * <p>
     * In order, the most "generic" pattern is determined by the following:
     * <ul>
     * <li>if it's null or a capture all pattern (i.e. it is equal to "/**")</li>
     * <li>if the other pattern is an actual match</li>
     * <li>if it's a catch-all pattern (i.e. it ends with "**"</li>
     * <li>if it's got more "*" than the other pattern</li>
     * <li>if it's got more "{foo}" than the other pattern</li>
     * <li>if it's shorter than the other pattern</li>
     * </ul>
     */
    protected static class AntPatternComparator implements Comparator<String> {

        private final String path;

        public AntPatternComparator(final String path) {
            this.path = path;
        }

        /**
         * Compare two patterns to determine which should match first, i.e. which is the most specific regarding the
         * current path.
         *
         * @param pattern1 表达式1
         * @param pattern2 表达式2
         * @return a negative integer, zero, or a positive integer as pattern1 is more specific, equally specific, or
         *         less specific than pattern2.
         */
        @Override
        public int compare(final String pattern1, final String pattern2) {
            final PatternInfo info1 = new PatternInfo(pattern1);
            final PatternInfo info2 = new PatternInfo(pattern2);

            if (info1.isLeastSpecific() && info2.isLeastSpecific()) {
                return 0;
            } else if (info1.isLeastSpecific()) {
                return 1;
            } else if (info2.isLeastSpecific()) {
                return -1;
            }

            final boolean pattern1EqualsPath = pattern1.equals(this.path);
            final boolean pattern2EqualsPath = pattern2.equals(this.path);
            if (pattern1EqualsPath && pattern2EqualsPath) {
                return 0;
            } else if (pattern1EqualsPath) {
                return -1;
            } else if (pattern2EqualsPath) {
                return 1;
            }

            if (info1.isPrefixPattern() && info2.isPrefixPattern()) {
                return info2.getLength() - info1.getLength();
            } else if (info1.isPrefixPattern() && info2.getDoubleWildcards() == 0) {
                return 1;
            } else if (info2.isPrefixPattern() && info1.getDoubleWildcards() == 0) {
                return -1;
            }

            if (info1.getTotalCount() != info2.getTotalCount()) {
                return info1.getTotalCount() - info2.getTotalCount();
            }

            if (info1.getLength() != info2.getLength()) {
                return info2.getLength() - info1.getLength();
            }

            if (info1.getSingleWildcards() < info2.getSingleWildcards()) {
                return -1;
            } else if (info2.getSingleWildcards() < info1.getSingleWildcards()) {
                return 1;
            }

            if (info1.getUriVars() < info2.getUriVars()) {
                return -1;
            } else if (info2.getUriVars() < info1.getUriVars()) {
                return 1;
            }

            return 0;
        }

        /**
         * Value class that holds information about the pattern, e.g. number of occurrences of "*", "**", and "{"
         * pattern elements.
         */
        private static class PatternInfo {

            private final String pattern;
            private int uriVars;
            private int singleWildcards;
            private int doubleWildcards;
            private boolean catchAllPattern;
            private boolean prefixPattern;
            private Integer length;

            public PatternInfo(final String pattern) {
                this.pattern = pattern;
                if (this.pattern != null) {
                    initCounters();
                    this.catchAllPattern = this.pattern.equals("/**");
                    this.prefixPattern = !this.catchAllPattern && this.pattern.endsWith("/**");
                }
                if (this.uriVars == 0) {
                    this.length = (this.pattern != null ? this.pattern.length() : 0);
                }
            }

            protected void initCounters() {
                int pos = 0;
                if (this.pattern != null) {
                    while (pos < this.pattern.length()) {
                        if (this.pattern.charAt(pos) == '{') {
                            this.uriVars++;
                            pos++;
                        } else if (this.pattern.charAt(pos) == Symbol.C_STAR) {
                            if (pos + 1 < this.pattern.length() && this.pattern.charAt(pos + 1) == Symbol.C_STAR) {
                                this.doubleWildcards++;
                                pos += 2;
                            } else if (pos > 0 && !this.pattern.substring(pos - 1).equals(".*")) {
                                this.singleWildcards++;
                                pos++;
                            } else {
                                pos++;
                            }
                        } else {
                            pos++;
                        }
                    }
                }
            }

            public int getUriVars() {
                return this.uriVars;
            }

            public int getSingleWildcards() {
                return this.singleWildcards;
            }

            public int getDoubleWildcards() {
                return this.doubleWildcards;
            }

            public boolean isLeastSpecific() {
                return (this.pattern == null || this.catchAllPattern);
            }

            public boolean isPrefixPattern() {
                return this.prefixPattern;
            }

            public int getTotalCount() {
                return this.uriVars + this.singleWildcards + (2 * this.doubleWildcards);
            }

            /**
             * Returns the length of the given pattern, where template variables are considered to be 1 long.
             *
             * @return 长度
             */
            public int getLength() {
                if (this.length == null) {
                    this.length = (this.pattern != null
                            ? VARIABLE_PATTERN.matcher(this.pattern).replaceAll(Symbol.HASH).length()
                            : 0);
                }
                return this.length;
            }
        }
    }

    /**
     * A simple cache for patterns that depend on the configured path separator.
     */
    private static class PathSeparatorPatternCache {

        private final String endsOnWildCard;

        private final String endsOnDoubleWildCard;

        public PathSeparatorPatternCache(final String pathSeparator) {
            this.endsOnWildCard = pathSeparator + Symbol.STAR;
            this.endsOnDoubleWildCard = pathSeparator + Symbol.STAR + Symbol.STAR;
        }

        public String getEndsOnWildCard() {
            return this.endsOnWildCard;
        }

        public String getEndsOnDoubleWildCard() {
            return this.endsOnDoubleWildCard;
        }
    }

}
