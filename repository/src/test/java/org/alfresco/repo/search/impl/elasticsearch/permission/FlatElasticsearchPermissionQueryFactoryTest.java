/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.permission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.util.collections.Sets.newSet;

import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.OWNER;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.READER;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;

import org.alfresco.repo.search.adaptor.QueryConstants;
import org.alfresco.service.cmr.security.PermissionService;

@RunWith(MockitoJUnitRunner.class)
public class FlatElasticsearchPermissionQueryFactoryTest
{

    @Mock
    private PermissionService permissionService;

    private FlatElasticsearchPermissionQueryFactory flatPermissionService;

    @Before
    public void setUp()
    {
        this.flatPermissionService = new FlatElasticsearchPermissionQueryFactory(permissionService);
        given(permissionService.getOwnerAuthority()).willReturn(PermissionService.OWNER_AUTHORITY);
    }

    @Test
    public void shouldGetUnfilteredQueryWhenUserIsASystemUser()
    {
        given(permissionService.getAuthorisations()).willReturn(newSet("System"));

        Query query = QueryBuilders.matchAll().build().toQuery();
        Query queryBuilderWithPermissionSystem = flatPermissionService.getQueryWithPermissionFilter(query, true);
        assertEquals(query, queryBuilderWithPermissionSystem);
    }

    @Test
    public void shouldGetUnfilteredQueryWhenUserIsAdmin()
    {
        given(permissionService.getAuthorisations()).willReturn(newSet(PermissionService.ADMINISTRATOR_AUTHORITY));

        Query query = QueryBuilders.matchAll().build().toQuery();
        Query queryBuilderWithPermissionSystem = flatPermissionService.getQueryWithPermissionFilter(query, true);
        assertEquals(query, queryBuilderWithPermissionSystem);
    }

    @Test
    public void shouldGetUnfilteredQueryWhenUserIsInGlobalReaders()
    {
        GlobalReaders.getReaders().add("CUSTOM");
        given(permissionService.getAuthorisations()).willReturn(newSet("CUSTOM"));

        Query query = QueryBuilders.matchAll().build().toQuery();
        Query queryBuilderWithPermissionSystem = flatPermissionService.getQueryWithPermissionFilter(query, true);
        assertEquals(query, queryBuilderWithPermissionSystem);
    }

    @Test
    public void shouldGetQueryWithPermissionFilterForNormalUser()
    {
        given(permissionService.getAuthorisations()).willReturn(newSet("username", "GROUP_a", "GROUP_b"));

        Query query = QueryBuilders.matchAll().build().toQuery();
        Query queryBuilderWithPermission = flatPermissionService.getQueryWithPermissionFilter(query, true);
        BoolQuery filteredQuery = queryBuilderWithPermission.bool();

        assertEquals(1, filteredQuery.must().size());
        assertEquals(query, filteredQuery.must().get(0));

        verify(permissionService, atMostOnce()).getAllAuthorities();
        assertAuthoritiesInFilterQuery(filteredQuery, "username", "GROUP_a", "GROUP_b");
    }

    @Test
    public void shouldGetQueryWithPermissionFilterWhenRoleOwnerIsNotGlobalReader()
    {
        GlobalReaders.getReaders().remove(permissionService.getOwnerAuthority());
        given(permissionService.getAuthorisations()).willReturn(newSet("username"));

        Query query = QueryBuilders.matchAll().build().toQuery();
        Query queryBuilderWithPermission = flatPermissionService.getQueryWithPermissionFilter(query, true);
        // Restore global readers list
        GlobalReaders.getReaders().add(permissionService.getOwnerAuthority());
        BoolQuery filteredQuery = queryBuilderWithPermission.bool();

        assertEquals(1, filteredQuery.must().size());
        assertEquals(query, filteredQuery.must().get(0));

        verify(permissionService, atMostOnce()).getAllAuthorities();
        assertAuthoritiesInFilterQuery(filteredQuery, true, "username");
    }

    @Test
    public void shouldGetQueryWithPermissionFilterForNormalUser_whenSourceQueryIsABoolQuery()
    {
        given(permissionService.getAuthorisations()).willReturn(newSet("username", "GROUP_a", "GROUP_b"));

        BoolQuery query = QueryBuilders.bool().mustNot(new Query.Builder().term(new TermQuery.Builder().field("test").value(FieldValue.of("test")).build()).build()).build();
        Query queryBuilderWithPermission = flatPermissionService.getQueryWithPermissionFilter(query.toQuery(), true);
        BoolQuery filteredQuery = queryBuilderWithPermission.bool();

        assertEquals(1, filteredQuery.must().size());
        assertEquals(query, filteredQuery.must().get(0).bool());

        verify(permissionService, atMostOnce()).getAllAuthorities();
        assertAuthoritiesInFilterQuery(filteredQuery, "username", "GROUP_a", "GROUP_b");
    }

