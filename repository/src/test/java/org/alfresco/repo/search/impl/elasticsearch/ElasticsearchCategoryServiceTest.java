/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import static java.util.Collections.emptySet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.alfresco.model.ContentModel.ASPECT_AUTHOR;
import static org.alfresco.model.ContentModel.ASPECT_GEN_CLASSIFIABLE;
import static org.alfresco.model.ContentModel.ASPECT_TAGGABLE;
import static org.alfresco.model.ContentModel.ASSOC_SUBCATEGORIES;
import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.model.ContentModel.TYPE_CATEGORY;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.service.namespace.QName.createQName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchCategoryService.TagSupport;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/** Unit tests for {@link ElasticsearchCategoryService}. */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD")
public class ElasticsearchCategoryServiceTest
{
    /** The root node of the spaces store. */
    private static final NodeRef ROOT_NODE = new NodeRef("root://node/");
    /** The parent of the root category. */
    private static final NodeRef CATEGORY_ROOT_NODE = new NodeRef("cm://categoryRoot/");
    /** The general classifiable node in the category hierarchy. */
    private static final NodeRef GENERAL_CLASSIFIABLE_NODE = new NodeRef("cm://generalclassifiable/");
    /** A category that is a child of the category root. */
    private static final NodeRef TOP_LEVEL_CATEGORY = new NodeRef("top://level/category");
    /** The parent child associations from the generalclassifiable node to the top level categories. */
    private static final List<ChildAssociationRef> TOP_LEVEL_CATEGORY_ASSOCS = List.of(new ChildAssociationRef(null, GENERAL_CLASSIFIABLE_NODE, null, TOP_LEVEL_CATEGORY));
    /** The name of the root category. */
    private static final String TOP_LEVEL_CATEGORY_NAME = "top level category name";

    private static final String NODE_ID_PREFIX = "node-id-";
    private static final StoreRef STORE_REF = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

    @InjectMocks
    private ElasticsearchCategoryService esCategoryService;
    @Mock
    private NodeService nodeService;
    @Mock
    private IndexerAndSearcher indexerAndSearcher;
    @Mock
    private TagSupport tagSupport;
    @Mock
    private NamespacePrefixResolver namespacePrefixResolver;

    @Mock
    private SearchService searcherMock;
    @Mock
    private DictionaryService dictionaryServiceMock;
    @Mock
    private AspectDefinition aspectDefinitionMock;
    @Mock
    private ResultSet resultSetMock;

    /**
     * A standard path to a top level category looks like:
     * 
     * <pre>
     * /cm:categoryRoot/cm:generalclassifiable/cm:topLevelCategory
     * </pre>
     */
    @Before
    public void setUp()
    {
        esCategoryService.setTagSupport(tagSupport);
        esCategoryService.setNamespacePrefixResolver(namespacePrefixResolver);
        lenient().when(namespacePrefixResolver.getPrefixes("http://www.alfresco.org/model/content/1.0")).thenReturn(Set.of("cm"));
        esCategoryService.setDictionaryService(dictionaryServiceMock);

        given(nodeService.getRootNode(STORE_REF_WORKSPACE_SPACESSTORE)).willReturn(ROOT_NODE);
        List<ChildAssociationRef> rootChildren = List.of(new ChildAssociationRef(null, ROOT_NODE, null, CATEGORY_ROOT_NODE));
        given(nodeService.getChildAssocs(ROOT_NODE, Set.of(ContentModel.TYPE_CATEGORYROOT))).willReturn(rootChildren);
        List<ChildAssociationRef> rootCategoryAssocs = List.of(new ChildAssociationRef(null, CATEGORY_ROOT_NODE, ASPECT_GEN_CLASSIFIABLE, GENERAL_CLASSIFIABLE_NODE));
        given(nodeService.getChildAssocs(CATEGORY_ROOT_NODE)).willReturn(rootCategoryAssocs);
        given(nodeService.exists(GENERAL_CLASSIFIABLE_NODE)).willReturn(true);
        given(nodeService.getPrimaryParent(GENERAL_CLASSIFIABLE_NODE)).willReturn(rootCategoryAssocs.get(0));
        given(nodeService.getChildAssocs(GENERAL_CLASSIFIABLE_NODE)).willReturn(TOP_LEVEL_CATEGORY_ASSOCS);
        given(nodeService.getProperty(TOP_LEVEL_CATEGORY, PROP_NAME)).willReturn(TOP_LEVEL_CATEGORY_NAME);
    }

