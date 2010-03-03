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

import java.util.Collections;
import java.util.List;

import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.cmr.repository.NodeRef;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Custom FreeMarker Template language method.
 * <p>
 * Gets the type definitions of a TemplateNode's aspects
 * <p>
 * Usage: cmisaspects(TemplateNode node)
 * 
 * @author dward
 */
public class CMISAspectsMethod implements TemplateMethodModelEx
{
    private CMISServices cmisService;

    /**
     * Construct
     */
    public CMISAspectsMethod(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }

    @SuppressWarnings("unchecked")
    public Object exec(List args) throws TemplateModelException
    {
        NodeRef nodeRef = null;
        try
        {
            int i = 0;
            // extract node ref
            Object arg = args.get(i++);
            if (arg instanceof BeanModel)
            {
                Object wrapped = ((BeanModel) arg).getWrappedObject();
                if (wrapped != null)
                {
                    if (wrapped instanceof TemplateNode)
                    {
                        nodeRef = ((TemplateNode) wrapped).getNodeRef();
                    }
                }
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore optional arguments
        }

        // query aspects
        if (nodeRef != null)
        {
            return cmisService.getAspects(nodeRef);
        }

        return Collections.<CMISTypeDefinition> emptySet();
    }
}
