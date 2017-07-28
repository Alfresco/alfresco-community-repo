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
package org.alfresco.rest.api.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.discovery.DiscoveryApiWebscript;
import org.alfresco.rest.api.model.DiscoveryDetails;
import org.alfresco.rest.api.model.ModulePackage;
import org.alfresco.rest.api.model.RepositoryInfo;
import org.alfresco.rest.api.model.RepositoryInfo.LicenseEntitlement;
import org.alfresco.rest.api.model.RepositoryInfo.LicenseInfo;
import org.alfresco.rest.api.model.RepositoryInfo.StatusInfo;
import org.alfresco.rest.api.model.RepositoryInfo.VersionInfo;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.admin.RepoUsage.LicenseMode;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.service.license.LicenseDescriptor;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Date;
import java.util.List;

/**
 * V1 REST API tests for retrieving detailed repository information.
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/discovery} </li>
 * </ul>
 *
 * @author Jamal Kaabi-Mofrad
 */
public class DiscoveryApiTest extends AbstractSingleNetworkSiteTest
{

    private static final DateTime NOW = DateTime.now();

    @Mock
    private DescriptorService descriptorServiceMock;
    @Mock
    private Descriptor serverDescriptor;
    @Mock
    private LicenseDescriptor licenseDescriptorMock;
    private DiscoveryApiWebscript discoveryApiWebscript;

    private Date licenseIssuedAt;
    private Date licenseExpiresAt;

