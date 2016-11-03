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
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DeleteLinksStatusReport;
import org.alfresco.service.cmr.repository.DocumentLinkService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Implementation of the document link service
 * In addition to the document link service, this class also provides a BeforeDeleteNodePolicy
 * 
 * @author Ana Bozianu
 * @since 5.1
 */
public class DocumentLinkServiceImpl implements DocumentLinkService, NodeServicePolicies.BeforeDeleteNodePolicy
{
    private static Log logger = LogFactory.getLog(DocumentLinkServiceImpl.class);

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private CheckOutCheckInService checkOutCheckInService;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;

    /** Shallow search for nodes with a name pattern */
    private static final String XPATH_QUERY_NODE_NAME_MATCH = "./*[like(@cm:name, $cm:name, false)]";
    
    /** Shallow search for links with a destination pattern */
    private static final String XPATH_QUERY_LINK_DEST_MATCH = ".//*[like(@cm:destination, $cm:destination, false)]";

    private static final String LINK_NODE_EXTENSION = ".url";

    /* I18N labels */
    private static final String LINK_TO_LABEL = "doclink_service.link_to_label";
    
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

        // Register interest in the beforeDeleteNode policy 

        //for nodes that have app:linked aspect
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
    public NodeRef createDocumentLink(NodeRef source, NodeRef destination)
    {
        if(logger.isDebugEnabled())
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
        //if file is working copy - create link to the original
        if (checkOutCheckInService.isWorkingCopy(source))
        {
            source = checkOutCheckInService.getCheckedOut(source);
        }
        /* Create link */
        String sourceName = (String) nodeService.getProperty(source, ContentModel.PROP_NAME);

        String newName = sourceName + LINK_NODE_EXTENSION;
        newName = I18NUtil.getMessage(LINK_TO_LABEL, newName);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, newName);
        props.put(ContentModel.PROP_LINK_DESTINATION, source);

        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(newName));

        // check if the link node already exists
        if (checkExists(newName, destination))
        {
            throw new IllegalArgumentException("A file with the name '" + newName + "' already exists in the destination folder");
        }

        ChildAssociationRef childRef = null;
        QName sourceType = nodeService.getType(source);
        
        if (dictionaryService.isSubClass(sourceType, ContentModel.TYPE_CONTENT))
        {
            // create File Link node
            childRef = nodeService.createNode(destination, ContentModel.ASSOC_CONTAINS, assocQName, ApplicationModel.TYPE_FILELINK, props);

        }
        else if (!dictionaryService.isSubClass(sourceType, SiteModel.TYPE_SITE) && dictionaryService.isSubClass(nodeService.getType(source), ContentModel.TYPE_FOLDER))
        {
            // create Folder link node
            childRef = nodeService.createNode(destination, ContentModel.ASSOC_CONTAINS, assocQName, ApplicationModel.TYPE_FOLDERLINK, props);
        }
        else
        {
            throw new IllegalArgumentException("unsupported source node type : " + nodeService.getType(source));
        }

        //add linked aspect to the sourceNode
        nodeService.addAspect(source, ApplicationModel.ASPECT_LINKED, null);

        return childRef.getChildRef();
    }

    /**
     * Check if node with specified <code>name</code> exists within a
     * <code>parent</code> folder.
     * 
     * @param name
     * @param parent
     * @return
     */
    private boolean checkExists(String name, NodeRef parent)
    {
        QueryParameterDefinition[] params = new QueryParameterDefinition[1];
        params[0] = new QueryParameterDefImpl(ContentModel.PROP_NAME, dictionaryService.getDataType(DataTypeDefinition.TEXT), true, name);

        // execute the query
        List<NodeRef> nodeRefs = searchService.selectNodes(parent, XPATH_QUERY_NODE_NAME_MATCH, params, namespaceService, false);

        return (nodeRefs.size() != 0);
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
    public List<NodeRef> getNodeLinks(NodeRef nodeRef)
    {
        /* Validate input */
        PropertyCheck.mandatory(this, "nodeRef", nodeRef);
        
        /* Get all links of the given nodeRef */
        QueryParameterDefinition[] params = new QueryParameterDefinition[1];
        params[0] = new QueryParameterDefImpl(ContentModel.PROP_LINK_DESTINATION, dictionaryService.getDataType(DataTypeDefinition.NODE_REF), true, nodeRef.toString());

        List<NodeRef> nodeLinks = new ArrayList<NodeRef>();
        List<NodeRef> nodeRefs;
        /* Search for links in all stores */
        for(StoreRef store : nodeService.getStores())
        {
            /* Get the root node */
            NodeRef rootNodeRef = nodeService.getRootNode(store);

            /* Execute the query, retrieve links to the document*/
            nodeRefs = searchService.selectNodes(rootNodeRef, XPATH_QUERY_LINK_DEST_MATCH, params, namespaceService, true);
            nodeLinks.addAll(nodeRefs);
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

        List<NodeRef> linkNodeRefs = getNodeLinks(document);
        report.addTotalLinksFoundCount(linkNodeRefs.size());

        for (NodeRef linkRef : linkNodeRefs)
        {
            try
            {
                nodeService.deleteNode(linkRef);

                /* if the node was successfully deleted increment the count */
                report.incrementDeletedLinksCount();
            }
            catch (AccessDeniedException ex)
            {
                /* if the node could not be deleted add it to the report */
                report.addErrorDetail(linkRef, ex);
            }
        }

        // remove also the aspect app:linked
        nodeService.removeAspect(document, ApplicationModel.ASPECT_LINKED);

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
        // NodeRef linkNodeRef = childAssocRef.getChildRef();
        NodeRef nodeRef = getLinkDestination(linkNodeRef);

        List<NodeRef> nodeRefLinks = getNodeLinks(nodeRef);

        if (nodeRefLinks.size() == 1 && nodeRefLinks.contains(linkNodeRef))
        {
            behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            try
            {
                // remove linked aspect to the sourceNode
                nodeService.removeAspect(nodeRef, ApplicationModel.ASPECT_LINKED);
            }
            finally
            {
                behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            }
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
}
