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

package org.alfresco.module.org_alfresco_module_rm.patch.v23;

import static java.util.Arrays.asList;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_SAVED_SEARCH;
import static org.alfresco.module.org_alfresco_module_rm.model.rma.type.RmSiteType.DEFAULT_SITE_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * patch.v23 unit test
 *
 * @author Ross Gale
 * @since 2.3
 */
public class RMv23SavedSearchesPatchUnitTest
{

    @Mock
    private NodeService nodeService;

    @Mock
    private SiteService siteService;

    @Mock
    private SiteInfo siteInfo;

    @Mock
    private RecordsManagementSearchServiceImpl recordsManagementSearchService;

    @Mock
    private SavedSearchDetails mockSavedSearchDetails1, mockSavedSearchDetails2;

    @InjectMocks
    private RMv23SavedSearchesPatch patch;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Given that I am upgrading an existing repository to v2.3
     * When I execute the patch
     * Then any existing rm saved searches will have the saved search aspect applied
     */
    @Test
    public void executePatch()
    {
        NodeRef noderef1 = new NodeRef("foo://123/456");
        NodeRef noderef2 = new NodeRef("bar://123/456");
        List<SavedSearchDetails> searches = asList(mockSavedSearchDetails1, mockSavedSearchDetails2);
        when(mockSavedSearchDetails1.getNodeRef()).thenReturn(noderef1);
        when(mockSavedSearchDetails2.getNodeRef()).thenReturn(noderef2);
        when(recordsManagementSearchService.getSavedSearches("rm")).thenReturn(searches);
        when(siteService.getSite(DEFAULT_SITE_NAME)).thenReturn(siteInfo);

        // execute patch
        patch.applyInternal();

        verify(nodeService, times(1)).addAspect(noderef1, ASPECT_SAVED_SEARCH, null);
        verify(nodeService, times(1)).addAspect(noderef2, ASPECT_SAVED_SEARCH, null);
    }

    /**
     * Test patch code doesnt run with an rm site
     */
    @Test
    public void testPatchDoesntRunWithoutRmSite()
    {
        when(siteService.getSite(DEFAULT_SITE_NAME)).thenReturn(null);

        // execute patch
        patch.applyInternal();

        verify(nodeService, times(0)).addAspect(any(NodeRef.class), any(QName.class), anyMap());
    }

}
