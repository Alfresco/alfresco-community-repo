/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.extensions.webscripts.GUID;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkCancellationRequest;
import org.alfresco.module.org_alfresco_module_rm.bulk.BulkOperation;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkMonitor;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkStatus;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkStatus.Status;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.rest.api.search.model.Query;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Hold bulk service integration test.
 */
@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.JUnit4TestShouldUseTestAnnotation"})
public class HoldBulkServiceTest extends BaseRMTestCase
{
    private static final int RECORD_COUNT = 10;
    private final SearchService searchServiceMock = mock(SearchService.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private HoldBulkServiceImpl holdBulkService;
    private HoldBulkMonitor holdBulkMonitor;

    @Override
    protected void initServices()
    {
        super.initServices();
        holdBulkMonitor = (HoldBulkMonitor) applicationContext.getBean("holdBulkMonitor");
        holdBulkService = (HoldBulkServiceImpl) applicationContext.getBean("holdBulkService");
        holdBulkService.setSearchService(searchServiceMock);
        Mockito.when(searchServiceMock.query(any(SearchParameters.class))).thenReturn(resultSet);
    }

    public void testCancelBulkOperation()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest() {
            private NodeRef hold;
            private HoldBulkStatus holdBulkStatus;
            private final ResultSet resultSet = mock(ResultSet.class);

            public void given()
            {
                Mockito.when(resultSet.getNumberFound()).thenReturn(4L);
                Mockito.when(resultSet.hasMore()).thenReturn(false).thenReturn(true).thenReturn(false);
                Mockito.when(resultSet.getNodeRefs())
                        .thenAnswer((Answer<List<NodeRef>>) invocationOnMock -> {
                            await().pollDelay(1, SECONDS).until(() -> true);
                            return List.of(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate()),
                                    new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate()));
                        });
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());
            }

            public void when()
            {
                BulkOperation bulkOperation = new BulkOperation(new Query("afts", "*", ""), "ADD");
                // execute the bulk operation
                holdBulkStatus = holdBulkService.execute(hold, bulkOperation);
                // cancel the bulk operation
                holdBulkMonitor.cancelBulkOperation(holdBulkStatus.bulkStatusId(),
                        new BulkCancellationRequest("No reason"));
                await().atMost(10, SECONDS)
                        .until(() -> Objects.equals(
                                holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId()).getStatus(),
                                Status.CANCELLED.getValue()));
            }

            public void then()
            {
                holdBulkStatus = holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId());
                assertNotNull(holdBulkStatus.startTime());
                assertNotNull(holdBulkStatus.endTime());
                assertEquals(holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId()).getStatus(),
                        HoldBulkStatus.Status.CANCELLED.getValue());
                assertEquals(holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId()).cancellationReason(),
                        "No reason");
            }
        });
    }

    public void testAddRecordsToHoldViaBulk()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest() {
            private NodeRef hold;
            private NodeRef recordFolder;
            private HoldBulkStatus holdBulkStatus;
            private final List<NodeRef> records = new ArrayList<>(RECORD_COUNT);

            public void given()
            {
                Mockito.when(resultSet.getNumberFound()).thenReturn(Long.valueOf(RECORD_COUNT));
                Mockito.when(resultSet.hasMore()).thenReturn(false).thenReturn(false);
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // create a record folder that contains records
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                for (int i = 0; i < RECORD_COUNT; i++)
                {
                    records.add(
                            recordService.createRecordFromContent(recordFolder, GUID.generate(), ContentModel.TYPE_CONTENT,
                                    null, null));
                }
                Mockito.when(resultSet.getNodeRefs()).thenReturn(records).thenReturn(records)
                        .thenReturn(Collections.emptyList());

                // assert current states
                assertFalse(freezeService.isFrozen(recordFolder));
                assertFalse(freezeService.hasFrozenChildren(recordFolder));
                for (NodeRef record : records)
                {
                    assertFalse(freezeService.isFrozen(record));
                }

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(0, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }

            public void when()
            {
                BulkOperation bulkOperation = new BulkOperation(new Query("afts", "*", ""), "ADD");
                // execute the bulk operation
                holdBulkStatus = holdBulkService.execute(hold, bulkOperation);
                await().atMost(10, SECONDS)
                        .until(() -> Objects.equals(
                                holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId()).getStatus(),
                                Status.DONE.getValue()));
            }

            public void then()
            {
                holdBulkStatus = holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId());
                assertNotNull(holdBulkStatus.startTime());
                assertNotNull(holdBulkStatus.endTime());
                assertEquals(RECORD_COUNT, holdBulkStatus.totalItems());
                assertEquals(RECORD_COUNT, holdBulkStatus.processedItems());
                assertEquals(0, holdBulkStatus.errorsCount());
                assertEquals(holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId()).getStatus(),
                        HoldBulkStatus.Status.DONE.getValue());

                // record is held
                for (NodeRef record : records)
                {
                    assertTrue(freezeService.isFrozen(record));
                }

                // record folder has frozen children
                assertFalse(freezeService.isFrozen(recordFolder));
                assertTrue(freezeService.hasFrozenChildren(recordFolder));

                // record folder is not held
                assertFalse(holdService.getHeld(hold).contains(recordFolder));
                assertFalse(holdService.heldBy(recordFolder, true).contains(hold));

                for (NodeRef record : records)
                {
                    // hold contains record
                    assertTrue(holdService.getHeld(hold).contains(record));
                    assertTrue(holdService.heldBy(record, true).contains(hold));
                }

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(RECORD_COUNT, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });
    }

    public void testAddRecordFolderToHoldViaBulk()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest() {
            private NodeRef hold;
            private NodeRef recordFolder;
            private final List<NodeRef> records = new ArrayList<>(RECORD_COUNT);
            private HoldBulkStatus holdBulkStatus;

            public void given()
            {
                Mockito.when(resultSet.getNumberFound()).thenReturn(1L);
                Mockito.when(resultSet.hasMore()).thenReturn(false).thenReturn(false);
                // create a hold
                hold = holdService.createHold(filePlan, GUID.generate(), GUID.generate(), GUID.generate());

                // create a record folder that contains records
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                for (int i = 0; i < RECORD_COUNT; i++)
                {
                    records.add(
                            recordService.createRecordFromContent(recordFolder, GUID.generate(), ContentModel.TYPE_CONTENT,
                                    null, null));
                }
                Mockito.when(resultSet.getNodeRefs()).thenReturn(Collections.singletonList(recordFolder))
                        .thenReturn(Collections.singletonList(recordFolder)).thenReturn(Collections.emptyList());

                // assert current states
                assertFalse(freezeService.isFrozen(recordFolder));
                assertFalse(freezeService.hasFrozenChildren(recordFolder));
                for (NodeRef record : records)
                {
                    assertFalse(freezeService.isFrozen(record));
                }

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(0, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }

            public void when()
            {
                BulkOperation bulkOperation = new BulkOperation(new Query("afts", "*", ""), "ADD");
                // execute the bulk operation
                holdBulkStatus = holdBulkService.execute(hold, bulkOperation);
                await().atMost(10, SECONDS)
                        .until(() -> Objects.equals(
                                holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId()).getStatus(),
                                Status.DONE.getValue()));
            }

            public void then()
            {
                holdBulkStatus = holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId());
                assertNotNull(holdBulkStatus.startTime());
                assertNotNull(holdBulkStatus.endTime());
                assertEquals(1, holdBulkStatus.totalItems());
                assertEquals(1, holdBulkStatus.processedItems());
                assertEquals(0, holdBulkStatus.errorsCount());
                assertEquals(holdBulkMonitor.getBulkStatus(holdBulkStatus.bulkStatusId()).getStatus(),
                        HoldBulkStatus.Status.DONE.getValue());

                for (NodeRef record : records)
                {
                    // record is held
                    assertTrue(freezeService.isFrozen(record));
                    assertFalse(holdService.getHeld(hold).contains(record));
                    assertTrue(holdService.heldBy(record, true).contains(hold));
                }

                // record folder has frozen children
                assertTrue(freezeService.isFrozen(recordFolder));
                assertTrue(freezeService.hasFrozenChildren(recordFolder));

                // hold contains record folder
                assertTrue(holdService.getHeld(hold).contains(recordFolder));
                assertTrue(holdService.heldBy(recordFolder, true).contains(hold));

                // additional check for child held caching
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_HELD_CHILDREN));
                assertEquals(RECORD_COUNT, nodeService.getProperty(recordFolder, PROP_HELD_CHILDREN_COUNT));
            }
        });

    }
}
