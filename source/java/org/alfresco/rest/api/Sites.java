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
package org.alfresco.rest.api;

import org.alfresco.query.PagingResults;
import org.alfresco.rest.api.model.FavouriteSite;
import org.alfresco.rest.api.model.MemberOfSite;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteContainer;
import org.alfresco.rest.api.model.SiteMember;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;

public interface Sites
{
	SiteInfo validateSite(String siteShortName);
	SiteInfo validateSite(NodeRef nodeRef);
    CollectionWithPagingInfo<SiteMember> getSiteMembers(String siteShortName, Parameters parameters);
    Site getSite(String siteId);
    
	/**
	 * people/<personId>/sites/<siteId>
	 * 
	 * @param siteId
	 * @param personId
	 * @return
	 */
	MemberOfSite getMemberOfSite(String personId, String siteShortName);
	SiteMember getSiteMember(String personId, String siteShortName);
	SiteMember addSiteMember(String siteShortName, SiteMember siteMember);
	void removeSiteMember(String personId, String siteId);
	SiteMember updateSiteMember(String siteShortName, SiteMember siteMember);
	CollectionWithPagingInfo<MemberOfSite> getSites(String personId, Parameters parameters);
	SiteContainer getSiteContainer(String siteShortName, String containerId);
	PagingResults<SiteContainer> getSiteContainers(String siteShortName, Paging paging);
	CollectionWithPagingInfo<Site> getSites(Parameters parameters);
    FavouriteSite getFavouriteSite(String personId, String siteShortName);
    void addFavouriteSite(String personId, FavouriteSite favouriteSite);
    void removeFavouriteSite(String personId, String siteId);
    CollectionWithPagingInfo<FavouriteSite> getFavouriteSites(String personId, Parameters parameters);
    
    SiteRole getSiteRole(String siteId);
    SiteRole getSiteRole(String siteId, String personId);
}
