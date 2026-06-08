/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.List.of;
import static java.util.Optional.ofNullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.alfresco.repo.search.SearchEngineResultSet;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.Pair;
import org.alfresco.util.testing.category.NeverRunsTests;

@Category(NeverRunsTests.class)
@SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
public class FieldFacetIT extends LuceneOrAFTSQueryIT
{
    public FieldFacetIT(String language)
    {
        super(language);
    }

    @Before
    public void initDocuments()
    {
        indexDocument("Mario Rossi", "A website on building software effectively");
        indexDocument("Mario Rossi", "A website on building software effectively (hardcover)");
        indexDocument("Mario Rossi", "A website on building software effectively, 2017");
        indexDocument("Mario Rossi", "A website on building software effectively, FUP 2020");
        indexDocument("Mario Rossi", "A website on building software effectively, (CDROM)");

        indexDocument("Alexa Green", "Object-Oriented Software");
        indexDocument("Alexa Green", "Object-Oriented Software 2nd Edition");
        indexDocument("Alexa Green", "Object-Oriented Software Reprinted");

        indexDocument("John Doe", "Agile Software Development");
        indexDocument("John Doe", "Agile Software Development Part II");
        indexDocument("John Doe", "Agile Software Development Second Edition");
        indexDocument("John Doe", "Agile Software Development Third Edition");

        indexDocument("Alexander The Great", "My son, ask for thyself another kingdom");
        indexDocument("Alexander The Great", "For that which I leave is too small for thee");
        indexDocument("Alexander The Great", "In an ancient land called Macedonia");
        indexDocument("Alexander The Great", "The Scythians fell by the river Jaxartes");
        indexDocument("Alexander The Great", "And he founded the city called Alexandria");
        indexDocument("Alexander The Great", "A Phrygian King had bound a chariot yoke");
    }

    @Test
    public void noFieldFacets()
    {
        var response = searchWithFieldFacets(language, name() + ":*", emptyList());
        assertTrue(response.getFieldFacets().isEmpty());
    }

