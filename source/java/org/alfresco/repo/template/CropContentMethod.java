/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.template;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateNode.TemplateContentData;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

/**
 * @author Kevin Roast
 * 
 * FreeMarker custom method to return the first N characters of a content stream.
 * <p>
 * Usage: String cropContent(TemplateContentData content, int length)
 */
public final class CropContentMethod implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        String result = null;
        
        if (args.size() == 2)
        {
            Object arg0 = args.get(0);
            Object arg1 = args.get(1);
            
            if (arg0 instanceof BeanModel && arg1 instanceof TemplateNumberModel)
            {
                Object wrapped = ((BeanModel)arg0).getWrappedObject();
                if (wrapped instanceof TemplateContentData)
                {
                   int bytes = ((TemplateNumberModel)arg1).getAsNumber().intValue();
                   
                   result = ((TemplateContentData)wrapped).getContentAsText(bytes);
                }
            }
        }
        
        return result != null ? result : "";
    }
}
