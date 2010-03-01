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

import org.alfresco.cmis.CMISRelationshipDirectionEnum;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.web.scripts.RepositoryImageResolver;
import org.alfresco.service.cmr.repository.AssociationRef;
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
 * Gets the associations of a TemplateNode
 * <p>
 * Usage: cmisassocs(TemplateNode node, String direction)
 * 
 * @author dward
 */
public class CMISAssocsMethod implements TemplateMethodModelEx
{
    private CMISServices cmisService;
    private TemplateImageResolver imageResolver;
    private TemplateValueConverter templateValueConverter;

    /**
     * Construct
     */
    public CMISAssocsMethod(CMISServices cmisService, RepositoryImageResolver imageResolver,
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
        CMISRelationshipDirectionEnum direction = CMISRelationshipDirectionEnum.SOURCE;
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
            // extract direction, if specified
            arg = args.get(i++);
            if (arg instanceof TemplateScalarModel)
            {
                direction = CMISRelationshipDirectionEnum.FACTORY.toEnum(((TemplateScalarModel)arg).getAsString());
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore optional arguments
        }

        // query relationships
        if (nodeRef != null)
        {
            AssociationRef[] assocs;
            try
            {
                assocs = cmisService.getRelationships(nodeRef, null, true, direction);
            }
            catch (CMISServiceException e)
            {
                throw new WebScriptException(e.getStatusCode(), e.getMessage(), e);
            }
            return templateValueConverter.convertValue(assocs, imageResolver);
        }

        return null;
    }

}
