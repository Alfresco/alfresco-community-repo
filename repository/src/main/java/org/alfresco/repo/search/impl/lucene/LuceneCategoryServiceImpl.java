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
package org.alfresco.repo.search.impl.lucene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;

/**
 * Category service implementation
 * 
 * @author andyh
 */
public class LuceneCategoryServiceImpl implements CategoryService
{
    protected NodeService nodeService;
    
    protected NodeService publicNodeService;

    protected TenantService tenantService;

    protected NamespacePrefixResolver namespacePrefixResolver;

    protected DictionaryService dictionaryService;

    protected IndexerAndSearcher indexerAndSearcher;
    
    protected int queryFetchSize = 5000;

    /**
     * 
     */
    public LuceneCategoryServiceImpl()
    {
        super();
    }

    // Inversion of control support

    /**
     * Set the node service
     * 
     * @param nodeService NodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the public node service
     * 
     * @param publicNodeService NodeService
     */
    public void setPublicNodeService(NodeService publicNodeService)
    {
        this.publicNodeService = publicNodeService;
    }

    /**
     * Set the tenant service
     * 
     * @param tenantService TenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * Set the service to map prefixes to uris
     * 
     * @param namespacePrefixResolver NamespacePrefixResolver
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /**
     * Set the dictionary service
     * 
     * @param dictionaryService DictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the indexer and searcher
     * 
     * @param indexerAndSearcher IndexerAndSearcher
     */
    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }
    
    public void setQueryFetchSize(int queryFetchSize) {
		this.queryFetchSize = queryFetchSize;
	}

	public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth)
    {
    	return getChildren(categoryRef, mode, depth, false, null, queryFetchSize);
    }
    
    public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth, String filter)
    {
    	return getChildren(categoryRef, mode, depth, false, filter, queryFetchSize);
    }

    private Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth, boolean sortByName, String filter, int fetchSize)
    {
        if (categoryRef == null)
        {
            return Collections.<ChildAssociationRef> emptyList();
        }
        
        categoryRef = tenantService.getBaseName(categoryRef); // for solr
        
        ResultSet resultSet = null;
        try
        {
            StringBuilder luceneQuery = new StringBuilder(64);

            switch (mode)
            {
            case ALL:
                luceneQuery.append("PATH:\"");
                luceneQuery.append(buildXPath(nodeService.getPath(categoryRef))).append("/");
                if (depth.equals(Depth.ANY))
                {
                    luceneQuery.append("/");
                }
                luceneQuery.append("*").append("\" ");
                break;
            case MEMBERS:
                luceneQuery.append("PATH:\"");
                luceneQuery.append(buildXPath(nodeService.getPath(categoryRef))).append("/");
                if (depth.equals(Depth.ANY))
                {
                    luceneQuery.append("/");
                }
                luceneQuery.append("member").append("\" ");
                break;
            case SUB_CATEGORIES:
                luceneQuery.append("+PATH:\"");
                luceneQuery.append(buildXPath(nodeService.getPath(categoryRef))).append("/");
                if (depth.equals(Depth.ANY))
                {
                    luceneQuery.append("/");
                }
                luceneQuery.append("*").append("\" ");
                luceneQuery.append("+TYPE:\"" + ContentModel.TYPE_CATEGORY.toString() + "\"");
                break;
            }
            if (filter != null)
            {
                luceneQuery.append(" " + "+@cm\\:name:\"*" + filter + "*\"");
            }

            // Get a searcher that will include Categories added in this transaction
            SearchService searcher = indexerAndSearcher.getSearcher(categoryRef.getStoreRef(), true);
            
            // Perform the search
            SearchParameters searchParameters = new SearchParameters();
            resultSet = searcher.query(categoryRef.getStoreRef(), "lucene", luceneQuery.toString(), null);
            searchParameters.setLanguage("lucene");
            if(sortByName)
            {
            	searchParameters.addSort("@" + ContentModel.PROP_NAME, true);
            }
            searchParameters.setQuery(luceneQuery.toString());
            searchParameters.setLimit(-1);
            searchParameters.setMaxItems(fetchSize);
            searchParameters.setLimitBy(LimitBy.FINAL_SIZE);
            searchParameters.addStore(categoryRef.getStoreRef());
            resultSet = searcher.query(searchParameters);

            // Convert from search results to the required Child Assocs
            return resultSetToChildAssocCollection(resultSet);
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    private String buildXPath(Path path)
    {
        StringBuilder pathBuffer = new StringBuilder(64);
        for (Iterator<Path.Element> elit = path.iterator(); elit.hasNext(); /**/)
        {
            Path.Element element = elit.next();
            if (!(element instanceof Path.ChildAssocElement))
            {
                throw new IndexerException("Confused path: " + path);
            }
            Path.ChildAssocElement cae = (Path.ChildAssocElement) element;
            if (cae.getRef().getParentRef() != null)
            {
                pathBuffer.append("/");
                pathBuffer.append(getPrefix(cae.getRef().getQName().getNamespaceURI()));
                pathBuffer.append(ISO9075.encode(cae.getRef().getQName().getLocalName()));
            }
        }
        return pathBuffer.toString();
    }

    HashMap<String, String> prefixLookup = new HashMap<String, String>();

    private String getPrefix(String uri)
    {
        String prefix = prefixLookup.get(uri);
        if (prefix == null)
        {
            Collection<String> prefixes = namespacePrefixResolver.getPrefixes(uri);
            for (String first : prefixes)
            {
                prefix = first;
                break;
            }

            prefixLookup.put(uri, prefix);
        }
        if (prefix == null)
        {
            return "";
        }
        else
        {
            return prefix + ":";
        }

    }

    private Collection<ChildAssociationRef> resultSetToChildAssocCollection(ResultSet resultSet)
    {
        List<ChildAssociationRef> collection = new LinkedList<ChildAssociationRef>();
        if (resultSet != null)
        {
            for (ResultSetRow row : resultSet)
            {
                try
                {
                    ChildAssociationRef car = nodeService.getPrimaryParent(row.getNodeRef());
                    collection.add(car);
                }
                catch(InvalidNodeRefException inre)
                {
                    // keep going the node has gone beneath us just skip it
                }
            }
        }
        return collection;
        // The caller closes the result set
    }

    public Collection<ChildAssociationRef> getCategories(StoreRef storeRef, QName aspectQName, Depth depth)
    {
        Collection<ChildAssociationRef> assocs = new LinkedList<ChildAssociationRef>();
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectQName);
        for (NodeRef nodeRef : nodeRefs)
        {
            assocs.addAll(getChildren(nodeRef, Mode.SUB_CATEGORIES, depth));
        }
        return assocs;
    }

    private Set<NodeRef> getClassificationNodes(StoreRef storeRef, QName qname)
    {
        ResultSet resultSet = null;
        try
        {
            resultSet = indexerAndSearcher.getSearcher(storeRef, false).query(storeRef, "lucene",
                    "PATH:\"/" + getPrefix(qname.getNamespaceURI()) + ISO9075.encode(qname.getLocalName()) + "\"", null);
            
            Set<NodeRef> nodeRefs = new HashSet<NodeRef>(resultSet.length());
            for (ResultSetRow row : resultSet)
            {
                nodeRefs.add(row.getNodeRef());
            }
            
            return nodeRefs;
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    public Collection<ChildAssociationRef> getClassifications(StoreRef storeRef)
    {
        ResultSet resultSet = null;
        try
        {
            resultSet = indexerAndSearcher.getSearcher(storeRef, false).query(storeRef, "lucene", "PATH:\"//cm:categoryRoot/*\"", null);
            return resultSetToChildAssocCollection(resultSet);
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    public Collection<QName> getClassificationAspects()
    {
        return dictionaryService.getSubAspects(ContentModel.ASPECT_CLASSIFIABLE, true);
    }

    public NodeRef createClassification(StoreRef storeRef, QName typeName, String attributeName)
    {
        throw new UnsupportedOperationException();
    }

    public PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName)
    {
        return getRootCategories(storeRef, aspectName, pagingRequest, sortByName, null);
    }

    public PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName, String filter)
    {
        final List<ChildAssociationRef> assocs = new LinkedList<ChildAssociationRef>();
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);

        final int skipCount = pagingRequest.getSkipCount();
        final int maxItems = pagingRequest.getMaxItems();
        final int size = (maxItems == CannedQueryPageDetails.DEFAULT_PAGE_SIZE ? CannedQueryPageDetails.DEFAULT_PAGE_SIZE : skipCount + maxItems);
        int count = 0;
        boolean moreItems = false;

        OUTER: for(NodeRef nodeRef : nodeRefs)
        {
        	Collection<ChildAssociationRef> children = getChildren(nodeRef, Mode.SUB_CATEGORIES, Depth.IMMEDIATE, sortByName, filter, skipCount + maxItems + 1);
        	for(ChildAssociationRef child : children)
        	{
        		count++;

	            if(count <= skipCount)
	            {
	            	continue;
	            }
	            
	            if(count > size)
	            {
	            	moreItems = true;
	            	break OUTER;
	            }

	            assocs.add(child);
        	}
        }
        
        final boolean hasMoreItems = moreItems;
        return new PagingResults<ChildAssociationRef>()
        {
			@Override
			public List<ChildAssociationRef> getPage()
			{
				return assocs;
			}

			@Override
			public boolean hasMoreItems()
			{
				return hasMoreItems;
			}

			@Override
			public Pair<Integer, Integer> getTotalResultCount()
			{
				return new Pair<Integer, Integer>(null, null);
			}

			@Override
			public String getQueryExecutionId()
			{
				return null;
			}
        };
    }

    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName)
    {
        return getRootCategories(storeRef, aspectName, null);
    }

    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, String filter)
    {
        Collection<ChildAssociationRef> assocs = new LinkedList<ChildAssociationRef>();
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        for (NodeRef nodeRef : nodeRefs)
        {
            assocs.addAll(getChildren(nodeRef, Mode.SUB_CATEGORIES, Depth.IMMEDIATE, false, filter, queryFetchSize));
        }
        return assocs;
    }

    public ChildAssociationRef getCategory(NodeRef parent, QName aspectName, String name)
    {
        String uri = nodeService.getPrimaryParent(parent).getQName().getNamespaceURI();
        String validLocalName = QName.createValidLocalName(name);
        Collection<ChildAssociationRef> assocs = nodeService.getChildAssocs(parent, ContentModel.ASSOC_SUBCATEGORIES,
                QName.createQName(uri, validLocalName), false);
        if (assocs.isEmpty())
        {
            return null;
        }
        return assocs.iterator().next();
    }

    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, String name,
            boolean create)
    {
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        if (nodeRefs.isEmpty())
        {
            return Collections.emptySet();
        }
        Collection<ChildAssociationRef> assocs = new LinkedList<ChildAssociationRef>();
        for (NodeRef nodeRef : nodeRefs)
        {
            ChildAssociationRef category = getCategory(nodeRef, aspectName, name);
            if (category != null)
            {
                assocs.add(category);
            }
        }
        if (create && assocs.isEmpty())
        {
            assocs.add(createCategoryInternal(nodeRefs.iterator().next(), name));
        }
        return assocs;
    }
    
    public NodeRef createCategory(NodeRef parent, String name)
    {
        return createCategoryInternal(parent, name).getChildRef();
    }

    private ChildAssociationRef createCategoryInternal(NodeRef parent, String name)
    {
        if (!nodeService.exists(parent))
        {
            throw new AlfrescoRuntimeException("Missing category?");
        }
        String uri = nodeService.getPrimaryParent(parent).getQName().getNamespaceURI();
        String validLocalName = QName.createValidLocalName(name);
        ChildAssociationRef newCategory = publicNodeService.createNode(parent, ContentModel.ASSOC_SUBCATEGORIES, QName.createQName(uri, validLocalName), ContentModel.TYPE_CATEGORY);
        publicNodeService.setProperty(newCategory.getChildRef(), ContentModel.PROP_NAME, name);
        return newCategory;
    }

    public NodeRef createRootCategory(StoreRef storeRef, QName aspectName, String name)
    {
        Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        if (nodeRefs.size() == 0)
        {
            throw new AlfrescoRuntimeException("Missing classification: " + aspectName);
        }
        NodeRef parent = nodeRefs.iterator().next();
        return createCategory(parent, name);
    }

    public void deleteCategory(NodeRef nodeRef)
    {
        publicNodeService.deleteNode(nodeRef);
    }

    public void deleteClassification(StoreRef storeRef, QName aspectName)
    {
        throw new UnsupportedOperationException();
    }

    public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count)
    {
        throw new UnsupportedOperationException();
    }

}
