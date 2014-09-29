/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.CRC32;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.ChildAssocRefQueryCallback;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.solr.SOLRDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
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
    private NamespaceService namespaceService;
    private boolean enabled = true;
    private boolean cacheAncestors =true;
    private boolean ignorePathsForSpecificTypes = false;
    private Set<QName> typesForIgnoringPaths = new HashSet<QName>();
    private List<String> typesForIgnoringPathsString;
    
    
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

    public boolean isIgnorePathsForSpecificTypes()
    {
        return ignorePathsForSpecificTypes;
    }

    public void setIgnorePathsForSpecificTypes(boolean ignorePersonAndConfigurationPaths)
    {
        this.ignorePathsForSpecificTypes = ignorePersonAndConfigurationPaths;
    }

    public void setTypesForIgnoringPaths(List<String> typesForIgnoringPaths)
    {
        typesForIgnoringPathsString = typesForIgnoringPaths;
    }

    /**
     * @param cacheAncestors the cacheAncestors to set
     */
    public void setCacheAncestors(boolean cacheAncestors)
    {
        this.cacheAncestors = cacheAncestors;
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

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
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

        if ((null != typesForIgnoringPathsString) && (null != namespaceService))
        {
            for (String typeQName : typesForIgnoringPathsString)
            {
                if ((null != typeQName) && !typeQName.isEmpty())
                {
                    try
                    {
                        QName type = QName.resolveToQName(namespaceService, typeQName);

                        if (null != dictionaryService.getType(type))
                        {
                            this.typesForIgnoringPaths.add(type);
                        }
                    }
                    catch (InvalidQNameException e)
                    {
                        // Just ignore
                    }
                }
            }
        }
    }
    
    @Override
    public List<AclChangeSet> getAclChangeSets(Long minAclChangeSetId, Long fromCommitTime, Long maxAclChangeSetId, Long toCommitTime, int maxResults)
    {
        if(enabled)
        {
            List<AclChangeSet> changesets = solrDAO.getAclChangeSets(minAclChangeSetId, fromCommitTime, maxAclChangeSetId, toCommitTime, maxResults);
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
            // We don't want the caches to lie and we may not be part of the cluster
            aclDAO.setCheckAclConsistency();

            /*
             * This is an N+1 query that should, in theory, make use of cached ACL readers data.
             */

            Map<Long, String> aclChangeSetTenant = new HashMap<Long, String>(aclIds.size());
            
            List<AclReaders> aclsReaders = new ArrayList<AclReaders>(aclIds.size() * 10);
            for (Long aclId : aclIds)
            {
                AclReaders readers = new AclReaders();
                readers.setAclId(aclId);
                Set<String> readersSet = permissionService.getReaders(aclId);
                readers.setReaders(readersSet);
                Set<String> deniedSet = permissionService.getReadersDenied(aclId);
                readers.setDenied(deniedSet);
                
                Long aclChangeSetId = aclDAO.getAccessControlList(aclId).getProperties().getAclChangeSetId();
                readers.setAclChangeSetId(aclChangeSetId);
                
                if (AuthenticationUtil.isMtEnabled())
                {
                	// MT - for now, derive the tenant for acl (via acl change set)
                    String tenantDomain = aclChangeSetTenant.get(aclChangeSetId);
                    if (tenantDomain == null)
                    {
                        tenantDomain = getTenant(aclId, aclChangeSetId);
                        if (tenantDomain == null)
                        {
                            // skip this acl !
                            continue;
                        }
                        aclChangeSetTenant.put(aclChangeSetId, tenantDomain);
                    }
                    readers.setTenantDomain(tenantDomain);
                }
                
                aclsReaders.add(readers);
            }
            
            return aclsReaders;
        }
        else
        {
            return Collections.<AclReaders>emptyList();
        }
    }
    
    private String getTenant(long aclId, long aclChangeSetId)
    {
        String tenantDomain = getAclTenant(aclId);
        if (tenantDomain == null)
        {
            List<Long> aclChangeSetIds = new ArrayList<Long>(1);
            aclChangeSetIds.add(aclChangeSetId);
            
            List<Acl> acls = solrDAO.getAcls(aclChangeSetIds, null, 1024);
            for (Acl acl : acls)
            {
                tenantDomain = getAclTenant(acl.getId());
                if (tenantDomain != null)
                {
                    break;
                }
            }
            
            if (tenantDomain == null)
            {
                // tenant not found - log warning ?
                tenantDomain = null; // temp - for debug breakpoint only
            }
        }
        return tenantDomain;
    }
    
    private String getAclTenant(long aclId)
    {
        List<Long> nodeIds = aclDAO.getADMNodesByAcl(aclId, 1);
        if (nodeIds.size() == 0)
        {
            return null;
        }
        
        nodeDAO.setCheckNodeConsistency();
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeIds.get(0));
        if (nodePair == null)
        {
            return null;
        }
        
        return tenantService.getDomain(nodePair.getSecond().getStoreRef().getIdentifier());
    }
    
    @Override
    public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, Long maxTxnId, Long toCommitTime, int maxResults)
    {
        if(enabled)
        {
            List<Transaction> txns = solrDAO.getTransactions(minTxnId, fromCommitTime, maxTxnId, toCommitTime, maxResults);
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
    
    static class CategoryPaths
    {
        Collection<Pair<Path, QName>> paths;
        List<ChildAssociationRef> categoryParents;
        
        CategoryPaths( Collection<Pair<Path, QName>> paths, List<ChildAssociationRef> categoryParents)
        {
            this.paths = paths;
            this.categoryParents = categoryParents;
        }

        /**
         * @return the paths
         */
        public Collection<Pair<Path, QName>> getPaths()
        {
            return paths;
        }

        /**
         * @return the categoryParents
         */
        public List<ChildAssociationRef> getCategoryParents()
        {
            return categoryParents;
        }
        
        
    }
    
    private CategoryPaths getCategoryPaths(NodeRef nodeRef, Set<QName> aspects, Map<QName, Serializable> properties)
    {
        ArrayList<Pair<Path, QName>> categoryPaths = new ArrayList<Pair<Path, QName>>();
        ArrayList<ChildAssociationRef> categoryParents = new ArrayList<ChildAssociationRef>();

        nodeDAO.setCheckNodeConsistency();
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
                        if(pair != null)
                        {
                            for (Path path : nodeDAO.getPaths(pair, false))
                            {
                                aspectPaths.add(new Pair<Path, QName>(path, aspDef.getName()));   
                            }
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
                ChildAssociationRef categoryParentRef = new ChildAssociationRef(assocRef.getTypeQName(), assocRef.getChildRef(), QName.createQName("member"), nodeRef);
                pair.getFirst().append(new Path.ChildAssocElement(categoryParentRef));
                categoryParents.add(categoryParentRef);
            }
        }

        return new CategoryPaths(categoryPaths, categoryParents);
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
        
        // Pre-evaluate ancestors so we can bulk load them
        List<Long> ancestors;
        if(cacheAncestors)
        {
            ancestors = cacheAncestors(nodeIds);
        }
        else
        {
            ancestors = nodeIds;
        }
        // Ensure that we get fresh node references
        nodeDAO.setCheckNodeConsistency();
        // bulk load nodes and their ancestors      
        nodeDAO.cacheNodesById(ancestors);
        
        return nodeIds;
    }
    
    /**
     * Does a 'breadth first' search of ancestors, caching as it goes
     * @param nodeIds initial list of nodes to visit
     * @return all visited nodes, in no particular order
     */
    private List<Long> cacheAncestors(List<Long> nodeIds)
    {
        final LinkedList<Long> toVisit = new LinkedList<Long>(nodeIds);
        Set<Long> visited = new TreeSet<Long>();
        Long nodeId;
        nodeDAO.cacheNodesById(toVisit);
        Long lastCached = toVisit.peekLast();
        while ((nodeId = toVisit.pollFirst()) != null)
        {
            if (visited.add(nodeId) && (nodeDAO.getNodeIdStatus(nodeId) != null) && (false == nodeDAO.getNodeIdStatus(nodeId).isDeleted()))
            {
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
                        toVisit.add(parentNodePair.getFirst());
                        return true;
                    }

                    @Override
                    public void done()
                    {
                    }
                });
            }
            final boolean nodeIdEqualsLastCached = (nodeId == null && lastCached == null) ||
                                                    nodeId.equals(lastCached);
            if (nodeIdEqualsLastCached && !toVisit.isEmpty())
            {
                nodeDAO.cacheNodesById(toVisit);
                lastCached = toVisit.peekLast();
            }
        }
        return new ArrayList<Long>(visited);
    }    


    protected Map<QName, Serializable> getProperties(Long nodeId)
    {
        Map<QName, Serializable> props = null;

        // ALF-10641
        // Residual properties are un-indexed -> break serlialisation
        nodeDAO.setCheckNodeConsistency();
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
            Status status = nodeDAO.getNodeIdStatus(nodeId);
            if (status == null)
            {
                // We've been called with the ID of a purged node, probably due to processing a transaction with a
                // cascading delete. Fine to skip and assume it will be processed in a transaction.
                // See org.alfresco.solr.tracker.CoreTracker.updateDescendantAuxDocs(NodeMetaData, boolean, SolrIndexSearcher)
                continue;
            }
            NodeRef nodeRef = status.getNodeRef();
          
            NodeMetaData nodeMetaData = new NodeMetaData();
            nodeMetaData.setNodeId(nodeId);
  
            if(includeNodeRef)
            {
                nodeMetaData.setNodeRef(tenantService.getBaseName(nodeRef, true));
            }
            
            if(includeTxnId)
            {
                nodeMetaData.setTxnId(status.getDbTxnId());
            }
            
            if(status.isDeleted())
            {
                rowHandler.processResult(nodeMetaData);
                continue;
            }
            
            Map<QName, Serializable> props = null;
            Set<QName> aspects = null;
        
            nodeMetaData.setAclId(nodeDAO.getNodeAclId(nodeId));
            
            if(includeType)
            {
                QName nodeType = getNodeType(nodeId);
                if(nodeType != null)
                {
                    nodeMetaData.setNodeType(nodeType);
                }
                else
                {
                   throw new AlfrescoRuntimeException("Nodes with no type are ignored by SOLR");
                }
            }

            if(includeProperties)
            {
                if(props == null)
                {
                    props = getProperties(nodeId);
                }
                nodeMetaData.setProperties(props);
            }
            else
            {
                nodeMetaData.setProperties(Collections.<QName, Serializable>emptyMap());
            }

            if(includeAspects || includePaths || includeParentAssociations)
            {
                aspects = new HashSet<QName>();
                Set<QName> sourceAspects = nodeDAO.getNodeAspects(nodeId);
                for(QName aspectQName : sourceAspects)
                {
                    AspectDefinition aspect = dictionaryService.getAspect(aspectQName);
                    if(aspect != null)
                    {
                        aspects.add(aspectQName);
                    }
                }
            }
            nodeMetaData.setAspects(aspects);

            boolean ignoreLargeMetadata = ignorePathsForSpecificTypes && shouldTypeBeIgnored(getNodeType(nodeId));
            if (!ignoreLargeMetadata && ignorePathsForSpecificTypes)
            {
                final List<Long> parentIds = new LinkedList<Long>();
                nodeDAO.getParentAssocs(nodeId, null, null, true, new ChildAssocRefQueryCallback()
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
                    public boolean handle(Pair<Long, ChildAssociationRef> childAssocPair, Pair<Long, NodeRef> parentNodePair, Pair<Long, NodeRef> childNodePair)
                    {
                        parentIds.add(parentNodePair.getFirst());
                        return false;
                    }

                    @Override
                    public void done()
                    {
                    }
                });

                QName parentType = (!parentIds.isEmpty()) ? (getNodeType(parentIds.iterator().next())) : (null);
                ignoreLargeMetadata = shouldTypeBeIgnored(parentType);
            }

            CategoryPaths categoryPaths = new CategoryPaths(new ArrayList<Pair<Path, QName>>(), new ArrayList<ChildAssociationRef>());
            if(!ignoreLargeMetadata && (includePaths || includeParentAssociations))
            {
                if(props == null)
                {
                    props = getProperties(nodeId);
                }
                categoryPaths = getCategoryPaths(status.getNodeRef(), aspects, props);
            }

            if (includePaths && !ignoreLargeMetadata)
            {
                if (props == null)
                {
                    props = getProperties(nodeId);
                }

                List<Path> directPaths = nodeDAO.getPaths(new Pair<Long, NodeRef>(nodeId, status.getNodeRef()), false);

                Collection<Pair<Path, QName>> paths = new ArrayList<Pair<Path, QName>>(directPaths.size() + categoryPaths.getPaths().size());
                for (Path path : directPaths)
                {
                    paths.add(new Pair<Path, QName>(path.getBaseNamePath(tenantService), null));
                }
                for (Pair<Path, QName> catPair : categoryPaths.getPaths())
                {
                    paths.add(new Pair<Path, QName>(catPair.getFirst().getBaseNamePath(tenantService), catPair.getSecond()));
                }

                nodeMetaData.setPaths(paths);
                
                // Calculate name path
                Collection<Collection<String>> namePaths = new ArrayList<Collection<String>>(2);
                nodeMetaData.setNamePaths(namePaths);
                for (Pair<Path, QName>  catPair : paths)
                {
                    Path path = catPair.getFirst();
                    
                    boolean added = false;
                    List<String> namePath = new ArrayList<String>(path.size());
                    NEXT_ELEMENT: for (Path.Element pathElement : path)
                    {
                        if (!(pathElement instanceof ChildAssocElement))
                        {
                            // This is some path element that is terminal to a cm:name path
                            break;
                        }
                        ChildAssocElement pathChildAssocElement = (ChildAssocElement) pathElement;
                        NodeRef childNodeRef = pathChildAssocElement.getRef().getChildRef();
                        Pair<Long, NodeRef> childNodePair = nodeDAO.getNodePair(childNodeRef);
                        if (childNodePair == null)
                        {
                            // Gone
                            break;
                        }
                        Long childNodeId = childNodePair.getFirst();
                        String childNodeName = (String) nodeDAO.getNodeProperty(childNodeId, ContentModel.PROP_NAME);
                        if (childNodeName == null)
                        {
                            // We have hit a non-name node, which acts as a root for cm:name
                            // DH: There is no particular constraint here.  This is just a decision made.
                            namePath.clear();
                            // We have to continue down the path as there could be a name path lower down
                            continue NEXT_ELEMENT;
                        }
                        // We can finally add the name to the path
                        namePath.add(childNodeName);
                        // Add the path if this is the first entry in the name path
                        if (!added)
                        {
                            namePaths.add(namePath);
                            added = true;
                        }
                    }
                }
                
            }
         
            nodeMetaData.setTenantDomain(tenantService.getDomain(nodeRef.getStoreRef().getIdentifier()));
            
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
                        boolean addCurrentChildAssoc = true;
                        if (ignorePathsForSpecificTypes)
                        {
                            QName nodeType = nodeDAO.getNodeType(childNodePair.getFirst());
                            addCurrentChildAssoc = !shouldTypeBeIgnored(nodeType);
                        }
                        if (addCurrentChildAssoc)
                        {
                            childAssocs.add(tenantService.getBaseName(childAssocPair.getSecond(), true));
                        }
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
                        boolean addCurrentId = true;
                        if (ignorePathsForSpecificTypes)
                        {
                            QName nodeType = nodeDAO.getNodeType(childNodePair.getFirst());
                            addCurrentId = !shouldTypeBeIgnored(nodeType);
                        }
                        if (addCurrentId)
                        {
                            childIds.add(childNodePair.getFirst());
                        }
                        return true;
                    }
                    
                    @Override
                    public void done()
                    {
                    }
                });
                nodeMetaData.setChildIds(childIds);
            }
            
            if(includeParentAssociations && !ignoreLargeMetadata)
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
                        parentAssocs.add(tenantService.getBaseName(childAssocPair.getSecond(), true));
                        return true;
                    }

                    @Override
                    public void done()
                    {
                    }
                });
                for(ChildAssociationRef ref : categoryPaths.getCategoryParents())
                {
                    parentAssocs.add(tenantService.getBaseName(ref, true));
                }
                
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
                nodeMetaData.setOwner(ownableService.getOwner(status.getNodeRef()));
            }
 
            rowHandler.processResult(nodeMetaData);
        }
    }

    private QName getNodeType(Long nodeId)
    {
        QName result = nodeDAO.getNodeType(nodeId);
        TypeDefinition type = dictionaryService.getType(result);
        return (null == type) ? (null) : (result);
    }

    private boolean shouldTypeBeIgnored(QName nodeType)
    {
        if (null != nodeType)
        {
            if (typesForIgnoringPaths.contains(nodeType))
            {
                return true;
            }

            for (QName type : typesForIgnoringPaths)
            {
                if (dictionaryService.isSubClass(nodeType, type))
                {
                    return true;
                }
            }
        }

        return false;
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

    @Override
    public Long getMaxTxnCommitTime()
    {
        nodeDAO.setCheckNodeConsistency();
        return nodeDAO.getMaxTxnCommitTime();
    }
    
    @Override
    public Long getMaxTxnId()
    {
        long maxCommitTime = System.currentTimeMillis()+1L;
        nodeDAO.setCheckNodeConsistency();
        return nodeDAO.getMaxTxnIdByCommitTime(maxCommitTime);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.solr.SOLRTrackingComponent#getMaxChangeSetCommitTime()
     */
    @Override
    public Long getMaxChangeSetCommitTime()
    {
        return aclDAO.getMaxChangeSetCommitTime();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.solr.SOLRTrackingComponent#getMaxChangeSetId()
     */
    @Override
    public Long getMaxChangeSetId()
    {
        long maxCommitTime = System.currentTimeMillis()+1L;
        return aclDAO.getMaxChangeSetIdByCommitTime(maxCommitTime);
    }
}
