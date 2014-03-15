/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.text.Collator;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteMembership;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.FavouriteSite;
import org.alfresco.rest.api.model.MemberOfSite;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteContainer;
import org.alfresco.rest.api.model.SiteImpl;
import org.alfresco.rest.api.model.SiteMember;
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
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Centralises access to site services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class SitesImpl implements Sites
{
    private static final String FAVOURITE_SITES_PREFIX = "org.alfresco.share.sites.favourites.";
    private static final int FAVOURITE_SITES_PREFIX_LENGTH = FAVOURITE_SITES_PREFIX.length();

    protected Nodes nodes;
    protected People people;
    protected NodeService nodeService;
    protected DictionaryService dictionaryService;
    protected SiteService siteService;
    protected FavouritesService favouritesService;
    protected PreferenceService preferenceService;

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
        // set the site id to the short name (to deal with case sensitivity
        // issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        Paging paging = parameters.getPaging();

        PagingRequest pagingRequest = Util.getPagingRequest(paging);

        final List<Pair<SiteService.SortFields, Boolean>> sort = new ArrayList<Pair<SiteService.SortFields, Boolean>>();
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.LastName, Boolean.TRUE));
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.FirstName, Boolean.TRUE));
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.Role, Boolean.TRUE));
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.Username, Boolean.TRUE));
        PagingResults<SiteMembership> pagedResults = siteService.listMembersPaged(siteId, true, sort, pagingRequest);

        List<SiteMembership> siteMembers = pagedResults.getPage();
        List<SiteMember> ret = new ArrayList<SiteMember>(siteMembers.size());
        for (SiteMembership siteMembership : siteMembers)
        {
            SiteMember siteMember = new SiteMember(siteMembership.getPersonId(), siteMembership.getRole());
            ret.add(siteMember);
        }

        return CollectionWithPagingInfo.asPaged(paging, ret, pagedResults.hasMoreItems(), null);
    }

    public SiteRole getSiteRole(String siteId)
    {
        String personId = AuthenticationUtil.getFullyAuthenticatedUser();
        return getSiteRole(siteId, personId);
    }

    public SiteRole getSiteRole(String siteId, String personId)
    {
        String roleStr = siteService.getMembersRole(siteId, personId);
        SiteRole role = (roleStr != null ? SiteRole.valueOf(roleStr) : null);
        return role;
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
        // set the site id to the short name (to deal with case sensitivity
        // issues with using the siteId from the url)
        siteId = siteInfo.getShortName();
        SiteRole role = null;
        if (includeRole)
        {
            role = getSiteRole(siteId);
        }
        return new SiteImpl(siteInfo, role);
    }

    /**
     * people/<personId>/sites/<siteId>
     * 
     * @param siteId
     * @param personId
     * @return
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
        // set the site id to the short name (to deal with case sensitivity
        // issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        String roleStr = siteService.getMembersRole(siteInfo.getShortName(), personId);
        if (roleStr != null)
        {
            SiteRole role = SiteRole.valueOf(roleStr);
            SiteImpl site = new SiteImpl(siteInfo, role);
            siteMember = new MemberOfSite(site.getId(), siteInfo.getNodeRef(), role);
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
            throw new RelationshipResourceNotFoundException(personId, siteId);
        }
        siteId = siteInfo.getShortName();

        String role = siteService.getMembersRole(siteId, personId);
        if (role != null)
        {
            siteMember = new SiteMember(personId, SiteRole.valueOf(role));
        }
        else
        {
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
            throw new EntityNotFoundException(siteId);
        }
        // set the site id to the short name (to deal with case sensitivity
        // issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        SiteRole siteRole = siteMember.getRole();
        if (siteRole == null)
        {
            throw new InvalidArgumentException("Must provide a role");
        }
        String role = siteRole.name();

        if (siteService.isMember(siteId, personId))
        {
            throw new ConstraintViolatedException(personId + " is already a member of site " + siteId);
        }

        if (!siteService.canAddMember(siteId, personId, role))
        {
            throw new PermissionDeniedException();
        }

        siteService.setMembership(siteId, personId, role);
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
        // set the site id to the short name (to deal with case sensitivity
        // issues with using the siteId from the url)
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
        SiteRole siteRole = siteMember.getRole();
        if (siteRole == null)
        {
            throw new InvalidArgumentException("Must provide a role");
        }

        siteService.setMembership(siteId, siteMember.getPersonId(), siteRole.toString());
        return siteMember;
    }

    public CollectionWithPagingInfo<MemberOfSite> getSites(String personId, Parameters parameters)
    {
        Paging paging = parameters.getPaging();

        personId = people.validatePerson(personId);

        PagingRequest pagingRequest = Util.getPagingRequest(paging);

        final List<Pair<SiteService.SortFields, Boolean>> sort = new ArrayList<Pair<SiteService.SortFields, Boolean>>();
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.SiteTitle, Boolean.TRUE));
        sort.add(new Pair<SiteService.SortFields, Boolean>(SiteService.SortFields.Role, Boolean.TRUE));

        PagingResults<SiteMembership> results = siteService.listSitesPaged(personId, sort, pagingRequest);
        List<SiteMembership> siteMembers = results.getPage();
        List<MemberOfSite> ret = new ArrayList<MemberOfSite>(siteMembers.size());
        for (SiteMembership siteMember : siteMembers)
        {
            SiteInfo siteInfo = siteMember.getSiteInfo();
            MemberOfSite memberOfSite = new MemberOfSite(siteInfo.getShortName(), siteInfo.getNodeRef(),
                        siteMember.getRole());
            ret.add(memberOfSite);
        }

        return CollectionWithPagingInfo.asPaged(paging, ret, results.hasMoreItems(), null);
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
        // set the site id to the short name (to deal with case sensitivity
        // issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        NodeRef containerNodeRef = siteService.getContainer(siteId, containerId);
        if (containerNodeRef == null)
        {
            throw new RelationshipResourceNotFoundException(siteId, containerId);
        }

        // check that the containerId is actually a container for the specified
        // site
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

        final PagingResults<FileInfo> pagingResults = siteService.listContainers(siteInfo.getShortName(),
                    Util.getPagingRequest(paging));
        List<FileInfo> containerFileInfos = pagingResults.getPage();
        final List<SiteContainer> siteContainers = new ArrayList<SiteContainer>(containerFileInfos.size());
        for (FileInfo containerFileInfo : containerFileInfos)
        {
            NodeRef nodeRef = containerFileInfo.getNodeRef();
            String containerId = (String) nodeService.getProperty(nodeRef, SiteModel.PROP_COMPONENT_ID);
            SiteContainer siteContainer = new SiteContainer(containerId, nodeRef);
            siteContainers.add(siteContainer);
        }

        return new PagingResults<SiteContainer>()
        {
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
        final PagingRequest pagingRequest = Util.getPagingRequest(paging);
        // pagingRequest.setRequestTotalCountMax(requestTotalCountMax)
        final List<Pair<QName, Boolean>> sortProps = new ArrayList<Pair<QName, Boolean>>();
        sortProps.add(new Pair<QName, Boolean>(ContentModel.PROP_NAME, Boolean.TRUE));

        PagingResults<SiteInfo> pagingResult = null;
        boolean withSiteAdminScope = Boolean.valueOf(parameters.getParameter("admin"));
        if (withSiteAdminScope && siteService.isSiteAdmin(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            pagingResult = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<PagingResults<SiteInfo>>()
            {
                public PagingResults<SiteInfo> doWork() throws Exception
                {
                    return siteService.listSites(null, sortProps, pagingRequest);
                }
            }, AuthenticationUtil.getSystemUserName());
        }
        else
        {
            pagingResult = siteService.listSites(null, sortProps, pagingRequest);
        }

        final List<SiteInfo> sites = pagingResult.getPage();
        int totalItems = pagingResult.getTotalResultCount().getFirst();
        final String personId = AuthenticationUtil.getFullyAuthenticatedUser();
        List<Site> page = new AbstractList<Site>()
        {
            @Override
            public SiteImpl get(int index)
            {
                SiteInfo siteInfo = sites.get(index);

                SiteRole role = null;
                if (filter.isAllowed(Site.ROLE))
                {
                    String roleStr = siteService.getMembersRole(siteInfo.getShortName(), personId);
                    role = (roleStr != null ? SiteRole.valueOf(roleStr) : null);
                }
                return new SiteImpl(siteInfo, role);
            }

            @Override
            public int size()
            {
                return sites.size();
            }
        };

        return CollectionWithPagingInfo.asPaged(paging, page, pagingResult.hasMoreItems(), totalItems);
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
        // set the site id to the short name (to deal with case sensitivity
        // issues with using the siteId from the url)
        siteId = siteInfo.getShortName();
        NodeRef nodeRef = siteInfo.getNodeRef();

        if (favouritesService.isFavourite(personId, nodeRef))
        {
            SiteRole role = getSiteRole(siteId, personId);
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
        // set the site id to the short name (to deal with case sensitivity
        // issues with using the siteId from the url)
        siteId = siteInfo.getShortName();

        StringBuilder prefKey = new StringBuilder(FAVOURITE_SITES_PREFIX);
        prefKey.append(siteId);
        String value = (String) preferenceService.getPreference(personId, prefKey.toString());
        boolean isFavouriteSite = (value == null ? false : value.equalsIgnoreCase("true"));

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
        boolean isFavouriteSite = (value == null ? false : value.equalsIgnoreCase("true"));

        if (!isFavouriteSite)
        {
            throw new NotFoundException("Site " + siteId + " is not a favourite site");
        }

        preferenceService.clearPreferences(personId, prefKey.toString());
    }

    private PagingResults<SiteInfo> getFavouriteSites(String userName, PagingRequest pagingRequest)
    {
        final Collator collator = Collator.getInstance();

        final Set<SiteInfo> sortedFavouriteSites = new TreeSet<SiteInfo>(new Comparator<SiteInfo>()
        {
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
                String siteShortName = entry.getKey().substring(FAVOURITE_SITES_PREFIX_LENGTH)
                            .replace(".favourited", "");
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

        return new PagingResults<SiteInfo>()
        {
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
            SiteRole role = null;
            if (filter.isAllowed(Site.ROLE))
            {
                role = getSiteRole(favouriteSite.getShortName(), personId);
            }
            FavouriteSite favourite = new FavouriteSite(favouriteSite, role);
            favourites.add(favourite);
        }

        return CollectionWithPagingInfo.asPaged(paging, favourites, favouriteSites.hasMoreItems(), favouriteSites
                    .getTotalResultCount().getFirst());
    }

    @Override
    public SiteImpl updateSite(final String siteShortName, final SiteImpl siteImpl)
    {
        if (siteService.isSiteAdmin(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<SiteImpl>()
            {
                public SiteImpl doWork() throws Exception
                {
                    // We have to wrap the whole method not just
                    // siteService.updateSite, as validateSite
                    // won't give us the private sites for site-admins
                    return updateSiteImpl(siteShortName, siteImpl);
                }
            }, AuthenticationUtil.getSystemUserName());
        }
        else
        {
            return updateSiteImpl(siteShortName, siteImpl);
        }
    }

    private SiteImpl updateSiteImpl(String siteShortName, SiteImpl siteImpl)
    {
        SiteInfo siteInfo = validateSite(siteShortName);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteShortName);
        }
        // Set the site's visibility
        SiteVisibility siteVisibility = siteImpl.getVisibility();
        if (siteVisibility == null)
        {
            throw new InvalidArgumentException("Must provide visibility");
        }
        siteInfo.setVisibility(siteImpl.getVisibility());
        siteService.updateSite(siteInfo);

        return siteImpl;
    }

    @Override
    public void deleteSite(final String siteShortName)
    {
        if (siteService.isSiteAdmin(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    deleteSiteImpl(siteShortName);
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
        else
        {
            deleteSiteImpl(siteShortName);
        }
    }

    private void deleteSiteImpl(String siteShortName)
    {
        SiteInfo siteInfo = validateSite(siteShortName);
        if (siteInfo == null)
        {
            // site does not exist
            throw new EntityNotFoundException(siteShortName);
        }
        siteService.deleteSite(siteInfo.getShortName());
    }
}
