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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.rest;

import java.util.List;

import org.alfresco.cmis.property.CMISPropertyService;
import org.alfresco.repo.template.TemplateNode;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Custom FreeMarker Template language method.
 * <p>
 * Retrieve the CMIS property value for an Alfresco node
 * <p>
 * Usage: cmisproperty(TemplateNode node, String propertyName)
 *        
 * @author davidc
 */
public final class CMISPropertyValueMethod implements TemplateMethodModelEx
{
    private CMISPropertyService propertyService;
    
    /**
     * Construct
     */
    public CMISPropertyValueMethod(CMISPropertyService propertyService)
    {
        this.propertyService = propertyService;
    }
    
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public Object exec(List args) throws TemplateModelException
    {
        Object result = null;
        
        if (args.size() == 2)
        {
            Object arg0 = args.get(0);
            if (arg0 instanceof BeanModel)
            {
                // extract node
                TemplateNode node = null;
                Object wrapped = ((BeanModel)arg0).getWrappedObject();
                if (wrapped != null && wrapped instanceof TemplateNode)
                {
                    node = (TemplateNode)wrapped;
                }
                
                // extract property name
                String propertyName = null;
                Object arg1 = args.get(1);
                if (arg1 instanceof TemplateScalarModel)
                {
                     propertyName = ((TemplateScalarModel)arg1).getAsString();
                }

                // retrieve property value
                result = propertyService.getProperty(node.getNodeRef(), propertyName);
            }
        }
        
        return result;
    }
}
