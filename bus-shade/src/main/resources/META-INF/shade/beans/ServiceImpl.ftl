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
package ${serviceImplUrl};

import ${entityUrl}.${entityName};
import ${mapperUrl}.${entityName}Mapper;
import ${serviceUrl}.${entityName}Service;
import impl.service.base.org.miaixz.bus.BaseServiceImpl;
import org.springframework.stereotype.Service;

/**
* ${entityComment}服务实现层
*
* @version: ${version}
* @author: ${author}
* @since Java 17+
*/
<#if isDubbo=="true" >
    @org.apache.dubbo.config.annotation.DubboService
</#if>
@Service
public class ${entityName}ServiceImpl extends BaseServiceImpl
<${entityName}, ${entityName}Mapper>
implements ${entityName}Service  {

}
