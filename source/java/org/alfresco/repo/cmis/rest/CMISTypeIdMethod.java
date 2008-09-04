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

import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.dictionary.CMISTypeId;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.namespace.QName;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Custom FreeMarker Template language method.
 * <p>
 * Retrieve the CMIS Type Id for an Alfresco node
 * <p>
 * Usage: cmisTypeId(ScriptNode node)
 *        cmisTypeId(QName nodeType)
 *        
 * @author davidc
 */
public final class CMISTypeIdMethod implements TemplateMethodModelEx
{
    private CMISMapping mappingService;
    
    /**
     * Construct
     */
    public CMISTypeIdMethod(CMISMapping mappingService)
    {
        this.mappingService = mappingService;
    }
    
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException
    {
        CMISTypeId result = null;
        
        if (args.size() == 1)
        {
            Object arg0 = args.get(0);
            if (arg0 instanceof BeanModel)
            {
                // extract node type qname
                QName nodeType = null;
                Object wrapped = ((BeanModel)arg0).getWrappedObject();
                if (wrapped != null)
                {
                    if (wrapped instanceof TemplateNode)
                    {
                        nodeType = ((TemplateNode)wrapped).getType();
                    }
                    else if (wrapped instanceof QName)
                    {
                        nodeType = (QName)wrapped;
                    }
                }
                
                // convert to CMIS type id
                if (nodeType != null)
                {
                    result = mappingService.getCmisTypeId(nodeType);
                }
            }
        }
        
        return result;
    }
}
