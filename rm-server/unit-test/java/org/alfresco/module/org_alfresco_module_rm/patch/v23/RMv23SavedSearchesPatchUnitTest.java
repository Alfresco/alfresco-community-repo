/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.patch.v23;


import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.search.SavedSearchDetails;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.repo.version.NodeServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.SAVED_SEARCH_ASPECT;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * patch.v23 unit test
 *
 * @author Ross Gale
 * @since 2.3
 */
public class RMv23SavedSearchesPatchUnitTest// extends BaseUnitTest
{

    @Mock
    private NodeService nodeService;

    @Mock
    private RecordsManagementSearchServiceImpl recordsManagementSearchService;

    @Mock
    private SavedSearchDetails mockSavedSearchDetails1, mockSavedSearchDetails2;

    @InjectMocks
    private RMv23SavedSearchesPatch patch;

    /**
     * Given that I am upgrading an existing repository to v2.2
     * When I execute the patch
     * Then the capabilities are updated
     */
    @Test
    public void executePatch()
    {
        MockitoAnnotations.initMocks(this);
        NodeRef noderef1 = new NodeRef("foo://123/456");
        NodeRef noderef2 = new NodeRef("bar://123/456");
        List<SavedSearchDetails> searches = new ArrayList<>();
        searches.add(mockSavedSearchDetails1);
        searches.add(mockSavedSearchDetails2);
        when(mockSavedSearchDetails1.getNodeRef()).thenReturn(noderef1);
        when(mockSavedSearchDetails2.getNodeRef()).thenReturn(noderef2);
        when(recordsManagementSearchService.getSavedSearches("rm")).thenReturn(searches);

        // execute patch
        patch.applyInternal();

        verify(nodeService, times(1)).addAspect(noderef1,SAVED_SEARCH_ASPECT,null);
        verify(nodeService, times(1)).addAspect(noderef2, SAVED_SEARCH_ASPECT, null);
    }

}

