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

import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.repo.template.TemplateAssociation;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.namespace.QName;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Custom FreeMarker Template language method.
 * <p>
 * Retrieve the CMIS Type Definition for an Alfresco node
 * <p>
 * Usage: cmistype(TemplateNode node)
 *        cmistype(QName nodeType)
 *        
 * @author davidc
 */
public class CMISTypeDefinitionMethod implements TemplateMethodModelEx
{
    private static CMISScope[] EMPTY_SCOPES = new CMISScope[] {};
    private CMISDictionaryService dictionaryService;
    
    
    /**
     * Construct
     */
    public CMISTypeDefinitionMethod(CMISDictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public Object exec(List args) throws TemplateModelException
    {
        CMISTypeDefinition result = null;
        
        if (args.size() == 1)
        {
            Object arg0 = args.get(0);
            if (arg0 instanceof BeanModel)
            {
                // extract node type qname
                CMISScope[] matchingScopes = EMPTY_SCOPES;
                QName nodeType = null;
                Object wrapped = ((BeanModel)arg0).getWrappedObject();
                if (wrapped != null)
                {
                    if (wrapped instanceof TemplateNode)
                    {
                        nodeType = ((TemplateNode)wrapped).getType();
                    }
                    else if (wrapped instanceof TemplateAssociation)
                    {
                        nodeType = ((TemplateAssociation)wrapped).getTypeQName();
                        matchingScopes = new CMISScope[] { CMISScope.RELATIONSHIP };
                    }
                    else if (wrapped instanceof QName)
                    {
                        nodeType = (QName)wrapped;
                    }
                    else if (wrapped instanceof CMISTypeDefinition)
                    {
                        result = ((CMISTypeDefinition)wrapped).getBaseType();
                    }
                }
                
                // convert to CMIS type
                if (nodeType != null)
                {
                    result = dictionaryService.findTypeForClass(nodeType, matchingScopes);
                }
            }
        }
        
        return result;
    }
}