    @Test
    public void shouldGetQueryWithPermissionFilterForAdmin_whenIncludeGroupsForRoleAdmin()
    {
        // the IncludeGroupsForRoleAdmin flag has no effect if the ADMINISTRATOR_AUTHORITY is in global readers
        GlobalReaders.getReaders().remove(PermissionService.ADMINISTRATOR_AUTHORITY);
        given(permissionService.getAuthorisations())
                .willReturn(newSet("admin", "GROUP_a", PermissionService.ADMINISTRATOR_AUTHORITY));

        BoolQuery query = QueryBuilders.bool().mustNot(new Query.Builder().term(new TermQuery.Builder().field("test").value(FieldValue.of("test")).build()).build()).build();
        Query queryBuilderWithPermission = flatPermissionService.getQueryWithPermissionFilter(query.toQuery(), true);
        GlobalReaders.getReaders().add(PermissionService.ADMINISTRATOR_AUTHORITY);
        BoolQuery filteredQuery = queryBuilderWithPermission.bool();

        assertEquals(1, filteredQuery.must().size());
        assertEquals(query, filteredQuery.must().get(0).bool());

        verify(permissionService, atMostOnce()).getAllAuthorities();
        assertAuthoritiesInFilterQuery(filteredQuery, "admin", "GROUP_a", PermissionService.ADMINISTRATOR_AUTHORITY);
    }

    @Test
    public void shouldGetQueryWithPermissionFilterForAdmin_whenNotIncludeGroupsForRoleAdmin()
    {
        // the IncludeGroupsForRoleAdmin flag has no effect if the ADMINISTRATOR_AUTHORITY is in global readers
        GlobalReaders.getReaders().remove(PermissionService.ADMINISTRATOR_AUTHORITY);
        given(permissionService.getAuthorisations())
                .willReturn(newSet("admin", "GROUP_a", PermissionService.ADMINISTRATOR_AUTHORITY));

        BoolQuery query = QueryBuilders.bool().mustNot(new Query.Builder().term(new TermQuery.Builder().field("test").value(FieldValue.of("test")).build()).build()).build();
        Query queryBuilderWithPermission = flatPermissionService.getQueryWithPermissionFilter(query.toQuery(), false);
        GlobalReaders.getReaders().add(PermissionService.ADMINISTRATOR_AUTHORITY);
        BoolQuery filteredQuery = queryBuilderWithPermission.bool();

        assertEquals(1, filteredQuery.must().size());
        assertEquals(query, filteredQuery.must().get(0).bool());

        verify(permissionService, atMostOnce()).getAllAuthorities();
        assertAuthoritiesInFilterQuery(filteredQuery, "admin", PermissionService.ADMINISTRATOR_AUTHORITY);
    }

    private void assertAuthoritiesInFilterQuery(BoolQuery filteredQuery, String... expectedAuths)
    {
        assertAuthoritiesInFilterQuery(filteredQuery, false, expectedAuths);
    }

    private void assertAuthoritiesInFilterQuery(BoolQuery filteredQuery, boolean roleOwnerInReaders, String... expectedAuths)
    {
        List<Query> filter = filteredQuery.filter();
        assertEquals(1, filter.size());
        Query filterQuery = filter.get(0).bool().toQuery();

        List<Query> should = filterQuery.bool().should();
        List<Query> mustNot = filterQuery.bool().mustNot();
        int authoritiesPlusOwnerQueryCount = expectedAuths.length + 1;
        assertEquals(authoritiesPlusOwnerQueryCount, should.size());

        String ownerName = expectedAuths[0];
        assertOwnerQuery(ownerName, should, roleOwnerInReaders);

        assertAuthorities(expectedAuths, mustNot, should);
    }

    private void assertAuthorities(String[] expectedAuths, List<Query> deniedQueries,
            List<Query> readerQueries)
    {

        for (String expected : expectedAuths)
        {
            readerQueries.forEach(readerQuery -> {
                if (readerQuery._kind().jsonValue().equals("match") && readerQuery.match().query()._get().toString().equals(expected) && readerQuery.match().field().equals(QueryConstants.FIELD_READER))
                {

                    boolean contains = true;
                    assertTrue("Permission reader filter doesn't check " + expected, contains);
                }
            });

            deniedQueries.forEach(deniedQuery -> {
                if (deniedQuery._kind().jsonValue().equals("match") && deniedQuery.match().query()._get().toString().equals(expected) && deniedQuery.match().field().equals(QueryConstants.FIELD_DENIED))
                {

                    boolean contains = true;
                    assertTrue("Permission denied filter doesn't check " + expected, contains);
                }
            });
        }
    }

    private void assertOwnerQuery(String ownerName, List<Query> queries, boolean roleOwnerInReaders)
    {
        if (roleOwnerInReaders)
        {
            BoolQuery ownerQuery = queries.stream().filter(o -> o._kind().jsonValue().equals("bool"))
                    .findFirst()
                    .get()
                    .bool();

            var termQuery = ownerQuery.must().get(0).term();
            assertEquals(termQuery.field(), OWNER);
            assertEquals(termQuery.value()._get().toString(), ownerName);

            var matchQuery = ownerQuery.must().get(1).match();
            assertEquals(matchQuery.field(), READER);
            assertEquals(matchQuery.query()._get().toString(), permissionService.getOwnerAuthority());

        }
        else
        {
            TermQuery termQuery = queries.stream().filter(o -> o._kind().jsonValue().equals("term")).findFirst().get().term();

            if (termQuery.field().equals(OWNER) && termQuery.value().equals(ownerName))
            {
                boolean contains = true;
                assertTrue("Query is not checking the owner as expected", contains);
            }
        }
    }
}
