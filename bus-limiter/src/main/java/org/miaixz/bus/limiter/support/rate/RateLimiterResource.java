/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.limiter.support.rate;

import org.miaixz.bus.limiter.annotation.LimiterParameter;
import org.miaixz.bus.limiter.metadata.LimitedResourceMetadata;
import org.miaixz.bus.limiter.resource.AbstractLimitedResource;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RateLimiterResource extends AbstractLimitedResource {

    @LimiterParameter
    private double rate;

    @LimiterParameter
    private long capacity;

    public RateLimiterResource(String key, Collection<String> argumentInjectors, String fallback, String errorHandler, String limiter, double rate, long capacity) {
        super(key, argumentInjectors, fallback, errorHandler, limiter);
        this.rate = rate;
        this.capacity = capacity;
    }

    @Override
    public LimitedResourceMetadata createMetadata(BeanFactory beanFactory, Class targetClass, Method targetMethod) {
        return new RateLimiterResourceMetadata(this, targetClass, targetMethod, beanFactory);
    }

}