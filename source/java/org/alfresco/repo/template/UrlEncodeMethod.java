/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.template;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author David Caruana
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Encode URL for HTML.
 * <p>
 * Usage: urlencode(String url)
 */
public final class UrlEncodeMethod implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        String result = "";
        
        if (args.size() == 1)
        {
            Object arg0 = args.get(0);
            if (arg0 instanceof TemplateScalarModel)
            {
                try
                {
                    // TODO: Support other encodings, although
                    //       http://java.sun.com/j2se/1.4.2/docs/api/java/net/URLEncoder.html
                    //       recommends UTF-8
                    result = URLEncoder.encode(((TemplateScalarModel)arg0).getAsString(), "UTF-8");
                }
                catch (UnsupportedEncodingException e)
                {
                    // fallback to original url
                    result = ((TemplateScalarModel)arg0).getAsString();
                }
            }
        }
        
        return result;
    }
}
