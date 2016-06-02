/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.quicksharelinks;

import org.alfresco.rest.api.QuickShareLinks;
import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiNoAuth;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * Enable rendition(s) to be download via Shared Link
 *
 * @author janv
 */
@RelationshipResource(name = "renditions", entityResource = QuickShareLinkEntityResource.class, title = "Node renditions via shared link")
public class QuickShareLinkRenditionsRelation implements
            RelationshipResourceAction.Read<Rendition>,
            RelationshipResourceBinaryAction.Read,
            InitializingBean
{
    private QuickShareLinks quickShareLinks;

    public void setQuickShareLinks(QuickShareLinks quickShareLinks)
    {
        this.quickShareLinks = quickShareLinks;
    }

    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("quickShareLinks", this.quickShareLinks);
    }

    @WebApiDescription(title = "Download shared link rendition", description = "Download rendition for shared link")
    @WebApiNoAuth
    @BinaryProperties({"content"})
    @Override
    public BinaryResource readProperty(String sharedId, String renditionId, Parameters parameters)
    {
        return quickShareLinks.readProperty(sharedId, renditionId, parameters);
    }

    @WebApiDescription(title = "List renditions", description = "List available (created) renditions")
    @WebApiNoAuth
    @Override
    public CollectionWithPagingInfo<Rendition> readAll(String sharedId, Parameters parameters)
    {
        return quickShareLinks.getRenditions(sharedId);
    }
}

