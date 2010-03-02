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
import java.util.Map;

import org.alfresco.cmis.CMISFilterNotValidException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.web.scripts.RepositoryImageResolver;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateValueConverter;
import org.springframework.extensions.webscripts.WebScriptException;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Custom FreeMarker Template language method.
 * <p>
 * Gets the renditions of a TemplateNode
 * <p>
 * Usage: cmisrenditions(TemplateNode node)
 *        cmisrenditions(TemplateNode node, String renditionFilter)
 * 
 * @author dward
 */
public class CMISRenditionsMethod implements TemplateMethodModelEx
{
    private CMISServices cmisService;
    private TemplateImageResolver imageResolver;
    private TemplateValueConverter templateValueConverter;

    /**
     * Construct
     */
    public CMISRenditionsMethod(CMISServices cmisService, RepositoryImageResolver imageResolver,
            TemplateValueConverter templateValueConverter)
    {
        this.cmisService = cmisService;
        this.imageResolver = imageResolver.getImageResolver();
        this.templateValueConverter = templateValueConverter;
    }

    @SuppressWarnings("unchecked")
    public Object exec(List args) throws TemplateModelException
    {
        NodeRef nodeRef = null;
        String renditionFilter = null;
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
            // extract rendition filter, if specified
            arg = args.get(i++);
            if (arg instanceof TemplateScalarModel)
            {
                renditionFilter = ((TemplateScalarModel) arg).getAsString();
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore optional arguments
        }

        // query renditions
        if (nodeRef != null)
        {
            Map<String, Object> renditions;
            try
            {
                renditions = cmisService.getRenditions(nodeRef, renditionFilter);
            }
            catch (CMISFilterNotValidException e)
            {
                throw new WebScriptException(e.getStatusCode(), e.getMessage(), e);
            }
            return templateValueConverter.convertValue(renditions, imageResolver);
        }

        return null;
    }

}