    @Before
    public void setup() throws Exception
    {
        super.setup();

        this.licenseIssuedAt = NOW.toDate();
        this.licenseExpiresAt = NOW.plusDays(5).toDate();

        // Mock the required descriptors
        this.descriptorServiceMock = mock(DescriptorService.class);
        this.serverDescriptor = mock(Descriptor.class);
        this.licenseDescriptorMock = mock(LicenseDescriptor.class);

        when(descriptorServiceMock.getServerDescriptor()).thenReturn(serverDescriptor);
        when(serverDescriptor.getEdition()).thenReturn("Enterprise");
        when(serverDescriptor.getVersionMajor()).thenReturn("5");
        when(serverDescriptor.getVersionMinor()).thenReturn("2");
        when(serverDescriptor.getVersionRevision()).thenReturn("1");
        when(serverDescriptor.getVersionLabel()).thenReturn(".3");
        when(serverDescriptor.getVersionBuild()).thenReturn("r123456-b0");
        when(serverDescriptor.getSchema()).thenReturn(10051);

        when(descriptorServiceMock.getLicenseDescriptor()).thenReturn(licenseDescriptorMock);
        when(licenseDescriptorMock.getIssued()).thenReturn(this.licenseIssuedAt);
        when(licenseDescriptorMock.getValidUntil()).thenReturn(this.licenseExpiresAt);
        when(licenseDescriptorMock.getRemainingDays()).thenReturn(5);
        when(licenseDescriptorMock.getLicenseMode()).thenReturn(LicenseMode.ENTERPRISE);
        when(licenseDescriptorMock.getHolderOrganisation()).thenReturn("Alfresco Dev Test");
        when(licenseDescriptorMock.getMaxUsers()).thenReturn(20L);
        when(licenseDescriptorMock.getMaxDocs()).thenReturn(1000L);
        when(licenseDescriptorMock.isClusterEnabled()).thenReturn(true);

        // Override the descriptor service
        discoveryApiWebscript = applicationContext
                    .getBean("webscript.org.alfresco.api.DiscoveryApiWebscript.get", DiscoveryApiWebscript.class);
        discoveryApiWebscript.setDescriptorService(descriptorServiceMock);
        discoveryApiWebscript.setEnabled(true);
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Tests get discovery.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/discovery}
     */
    @Test
    public void testGetDiscovery() throws Exception
    {
        setRequestContext(null, user1, "wrongPassword");
        get("discovery", null, 401);

        setRequestContext(null, user1, null);
        HttpResponse response = get("discovery", null, 200);

        DiscoveryDetails discoveryDetails = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), DiscoveryDetails.class);
        assertNotNull(discoveryDetails);
        RepositoryInfo repositoryInfo = discoveryDetails.getRepository();
        assertNotNull(repositoryInfo);
        assertEquals("Enterprise", repositoryInfo.getEdition());

        // Check version info
        VersionInfo versionInfo = repositoryInfo.getVersion();
        assertNotNull(versionInfo);
        assertEquals("5", versionInfo.getMajor());
        assertEquals("2", versionInfo.getMinor());
        assertEquals("1", versionInfo.getPatch());
        assertEquals("3", versionInfo.getHotfix());
        assertEquals("r123456-b0", versionInfo.getLabel());
        assertEquals(10051, versionInfo.getSchema());
        assertEquals("5.2.1.3 (r123456-b0) schema 10051", versionInfo.getDisplay());

        // Check license info
        LicenseInfo licenseInfo = repositoryInfo.getLicense();
        assertNotNull(licenseInfo);
        assertEquals(LicenseMode.ENTERPRISE.name(), licenseInfo.getMode());
        assertEquals(licenseIssuedAt.toString(), licenseInfo.getIssuedAt().toString());
        assertEquals(licenseExpiresAt.toString(), licenseInfo.getExpiresAt().toString());
        assertEquals(Integer.valueOf(5), licenseInfo.getRemainingDays());
        assertEquals("Alfresco Dev Test", licenseInfo.getHolder());
        LicenseEntitlement entitlements = licenseInfo.getEntitlements();
        assertNotNull(entitlements);
        assertNotNull(entitlements.getMaxUsers());
        assertEquals(20L, entitlements.getMaxUsers().longValue());
        assertNotNull(entitlements.getMaxDocs());
        assertEquals(1000L, entitlements.getMaxDocs().longValue());
        assertTrue(entitlements.getIsClusterEnabled());
        assertFalse(entitlements.getIsCryptodocEnabled());

        // Check status
        StatusInfo statusInfo = repositoryInfo.getStatus();
        assertNotNull(statusInfo);
        assertFalse(statusInfo.getIsReadOnly());
        assertTrue(statusInfo.getIsAuditEnabled());
        assertTrue(statusInfo.getIsQuickShareEnabled());
        assertTrue(statusInfo.getIsThumbnailGenerationEnabled());

        // Check modules
        List<ModulePackage> modulePackageList = repositoryInfo.getModules();
        assertNotNull(modulePackageList);
    }

    /**
     * Tests get discovery.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/discovery}
     */
    @Test
    public void testGetDiscovery_hotfixValue() throws Exception
    {
        /*
        * The agreement was that if the hotfix value (versionLabel) does not follow the standard
        * of "dot then digits" or just "digits", the API should return zero.
         */

        when(serverDescriptor.getVersionLabel()).thenReturn("4");
        setRequestContext(null, user1, null);
        HttpResponse response = get("discovery", null, 200);

        DiscoveryDetails discoveryDetails = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), DiscoveryDetails.class);
        assertNotNull(discoveryDetails);
        RepositoryInfo repositoryInfo = discoveryDetails.getRepository();
        assertNotNull(repositoryInfo);
        assertEquals("Enterprise", repositoryInfo.getEdition());

        // Check version info
        VersionInfo versionInfo = repositoryInfo.getVersion();
        assertNotNull(versionInfo);
        assertEquals("5", versionInfo.getMajor());
        assertEquals("2", versionInfo.getMinor());
        assertEquals("1", versionInfo.getPatch());
        assertEquals("4", versionInfo.getHotfix());
        assertEquals("r123456-b0", versionInfo.getLabel());
        assertEquals(10051, versionInfo.getSchema());
        assertEquals("5.2.1.4 (r123456-b0) schema 10051", versionInfo.getDisplay());

        when(serverDescriptor.getVersionLabel()).thenReturn("d");
        response = get("discovery", null, 200);

        discoveryDetails = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), DiscoveryDetails.class);
        assertNotNull(discoveryDetails);
        repositoryInfo = discoveryDetails.getRepository();
        assertNotNull(repositoryInfo);
        // Check version info
        versionInfo = repositoryInfo.getVersion();
        assertNotNull(versionInfo);
        assertEquals("5", versionInfo.getMajor());
        assertEquals("2", versionInfo.getMinor());
        assertEquals("1", versionInfo.getPatch());
        assertEquals("0", versionInfo.getHotfix());
        assertEquals("r123456-b0", versionInfo.getLabel());
        assertEquals(10051, versionInfo.getSchema());
        assertEquals("5.2.1.0 (r123456-b0) schema 10051", versionInfo.getDisplay());

        when(serverDescriptor.getVersionLabel()).thenReturn("39.4");
        response = get("discovery", null, 200);

        discoveryDetails = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), DiscoveryDetails.class);
        assertNotNull(discoveryDetails);
        repositoryInfo = discoveryDetails.getRepository();
        assertNotNull(repositoryInfo);
        // Check version info
        versionInfo = repositoryInfo.getVersion();
        assertNotNull(versionInfo);
        assertEquals("5", versionInfo.getMajor());
        assertEquals("2", versionInfo.getMinor());
        assertEquals("1", versionInfo.getPatch());
        assertEquals("0", versionInfo.getHotfix());
        assertEquals("r123456-b0", versionInfo.getLabel());
        assertEquals(10051, versionInfo.getSchema());
        assertEquals("5.2.1.0 (r123456-b0) schema 10051", versionInfo.getDisplay());
    }

    /**
     * Tests get discovery.
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/discovery}
     */
    @Test
    public void testGetDiscovery_licenseEntitlement() throws Exception
    {
        // Override maxUsers
        when(licenseDescriptorMock.getMaxUsers()).thenReturn(null);

        setRequestContext(null, user1, null);
        HttpResponse response = get("discovery", null, 200);

        DiscoveryDetails discoveryDetails = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), DiscoveryDetails.class);
        assertNotNull(discoveryDetails);
        RepositoryInfo repositoryInfo = discoveryDetails.getRepository();
        assertNotNull(repositoryInfo);
        assertEquals("Enterprise", repositoryInfo.getEdition());

        // Check license info
        LicenseInfo licenseInfo = repositoryInfo.getLicense();
        assertNotNull(licenseInfo);
        assertEquals(LicenseMode.ENTERPRISE.name(), licenseInfo.getMode());
        assertEquals(licenseIssuedAt.toString(), licenseInfo.getIssuedAt().toString());
        assertEquals(licenseExpiresAt.toString(), licenseInfo.getExpiresAt().toString());
        assertEquals(Integer.valueOf(5), licenseInfo.getRemainingDays());
        assertEquals("Alfresco Dev Test", licenseInfo.getHolder());
        LicenseEntitlement entitlements = licenseInfo.getEntitlements();
        assertNotNull(entitlements);
        assertNull(entitlements.getMaxUsers());
        assertEquals(1000L, entitlements.getMaxDocs().longValue());
        assertTrue(entitlements.getIsClusterEnabled());
        assertFalse(entitlements.getIsCryptodocEnabled());

        // Override entitlements
        when(licenseDescriptorMock.getMaxDocs()).thenReturn(null);
        when(licenseDescriptorMock.isClusterEnabled()).thenReturn(false);
        when(licenseDescriptorMock.isCryptodocEnabled()).thenReturn(true);

        response = get("discovery", null, 200);

        discoveryDetails = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), DiscoveryDetails.class);
        assertNotNull(discoveryDetails);
        repositoryInfo = discoveryDetails.getRepository();
        assertNotNull(repositoryInfo);

        // Check license info
        licenseInfo = repositoryInfo.getLicense();
        assertNotNull(licenseInfo);
        entitlements = licenseInfo.getEntitlements();
        assertNotNull(entitlements);
        assertNull(entitlements.getMaxUsers());
        assertNull(entitlements.getMaxDocs());
        assertFalse(entitlements.getIsClusterEnabled());
        assertTrue(entitlements.getIsCryptodocEnabled());
    }

    @Test
    public void testDiscoveryDisabled() throws Exception
    {
        try
        {
            discoveryApiWebscript.setEnabled(false);

            setRequestContext(null, user1, null);
            get("discovery", null, 501);
        }
        finally
        {
            discoveryApiWebscript.setEnabled(true);
        }
    }
}

