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
package org.alfresco.repo.search.impl.elasticsearch.query;

import static org.assertj.core.api.Assertions.assertThat;

import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_MIME_TYPE;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_SIZE;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.NAME;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.USER_MODIFIER;
import static org.alfresco.transform.common.Mimetype.MIMETYPE_IMAGE_JPEG;
import static org.alfresco.transform.common.Mimetype.MIMETYPE_PDF;
import static org.alfresco.transform.common.Mimetype.MIMETYPE_TEXT_PLAIN;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.Ordering;
import com.google.common.collect.Streams;
import com.ibm.icu.util.Calendar;
import org.junit.Before;
import org.junit.Test;

import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.DictionaryDAOImpl;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.NamespaceDAO;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.ContentModelSynchronizer;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.FieldMappingBuilder;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;

// ResultSet is closed automatically at the end of the transaction - see org.alfresco.service.cmr.search.ResultSetSPI.close
@SuppressWarnings("PMD")
public class SortIT extends ElasticsearchBaseQueryIT
{
    private NodeRef doc100jpgByTom;
    private NodeRef doc500pdfByAdam;
    private NodeRef doc100_000txtByJohn;
    private List<NodeRef> batchOfDocuments;

    @Before
    public void initDocuments() throws Exception
    {
        DictionaryDAO dictionaryDAOImpl = (DictionaryDAOImpl) this.applicationContext.getBean("dictionaryDAO");
        NamespaceDAO namespaceDAOImpl = (NamespaceDAO) this.applicationContext.getBean("namespaceDAO");

        FieldMappingBuilder mappingBuilder = elasticsearchContext.getBean(FieldMappingBuilder.class);

        ContentModelSynchronizer modelSynchronizer = new ContentModelSynchronizer(mappingBuilder, elasticsearchHttpClientFactory, Locale.ENGLISH.getLanguage(),
                indexConfigurationInitializer);

        InputStream modelStream = getClass().getResourceAsStream("/alfresco/search/contentModels/content-model.xml");
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDAOImpl.putModel(model);
        CompiledModel sampleModel = model.compile(dictionaryDAOImpl, namespaceDAOImpl, false);

        boolean acknowledged = modelSynchronizer.initializeElasticsearchIndexMappings(sampleModel.getProperties())
                .isAcknowledged();

        assertTrue("Elasticsearch mappings weren't initialized", acknowledged);

        indexSampleDocuments();
    }

    private void indexSampleDocuments()
    {

        indexDocument("Cloud", "text number one",
                new GregorianCalendar(2018, Calendar.JUNE, 25, 5, 0).getTime());
        indexDocument("Aerith", "text number one",
                new GregorianCalendar(2018, Calendar.AUGUST, 25, 5, 0).getTime());
        indexDocument("Aerith", "number number three",
                new GregorianCalendar(2018, Calendar.AUGUST, 25, 5, 0).getTime());
        indexDocument("Red", "text four text text",
                new GregorianCalendar(2017, Calendar.MAY, 24, 5, 0).getTime());
        indexDocument(null, "text four", new Date());

        indexDocument("Pear", "fruit", Map.of("acme:projectNumber", "300", "acme:projectDetail", "Some details"));
        indexDocument("Apple", "another fruit", Map.of("acme:projectNumber", "200", "acme:projectDetail", "Some other details"));
        indexDocument("Banana", "yet another fruit",
                Map.of("acme:projectNumber", "100", "acme:projectDetail", "Different details"));
        indexDocument("Lettuce", "Not a fruit", Map.of("acme:projectNumber", "400", "acme:projectDetail", "No details"));

        doc100jpgByTom = indexDocument("100.txt", "hundred", Map.of(CONTENT_SIZE, 100, CONTENT_MIME_TYPE, MIMETYPE_IMAGE_JPEG, USER_MODIFIER, "tom"));
        doc500pdfByAdam = indexDocument("500.jpg", "five hundred", Map.of(CONTENT_SIZE, 500, CONTENT_MIME_TYPE, MIMETYPE_PDF, USER_MODIFIER, "adam"));
        doc100_000txtByJohn = indexDocument("100000.pdf", "hundred thousand", Map.of(CONTENT_SIZE, 100_000, CONTENT_MIME_TYPE, MIMETYPE_TEXT_PLAIN, USER_MODIFIER, "john"));

        batchOfDocuments = IntStream.rangeClosed(1, 30)
                .mapToObj(i -> indexDocument("Batch", "batch_" + i))
                .toList();
    }

