/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.AlfMock;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rest.api.impl.SiteImportPackageHandler;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteUpdate;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.RMSite;
import org.alfresco.rm.rest.api.model.RMSiteCompliance;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit Test class for RMSitesImpl.
 *
 * @author Silviu Dinuta
 * @since 2.6
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RMSitesImplUnitTest  extends BaseUnitTest
{
    private static final String RM_SITE_TITLE_AFTER_UPDATE = "Updated Title";
    private static final String RM_SITE_DESCRIPTION_AFTER_UPDATE = "Updated Description";
    private static final String RM_SITE_ID = "rm";
    private static final String RM_SITE_MANAGER_ROLE = "SiteManager";
    private static final String RM_SITE_TITLE = "RM Site Title";
    private static final String RM_SITE_DESCRIPTION = "RM Site Description";
    private static final String RM_SITE_PRESET = "rm-site-dashboard";
    private static final String PARAM_SKIP_ADDTOFAVORITES = "skipAddToFavorites";
    @InjectMocks
    private RMSitesImpl rmSitesImpl;
    @Mock
    private SiteService mockedSiteService;
    @Mock
    private ImporterService mockedImporterService;
    @Mock
    private FavouritesService mockedFavouritesService;

    @Before
    public void before()
    {
    }

    @Test
    public void createRMStandardSite() throws Exception
    {
        RMSite toCreate = new RMSite();
        toCreate.setTitle(RM_SITE_TITLE);
        toCreate.setDescription(RM_SITE_DESCRIPTION);

        //mocked SiteInfo
        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        NodeRef siteNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedSiteInfo.getShortName()).thenReturn(RM_SITE_ID);
        when(mockedSiteInfo.getNodeRef()).thenReturn(siteNodeRef);
        when(mockedSiteInfo.getDescription()).thenReturn(RM_SITE_DESCRIPTION);
        when(mockedSiteInfo.getTitle()).thenReturn(RM_SITE_TITLE);
        when(mockedSiteInfo.getVisibility()).thenReturn(SiteVisibility.PUBLIC);

        when(mockedSiteService.createSite(any(String.class), any(String.class), any(String.class), any(String.class), any(SiteVisibility.class), any(QName.class))).thenReturn(mockedSiteInfo);

        //mock Parameters
        Parameters mockedParameters = mock(Parameters.class);
        //call createRMSite method
        RMSite createdRMSite = rmSitesImpl.createRMSite(toCreate, mockedParameters);

        //check siteService.createSite parameters
        ArgumentCaptor<String> sitePresetCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SiteVisibility> visibilityCaptor = ArgumentCaptor.forClass(SiteVisibility.class);
        ArgumentCaptor<QName> siteTypeCaptor = ArgumentCaptor.forClass(QName.class);
        verify(mockedSiteService, times(1)).createSite(sitePresetCaptor.capture(), idCaptor.capture(), titleCaptor.capture(), descriptionCaptor.capture(), visibilityCaptor.capture(), siteTypeCaptor.capture());
        assertEquals(RM_SITE_PRESET, sitePresetCaptor.getValue());
        assertEquals(RM_SITE_ID, idCaptor.getValue());
        assertEquals(RM_SITE_TITLE, titleCaptor.getValue());
        assertEquals(RM_SITE_DESCRIPTION, descriptionCaptor.getValue());
        assertEquals(SiteVisibility.PUBLIC, visibilityCaptor.getValue());
        assertEquals(RecordsManagementModel.TYPE_RM_SITE, siteTypeCaptor.getValue());

        verify(mockedImporterService, times(1)).importView(any(SiteImportPackageHandler.class), any(Location.class), any(ImporterBinding.class), eq(null));
        verify(mockedSiteService, times(1)).createContainer(RM_SITE_ID, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        verify(mockedFavouritesService, times(1)).addFavourite(nullable(String.class), any(NodeRef.class));

        //verify returned values for RM site are the right ones
        assertEquals(RMSiteCompliance.STANDARD, createdRMSite.getCompliance());
        assertEquals(null, createdRMSite.getRole());
        assertEquals(RM_SITE_ID, createdRMSite.getId());
        assertEquals(siteNodeRef.getId(), createdRMSite.getGuid());
        assertEquals(RM_SITE_DESCRIPTION, createdRMSite.getDescription());
        assertEquals(RM_SITE_TITLE, createdRMSite.getTitle());
        assertEquals(SiteVisibility.PUBLIC, createdRMSite.getVisibility());
    }

    @Test
    public void createRMDOD5015Site() throws Exception
    {
        RMSite toCreate = new RMSite();
        toCreate.setTitle(RM_SITE_TITLE);
        toCreate.setDescription(RM_SITE_DESCRIPTION);
        toCreate.setCompliance(RMSiteCompliance.DOD5015);

        //mocked SiteInfo
        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        NodeRef siteNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedSiteInfo.getShortName()).thenReturn(RM_SITE_ID);
        when(mockedSiteInfo.getNodeRef()).thenReturn(siteNodeRef);
        when(mockedSiteInfo.getDescription()).thenReturn(RM_SITE_DESCRIPTION);
        when(mockedSiteInfo.getTitle()).thenReturn(RM_SITE_TITLE);
        when(mockedSiteInfo.getVisibility()).thenReturn(SiteVisibility.PUBLIC);

        when(mockedSiteService.createSite(any(String.class), any(String.class), any(String.class), any(String.class), any(SiteVisibility.class), any(QName.class))).thenReturn(mockedSiteInfo);

        //mock Parameters
        Parameters mockedParameters = mock(Parameters.class);
        //call createRMSite method
        RMSite createdRMSite = rmSitesImpl.createRMSite(toCreate, mockedParameters);

        //check siteService.createSite parameters
        ArgumentCaptor<String> sitePresetCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SiteVisibility> visibilityCaptor = ArgumentCaptor.forClass(SiteVisibility.class);
        ArgumentCaptor<QName> siteTypeCaptor = ArgumentCaptor.forClass(QName.class);
        verify(mockedSiteService, times(1)).createSite(sitePresetCaptor.capture(), idCaptor.capture(), titleCaptor.capture(), descriptionCaptor.capture(), visibilityCaptor.capture(), siteTypeCaptor.capture());
        assertEquals(RM_SITE_PRESET, sitePresetCaptor.getValue());
        assertEquals(RM_SITE_ID, idCaptor.getValue());
        assertEquals(RM_SITE_TITLE, titleCaptor.getValue());
        assertEquals(RM_SITE_DESCRIPTION, descriptionCaptor.getValue());
        assertEquals(SiteVisibility.PUBLIC, visibilityCaptor.getValue());
        assertEquals(DOD5015Model.TYPE_DOD_5015_SITE, siteTypeCaptor.getValue());

        verify(mockedImporterService, times(1)).importView(any(SiteImportPackageHandler.class), any(Location.class), any(ImporterBinding.class), eq(null));
        verify(mockedSiteService, times(1)).createContainer(RM_SITE_ID, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        verify(mockedFavouritesService, times(1)).addFavourite(nullable(String.class), any(NodeRef.class));

        //verify returned values for RM site are the right ones
        assertEquals(RMSiteCompliance.DOD5015, createdRMSite.getCompliance());
        assertEquals(null, createdRMSite.getRole());
        assertEquals(RM_SITE_ID, createdRMSite.getId());
        assertEquals(siteNodeRef.getId(), createdRMSite.getGuid());
        assertEquals(RM_SITE_DESCRIPTION, createdRMSite.getDescription());
        assertEquals(RM_SITE_TITLE, createdRMSite.getTitle());
        assertEquals(SiteVisibility.PUBLIC, createdRMSite.getVisibility());
    }

    @Test
    public void createRMSiteWithSkipAddToFavouritesParameter() throws Exception
    {
        RMSite toCreate = new RMSite();
        toCreate.setTitle(RM_SITE_TITLE);
        toCreate.setDescription(RM_SITE_DESCRIPTION);

        //mocked SiteInfo
        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        NodeRef siteNodeRef = AlfMock.generateNodeRef(mockedNodeService);
        when(mockedSiteInfo.getShortName()).thenReturn(RM_SITE_ID);
        when(mockedSiteInfo.getNodeRef()).thenReturn(siteNodeRef);
        when(mockedSiteInfo.getDescription()).thenReturn(RM_SITE_DESCRIPTION);
        when(mockedSiteInfo.getTitle()).thenReturn(RM_SITE_TITLE);
        when(mockedSiteInfo.getVisibility()).thenReturn(SiteVisibility.PUBLIC);

        when(mockedSiteService.createSite(any(String.class), any(String.class), any(String.class), any(String.class), any(SiteVisibility.class), any(QName.class))).thenReturn(mockedSiteInfo);

        //mock Parameters
        Parameters mockedParameters = mock(Parameters.class);
        when(mockedParameters.getParameter(PARAM_SKIP_ADDTOFAVORITES)).thenReturn(Boolean.toString(true));

        //call createRMSite method
        rmSitesImpl.createRMSite(toCreate, mockedParameters);

        verify(mockedSiteService, times(1)).createSite(RM_SITE_PRESET, RM_SITE_ID, RM_SITE_TITLE, RM_SITE_DESCRIPTION, SiteVisibility.PUBLIC, RecordsManagementModel.TYPE_RM_SITE);
        verify(mockedImporterService, times(1)).importView(any(SiteImportPackageHandler.class), any(Location.class), any(ImporterBinding.class), eq(null));
        verify(mockedSiteService, times(1)).createContainer(RM_SITE_ID, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        verify(mockedFavouritesService, never()).addFavourite(any(String.class), any(NodeRef.class));
    }

    @Test
    public void updateRMSite() throws Exception
    {
        String siteId = RM_SITE_ID;
        SiteInfo mockedSiteInfo = mock(SiteInfo.class);
        NodeRef siteNodeRef = AlfMock.generateNodeRef(mockedNodeService);

        //mock SiteInfo
        when(mockedSiteInfo.getShortName()).thenReturn(siteId);
        when(mockedSiteInfo.getNodeRef()).thenReturn(siteNodeRef);
        when(mockedSiteInfo.getDescription()).thenReturn(RM_SITE_DESCRIPTION)
                                             .thenReturn(RM_SITE_DESCRIPTION_AFTER_UPDATE);
        when(mockedSiteInfo.getTitle()).thenReturn(RM_SITE_TITLE)
                                       .thenReturn(RM_SITE_TITLE_AFTER_UPDATE);
        when(mockedSiteInfo.getVisibility()).thenReturn(SiteVisibility.PUBLIC);

        when(mockedNodeService.getType(siteNodeRef)).thenReturn(RecordsManagementModel.TYPE_RM_SITE);

        when(mockedSiteService.getSite(siteId)).thenReturn(mockedSiteInfo);
        when(mockedSiteService.getMembersRole(eq(siteId), nullable(String.class))).thenReturn(RM_SITE_MANAGER_ROLE);

        //mock UpdateSite
        SiteUpdate mockedSiteUpdate= mock(SiteUpdate.class);
        when(mockedSiteUpdate.getDescription()).thenReturn(RM_SITE_DESCRIPTION_AFTER_UPDATE);
        when(mockedSiteUpdate.getTitle()).thenReturn(RM_SITE_TITLE_AFTER_UPDATE);
        when(mockedSiteUpdate.wasSet(Site.TITLE)).thenReturn(true);
        when(mockedSiteUpdate.wasSet(Site.DESCRIPTION)).thenReturn(true);

        //mock Parameters
        Parameters mockedParameters = mock(Parameters.class);

        //call updateRMSite method
        RMSite updatedRMSite = rmSitesImpl.updateRMSite(siteId, mockedSiteUpdate, mockedParameters);

        //check if the new title is set to siteInfo
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedSiteInfo, times(1)).setTitle(titleCaptor.capture());
        assertEquals(RM_SITE_TITLE_AFTER_UPDATE, titleCaptor.getValue());

        //check that new description is set to siteInfo
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockedSiteInfo, times(1)).setDescription(descriptionCaptor.capture());
        assertEquals(RM_SITE_DESCRIPTION_AFTER_UPDATE, descriptionCaptor.getValue());

        //check that site visibility is not changed
        verify(mockedSiteInfo, never()).setVisibility(any(SiteVisibility.class));

        //check that updateSite is called
        verify(mockedSiteService, times(1)).updateSite(any(SiteInfo.class));

        //verify returned values for RM site are the right ones
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
        when(mockedSiteService.getMembersRole(eq(siteId), nullable(String.class))).thenReturn(RM_SITE_MANAGER_ROLE);

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