    @Test
    public void getRootCategories_tags()
    {
        List<ChildAssociationRef> tagAssocs = List.of(mock(ChildAssociationRef.class));
        given(tagSupport.findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, (String) null, false)).willReturn(tagAssocs);

        Collection<ChildAssociationRef> rootCategoryAssocs = esCategoryService.getRootCategories(
                STORE_REF_WORKSPACE_SPACESSTORE,
                ASPECT_TAGGABLE);

        assertEquals("Unexpected list of tags.", tagAssocs, rootCategoryAssocs);
    }

    @Test
    public void getRootCategories_categories()
    {
        Collection<ChildAssociationRef> rootCategoryAssocs = esCategoryService.getRootCategories(
                STORE_REF_WORKSPACE_SPACESSTORE,
                ASPECT_GEN_CLASSIFIABLE);

        assertEquals("Unexpected list of root categories.", TOP_LEVEL_CATEGORY_ASSOCS, rootCategoryAssocs);
        verifyNoInteractions(indexerAndSearcher);
    }

    @Test
    public void getRootCategories_aspectNotSupported()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_AUTHOR));
    }

    @Test
    public void getFilteredRootCategories_tags()
    {
        List<ChildAssociationRef> tagAssocs = List.of(mock(ChildAssociationRef.class));
        given(tagSupport.findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, "filter", false)).willReturn(tagAssocs);

        Collection<ChildAssociationRef> rootCategoryAssocs = esCategoryService.getRootCategories(
                STORE_REF_WORKSPACE_SPACESSTORE,
                ASPECT_TAGGABLE,
                "filter");

        assertEquals("Unexpected list of tags.", tagAssocs, rootCategoryAssocs);
    }

    @Test
    public void getFilteredRootCategories_categories()
    {
        Collection<ChildAssociationRef> rootCategoryAssocs = esCategoryService.getRootCategories(
                STORE_REF_WORKSPACE_SPACESSTORE,
                ASPECT_GEN_CLASSIFIABLE,
                TOP_LEVEL_CATEGORY_NAME);

        assertEquals("Unexpected list of root categories.", new HashSet<>(TOP_LEVEL_CATEGORY_ASSOCS), rootCategoryAssocs);
        verifyNoInteractions(indexerAndSearcher);
    }

    @Test
    public void getFilteredRootCategories_notFound()
    {
        String notMatchingFilter = "Not matching filter";

        Collection<ChildAssociationRef> rootCategoryAssocs = esCategoryService.getRootCategories(
                STORE_REF_WORKSPACE_SPACESSTORE,
                ASPECT_GEN_CLASSIFIABLE,
                notMatchingFilter);

        assertEquals("Expected no root categories to match filter.", emptySet(), rootCategoryAssocs);
    }

    @Test
    public void getFilteredRootCategories_aspectNotSupported()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_AUTHOR, "filter"));
    }

    @Test
    public void getRootCategoriesWithCreateOption_tags()
    {
        ChildAssociationRef tagAssoc = mock(ChildAssociationRef.class);
        given(tagSupport.findExistingTag(STORE_REF_WORKSPACE_SPACESSTORE, "name")).willReturn(Optional.of(tagAssoc));

        Collection<ChildAssociationRef> rootCategoryAssocs = esCategoryService.getRootCategories(
                STORE_REF_WORKSPACE_SPACESSTORE,
                ASPECT_TAGGABLE,
                "name",
                true);

        assertEquals("Unexpected list of tags.", List.of(tagAssoc), rootCategoryAssocs);
    }

    @Test
    public void getRootCategoriesWithCreateOption_dontCreateExistingCategory()
    {
        Collection<ChildAssociationRef> rootCategoryAssocs = esCategoryService.getRootCategories(
                STORE_REF_WORKSPACE_SPACESSTORE,
                ASPECT_GEN_CLASSIFIABLE,
                TOP_LEVEL_CATEGORY_NAME,
                true);

        assertEquals("Unexpected list of root categories.", new HashSet<>(TOP_LEVEL_CATEGORY_ASSOCS), rootCategoryAssocs);
    }

    @Test
    public void getRootCategoriesWithCreateOption_createNewCategory()
    {
        String newCategoryName = "new category name";
        SearchService searchService = mock(SearchService.class);
        lenient().when(indexerAndSearcher.getSearcher(STORE_REF_WORKSPACE_SPACESSTORE, false)).thenReturn(searchService);
        ResultSet resultSet = mock(ResultSet.class);
        lenient().when(searchService.query(STORE_REF_WORKSPACE_SPACESSTORE, "lucene", "PATH:\"/cm:categoryRoot/cm:generalclassifiable\"", null))
                .thenReturn(resultSet);
        lenient().when(resultSet.getNodeRefs()).thenReturn(List.of(GENERAL_CLASSIFIABLE_NODE));
        // Mocking for the newly created category node.
        NodeRef createdCategory = new NodeRef("created://category/");
        ChildAssociationRef newCategoryAssociation = new ChildAssociationRef(null, GENERAL_CLASSIFIABLE_NODE, null, createdCategory);
        given(nodeService.createNode(GENERAL_CLASSIFIABLE_NODE, ASSOC_SUBCATEGORIES,
                createQName("http://www.alfresco.org/model/content/1.0", newCategoryName),
                TYPE_CATEGORY))
                        .willReturn(newCategoryAssociation);
        given(nodeService.getParentAssocs(createdCategory)).willReturn(List.of(newCategoryAssociation));

        // Call the method under test.
        Collection<ChildAssociationRef> actual = esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_GEN_CLASSIFIABLE, newCategoryName, true);

        assertEquals("Unexpected root categories returned.", List.of(newCategoryAssociation), actual);
    }

    @Test
    public void getRootCategoriesWithCreateOption_dontCreate()
    {
        Collection<ChildAssociationRef> rootCategoryAssocs = esCategoryService.getRootCategories(
                STORE_REF_WORKSPACE_SPACESSTORE,
                ASPECT_GEN_CLASSIFIABLE,
                TOP_LEVEL_CATEGORY_NAME,
                false);

        assertEquals("Unexpected list of root categories.", new HashSet<>(TOP_LEVEL_CATEGORY_ASSOCS), rootCategoryAssocs);
    }

    @Test
    public void getRootCategoriesWithCreateOption_aspectNotSupported()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_AUTHOR, "name", true));
    }

    @Test
    public void getPagedRootCategories_sorting()
    {
        PagingRequest pagingRequest = new PagingRequest(10);

        // Simulate three root categories with different names.
        Map<String, ChildAssociationRef> associations = new HashMap<>();
        Stream.of("three", "root", "categories").forEach(name -> {
            NodeRef nodeRef = new NodeRef("root://category/" + name);
            given(nodeService.getProperty(nodeRef, PROP_NAME)).willReturn(name);
            ChildAssociationRef association = new ChildAssociationRef(null, GENERAL_CLASSIFIABLE_NODE, null, nodeRef);
            associations.put(name, association);
        });
        given(nodeService.getChildAssocs(GENERAL_CLASSIFIABLE_NODE)).willReturn(new ArrayList<>(associations.values()));

        PagingResults<ChildAssociationRef> rootCategories = esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_GEN_CLASSIFIABLE, pagingRequest, true);

        List<ChildAssociationRef> expected = List.of(associations.get("categories"), associations.get("root"), associations.get("three"));
        assertEquals("Unexpected list of root categories.", expected, rootCategories.getPage());
    }

    @Test
    public void getPagedRootCategories_noSorting()
    {
        PagingRequest pagingRequest = new PagingRequest(2);

        // Simulate three root categories with different names.
        Map<String, ChildAssociationRef> associations = new HashMap<>();
        Stream.of("three", "root", "categories").forEach(name -> {
            NodeRef nodeRef = new NodeRef("root://category/" + name);
            given(nodeService.getProperty(nodeRef, PROP_NAME)).willReturn(name);
            ChildAssociationRef association = new ChildAssociationRef(null, GENERAL_CLASSIFIABLE_NODE, null, nodeRef);
            associations.put(name, association);
        });
        given(nodeService.getChildAssocs(GENERAL_CLASSIFIABLE_NODE)).willReturn(List.of(associations.get("three"), associations.get("root"), associations.get("categories")));

        PagingResults<ChildAssociationRef> rootCategories = esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_GEN_CLASSIFIABLE, pagingRequest, false);

        // We only asked for two items per page.
        List<ChildAssociationRef> expected = List.of(associations.get("three"), associations.get("root"));
        assertEquals("Unexpected list of root categories.", expected, rootCategories.getPage());
    }

    @Test
    public void getPagedRootCategories_aspectNotSupported()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_AUTHOR, null, true, null, null));
    }

    @Test
    public void testGetRootCategories_taggableExactAndAlikeFiltered()
    {
        final PagingRequest pagingRequest = new PagingRequest(10);
        final Collection<String> exactNamesFilter = Set.of("tag");
        final Collection<String> alikeNamesFilter = Set.of("test-tag*");

        // when
        esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_TAGGABLE, pagingRequest, false,
                exactNamesFilter, alikeNamesFilter);

        then(tagSupport).should().findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, exactNamesFilter);
        then(tagSupport).should().findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, alikeNamesFilter, false);
        then(tagSupport).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testGetRootCategories_taggableExactFiltered()
    {
        final PagingRequest pagingRequest = new PagingRequest(10);
        final Collection<String> exactNamesFilter = Set.of("tag");

        // when
        esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_TAGGABLE, pagingRequest, false,
                exactNamesFilter, null);

        then(tagSupport).should().findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, exactNamesFilter);
        then(tagSupport).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testGetRootCategories_taggableAlikeFiltered()
    {
        final PagingRequest pagingRequest = new PagingRequest(10);
        final Collection<String> alikeNamesFilter = Set.of("test-tag*");

        // when
        esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_TAGGABLE, pagingRequest, false,
                null, alikeNamesFilter);

        then(tagSupport).should().findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, alikeNamesFilter, false);
        then(tagSupport).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testGetRootCategories_taggableNotFiltered()
    {
        final PagingRequest pagingRequest = new PagingRequest(10);

        // when
        esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_TAGGABLE, pagingRequest, false,
                null, null);

        then(tagSupport).should().findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, null);
        then(tagSupport).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testGetRootCategories_taggableExactAndAlikeFilteredSorted()
    {
        final PagingRequest pagingRequest = new PagingRequest(10);
        final Collection<String> exactNamesFilter = Set.of("b");
        final Collection<String> alikeNamesFilter = Set.of("*a");
        final Map<String, ChildAssociationRef> associations = new HashMap<>();
        Stream.of("ca", "a", "b").forEach(name -> {
            NodeRef nodeRef = new NodeRef("root://category/" + name);
            QName qName = QName.createQName(null, name);
            ChildAssociationRef association = new ChildAssociationRef(null, GENERAL_CLASSIFIABLE_NODE, qName, nodeRef);
            associations.put(name, association);
        });
        given(tagSupport.findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, exactNamesFilter)).willReturn(List.of(associations.get("b")));
        given(tagSupport.findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, alikeNamesFilter, true)).willReturn(List.of(associations.get("ca"), associations.get("a")));

        // when
        final PagingResults<ChildAssociationRef> actualTags = esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_TAGGABLE, pagingRequest, true,
                exactNamesFilter, alikeNamesFilter);

        assertEquals(List.of(associations.get("a"), associations.get("b"), associations.get("ca")), actualTags.getPage());
    }

    @Test
    public void testGetRootCategories_classifiableExactAndAlikeFilteredSorted()
    {
        final PagingRequest pagingRequest = new PagingRequest(10);
        final Collection<String> exactNamesFilter = Set.of("x");
        final Collection<String> alikeNamesFilter = Set.of("*a", "*z*");
        final Map<String, ChildAssociationRef> associations = new HashMap<>();
        Stream.of("ca", "kxk", "azb", "da", "lk", "x", "a", "b").forEach(name -> {
            NodeRef nodeRef = new NodeRef("root://category/" + name);
            QName qName = QName.createQName(null, name);
            ChildAssociationRef association = new ChildAssociationRef(null, GENERAL_CLASSIFIABLE_NODE, qName, nodeRef);
            associations.put(name, association);
            given(nodeService.getProperty(nodeRef, PROP_NAME)).willReturn(name);
        });
        given(nodeService.getChildAssocs(GENERAL_CLASSIFIABLE_NODE)).willReturn(new ArrayList<>(associations.values()));

        // when
        final PagingResults<ChildAssociationRef> actualCategories = esCategoryService.getRootCategories(STORE_REF_WORKSPACE_SPACESSTORE, ASPECT_GEN_CLASSIFIABLE, pagingRequest, true,
                exactNamesFilter, alikeNamesFilter);

        assertEquals(Stream.of("a", "azb", "ca", "da", "x").map(associations::get).collect(Collectors.toList()), actualCategories.getPage());
    }

    @Test
    public void testGetTopCategories()
    {
        given(indexerAndSearcher.getSearcher(STORE_REF, false)).willReturn(searcherMock);
        final QName aspectGenClassifiable = ContentModel.ASPECT_GEN_CLASSIFIABLE;
        mockAspectDefinition(ContentModel.PROP_CATEGORIES, null);
        given(dictionaryServiceMock.getAspect(aspectGenClassifiable)).willReturn(aspectDefinitionMock);
        final QName categoryProperty = ContentModel.PROP_CATEGORIES;
        final String field = getField(categoryProperty);
        final int count = 100;
        final SearchParameters searchParameters = prepareSearchParams(STORE_REF, categoryProperty, field, count);
        final List<Integer> countList = List.of(11, 9, 8);
        mockResultSet(field, countList);
        given(searcherMock.query(searchParameters)).willReturn(resultSetMock);
        given(nodeService.exists(any(NodeRef.class))).willReturn(true);

        // when
        final List<Pair<NodeRef, Integer>> topCategories = esCategoryService.getTopCategories(STORE_REF, aspectGenClassifiable, count);

        then(indexerAndSearcher).should().getSearcher(STORE_REF, false);
        then(indexerAndSearcher).shouldHaveNoMoreInteractions();
        then(dictionaryServiceMock).should().getAspect(aspectGenClassifiable);
        then(dictionaryServiceMock).shouldHaveNoMoreInteractions();
        then(searcherMock).should().query(searchParameters);
        then(searcherMock).shouldHaveNoMoreInteractions();
        then(nodeService).should(times(3)).exists(any(NodeRef.class));
        then(nodeService).shouldHaveNoMoreInteractions();

        IntStream.range(0, countList.size())
                .forEach(i -> {
                    final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, NODE_ID_PREFIX + i);
                    assertEquals(nodeRef, topCategories.get(i).getFirst());
                    assertEquals(countList.get(i), topCategories.get(i).getSecond());
                });
    }

    @Test
    public void testGetTopCategories_nonExistingAspect()
    {
        final QName aspectGenClassifiable = ContentModel.ASPECT_GEN_CLASSIFIABLE;
        given(dictionaryServiceMock.getAspect(aspectGenClassifiable)).willReturn(null);

        // when
        assertThrows(IllegalStateException.class, () -> esCategoryService.getTopCategories(STORE_REF, aspectGenClassifiable, 100));

        then(indexerAndSearcher).shouldHaveNoInteractions();
        then(dictionaryServiceMock).should().getAspect(aspectGenClassifiable);
        then(dictionaryServiceMock).shouldHaveNoMoreInteractions();
        then(searcherMock).shouldHaveNoInteractions();
        then(nodeService).shouldHaveNoInteractions();
    }

    @Test
    public void testGetTopCategories_customCategoryAspect()
    {
        given(indexerAndSearcher.getSearcher(STORE_REF, false)).willReturn(searcherMock);
        final QName aspectCustomCategories = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "customcategories");
        mockAspectDefinition(aspectCustomCategories, DataTypeDefinition.CATEGORY);
        given(dictionaryServiceMock.getAspect(aspectCustomCategories)).willReturn(aspectDefinitionMock);
        final String field = getField(aspectCustomCategories);
        final int count = 100;
        final SearchParameters searchParameters = prepareSearchParams(STORE_REF, aspectCustomCategories, field, count);
        final List<Integer> countList = List.of(11, 9, 8);
        mockResultSet(field, countList);
        given(searcherMock.query(searchParameters)).willReturn(resultSetMock);
        given(nodeService.exists(any(NodeRef.class))).willReturn(true);

        // when
        final List<Pair<NodeRef, Integer>> topCategories = esCategoryService.getTopCategories(STORE_REF, aspectCustomCategories, count);

        then(indexerAndSearcher).should().getSearcher(STORE_REF, false);
        then(indexerAndSearcher).shouldHaveNoMoreInteractions();
        then(dictionaryServiceMock).should().getAspect(aspectCustomCategories);
        then(dictionaryServiceMock).shouldHaveNoMoreInteractions();
        then(searcherMock).should().query(searchParameters);
        then(searcherMock).shouldHaveNoMoreInteractions();
        then(nodeService).should(times(3)).exists(any(NodeRef.class));
        then(nodeService).shouldHaveNoMoreInteractions();

        IntStream.range(0, countList.size())
                .forEach(i -> {
                    final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, NODE_ID_PREFIX + i);
                    assertEquals(nodeRef, topCategories.get(i).getFirst());
                    assertEquals(countList.get(i), topCategories.get(i).getSecond());
                });
    }

    @Test
    public void testGetTopCategories_invalidCustomCategoryAspect()
    {
        final QName aspectCustomCategories = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "customcategories");
        mockAspectDefinition(aspectCustomCategories, DataTypeDefinition.QNAME);
        given(dictionaryServiceMock.getAspect(aspectCustomCategories)).willReturn(aspectDefinitionMock);
        final String field = getField(aspectCustomCategories);
        final int count = 100;

        // when
        assertThrows(IllegalStateException.class, () -> esCategoryService.getTopCategories(STORE_REF, aspectCustomCategories, count));

        then(indexerAndSearcher).shouldHaveNoInteractions();
        then(dictionaryServiceMock).should().getAspect(aspectCustomCategories);
        then(dictionaryServiceMock).shouldHaveNoMoreInteractions();
        then(searcherMock).shouldHaveNoInteractions();
        then(nodeService).shouldHaveNoInteractions();
    }

    private String getField(QName categoryProperty)
    {
        return "@" + categoryProperty;
    }

    private SearchParameters prepareSearchParams(StoreRef storeRef, QName categoryProperty, String field, int count)
    {
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        sp.addStore(storeRef);
        sp.setQuery(categoryProperty + ":*");
        final SearchParameters.FieldFacet ff = new SearchParameters.FieldFacet(field);
        ff.setLimitOrNull(count);
        sp.addFieldFacet(ff);
        sp.setMaxItems(1);
        sp.setSkipCount(0);
        return sp;
    }

    private void mockResultSet(final String field, List<Integer> countList)
    {
        final List<Pair<String, Integer>> facetedResults = IntStream.range(0, countList.size())
                .mapToObj(i -> new Pair<>(NODE_ID_PREFIX + i, countList.get(i)))
                .collect(Collectors.toList());
        given(resultSetMock.getFieldFacet(field)).willReturn(CollectionUtils.isEmpty(facetedResults) ? null : facetedResults);
    }

    private void mockAspectDefinition(final QName qName, final QName dataType)
    {
        final PropertyDefinition propertyDefinitionMock = mock(PropertyDefinition.class);
        final DataTypeDefinition dataTypeMock = mock(DataTypeDefinition.class);
        if (dataType != null)
        {
            given(propertyDefinitionMock.getDataType()).willReturn(dataTypeMock);
            given(dataTypeMock.getName()).willReturn(dataType);
        }
        given(aspectDefinitionMock.getProperties()).willReturn(Map.of(qName, propertyDefinitionMock));
    }
}