    @Test
    public void qualifiedFieldFacet()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareQualifiedNameField());
        facet.setMinCount(1);
        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexander The Great", 6),
                pair("Mario Rossi", 5),
                pair("John Doe", 4),
                pair("Alexa Green", 3));

        assertEquals(expected, response.getFieldFacet(syntaxAwareQualifiedNameField()));
    }

    @Test
    public void fullyQualifiedFieldFacet()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareFullyQualifiedNameField());
        facet.setMinCount(1);
        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexander The Great", 6),
                pair("Mario Rossi", 5),
                pair("John Doe", 4),
                pair("Alexa Green", 3));

        assertEquals(expected, response.getFieldFacet(syntaxAwareFullyQualifiedNameField()));
    }

    @Test
    public void unqualifiedFieldFacet()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareUnqualifiedNameField());
        facet.setMinCount(1);
        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexander The Great", 6),
                pair("Mario Rossi", 5),
                pair("John Doe", 4),
                pair("Alexa Green", 3));

        assertEquals(expected, response.getFieldFacet(syntaxAwareUnqualifiedNameField()));
    }

    @Test
    public void basicFieldFacet()
    {
        String fieldName = "READER";
        var facet = new SearchParameters.FieldFacet(fieldName);

        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));
        var expected = List.of(pair("GROUP_EVERYONE", 18));

        assertEquals(expected, response.getFieldFacet(fieldName));
    }

    @Test
    public void twoFieldFacets()
    {
        String firstFacet = "READER";
        String secondFacet = syntaxAwareQualifiedNameField();

        var readerFacet = new SearchParameters.FieldFacet(firstFacet);
        readerFacet.setMinCount(1);

        var nameFacet = new SearchParameters.FieldFacet(secondFacet);
        nameFacet.setMinCount(1);

        var response = searchWithFieldFacets(language, name() + ":*", List.of(nameFacet, readerFacet));

        assertEquals(
                of(pair("GROUP_EVERYONE", 18)),
                response.getFieldFacet(firstFacet));

        assertEquals(List.of(
                pair("Alexander The Great", 6),
                pair("Mario Rossi", 5),
                pair("John Doe", 4),
                pair("Alexa Green", 3)),
                response.getFieldFacet(secondFacet));
    }

    @Test
    public void labeledFieldFacet()
    {
        String label = "thisIsTheLabelForTheNameFieldAbove";

        var facet = new SearchParameters.FieldFacet(syntaxAwareQualifiedNameField());
        facet.setMinCount(1);
        facet.setLabel(label);

        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexander The Great", 6),
                pair("Mario Rossi", 5),
                pair("John Doe", 4),
                pair("Alexa Green", 3));

        assertTrue(response.getFieldFacet(syntaxAwareQualifiedNameField()).isEmpty());
        assertEquals(expected, response.getFieldFacet(label));
    }

    @Test
    public void indexOrderedBuckets()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareQualifiedNameField());
        facet.setMinCount(1);
        facet.setSort(SearchParameters.FieldFacetSort.INDEX);

        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexa Green", 3),
                pair("Alexander The Great", 6),
                pair("John Doe", 4),
                pair("Mario Rossi", 5));

        assertEquals(expected, response.getFieldFacet(syntaxAwareQualifiedNameField()));
    }

    @Test
    public void countOrderedBuckets()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareQualifiedNameField());
        facet.setMinCount(1);
        facet.setSort(SearchParameters.FieldFacetSort.COUNT);

        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexander The Great", 6),
                pair("Mario Rossi", 5),
                pair("John Doe", 4),
                pair("Alexa Green", 3));

        assertEquals(expected, response.getFieldFacet(syntaxAwareQualifiedNameField()));
    }

    @Test
    public void prefixedFieldFacet()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareQualifiedNameField());
        facet.setPrefix("Alex");

        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexander The Great", 6),
                pair("Alexa Green", 3));

        assertEquals(expected, response.getFieldFacet(syntaxAwareQualifiedNameField()));
    }

    @Test
    public void missingBucket()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareQualifiedNameField());
        facet.setCountDocsMissingFacetField(true);

        var response = searchWithFieldFacets(language, "software", singletonList(facet));

        assertTrue(response.getFieldFacet(syntaxAwareQualifiedNameField()).contains(pair("null", 0)));
    }

    @Test
    public void limitedFieldFacet()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareQualifiedNameField());
        facet.setLimitOrNull(2);

        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexander The Great", 6),
                pair("Mario Rossi", 5));

        assertEquals(expected, response.getFieldFacet(syntaxAwareQualifiedNameField()));
    }

    @Test
    public void minCountFieldFacet()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareQualifiedNameField());
        facet.setMinCount(4);

        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexander The Great", 6),
                pair("Mario Rossi", 5),
                pair("John Doe", 4));

        assertEquals(expected, response.getFieldFacet(syntaxAwareQualifiedNameField()));
    }

    @Test
    public void minCountFieldFacet222()
    {
        var facet = new SearchParameters.FieldFacet(syntaxAwareQualifiedNameField());
        facet.setMinCount(4);

        var response = searchWithFieldFacets(language, name() + ":*", singletonList(facet));

        var expected = List.of(
                pair("Alexander The Great", 6),
                pair("Mario Rossi", 5),
                pair("John Doe", 4));

        assertEquals(expected, response.getFieldFacet(syntaxAwareQualifiedNameField()));
    }

    private Pair<String, Integer> pair(String name, Integer value)
    {
        return new Pair<>(name, value);
    }

    private SearchEngineResultSet searchWithFieldFacets(String language, String query, List<SearchParameters.FieldFacet> facets)
    {
        SearchParameters searchParams = new SearchParameters();
        searchParams.setLanguage(language);
        searchParams.setQuery(query);
        searchParams.setSkipCount(0);
        searchParams.setMaxItems(20);

        ofNullable(facets)
                .stream()
                .flatMap(Collection::stream)
                .forEach(searchParams::addFieldFacet);

        searchParams.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        var resultSet = "lucene".equals(language)
                ? luceneQueryExecutor.executeQuery(searchParams)
                : aftsQueryExecutor.executeQuery(searchParams);

        return Optional.of(resultSet)
                .map(SearchEngineResultSet.class::cast)
                .orElseThrow(() -> new RuntimeException("No ResultSet (null) available in response"));
    }

    private String syntaxAwareUnqualifiedNameField()
    {
        return name();
    }

    private String syntaxAwareQualifiedNameField()
    {
        return searchSyntaxAwareName("cm:name");
    }

    private String syntaxAwareFullyQualifiedNameField()
    {
        return searchSyntaxAwareName("{http://www.alfresco.org/model/content/1.0}name");
    }

    private String name()
    {
        return searchSyntaxAwareName("name");
    }

    private String searchSyntaxAwareName(String name)
    {
        return isLuceneSyntaxInUse() ? "@" + name : name;
    }
}
