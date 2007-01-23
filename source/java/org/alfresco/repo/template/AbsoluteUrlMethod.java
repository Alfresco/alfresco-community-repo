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

import java.util.List;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author David Caruana
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Render absolute url for the specified url (only if the url isn't already absolute).
 * <p>
 * Usage: absurl(String url)
 */
public final class AbsoluteUrlMethod implements TemplateMethodModelEx
{
    private String basePath;
    
    /**
     * Construct
     * 
     * @param basePath  base path used to construct absolute url
     */
    public AbsoluteUrlMethod(String basePath)
    {
        this.basePath = basePath;
    }
    
    
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
                result = ((TemplateScalarModel)arg0).getAsString();
                if (result.startsWith("/"))
                {
                    result = basePath + result;
                }
            }
        }
        
        return result;
    }
}
