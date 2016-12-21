/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.doclink;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DeleteLinksStatusReport;
import org.alfresco.service.cmr.repository.DocumentLinkService;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Implementation of the document link service In addition to the document link
 * service, this class also provides a BeforeDeleteNodePolicy
 * 
 * @author Ana Bozianu
 * @since 5.1
 */
public class DocumentLinkServiceImpl implements DocumentLinkService, NodeServicePolicies.BeforeDeleteNodePolicy
{
    private static Log logger = LogFactory.getLog(DocumentLinkServiceImpl.class);

    protected static final String CANNED_QUERY_GET_DOC_LINKS = "getDoclinkNodesCannedQueryFactory";

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private CheckOutCheckInService checkOutCheckInService;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private PermissionService permissionService;
    private CannedQueryDAO cannedQueryDAO;
    private QNameDAO qnameDAO;

    private static final String LINK_NODE_EXTENSION = ".url";

    /* I18N labels */
    private static final String LINK_TO_LABEL = "doclink_service.link_to_label";

    private boolean isNodePendingDelete = false;

    /**
     * The initialise method. Register our policies.
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "checkOutCheckInService", checkOutCheckInService);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "behaviourFilter", behaviourFilter);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "cannedQueryDAO", cannedQueryDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);

        // Register interest in the beforeDeleteNode policy

        // for nodes that have app:linked aspect
        policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                ApplicationModel.ASPECT_LINKED,
                new JavaBehaviour(this, "beforeDeleteNode"));

       //for app:filelink node types 
       policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                ApplicationModel.TYPE_FILELINK,
                new JavaBehaviour(this, "beforeDeleteLinkNode"));

        //for app:folderlink node types 
        policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                ApplicationModel.TYPE_FOLDERLINK,
                new JavaBehaviour(this, "beforeDeleteLinkNode"));
    }

    @Override
    public NodeRef createDocumentLink(final NodeRef source, NodeRef destination)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Creating document link. source: " + source + ", destination: " + destination);
        }

        /* Validate input */
        PropertyCheck.mandatory(this, "source", source);
        PropertyCheck.mandatory(this, "destination", destination);

        // check if source node exists
        if (!nodeService.exists(source))
        {
            throw new IllegalArgumentException("Source NodeRef '" + source + "' does not exist");
        }
        // check if destination node exists
        if (!nodeService.exists(destination))
        {
            throw new IllegalArgumentException("Destination NodeRef '" + destination + "' does not exist");
        }
        // check if destination node is a directory
        if (!dictionaryService.isSubClass(nodeService.getType(destination), ContentModel.TYPE_FOLDER))
        {
            throw new IllegalArgumentException("Destination node NodeRef '" + source + "' must be of type " + ContentModel.TYPE_FOLDER);
        }

        /* Create link */
        String sourceName = (String) nodeService.getProperty(source, ContentModel.PROP_NAME);

