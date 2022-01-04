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

package org.alfresco.rm.rest.api.sites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rest.api.model.SiteUpdate;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rm.rest.api.RMSites;
import org.alfresco.rm.rest.api.model.RMSite;
import org.alfresco.rm.rest.api.model.RMSiteCompliance;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit Test class for RMSiteEntityResource.
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
public class RMSiteEntityResourceUnitTest extends BaseUnitTest
{
    private static final String NON_RM_SITE_ID = "not_rm";

    private static final String PERMANENT_PARAMETER = "permanent";

    private static final String RM_SITE_ID = "rm";

    private static final String RM_SITE_DESCRIPTION = "RM Site Description";

    private static final String RM_SITE_TITLE = "RM Site Title";

    @Mock
    private RMSites mockedRMSites;

    @InjectMocks
    private RMSiteEntityResource rmSiteEntityResource;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void create() throws Exception
    {
        RMSite rmSite = new RMSite();
        rmSite.setTitle(RM_SITE_TITLE);
        rmSite.setId(RM_SITE_ID);
        rmSite.setDescription(RM_SITE_DESCRIPTION);
        rmSite.setCompliance(RMSiteCompliance.STANDARD);

        List<RMSite> entity = new ArrayList<>();
        Params parameters = mock(Params.class);
        entity.add(rmSite);
        when(mockedRMSites.createRMSite(rmSite, parameters)).thenReturn(rmSite);
        List<RMSite> createdRMSites = rmSiteEntityResource.create(entity, parameters);

        verify(mockedRMSites, times(1)).createRMSite(rmSite, parameters);

        assertEquals("Created sites size should be 1.", 1, createdRMSites.size());
        assertNotNull(createdRMSites.get(0));
        assertEquals(rmSite, createdRMSites.get(0));
    }

    @Test
    public void happyPathDelete() throws Exception
    {
        String siteId = RM_SITE_ID;
        Params parameters = mock(Params.class);
        when(parameters.getParameter(PERMANENT_PARAMETER)).thenReturn(null);
        rmSiteEntityResource.delete(siteId, parameters);
        verify(mockedRMSites, times(1)).deleteRMSite(siteId, parameters);
    }

    @Test
    public void deleteNonRMSite() throws Exception
    {
        String siteId = NON_RM_SITE_ID;
        Params parameters = mock(Params.class);
        when(parameters.getParameter(PERMANENT_PARAMETER)).thenReturn(null);
        try
        {
            rmSiteEntityResource.delete(siteId, parameters);
            fail("Expected ecxeption as siteId was different than rm");
        }
        catch(InvalidParameterException ex)
        {
            assertEquals("The Deletion is supported only for siteId = rm.", ex.getMessage());
        }
        verify(mockedRMSites, never()).deleteRMSite(siteId, parameters);
    }

    @Test
    public void deleteRMSiteWithPermanentParam() throws Exception
    {
        String siteId = RM_SITE_ID;
        Params parameters = mock(Params.class);
        when(parameters.getParameter(PERMANENT_PARAMETER)).thenReturn(Boolean.toString(true));
        try
        {
            rmSiteEntityResource.delete(siteId, parameters);
            fail("Expected ecxeption as parameter permanent was present in the request.");
        }
        catch(InvalidArgumentException ex)
        {
            assertEquals("DELETE does not support parameter: permanent", ex.getMsgId());
        }
        verify(mockedRMSites, never()).deleteSite(siteId, parameters);
    }

    @Test
    public void happyPathGet() throws Exception
    {
        String siteId = RM_SITE_ID;
        Params parameters = mock(Params.class);
        rmSiteEntityResource.readById(siteId, parameters);
        verify(mockedRMSites, times(1)).getRMSite(siteId);
    }

    @Test
    public void getNonRMSite() throws Exception
    {
        String siteId = NON_RM_SITE_ID;
        Params parameters = mock(Params.class);
        try
        {
            rmSiteEntityResource.readById(siteId, parameters);
            fail("Expected ecxeption as siteId was different than rm");
        }
        catch(InvalidParameterException ex)
        {
            assertEquals("GET is supported only for siteId = rm.", ex.getMessage());
        }
        verify(mockedRMSites, never()).getRMSite(siteId);
    }

    @Test
    public void happyPathUpdate() throws Exception
    {
        String siteId = RM_SITE_ID;
        Params parameters = mock(Params.class);
        RMSite site = new RMSite();
        site.setTitle("New Title");
        site.setDescription("New Description");
        rmSiteEntityResource.update(siteId, site, parameters);
        verify(mockedRMSites, times(1)).updateRMSite(any(String.class), any(SiteUpdate.class), any(Parameters.class));
    }

