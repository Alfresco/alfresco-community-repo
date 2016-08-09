/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.nodes;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceBinaryAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node Versions - version history
 * 
 * @author janv
 */
@RelationshipResource(name = "versions", entityResource = NodesEntityResource.class, title = "Node Versions")
public class NodeVersionsRelation implements
        RelationshipResourceAction.Read<Node>,
        RelationshipResourceAction.ReadById<Node>,
        RelationshipResourceBinaryAction.Read,
        InitializingBean
{
    protected ServiceRegistry sr;
    protected Nodes nodes;
    protected VersionService versionService;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setServiceRegistry(ServiceRegistry sr)
    {
        this.sr = sr;
    }

    @Override
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "serviceRegistry", sr);
        ParameterCheck.mandatory("nodes", this.nodes);

        this.versionService = sr.getVersionService();
    }
    /**
     * List version history
     *
     * @param nodeId String id of (live) node
     */
    @Override
    @WebApiDescription(title = "Return version history as a paged list of version node infos")
    public CollectionWithPagingInfo<Node> readAll(String nodeId, Parameters parameters)
    {
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId, null);

        VersionHistory vh = versionService.getVersionHistory(nodeRef);

        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);
        List<String> includeParam = parameters.getInclude();

        // TODO fixme - add paging etc
        List<Node> collection = null;
        if (vh != null)
        {
            collection = new ArrayList<>(vh.getAllVersions().size());
            for (Version v : vh.getAllVersions())
            {
                Node node = nodes.getFolderOrDocument(v.getFrozenStateNodeRef(), null, null, includeParam, mapUserInfo);
                mapVersionInfo(v, node);
                collection.add(node);
            }
        }

        Paging paging = parameters.getPaging();
        return CollectionWithPagingInfo.asPaged(paging, collection, false, (collection != null ? collection.size() : 0));
    }

    private void mapVersionInfo(Version v, Node aNode)
    {
        aNode.setNodeRef(new NodeRef("", "", v.getVersionLabel()));
        
        aNode.setVersionComment(v.getDescription());

        //Don't show parentId, createdAt, createdByUser
        aNode.setParentId(null);
        aNode.setCreated(null);
        aNode.setCreatedByUser(null);
    }

    @Override
    @WebApiDescription(title="Get version node info", description = "Return metadata for a specific version node")
    public Node readById(String nodeId, String versionId, Parameters parameters)
    {
        Version v = findVersion(nodeId, versionId);

        if (v != null)
        {
            List<String> includeParam = parameters.getInclude();
            Node node = nodes.getFolderOrDocument(v.getFrozenStateNodeRef(), null, null, includeParam, null);
            mapVersionInfo(v, node);
            return node;
        }

        throw new EntityNotFoundException(nodeId+"-"+versionId);
    }

    @WebApiDescription(title = "Download version content", description = "Download version content")
    @BinaryProperties({ "content" })
    @Override
    public BinaryResource readProperty(String nodeId, String versionId, Parameters parameters)
    {
        Version v = findVersion(nodeId, versionId);

        if (v != null)
        {
            NodeRef versionNodeRef = v.getFrozenStateNodeRef();
            return nodes.getContent(versionNodeRef, parameters, true); // TODO should we record version downloads ?
        }

        throw new EntityNotFoundException(nodeId+"-"+versionId);
    }

    private Version findVersion(String nodeId, String versionLabelId)
     {
        // note: sub-optimal
        NodeRef nodeRef = nodes.validateOrLookupNode(nodeId, null);
        VersionHistory vh = versionService.getVersionHistory(nodeRef);
        if (vh != null)
        {
            for (Version v : vh.getAllVersions())
            {
                if (v.getVersionLabel().equals(versionLabelId))
                {
                    return v;
                }
            }
        }
        return null;
    }
}
