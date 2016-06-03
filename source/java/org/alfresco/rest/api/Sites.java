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

public interface Sites
{
	SiteInfo validateSite(String siteShortName);
	SiteInfo validateSite(NodeRef nodeRef);
    CollectionWithPagingInfo<SiteMember> getSiteMembers(String siteShortName, Parameters parameters);
    Site getSite(String siteId);
	void deleteSite(String siteId, Parameters parameters);
    Site createSite(Site site, Parameters parameters);
    
	/**
	 * people/<personId>/sites/<siteId>
	 * 
	 * @param personId String
     * @param siteShortName String
     * @return MemberOfSite
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
    
    String getSiteRole(String siteId);
    String getSiteRole(String siteId, String personId);

    String PARAM_PERMANENT = "permanent";
	String PARAM_SKIP_ADDTOFAVORITES = "skipAddToFavorites";
	String PARAM_SKIP_SURF_CONFIGURATION = "skipConfiguration";
}
