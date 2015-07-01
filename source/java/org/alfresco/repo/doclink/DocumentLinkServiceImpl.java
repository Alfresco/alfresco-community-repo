/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.doclink;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.repo.security.permissions.AccessDeniedException;
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
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the document link service
 * 
 * @author Ana Bozianu
 * @since 5.1
 */
public class DocumentLinkServiceImpl implements DocumentLinkService
{
    private static Log logger = LogFactory.getLog(DocumentLinkServiceImpl.class);

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private SearchService searchService;
    private NamespaceService namespaceService;

    /** Shallow search for nodes with a name pattern */
    private static final String XPATH_QUERY_NODE_NAME_MATCH = "./*[like(@cm:name, $cm:name, false)]";
    
    /** Shallow search for links with a destination pattern */
    private static final String XPATH_QUERY_LINK_DEST_MATCH = ".//*[like(@cm:destination, $cm:destination, false)]";

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

        /* Create link */
        String sourceName = (String) nodeService.getProperty(source, ContentModel.PROP_NAME);
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, sourceName);
        props.put(ContentModel.PROP_LINK_DESTINATION, source);
        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(sourceName));

        // check if the link node already exists
        if (checkExists(sourceName, destination))
        {
            throw new IllegalArgumentException("A file with the name '" + sourceName + "' already exists in the destination folder");
        }

        ChildAssociationRef childRef = null;
        if (dictionaryService.isSubClass(nodeService.getType(source), ContentModel.TYPE_CONTENT))
        {
            // create File Link node
            childRef = nodeService.createNode(destination, ContentModel.ASSOC_CONTAINS, assocQName, ApplicationModel.TYPE_FILELINK, props);

        }
        else if (dictionaryService.isSubClass(nodeService.getType(source), ContentModel.TYPE_FOLDER))
        {
            // create Folder link node
            childRef = nodeService.createNode(destination, ContentModel.ASSOC_CONTAINS, assocQName, ApplicationModel.TYPE_FOLDERLINK, props);
        }
        else
        {
            throw new IllegalArgumentException("unsupported source node type : " + nodeService.getType(source));
        }

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
    public DeleteLinksStatusReport deleteLinksToDocument(NodeRef document)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Deleting links of a document. document: " + document);
        }
        
        /* Validate input */
        PropertyCheck.mandatory(this, "document", document);
        
        /* Get all links of the given document */
        QueryParameterDefinition[] params = new QueryParameterDefinition[1];
        params[0] = new QueryParameterDefImpl(ContentModel.PROP_LINK_DESTINATION, dictionaryService.getDataType(DataTypeDefinition.NODE_REF), true, document.toString());

        /* Search for links in all stores */
        DeleteLinksStatusReport report = new DeleteLinksStatusReport();
        for(StoreRef store : nodeService.getStores())
        {
            /* Get the root node */
            NodeRef rootNodeRef = nodeService.getRootNode(store);
            
            /* Execute the query, retrieve links to the document*/
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, XPATH_QUERY_LINK_DEST_MATCH, params, namespaceService, true);
            report.addTotalLinksFoundCount(nodeRefs.size());
            
            /* Delete the found nodes */
            for(NodeRef linkRef : nodeRefs)
            {
                try{
                    nodeService.deleteNode(linkRef);
                    
                    /* if the node was successfully deleted increment the count */
                    report.incrementDeletedLinksCount();
                }
                catch(AccessDeniedException ex)
                {
                    /* if the node could not be deleted add it to the report */
                    report.addErrorDetail(linkRef, ex);
                }
            }
        }
        
        return report;
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
}
