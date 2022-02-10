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
package org.alfresco.module.org_alfresco_module_rm.audit;

import static java.util.Collections.emptyList;

import static org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService.ReportFormat.JSON;
import static org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditServiceImpl.DOD5015_AUDIT_APPLICATION_NAME;
import static org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditServiceImpl.RM_AUDIT_APPLICATION_NAME;
import static org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditServiceImpl.RM_AUDIT_PATH_ROOT;
import static org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model.TYPE_DOD_5015_SITE;
import static org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService.DEFAULT_RM_SITE_ID;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.TYPE_RM_SITE;
import static org.alfresco.module.org_alfresco_module_rm.model.rma.type.RmSiteType.DEFAULT_SITE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for {@link RecordsManagementAuditServiceImpl}.
 *
 * @author Tom Page
 * @since 2.7
 */
public class RecordsManagementAuditServiceImplUnitTest
{
    /** The maximum entries to return in the audit query. */
    private static final int MAX_ENTRIES = 10;
    /** A node representing the file plan root. */
    private static final NodeRef FILE_PLAN_NODE = new NodeRef("file://plan/node");
    /** A node representing the RM site. */
    private static final NodeRef RM_SITE_NODE = new NodeRef("rm://site/node");
    /** The class under test. */
    @InjectMocks
    private RecordsManagementAuditServiceImpl recordsManagementAuditServiceImpl;
    @Mock
    private NodeService mockNodeService;
    @Mock
    private SiteService mockSiteService;
    @Mock
    private AuditService mockAuditService;
    @Mock
    private FilePlanService mockFilePlanService;
    @Mock
    AuditComponent mockAuditComponent;
    @Mock
    private Writer mockWriter;
    @Mock
    private SiteInfo mockSiteInfo;
    @Captor
    private ArgumentCaptor<AuditQueryParameters> queryParamsCaptor;

    /** Set up the mocks. */
    @Before
    public void setUp()
    {
        initMocks(this);

        when(mockFilePlanService.getFilePlanBySiteId(DEFAULT_RM_SITE_ID)).thenReturn(FILE_PLAN_NODE);
        when(mockSiteService.getSite(DEFAULT_SITE_NAME)).thenReturn(mockSiteInfo);
        when(mockSiteInfo.getNodeRef()).thenReturn(RM_SITE_NODE);

        recordsManagementAuditServiceImpl.setIgnoredAuditProperties(emptyList());
    }

    /**
     * Check that if the RM site is not a DOD site then the audit trail doesn't make a query for DOD events.
     *
     * @throws IOException Unexpected.
     */
    @Test
    public void testAuditWithoutDOD() throws IOException
    {
        RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
        params.setMaxEntries(MAX_ENTRIES);
        List<RecordsManagementAuditEntry> results = new ArrayList<>();
        // Return a standard site type.
        when(mockNodeService.getType(RM_SITE_NODE)).thenReturn(TYPE_RM_SITE);

        // Call the method under test.
        recordsManagementAuditServiceImpl.getAuditTrailImpl(params, results, mockWriter, JSON);

        // Check that exactly one audit query was performed.
        verify(mockAuditService, times(1))
                    .auditQuery(any(AuditService.AuditQueryCallback.class), queryParamsCaptor.capture(),
                                eq(MAX_ENTRIES));
        // We always need to make the standard query - regardless of the type of RM site (to get events like RM site created).
        assertEquals("The application name should be the standard RM application", RM_AUDIT_APPLICATION_NAME,
                    queryParamsCaptor.getValue().getApplicationName());
        // Check that the event of viewing the audit log was itself audited.
        verify(mockAuditComponent).recordAuditValues(eq(RM_AUDIT_PATH_ROOT), any(Map.class));
    }

    /**
     * Check that if the RM site is a DOD site then the audit trail makes a query for DOD events and the standard events.
     *
     * @throws IOException Unexpected.
     */
    @Test
    public void testAuditWithDOD() throws IOException
    {
        RecordsManagementAuditQueryParameters params = new RecordsManagementAuditQueryParameters();
        params.setMaxEntries(MAX_ENTRIES);
        List<RecordsManagementAuditEntry> results = new ArrayList<>();
        // Return a DOD site type.
        when(mockNodeService.getType(RM_SITE_NODE)).thenReturn(TYPE_DOD_5015_SITE);

        // Call the method under test.
        recordsManagementAuditServiceImpl.getAuditTrailImpl(params, results, mockWriter, JSON);

        // Check that two audit queries were performed (one for DOD events and one for standard events).
        verify(mockAuditService, times(2))
                    .auditQuery(any(AuditService.AuditQueryCallback.class), queryParamsCaptor.capture(),
                                eq(MAX_ENTRIES));
        Set<String> apps = queryParamsCaptor.getAllValues().stream().map(AuditQueryParameters::getApplicationName)
                    .collect(Collectors.toSet());
        // We always need to make the standard query - regardless of the type of RM site (to get events like RM site created).
        assertEquals("Expected the standard audit query and the DOD audit query.",
                    Sets.newHashSet(RM_AUDIT_APPLICATION_NAME, DOD5015_AUDIT_APPLICATION_NAME), apps);
        // Check that the event of viewing the audit log was itself audited.
        verify(mockAuditComponent).recordAuditValues(eq(RM_AUDIT_PATH_ROOT), any(Map.class));
    }

    /** Check that passing null to getStartOfDay doesn't result in null being returned. */
    @Test
    public void testGetStartOfDay_null()
    {
        Date startOfDay = recordsManagementAuditServiceImpl.getStartOfDay(null);
        assertNotNull("Expected date to be created by method.", startOfDay);
    }

    /** Check that any time component passed to getStartOfDay is not included in the response. */
    @Test
    public void testGetStartOfDay_timeDiscarded() throws Exception
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        Date date = format.parse("2001-02-03 04:05:06.789");

        // Call the method under test.
        Date startOfDay = recordsManagementAuditServiceImpl.getStartOfDay(date);

        assertEquals("Unexpected date truncation.", format.parse("2001-02-03 00:00:00.000"), startOfDay);
    }
}
