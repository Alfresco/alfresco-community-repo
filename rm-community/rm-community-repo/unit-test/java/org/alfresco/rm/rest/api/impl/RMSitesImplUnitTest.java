/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rm.rest.api.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.RMSite;
import org.alfresco.rm.rest.api.model.RMSiteCompliance;
import org.alfresco.rm.rest.api.model.SiteUpdate;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit Test class for RMSitesImpl.
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
public class RMSitesImplUnitTest  extends BaseUnitTest
{
    private static final String RM_SITE_TITLE_AFTER_UPDATE = "Updated Title";
    private static final String RM_SITE_DESCRIPTION_AFTER_UPDATE = "Updated Description";
    private static final String RM_SITE_ID = "rm";
    private static final String RM_SITE_MANAGER_ROLE = "SiteManager";
    private static final String RM_SITE_TITLE = "RM Site Title";
    private static final String RM_SITE_DESCRIPTION = "RM Site Description";
    @InjectMocks
    private RMSitesImpl rmSitesImpl;
    @Mock
    private SiteService mockedSiteService;
    @Mock
    AuthenticationUtil mockAuthenticationUtil;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createRMSite() throws Exception
    {
        //TODO
    }

    @Test
    public void updateRMSite() throws Exception
    {
        String siteId = RM_SITE_ID;
        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        NodeRef siteNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedSiteInfo.getShortName()).thenReturn(siteId);
        when(mockedSiteInfo.getNodeRef()).thenReturn(siteNodeRef);
        when(mockedSiteInfo.getDescription()).thenReturn(RM_SITE_DESCRIPTION)
                                             .thenReturn(RM_SITE_DESCRIPTION_AFTER_UPDATE);
        when(mockedSiteInfo.getTitle()).thenReturn(RM_SITE_TITLE)
                                       .thenReturn(RM_SITE_TITLE_AFTER_UPDATE);
        when(mockedSiteInfo.getVisibility()).thenReturn(SiteVisibility.PUBLIC);

        when(mockedNodeService.getType(siteNodeRef)).thenReturn(RecordsManagementModel.TYPE_RM_SITE);

        when(mockedSiteService.getSite(siteId)).thenReturn(mockedSiteInfo);
        when(mockedSiteService.getMembersRole(eq(siteId), any(String.class))).thenReturn(RM_SITE_MANAGER_ROLE);

        SiteUpdate mockedSiteUpdate= mock(SiteUpdate.class);
        when(mockedSiteUpdate.getDescription()).thenReturn(RM_SITE_DESCRIPTION_AFTER_UPDATE);
        when(mockedSiteUpdate.getTitle()).thenReturn(RM_SITE_TITLE_AFTER_UPDATE);
        when(mockedSiteUpdate.getVisibility()).thenReturn(null);

        Parameters mockedParameters = mock(Parameters.class);
        RMSite updatedRMSite = rmSitesImpl.updateRMSite(siteId, mockedSiteUpdate, mockedParameters);

        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedSiteInfo, times(1)).setTitle(titleCaptor.capture());
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedSiteInfo, times(1)).setDescription(descriptionCaptor.capture());
        verify(mockedSiteInfo, never()).setVisibility(any(SiteVisibility.class));

        verify(mockedSiteService, times(1)).updateSite(any(SiteInfo.class));

        assertEquals(RM_SITE_TITLE_AFTER_UPDATE, titleCaptor.getValue());
        assertEquals(RM_SITE_DESCRIPTION_AFTER_UPDATE, descriptionCaptor.getValue());

        assertEquals(RMSiteCompliance.STANDARD, updatedRMSite.getCompliance());
        assertEquals(RM_SITE_MANAGER_ROLE, updatedRMSite.getRole());
        assertEquals(siteId, updatedRMSite.getId());
        assertEquals(siteNodeRef.getId(), updatedRMSite.getGuid());
        assertEquals(RM_SITE_DESCRIPTION_AFTER_UPDATE, updatedRMSite.getDescription());
        assertEquals(RM_SITE_TITLE_AFTER_UPDATE, updatedRMSite.getTitle());
        assertEquals(SiteVisibility.PUBLIC, updatedRMSite.getVisibility());
    }

    @Test
    public void getRMSite() throws Exception
    {
        String siteId = RM_SITE_ID;
        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        NodeRef siteNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedSiteInfo.getShortName()).thenReturn(siteId);
        when(mockedSiteInfo.getNodeRef()).thenReturn(siteNodeRef);
        when(mockedSiteInfo.getDescription()).thenReturn(RM_SITE_DESCRIPTION);
        when(mockedSiteInfo.getTitle()).thenReturn(RM_SITE_TITLE);
        when(mockedSiteInfo.getVisibility()).thenReturn(SiteVisibility.PUBLIC);
        when(mockedNodeService.getType(siteNodeRef)).thenReturn(RecordsManagementModel.TYPE_RM_SITE);

        when(mockedSiteService.getSite(siteId)).thenReturn(mockedSiteInfo);
        when(mockedSiteService.getMembersRole(eq(siteId), any(String.class))).thenReturn(RM_SITE_MANAGER_ROLE);

        //STANDARD compliance
        RMSite rmSite = rmSitesImpl.getRMSite(siteId);
        assertEquals(RMSiteCompliance.STANDARD, rmSite.getCompliance());
        assertEquals(RM_SITE_MANAGER_ROLE, rmSite.getRole());
        assertEquals(siteId, rmSite.getId());
        assertEquals(siteNodeRef.getId(), rmSite.getGuid());
        assertEquals(RM_SITE_DESCRIPTION, rmSite.getDescription());
        assertEquals(RM_SITE_TITLE, rmSite.getTitle());
        assertEquals(SiteVisibility.PUBLIC, rmSite.getVisibility());

        //DOD5015 compliance
        when(mockedNodeService.getType(siteNodeRef)).thenReturn(DOD5015Model.TYPE_DOD_5015_SITE);
        rmSite = rmSitesImpl.getRMSite(siteId);
        assertEquals(RMSiteCompliance.DOD5015, rmSite.getCompliance());
        assertEquals(RM_SITE_MANAGER_ROLE, rmSite.getRole());
        assertEquals(siteId, rmSite.getId());
        assertEquals(siteNodeRef.getId(), rmSite.getGuid());
        assertEquals(RM_SITE_DESCRIPTION, rmSite.getDescription());
        assertEquals(RM_SITE_TITLE, rmSite.getTitle());
        assertEquals(SiteVisibility.PUBLIC, rmSite.getVisibility());
    }
}