    @Test
    public void updateNonRMSite() throws Exception
    {
        String siteId = NON_RM_SITE_ID;
        Params parameters = mock(Params.class);
        RMSite site = new RMSite();
        site.setTitle("New Title");
        site.setDescription("New Description");
        try
        {
            rmSiteEntityResource.update(siteId, site, parameters);
            fail("Expected ecxeption as siteId was different than rm");
        }
        catch(InvalidParameterException ex)
        {
            assertEquals("The Update is supported only for siteId = rm.", ex.getMessage());
        }
        verify(mockedRMSites, never()).updateRMSite(any(String.class), any(SiteUpdate.class), any(Parameters.class));
    }

    @Test
    public void updateRMSiteId() throws Exception
    {
        String siteId = RM_SITE_ID;
        Params parameters = mock(Params.class);
        RMSite site = new RMSite();
        site.setTitle("New Title");
        site.setDescription("New Description");
        site.setId("newSiteID");
        try
        {
            rmSiteEntityResource.update(siteId, site, parameters);
            fail("Expected ecxeption as rm site id cannot be changed.");
        }
        catch(InvalidArgumentException ex)
        {
            assertEquals("Site update does not support field: id", ex.getMsgId());
        }
        verify(mockedRMSites, never()).updateRMSite(any(String.class), any(SiteUpdate.class), any(Parameters.class));
    }

    @Test
    public void updateRMSiteGuid() throws Exception
    {
        String siteId = RM_SITE_ID;
        Params parameters = mock(Params.class);
        RMSite site = new RMSite();
        site.setTitle("New Title");
        site.setDescription("New Description");
        site.setGuid("newGUID");
        try
        {
            rmSiteEntityResource.update(siteId, site, parameters);
            fail("Expected ecxeption as rm site guid cannot be changed.");
        }
        catch(InvalidArgumentException ex)
        {
            assertEquals("Site update does not support field: guid", ex.getMsgId());
        }
        verify(mockedRMSites, never()).updateRMSite(any(String.class), any(SiteUpdate.class), any(Parameters.class));
    }

    @Test
    public void updateRMSiteRole() throws Exception
    {
        String siteId = RM_SITE_ID;
        Params parameters = mock(Params.class);
        RMSite site = new RMSite();
        site.setTitle("New Title");
        site.setDescription("New Description");
        site.setRole("newRole");
        try
        {
            rmSiteEntityResource.update(siteId, site, parameters);
            fail("Expected ecxeption as rm site role cannot be changed.");
        }
        catch(InvalidArgumentException ex)
        {
            assertEquals("Site update does not support field: role", ex.getMsgId());
        }
        verify(mockedRMSites, never()).updateRMSite(any(String.class), any(SiteUpdate.class), any(Parameters.class));
    }

    @Test
    public void updateRMSiteCompliance() throws Exception
    {
        String siteId = RM_SITE_ID;
        Params parameters = mock(Params.class);
        RMSite site = new RMSite();
        site.setTitle("New Title");
        site.setDescription("New Description");
        site.setCompliance(RMSiteCompliance.STANDARD);
        try
        {
            rmSiteEntityResource.update(siteId, site, parameters);
            fail("Expected ecxeption as rm site compliance cannot be changed.");
        }
        catch(InvalidArgumentException ex)
        {
            assertEquals("Site update does not support field: compliance", ex.getMsgId());
        }
        verify(mockedRMSites, never()).updateRMSite(any(String.class), any(SiteUpdate.class), any(Parameters.class));
    }

    @Test
    public void updateRMSiteVisibility() throws Exception
    {
        String siteId = RM_SITE_ID;
        Params parameters = mock(Params.class);
        RMSite site = new RMSite();
        site.setTitle("New Title");
        site.setDescription("New Description");
        site.setVisibility(SiteVisibility.PRIVATE);
        try
        {
            rmSiteEntityResource.update(siteId, site, parameters);
            fail("Expected ecxeption as rm site visibility cannot be changed.");
        }
        catch(InvalidArgumentException ex)
        {
            assertEquals("Site update does not support field: visibility", ex.getMsgId());
        }
        verify(mockedRMSites, never()).updateRMSite(any(String.class), any(SiteUpdate.class), any(Parameters.class));
    }
}
