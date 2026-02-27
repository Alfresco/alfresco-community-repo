/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static org.alfresco.model.ContentModel.ASPECT_GEN_CLASSIFIABLE;
import static org.alfresco.model.ContentModel.ASPECT_TAGGABLE;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_FTS_ALFRESCO;
import static org.alfresco.util.ParameterCheck.mandatory;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.ListBackedPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.AbstractCategoryServiceImpl;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;

/**
 * Tag and category support using Elasticsearch.
 */
public class ElasticsearchCategoryService extends AbstractCategoryServiceImpl
{
    /** The root node for all tags and categories. */
    private static final String CM_CATEGORY_ROOT = "cm:categoryRoot";
    private TagSupport tagSupport;

    public ElasticsearchCategoryService(NodeService publicNodeService, IndexerAndSearcher indexerAndSearcher, NodeService nodeService)
    {
        this.publicNodeService = Objects.requireNonNull(publicNodeService);
        this.indexerAndSearcher = Objects.requireNonNull(indexerAndSearcher);
        this.nodeService = Objects.requireNonNull(nodeService);

        tagSupport = new TagSupport(publicNodeService, indexerAndSearcher, nodeService);
    }

    public void setQueryFetchSize(int queryFetchSize)
    {
        tagSupport.setQueryFetchSize(queryFetchSize);
    }