    @Test
    public void whenSearchWithSortFieldDesc_shouldReturnDocumentSortedByFieldDesc()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("text");
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, "cm:name", false));
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        ResultSet rs = aftsQueryExecutor.executeQuery(searchParams);

        List<Serializable> sortedNames = Streams.stream(rs.iterator())
                .map(row -> row.getValue(namePropertyQualified))
                .map(name -> name == null ? "" : name)
                .collect(Collectors.toList());
        assertEquals(List.of("Red", "Cloud", "Aerith", ""), sortedNames);
    }

    @Test
    public void whenSearchWithSortFieldNotIndexed_shouldReturnDocument()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("fruit");
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addSort(
                new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, "acme:projectDetail", false));
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, NAME, false));
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        ResultSet rs = aftsQueryExecutor.executeQuery(searchParams);

        List<Serializable> sortedNames = Streams.stream(rs.iterator()).map(row -> row.getValue(namePropertyQualified))
                .map(name -> name == null ? "" : name).collect(Collectors.toList());
        assertEquals(List.of("Pear", "Lettuce", "Banana", "Apple"), sortedNames);
    }

    @Test
    public void whenSearchWithSortTokenizedField_shouldReturnDocument()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("fruit");
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addSort(
                new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, "acme:projectNumber", false));
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, NAME, false));
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        ResultSet rs = aftsQueryExecutor.executeQuery(searchParams);

        List<Serializable> sortedNames = Streams.stream(rs.iterator()).map(row -> row.getValue(namePropertyQualified))
                .map(name -> name == null ? "" : name).collect(Collectors.toList());
        assertEquals(List.of("Pear", "Lettuce", "Banana", "Apple"), sortedNames);
    }

    @Test
    public void whenSearchWithScoreSort_shouldReturnDocumentSortedByScore()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("text");
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.SCORE, null, false));
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        ResultSet rs = aftsQueryExecutor.executeQuery(searchParams);

        List<Float> sortedScores = Streams.stream(rs.iterator())
                .map(row -> row.getScore())
                .collect(Collectors.toList());

        assertTrue("scores should be sorted in reverse ordering", Ordering.natural().reverse().isOrdered(sortedScores));
    }

    @Test
    public void whenSearchWithTwoSorts_shouldReturnDocumentsSortedAccordinglyWithBoth()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("TEXT:number");
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.SCORE, null, false));
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, NAME, false));
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        ResultSet rs = aftsQueryExecutor.executeQuery(searchParams);

        List<Serializable> sortedNames = Streams.stream(rs.iterator())
                .map(row -> row.getValue(namePropertyQualified))
                .map(name -> name == null ? "" : name)
                .collect(Collectors.toList());
        assertEquals(List.of("Aerith", "Cloud", "Aerith"), sortedNames);

    }

    @Test
    public void whenSearchWithTwoSortDate_shouldReturnDocumenstSortedByDate()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("text");
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, "cm:modified", true));
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        ResultSet rs = aftsQueryExecutor.executeQuery(searchParams);

        List<Serializable> sortedNames = Streams.stream(rs.iterator())
                .map(row -> row.getValue(namePropertyQualified))
                .map(name -> name == null ? "" : name)
                .collect(Collectors.toList());
        assertEquals(List.of("Red", "Cloud", "Aerith", ""), sortedNames);
    }

    @Test
    public void whenSearchSortByContent_shouldBeIgnored()
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("number");
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, "cm:content", false));
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        ResultSet rs = aftsQueryExecutor.executeQuery(searchParams);

        List<String> actualResults = Streams.stream(rs.iterator())
                .map(row -> (String) row.getValue(namePropertyQualified))
                .map(name -> name == null ? "" : name)
                .collect(Collectors.toList());
        // Sort the list before comparing because we don't care what order the results come back in.
        Collections.sort(actualResults);
        List<String> expectedResults = List.of("Aerith", "Aerith", "Cloud");
        assertEquals("Unexpected values returned", expectedResults, actualResults);
    }

    @Test
    public void testSearchByDocSize_shouldReturnDocumentsSortedBySize()
    {
        // given
        SearchParameters searchParameters = searchParametersWith("hundred", CONTENT_SIZE);

        // when
        ResultSet resultSet = aftsQueryExecutor.executeQuery(searchParameters);

        // then
        List<String> actualResult = Streams.stream(resultSet.iterator()).map(row -> row.getNodeRef().toString()).collect(Collectors.toList());
        List<String> expectedResultOrder = List.of(doc100jpgByTom.toString(), doc500pdfByAdam.toString(), doc100_000txtByJohn.toString());
        assertEquals(expectedResultOrder, actualResult);
    }

    @Test
    public void testSearchByDocSize_shouldReturnDocumentsSortedBySizeDescending()
    {
        // given
        SearchParameters searchParameters = searchParametersWith("hundred", CONTENT_SIZE, false);

        // when
        ResultSet resultSet = aftsQueryExecutor.executeQuery(searchParameters);

        // then
        List<String> actualResult = Streams.stream(resultSet.iterator()).map(row -> row.getNodeRef().toString()).collect(Collectors.toList());
        List<String> expectedResultOrder = List.of(doc100_000txtByJohn.toString(), doc500pdfByAdam.toString(), doc100jpgByTom.toString());
        assertEquals(expectedResultOrder, actualResult);
    }

    @Test
    public void testSearchByDocSize_shouldReturnDocumentsSortedByMimetype()
    {
        // given
        SearchParameters searchParameters = searchParametersWith("hundred", CONTENT_MIME_TYPE);

        // when
        ResultSet resultSet = aftsQueryExecutor.executeQuery(searchParameters);

        // then
        List<String> actualResult = Streams.stream(resultSet.iterator()).map(row -> row.getNodeRef().toString()).collect(Collectors.toList());
        List<String> expectedResultOrder = List.of(doc500pdfByAdam.toString(), doc100jpgByTom.toString(), doc100_000txtByJohn.toString());
        assertEquals(expectedResultOrder, actualResult);
    }

    @Test
    public void whenSearchSortByDocument_shouldReturnDocumentsSortedByIndexOrder()
    {
        // given
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery("batch_");
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.DOCUMENT, null, false));
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        // when
        ResultSet firstPageResultSet = aftsQueryExecutor.executeQuery(searchParams);

        // then
        assertThat(firstPageResultSet.getNumberFound())
                .as("Check if first page has total number of documents")
                .isEqualTo(batchOfDocuments.size());
        List<ResultSetRow> firstPageHits = StreamSupport.stream(firstPageResultSet.spliterator(), false).toList();
        assertThat(firstPageHits)
                .as("Check if first page contains 10 documents")
                .hasSize(10);

        // when
        searchParams.setSkipCount(10);
        ResultSet secondPageResultSet = aftsQueryExecutor.executeQuery(searchParams);

        // then
        assertThat(secondPageResultSet.getNumberFound())
                .as("Check if second page has total number of documents")
                .isEqualTo(batchOfDocuments.size());
        List<ResultSetRow> secondPageHits = StreamSupport.stream(secondPageResultSet.spliterator(), false).toList();
        assertThat(secondPageHits)
                .as("Check if second page contains 10 documents")
                .hasSize(10);

        // when
        searchParams.setSkipCount(20);
        ResultSet thirdPageResultSet = aftsQueryExecutor.executeQuery(searchParams);

        // then
        assertThat(thirdPageResultSet.getNumberFound())
                .as("Check if third page has total number of documents")
                .isEqualTo(batchOfDocuments.size());
        List<ResultSetRow> thirdPageHits = StreamSupport.stream(thirdPageResultSet.spliterator(), false).toList();
        assertThat(thirdPageHits)
                .as("Check if third page contains 10 documents")
                .hasSize(10);

        // when
        searchParams.setSkipCount(30);

        // then
        assertThat(aftsQueryExecutor.executeQuery(searchParams))
                .as("Check if there are no more documents to fetch")
                .isEmpty();

        assertThat(Stream.of(firstPageHits, secondPageHits, thirdPageHits).flatMap(List::stream).toList())
                .as("Check if all 3 pages in total contain all documents")
                .extracting(ResultSetRow::getNodeRef)
                // the actual index order can't be easily predicted, so we just check if all documents are present
                .containsExactlyInAnyOrderElementsOf(batchOfDocuments);
    }

    @Test
    public void testSearchByDocSize_shouldReturnDocumentsSortedByMimetypeDescending()
    {
        // given
        SearchParameters searchParameters = searchParametersWith("hundred", CONTENT_MIME_TYPE, false);

        // when
        ResultSet resultSet = aftsQueryExecutor.executeQuery(searchParameters);

        // then
        List<String> actualResult = Streams.stream(resultSet.iterator()).map(row -> row.getNodeRef().toString()).collect(Collectors.toList());
        List<String> expectedResultOrder = List.of(doc100_000txtByJohn.toString(), doc100jpgByTom.toString(), doc500pdfByAdam.toString());
        assertEquals(expectedResultOrder, actualResult);
    }

    @Test
    public void testSearchByDocSize_shouldReturnDocumentsSortedByModifier()
    {
        // given
        SearchParameters searchParameters = searchParametersWith("hundred", USER_MODIFIER);

        // when
        ResultSet resultSet = aftsQueryExecutor.executeQuery(searchParameters);

        // then
        List<String> actualResult = Streams.stream(resultSet.iterator()).map(row -> row.getNodeRef().toString()).collect(Collectors.toList());
        List<String> expectedResultOrder = List.of(doc500pdfByAdam.toString(), doc100_000txtByJohn.toString(), doc100jpgByTom.toString());
        assertEquals(expectedResultOrder, actualResult);
    }

    @Test
    public void testSearchByDocSize_shouldReturnDocumentsSortedByModifierDescending()
    {
        // given
        SearchParameters searchParameters = searchParametersWith("hundred", USER_MODIFIER, false);

        // when
        ResultSet resultSet = aftsQueryExecutor.executeQuery(searchParameters);

        // then
        List<String> actualResult = Streams.stream(resultSet.iterator()).map(row -> row.getNodeRef().toString()).collect(Collectors.toList());
        List<String> expectedResultOrder = List.of(doc100jpgByTom.toString(), doc100_000txtByJohn.toString(), doc500pdfByAdam.toString());
        assertEquals(expectedResultOrder, actualResult);
    }

    private static SearchParameters searchParametersWith(String query, String sortBy)
    {
        return searchParametersWith(query, sortBy, true);
    }

    private static SearchParameters searchParametersWith(String query, String sortBy, boolean sortAscending)
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setQuery(query);
        searchParams.setLimit(10);
        searchParams.setSkipCount(0);
        searchParams.addSort(new SearchParameters.SortDefinition(SearchParameters.SortDefinition.SortType.FIELD, sortBy, sortAscending));
        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        return searchParams;
    }
}
