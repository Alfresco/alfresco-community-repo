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
package org.alfresco.repo.search.impl.elasticsearch.permission;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_OWNER;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_READER;
import static org.alfresco.service.cmr.security.AuthorityType.getAuthorityType;
import static org.alfresco.service.cmr.security.PermissionService.ADMINISTRATOR_AUTHORITY;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;

import org.alfresco.repo.search.adaptor.QueryConstants;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * This class aims to apply a permission filter to a query using the Flat approach. This means that permission each Elasticsearch document includes two additional list fields: READER and DENIED.
 */
public class FlatElasticsearchPermissionQueryFactory implements ElasticsearchPermissionQueryFactory
{

    private final PermissionService permissionService;
    private final HashSet<String> globalReaders;

    public FlatElasticsearchPermissionQueryFactory(PermissionService permissionService)
    {
        this.permissionService = permissionService;
        this.globalReaders = GlobalReaders.getReaders();
    }

    /**
     * @param queryBuilder
     *            the query to filter using permissions
     * @param includeGroupsForRoleAdmin
     *            define if groups belongs to an admin should be used or not.
     * @return a QueryBuilder object including permission filter.
     */
    @Override
    public Query getQueryWithPermissionFilter(Query queryBuilder, boolean includeGroupsForRoleAdmin)
    {
        Set<String> authorities = getAuthorisations(includeGroupsForRoleAdmin);

        if (hasGlobalReader(authorities))
        {
            // No filter will be applied, so a unfiltered query will be returned
            return queryBuilder;
        }
        else
        {
            BoolQuery permissionQueryBuilder = buildFilter(authorities);
            return applyFilter(queryBuilder, permissionQueryBuilder).toQuery();
        }
    }

    /**
     * Check if the authorities list contains at least one global reader
     * 
     * @param authorities
     * @return
     */
    private boolean hasGlobalReader(Set<String> authorities)
    {
        return authorities.stream().anyMatch(globalReaders::contains);
    }

    /**
     * @param queryBuilder
     *            The query will be wrapped in a BoolQueryBuilder in order to apply the filter.
     * @param permissionQueryBuilder
     *            the permission query to add as filter to the current query.
     * @return a query with a permission filter
     */
    private BoolQuery applyFilter(Query queryBuilder, BoolQuery permissionQueryBuilder)
    {
        return QueryBuilders
                .bool()
                .must(queryBuilder)
                .filter(permissionQueryBuilder.toQuery()).build();
    }

    /**
     * @param authorities
     *            that must be used in the query
     * @return a BoolQueryBuilder object with a "should" clause for each authority.
     */
    private BoolQuery buildFilter(Set<String> authorities)
    {
        BoolQuery.Builder aclQueryBuilder = QueryBuilders.bool();
        authorities.forEach(auth -> aclQueryBuilder.should(new Query.Builder().match(new MatchQuery.Builder().field(FIELD_READER).query(FieldValue.of(auth)).build()).build()));
        authorities.forEach(auth -> aclQueryBuilder.mustNot(new Query.Builder().match(new MatchQuery.Builder().field(QueryConstants.FIELD_DENIED).query(FieldValue.of(auth)).build()).build()));
        addOwnerQuery(authorities, aclQueryBuilder);
        return aclQueryBuilder.build();
    }

    /**
     * @param includeGroupsForRoleAdmin
     *            if false and if the user has role ROLE_ADMINISTRATOR, groups will be removed from the list.
     * @return an authorities set
     */
    private Set<String> getAuthorisations(boolean includeGroupsForRoleAdmin)
    {
        Set<String> allAuthorisations = permissionService.getAuthorisations();
        boolean includeGroups = includeGroupsForRoleAdmin || !allAuthorisations.contains(ADMINISTRATOR_AUTHORITY);

        return allAuthorisations.stream()
                .filter(authority -> includeGroups || getAuthorityType(authority) != AuthorityType.GROUP)
                .collect(Collectors.toSet());
    }

    /**
     * Add the owner query to the current query
     * 
     * @param authorities
     *            list of authorities that could contain the username * @param aclQueryBuilder the target query where adding the owner query if the authorities list contains a username.
     */
    private void addOwnerQuery(Set<String> authorities, BoolQuery.Builder aclQueryBuilder)
    {
        getUser(authorities)
                .map(user -> getOwnerQuery(user))
                .ifPresent(aclQueryBuilder::should);
    }

    /**
     * 
     * @param owner
     *            username
     * @return the search query for the owner depending on global readers list
     */
    private Query getOwnerQuery(String owner)
    {
        Query ownerQuery;
        TermQuery.Builder basicOwnerQuery = QueryBuilders.term().field(FIELD_OWNER).value(FieldValue.of(owner));

        if (globalReaders.contains(permissionService.getOwnerAuthority()))
        {
            ownerQuery = basicOwnerQuery.build().toQuery();
        }
        else
        {
            ownerQuery = QueryBuilders
                    .bool()
                    .must(basicOwnerQuery.build().toQuery())
                    .must(new Query.Builder().match(new MatchQuery.Builder().field(FIELD_READER).query(FieldValue.of(permissionService.getOwnerAuthority())).build()).build()).build().toQuery();

        }
        return ownerQuery;
    }

    /**
     * @param authorities
     *            list of authorities that could contain the username
     * @return the username of logged user
     */
    private Optional<String> getUser(Set<String> authorities)
    {
        return authorities.stream().filter(current -> getAuthorityType(current) == AuthorityType.USER).findFirst();
    }

}
