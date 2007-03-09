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
package org.alfresco.repo.template;

import java.util.List;

import org.alfresco.service.cmr.repository.TemplateNode;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author Kevin Roast
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Method returns whether a TemplateNode has a particular aspect applied to it. The aspect
 * name can be either the fully qualified QName or the short prefixed name string.
 * <p>
 * Usage: hasAspect(TemplateNode node, String aspect) - 1 on true, 0 on false
 */
public class HasAspectMethod extends BaseTemplateExtensionImplementation implements TemplateMethodModelEx
{
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        int result = 0;
        
        if (args.size() == 2)
        {
            // arg 0 must be a wrapped TemplateNode object
            BeanModel arg0 = (BeanModel)args.get(0);
            
            // arg 1 can be either wrapped QName object or a String 
            String arg1String = null;
            Object arg1 = args.get(1);
            if (arg1 instanceof BeanModel)
            {
                arg1String = ((BeanModel)arg1).getWrappedObject().toString();
            }
            else if (arg1 instanceof TemplateScalarModel)
            {
                arg1String = ((TemplateScalarModel)arg1).getAsString();
            }
            if (arg0.getWrappedObject() instanceof TemplateNode)
            {
                // test to see if this node has the aspect
                if ( ((TemplateNode)arg0.getWrappedObject()).hasAspect(arg1String) )
                {
                    result = 1;
                }
            }
        }
        
        return Integer.valueOf(result);
    }
}
