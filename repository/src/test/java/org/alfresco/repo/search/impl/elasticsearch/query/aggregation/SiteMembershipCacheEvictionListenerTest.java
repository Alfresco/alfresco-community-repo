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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authority.AuthorityServicePolicies.OnAuthorityAddedToGroup;
import org.alfresco.repo.security.authority.AuthorityServicePolicies.OnAuthorityRemovedFromGroup;
import org.alfresco.service.cmr.site.SiteService;

/**
 * Unit tests for {@link SiteMembershipCacheEvictionListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SiteMembershipCacheEvictionListenerTest
{
    private static final String SITE_GROUP = "GROUP_site_engineering_SiteManager";
    private static final String SITE_SHORT_NAME = "engineering";
    private static final String NON_SITE_GROUP = "GROUP_ALFRESCO_ADMINISTRATORS";
    private static final String USER = "jbloggs";

    @Mock
    private PolicyComponent policyComponent;
    @Mock
    private SiteService siteService;
    @Mock
    private SimpleCache<String, Map<String, String>> sitesCache;

    private SiteMembershipCacheEvictionListener listener;

    @Before
    public void setUp()
    {
        listener = new SiteMembershipCacheEvictionListener(policyComponent, siteService, sitesCache);
        when(siteService.resolveSite(SITE_GROUP)).thenReturn(SITE_SHORT_NAME);
        when(siteService.resolveSite(NON_SITE_GROUP)).thenReturn(null);
    }

    @Test
    public void onAuthorityAddedToGroup_siteGroup_evictsUser()
    {
        listener.onAuthorityAddedToGroup(SITE_GROUP, USER);

        verify(sitesCache).remove(USER);
    }

    @Test
    public void onAuthorityRemovedFromGroup_siteGroup_evictsUser()
    {
        listener.onAuthorityRemovedFromGroup(SITE_GROUP, USER);

        verify(sitesCache).remove(USER);
    }

    @Test
    public void onAuthorityAddedToGroup_nonSiteGroup_doesNotEvict()
    {
        listener.onAuthorityAddedToGroup(NON_SITE_GROUP, USER);

        verify(sitesCache, never()).remove(anyString());
    }

    @Test
    public void onAuthorityRemovedFromGroup_nonSiteGroup_doesNotEvict()
    {
        listener.onAuthorityRemovedFromGroup(NON_SITE_GROUP, USER);

        verify(sitesCache, never()).remove(anyString());
    }

    @Test
    public void init_bindsBothMembershipPolicies()
    {
        listener.init();

        verify(policyComponent).bindClassBehaviour(eq(OnAuthorityAddedToGroup.QNAME), eq(ContentModel.TYPE_BASE),
                any(JavaBehaviour.class));
        verify(policyComponent).bindClassBehaviour(eq(OnAuthorityRemovedFromGroup.QNAME), eq(ContentModel.TYPE_BASE),
                any(JavaBehaviour.class));
    }
}
