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
package org.alfresco.repo.search.impl.elasticsearch.query.aggregation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authority.AuthorityServicePolicies.OnAuthorityAddedToGroup;
import org.alfresco.repo.security.authority.AuthorityServicePolicies.OnAuthorityRemovedFromGroup;
import org.alfresco.service.cmr.site.SiteService;

/**
 * Evicts a user's entry from the {@link SiteTermsAggregationBuilder} sites cache whenever that user is added to or removed from a site, since site membership changes what {@link SiteService#listSites(String, String)} returns for them.
 */
public class SiteMembershipCacheEvictionListener implements OnAuthorityAddedToGroup, OnAuthorityRemovedFromGroup
{
    private final PolicyComponent policyComponent;
    private final SiteService siteService;
    private final SimpleCache<String, Map<String, String>> sitesCache;

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteMembershipCacheEvictionListener.class);

    public SiteMembershipCacheEvictionListener(PolicyComponent policyComponent, SiteService siteService,
            SimpleCache<String, Map<String, String>> sitesCache)
    {
        this.policyComponent = policyComponent;
        this.siteService = siteService;
        this.sitesCache = sitesCache;
    }

    public void init()
    {
        policyComponent.bindClassBehaviour(OnAuthorityAddedToGroup.QNAME, ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onAuthorityAddedToGroup"));
        policyComponent.bindClassBehaviour(OnAuthorityRemovedFromGroup.QNAME, ContentModel.TYPE_BASE,
                new JavaBehaviour(this, "onAuthorityRemovedFromGroup"));
    }

    @Override
    public void onAuthorityAddedToGroup(String parentGroup, String childAuthority)
    {
        evictIfSiteGroup(parentGroup, childAuthority);
    }

    @Override
    public void onAuthorityRemovedFromGroup(String parentGroup, String childAuthority)
    {
        evictIfSiteGroup(parentGroup, childAuthority);
    }

    private void evictIfSiteGroup(String parentGroup, String childAuthority)
    {
        if (parentGroup != null && siteService.resolveSite(parentGroup) != null)
        {
            LOGGER.debug("Evicting sites cache entry for '{}' following a membership change in site group '{}'.", childAuthority, parentGroup);
            sitesCache.remove(childAuthority);
        }
    }
}
