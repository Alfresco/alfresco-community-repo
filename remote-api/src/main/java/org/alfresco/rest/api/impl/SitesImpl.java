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

import java.io.Serializable;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.query.PageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.node.getchildren.FilterPropBoolean;
import org.alfresco.repo.node.getchildren.FilterPropString;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.*;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.*;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalkerOrSupported;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteMemberInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterContentCache;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Centralises access to site services and maps between representations.
 * 
 * @author steveglover
 * @author janv
 * @since publicapi1.0
 */
public class SitesImpl implements Sites
{
    private static final Log logger = LogFactory.getLog(SitesImpl.class);

    private static final String FAVOURITE_SITES_PREFIX = "org.alfresco.share.sites.favourites.";
    private static final int FAVOURITE_SITES_PREFIX_LENGTH = FAVOURITE_SITES_PREFIX.length();

    // based on Share create site
    private static final int SITE_MAXLEN_ID = 72;
    private static final int SITE_MAXLEN_TITLE = 256;
    private static final int SITE_MAXLEN_DESCRIPTION = 512;

    private static final String SITE_ID_VALID_CHARS_PARTIAL_REGEX = "A-Za-z0-9\\-";

    private static final String DEFAULT_SITE_PRESET = "site-dashboard";
    private static final String PARAM_IS_MEMBER_OF_GROUP = "isMemberOfGroup";

    private final static Map<String, QName> SORT_PARAMS_TO_QNAMES;
    static
    {
        Map<String, QName> aMap = new HashMap<>(3);
        aMap.put(PARAM_SITE_TITLE, ContentModel.PROP_TITLE);
        aMap.put(PARAM_SITE_ID, ContentModel.PROP_NAME);
        aMap.put(PARAM_SITE_DESCRIPTION, ContentModel.PROP_DESCRIPTION);
        SORT_PARAMS_TO_QNAMES = Collections.unmodifiableMap(aMap);
    }

    private final static Map<String, SiteService.SortFields> SORT_SITE_MEMBERSHIP;
    static
    {
        Map<String, SiteService.SortFields> aMap = new HashMap<>(3);
        aMap.put(PARAM_SITE_TITLE, SiteService.SortFields.SiteTitle);
        aMap.put(SiteService.SortFields.SiteTitle.toString(), SiteService.SortFields.SiteTitle); // for backwards compat'
        aMap.put(PARAM_SITE_ID, SiteService.SortFields.SiteShortName);
        aMap.put(SiteService.SortFields.SiteShortName.toString(), SiteService.SortFields.SiteShortName); // for backwards compat'
        aMap.put(PARAM_SITE_ROLE, SiteService.SortFields.Role);
        aMap.put(SiteService.SortFields.Role.toString(), SiteService.SortFields.Role); // for backwards compat'
        SORT_SITE_MEMBERSHIP = Collections.unmodifiableMap(aMap);
    }

    // list children filtering (via where clause)
    private final static Set<String> LIST_SITES_EQUALS_QUERY_PROPERTIES = new HashSet<>(Arrays.asList(PARAM_VISIBILITY, PARAM_PRESET));

    protected Nodes nodes;
    protected People people;
    protected NodeService nodeService;
    protected DictionaryService dictionaryService;
    protected SiteService siteService;
    protected FavouritesService favouritesService;
    protected PreferenceService preferenceService;
    protected ImporterService importerService;
    protected SiteSurfConfig siteSurfConfig;
    protected PermissionService permissionService;
    protected SiteServiceImpl siteServiceImpl;
    protected AuthorityService authorityService;

