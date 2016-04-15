/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.template;

import java.util.List;


import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * @author Kevin Roast
 * 
 * Custom FreeMarker Template language method.
 * <p>
 * Method returns whether the current user is granted a particular permission a the TemplateNode.
 * <p>
 * Usage: hasPermission(TemplateNode node, String permission) - 1 on true, 0 on false
 */
public class HasPermissionMethod extends BaseTemplateProcessorExtension implements TemplateMethodModelEx
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
            
            // arg 1 must be a String permission name
            String permission;
            Object arg1 = args.get(1);
            if (arg1 instanceof TemplateScalarModel)
            {
                permission = ((TemplateScalarModel)arg1).getAsString();
                
                if (arg0.getWrappedObject() instanceof TemplateNode)
                {
                    // test to see if this node has the permission
                    if ( ((TemplateNode)arg0.getWrappedObject()).hasPermission(permission) )
                    {
                        result = 1;
                    }
                }
            }
        }
        
        return Integer.valueOf(result);
    }
}
