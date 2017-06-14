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

import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAsSystem;

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
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.security.authority.AuthorityException;
import org.alfresco.repo.security.authority.AuthorityInfo;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Groups;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.model.Group;
import org.alfresco.rest.api.model.GroupMember;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalkerOrSupported;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.AlfrescoCollator;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Centralises access to groups services and maps between representations.
 * 
 * @author cturlica
 */
public class GroupsImpl implements Groups
{
    private static final int MAX_ZONES = 1;
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
    private AuthorityDAO authorityDAO;

    protected People people;

    public AuthorityService getAuthorityService()
    {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void setAuthorityDAO(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }

    public void setPeople(People people)
    {
        this.people = people;
    }

    public Group create(Group group, Parameters parameters)
    {
        validateGroup(group, false);

        // Create authority with default zones.
        final Set<String> authorityZones = authorityService.getDefaultZones();
        String authorityDisplayName = null;
        if(group.getDisplayName() != null && !group.getDisplayName().isEmpty())
        {
            authorityDisplayName = group.getDisplayName();
        }

        String authority = authorityService.createAuthority(AuthorityType.GROUP, group.getId(), authorityDisplayName, authorityZones);

        // Set a given child authority to be included by the given parent
        // authorities.
        if (group.getParentIds() != null && !group.getParentIds().isEmpty())
        {
            authorityService.addAuthority(group.getParentIds(), authority);
        }

        return getGroup(authority, parameters);
    }

    public Group update(String groupId, Group group, Parameters parameters)
    {
        validateGroupId(groupId, false);
        validateGroup(group, true);

        authorityService.setAuthorityDisplayName(groupId, group.getDisplayName());

        return getGroup(groupId, parameters);
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

        String zoneFilter = null;
        if (q != null)
        {
            GroupsQueryWalker propertyWalker = new GroupsQueryWalker(LIST_GROUPS_EQUALS_QUERY_PROPERTIES, null);
            QueryHelper.walk(q, propertyWalker);

            isRootParam = propertyWalker.getProperty(PARAM_IS_ROOT, WhereClauseParser.EQUALS, Boolean.class);
            List<String> zonesParam = propertyWalker.getZones();
            if (zonesParam != null)
            {
                validateZonesParam(zonesParam);
                zoneFilter = zonesParam.get(0);
            }

        }

        final AuthorityType authorityType = AuthorityType.GROUP;
        final Set<String> rootAuthorities = getAllRootAuthorities(authorityType);

        PagingResults<AuthorityInfo> pagingResult;
        try
        {
            pagingResult = getAuthoritiesInfo(authorityType, isRootParam, zoneFilter, rootAuthorities, sortProp, paging);
        }
        catch (UnknownAuthorityException e)
        {
            // Non-existent zone
            pagingResult = new EmptyPagingResults<>();
        }

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

    private void validateZonesParam(List<String> zonesParam)
    {
        if (zonesParam.size() > MAX_ZONES)
        {
            throw new IllegalArgumentException("A maximum of " + MAX_ZONES + " zones may be specified.");
        }
        else if (zonesParam.isEmpty())
        {
            throw new IllegalArgumentException("Zones filter list cannot be empty.");
        }
        // Validate each zone name
        zonesParam.forEach(zone -> {
            if (zone.isEmpty())
            {
                throw new IllegalArgumentException("Zone name cannot be empty (i.e. '')");
            }
        });
    }

    @Override
    public CollectionWithPagingInfo<Group> getGroupsByPersonId(String requestedPersonId, Parameters parameters)
    {
        // Canonicalize the person ID, performing -me- alias substitution.
        final String personId = people.validatePerson(requestedPersonId);

        // Non-admins can only access their own data
        // TODO: this is also in PeopleImpl.update(personId,personInfo) - refactor?
        boolean isAdmin = authorityService.hasAdminAuthority();
        String currentUserId = AuthenticationUtil.getFullyAuthenticatedUser();
        if (!isAdmin && !currentUserId.equalsIgnoreCase(personId))
        {
            // The user is not an admin user and is not attempting to retrieve *their own* details.
            throw new PermissionDeniedException();
        }

        Query q = parameters.getQuery();
        List<String> zonesParam = null;
        if (q != null)
        {
            GroupsQueryWalker propertyWalker = new GroupsQueryWalker(LIST_GROUPS_EQUALS_QUERY_PROPERTIES, null);
            QueryHelper.walk(q, propertyWalker);
            zonesParam = propertyWalker.getZones();
            if (zonesParam != null)
            {
                validateZonesParam(zonesParam);
            }
        }
        final String zoneFilter = zonesParam != null ? zonesParam.get(0) : null;

        final List<String> includeParam = parameters.getInclude();
        Paging paging = parameters.getPaging();

        // Retrieve sort column. This is limited for now to sort column due to
        // v0 api implementation. Should be improved in the future.
        Pair<String, Boolean> sortProp = getGroupsSortProp(parameters);

        // Get all the authorities for a user, including but not limited to, groups.
        Set<String> userAuthorities = runAsSystem(
                () -> authorityService.getAuthoritiesForUser(personId));

        // Filter, transform and sort the list of user authorities into
        // a suitable list of AuthorityInfo objects.
        List<AuthorityInfo> groupAuthorities = userAuthorities.stream().
                filter(a -> a.startsWith(AuthorityType.GROUP.getPrefixString())).
                filter(a -> zonePredicate(a, zoneFilter)).
                map(this::getAuthorityInfo).
                sorted(new AuthorityInfoComparator(sortProp.getFirst(), sortProp.getSecond())).
                collect(Collectors.toList());

        PagingResults<AuthorityInfo> pagingResult = Util.wrapPagingResults(paging, groupAuthorities);

        // Create response.
        final List<AuthorityInfo> page = pagingResult.getPage();
        int totalItems = pagingResult.getTotalResultCount().getFirst();

        // Transform the page of results into Group objects
        final Set<String> rootAuthorities = getAllRootAuthorities(AuthorityType.GROUP);

        List<Group> groups = page.stream().
                map(authority -> getGroup(authority, includeParam, rootAuthorities)).
                collect(Collectors.toList());

        return CollectionWithPagingInfo.asPaged(paging, groups, pagingResult.hasMoreItems(), totalItems);
    }

    private PagingResults<AuthorityInfo> getAuthoritiesInfo(AuthorityType authorityType, Boolean isRootParam, String zoneFilter, Set<String> rootAuthorities, Pair<String, Boolean> sortProp,
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
                List<AuthorityInfo> authorities = rootAuthorities.stream().
                        map(this::getAuthorityInfo).
                        filter(auth -> zonePredicate(auth.getAuthorityName(), zoneFilter)).
                        collect(Collectors.toList());
                groupList = new ArrayList<>(rootAuthorities.size());
                groupList.addAll(authorities);

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
                PagingResults<AuthorityInfo> nonPagingResult = authorityService.getAuthoritiesInfo(authorityType, zoneFilter, null, sortProp.getFirst(), sortProp.getSecond(),
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
            pagingResult = authorityService.getAuthoritiesInfo(authorityType, zoneFilter, null, sortProp.getFirst(), sortProp.getSecond(), pagingRequest);
        }
        return pagingResult;
    }

    /**
     * Checks to see if the named group authority should be included in results
     * when filtered by zone.
     *
     * @see #zonePredicate(Set, String)
     * @param groupName
     * @param zone
     * @return true if result should be included.
     */
    private boolean zonePredicate(String groupName, String zone)
    {
        Set<String> zones = null;
        if (zone != null)
        {
            // Don't bother doing this lookup unless the zone filter is non-null.
            zones = authorityService.getAuthorityZones(groupName);
        }
        return zonePredicate(zones, zone);
    }

    /**
     * Checks a list of zones to see if it matches the supplied zone filter
     * ({@code requiredZone} parameter).
     * <p>
     * If the requiredZone parameter is null, then the filter will not be applied (returns true.)
     * <p>
     * If the requiredZone parameter is not null (i.e. a filter must be applied) and the
     * {@code zones}) list is {@code null} then the predicate will return false.
     *
     * @param zones
     * @param requiredZone
     * @return true if result should be included.
     */
    private boolean zonePredicate(Set<String> zones, String requiredZone)
    {
        if (requiredZone != null)
        {
            return zones != null && zones.contains(requiredZone);
        }
        return true;
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
        return getAuthorityInfo(id, false);
    }

    /**
     * Retrieve authority info by name. <b>Node id field isn't used at this time
     * and it is set to null.</b>
     *
     * @param id
     *            The authority name.
     * @param defaultDisplayNameIfNull
     *            True if we would like to get a default value (e.g. shortName of the authority) if the authority display name is null.
     * @return The authority info.
     */
    private AuthorityInfo getAuthorityInfo(String id, boolean defaultDisplayNameIfNull)
    {
        if (id == null || id.isEmpty())
        {
            throw new InvalidArgumentException("id is null or empty");
        }

        // authorityService.authorityExists(ALL_AUTHORITIES) returns false!
        if (!id.equals(PermissionService.ALL_AUTHORITIES) && !authorityService.authorityExists(id))
        {
            throw new EntityNotFoundException(id);
        }

        String authorityDisplayName = getAuthorityDisplayName(id, defaultDisplayNameIfNull);

        return new AuthorityInfo(null, authorityDisplayName, id);
    }

    private String getAuthorityDisplayName(String id, boolean defaultDisplayNameIfNull)
    {
        return defaultDisplayNameIfNull ? authorityService.getAuthorityDisplayName(id) : authorityDAO.getAuthorityDisplayName(id);
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
            col = AlfrescoCollator.getInstance(I18NUtil.getLocale());
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

                // Please see GetAuthoritiesCannedQuery.AuthComparator for details.

                // Get the value from the group
                if (DISPLAY_NAME.equals(sortBy))
                {
                    v = g.getAuthorityDisplayName();
                }
                else if (SHORT_NAME.equals(sortBy))
                {
                    v = g.getShortName();
                }
                else
                {
                    throw new InvalidArgumentException("Invalid sort field: " + sortBy);
                }

                if (v == null)
                {
                    v = g.getAuthorityName();
                }

                // // Lower case it for case insensitive search
                // v = v.toLowerCase();

                // Cache it
                nameCache.put(g, v);
            }
            return v;
        }
    }