        String newName = sourceName + LINK_NODE_EXTENSION;
        newName = I18NUtil.getMessage(LINK_TO_LABEL, newName);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, newName);
        props.put(ContentModel.PROP_LINK_DESTINATION, source);
        props.put(ContentModel.PROP_TITLE, newName);
        props.put(ContentModel.PROP_DESCRIPTION, newName);

        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(newName));

        ChildAssociationRef childRef = null;
        QName sourceType = nodeService.getType(source);

        if (checkOutCheckInService.isWorkingCopy(source) || nodeService.hasAspect(source, ContentModel.ASPECT_LOCKABLE))
        {
            throw new IllegalArgumentException("Cannot perform operation since the node (id:" + source.getId() + ") is locked.");
        }

        try
        {
            if (dictionaryService.isSubClass(sourceType, ContentModel.TYPE_CONTENT))
            {
                // create File Link node
                childRef = nodeService.createNode(destination, ContentModel.ASSOC_CONTAINS, assocQName, ApplicationModel.TYPE_FILELINK, props);

            }
            else if (!dictionaryService.isSubClass(sourceType, SiteModel.TYPE_SITE)
                    && dictionaryService.isSubClass(nodeService.getType(source), ContentModel.TYPE_FOLDER))
            {
                // create Folder link node
                props.put(ApplicationModel.PROP_ICON, "space-icon-link");
                childRef = nodeService.createNode(destination, ContentModel.ASSOC_CONTAINS, assocQName, ApplicationModel.TYPE_FOLDERLINK, props);
            }
            else
            {
                throw new IllegalArgumentException("Unsupported source node type : " + nodeService.getType(source));
            }
        }
        catch (DuplicateChildNodeNameException ex)
        {
            throw new IllegalArgumentException("A file with the name '" + newName + "' already exists in the destination folder", ex);
        }

        // add linked aspect to the sourceNode - run as System
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
        {
            public Void doWork() throws Exception
            {
                behaviourFilter.disableBehaviour(source, ContentModel.ASPECT_AUDITABLE);
                try
                {
                    nodeService.addAspect(source, ApplicationModel.ASPECT_LINKED, null);
                }
                finally
                {
                    behaviourFilter.enableBehaviour(source, ContentModel.ASPECT_AUDITABLE);
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

        return childRef.getChildRef();
    }

    @Override
    public NodeRef getLinkDestination(NodeRef linkNodeRef)
    {
        /* Validate input */
        PropertyCheck.mandatory(this, "linkNodeRef", linkNodeRef);

        /* Check if the node exists */
        if (!nodeService.exists(linkNodeRef))
        {
            throw new IllegalArgumentException("The provided node does not exist");
        }

        if (!nodeService.getType(linkNodeRef).equals(ApplicationModel.TYPE_FILELINK)
                && !nodeService.getType(linkNodeRef).equals(ApplicationModel.TYPE_FOLDERLINK))
        {
            throw new IllegalArgumentException("The provided node is not a document link");
        }

        return (NodeRef) nodeService.getProperty(linkNodeRef, ContentModel.PROP_LINK_DESTINATION);
    }

    @Override
    public List<Long> getNodeLinksIds(NodeRef nodeRef)
    {
        /* Validate input */
        PropertyCheck.mandatory(this, "nodeRef", nodeRef);

        /* Get all links of the given nodeRef */
        PagingRequest pagingRequest = new PagingRequest(0, 100000);
        List<Long> nodeLinks = new ArrayList<Long>();

        Pair<Long, QName> nameQName = qnameDAO.getQName(ContentModel.PROP_LINK_DESTINATION);
        if (nameQName != null)
        {
            // Execute the canned query if there are links in the database
            GetDoclinkNodesCannedQueryParams parameterBean = new GetDoclinkNodesCannedQueryParams(nodeRef.toString(), 
                                                                                                  nameQName.getFirst(), 
                                                                                                  pagingRequest.getMaxItems());
            CannedQueryParameters params = new CannedQueryParameters(parameterBean, 
                                                                     null, 
                                                                     null, 
                                                                     pagingRequest.getRequestTotalCountMax(), 
                                                                     pagingRequest.getQueryExecutionId());
            CannedQuery<Long> query = new GetDoclinkNodesCannedQuery(cannedQueryDAO, 
                                                                     params);
            CannedQueryResults<Long> results = query.execute();

            for (Long nodeId : results.getPage())
            {
                nodeLinks.add(nodeId);
            }
        }

        return nodeLinks;
    }

    @Override
    public DeleteLinksStatusReport deleteLinksToDocument(NodeRef document)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Deleting links of a document. document: " + document);
        }

        /* Validate input */
        PropertyCheck.mandatory(this, "document", document);

        DeleteLinksStatusReport report = new DeleteLinksStatusReport();

        List<Long> linkNodeIds = getNodeLinksIds(document);
        report.addTotalLinksFoundCount(linkNodeIds.size());

        for (Long linkId : linkNodeIds)
        {
            NodeRef linkNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    NodeRef nodeRef = nodeService.getNodeRef(linkId);
                    isNodePendingDelete = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_PENDING_DELETE);
                    return nodeRef;
                }
            }, AuthenticationUtil.getSystemUserName());

            if (!isNodePendingDelete)
            {
                if (permissionService.hasPermission(linkNodeRef, PermissionService.DELETE_NODE) == AccessStatus.DENIED)
                {
                    report.addErrorDetail(linkNodeRef,
                            new AccessDeniedException("User '" + AuthenticationUtil.getFullyAuthenticatedUser() + "' doesn't have permission to create discussion on node '" + linkNodeRef + "'"));
                }
                else
                {
                    nodeService.deleteNode(linkNodeRef);
                    // if the node was successfully deleted increment the count
                    report.incrementDeletedLinksCount();
                }
            }
        }

        // remove also the aspect app:linked if all links were deleted with success
        if (report.getTotalLinksFoundCount() == report.getDeletedLinksCount())
        {
            behaviourFilter.disableBehaviour(document, ContentModel.ASPECT_AUDITABLE);
            behaviourFilter.disableBehaviour(document, ContentModel.ASPECT_LOCKABLE);
            try
            {
                nodeService.removeAspect(document, ApplicationModel.ASPECT_LINKED);
            }
            finally
            {
                behaviourFilter.enableBehaviour(document, ContentModel.ASPECT_AUDITABLE);
                behaviourFilter.enableBehaviour(document, ContentModel.ASPECT_LOCKABLE);
            }
        }

        return report;
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        try
        {
            deleteLinksToDocument(nodeRef);
        }
        finally
        {
            behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
        }

    }

    public void beforeDeleteLinkNode(NodeRef linkNodeRef)
    {
        final NodeRef nodeRef = getLinkDestination(linkNodeRef);

        List<Long> nodeRefLinks = getNodeLinksIds(nodeRef);

        long linkNodeId = (Long) nodeService.getProperty(linkNodeRef, ContentModel.PROP_NODE_DBID);

        if (nodeRefLinks.size() == 1 && nodeRefLinks.contains(linkNodeId))
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>()
            {
                public Void doWork() throws Exception
                {
                    behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                    behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_LOCKABLE);
                    try
                    {
                        nodeService.removeAspect(nodeRef, ApplicationModel.ASPECT_LINKED);
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                        behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_LOCKABLE);
                    }

                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService)
    {
        this.checkOutCheckInService = checkOutCheckInService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setCannedQueryDAO(CannedQueryDAO cannedQueryDAO)
    {
        this.cannedQueryDAO = cannedQueryDAO;
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

}