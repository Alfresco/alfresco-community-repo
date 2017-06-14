/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.impl;

import java.text.Collator;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authority.AuthorityInfo;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Groups;
import org.alfresco.rest.api.model.Group;
import org.alfresco.rest.api.model.GroupMember;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalkerOrSupported;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Centralises access to groups services and maps between representations.
 * 
 * @author cturlica
 */
public class GroupsImpl implements Groups
{
    private static final String DISPLAY_NAME = "displayName";
    private static final String SHORT_NAME = "shortName";
    // private static final String AUTHORITY_NAME = "authorityName";

    private final static Map<String, String> SORT_PARAMS_TO_NAMES;
    static
    {
        Map<String, String> aMap = new HashMap<>(2);
        aMap.put(PARAM_ID, SHORT_NAME);
        aMap.put(PARAM_DISPLAY_NAME, DISPLAY_NAME);

        SORT_PARAMS_TO_NAMES = Collections.unmodifiableMap(aMap);
    }

    // List groups filtering (via where clause)
    private final static Set<String> LIST_GROUPS_EQUALS_QUERY_PROPERTIES = new HashSet<>(Arrays.asList(new String[] { PARAM_IS_ROOT }));

    private final static Set<String> LIST_GROUP_MEMBERS_QUERY_PROPERTIES = new HashSet<>(Arrays.asList(new String[] { PARAM_MEMBER_TYPE }));

    protected AuthorityService authorityService;

