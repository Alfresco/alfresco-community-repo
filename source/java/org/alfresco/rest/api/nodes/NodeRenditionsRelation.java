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

package org.alfresco.rest.api.nodes;

import org.alfresco.rest.api.Renditions;
import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Status;

import java.util.Collections;
import java.util.List;

/**
 * Node renditions.
 *
 * @author Jamal Kaabi-Mofrad
 */
@RelationshipResource(name = "renditions", entityResource = NodesEntityResource.class, title = "Node renditions")
public class NodeRenditionsRelation implements RelationshipResourceAction.Read<Rendition>,
            RelationshipResourceAction.ReadById<Rendition>,
            RelationshipResourceAction.Create<Rendition>,
            RelationshipResourceBinaryAction.Read,
            InitializingBean
{

    private Renditions renditions;

    public void setRenditions(Renditions renditions)
    {
        this.renditions = renditions;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "renditions", renditions);
    }

    @Override
    public CollectionWithPagingInfo<Rendition> readAll(String nodeId, Parameters parameters)
    {
        return renditions.getRenditions(nodeId, parameters);
    }

    @Override
    public Rendition readById(String nodeId, String renditionId, Parameters parameters)
    {
        return renditions.getRendition(nodeId, renditionId, parameters);
    }

    @WebApiDescription(title = "Create rendition", successStatus = Status.STATUS_ACCEPTED)
    @Override
    public List<Rendition> create(String nodeId, List<Rendition> entity, Parameters parameters)
    {
        for (Rendition rendition : entity)
        {
            renditions.createRendition(nodeId, rendition, parameters);
        }
        return Collections.emptyList();
    }

    @WebApiDescription(title = "Download rendition", description = "Download rendition")
    @BinaryProperties({ "content" })
    @Override
    public BinaryResource readProperty(String nodeId, String renditionId, Parameters parameters)
    {
        return renditions.getContent(nodeId, renditionId, parameters);
    }
}