    public void setPreferenceService(PreferenceService preferenceService)
    {
        this.preferenceService = preferenceService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

    public void setFavouritesService(FavouritesService favouritesService)
    {
        this.favouritesService = favouritesService;
    }

    public void setPeople(People people)
    {
        this.people = people;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setImporterService(ImporterService importerService)
    {
        this.importerService = importerService;
    }

    public void setSiteSurfConfig(SiteSurfConfig siteSurfConfig)
    {
        this.siteSurfConfig = siteSurfConfig;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setSiteServiceImpl(SiteServiceImpl siteServiceImpl)
    {
        this.siteServiceImpl = siteServiceImpl;
    }

    public AuthorityService getAuthorityService()
    {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public SiteInfo validateSite(NodeRef guid)
    {
        SiteInfo siteInfo = null;

        if (guid == null)
        {
            throw new InvalidArgumentException("guid is null");
        }
        nodes.validateNode(guid);
        QName type = nodeService.getType(guid);
        boolean isSiteNodeRef = dictionaryService.isSubClass(type, SiteModel.TYPE_SITE);
        if (isSiteNodeRef)
        {
            siteInfo = siteService.getSite(guid);
            if (siteInfo == null)
            {
                // not a site
                throw new InvalidArgumentException(guid.getId() + " is not a site");
            }
        }
        else
        {
            // site does not exist
            throw new EntityNotFoundException(guid.getId());
        }

        return siteInfo;
    }

    public SiteInfo validateSite(String siteId)
    {
        if (siteId == null)
        {
            throw new InvalidArgumentException("siteId is null");
        }
        SiteInfo siteInfo = siteService.getSite(siteId);
        return siteInfo;
    }

    public CollectionWithPagingInfo<SiteMember> getSiteMembers(String siteId, Parameters parameters)
    {
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteId);
        }
        // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        Paging paging = parameters.getPaging();
        PagingRequest pagingRequest = Util.getPagingRequest(paging);
        pagingRequest.setRequestTotalCountMax(100);

        MapBasedQueryWalker propertyWalker = new MapBasedQueryWalker(new HashSet<>(Collections.singletonList(PARAM_IS_MEMBER_OF_GROUP)), null);
        ;
        QueryHelper.walk(parameters.getQuery(), propertyWalker);

        Boolean expandGroups = propertyWalker.getProperty(PARAM_IS_MEMBER_OF_GROUP, WhereClauseParser.EQUALS, Boolean.class);

        if (expandGroups == null)
        {
            expandGroups = true;
        }

        final List<Pair<SiteService.SortFields, Boolean>> sort = new ArrayList<Pair<SiteService.SortFields, Boolean>>();
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.LastName, Boolean.TRUE));
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.FirstName, Boolean.TRUE));
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.Role, Boolean.TRUE));
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.Username, Boolean.TRUE));
        PagingResults<SiteMembership> pagedResults = siteService.listMembersPaged(siteId, expandGroups, sort, pagingRequest);

        List<SiteMember> ret = pagedResults.getPage()
                .stream()
                .map((siteMembership) -> new SiteMember(siteMembership.getPersonId(), siteMembership.getRole(), siteMembership.isMemberOfGroup()))
                .collect(Collectors.toList());

        return CollectionWithPagingInfo.asPaged(paging, ret, pagedResults.hasMoreItems(), pagedResults.getTotalResultCount().getFirst());
    }

    public String getSiteRole(String siteId)
    {
        String personId = AuthenticationUtil.getFullyAuthenticatedUser();
        return getSiteRole(siteId, personId);
    }

    public String getSiteRole(String siteId, String personId)
    {
        return siteService.getMembersRole(siteId, personId);
    }

    public Site getSite(String siteId)
    {
        return getSite(siteId, true);
    }

    public Site getSite(String siteId, boolean includeRole)
    {
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteId);
        }
        return getSite(siteInfo, includeRole);
    }

    private Site getSite(SiteInfo siteInfo, boolean includeRole)
    {
        // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        String siteId = siteInfo.getShortName();
        String role = null;
        if (includeRole)
        {
            role = getSiteRole(siteId);
        }
        return new Site(siteInfo, role);
    }

    /**
     * people/<personId>/sites/<siteId>
     *
     * @param siteId
     *            String
     * @param personId
     *            String
     * @return MemberOfSite
     */
    public MemberOfSite getMemberOfSite(String personId, String siteId)
    {
        MemberOfSite siteMember = null;

        personId = people.validatePerson(personId);
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new RelationshipResourceNotFoundException(personId, siteId);
        }
        // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        String roleStr = siteService.getMembersRole(siteInfo.getShortName(), personId);
        if (roleStr != null)
        {
            Site site = new Site(siteInfo, roleStr);
            siteMember = new MemberOfSite(site.getId(), siteInfo.getNodeRef(), roleStr);
        }
        else
        {
            throw new RelationshipResourceNotFoundException(personId, siteId);
        }

        return siteMember;
    }

    public SiteMember getSiteMember(String personId, String siteId)
    {
        SiteMember siteMember = null;

        personId = people.validatePerson(personId);
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            logger.debug("Site does not exist: " + siteId);
            throw new RelationshipResourceNotFoundException(personId, siteId);
        }
        siteId = siteInfo.getShortName();

        logger.debug("Getting member role for " + siteId + " person " + personId);
        String role = siteService.getMembersRole(siteId, personId);
        if (role != null)
        {
            siteMember = new SiteMember(personId, role);
        }
        else
        {
            logger.debug("Getting member role but role is null");
            throw new RelationshipResourceNotFoundException(personId, siteId);
        }

        return siteMember;
    }

    public SiteMember addSiteMember(String siteId, SiteMember siteMember)
    {
        String personId = people.validatePerson(siteMember.getPersonId());
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            logger.debug("addSiteMember:  site does not exist " + siteId + " person " + personId);
            throw new EntityNotFoundException(siteId);
        }
        // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        String role = siteMember.getRole();
        if (role == null)
        {
            logger.debug("addSiteMember:  Must provide a role " + siteMember);
            throw new InvalidArgumentException("Must provide a role");
        }

        if (siteService.isMember(siteId, personId))
        {
            logger.debug("addSiteMember:  " + personId + " is already a member of site " + siteId);
            throw new ConstraintViolatedException(personId + " is already a member of site " + siteId);
        }

        if (!siteService.canAddMember(siteId, personId, role))
        {
            logger.debug("addSiteMember:  PermissionDeniedException " + siteId + " person " + personId + " role " + role);
            throw new PermissionDeniedException();
        }

        try
        {
            siteService.setMembership(siteId, personId, role);
        }
        catch (UnknownAuthorityException e)
        {
            logger.debug("addSiteMember:  UnknownAuthorityException " + siteId + " person " + personId + " role " + role);
            throw new InvalidArgumentException("Unknown role '" + role + "'");
        }
        return siteMember;
    }

    public void removeSiteMember(String personId, String siteId)
    {
        personId = people.validatePerson(personId);
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new RelationshipResourceNotFoundException(personId, siteId);
        }
        // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        boolean isMember = siteService.isMember(siteId, personId);
        if (!isMember)
        {
            throw new InvalidArgumentException();
        }
        String role = siteService.getMembersRole(siteId, personId);
        if (role != null)
        {
            if (role.equals(SiteModel.SITE_MANAGER))
            {
                int numAuthorities = siteService.countAuthoritiesWithRole(siteId, SiteModel.SITE_MANAGER);
                if (numAuthorities <= 1)
                {
                    throw new InvalidArgumentException("Can't remove last manager of site " + siteId);
                }
                siteService.removeMembership(siteId, personId);
            }
            else
            {
                siteService.removeMembership(siteId, personId);
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("Unable to determine role of site member");
        }
    }

    public SiteMember updateSiteMember(String siteId, SiteMember siteMember)
    {
        String siteMemberId = siteMember.getPersonId();
        if (siteMemberId == null)
        {
            throw new InvalidArgumentException("Member id is null");
        }
        siteMemberId = people.validatePerson(siteMemberId);
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteId);
        }
        siteId = siteInfo.getShortName();
        String siteRole = siteMember.getRole();
        if (siteRole == null)
        {
            throw new InvalidArgumentException("Must provide a role");
        }

        /* MNT-10551 : fix */
        if (!siteService.isMember(siteId, siteMember.getPersonId()))
        {
            throw new InvalidArgumentException("User is not a member of the site");
        }

        try
        {
            siteService.setMembership(siteId, siteMember.getPersonId(), siteRole);
        }
        catch (UnknownAuthorityException e)
        {
            throw new InvalidArgumentException("Unknown role '" + siteRole + "'");
        }
        return siteMember;
    }

    public CollectionWithPagingInfo<MemberOfSite> getSites(String personId, Parameters parameters)
    {
        Paging paging = parameters.getPaging();

        personId = people.validatePerson(personId);

        PagingRequest pagingRequest = Util.getPagingRequest(paging);

        // get the sorting options
        List<Pair<? extends Object, SortOrder>> sortPairs = new ArrayList<>(parameters.getSorting().size());

        List<SortColumn> sortCols = parameters.getSorting();
        if ((sortCols != null) && (sortCols.size() > 0))
        {
            for (SortColumn sortCol : sortCols)
            {
                SiteService.SortFields sortProp = SORT_SITE_MEMBERSHIP.get(sortCol.column);
                if (sortProp == null)
                {
                    throw new InvalidArgumentException("Invalid sort field: " + sortCol.column);
                }
                sortPairs.add(new Pair<>(sortProp, (sortCol.asc ? SortOrder.ASCENDING : SortOrder.DESCENDING)));
            }
        }
        else
        {
            // default sort order
            sortPairs.add(new Pair<SiteService.SortFields, SortOrder>(
                    SiteService.SortFields.SiteTitle,
                    SortOrder.ASCENDING));
        }

        // get the unsorted list of site memberships
        List<SiteMembership> siteMembers = siteService.listSiteMemberships(personId, 0);

        // sort the list of site memberships
        int totalSize = siteMembers.size();
        final List<SiteMembership> sortedSiteMembers = new ArrayList<>(siteMembers);
        Collections.sort(sortedSiteMembers, new SiteMembershipComparator(
                sortPairs,
                SiteMembershipComparator.Type.SITES));

        PageDetails pageDetails = PageDetails.getPageDetails(pagingRequest, totalSize);
        List<MemberOfSite> ret = new ArrayList<>(totalSize);

        List<FilterProp> filterProps = getFilterPropListOfSites(parameters);

        int counter;
        int totalItems = 0;
        Iterator<SiteMembership> it = sortedSiteMembers.iterator();
        for (counter = 0; it.hasNext();)
        {
            SiteMembership siteMember = it.next();

            if (filterProps != null && !includeFilter(siteMember, filterProps))
            {
                continue;
            }

            if (counter < pageDetails.getSkipCount())
            {
                totalItems++;
                counter++;
                continue;
            }

            if (counter <= pageDetails.getEnd() - 1)
            {
                SiteInfo siteInfo = siteMember.getSiteInfo();
                MemberOfSite memberOfSite = new MemberOfSite(siteInfo.getShortName(), siteInfo.getNodeRef(), siteMember.getRole());
                ret.add(memberOfSite);

                counter++;
            }

            totalItems++;
        }
        return CollectionWithPagingInfo.asPaged(paging, ret, counter < totalItems, totalItems);

    }

    public SiteContainer getSiteContainer(String siteId, String containerId)
    {
        // check site and container node validity
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new RelationshipResourceNotFoundException(siteId, containerId);
        }
        // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        NodeRef containerNodeRef = siteService.getContainer(siteId, containerId);
        if (containerNodeRef == null)
        {
            throw new RelationshipResourceNotFoundException(siteId, containerId);
        }

        // check that the containerId is actually a container for the specified site
        SiteInfo testSiteInfo = siteService.getSite(containerNodeRef);
        if (testSiteInfo == null)
        {
            throw new RelationshipResourceNotFoundException(siteId, containerId);
        }
        else
        {
            if (!testSiteInfo.getShortName().equals(siteId))
            {
                throw new RelationshipResourceNotFoundException(siteId, containerId);
            }
        }

        String folderId = (String) nodeService.getProperty(containerNodeRef, SiteModel.PROP_COMPONENT_ID);

        SiteContainer siteContainer = new SiteContainer(folderId, containerNodeRef);
        return siteContainer;
    }

    public PagingResults<SiteContainer> getSiteContainers(String siteId, Paging paging)
    {
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteId);
        }

        final PagingResults<FileInfo> pagingResults = siteService.listContainers(siteInfo.getShortName(), Util.getPagingRequest(paging));
        List<FileInfo> containerFileInfos = pagingResults.getPage();
        final List<SiteContainer> siteContainers = new ArrayList<SiteContainer>(containerFileInfos.size());
        for (FileInfo containerFileInfo : containerFileInfos)
        {
            NodeRef nodeRef = containerFileInfo.getNodeRef();
            String containerId = (String) nodeService.getProperty(nodeRef, SiteModel.PROP_COMPONENT_ID);
            SiteContainer siteContainer = new SiteContainer(containerId, nodeRef);
            siteContainers.add(siteContainer);
        }

        return new PagingResults<SiteContainer>() {
            @Override
            public List<SiteContainer> getPage()
            {
                return siteContainers;
            }

            @Override
            public boolean hasMoreItems()
            {
                return pagingResults.hasMoreItems();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                return pagingResults.getTotalResultCount();
            }

            @Override
            public String getQueryExecutionId()
            {
                return null;
            }
        };
    }

    public CollectionWithPagingInfo<Site> getSites(final Parameters parameters)
    {
        final BeanPropertiesFilter filter = parameters.getFilter();

        Paging paging = parameters.getPaging();
        PagingRequest pagingRequest = Util.getPagingRequest(paging);
        // pagingRequest.setRequestTotalCountMax(requestTotalCountMax)

        List<Pair<QName, Boolean>> sortProps = new ArrayList<Pair<QName, Boolean>>();
        List<SortColumn> sortCols = parameters.getSorting();
        if ((sortCols != null) && (sortCols.size() > 0))
        {
            for (SortColumn sortCol : sortCols)
            {
                QName sortPropQName = SORT_PARAMS_TO_QNAMES.get(sortCol.column);
                if (sortPropQName == null)
                {
                    throw new InvalidArgumentException("Invalid sort field: " + sortCol.column);
                }
                sortProps.add(new Pair<>(sortPropQName, (sortCol.asc ? Boolean.TRUE : Boolean.FALSE)));
            }
        }
        else
        {
            // default sort order
            sortProps.add(new Pair<>(ContentModel.PROP_TITLE, Boolean.TRUE));
        }

        List<FilterProp> filterProps = getFilterPropListOfSites(parameters);

        final PagingResults<SiteInfo> pagingResult = siteService.listSites(filterProps, sortProps, pagingRequest);
        final List<SiteInfo> sites = pagingResult.getPage();
        int totalItems = pagingResult.getTotalResultCount().getFirst();
        final String personId = AuthenticationUtil.getFullyAuthenticatedUser();
        List<Site> page = new AbstractList<Site>() {
            @Override
            public Site get(int index)
            {
                SiteInfo siteInfo = sites.get(index);

                String role = null;
                if (filter.isAllowed(Site.ROLE))
                {
                    role = siteService.getMembersRole(siteInfo.getShortName(), personId);
                }
                return new Site(siteInfo, role);
            }

            @Override
            public int size()
            {
                return sites.size();
            }
        };

        return CollectionWithPagingInfo.asPaged(paging, page, pagingResult.hasMoreItems(), totalItems);
    }

    private SiteVisibility getSiteVisibilityFromParam(String siteVisibilityStr)
    {
        SiteVisibility visibility;
        try
        {
            // Create the enum value from the string
            visibility = SiteVisibility.valueOf(siteVisibilityStr);
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidArgumentException("Site visibility is invalid (expected eg. PUBLIC, PRIVATE, MODERATED)");
        }

        return visibility;
    }

    private List<FilterProp> getFilterPropListOfSites(final Parameters parameters)
    {
        List<FilterProp> filterProps = new ArrayList<FilterProp>();
        Query q = parameters.getQuery();
        if (q != null)
        {
            MapBasedQueryWalkerOrSupported propertyWalker = new MapBasedQueryWalkerOrSupported(LIST_SITES_EQUALS_QUERY_PROPERTIES, null);
            QueryHelper.walk(q, propertyWalker);

            String siteVisibilityStr = propertyWalker.getProperty(PARAM_VISIBILITY, WhereClauseParser.EQUALS, String.class);
            if (siteVisibilityStr != null && !siteVisibilityStr.isEmpty())
            {
                SiteVisibility siteVisibility = getSiteVisibilityFromParam(siteVisibilityStr);
                filterProps.add(new FilterPropString(SiteModel.PROP_SITE_VISIBILITY, siteVisibility.name(), FilterPropString.FilterTypeString.EQUALS));
            }

            String sitePreset = propertyWalker.getProperty(PARAM_PRESET, WhereClauseParser.EQUALS, String.class);
            if (sitePreset != null && !sitePreset.isEmpty())
            {
                filterProps.add(new FilterPropString(SiteModel.PROP_SITE_PRESET, sitePreset, FilterPropString.FilterTypeString.EQUALS));
            }
        }

        // expected null or non-empty list
        return filterProps.isEmpty() ? null : filterProps;
    }

    private boolean includeFilter(SiteMembership siteMembership, List<FilterProp> filterProps)
    {
        Map<QName, Serializable> propVals = new HashMap<>();
        propVals.put(SiteModel.PROP_SITE_VISIBILITY, siteMembership.getSiteInfo().getVisibility().name());
        propVals.put(SiteModel.PROP_SITE_PRESET, siteMembership.getSiteInfo().getSitePreset());
        return includeFilter(propVals, filterProps);
    }

    // note: currently inclusive and OR-based
    private boolean includeFilter(Map<QName, Serializable> propVals, List<FilterProp> filterProps)
    {
        for (FilterProp filterProp : filterProps)
        {
            Serializable propVal = propVals.get(filterProp.getPropName());
            if (propVal != null)
            {
                if ((filterProp instanceof FilterPropString) && (propVal instanceof String))
                {
                    String val = (String) propVal;
                    String filter = (String) filterProp.getPropVal();

                    switch ((FilterPropString.FilterTypeString) filterProp.getFilterType())
                    {
                    case STARTSWITH:
                        if (val.startsWith(filter))
                        {
                            return true;
                        }
                        break;
                    case STARTSWITH_IGNORECASE:
                        if (val.toLowerCase().startsWith(filter.toLowerCase()))
                        {
                            return true;
                        }
                        break;
                    case EQUALS:
                        if (val.equals(filter))
                        {
                            return true;
                        }
                        break;
                    case EQUALS_IGNORECASE:
                        if (val.equalsIgnoreCase(filter))
                        {
                            return true;
                        }
                        break;
                    case ENDSWITH:
                        if (val.endsWith(filter))
                        {
                            return true;
                        }
                        break;
                    case ENDSWITH_IGNORECASE:
                        if (val.toLowerCase().endsWith(filter.toLowerCase()))
                        {
                            return true;
                        }
                        break;
                    case MATCHES:
                        if (val.matches(filter))
                        {
                            return true;
                        }
                        break;
                    case MATCHES_IGNORECASE:
                        if (val.toLowerCase().matches(filter.toLowerCase()))
                        {
                            return true;
                        }
                        break;
                    default:
                    }
                }
            }

            if ((filterProp instanceof FilterPropBoolean) && (propVal instanceof Boolean))
            {
                Boolean val = (Boolean) propVal;
                Boolean filter = (Boolean) filterProp.getPropVal();

                return (val == filter);
            }
        }

        return false;
    }

    public FavouriteSite getFavouriteSite(String personId, String siteId)
    {
        personId = people.validatePerson(personId);
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new RelationshipResourceNotFoundException(personId, siteId);
        }
        // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        siteId = siteInfo.getShortName();
        NodeRef nodeRef = siteInfo.getNodeRef();

        if (favouritesService.isFavourite(personId, nodeRef))
        {
            String role = getSiteRole(siteId, personId);
            return new FavouriteSite(siteInfo, role);
        }
        else
        {
            throw new RelationshipResourceNotFoundException(personId, siteId);
        }
    }

    public void addFavouriteSite(String personId, FavouriteSite favouriteSite)
    {
        personId = people.validatePerson(personId);
        String siteId = favouriteSite.getId();
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteId);
        }
        // set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        StringBuilder prefKey = new StringBuilder(FAVOURITE_SITES_PREFIX);
        prefKey.append(siteId);
        String value = (String) preferenceService.getPreference(personId, prefKey.toString());
        boolean isFavouriteSite = (value != null && value.equalsIgnoreCase("true"));

        if (isFavouriteSite)
        {
            throw new ConstraintViolatedException("Site " + siteId + " is already a favourite site");
        }

        prefKey = new StringBuilder(FAVOURITE_SITES_PREFIX);
        prefKey.append(siteId);

        Map<String, Serializable> preferences = new HashMap<String, Serializable>(1);
        preferences.put(prefKey.toString(), Boolean.TRUE);
        preferenceService.setPreferences(personId, preferences);
    }

    public void removeFavouriteSite(String personId, String siteId)
    {
        personId = people.validatePerson(personId);
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new RelationshipResourceNotFoundException(personId, siteId);
        }
        siteId = siteInfo.getShortName();

        StringBuilder prefKey = new StringBuilder(FAVOURITE_SITES_PREFIX);
        prefKey.append(siteId);
        String value = (String) preferenceService.getPreference(personId, prefKey.toString());
        boolean isFavouriteSite = (value != null && value.equalsIgnoreCase("true"));

        if (!isFavouriteSite)
        {
            throw new NotFoundException("Site " + siteId + " is not a favourite site");
        }

        preferenceService.clearPreferences(personId, prefKey.toString());
    }

    private PagingResults<SiteInfo> getFavouriteSites(String userName, PagingRequest pagingRequest)
    {
        final Collator collator = Collator.getInstance();

        final Set<SiteInfo> sortedFavouriteSites = new TreeSet<SiteInfo>(new Comparator<SiteInfo>() {
            @Override
            public int compare(SiteInfo o1, SiteInfo o2)
            {
                return collator.compare(o1.getTitle(), o2.getTitle());
            }
        });

        Map<String, Serializable> prefs = preferenceService.getPreferences(userName, FAVOURITE_SITES_PREFIX);
        for (Entry<String, Serializable> entry : prefs.entrySet())
        {
            boolean isFavourite = false;
            Serializable s = entry.getValue();
            if (s instanceof Boolean)
            {
                isFavourite = (Boolean) s;
            }
            if (isFavourite)
            {
                String siteShortName = entry.getKey().substring(FAVOURITE_SITES_PREFIX_LENGTH).replace(".favourited", "");
                SiteInfo siteInfo = siteService.getSite(siteShortName);
                if (siteInfo != null)
                {
                    sortedFavouriteSites.add(siteInfo);
                }
            }
        }

        int totalSize = sortedFavouriteSites.size();
        final PageDetails pageDetails = PageDetails.getPageDetails(pagingRequest, totalSize);

        final List<SiteInfo> page = new ArrayList<SiteInfo>(pageDetails.getPageSize());
        Iterator<SiteInfo> it = sortedFavouriteSites.iterator();
        for (int counter = 0; counter < pageDetails.getEnd() && it.hasNext(); counter++)
        {
            SiteInfo favouriteSite = it.next();

            if (counter < pageDetails.getSkipCount())
            {
                continue;
            }

            if (counter > pageDetails.getEnd() - 1)
            {
                break;
            }

            page.add(favouriteSite);
        }

        return new PagingResults<SiteInfo>() {
            @Override
            public List<SiteInfo> getPage()
            {
                return page;
            }

            @Override
            public boolean hasMoreItems()
            {
                return pageDetails.hasMoreItems();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                Integer total = Integer.valueOf(sortedFavouriteSites.size());
                return new Pair<Integer, Integer>(total, total);
            }

            @Override
            public String getQueryExecutionId()
            {
                return null;
            }
        };
    }

    public CollectionWithPagingInfo<FavouriteSite> getFavouriteSites(String personId, Parameters parameters)
    {
        personId = people.validatePerson(personId);

        Paging paging = parameters.getPaging();
        BeanPropertiesFilter filter = parameters.getFilter();

        PagingResults<SiteInfo> favouriteSites = getFavouriteSites(personId, Util.getPagingRequest(paging));
        List<FavouriteSite> favourites = new ArrayList<FavouriteSite>(favouriteSites.getPage().size());
        for (SiteInfo favouriteSite : favouriteSites.getPage())
        {
            String role = null;
            if (filter.isAllowed(Site.ROLE))
            {
                role = getSiteRole(favouriteSite.getShortName(), personId);
            }
            FavouriteSite favourite = new FavouriteSite(favouriteSite, role);
            favourites.add(favourite);
        }

        return CollectionWithPagingInfo.asPaged(paging, favourites, favouriteSites.hasMoreItems(), favouriteSites.getTotalResultCount().getFirst());
    }

    public void deleteSite(String siteId, Parameters parameters)
    {
        boolean isSiteAdmin = siteService.isSiteAdmin(AuthenticationUtil.getFullyAuthenticatedUser());
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteId);
        }
        siteId = siteInfo.getShortName();

        NodeRef siteNodeRef = siteInfo.getNodeRef();

        // belt-and-braces - double-check before purge/delete (rather than
        // rollback)
        if ((isSiteAdmin == false) && (permissionService.hasPermission(siteNodeRef, PermissionService.DELETE) != AccessStatus.ALLOWED))
        {
            throw new AccessDeniedException("Cannot delete site: " + siteId);
        }

        // default false (if not provided)
        boolean permanentDelete = Boolean.valueOf(parameters.getParameter(PARAM_PERMANENT));

        if (permanentDelete == true)
        {
            // Set as temporary to delete node instead of archiving.
            nodeService.addAspect(siteNodeRef, ContentModel.ASPECT_TEMPORARY, null);

            // bypassing trashcan means that purge behaviour will not fire, so
            // explicitly force cleanup here
            siteServiceImpl.beforePurgeNode(siteNodeRef);
        }

        siteService.deleteSite(siteId);
    }

    /**
     * Uses site service for creating site info
     *
     * Extracted this call in a separate method because it might be needed to call different site service method when creating site info (e.g. siteService.createSite(String, String, String, String, SiteVisibility, QName))
     * 
     * @param site
     * @return
     */
    protected SiteInfo createSite(Site site)
    {
        if (site.getPreset() != null)
        {
            throw new InvalidArgumentException("Site preset should not be set");
        }
        return siteService.createSite(DEFAULT_SITE_PRESET, site.getId(), site.getTitle(), site.getDescription(), site.getVisibility());
    }

    /**
     * Create default/fixed preset (Share) site - with DocLib container/component
     *
     * @param site
     * @return
     */
    public Site createSite(Site site, Parameters parameters)
    {
        // note: if site id is null then will be generated from the site title
        site = validateSite(site);

        SiteInfo siteInfo = null;
        try
        {
            siteInfo = createSite(site);
        }
        catch (SiteServiceException sse)
        {
            if (sse.getMsgId().equals("site_service.unable_to_create"))
            {
                throw new ConstraintViolatedException(sse.getMessage());
            }
            else
            {
                throw sse;
            }
        }

        String siteId = siteInfo.getShortName();
        NodeRef siteNodeRef = siteInfo.getNodeRef();

        // default false (if not provided)
        boolean skipShareSurfConfig = Boolean.valueOf(parameters.getParameter(PARAM_SKIP_SURF_CONFIGURATION));
        if (skipShareSurfConfig == false)
        {
            // import default/fixed preset Share surf config
            importSite(siteId, siteNodeRef);
        }

        // pre-create doclib
        siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);

        // default false (if not provided)
        boolean skipAddToFavorites = Boolean.valueOf(parameters.getParameter(PARAM_SKIP_ADDTOFAVORITES));
        if (skipAddToFavorites == false)
        {
            String personId = AuthenticationUtil.getFullyAuthenticatedUser();
            favouritesService.addFavourite(personId, siteNodeRef); // ignore result
        }

        return getSite(siteInfo, true);
    }

    @Override
    public Site updateSite(String siteId, SiteUpdate update, Parameters parameters)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Updating site, ID: " + siteId + ", site data: " + update + ", parameters: " + parameters);
        }

        // Get the site by ID (aka short name)
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteId);
        }

        // Bind any provided values to the site info, allowing for "partial" updates.
        if (update.wasSet(Site.TITLE))
        {
            siteInfo.setTitle(update.getTitle());
        }
        if (update.wasSet(Site.DESCRIPTION))
        {
            siteInfo.setDescription(update.getDescription());
        }
        if (update.wasSet(Site.VISIBILITY))
        {
            siteInfo.setVisibility(update.getVisibility());
        }

        // Validate the new details
        validateSite(new Site(siteInfo, null));

        // Perform the actual update.
        siteService.updateSite(siteInfo);

        return getSite(siteId);
    }

    protected Site validateSite(Site site)
    {
        // site title - mandatory
        String siteTitle = site.getTitle();
        if ((siteTitle == null) || siteTitle.isEmpty())
        {
            throw new InvalidArgumentException("Site title is expected: " + siteTitle);
        }
        else if (siteTitle.length() > SITE_MAXLEN_TITLE)
        {
            throw new InvalidArgumentException("Site title exceeds max length of " + SITE_MAXLEN_TITLE + " characters");
        }

        SiteVisibility siteVisibility = site.getVisibility();
        if (siteVisibility == null)
        {
            throw new InvalidArgumentException("Site visibility is expected: " + siteTitle + " (eg. PUBLIC, PRIVATE, MODERATED)");
        }

        String siteId = site.getId();
        if (siteId == null)
        {
            // generate a site id from title (similar to Share create site dialog)
            siteId = siteTitle.trim(). // trim leading & trailing whitespace
                    replaceAll("[^" + SITE_ID_VALID_CHARS_PARTIAL_REGEX + " ]", ""). // remove special characters (except spaces)
                    replaceAll(" +", " "). // collapse multiple spaces to single space
                    replace(" ", "-"). // replaces spaces with dashs
                    toLowerCase(); // lowercase :-)
        }
        else
        {
            if (!siteId.matches("^[" + SITE_ID_VALID_CHARS_PARTIAL_REGEX + "]+"))
            {
                throw new InvalidArgumentException("Invalid site id - should consist of alphanumeric/dash characters");
            }
        }

        if (siteId.length() > SITE_MAXLEN_ID)
        {
            throw new InvalidArgumentException("Site id exceeds max length of " + SITE_MAXLEN_ID + " characters");
        }

        site.setId(siteId);

        String siteDescription = site.getDescription();

        if (siteDescription == null)
        {
            // workaround: to avoid Share error (eg. in My Sites dashlet / freemarker template)
            site.setDescription("");
        }

        if ((siteDescription != null) && (siteDescription.length() > SITE_MAXLEN_DESCRIPTION))
        {
            throw new InvalidArgumentException("Site description exceeds max length of " + SITE_MAXLEN_DESCRIPTION + " characters");
        }

        return site;
    }

    private void importSite(final String siteId, final NodeRef siteNodeRef)
    {
        ImportPackageHandler acpHandler = new SiteImportPackageHandler(siteSurfConfig, siteId);
        Location location = new Location(siteNodeRef);
        ImporterBinding binding = new ImporterBinding() {
            @Override
            public String getValue(String key)
            {
                if (key.equals("siteId"))
                {
                    return siteId;
                }
                return null;
            }

            @Override
            public UUID_BINDING getUUIDBinding()
            {
                return UUID_BINDING.CREATE_NEW;
            }

            @Override
            public QName[] getExcludedClasses()
            {
                return null;
            }

            @Override
            public boolean allowReferenceWithinTransaction()
            {
                return false;
            }

            @Override
            public ImporterContentCache getImportConentCache()
            {
                return null;
            }
        };
        importerService.importView(acpHandler, location, binding, null);
    }

    @Override
    public CollectionWithPagingInfo<SiteGroup> getSiteGroupMemberships(String siteId, Parameters parameters)
    {
        validateSite(siteId);

        PagingRequest pagingRequest = Util.getPagingRequest(parameters.getPaging());
        pagingRequest.setRequestTotalCountMax(100);
        PagingResults<SiteGroupMembership> pagedResults = siteService.listGroupMembersPaged(siteId, new ArrayList<>(), pagingRequest);
        List<SiteGroup> groups = pagedResults.getPage().stream().map(siteMembership -> new SiteGroup(siteMembership.getId(), siteMembership.getRole())).collect(Collectors.toList());
        return CollectionWithPagingInfo.asPaged(parameters.getPaging(), groups, pagedResults.hasMoreItems(), pagedResults.getTotalResultCount().getFirst());
    }

    @Override
    public SiteGroup addSiteGroupMembership(String siteId, SiteGroup group)
    {
        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            logger.debug("Site does not exist: " + siteId);
            throw new EntityNotFoundException(siteId);
        }

        validateGroup(group.getId());

        SiteMemberInfo groupInfo = siteService.getMembersRoleInfo(siteId, group.getId());
        if (groupInfo != null)
        {
            logger.debug("addSiteGroupMembership:  " + group.getId() + " is already a member of site " + siteId);
            throw new ConstraintViolatedException(group.getId() + " is already a member of site " + siteId);
        }
        if (group.getRole() == null)
        {
            logger.debug("Getting member role but role is null");
            throw new RelationshipResourceNotFoundException(group.getId(), siteId);
        }

        siteService.setMembership(siteId, group.getId(), group.getRole());
        return group;
    }

    @Override
    public SiteGroup getSiteGroupMembership(String siteId, String groupId)
    {
        SiteMemberInfo groupInfo = isMemberOfSite(siteId, groupId);
        return new SiteGroup(groupId, groupInfo.getMemberRole());
    }

    @Override
    public SiteGroup updateSiteGroupMembership(String siteId, SiteGroup group)
    {
        isMemberOfSite(siteId, group.getId());
        siteService.setMembership(siteId, group.getId(), group.getRole());
        return group;
    }

    @Override
    public void removeSiteGroupMembership(String siteId, String groupId)
    {
        isMemberOfSite(siteId, groupId);
        String role = this.siteService.getMembersRole(siteId, groupId);
        if (role != null)
        {
            if (role.equals(SiteModel.SITE_MANAGER))
            {
                int numAuthorities = this.siteService.countAuthoritiesWithRole(siteId, SiteModel.SITE_MANAGER);
                if (numAuthorities <= 1)
                {
                    throw new InvalidArgumentException("Can't remove last manager of site " + siteId);
                }
                this.siteService.removeMembership(siteId, groupId);
            }
            else
            {
                this.siteService.removeMembership(siteId, groupId);
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("Unable to determine role of site member");
        }

    }

    private SiteMemberInfo isMemberOfSite(String siteId, String id)
    {

        SiteInfo siteInfo = validateSite(siteId);
        if (siteInfo == null)
        {
            logger.debug("Site does not exist: " + siteId);
            throw new EntityNotFoundException(siteId);
        }

        validateGroup(id);

        SiteMemberInfo memberInfo = this.siteService.getMembersRoleInfo(siteId, id);
        if (memberInfo == null)
        {
            logger.debug("Given authority is not a member of the site");
            throw new InvalidArgumentException("Given authority is not a member of the site");
        }
        if (memberInfo.getMemberRole() == null)
        {
            logger.debug("Getting authority role but role is null");
            throw new RelationshipResourceNotFoundException(memberInfo.getMemberName(), siteId);
        }
        return memberInfo;
    }

    private void validateGroup(String groupId) throws EntityNotFoundException
    {
        String authorityName = authorityService.getName(AuthorityType.GROUP, groupId);
        if (authorityName == null)
        {
            logger.debug("AuthorityName does not exist: " + groupId);
            throw new EntityNotFoundException(groupId);
        }
    }
}
