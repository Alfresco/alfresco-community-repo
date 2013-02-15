/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.template;

import java.util.List;

import org.springframework.extensions.surf.util.URLDecoder;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author Kevin Roast
 * @since 4.2
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Decodes a URL encoded string. The empty string is returned for invalid input values.
 * <p>
 * Usage: urldecode(String s)
 */
public class URLDecodeMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        String result = "";
        
        if (args.size() != 0)
        {
            String s = "";
            Object arg0 = args.get(0);
            if (arg0 instanceof TemplateScalarModel)
            {
                s = ((TemplateScalarModel)arg0).getAsString();
            }
            
            if (s != null)
            {
                result = URLDecoder.decode(s);
            }
        }
        
        return result;
    }
}
