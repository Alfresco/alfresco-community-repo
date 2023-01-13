/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.search.impl.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.IndexerAndSearcher;
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
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * This test class covers part of the code implemented in abstract class {@link org.alfresco.repo.search.impl.AbstractCategoryServiceImpl}.
 * That's because abstract class cannot be instantiated and directly tested.
 */
@RunWith(MockitoJUnitRunner.class)
public class SolrCategoryServiceImplTest
{
    private static final String PATH_ROOT = "-root-";
    private static final String CAT_ROOT_NODE_ID = "cat-root-node-id";
    private static final String NODE_ID_PREFIX = "node-id-";
    private static final StoreRef STORE_REF = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private ChildAssociationRef categoryRootChildAssociationRefMock;
    @Mock
    private ChildAssociationRef categoryChildAssociationRefMock;
    @Mock
    private IndexerAndSearcher indexerAndSearcherMock;
    @Mock
    private SearchService searcherMock;
    @Mock
    private DictionaryService dictionaryServiceMock;
    @Mock
    private AspectDefinition aspectDefinitionMock;
    @Mock
    private ResultSet resultSetMock;

    @InjectMocks
    private SolrCategoryServiceImpl objectUnderTest;

    @Test
    public void testGetRootCategoryNodeRef()
    {
        final NodeRef rootNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PATH_ROOT);
        given(nodeServiceMock.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)).willReturn(rootNodeRef);
        given(nodeServiceMock.getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT)))
                .willReturn(List.of(categoryRootChildAssociationRefMock));
        given(categoryChildAssociationRefMock.getQName()).willReturn(ContentModel.ASPECT_GEN_CLASSIFIABLE);
        final NodeRef categoryRootNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, CAT_ROOT_NODE_ID);
        given(categoryChildAssociationRefMock.getChildRef()).willReturn(categoryRootNodeRef);
        given(nodeServiceMock.getChildAssocs(categoryRootChildAssociationRefMock.getChildRef()))
                .willReturn(List.of(categoryChildAssociationRefMock));

        //when
        final Optional<NodeRef> rooCategoryNodeRef = objectUnderTest.getRootCategoryNodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        then(nodeServiceMock).should().getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        then(nodeServiceMock).should().getChildAssocs(rootNodeRef, Set.of(ContentModel.TYPE_CATEGORYROOT));
        then(nodeServiceMock).should().getChildAssocs(categoryRootChildAssociationRefMock.getChildRef());
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

        assertTrue(rooCategoryNodeRef.isPresent());
        assertEquals(CAT_ROOT_NODE_ID, rooCategoryNodeRef.get().getId());
    }

    @Test
    public void testGetTopCategories()
    {
        given(indexerAndSearcherMock.getSearcher(STORE_REF, false)).willReturn(searcherMock);
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
        given(nodeServiceMock.exists(any(NodeRef.class))).willReturn(true);

        //when
        final List<Pair<NodeRef, Integer>> topCategories = objectUnderTest.getTopCategories(STORE_REF, aspectGenClassifiable, count);

        then(indexerAndSearcherMock).should().getSearcher(STORE_REF, false);
        then(indexerAndSearcherMock).shouldHaveNoMoreInteractions();
        then(dictionaryServiceMock).should().getAspect(aspectGenClassifiable);
        then(dictionaryServiceMock).shouldHaveNoMoreInteractions();
        then(searcherMock).should().query(searchParameters);
        then(searcherMock).shouldHaveNoMoreInteractions();
        then(nodeServiceMock).should(times(3)).exists(any(NodeRef.class));
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

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

        //when
        assertThrows(IllegalStateException.class, () -> objectUnderTest.getTopCategories(STORE_REF, aspectGenClassifiable, 100));

        then(indexerAndSearcherMock).shouldHaveNoInteractions();
        then(dictionaryServiceMock).should().getAspect(aspectGenClassifiable);
        then(dictionaryServiceMock).shouldHaveNoMoreInteractions();
        then(searcherMock).shouldHaveNoInteractions();
        then(nodeServiceMock).shouldHaveNoInteractions();
    }

    @Test
    public void testGetTopCategories_customCategoryAspect()
    {
        given(indexerAndSearcherMock.getSearcher(STORE_REF, false)).willReturn(searcherMock);
        final QName aspectCustomCategories = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "customcategories");
        mockAspectDefinition(aspectCustomCategories, DataTypeDefinition.CATEGORY);
        given(dictionaryServiceMock.getAspect(aspectCustomCategories)).willReturn(aspectDefinitionMock);
        final String field = getField(aspectCustomCategories);
        final int count = 100;
        final SearchParameters searchParameters = prepareSearchParams(STORE_REF, aspectCustomCategories, field, count);
        final List<Integer> countList = List.of(11, 9, 8);
        mockResultSet(field, countList);
        given(searcherMock.query(searchParameters)).willReturn(resultSetMock);
        given(nodeServiceMock.exists(any(NodeRef.class))).willReturn(true);

        //when
        final List<Pair<NodeRef, Integer>> topCategories = objectUnderTest.getTopCategories(STORE_REF, aspectCustomCategories, count);

        then(indexerAndSearcherMock).should().getSearcher(STORE_REF, false);
        then(indexerAndSearcherMock).shouldHaveNoMoreInteractions();
        then(dictionaryServiceMock).should().getAspect(aspectCustomCategories);
        then(dictionaryServiceMock).shouldHaveNoMoreInteractions();
        then(searcherMock).should().query(searchParameters);
        then(searcherMock).shouldHaveNoMoreInteractions();
        then(nodeServiceMock).should(times(3)).exists(any(NodeRef.class));
        then(nodeServiceMock).shouldHaveNoMoreInteractions();

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

        //when
        assertThrows(IllegalStateException.class, () -> objectUnderTest.getTopCategories(STORE_REF, aspectCustomCategories, count));

        then(indexerAndSearcherMock).shouldHaveNoInteractions();
        then(dictionaryServiceMock).should().getAspect(aspectCustomCategories);
        then(dictionaryServiceMock).shouldHaveNoMoreInteractions();
        then(searcherMock).shouldHaveNoInteractions();
        then(nodeServiceMock).shouldHaveNoInteractions();
    }

    private String getField(QName categoryProperty)
    {
        return "@" + categoryProperty;
    }

    private SearchParameters prepareSearchParams(StoreRef storeRef, QName categoryProperty, String field, int count)
    {
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_INDEX_FTS_ALFRESCO);
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
                .mapToObj(i -> new Pair<>(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE + "/" + NODE_ID_PREFIX + i, countList.get(i)))
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
