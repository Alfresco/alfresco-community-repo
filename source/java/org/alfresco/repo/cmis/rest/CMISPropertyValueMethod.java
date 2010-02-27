/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.rest;

import java.util.List;

import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.repo.template.TemplateAssociation;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.web.scripts.RepositoryImageResolver;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateValueConverter;
import org.springframework.extensions.webscripts.WebScriptException;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Custom FreeMarker Template language method.
 * <p>
 * Retrieve the CMIS property value for an Alfresco node and optionally dereferences it as an objectId.
 * <p>
 * Usage: cmisproperty(TemplateNode node, String propertyName)
 *        cmisproperty(TemplateNode node, String propertyName, Boolean asObject)
 * @author davidc
 * @author dward
 */
public class CMISPropertyValueMethod implements TemplateMethodModelEx
{
    /**
     * NULL value marker
     */
    public static class NULL
    {
    };

    public static TemplateModel IS_NULL = new BeanModel(new NULL(), BeansWrapper.getDefaultInstance());

    private CMISServices cmisService;
    private TemplateImageResolver imageResolver;
    private TemplateValueConverter templateValueConverter;

    /**
     * Construct
     */
    public CMISPropertyValueMethod(CMISServices cmisService, RepositoryImageResolver imageResolver,
            TemplateValueConverter templateValueConverter)
    {
        this.cmisService = cmisService;
        this.imageResolver = imageResolver.getImageResolver();
        this.templateValueConverter = templateValueConverter;
    }

    @SuppressWarnings("unchecked")
    public Object exec(List args) throws TemplateModelException
    {
        Object wrapped = null;
        String propertyName = null;
        boolean asObject = false;
        try
        {
            int i = 0;

            // extract object
            Object arg = args.get(i++);
            if (arg instanceof BeanModel)
            {
                wrapped = ((BeanModel) arg).getWrappedObject();
            }

            // extract property name
            arg = args.get(i++);
            if (arg instanceof TemplateScalarModel)
            {
                propertyName = ((TemplateScalarModel) arg).getAsString();
            }
            
            // extract asObject flag
            arg = args.get(i++);
            if (arg instanceof TemplateBooleanModel)
            {
                asObject = ((TemplateBooleanModel) arg).getAsBoolean();
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore optional arguments
        }

        try
        {
            Object result = null;
            if (wrapped != null && wrapped instanceof TemplateNode)
            {
                // retrieve property value from node
                result = cmisService.getProperty(((TemplateNode) wrapped).getNodeRef(), propertyName);
            }
            else if (wrapped != null && wrapped instanceof TemplateAssociation)
            {
                // retrieve property value from association
                result = cmisService.getProperty(((TemplateAssociation) wrapped).getAssociationRef(), propertyName);
            }
            if (asObject && result instanceof String)
            {
                // convert result to an object if required
                result = cmisService.getReadableObject((String) result, Object.class);
            }
            return result == null ? IS_NULL : templateValueConverter.convertValue(result, imageResolver);
        }
        catch (CMISServiceException e)
        {
            throw new WebScriptException(e.getStatusCode(), e.getMessage(), e);
        }
        
    }

}
