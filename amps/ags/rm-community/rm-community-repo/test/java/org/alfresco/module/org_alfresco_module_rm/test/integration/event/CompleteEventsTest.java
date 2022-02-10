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

package org.alfresco.module.org_alfresco_module_rm.test.integration.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.GUID;

/**
 * Complete events integration tests.
 * <p>
 * Relates to:
 *  - https://issues.alfresco.com/jira/browse/RM-1341
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public class CompleteEventsTest extends BaseRMTestCase
{
    private static final String ANOTHER_EVENT = "abolished";
    
    /**
     * test completion of a single event on a record level disposition schedule
     */
    public void testCompleteSingleEventRecordLevel()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            
            public void given()
            {
                // create record category
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                
                // create disposition schedule
                utils.createBasicDispositionSchedule(recordCategory, "instructions", "authority", true, true);
                
                // create record folder
                NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                
                // file record
                record = utils.createRecord(recordFolder, GUID.generate(), "title");
                utils.completeRecord(record);
            }
            
            public void when()
            {
                // build action properties
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                
                // complete event
                rmActionService.executeRecordsManagementAction(record, CompleteEventAction.NAME, params);
                
            }
            
            public void then()
            {
                // check that the record is now eligible for the next disposition action
                assertTrue(dispositionService.isNextDispositionActionEligible(record));
                
                // check the next disposition action
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionAction); 
                assertEquals("cutoff", dispositionAction.getName());
                
                EventCompletionDetails eventDetails = dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                assertNotNull(eventDetails);
                assertEquals(CommonRMTestUtils.DEFAULT_EVENT_NAME, eventDetails.getEventName());
                assertTrue(eventDetails.isEventComplete());
                assertNotNull(eventDetails.getEventCompletedAt());
                assertNotNull(eventDetails.getEventCompletedBy());
            }
        });
    }
    
    /**
     * test completion of a single event at the record level
     */
    public void testCompleteSimpleSingleEventRecordFolderLevel()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef recordFolder;
            
            public void given()
            {
                // create record category
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                
                // create disposition schedule
                utils.createBasicDispositionSchedule(recordCategory, "instructions", "authority", false, true);
                
                // create record folder
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                
                // file record
                NodeRef record = utils.createRecord(recordFolder, GUID.generate(), "title");
                utils.completeRecord(record);

            }
            
            public void when()
            {
                // build action properties
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                
                // complete event
                rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);                
            }
            
            public void then()
            {
                // check that the record is now eligible for the next disposition action
                assertTrue(dispositionService.isNextDispositionActionEligible(recordFolder));
                
                // check the next disposition action
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(dispositionAction); 
                assertEquals("cutoff", dispositionAction.getName());
                
                EventCompletionDetails eventDetails = dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                assertNotNull(eventDetails);
                assertEquals(CommonRMTestUtils.DEFAULT_EVENT_NAME, eventDetails.getEventName());
                assertTrue(eventDetails.isEventComplete());
                assertNotNull(eventDetails.getEventCompletedAt());
                assertNotNull(eventDetails.getEventCompletedBy());
            }
        });       
    }
    
    /**
     * test complete event given at least one event is needed for the disposition action to be eligible
     */
    public void testAtLeastOneEventToBeEligible()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef recordFolder = null;
            
            public void given()
            {
                // create record category
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                
                // create disposition schedule
                DispositionSchedule mySchedule = utils.createBasicDispositionSchedule(recordCategory, "instructions", "authority", false, false);
                
                Map<QName, Serializable> adParams = new HashMap<>(3);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                adParams.put(PROP_DISPOSITION_DESCRIPTION, CommonRMTestUtils.DEFAULT_DISPOSITION_DESCRIPTION);

                List<String> events = new ArrayList<>(1);
                events.add(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                events.add(ANOTHER_EVENT);
                adParams.put(PROP_DISPOSITION_EVENT, (Serializable)events);
                dispositionService.addDispositionActionDefinition(mySchedule, adParams);
                
                // create record folder
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                
                // file record
                NodeRef record = utils.createRecord(recordFolder, GUID.generate(), "title");
                utils.completeRecord(record);

            }
            public void when()
            {
                // build action properties
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                
                // complete event
                rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);                   
            }
            
            public void then()
            {                
                // check that the record is now eligible for the next disposition action
                assertTrue(dispositionService.isNextDispositionActionEligible(recordFolder));
                
                // check the next disposition action
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(dispositionAction); 
                assertEquals("cutoff", dispositionAction.getName());
                
                EventCompletionDetails eventDetails = dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                assertNotNull(eventDetails);
                assertEquals(CommonRMTestUtils.DEFAULT_EVENT_NAME, eventDetails.getEventName());
                assertTrue(eventDetails.isEventComplete());
                assertNotNull(eventDetails.getEventCompletedAt());
                assertNotNull(eventDetails.getEventCompletedBy());
                
                eventDetails = dispositionAction.getEventCompletionDetails(ANOTHER_EVENT);
                assertNotNull(eventDetails);
                assertEquals(ANOTHER_EVENT, eventDetails.getEventName());
                assertFalse(eventDetails.isEventComplete());
                assertNull(eventDetails.getEventCompletedAt());
                assertNull(eventDetails.getEventCompletedBy());
            }
        }); 
    }
    
    /**
     * test that disposition action is not eligible given all events need to be completed and only has been
     */
    public void testOnlyOneOfAllEventsSoNotEligible()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef recordFolder = null;
            
            public void given()
            {
                // create record category
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                
                // create disposition schedule
                DispositionSchedule mySchedule = utils.createBasicDispositionSchedule(recordCategory, "instructions", "authority", false, false);
                
                Map<QName, Serializable> adParams = new HashMap<>(3);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                adParams.put(PROP_DISPOSITION_DESCRIPTION, CommonRMTestUtils.DEFAULT_DISPOSITION_DESCRIPTION);
                adParams.put(PROP_DISPOSITION_EVENT_COMBINATION, "and");

                List<String> events = new ArrayList<>(1);
                events.add(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                events.add(ANOTHER_EVENT);
                adParams.put(PROP_DISPOSITION_EVENT, (Serializable)events);
                dispositionService.addDispositionActionDefinition(mySchedule, adParams);
                
                // create record folder
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                
                // file record
                NodeRef record = utils.createRecord(recordFolder, GUID.generate(), "title");
                utils.completeRecord(record);
            }
            
            public void when()
            {
                // build action properties
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                
                // complete event
                rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);                   
            }
            
            public void then()
            {           
                assertFalse(dispositionService.isNextDispositionActionEligible(recordFolder));
                
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(dispositionAction); 
                assertEquals("cutoff", dispositionAction.getName());
                
                EventCompletionDetails eventDetails = dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                assertNotNull(eventDetails);
                assertEquals(CommonRMTestUtils.DEFAULT_EVENT_NAME, eventDetails.getEventName());
                assertTrue(eventDetails.isEventComplete());
                assertNotNull(eventDetails.getEventCompletedAt());
                assertNotNull(eventDetails.getEventCompletedBy());
                
                eventDetails = dispositionAction.getEventCompletionDetails(ANOTHER_EVENT);
                assertNotNull(eventDetails);
                assertEquals(ANOTHER_EVENT, eventDetails.getEventName());
                assertFalse(eventDetails.isEventComplete());
                assertNull(eventDetails.getEventCompletedAt());
                assertNull(eventDetails.getEventCompletedBy());
            }
        }); 
    }
    
    /**
     * test event complete makes disposition eligible given that all events are complete and required
     */
    public void testAllEventsSoEligible()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            NodeRef recordFolder = null;
            
            public void given()
            {
                // create record category
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                
                // create disposition schedule
                DispositionSchedule mySchedule = utils.createBasicDispositionSchedule(recordCategory, "instructions", "authority", false, false);
                
                Map<QName, Serializable> adParams = new HashMap<>(3);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                adParams.put(PROP_DISPOSITION_DESCRIPTION, CommonRMTestUtils.DEFAULT_DISPOSITION_DESCRIPTION);
                adParams.put(PROP_DISPOSITION_EVENT_COMBINATION, "and");

                List<String> events = new ArrayList<>(1);
                events.add(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                events.add(ANOTHER_EVENT);
                adParams.put(PROP_DISPOSITION_EVENT, (Serializable)events);
                dispositionService.addDispositionActionDefinition(mySchedule, adParams);
                
                // create record folder
                recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                
                // file record
                NodeRef record = utils.createRecord(recordFolder, GUID.generate(), "title");
                utils.completeRecord(record);
            }
            
            public void when()
            { 
                // build action properties
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                
                // complete event
                rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);  
                
                // build action properties
                params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, ANOTHER_EVENT);
                
                // complete event
                rmActionService.executeRecordsManagementAction(recordFolder, CompleteEventAction.NAME, params);                 
            }
            
            public void then()
            {
                assertTrue(dispositionService.isNextDispositionActionEligible(recordFolder));
                
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(dispositionAction); 
                assertEquals("cutoff", dispositionAction.getName());
                
                EventCompletionDetails eventDetails = dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                assertNotNull(eventDetails);
                assertEquals(CommonRMTestUtils.DEFAULT_EVENT_NAME, eventDetails.getEventName());
                assertTrue(eventDetails.isEventComplete());
                assertNotNull(eventDetails.getEventCompletedAt());
                assertNotNull(eventDetails.getEventCompletedBy());
                
                eventDetails = dispositionAction.getEventCompletionDetails(ANOTHER_EVENT);
                assertNotNull(eventDetails);
                assertEquals(ANOTHER_EVENT, eventDetails.getEventName());
                assertTrue(eventDetails.isEventComplete());
                assertNotNull(eventDetails.getEventCompletedAt());
                assertNotNull(eventDetails.getEventCompletedBy());
 
            }
        }); 
    }
    
    /**
     * test complete event works for multi-filed record
     */
    public void testCompleteEventWhenCutoffMultiFiled_RM1341()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
        
            public void given()
            {
                // create record category
                NodeRef recordCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                NodeRef recordCategory2 = filePlanService.createRecordCategory(filePlan, GUID.generate());
                
                // create disposition schedule
                utils.createBasicDispositionSchedule(recordCategory, "instructions", "authority", true, true);
                
                // create record folder
                NodeRef recordFolder = recordFolderService.createRecordFolder(recordCategory, GUID.generate());
                NodeRef recordFolder2 = recordFolderService.createRecordFolder(recordCategory2, GUID.generate());
                
                // file record
                String recordName = GUID.generate();
                record = utils.createRecord(recordFolder, recordName, "title");
                utils.completeRecord(record);
                
                // link record to second record folder
                nodeService.addChild(recordFolder2, record, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, recordName));
            }
            
            public void when()
            {
                // build action properties
                Map<String, Serializable> params = new HashMap<>(1);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, CommonRMTestUtils.DEFAULT_EVENT_NAME);
                
                // complete event
                rmActionService.executeRecordsManagementAction(record, CompleteEventAction.NAME, params);                
            }
            
            public void then()
            {
                // check that the record is now eligible for the next disposition action
                assertTrue(dispositionService.isNextDispositionActionEligible(record));
                
                // check the next disposition action
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionAction); 
                assertEquals("cutoff", dispositionAction.getName());
                
                EventCompletionDetails eventDetails = dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME);
                assertNotNull(eventDetails);
                assertEquals(CommonRMTestUtils.DEFAULT_EVENT_NAME, eventDetails.getEventName());
                assertTrue(eventDetails.isEventComplete());
                assertNotNull(eventDetails.getEventCompletedAt());
                assertNotNull(eventDetails.getEventCompletedBy());
            }
        }); 
    }
}
