/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.search.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.ListBackedPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.Experimental;
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
import org.alfresco.service.cmr.search.CategoryServiceException;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ISO9075;
import org.alfresco.util.Pair;
import org.alfresco.util.collections.Function;
import org.apache.commons.collections.CollectionUtils;

/**
 * Category service implementation
 * 
 * @author andyh
 */
public abstract class AbstractCategoryServiceImpl implements CategoryService
{
    static final String CATEGORY_ROOT_NODE_NOT_FOUND = "Category root node not found";
    static final String NODE_WITH_CATEGORY_ROOT_TYPE_NOT_FOUND = "Node with category_root type not found";

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
    public AbstractCategoryServiceImpl()
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
    	return getChildren(categoryRef, mode, depth, false, (Collection<String>) null, queryFetchSize);
    }
    
    public Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth, String filter)
    {
    	return getChildren(categoryRef, mode, depth, false, filter, queryFetchSize);
    }

    private Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth, boolean sortByName, String filter, int fetchSize)
    {
        Collection<String> matchingFilter = Optional.ofNullable(filter).map(f -> "*".concat(f).concat("*")).map(Set::of).orElse(null);
        return getChildren(categoryRef, mode, depth, sortByName, matchingFilter, fetchSize);
    }

    private Collection<ChildAssociationRef> getChildren(NodeRef categoryRef, Mode mode, Depth depth, boolean sortByName, Collection<String> namesFilter, int fetchSize)
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
                luceneQuery.append("+TYPE:\"" + ContentModel.TYPE_CATEGORY + "\"");
                break;
            }
            if (CollectionUtils.isNotEmpty(namesFilter))
            {
                final StringJoiner filterJoiner = new StringJoiner(" OR ", " +(", ")");
                namesFilter.forEach(nameFilter -> filterJoiner.add("@cm\\:name:\"" + nameFilter + "\""));
                luceneQuery.append(filterJoiner);
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

    protected String getPrefix(String uri)
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

    protected Set<NodeRef> getClassificationNodes(StoreRef storeRef, QName aspectQName)
    {
        try
        {
            return getRootCategoryNodeRef(storeRef, aspectQName).stream().collect(Collectors.toSet());
        }
        catch (CategoryServiceException ignore)
        {
            return Collections.emptySet();
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
        return getRootCategories(storeRef, aspectName, pagingRequest, sortByName, null, null);
    }

    public PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName, String filter)
    {
        final Collection<String> alikeNamesFilter = Optional.ofNullable(filter).map(f -> "*".concat(f).concat("*")).map(Set::of).orElse(null);
        return getRootCategories(storeRef, aspectName, pagingRequest, sortByName, null, alikeNamesFilter);
    }

    public PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName,
        Collection<String> exactNamesFilter, Collection<String> alikeNamesFilter)
    {
        final Set<NodeRef> nodeRefs = getClassificationNodes(storeRef, aspectName);
        final List<ChildAssociationRef> associations = new LinkedList<>();
        final int skipCount = pagingRequest.getSkipCount();
        final int maxItems = pagingRequest.getMaxItems();
        final int size = (maxItems == CannedQueryPageDetails.DEFAULT_PAGE_SIZE ? CannedQueryPageDetails.DEFAULT_PAGE_SIZE : skipCount + maxItems);
        int count = 0;
        boolean moreItems = false;

        final Function<NodeRef, Collection<ChildAssociationRef>> childNodesSupplier = (nodeRef) -> {
            final Set<ChildAssociationRef> childNodes = new HashSet<>();
            if (CollectionUtils.isEmpty(exactNamesFilter) && CollectionUtils.isEmpty(alikeNamesFilter))
            {
                // lookup in DB without filtering
                childNodes.addAll(nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_SUBCATEGORIES, RegexQNamePattern.MATCH_ALL));
            }
            else
            {
                if (CollectionUtils.isNotEmpty(exactNamesFilter))
                {
                    // lookup in DB filtering by name
                    childNodes.addAll(nodeService.getChildrenByName(nodeRef, ContentModel.ASSOC_SUBCATEGORIES, exactNamesFilter));
                }
                if (CollectionUtils.isNotEmpty(alikeNamesFilter))
                {
                    // lookup using search engin filtering by name
                    childNodes.addAll(getChildren(nodeRef, Mode.SUB_CATEGORIES, Depth.IMMEDIATE, sortByName, alikeNamesFilter, skipCount + maxItems + 1));
                }
            }

            Stream<ChildAssociationRef> childNodesStream = childNodes.stream();
            if (sortByName)
            {
                childNodesStream = childNodesStream.sorted(Comparator.comparing(tag -> tag.getQName().getLocalName()));
            }
            return childNodesStream.collect(Collectors.toList());
        };

        OUTER_LOOP: for(NodeRef nodeRef : nodeRefs)
        {
            Collection<ChildAssociationRef> children = childNodesSupplier.apply(nodeRef);
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
                    break OUTER_LOOP;
                }

                associations.add(child);
            }
        }

        return new ListBackedPagingResults<>(associations, moreItems);
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

    public abstract List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count);

    /**
     * Creates search query parameters used to get top categories.
     * Can be used as a base both wih SOLR and ES.
     * @param storeRef Node store reference
     * @param aspectName Aspect name. "cm:generalclassifiable" aspect should be used for usual cases.
     *                   It is possible to use a custom aspect but it must have valid category property
     * @param count Will be used as faceted results limit, when system has very many categories this must be reflecting that number
     * @return SearchParameters to perform search for top categories.
     */
    protected SearchParameters createSearchTopCategoriesParameters(StoreRef storeRef, QName aspectName, int count) {
        final AspectDefinition aspectDefinition = dictionaryService.getAspect(aspectName);
        if(aspectDefinition == null)
        {
            throw new IllegalStateException("Unknown aspect");
        }
        final Map<QName, PropertyDefinition> aspectProperties = aspectDefinition.getProperties();
        final Optional<QName> catProperty = aspectProperties.entrySet().stream()
                //for backwards compatibility I'm leaving the part where we get custom category aspects
                .filter(ap -> ContentModel.ASPECT_GEN_CLASSIFIABLE.isMatch(aspectName) || isValidCategoryTypeProperty(aspectName, ap))
                .map(Map.Entry::getKey)
                .findFirst();

        return catProperty.map(cp -> {
            final String field = "@" + cp;
            final SearchParameters sp = new SearchParameters();
            sp.addStore(storeRef);
            sp.setQuery(cp + ":*");
            //we only care about faceted results and don't need query results so we can limit them to minimum
            sp.setMaxItems(1);
            sp.setSkipCount(0);
            final SearchParameters.FieldFacet ff = new SearchParameters.FieldFacet(field);
            ff.setLimitOrNull(count < 0 ? null : count);
            sp.addFieldFacet(ff);
            return sp;
        })
                .orElseThrow(() -> new IllegalStateException("Aspect does not have category property mirroring the aspect name"));
    }

    /**
     * Checks whether given aspect property definition is valid category property

     * @param aspectName Aspect name
     * @param propertyDef Aspect property definition.
     * @return is valid category property
     */
    private boolean isValidCategoryTypeProperty(QName aspectName, Map.Entry<QName, PropertyDefinition> propertyDef)
    {
        return propertyDef.getKey().getNamespaceURI().equals(aspectName.getNamespaceURI()) &&
                propertyDef.getKey().getLocalName().equals(aspectName.getLocalName()) &&
                DataTypeDefinition.CATEGORY.equals(propertyDef.getValue().getDataType().getName());
    }

    @Override
    @Experimental
    public Optional<NodeRef> getRootCategoryNodeRef(final StoreRef storeRef)
    {
        return getRootCategoryNodeRef(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE);
    }

    private Optional<NodeRef> getRootCategoryNodeRef(final StoreRef storeRef, final QName childNodeType)
    {
        final NodeRef rootNode = nodeService.getRootNode(storeRef);
        final ChildAssociationRef categoryRoot = nodeService.getChildAssocs(rootNode, Set.of(ContentModel.TYPE_CATEGORYROOT)).stream()
            .findFirst()
            .orElseThrow(() -> new CategoryServiceException(NODE_WITH_CATEGORY_ROOT_TYPE_NOT_FOUND));
        final List<ChildAssociationRef> categoryRootAssocs = nodeService.getChildAssocs(categoryRoot.getChildRef());
        if (CollectionUtils.isEmpty(categoryRootAssocs))
        {
            throw new CategoryServiceException(CATEGORY_ROOT_NODE_NOT_FOUND);
        }
        return categoryRootAssocs.stream()
            .filter(ca -> ca.getQName().equals(childNodeType))
            .map(ChildAssociationRef::getChildRef)
            .findFirst();
    }
}
