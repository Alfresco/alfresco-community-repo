/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
