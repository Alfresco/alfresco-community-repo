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

import org.activiti.engine.history.HistoricActivityInstance;
import org.alfresco.repo.web.scripts.admin.NodeBrowserPost;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Assoc;
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.model.Comment;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.WebApiParameters;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.MultiPartRelationshipResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rest.workflow.api.model.Activity;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.webscripts.servlet.FormData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.alfresco.repo.publishing.PublishingModel.ASSOC_LAST_PUBLISHING_EVENT;
import static org.alfresco.util.collections.CollectionUtils.isEmpty;

/**
 * Node Targets
 *
 * - list node (peer) associations - from source to target
 * 
 * @author janv
 */
@RelationshipResource(name = "targets",  entityResource = NodesEntityResource.class, title = "Node Targets")
public class NodeTargetsRelation implements
        RelationshipResourceAction.Read<Node>,
        RelationshipResourceAction.Create<AssocTarget>,
        RelationshipResourceAction.Delete, InitializingBean
{
    private ServiceRegistry sr;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private Nodes nodes;

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

        this.nodeService = sr.getNodeService();
        this.namespaceService = sr.getNamespaceService();
    }

    /**
     * List targets
     *
     * @param sourceNodeId String id of source node
     */
    @Override
    @WebApiDescription(title = "Return a paged list of target nodes based on (peer) assocs")
    public CollectionWithPagingInfo<Node> readAll(String sourceNodeId, Parameters parameters)
    {
        NodeRef sourceNodeRef = nodes.validateOrLookupNode(sourceNodeId, null);

        // TODO option to filter by assocType ... ?
        List<AssociationRef> assocRefs = nodeService.getTargetAssocs(sourceNodeRef, RegexQNamePattern.MATCH_ALL);

        Map<QName, String> qnameMap = new HashMap<>(3);

        Map<String, UserInfo> mapUserInfo = new HashMap<>(10);

        List<String> includeParam = parameters.getInclude();

        List<Node> collection = new ArrayList<Node>(assocRefs.size());
        for (AssociationRef assocRef : assocRefs)
        {
            // minimal info by default (unless "include"d otherwise)
            Node node = nodes.getFolderOrDocument(assocRef.getTargetRef(), null, null, includeParam, mapUserInfo);

            QName assocTypeQName = assocRef.getTypeQName();
            String assocType = qnameMap.get(assocTypeQName);
            if (assocType == null)
            {
                assocType = assocTypeQName.toPrefixString(namespaceService);
                qnameMap.put(assocTypeQName, assocType);
            }
            node.setAssociation(new Assoc(assocType));

            collection.add(node);
        }

        Paging paging = parameters.getPaging();
        return CollectionWithPagingInfo.asPaged(paging, collection, false, collection.size());
    }

    @Override
    @WebApiDescription(title="Add node assoc")
    public List<AssocTarget> create(String sourceNodeId, List<AssocTarget> entity, Parameters parameters)
    {
        List<AssocTarget> result = new ArrayList<>(entity.size());

        NodeRef srcNodeRef = nodes.validateNode(sourceNodeId);

        for (AssocTarget assoc : entity)
        {
            String assocTypeStr = assoc.getAssocType();
            if ((assocTypeStr == null) || assocTypeStr.isEmpty())
            {
                throw new InvalidArgumentException("Missing assocType");
            }

            QName assocTypeQName = QName.createQName(assocTypeStr, namespaceService);

            try
            {
                // TODO consider x-store refs ?
                NodeRef tgtNodeRef = nodes.validateNode(assoc.getTargetId());
                nodeService.createAssociation(srcNodeRef, tgtNodeRef, assocTypeQName);
            }
            catch (AssociationExistsException aee)
            {
                throw new ConstraintViolatedException(aee.getMessage());
            }

            result.add(assoc);
        }
        return result;
    }

    @Override
    @WebApiDescription(title = "Remove node assoc(s)")
    public void delete(String sourceNodeId, String targetNodeId, Parameters parameters)
    {
        // TODO consider x-store refs ?
        NodeRef srcNodeRef = nodes.validateNode(sourceNodeId);
        NodeRef tgtNodeRef = nodes.validateNode(targetNodeId);

        String assocTypeStr = parameters.getParameter("assocType");
        if ((assocTypeStr != null) && (! assocTypeStr.isEmpty()))
        {
            QName assocTypeQName = QName.createQName(assocTypeStr, namespaceService);
            nodeService.removeAssociation(srcNodeRef, tgtNodeRef, assocTypeQName);
        }
        else
        {
            List<AssociationRef> assocRefs = nodeService.getTargetAssocs(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, sourceNodeId), RegexQNamePattern.MATCH_ALL);
            for (AssociationRef assocRef : assocRefs)
            {
                if (assocRef.getTargetRef().equals(tgtNodeRef))
                {
                    nodeService.removeAssociation(srcNodeRef, tgtNodeRef, assocRef.getTypeQName());
                }
            }
        }
    }
}