    public AuthorityService getAuthorityService()
    {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public Group getGroup(String groupId, Parameters parameters) throws EntityNotFoundException
    {
        AuthorityInfo authorityInfo = getAuthorityInfo(groupId);

        final Set<String> rootAuthorities = getAllRootAuthorities(AuthorityType.GROUP);
        final List<String> includeParam = parameters.getInclude();

        return getGroup(authorityInfo, includeParam, rootAuthorities);
    }

    public CollectionWithPagingInfo<Group> getGroups(final Parameters parameters)
    {
        final List<String> includeParam = parameters.getInclude();

        Paging paging = parameters.getPaging();

        // Retrieve sort column. This is limited for now to sort column due to
        // v0 api implementation. Should be improved in the future.
        Pair<String, Boolean> sortProp = getGroupsSortProp(parameters);

        // Parse where clause properties.
        Boolean isRootParam = null;
        Query q = parameters.getQuery();
        if (q != null)
        {
            MapBasedQueryWalkerOrSupported propertyWalker = new MapBasedQueryWalkerOrSupported(LIST_GROUPS_EQUALS_QUERY_PROPERTIES, null);
            QueryHelper.walk(q, propertyWalker);

            isRootParam = propertyWalker.getProperty(PARAM_IS_ROOT, WhereClauseParser.EQUALS, Boolean.class);
        }

        final AuthorityType authorityType = AuthorityType.GROUP;
        final Set<String> rootAuthorities = getAllRootAuthorities(authorityType);

        PagingResults<AuthorityInfo> pagingResult = getAuthoritiesInfo(authorityType, isRootParam, rootAuthorities, sortProp, paging);

        // Create response.
        final List<AuthorityInfo> page = pagingResult.getPage();
        int totalItems = pagingResult.getTotalResultCount().getFirst();
        List<Group> groups = new AbstractList<Group>()
        {
            @Override
            public Group get(int index)
            {
                AuthorityInfo authorityInfo = page.get(index);
                return getGroup(authorityInfo, includeParam, rootAuthorities);
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        return CollectionWithPagingInfo.asPaged(paging, groups, pagingResult.hasMoreItems(), totalItems);
    }

    private PagingResults<AuthorityInfo> getAuthoritiesInfo(AuthorityType authorityType, Boolean isRootParam, Set<String> rootAuthorities, Pair<String, Boolean> sortProp,
            Paging paging)
    {
        PagingResults<AuthorityInfo> pagingResult;
        if (isRootParam != null)
        {
            List<AuthorityInfo> groupList;

            if (isRootParam)
            {
                // Limit the post processing work by using the already loaded
                // list of root authorities.
                groupList = new ArrayList<>(rootAuthorities.size());
                groupList.addAll(rootAuthorities.stream().map(this::getAuthorityInfo).collect(Collectors.toList()));

                // Post process sorting - this should be moved to service
                // layer. It is done here because sorting is not supported at
                // service layer.
                AuthorityInfoComparator authorityComparator = new AuthorityInfoComparator(sortProp.getFirst(), sortProp.getSecond());
                Collections.sort(groupList, authorityComparator);
            }
            else
            {
                PagingRequest pagingNoMaxItems = new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE);

                // Get authorities using canned query but without using
                // the requested paginating now because we need to filter out
                // the root authorities.
                PagingResults<AuthorityInfo> nonPagingResult = authorityService.getAuthoritiesInfo(authorityType, null, null, sortProp.getFirst(), sortProp.getSecond(),
                        pagingNoMaxItems);

                // Post process filtering - this should be moved to service
                // layer. It is done here because filtering by "isRoot" is not
                // supported at service layer.
                groupList = nonPagingResult.getPage();
                if (groupList != null)
                {
                    for (Iterator<AuthorityInfo> i = groupList.iterator(); i.hasNext();)
                    {
                        AuthorityInfo authorityInfo = i.next();
                        if (!isRootParam.equals(isRootAuthority(rootAuthorities, authorityInfo.getAuthorityName())))
                        {
                            i.remove();
                        }
                    }
                }
            }

            // Post process paging - this should be moved to service layer.
            pagingResult = Util.wrapPagingResults(paging, groupList);
        }
        else
        {
            PagingRequest pagingRequest = Util.getPagingRequest(paging);

            // Get authorities using canned query.
            pagingResult = authorityService.getAuthoritiesInfo(authorityType, null, null, sortProp.getFirst(), sortProp.getSecond(), pagingRequest);
        }
        return pagingResult;
    }

    private Set<String> getAllRootAuthorities(AuthorityType authorityType)
    {
        Set<String> authorities;
        try
        {
            authorities = authorityService.getAllRootAuthorities(authorityType);
        }
        catch (UnknownAuthorityException e)
        {
            authorities = Collections.emptySet();
        }

        return authorities;
    }

    /**
     * Retrieve authority info by name. <b>Node id field isn't used at this time
     * and it is set to null.</b>
     * 
     * @param id
     *            The authority name.
     * @return The authority info.
     */
    private AuthorityInfo getAuthorityInfo(String id)
    {
        if (id == null || id.isEmpty())
        {
            throw new InvalidArgumentException("id is null or empty");
        }

        if (!authorityService.authorityExists(id))
        {
            throw new EntityNotFoundException(id);
        }

        String authorityDisplayName = authorityService.getAuthorityDisplayName(id);

        return new AuthorityInfo(null, authorityDisplayName, id);
    }

    private Group getGroup(AuthorityInfo authorityInfo, List<String> includeParam, Set<String> rootAuthorities)
    {
        if (authorityInfo == null)
        {
            return null;
        }

        Group group = new Group();
        group.setId(authorityInfo.getAuthorityName());

        // REPO-1743
        String authorityDisplayName = authorityInfo.getAuthorityDisplayName();
        if (authorityDisplayName == null || authorityDisplayName.isEmpty())
        {
            authorityDisplayName = authorityService.getAuthorityDisplayName(authorityInfo.getAuthorityName());
        }

        group.setDisplayName(authorityDisplayName);

        group.setIsRoot(isRootAuthority(rootAuthorities, authorityInfo.getAuthorityName()));

        // Optionally include
        if (includeParam != null)
        {
            if (includeParam.contains(PARAM_INCLUDE_PARENT_IDS))
            {
                Set<String> containingAuthorities = authorityService.getContainingAuthorities(AuthorityType.GROUP, authorityInfo.getAuthorityName(), true);
                group.setParentIds(containingAuthorities);
            }

            if (includeParam.contains(PARAM_INCLUDE_ZONES))
            {
                Set<String> authorityZones = authorityService.getAuthorityZones(authorityInfo.getAuthorityName());
                group.setZones(authorityZones);
            }
        }

        return group;
    }

    private boolean isRootAuthority(Set<String> rootAuthorities, String authorityName)
    {
        return rootAuthorities.contains(authorityName);
    }

    private Pair<String, Boolean> getGroupsSortProp(Parameters parameters)
    {
        Pair<String, Boolean> sortProp;
        List<SortColumn> sortCols = parameters.getSorting();

        if ((sortCols != null) && (sortCols.size() > 0))
        {
            if (sortCols.size() > 1)
            {
                throw new InvalidArgumentException("Multiple sort fields not allowed.");
            }

            SortColumn sortCol = sortCols.get(0);

            String sortPropName = SORT_PARAMS_TO_NAMES.get(sortCol.column);
            if (sortPropName == null)
            {
                throw new InvalidArgumentException("Invalid sort field: " + sortCol.column);
            }

            sortProp = new Pair<>(sortPropName, (sortCol.asc ? Boolean.TRUE : Boolean.FALSE));
        }
        else
        {
            sortProp = getGroupsSortPropDefault();
        }
        return sortProp;
    }

    /**
     * <p>
     * Returns the default sort order.
     * </p>
     *
     * @return The default <code>Pair&lt;QName, Boolean&gt;</code> sort
     *         property.
     */
    private Pair<String, Boolean> getGroupsSortPropDefault()
    {
        return new Pair<>(DISPLAY_NAME, Boolean.TRUE);
    }

    private class AuthorityInfoComparator implements Comparator<AuthorityInfo>
    {
        private Map<AuthorityInfo, String> nameCache;
        private String sortBy;
        private Collator col;
        private int orderMultiplier = 1;

        private AuthorityInfoComparator(String sortBy, boolean sortAsc)
        {
            col = Collator.getInstance(I18NUtil.getLocale());
            this.sortBy = sortBy;
            this.nameCache = new HashMap<>();

            if (!sortAsc)
            {
                orderMultiplier = -1;
            }
        }

        @Override
        public int compare(AuthorityInfo g1, AuthorityInfo g2)
        {
            return col.compare(get(g1), get(g2)) * orderMultiplier;
        }

        private String get(AuthorityInfo g)
        {
            String v = nameCache.get(g);
            if (v == null)
            {
                // Get the value from the group
                if (DISPLAY_NAME.equals(sortBy))
                {
                    v = g.getAuthorityDisplayName();
                }
                else if (SHORT_NAME.equals(sortBy))
                {
                    v = g.getAuthorityName();
                }
                else
                {
                    throw new InvalidArgumentException("Invalid sort field: " + sortBy);
                }

                // Lower case it for case insensitive search
                v = v.toLowerCase();

                // Cache it
                nameCache.put(g, v);
            }
            return v;
        }
    }

    public CollectionWithPagingInfo<GroupMember> getGroupMembers(String groupId, final Parameters parameters)
    {
        validateGroupId(groupId);

        Paging paging = parameters.getPaging();

        // Retrieve sort column. This is limited for now to sort column due to
        // v0 api implementation. Should be improved in the future.
        Pair<String, Boolean> sortProp = getGroupsSortProp(parameters);

        AuthorityType authorityType = null;

        // Parse where clause properties.
        Query q = parameters.getQuery();
        if (q != null)
        {
            MapBasedQueryWalkerOrSupported propertyWalker = new MapBasedQueryWalkerOrSupported(LIST_GROUP_MEMBERS_QUERY_PROPERTIES, null);
            QueryHelper.walk(q, propertyWalker);

            String memberTypeStr = propertyWalker.getProperty(PARAM_MEMBER_TYPE, WhereClauseParser.EQUALS, String.class);

            if (memberTypeStr != null && !memberTypeStr.isEmpty())
            {
                switch (memberTypeStr)
                {
                case PARAM_MEMBER_TYPE_GROUP:
                    authorityType = AuthorityType.GROUP;
                    break;
                case PARAM_MEMBER_TYPE_PERSON:
                    authorityType = AuthorityType.USER;
                    break;
                default:
                    throw new InvalidArgumentException("MemberType is invalid (expected eg. GROUP, PERSON)");
                }
            }
        }

        PagingResults<AuthorityInfo> pagingResult = getAuthoritiesInfo(authorityType, groupId, sortProp, paging);

        // Create response.
        final List<AuthorityInfo> page = pagingResult.getPage();
        int totalItems = pagingResult.getTotalResultCount().getFirst();
        List<GroupMember> groupMembers = new AbstractList<GroupMember>()
        {
            @Override
            public GroupMember get(int index)
            {
                AuthorityInfo authorityInfo = page.get(index);
                return getGroupMember(authorityInfo);
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        return CollectionWithPagingInfo.asPaged(paging, groupMembers, pagingResult.hasMoreItems(), totalItems);
    }

    private PagingResults<AuthorityInfo> getAuthoritiesInfo(AuthorityType authorityType, String groupId, Pair<String, Boolean> sortProp, Paging paging)
    {
        Set<String> authorities;
        try
        {
            authorities = authorityService.findAuthorities(authorityType, groupId, true, null, null);
        }
        catch (UnknownAuthorityException e)
        {
            authorities = Collections.emptySet();
        }

        List<AuthorityInfo> authorityInfoList = new ArrayList<>(authorities.size());
        authorityInfoList.addAll(authorities.stream().map(this::getAuthorityInfo).collect(Collectors.toList()));

        // Post process sorting - this should be moved to service
        // layer. It is done here because sorting is not supported at
        // service layer.
        AuthorityInfoComparator authorityComparator = new AuthorityInfoComparator(sortProp.getFirst(), sortProp.getSecond());
        Collections.sort(authorityInfoList, authorityComparator);

        // Post process paging - this should be moved to service layer.
        return Util.wrapPagingResults(paging, authorityInfoList);
    }

    private GroupMember getGroupMember(AuthorityInfo authorityInfo)
    {
        if (authorityInfo == null)
        {
            return null;
        }

        GroupMember groupMember = new GroupMember();
        groupMember.setId(authorityInfo.getAuthorityName());
        groupMember.setDisplayName(authorityInfo.getAuthorityDisplayName());

        String memberType = null;
        AuthorityType authorityType = AuthorityType.getAuthorityType(authorityInfo.getAuthorityName());
        switch (authorityType)
        {
        case GROUP:
            memberType = PARAM_MEMBER_TYPE_GROUP;
            break;
        case USER:
            memberType = PARAM_MEMBER_TYPE_PERSON;
            break;
        default:
        }
        groupMember.setMemberType(memberType);

        return groupMember;
    }

    private void validateGroupId(String groupId)
    {
        if (groupId == null || groupId.isEmpty())
        {
            throw new InvalidArgumentException("groupId is null or empty");
        }

        if (!authorityService.authorityExists(groupId))
        {
            throw new EntityNotFoundException(groupId);
        }
    }
}
