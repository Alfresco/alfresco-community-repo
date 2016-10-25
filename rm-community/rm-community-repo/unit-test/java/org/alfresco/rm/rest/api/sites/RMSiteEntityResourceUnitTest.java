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

package org.alfresco.rm.rest.api.sites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rm.rest.api.RMSites;
import org.alfresco.rm.rest.api.model.RMSite;
import org.alfresco.rm.rest.api.model.RMSiteCompliance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test class for RMSiteEntityResource.
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
public class RMSiteEntityResourceUnitTest extends BaseUnitTest
{
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
        RMSite rmSite = createRMSite();
        List<RMSite> entity = new ArrayList<RMSite>();
        Params parameters = Params.valueOf(null, null, null, rmSite, null);
        entity.add(rmSite);
        when(mockedRMSites.createRMSite(rmSite, parameters)).thenReturn(rmSite);
        List<RMSite> createdRMSites = rmSiteEntityResource.create(entity, parameters);
        assertEquals("Created sites size should be 1.", 1, createdRMSites.size());
        assertNotNull(createdRMSites.get(0));
        assertEquals(rmSite, createdRMSites.get(0));
    }

    @Test
    public void delete() throws Exception
    {
        String siteId = "rm";
        Params parameters = Params.valueOf(siteId, null, null, null, null);
        rmSiteEntityResource.delete(siteId, parameters);
        verify(mockedRMSites, times(1)).deleteSite(siteId, parameters);
    }

    @Test
    public void get() throws Exception
    {

    }

    @Test
    public void update() throws Exception
    {

    }

    private RMSite createRMSite()
    {
        RMSite rmSite = new RMSite();

        rmSite.setTitle("RM Site Title");
        rmSite.setId("rm");
        rmSite.setDescription("RM Site Description");
        rmSite.setCompliance(RMSiteCompliance.STANDARD);
        return rmSite;
    }
}
