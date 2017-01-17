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

import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalkerOrSupported;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.AlfrescoCollator;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

import static org.alfresco.repo.security.authentication.AuthenticationUtil.runAsSystem;

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
        validateGroup(group);

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
            // The user is not an admin user and is not attempting to update *their own* details.
            throw new PermissionDeniedException();
        }

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

        // authorityService.authorityExists("GROUP_EVERYONE") returns false!
        if (!id.equals("GROUP_EVERYONE") && !authorityService.authorityExists(id))
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

    private void validateGroupId(String groupId)
    {
        if (groupId == null || groupId.isEmpty())
        {
            throw new InvalidArgumentException("groupId is null or empty");
        }

        if (!groupAuthorityExists(groupId))
        {
            throw new EntityNotFoundException(groupId);
        }
    }

    private void validateGroup(Group group)
    {
        if (group == null)
        {
            throw new InvalidArgumentException("group is null");
        }

        if (group.getId() == null || group.getId().isEmpty())
        {
            throw new InvalidArgumentException("groupId is null or empty");
        }

        if (groupAuthorityExists(group.getId()))
        {
            throw new ConstraintViolatedException("Group '" + group.getId() + "' already exists.");
        }
    }

    private boolean groupAuthorityExists(String shortName)
    {
        return authorityExists(AuthorityType.GROUP, shortName);
    }

    private boolean authorityExists(AuthorityType authorityType, String shortName)
    {
        String name = authorityService.getName(authorityType, shortName);

        return (name != null && authorityService.authorityExists(name));
    }
}