    @Override
    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName)
    {
        mandatory("storeRef", storeRef);
        mandatory("aspectName", aspectName);

        if (aspectName.equals(ASPECT_TAGGABLE))
        {
            return tagSupport.findAllTags(storeRef, (String) null, false);
        }
        else if (aspectName.equals(ASPECT_GEN_CLASSIFIABLE))
        {
            return getRootCategoryNodeRef(storeRef).stream()
                    .flatMap(node -> publicNodeService.getChildAssocs(node).stream())
                    .collect(toList());
        }
        throw new UnsupportedOperationException("Finding the category root for " + aspectName + " is currently not supported.");
    }

    /**
     * Create a collection of parent-child associations from the root node to the top level categories.
     *
     * @param storeRef
     *            The store to load the associations from.
     * @param aspectName
     *            The aspect that the categories should have.
     * @param nameFilter
     *            The filter to use to restrict the output, or null if no filtering is required.
     * @param nameSortComparator
     *            The comparator to use to sort the output, or null if no sorting is required.
     * @param collector
     *            The collector to use to create the output collection.
     * @return The collection of root categories.
     */
    private <T extends Collection<ChildAssociationRef>> T collectRootCategories(StoreRef storeRef,
            QName aspectName,
            Predicate<String> nameFilter,
            Comparator<Pair<String, ChildAssociationRef>> nameSortComparator,
            Collector<ChildAssociationRef, ?, T> collector)
    {
        Stream<Pair<String, ChildAssociationRef>> stream = getRootCategories(storeRef, aspectName).stream()
                .map(assoc -> new Pair<>((String) nodeService.getProperty(assoc.getChildRef(), PROP_NAME), assoc))
                .filter(pair -> nameFilter == null || nameFilter.test(pair.getFirst()));
        if (nameSortComparator != null)
        {
            stream = stream.sorted(nameSortComparator);
        }
        return stream.map(Pair::getSecond)
                .collect(collector);
    }

    @Override
    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, String filter)
    {
        mandatory("storeRef", storeRef);
        mandatory("aspectName", aspectName);

        if (aspectName.equals(ASPECT_TAGGABLE))
        {
            return tagSupport.findAllTags(storeRef, filter, false);
        }
        else if (aspectName.equals(ASPECT_GEN_CLASSIFIABLE))
        {
            Predicate<String> nameFilter = (filter == null ? null : ((name) -> name.contains(filter)));
            return collectRootCategories(storeRef, aspectName, nameFilter, null, Collectors.toSet());
        }
        throw new UnsupportedOperationException("Finding the category root for " + aspectName + " is currently not supported.");
    }

    @Override
    public Collection<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, String name, boolean create)
    {
        mandatory("storeRef", storeRef);
        mandatory("aspectName", aspectName);
        mandatory("name", name);

        if (aspectName.equals(ASPECT_TAGGABLE))
        {
            final Optional<ChildAssociationRef> existing = tagSupport.findExistingTag(storeRef, name);
            if (existing.isPresent())
            {
                return List.of(existing.get());
            }
            if (create)
            {
                return List.of(tagSupport.createTag(storeRef, name));
            }
            return List.of();
        }
        else if (aspectName.equals(ASPECT_GEN_CLASSIFIABLE))
        {

            Collection<ChildAssociationRef> rootCategories = collectRootCategories(storeRef, aspectName, element -> element.equals(name), null, toSet());
            if (rootCategories.isEmpty() && create)
            {
                NodeRef rootCategory = createRootCategory(storeRef, aspectName, name);
                return nodeService.getParentAssocs(rootCategory);
            }
            return rootCategories;
        }
        throw new UnsupportedOperationException("Finding the category root for " + aspectName + " is currently not supported.");
    }

    @Override
    public PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName)
    {
        return getRootCategories(storeRef, aspectName, pagingRequest, sortByName, null);
    }

    @Override
    public PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName, String filter)
    {
        final Collection<String> alikeNamesFilter = Optional.ofNullable(filter).map(f -> "*" + f + "*").map(Set::of).orElse(null);
        return getRootCategories(storeRef, aspectName, pagingRequest, sortByName, null, alikeNamesFilter);
    }

    @Override
    public PagingResults<ChildAssociationRef> getRootCategories(StoreRef storeRef, QName aspectName, PagingRequest pagingRequest, boolean sortByName,
            Collection<String> exactNamesFilter, Collection<String> alikeNamesFilter)
    {
        mandatory("storeRef", storeRef);
        mandatory("aspectName", aspectName);

        if (aspectName.equals(ASPECT_TAGGABLE))
        {
            final Set<ChildAssociationRef> allTags = new HashSet<>();
            if (CollectionUtils.isNotEmpty(exactNamesFilter) || CollectionUtils.isEmpty(alikeNamesFilter))
            {
                // lookup tags in DB
                allTags.addAll(tagSupport.findAllTags(storeRef, exactNamesFilter));
            }
            if (CollectionUtils.isNotEmpty(alikeNamesFilter))
            {
                // lookup tags using search engine
                allTags.addAll(tagSupport.findAllTags(storeRef, alikeNamesFilter, sortByName));
            }

            Stream<ChildAssociationRef> tagsStream = allTags.stream();
            if (sortByName)
            {
                tagsStream = tagsStream.sorted(Comparator.comparing(tag -> tag.getQName().getLocalName()));
            }
            return new ListBackedPagingResults<>(tagsStream.collect(toList()), pagingRequest);
        }
        else if (aspectName.equals(ASPECT_GEN_CLASSIFIABLE))
        {
            Predicate<String> nameFilter = getNameFilter(exactNamesFilter, alikeNamesFilter);
            Comparator<Pair<String, ChildAssociationRef>> nameSortComparator = sortByName ? Comparator.comparing(Pair::getFirst) : null;
            List<ChildAssociationRef> rootCategoryList = collectRootCategories(storeRef, aspectName, nameFilter, nameSortComparator, toList());
            return new ListBackedPagingResults<>(rootCategoryList, pagingRequest);
        }
        throw new UnsupportedOperationException("Finding the category root for " + aspectName + " is currently not supported.");
    }

    @Override
    public void deleteCategory(NodeRef nodeRef)
    {
        publicNodeService.deleteNode(nodeRef);
    }

    @Override
    public List<Pair<NodeRef, Integer>> getTopCategories(StoreRef storeRef, QName aspectName, int count)
    {
        final SearchParameters searchParameters = createSearchTopCategoriesParameters(storeRef, aspectName, count);
        searchParameters.setLanguage(LANGUAGE_FTS_ALFRESCO);

        final String field = searchParameters.getFieldFacets().stream()
                .map(SearchParameters.FieldFacet::getField)
                .findFirst()
                .orElse("");

        ResultSet resultSet = null;
        try
        {
            resultSet = indexerAndSearcher.getSearcher(storeRef, false).query(searchParameters);
            final List<Pair<String, Integer>> facetCounts = resultSet.getFieldFacet(field);
            return facetCounts.stream()
                    .map(fc -> new Pair<>(new NodeRef(storeRef, fc.getFirst()), fc.getSecond()))
                    .filter(pair -> nodeService.exists(pair.getFirst()))
                    .collect(toList());
        }
        finally
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
        }
    }

    private Predicate<String> getNameFilter(final Collection<String> exactNamesFilter, final Collection<String> alikeNamesFilter)
    {
        if (CollectionUtils.isEmpty(exactNamesFilter) && CollectionUtils.isEmpty(alikeNamesFilter))
        {
            return null;
        }

        final Collection<String> namesFilter = new HashSet<>();
        if (exactNamesFilter != null)
        {
            namesFilter.addAll(exactNamesFilter);
        }
        if (alikeNamesFilter != null)
        {
            namesFilter.addAll(alikeNamesFilter);
        }

        return (name) -> namesFilter.stream().anyMatch(filter -> {
            if (filter.startsWith("*"))
            {
                if (filter.endsWith("*"))
                {
                    return name.contains(filter.replace("*", StringUtils.EMPTY));
                }
                else
                {
                    return name.endsWith(filter.replace("*", StringUtils.EMPTY));
                }
            }
            else if (filter.endsWith("*"))
            {
                return name.startsWith(filter.replace("*", StringUtils.EMPTY));
            }
            else
            {
                return name.equals(filter);
            }
        });
    }

    static class TagSupport
    {
        private final NodeService publicNodeService;
        private final IndexerAndSearcher indexerAndSearcher;
        private final NodeService nodeService;
        private int queryFetchSize = 5000;

        TagSupport(NodeService publicNodeService, IndexerAndSearcher indexerAndSearcher, NodeService nodeService)
        {
            this.publicNodeService = Objects.requireNonNull(publicNodeService);
            this.indexerAndSearcher = Objects.requireNonNull(indexerAndSearcher);
            this.nodeService = Objects.requireNonNull(nodeService);
        }

        Optional<ChildAssociationRef> findExistingTag(final StoreRef storeRef, final String tagName)
        {
            final Optional<NodeRef> taggableNodeOptional = findTaggableNode(storeRef);
            if (taggableNodeOptional.isEmpty())
            {
                return Optional.empty();
            }
            final NodeRef taggableNode = taggableNodeOptional.get();

            QName tagQName = getTagQName(taggableNode, tagName);
            return nodeService
                    .getChildAssocs(taggableNode, ContentModel.ASSOC_SUBCATEGORIES, tagQName, false)
                    .stream()
                    .findFirst();
        }

        ChildAssociationRef createTag(final StoreRef storeRef, final String tagName)
        {
            final Optional<NodeRef> taggableNodeOptional = findTaggableNode(storeRef);
            if (taggableNodeOptional.isEmpty() || !nodeService.exists(taggableNodeOptional.get()))
            {
                throw new AlfrescoRuntimeException("Missing category?");
            }
            final NodeRef taggableNode = taggableNodeOptional.get();

            final QName tagQName = getTagQName(taggableNode, tagName);

            final ChildAssociationRef newTagAssociation = publicNodeService
                    .createNode(taggableNode, ContentModel.ASSOC_SUBCATEGORIES, tagQName, ContentModel.TYPE_CATEGORY);
            publicNodeService.setProperty(newTagAssociation.getChildRef(), ContentModel.PROP_NAME, tagName);

            return newTagAssociation;
        }

        List<ChildAssociationRef> findAllTags(final StoreRef storeRef, final String namesFilter, boolean sortByName)
        {
            final Collection<String> alikeNamesFilter = Optional.ofNullable(namesFilter).map(f -> "*" + f + "*").map(Set::of).orElse(null);
            return findAllTags(storeRef, alikeNamesFilter, sortByName);
        }

        List<ChildAssociationRef> findAllTags(final StoreRef storeRef, final Collection<String> namesFilter, boolean sortByName)
        {
            final Optional<NodeRef> taggableNodeOptional = findTaggableNode(storeRef);
            if (taggableNodeOptional.isEmpty())
            {
                return Collections.emptyList();
            }
            final NodeRef taggableNode = taggableNodeOptional.get();

            final StringBuilder query = new StringBuilder("PATH:\"/" + CM_CATEGORY_ROOT + "/cm:taggable/*\" AND TYPE:\"cm:category\"");
            if (CollectionUtils.isNotEmpty(namesFilter))
            {
                final StringJoiner filters = new StringJoiner(" OR ", " AND (", ")");
                namesFilter.forEach(nameFilter -> filters.add("cm:name:" + nameFilter));
                query.append(filters);
            }
            final SearchParameters searchParameters = new SearchParameters();
            searchParameters.setLanguage(LANGUAGE_FTS_ALFRESCO);
            searchParameters.setQuery(query.toString());
            searchParameters.setLimit(queryFetchSize);
            searchParameters.setMaxItems(queryFetchSize);
            searchParameters.setLimitBy(LimitBy.FINAL_SIZE);
            searchParameters.addStore(taggableNode.getStoreRef());
            if (sortByName)
            {
                searchParameters.addSort("@" + ContentModel.PROP_NAME, true);
            }

            final ResultSet queryResult = getSearcher(taggableNode).query(searchParameters);
            try
            {
                return queryResult.getNodeRefs().stream()
                        .flatMap(this::toChildAssociationRef)
                        .collect(Collectors.toUnmodifiableList());
            }
            finally
            {
                queryResult.close();
            }
        }

        List<ChildAssociationRef> findAllTags(final StoreRef storeRef, final Collection<String> namesFilter)
        {
            final Optional<NodeRef> taggableNodeOptional = findTaggableNode(storeRef);
            if (taggableNodeOptional.isEmpty())
            {
                return Collections.emptyList();
            }
            final NodeRef taggableNode = taggableNodeOptional.get();
            if (CollectionUtils.isEmpty(namesFilter))
            {
                return nodeService.getChildAssocs(taggableNode, ContentModel.ASSOC_SUBCATEGORIES, RegexQNamePattern.MATCH_ALL);
            }
            else
            {
                return nodeService.getChildrenByName(taggableNode, ContentModel.ASSOC_SUBCATEGORIES, namesFilter);
            }
        }

        private Optional<NodeRef> findTaggableNode(StoreRef storeRef)
        {
            final NodeRef taggableNodeRef = new NodeRef(storeRef, "tag:tag-root");
            if (nodeService.exists(storeRef) && nodeService.exists(taggableNodeRef))
            {
                return Optional.of(taggableNodeRef);
            }
            return Optional.empty();
        }

        void setQueryFetchSize(int queryFetchSize)
        {
            this.queryFetchSize = queryFetchSize;
        }

        private QName getTagQName(final NodeRef taggableNode, final String tagName)
        {
            final String uri = nodeService.getPrimaryParent(taggableNode).getQName().getNamespaceURI();
            final String validLocalName = QName.createValidLocalName(tagName);

            return QName.createQName(uri, validLocalName);
        }

        private SearchService getSearcher(final NodeRef taggableNode)
        {
            return getSearcher(taggableNode.getStoreRef(), true);
        }

        private SearchService getSearcher(final StoreRef storeRef, boolean searchDelta)
        {
            return indexerAndSearcher.getSearcher(storeRef, searchDelta);
        }

        private Stream<ChildAssociationRef> toChildAssociationRef(NodeRef nodeRef)
        {
            if (nodeRef == null)
            {
                return Stream.empty();
            }
            try
            {
                return Stream.of(nodeService.getPrimaryParent(nodeRef));
            }
            catch (InvalidNodeRefException e)
            {
                // keep going the node has gone beneath us just skip it
                return Stream.empty();
            }
        }
    }

    private RuntimeException notSupportedYet()
    {
        return new UnsupportedOperationException("This use case is not supported yet. Only TAG related use cases are supported.");
    }

    /** Package protected setter for use in unit tests. */
    void setTagSupport(TagSupport tagSupport)
    {
        this.tagSupport = tagSupport;
    }
}
