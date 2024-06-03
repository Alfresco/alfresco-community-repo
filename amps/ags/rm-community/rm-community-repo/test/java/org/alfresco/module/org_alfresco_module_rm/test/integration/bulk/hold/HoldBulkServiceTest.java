/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.test.integration.bulk.hold;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.bulk.BulkOperation;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.rm.rest.api.model.HoldBulkStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.webscripts.GUID;

public class HoldBulkServiceTest extends BaseRMTestCase
{
    public void testAddRecordToHold()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef hold;
            private HoldBulkServiceImpl holdBulkServiceImpl;
            private HoldBulkStatus holdBulkStatus;
            private SearchService searchServiceMock = mock(SearchService.class);
            private ResultSet resultSet = mock(ResultSet.class);

            public void given()
            {
                Mockito.when(resultSet.getNumberFound()).thenReturn(4L);
                Mockito.when(resultSet.hasMore()).thenReturn(false).thenReturn(true).thenReturn(false);
                Mockito.when(resultSet.getNodeRefs())
                    .thenAnswer((Answer<List<NodeRef>>) invocationOnMock -> {
                        Thread.sleep(1000);
                        return List.of(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate()),
                            new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate()));
                    });
                Mockito.when(searchServiceMock.query(any(SearchParameters.class))).thenReturn(resultSet);
                holdBulkServiceImpl = (HoldBulkServiceImpl) holdBulkService;
                holdBulkServiceImpl.setSearchService(searchServiceMock);
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
            }

            public void when()
            {
                BulkOperation bulkOperation = new BulkOperation(new Query("afts", "*", ""), "ADD");
                holdBulkStatus = holdBulkServiceImpl.execute(hold, bulkOperation);
                holdBulkMonitor.cancelBulkOperation(holdBulkStatus.bulkStatusId(), "No reason");
            }

            public void then()
            {
                assertEquals(holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId()).getStatus(),
                    HoldBulkStatus.Status.CANCELLED.getValue());
                assertEquals(holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId()).cancellationReason(),
                    "No reason");
            }
        });
    }
}
