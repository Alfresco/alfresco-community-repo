/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.solr;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.ChildAssocRefQueryCallback;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.solr.SOLRDAO;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;

/**
 * Component providing data for SOLR tracking
 * 
 * @since 4.0
 */
public class SOLRTrackingComponentImpl implements SOLRTrackingComponent
{
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private SOLRDAO solrDAO;
    private DictionaryDAO dictionaryDAO;
    private PermissionService permissionService;
    private AclDAO aclDAO;
    private OwnableService ownableService;
    private TenantService tenantService;
    private DictionaryService dictionaryService;
    private boolean enabled = true;
    
    
    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setSolrDAO(SOLRDAO solrDAO)
    {
        this.solrDAO = solrDAO;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setAclDAO(AclDAO aclDAO)
    {
        this.aclDAO = aclDAO;
    }

    public void setDictionaryDAO(DictionaryDAO dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }

    /**
     * Initialize
     */    
    public void init()
    {
        PropertyCheck.mandatory(this, "solrDAO", solrDAO);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "ownableService", ownableService);
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "dictionaryDAO", dictionaryDAO);
        PropertyCheck.mandatory(this, "aclDAO", aclDAO);
    }
    
    @Override
    public List<AclChangeSet> getAclChangeSets(Long minAclChangeSetId, Long fromCommitTime, int maxResults)
    {
        if(enabled)
        {
            List<AclChangeSet> changesets = solrDAO.getAclChangeSets(minAclChangeSetId, fromCommitTime, maxResults);
            return changesets;
        }
        else
        {
            return Collections.<AclChangeSet>emptyList();
        }
    }

    @Override
    public List<Acl> getAcls(List<Long> aclChangeSetIds, Long minAclId, int maxResults)
    {
        if(enabled)
        {
            List<Acl> acls = solrDAO.getAcls(aclChangeSetIds, minAclId, maxResults);
            return acls;
        }
        else
        {
            return Collections.<Acl>emptyList();
        }
    }

    @Override
    public List<AclReaders> getAclsReaders(List<Long> aclIds)
    {
        if(enabled)
        {
            /*
             * This is an N+1 query that should, in theory, make use of cached ACL readers data.
             */

            List<AclReaders> aclsReaders = new ArrayList<AclReaders>(aclIds.size() * 10);
            for (Long aclId : aclIds)
            {
                Set<String> readersSet = permissionService.getReaders(aclId);
                AclReaders readers = new AclReaders();
                readers.setAclId(aclId);
                readers.setReaders(readersSet);
                readers.setAclChangeSetId(aclDAO.getAccessControlList(aclId).getProperties().getAclChangeSetId());
                aclsReaders.add(readers);
            }

            return aclsReaders;
        }
        else
        {
            return Collections.<AclReaders>emptyList();
        }
    }

    @Override
    public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, int maxResults)
    {
        if(enabled)
        {
            List<Transaction> txns = solrDAO.getTransactions(minTxnId, fromCommitTime, maxResults);
            return txns;
        }
        else
        {
            return Collections.<Transaction>emptyList();
        } 
    }

    /**
     * {@inheritDoc}
     */
	public void getNodes(NodeParameters nodeParameters, NodeQueryCallback callback)
	{
	    if(enabled)
	    {
	        List<Node> nodes = solrDAO.getNodes(nodeParameters);

	        for (Node node : nodes)
	        {
	            callback.handleNode(node);
	        }
	    }
	}

	/**
	 * A dumb iterator that iterates over longs in sequence.
	 */
	private static class SequenceIterator implements Iterable<Long>, Iterator<Long>
	{
	    private long fromId;
	    private long toId;
	    private long counter;
	    private int maxResults;
	    private boolean inUse = false;

	    SequenceIterator(Long fromId, Long toId, int maxResults)
	    {
	        this.fromId = (fromId == null ? 1 : fromId.longValue());
	        this.toId = (toId == null ? Long.MAX_VALUE : toId.longValue());
	        this.maxResults = maxResults;
	        this.counter = this.fromId;
	    }
	    
        @Override
        public Iterator<Long> iterator()
        {
            if(inUse)
            {
                throw new IllegalStateException("Already in use");
            }
            this.counter = this.fromId;
            this.inUse = true;
            return this;
        }

        @Override
        public boolean hasNext()
        {
            return ((counter - this.fromId) < maxResults) &&  counter <= toId;
        }

        @Override
        public Long next()
        {
            return counter++;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
	}

    private boolean isCategorised(AspectDefinition aspDef)
    {
        if(aspDef == null)
        {
            return false;
        }
        AspectDefinition current = aspDef;
        while (current != null)
        {
            if (current.getName().equals(ContentModel.ASPECT_CLASSIFIABLE))
            {
                return true;
            }
            else
            {
                QName parentName = current.getParentName();
                if (parentName == null)
                {
                    break;
                }
                current = dictionaryService.getAspect(parentName);
            }
        }
        return false;
    }
    
    private Collection<Pair<Path, QName>> getCategoryPaths(NodeRef nodeRef, Set<QName> aspects, Map<QName, Serializable> properties)
    {
        ArrayList<Pair<Path, QName>> categoryPaths = new ArrayList<Pair<Path, QName>>();

        for (QName classRef : aspects)
        {
            AspectDefinition aspDef = dictionaryService.getAspect(classRef);
            if (!isCategorised(aspDef))
            {
                continue;
            }
            LinkedList<Pair<Path, QName>> aspectPaths = new LinkedList<Pair<Path, QName>>();
            for (PropertyDefinition propDef : aspDef.getProperties().values())
            {
                if (!propDef.getDataType().getName().equals(DataTypeDefinition.CATEGORY))
                {
                    // The property is not a category
                    continue;
                }
                // Don't try to iterate if the property is null
                Serializable propVal = properties.get(propDef.getName());
                if (propVal == null)
                {
                    continue;
                }
                for (NodeRef catRef : DefaultTypeConverter.INSTANCE.getCollection(NodeRef.class, propVal))
                {
                    if (catRef == null)
                    {
                        continue;
                    }
                    // can be running in context of System user, hence use input nodeRef
                    catRef = tenantService.getName(nodeRef, catRef);

                    try
                    {
                        Pair<Long, NodeRef> pair = nodeDAO.getNodePair(catRef);
                        for (Path path : nodeDAO.getPaths(pair, false))
                        {
                            aspectPaths.add(new Pair<Path, QName>(path, aspDef.getName()));   
                        }
                    }
                    catch (InvalidNodeRefException e)
                    {
                        // If the category does not exists we move on the next
                    }
                }
            }
            categoryPaths.addAll(aspectPaths);
        }
        // Add member final element
        for (Pair<Path, QName> pair : categoryPaths)
        {
            if (pair.getFirst().last() instanceof Path.ChildAssocElement)
            {
                Path.ChildAssocElement cae = (Path.ChildAssocElement) pair.getFirst().last();
                ChildAssociationRef assocRef = cae.getRef();
                pair.getFirst().append(new Path.ChildAssocElement(new ChildAssociationRef(assocRef.getTypeQName(), assocRef.getChildRef(), QName.createQName("member"), nodeRef)));
            }
        }

        return categoryPaths;
    }
    
    private List<Long> preCacheNodes(NodeMetaDataParameters nodeMetaDataParameters)
    {
        int maxResults = nodeMetaDataParameters.getMaxResults();
        boolean isLimitSet = (maxResults != 0 && maxResults != Integer.MAX_VALUE);

        List<Long> nodeIds = null;
        Iterable<Long> iterable = null;
        List<Long> allNodeIds = nodeMetaDataParameters.getNodeIds();
        if(allNodeIds != null)
        {
            int toIndex = (maxResults > allNodeIds.size() ? allNodeIds.size() : maxResults);
            nodeIds = isLimitSet ? allNodeIds.subList(0, toIndex) : nodeMetaDataParameters.getNodeIds();
            iterable = nodeMetaDataParameters.getNodeIds();
        }
        else
        {
            Long fromNodeId = nodeMetaDataParameters.getFromNodeId();
            Long toNodeId = nodeMetaDataParameters.getToNodeId();
            nodeIds = new ArrayList<Long>(isLimitSet ? maxResults : 100); // TODO better default here?
            iterable = new SequenceIterator(fromNodeId, toNodeId, maxResults);
            int counter = 1;
            for(Long nodeId : iterable)
            {
                if(isLimitSet && counter++ > maxResults)
                {
                    break;
                }
                nodeIds.add(nodeId);
            }
        }
        // pre-cache nodes
        nodeDAO.cacheNodesById(nodeIds);
        
        return nodeIds;
    }

    protected Map<QName, Serializable> getProperties(Long nodeId)
    {
        Map<QName, Serializable> props = null;

        // ALF-10641
        // Residual properties are un-indexed -> break serlialisation
        Map<QName, Serializable> sourceProps = nodeDAO.getNodeProperties(nodeId);
        props = new HashMap<QName, Serializable>((int)(sourceProps.size() * 1.3));
        for(QName propertyQName : sourceProps.keySet())
        {
            PropertyDefinition propDef = dictionaryService.getProperty(propertyQName);
            if(propDef != null)
            {
                props.put(propertyQName, sourceProps.get(propertyQName));
            }
        }

        return props;
    }

    /**
     * {@inheritDoc}
     */
    public void getNodesMetadata(
            NodeMetaDataParameters nodeMetaDataParameters,
            MetaDataResultsFilter resultFilter,
            NodeMetaDataQueryCallback callback)
    {
        if(false == enabled)
        {
            return;
        }
        
        NodeMetaDataQueryRowHandler rowHandler = new NodeMetaDataQueryRowHandler(callback);
        boolean includeType = (resultFilter == null ? true : resultFilter.getIncludeType());
        boolean includeProperties = (resultFilter == null ? true : resultFilter.getIncludeProperties());
        boolean includeAspects = (resultFilter == null ? true : resultFilter.getIncludeAspects());
        boolean includePaths = (resultFilter == null ? true : resultFilter.getIncludePaths());
        boolean includeNodeRef = (resultFilter == null ? true : resultFilter.getIncludeNodeRef());
        boolean includeParentAssociations = (resultFilter == null ? true : resultFilter.getIncludeParentAssociations());
        boolean includeChildAssociations = (resultFilter == null ? true : resultFilter.getIncludeChildAssociations());
        boolean includeOwner = (resultFilter == null ? true : resultFilter.getIncludeOwner());
        boolean includeChildIds = (resultFilter == null ? true : resultFilter.getIncludeChildIds());
        boolean includeTxnId = (resultFilter == null ? true : resultFilter.getIncludeTxnId());
        
        List<Long> nodeIds = preCacheNodes(nodeMetaDataParameters);

        for(Long nodeId : nodeIds)
        {
            Map<QName, Serializable> props = null;
            Set<QName> aspects = null;
            
            if (!nodeDAO.exists(nodeId))
            {
                // Deleted nodes have no metadata
                continue;
            }

            NodeMetaData nodeMetaData = new NodeMetaData();
            nodeMetaData.setNodeId(nodeId);

            Pair<Long, NodeRef> pair = nodeDAO.getNodePair(nodeId);
            nodeMetaData.setAclId(nodeDAO.getNodeAclId(nodeId));

            if(includeTxnId)
            {
                nodeMetaData.setTxnId(nodeDAO.getNodeRefStatus(pair.getSecond()).getDbTxnId());
            }
            
            if(includeType)
            {
                QName nodeType = nodeDAO.getNodeType(nodeId);
                nodeMetaData.setNodeType(nodeType);
            }

            if(includeProperties)
            {
                props = getProperties(nodeId);
                nodeMetaData.setProperties(props);
            }
            else
            {
                nodeMetaData.setProperties(Collections.<QName, Serializable>emptyMap());
            }

            if(includeAspects)
            {
                aspects = nodeDAO.getNodeAspects(nodeId);
            }
            nodeMetaData.setAspects(aspects);
            
            if(includePaths)
            {
                if(props == null)
                {
                    props = getProperties(nodeId);
                }
                Collection<Pair<Path, QName>> categoryPaths = getCategoryPaths(pair.getSecond(), aspects, props);
                List<Path> directPaths = nodeDAO.getPaths(pair, false);

                Collection<Pair<Path, QName>> paths = new ArrayList<Pair<Path, QName>>(directPaths.size() + categoryPaths.size());
                for (Path path : directPaths)
                {
                    paths.add(new Pair<Path, QName>(path, null));
                }
                paths.addAll(categoryPaths);

                nodeMetaData.setPaths(paths);
            }

            if(includeNodeRef)
            {
                nodeMetaData.setNodeRef(pair.getSecond());
            }
        
            if(includeChildAssociations)
            {
                final List<ChildAssociationRef> childAssocs = new ArrayList<ChildAssociationRef>(100);
                nodeDAO.getChildAssocs(nodeId, null, null, null, null, null, new ChildAssocRefQueryCallback()
                {
                    @Override
                    public boolean preLoadNodes()
                    {
                        return false;
                    }
                    
                    @Override
                    public boolean orderResults()
                    {
                        return false;
                    }

                    @Override
                    public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair,
                            Pair<Long, NodeRef> childNodePair)
                    {
                        childAssocs.add(childAssocPair.getSecond());
                        return true;
                    }
                    
                    @Override
                    public void done()
                    {
                    }
                });
                nodeMetaData.setChildAssocs(childAssocs);
            }
            
            if(includeChildIds)
            {
                final List<Long> childIds = new ArrayList<Long>(100);
                nodeDAO.getChildAssocs(nodeId, null, null, null, null, null, new ChildAssocRefQueryCallback()
                {
                    @Override
                    public boolean preLoadNodes()
                    {
                        return false;
                    }
                    
                    @Override
                    public boolean orderResults()
                    {
                        return false;
                    }

                    @Override
                    public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair,
                            Pair<Long, NodeRef> childNodePair)
                    {
                        childIds.add(childNodePair.getFirst());
                        return true;
                    }
                    
                    @Override
                    public void done()
                    {
                    }
                });
                nodeMetaData.setChildIds(childIds);
            }

            if(includeParentAssociations)
            {
                final List<ChildAssociationRef> parentAssocs = new ArrayList<ChildAssociationRef>(100);
                nodeDAO.getParentAssocs(nodeId, null, null, null, new ChildAssocRefQueryCallback()
                {
                    @Override
                    public boolean preLoadNodes()
                    {
                        return false;
                    }
                    
                    @Override
                    public boolean orderResults()
                    {
                        return false;
                    }

                    @Override
                    public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair,
                            Pair<Long, NodeRef> parentNodePair, Pair<Long, NodeRef> childNodePair)
                    {
                        parentAssocs.add(childAssocPair.getSecond());
                        return true;
                    }

                    @Override
                    public void done()
                    {
                    }
                });
                
                CRC32 crc = new CRC32();
                for(ChildAssociationRef car : parentAssocs)
                {
                    try
                    {
                        crc.update(car.toString().getBytes("UTF-8"));
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        throw new RuntimeException("UTF-8 encoding is not supported");
                    }
                }
                nodeMetaData.setParentAssocs(parentAssocs, crc.getValue());
                        
                // TODO non-child associations
//                Collection<Pair<Long, AssociationRef>> sourceAssocs = nodeDAO.getSourceNodeAssocs(nodeId);
//                Collection<Pair<Long, AssociationRef>> targetAssocs = nodeDAO.getTargetNodeAssocs(nodeId);
//                
//                nodeMetaData.setAssocs();
            }
            
            if(includeOwner)
            {
                // cached in OwnableService
                nodeMetaData.setOwner(ownableService.getOwner(pair.getSecond()));
            }
 
            rowHandler.processResult(nodeMetaData);
        }
    }
   
    /**
     * {@inheritDoc}
     */
    public AlfrescoModel getModel(QName modelName)
    {
        if(enabled)
        {
            ModelDefinition modelDef = dictionaryService.getModel(modelName);
            return (modelDef != null ? new AlfrescoModel(modelDef) : null);
        }
        else
        {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<AlfrescoModelDiff> getModelDiffs(Map<QName, Long> models)
    {
        if(false == enabled)
        {
            return Collections.<AlfrescoModelDiff>emptyList();
        }
        
        List<AlfrescoModelDiff> diffs = new ArrayList<AlfrescoModelDiff>();

        // get all models the repository knows about and add each to a list with its checksum
        Collection<QName> allModels = dictionaryService.getAllModels();

        // look for changed and removed models
        for(QName modelName : models.keySet())
        {
            if(allModels.contains(modelName))
            {
                Long checksum = models.get(modelName);
                AlfrescoModel serverModel = getModel(modelName);
                if(serverModel.getChecksum() != checksum.longValue())
                {
                    // model has changed, add the changed server model
                    diffs.add(new AlfrescoModelDiff(modelName,
                            AlfrescoModelDiff.TYPE.CHANGED, checksum, serverModel.getChecksum()));
                }
            }
            else
            {
                // model no longer exists, just add it's name
            	diffs.add(new AlfrescoModelDiff(modelName,
                        AlfrescoModelDiff.TYPE.REMOVED, null, null));
            }
        }

        // look for new models
        for(QName modelName : allModels)
        {
            if(!models.containsKey(modelName))
            {
                // new model, add the model xml and checksum
                AlfrescoModel model = getModel(modelName);
                diffs.add(new AlfrescoModelDiff(modelName,
                        AlfrescoModelDiff.TYPE.NEW, null, model.getChecksum()));
            }
        }

//        for(AlfrescoModelDiff diff : diffs)
//        {
//            if(diff.getType() != TYPE.REMOVED)
//            {
//                CompiledModel cm = ((DictionaryDAOImpl)dictionaryDAO).getCompiledModel(QName.createQName(diff.getModelName()));
//                File file = TempFileProvider.createTempFile(cm.getM2Model().getChecksum(XMLBindingType.DEFAULT)+ cm.getM2Model().getNamespaces().get(0).getPrefix(), ".xml");
//                FileOutputStream os;
//                try
//                {
//                    os = new FileOutputStream(file);
//                    cm.getM2Model().toXML(os);
//                    os.flush();
//                    os.close();
//
//                }
//                catch (IOException e)
//                {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//
//        }
        
        return diffs;
    }
    
    /**
     * Class that passes results from a result entity into the client callback
     */
    protected class NodeQueryRowHandler
    {
        private final NodeQueryCallback callback;
        private boolean more;

        private NodeQueryRowHandler(NodeQueryCallback callback)
        {
            this.callback = callback;
            this.more = true;
        }
        
        public void processResult(Node row)
        {
            if (!more)
            {
                // No more results required
                return;
            }
            
            more = callback.handleNode(row);
        }
    }
    
    /**
     * Class that passes results from a result entity into the client callback
     */
    protected class NodeMetaDataQueryRowHandler
    {
        private final NodeMetaDataQueryCallback callback;
        private boolean more;

        private NodeMetaDataQueryRowHandler(NodeMetaDataQueryCallback callback)
        {
            this.callback = callback;
            this.more = true;
        }
        
        public void processResult(NodeMetaData row)
        {
            if (!more)
            {
                // No more results required
                return;
            }
            
            more = callback.handleNodeMetaData(row);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.solr.SOLRTrackingComponent#getLastTransactionTimestamp()
     */
    @Override
    public Long getMaxTxnCommitTime()
    {
        return nodeDAO.getMaxTxnCommitTime();
    }
}
