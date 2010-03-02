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

import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypesFilterEnum;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.web.scripts.RepositoryImageResolver;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.springframework.extensions.webscripts.WebScriptException;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Custom FreeMarker Template language method.
 * <p>
 * Lists the (CMIS) children of a TemplateNode
 * <p>
 * Usage: cmischildren(TemplateNode node)
 *        cmischildren(TemplateNode node, String typesFilter)
 *        
 * @author davidc
 */
public class CMISChildrenMethod implements TemplateMethodModelEx
{
    private CMISServices cmisService;
    private ServiceRegistry serviceRegistry;
    private TemplateImageResolver imageResolver;
    
    /**
     * Construct
     */
    public CMISChildrenMethod(CMISServices cmisService, ServiceRegistry serviceRegistry, RepositoryImageResolver imageResolver)
    {
        this.cmisService = cmisService;
        this.serviceRegistry = serviceRegistry;
        this.imageResolver = imageResolver.getImageResolver();
    }
    
    @SuppressWarnings("unchecked")
    public Object exec(List args) throws TemplateModelException
    {
        TemplateNode[] children = null;
        
        if (args.size() > 0)
        {
            Object arg0 = args.get(0);
            if (arg0 instanceof BeanModel)
            {
                // extract node ref
                Object wrapped = ((BeanModel)arg0).getWrappedObject();
                if (wrapped != null)
                {
                    if (wrapped instanceof TemplateNode)
                    {
                        NodeRef nodeRef = ((TemplateNode)wrapped).getNodeRef();
                        CMISTypesFilterEnum typesFilter = CMISTypesFilterEnum.ANY;
                        if (args.size() > 1)
                        {
                            // extract types filter, if specified
                            Object arg1 = args.get(1);
                            if (arg1 instanceof TemplateScalarModel)
                            {
                                String typesFilterStr = ((TemplateScalarModel)arg1).getAsString();
                                if (typesFilterStr != null && typesFilterStr.length() > 0)
                                {
                                    typesFilter = (CMISTypesFilterEnum)CMISTypesFilterEnum.FACTORY.toEnum(typesFilterStr);
                                }
                            }
                        }
                        
                        // query children
                        NodeRef[] childNodeRefs;
                        try
                        {
                            childNodeRefs = cmisService.getChildren(nodeRef, typesFilter, null);
                        }
                        catch (CMISInvalidArgumentException e)
                        {
                            throw new WebScriptException(e.getStatusCode(), e.getMessage(), e);
                        }
                        children = new TemplateNode[childNodeRefs.length];
                        for (int i = 0; i < childNodeRefs.length; i++)
                        {
                            children[i] = new TemplateNode(childNodeRefs[i], serviceRegistry, imageResolver);
                        }
                    }
                }
            }
        }
        
        return children;
    }

}
