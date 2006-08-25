/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.webservice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.webservice.repository.UpdateResult;
import org.alfresco.repo.webservice.types.CML;
import org.alfresco.repo.webservice.types.CMLAddAspect;
import org.alfresco.repo.webservice.types.CMLAddChild;
import org.alfresco.repo.webservice.types.CMLCopy;
import org.alfresco.repo.webservice.types.CMLCreate;
import org.alfresco.repo.webservice.types.CMLCreateAssociation;
import org.alfresco.repo.webservice.types.CMLDelete;
import org.alfresco.repo.webservice.types.CMLMove;
import org.alfresco.repo.webservice.types.CMLRemoveAspect;
import org.alfresco.repo.webservice.types.CMLRemoveAssociation;
import org.alfresco.repo.webservice.types.CMLRemoveChild;
import org.alfresco.repo.webservice.types.CMLUpdate;
import org.alfresco.repo.webservice.types.CMLWriteContent;
import org.alfresco.repo.webservice.types.ContentFormat;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.repo.webservice.types.ParentReference;
import org.alfresco.repo.webservice.types.Predicate;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 * @author Roy Wetherall
 */
public class CMLUtil
{
    private static final String CREATE = "create";
    private static final String ADD_ASPECT = "addAspect";
    private static final String REMOVE_ASPECT = "removeAspect";
    private static final String UPDATE = "update";
    private static final String DELETE = "delete";
    private static final String MOVE = "move";
    private static final String COPY = "copy";
    private static final String ADD_CHILD = "addChild";
    private static final String REMOVE_CHILD = "removeChild";
    private static final String CREATE_ASSOCIATION = "createAssociation";
    private static final String REMOVE_ASSOCIATION = "removeAssociation";
    private static final String WRITE_CONTENT = "writeContent";
    
    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private CopyService copyService;
    private DictionaryService dictionaryService;
    private ContentService contentService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setCopyService(CopyService copyService)
    {
        this.copyService = copyService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Execute a cml update query.
     * 
     * @param cml   the cml objects
     * @return      the update result
     */
    public UpdateResult[] executeCML(CML cml)
    {
        ExecutionContext context = new ExecutionContext();
        List<UpdateResult> results = new ArrayList<UpdateResult>();        
        
        // Execute creates
        CMLCreate[] creates = cml.getCreate();
        if (creates != null)
        {
            for (CMLCreate create : creates)
            {
                executeCMLCreate(create, context, results);
            }
        }
        
        // Exceute add aspect
        CMLAddAspect[] addAspects = cml.getAddAspect();
        if (addAspects != null)
        {
            for (CMLAddAspect addAspect : addAspects)
            {
                executeCMLAddAspect(addAspect, context, results);
            }
        }
        
        // Execeute remove aspect
        CMLRemoveAspect[] removeAspects = cml.getRemoveAspect();
        if (removeAspects != null)
        {
            for (CMLRemoveAspect removeAspect : removeAspects)
            {
                executeCMLRemoveAspect(removeAspect, context, results);
            }
        }
        
        // Execute update
        CMLUpdate[] updates = cml.getUpdate();
        if (updates != null)
        {
            for (CMLUpdate update : updates)
            {
                executeCMLUpdate(update, context, results);
            }
        }
        
        CMLWriteContent[] writes = cml.getWriteContent();
        if (writes != null)
        {
            for (CMLWriteContent write : writes)
            {
                executeCMLWriteContent(write, context, results);
            }
        }
        
        // Execute delete
        CMLDelete[] deletes = cml.getDelete();
        if (deletes != null)
        {
            for (CMLDelete delete  : deletes)
            {
                executeCMLDelete(delete, context, results);
            }
        }
        
        // Execute move 
        CMLMove[] moves = cml.getMove();
        if (moves != null)
        {
            for (CMLMove move : moves)
            {
                executeCMLMove(move, context, results);
            }
        }
        
        // Execute copy
        CMLCopy[] copies  = cml.getCopy();
        if (copies != null)
        {
            for (CMLCopy copy : copies)
            {
                executeCMLCopy(copy, context, results);
            }
        }
        
        // Execute addChild
        CMLAddChild[] addChildren = cml.getAddChild();
        if (addChildren != null)
        {
            for (CMLAddChild addChild  : addChildren)
            {
                executeCMLAddChild(addChild, context, results);
            }
        }
        
        // Execute removeChild
        CMLRemoveChild[] removeChildren  = cml.getRemoveChild();
        if (removeChildren != null)
        {
            for (CMLRemoveChild removeChild : removeChildren)
            {
                executeCMLRemoveChild(removeChild, context, results);
            }
        }
        
        // Execute createAssociation
        CMLCreateAssociation[] createAssocs  = cml.getCreateAssociation();
        if (createAssocs != null)
        {
            for (CMLCreateAssociation createAssoc : createAssocs)
            {
                executeCMLCreateAssociation(createAssoc, context, results);
            }
        }
        
        // Execute removeAssociation
        CMLRemoveAssociation[] removeAssocs = cml.getRemoveAssociation();
        if (removeAssocs != null)
        {
            for (CMLRemoveAssociation removeAssoc : removeAssocs)
            {
                executeCMLRemoveAssociation(removeAssoc, context, results);
            }
        }
        
        return results.toArray(new UpdateResult[results.size()]);
    }

    /**
     * 
     * @param create
     * @param result
     */
    private void executeCMLCreate(CMLCreate create, ExecutionContext context, List<UpdateResult> results)
    {
        // Get the detail of the parent
        ParentReference parentReference = create.getParent();        
        NodeRef parentNodeRef = Utils.convertToNodeRef(
                                            parentReference, 
                                            this.nodeService, 
                                            this.searchService, 
                                            this.namespaceService);
        QName assocTypeQName = QName.createQName(parentReference.getAssociationType());
        QName assocQName = QName.createQName(parentReference.getChildName());
        
        // Get the type of the node to create
        QName nodeTypeQName = QName.createQName(create.getType());
        
        // Get the properties
        PropertyMap properties = getPropertyMap(create.getProperty());
        
        // Create the new node
        NodeRef nodeRef = this.nodeService.createNode(parentNodeRef, assocTypeQName, assocQName, nodeTypeQName, properties).getChildRef();    
        
        // Store the node ref in the execution context (if appropraite)
        String id = create.getId();
        if (id != null && id.length() != 0)
        {
            context.addId(id, nodeRef);
        }

        results.add(createResult(CREATE, id, null, nodeRef));
    }
    
    /**
     * Get a property map from the named value array that can be used when setting properties
     * 
     * @param namedValues   a array of named value properties
     * @return              a property map of vlaues
     */ 
    private PropertyMap getPropertyMap(NamedValue[] namedValues)
    {
        PropertyMap properties = new PropertyMap();
        if (namedValues != null)
        {
            for (NamedValue value : namedValues)
            {
                QName qname = QName.createQName(value.getName());
                Serializable propValue = Utils.getValueFromNamedValue(this.dictionaryService, qname, value);
                properties.put(qname, propValue);
            }
        }
        return properties;
    }
    
    private UpdateResult createResult(String cmd, String sourceId, NodeRef sourceNodeRef, NodeRef destinationNodeRef)
    {
        UpdateResult result = new UpdateResult();
        result.setStatement(cmd);
        if (sourceId != null)
        {
            result.setSourceId(sourceId);
        }
        if (sourceNodeRef != null)
        {
            result.setSource(Utils.convertToReference(sourceNodeRef));
        }
        if (destinationNodeRef != null)
        {
            result.setDestination(Utils.convertToReference(destinationNodeRef));
        }
        // Sort out the count ???        
        return result;
    }
    
    /**
     * 
     * @param addAspect
     * @param result
     */
    private void executeCMLAddAspect(CMLAddAspect addAspect, ExecutionContext context, List<UpdateResult> results)
    {
        // Get the node refs
        List<NodeRef> nodeRefs = getNodeRefList(addAspect.getWhere_id(), addAspect.getWhere(), context); 
        
        // Get the aspect name and the properties
        QName aspectQName = QName.createQName(addAspect.getAspect());
        PropertyMap properties = getPropertyMap(addAspect.getProperty());
                
        for (NodeRef nodeRef : nodeRefs)        
        {
            // Add the aspect
            this.nodeService.addAspect(nodeRef, aspectQName, properties);
            
            // Create the result
            results.add(createResult(ADD_ASPECT, null, nodeRef, nodeRef));
        }        
    }
    
    private void executeCMLRemoveAspect(CMLRemoveAspect removeAspect, ExecutionContext context, List<UpdateResult> results)
    {
        // Get the node refs
        List<NodeRef> nodeRefs = getNodeRefList(removeAspect.getWhere_id(), removeAspect.getWhere(), context); 
        
        // Get the aspect name 
        QName aspectQName = QName.createQName(removeAspect.getAspect());
        
        for (NodeRef nodeRef : nodeRefs)        
        {
            // Add the aspect
            this.nodeService.removeAspect(nodeRef, aspectQName);
            
            // Create the result
            results.add(createResult(REMOVE_ASPECT, null, nodeRef, nodeRef));
        } 
    }
    
    private List<NodeRef> getNodeRefList(String id, Predicate predicate, ExecutionContext context)
    {
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
        if (id != null && id.length() != 0)
        {
            NodeRef localNodeRef = context.getNodeRef(id);
            if (localNodeRef != null)
            {
                nodeRefs.add(localNodeRef);
            }
        }
        else
        {
            nodeRefs = Utils.resolvePredicate(predicate, this.nodeService, this.searchService, this.namespaceService);
        }
        return nodeRefs;
    }
    
    private void executeCMLUpdate(CMLUpdate update, ExecutionContext context, List<UpdateResult> results)
    {
        // Get the nodes and properties
        List<NodeRef> nodeRefs = getNodeRefList(update.getWhere_id(), update.getWhere(), context);
        PropertyMap props = getPropertyMap(update.getProperty());
        
        for (NodeRef nodeRef : nodeRefs)
        {
            // Update the property values
            Map<QName, Serializable> currentProps = this.nodeService.getProperties(nodeRef);
            currentProps.putAll(props);
            this.nodeService.setProperties(nodeRef, currentProps);
            
            // Get the result
            results.add(createResult(UPDATE, null, nodeRef, nodeRef));
        }        
    }
    
    private void executeCMLWriteContent(CMLWriteContent write, ExecutionContext context, List<UpdateResult> results)
    {
        // Get the nodes and content property
        List<NodeRef> nodeRefs = getNodeRefList(write.getWhere_id(), write.getWhere(), context);
        QName property = QName.createQName(write.getProperty());
        ContentFormat format = write.getFormat();
        byte[] content = write.getContent();
        
        for (NodeRef nodeRef : nodeRefs)
        {            
            //Get the content writer
            ContentWriter writer = this.contentService.getWriter(nodeRef, property, true);
            
            // Set the content format details (if they have been specified)
            if (format != null)
            {
                writer.setEncoding(format.getEncoding());
                writer.setMimetype(format.getMimetype());
            }
            
            // Write the content 
            InputStream is = new ByteArrayInputStream(content);
            writer.putContent(is);
            
            results.add(createResult(WRITE_CONTENT, null, nodeRef, nodeRef));
        }
    }
    
    private void executeCMLDelete(CMLDelete delete, ExecutionContext context, List<UpdateResult> results)
    {
        List<NodeRef> nodeRefs = Utils.resolvePredicate(delete.getWhere(), this.nodeService, this.searchService, this.namespaceService);
        for (NodeRef nodeRef : nodeRefs)
        {
            // Delete the node
            this.nodeService.deleteNode(nodeRef);
            
            // Create the result
            results.add(createResult(DELETE, null, nodeRef, null));
        }
    }
    
    private void executeCMLMove(CMLMove move, ExecutionContext context, List<UpdateResult> results)
    {
        NodeRef destinationNodeRef = getNodeRef(move.getTo_id(), move.getTo(), context);
        if (destinationNodeRef != null)
        {
            QName assocType = null;
            QName assocName = null;
            if (move.getTo_id() != null)
            {            
                assocType = QName.createQName(move.getAssociationType());
                assocName = QName.createQName(move.getChildName());
            }
            else
            {
                assocType = QName.createQName(move.getTo().getAssociationType());
                assocName = QName.createQName(move.getTo().getChildName());
            }
            
            List<NodeRef> nodesToMove = getNodeRefList(move.getWhere_id(), move.getWhere(), context);
            for (NodeRef nodeToMove : nodesToMove)
            {
                NodeRef newNodeRef = this.nodeService.moveNode(nodeToMove, destinationNodeRef, assocType, assocName).getChildRef();
                
                // Create the result
                results.add(createResult(MOVE, null, nodeToMove, newNodeRef));
            }
        }
    }    
    
    private NodeRef getNodeRef(String id, ParentReference parentReference, ExecutionContext context)
    {
        NodeRef nodeRef = null;
        if (id != null && id.length() != 0)
        {
            nodeRef = context.getNodeRef(id);
        }
        else
        {
            nodeRef = Utils.convertToNodeRef(parentReference, this.nodeService, this.searchService, this.namespaceService);
        }
        
        return nodeRef;
    }
    
    private NodeRef getNodeRef(String id, Reference reference, ExecutionContext context)
    {
        NodeRef nodeRef = null;
        if (id != null && id.length() != 0)
        {
            nodeRef = context.getNodeRef(id);
        }
        else
        {
            nodeRef = Utils.convertToNodeRef(reference, this.nodeService, this.searchService, this.namespaceService);
        }
        
        return nodeRef;
    }
    
    private void executeCMLCopy(CMLCopy copy, ExecutionContext context, List<UpdateResult> results)
    {
        NodeRef destinationNodeRef = getNodeRef(copy.getTo_id(), copy.getTo(), context);
        if (destinationNodeRef != null)
        {
            QName assocType = null;
            QName assocName = null;
            if (copy.getTo_id() != null)
            {            
                assocType = QName.createQName(copy.getAssociationType());
                assocName = QName.createQName(copy.getChildName());
            }
            else
            {
                assocType = QName.createQName(copy.getTo().getAssociationType());
                assocName = QName.createQName(copy.getTo().getChildName());
            }
            
            boolean copyChildren = false;
            Boolean value = copy.getChildren();
            if (value != null)
            {
                copyChildren = value.booleanValue();
            }
            
            List<NodeRef> nodesToCopy = getNodeRefList(copy.getWhere_id(), copy.getWhere(), context);
            for (NodeRef nodeToCopy : nodesToCopy)
            {
                NodeRef newNodeRef = this.copyService.copy(nodeToCopy, destinationNodeRef, assocType, assocName, copyChildren);
                
                // Create the result
                results.add(createResult(COPY, null, nodeToCopy, newNodeRef));
            }
        }
        
    }

    private void executeCMLAddChild(CMLAddChild addChild, ExecutionContext context, List<UpdateResult> results)
    {
        NodeRef nodeRef = getNodeRef(addChild.getTo_id(), addChild.getTo(), context);
        if (nodeRef != null)
        {
            QName assocType = null;
            QName assocName = null;
            if (addChild.getTo_id() != null)
            {            
                assocType = QName.createQName(addChild.getAssociationType());
                assocName = QName.createQName(addChild.getChildName());
            }
            else
            {
                assocType = QName.createQName(addChild.getTo().getAssociationType());
                assocName = QName.createQName(addChild.getTo().getChildName());
            }
            
            List<NodeRef> whereNodeRefs = getNodeRefList(addChild.getWhere_id(), addChild.getWhere(), context);
            for (NodeRef whereNodeRef : whereNodeRefs)
            {
                this.nodeService.addChild(nodeRef, whereNodeRef, assocType, assocName);
                
                // Create the result
                results.add(createResult(ADD_CHILD, null, nodeRef, whereNodeRef));
            }
        }
    }    
    
    private void executeCMLRemoveChild(CMLRemoveChild removeChild, ExecutionContext context, List<UpdateResult> results)
    {
        NodeRef parentNodeRef = getNodeRef(removeChild.getFrom_id(), removeChild.getFrom(), context);
        if (parentNodeRef != null)
        {            
            List<NodeRef> childNodeRefs = getNodeRefList(removeChild.getWhere_id(), removeChild.getWhere(), context);
            for (NodeRef childNodeRef : childNodeRefs)
            {
                this.nodeService.removeChild(parentNodeRef, childNodeRef);
                
                // Create the result
                results.add(createResult(REMOVE_CHILD, null, parentNodeRef, null));
            }
        }
        
    }

    private void executeCMLCreateAssociation(CMLCreateAssociation createAssoc, ExecutionContext context, List<UpdateResult> results)
    {
        QName assocType = QName.createQName(createAssoc.getAssociation());
        if (assocType != null)
        {
            List<NodeRef> fromNodeRefs = getNodeRefList(createAssoc.getFrom_id(), createAssoc.getFrom(), context);
            List<NodeRef> toNodeRefs = getNodeRefList(createAssoc.getTo_id(), createAssoc.getTo(), context);
            for (NodeRef fromNodeRef : fromNodeRefs)
            {
                for (NodeRef toNodeRef : toNodeRefs)
                {
                    this.nodeService.createAssociation(fromNodeRef, toNodeRef, assocType);
                    
                    // Create the result
                    results.add(createResult(CREATE_ASSOCIATION, null, fromNodeRef, toNodeRef));
                }
            }
        }        
    }

    private void executeCMLRemoveAssociation(CMLRemoveAssociation removeAssoc, ExecutionContext context, List<UpdateResult> results)
    {
        QName assocType = QName.createQName(removeAssoc.getAssociation());
        if (assocType != null)
        {
            List<NodeRef> fromNodeRefs = getNodeRefList(removeAssoc.getFrom_id(), removeAssoc.getFrom(), context);
            List<NodeRef> toNodeRefs = getNodeRefList(removeAssoc.getTo_id(), removeAssoc.getTo(), context);
            for (NodeRef fromNodeRef : fromNodeRefs)
            {
                for (NodeRef toNodeRef : toNodeRefs)
                {
                    this.nodeService.removeAssociation(fromNodeRef, toNodeRef, assocType);
                    
                    // Create the result
                    results.add(createResult(REMOVE_ASSOCIATION, null, fromNodeRef, toNodeRef));
                }
            }
        }        
    }
    
    private class ExecutionContext
    {
        private Map<String, NodeRef> idMap = new HashMap<String, NodeRef>();
        private Map<NodeRef, String> nodeRefMap = new HashMap<NodeRef, String>();
        
        public void addId(String id, NodeRef nodeRef)
        {
            this.idMap.put(id, nodeRef);
            this.nodeRefMap.put(nodeRef, id);
        }
        
        public NodeRef getNodeRef(String id)
        {
            return this.idMap.get(id);
        }
        
        public String getId(NodeRef nodeRef)
        {
            return this.nodeRefMap.get(nodeRef);
        }
    }
}