    public void delete(String groupId, Parameters parameters)
    {
        if (!isGroupAuthority(groupId))
        {
            throw new InvalidArgumentException("Invalid group id: " + groupId);
        }

        // Get cascade param - default false (if not provided).
        boolean cascade = Boolean.valueOf(parameters.getParameter(PARAM_CASCADE));

        try
        {
            authorityService.deleteAuthority(groupId, cascade);
        }
        catch (AuthorityException ae)
        {
            if (ae.getMsgId().equals("Trying to modify a fixed authority"))
            {
                throw new ConstraintViolatedException("Trying to modify a fixed authority");
            }
            else
            {
                throw ae;
            }
        }
    }

    public CollectionWithPagingInfo<GroupMember> getGroupMembers(String groupId, final Parameters parameters)
    {
        validateGroupId(groupId, false);

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
            authorityType = getAuthorityType(memberTypeStr);
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

    @Override
    public GroupMember createGroupMember(String groupId, GroupMember groupMember)
    {
        validateGroupId(groupId, false);
        validateGroupMember(groupMember);

        AuthorityType authorityType = getAuthorityType(groupMember.getMemberType());

        if (!authorityService.authorityExists(groupMember.getId()))
        {
            throw new EntityNotFoundException(groupMember.getId());
        }

        AuthorityType existingAuthorityType = AuthorityType.getAuthorityType(groupMember.getId());
        if (existingAuthorityType != authorityType)
        {
            throw new IllegalArgumentException("Incorrect group member type, "
                    + (AuthorityType.USER.equals(existingAuthorityType) ? Groups.PARAM_MEMBER_TYPE_PERSON : existingAuthorityType) + " exists with the given id");
        }

        authorityService.addAuthority(groupId, groupMember.getId());
        String authority = authorityService.getName(authorityType, groupMember.getId());

        return getGroupMember(authority);
    }

    public void deleteGroupMembership(String groupId, String groupMemberId)
    {
        validateGroupId(groupId, false);
        validateGroupMemberId(groupMemberId);
        // TODO: Verify if groupMemberId is member of groupId
        authorityService.removeAuthority(groupId, groupMemberId);
    }

    private AuthorityType getAuthorityType(String memberType)
    {
        AuthorityType authorityType = null;
        if (memberType != null && !memberType.isEmpty())
        {
            switch (memberType)
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
        return authorityType;
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

        String authorityDisplayName = authorityInfo.getAuthorityDisplayName();
        if (authorityDisplayName == null || authorityDisplayName.isEmpty())
        {
            authorityDisplayName = authorityService.getAuthorityDisplayName(authorityInfo.getAuthorityName());
        }

        groupMember.setDisplayName(authorityDisplayName);

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

    private GroupMember getGroupMember(String authorityId)
    {
        AuthorityInfo authorityInfo = getAuthorityInfo(authorityId);

        return getGroupMember(authorityInfo);
    }

    private void validateGroupId(String groupId, boolean inferPrefix)
    {
        if (groupId == null || groupId.isEmpty())
        {
            throw new InvalidArgumentException("groupId is null or empty");
        }

        if (!groupAuthorityExists(groupId, inferPrefix))
        {
            throw new EntityNotFoundException(groupId);
        }
    }

    private void validateGroupMemberId(String groupMemberId)
    {
        if (groupMemberId == null || groupMemberId.isEmpty())
        {
            throw new InvalidArgumentException("group member id is null or empty");
        }
        if (!(personAuthorityExists(groupMemberId) || groupAuthorityExists(groupMemberId, false)))
        {
            throw new EntityNotFoundException(groupMemberId);
        }
    }

    private void validateGroup(Group group, boolean isUpdate)
    {
        if (group == null)
        {
            throw new InvalidArgumentException("group is null");
        }

        if (!isUpdate)
        {
            if (group.getId() == null || group.getId().isEmpty())
            {
                throw new InvalidArgumentException("groupId is null or empty");
            }

            if (group.getId().indexOf('/') != -1)
            {
                throw new IllegalArgumentException("groupId contains characters that are not permitted.");
            }

            if (groupAuthorityExists(group.getId()))
            {
                throw new ConstraintViolatedException("Group '" + group.getId() + "' already exists.");
            }
        }
        else
        {
            if (group.wasSet(Group.ID))
            {
                throw new InvalidArgumentException("Group update does not support field: id");
            }

            if (group.wasSet(Group.IS_ROOT))
            {
                throw new InvalidArgumentException("Group update does not support field: isRoot");
            }

            if (group.wasSet(Group.PARENT_IDS))
            {
                throw new InvalidArgumentException("Group update does not support field: parentIds");
            }

            if (group.wasSet(Group.ZONES))
            {
                throw new InvalidArgumentException("Group update does not support field: zones");
            }
        }
    }

    private void validateGroupMember(GroupMember groupMember)
    {
        if (groupMember == null)
        {
            throw new InvalidArgumentException("group member is null");
        }

        if (groupMember.getId() == null || groupMember.getId().isEmpty())
        {
            throw new InvalidArgumentException("group member Id is null or empty");
        }

        if (groupMember.getMemberType() == null || groupMember.getMemberType().isEmpty())
        {
            throw new InvalidArgumentException("group member type is null or empty");
        }
    }

    private boolean groupAuthorityExists(String authorityName)
    {
        return groupAuthorityExists(authorityName, true);
    }

    private boolean groupAuthorityExists(String authorityName, boolean inferPrefix)
    {
        return authorityExists(AuthorityType.GROUP, authorityName, inferPrefix);
    }

    private boolean personAuthorityExists(String authorityName)
    {
        return authorityExists(AuthorityType.USER, authorityName, false);
    }

    private boolean authorityExists(AuthorityType authorityType, String authorityName, boolean inferPrefix)
    {
        String name = inferPrefix ? authorityService.getName(authorityType, authorityName) : authorityName;

        return (name != null && authorityService.authorityExists(name));
    }

    private boolean isGroupAuthority(String authorityName)
    {
        AuthorityType authorityType = AuthorityType.getAuthorityType(authorityName);
        return AuthorityType.GROUP.equals(authorityType) || AuthorityType.EVERYONE.equals(authorityType);
    }

    private static class GroupsQueryWalker extends MapBasedQueryWalker
    {
        private List<String> zones;

        @Override
        public void and()
        {
            // allow AND, e.g. isRoot=true AND zones in ('BLAH')
        }

        @Override
        public void in(String propertyName, boolean negated, String... propertyValues)
        {
            if (propertyName.equalsIgnoreCase("zones"))
            {
                zones = Arrays.asList(propertyValues);
            }
        }

        public GroupsQueryWalker(Set<String> supportedEqualsParameters, Set<String> supportedMatchesParameters)
        {
            super(supportedEqualsParameters, supportedMatchesParameters);
        }

        /**
         * The list of zones specified in the where clause.
         *
         * @return The zones list if specified, or null if not.
         */
        public List<String> getZones()
        {
            return zones;
        }
    }
}
