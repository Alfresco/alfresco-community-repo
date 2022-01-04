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

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.test.util.CommonRMTestUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.GUID;

/**
 * Move record tests.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
@SuppressWarnings("unchecked")
public class MoveRecordTest extends BaseRMTestCase
{        
    private static final String OTHER_EVENT = "abolished";
    
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }
    
    /**
     * Given a record is filed in a event disposition and moved then the 
     * record no longer has any disposition.
     * 
     * @see https://issues.alfresco.com/jira/browse/RM-1540
     */
    public void testMoveRecordEventDispositinoToNoDisposition() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {   
            NodeRef sourceCategory;
            NodeRef sourceRecordFolder;
            NodeRef destinationCategory;
            NodeRef destinationRecordFolder;
            NodeRef record; 
            
            public void given()
            {   
                // create test data
                sourceCategory = filePlanService.createRecordCategory(filePlan, GUID.generate()); 
                utils.createBasicDispositionSchedule(sourceCategory, GUID.generate(), GUID.generate(), true, true);
                sourceRecordFolder = recordFolderService.createRecordFolder(sourceCategory, GUID.generate());
                destinationCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                destinationRecordFolder = recordFolderService.createRecordFolder(destinationCategory, GUID.generate());
     
                // create record
                record = utils.createRecord(sourceRecordFolder, GUID.generate());
                
                // check for the lifecycle aspect
                assertFalse(nodeService.hasAspect(sourceRecordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                
                // check the disposition action details
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionAction);
                assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                assertNull(dispositionAction.getAsOfDate());
                assertFalse(dispositionService.isNextDispositionActionEligible(record));
                assertNotNull(dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME));
            }
                        
            public void when() throws Exception
            {        
                // check the search aspect details
                assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                assertEquals(CutOffAction.NAME, nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                assertNull(nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_AS_OF));
                assertTrue(((List<String>)nodeService.getProperty(record, PROP_RS_DISPOSITION_EVENTS)).contains(CommonRMTestUtils.DEFAULT_EVENT_NAME));
                
                // move record
                fileFolderService.move(record, destinationRecordFolder, null);               
            }
            
            public void then()
            {
                // check for the lifecycle aspect
                assertFalse(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                
                // check the disposition action details
                assertNull(dispositionService.getNextDispositionAction(record));
   
                // check the search aspect properties
                assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));

            }            
        });                 
    }   
    
    /**
     * Given a record in a event disposition, when it moved to another event disposition then the record should have the
     * new events, rather than the old ones.
     */
    public void testMoveRecordEventDisToEventDis()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {   
            NodeRef sourceCategory;
            NodeRef sourceRecordFolder;
            NodeRef destinationCategory;
            NodeRef destinationRecordFolder;
            NodeRef record; 
            
            public void given()
            {   
                // create test data
                sourceCategory = filePlanService.createRecordCategory(filePlan, GUID.generate()); 
                utils.createBasicDispositionSchedule(sourceCategory, GUID.generate(), GUID.generate(), true, true);
                sourceRecordFolder = recordFolderService.createRecordFolder(sourceCategory, GUID.generate());
                
                destinationCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                utils.createDispositionSchedule(destinationCategory, GUID.generate(), GUID.generate(), true, true, false, OTHER_EVENT);
                destinationRecordFolder = recordFolderService.createRecordFolder(destinationCategory, GUID.generate());
     
                // create record
                record = utils.createRecord(sourceRecordFolder, GUID.generate());
                
                // check for the lifecycle aspect
                assertFalse(nodeService.hasAspect(sourceRecordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                
                // check the disposition action details
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionAction);
                assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                assertNull(dispositionAction.getAsOfDate());
                assertFalse(dispositionService.isNextDispositionActionEligible(record));
                assertNotNull(dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME));
                assertNull(dispositionAction.getEventCompletionDetails(OTHER_EVENT));
            }
            
            public void when() throws Exception
            {        
                // check the search aspect details
                assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                assertEquals(CutOffAction.NAME, nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                assertNull(nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_AS_OF));
                assertTrue(((List<String>)nodeService.getProperty(record, PROP_RS_DISPOSITION_EVENTS)).contains(CommonRMTestUtils.DEFAULT_EVENT_NAME));
                
                // move record
                fileFolderService.move(record, destinationRecordFolder, null);               
            }
            
            public void then()
            {
                // check for the lifecycle aspect
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                
                // check the disposition action details
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionAction);
                assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                assertNull(dispositionAction.getAsOfDate());
                assertFalse(dispositionService.isNextDispositionActionEligible(record));
                assertNull(dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME));
                assertNotNull(dispositionAction.getEventCompletionDetails(OTHER_EVENT)); 
   
                // check the search aspect details
                assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                assertEquals(CutOffAction.NAME, nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                assertNull(nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_AS_OF));
                assertTrue(((List<String>)nodeService.getProperty(record, PROP_RS_DISPOSITION_EVENTS)).contains(OTHER_EVENT));    
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_INSTRUCTIONS));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_AUTHORITY)); 
                assertTrue((Boolean)nodeService.getProperty(record, PROP_RS_HAS_DISPOITION_SCHEDULE));
            }            
        });
        
    }
    
    /**
     * Given a record in a event disposition, when it moved to a time disposition then the record should have the correct as of
     * date and no longer have the events.
     */
    public void testMoveRecordEventDisToTimeDis()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {   
            NodeRef sourceCategory;
            NodeRef sourceRecordFolder;
            NodeRef destinationCategory;
            NodeRef destinationRecordFolder;
            NodeRef record; 
            
            public void given()
            {   
                // create test data
                sourceCategory = filePlanService.createRecordCategory(filePlan, GUID.generate()); 
                utils.createBasicDispositionSchedule(sourceCategory, GUID.generate(), GUID.generate(), true, true);
                sourceRecordFolder = recordFolderService.createRecordFolder(sourceCategory, GUID.generate());
                
                destinationCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                DispositionSchedule dis = utils.createBasicDispositionSchedule(destinationCategory, GUID.generate(), GUID.generate(), true, false);
                Map<QName, Serializable> adParams = new HashMap<>(3);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                adParams.put(PROP_DISPOSITION_DESCRIPTION, GUID.generate());
                adParams.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
                dispositionService.addDispositionActionDefinition(dis, adParams);
                destinationRecordFolder = recordFolderService.createRecordFolder(destinationCategory, GUID.generate());
     
                // create record
                record = utils.createRecord(sourceRecordFolder, GUID.generate());
                
                // check for the lifecycle aspect
                assertFalse(nodeService.hasAspect(sourceRecordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                
                // check the disposition action details
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionAction);
                assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                assertNull(dispositionAction.getAsOfDate());
                assertFalse(dispositionService.isNextDispositionActionEligible(record));
                assertNotNull(dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME));
                assertNull(dispositionAction.getEventCompletionDetails(OTHER_EVENT));
            }
            
            public void when() throws Exception
            {        
                // check the search aspect details
                assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                assertEquals(CutOffAction.NAME, nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                assertNull(nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_AS_OF));
                assertTrue(((List<String>)nodeService.getProperty(record, PROP_RS_DISPOSITION_EVENTS)).contains(CommonRMTestUtils.DEFAULT_EVENT_NAME));
                
                // move record
                fileFolderService.move(record, destinationRecordFolder, null);               
            }
            
            public void then()
            {
                // check for the lifecycle aspect
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                
                // check the disposition action details
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(record);
                assertNotNull(dispositionAction);
                assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                assertNotNull(dispositionAction.getAsOfDate());
                assertTrue(dispositionService.isNextDispositionActionEligible(record));  
                assertNull(dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME));
                
                // check the search aspect details
                assertTrue(nodeService.hasAspect(record, ASPECT_RM_SEARCH));
                assertEquals(CutOffAction.NAME, nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_NAME));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOSITION_ACTION_AS_OF));
                assertNull(((List<String>)nodeService.getProperty(record, PROP_RS_DISPOSITION_EVENTS)));     
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_INSTRUCTIONS));
                assertNotNull(nodeService.getProperty(record, PROP_RS_DISPOITION_AUTHORITY));  
                assertTrue((Boolean)nodeService.getProperty(record, PROP_RS_HAS_DISPOITION_SCHEDULE));               
            } 
        });        
    }
    
    /**
     * See https://issues.alfresco.com/jira/browse/RM-1502
     */
    public void testMoveDMtoRM() 
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {   
            NodeRef destinationCategory;
            NodeRef destinationRecordFolder;
            
            public void given()
            {   
                // destination category
                destinationCategory = filePlanService.createRecordCategory(filePlan, GUID.generate());
                DispositionSchedule dis = utils.createBasicDispositionSchedule(destinationCategory, GUID.generate(), GUID.generate(), true, false);
                Map<QName, Serializable> adParams = new HashMap<>(3);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
                adParams.put(PROP_DISPOSITION_DESCRIPTION, GUID.generate());
                adParams.put(PROP_DISPOSITION_PERIOD, CommonRMTestUtils.PERIOD_IMMEDIATELY);
                dispositionService.addDispositionActionDefinition(dis, adParams);
                destinationRecordFolder = recordFolderService.createRecordFolder(destinationCategory, GUID.generate());
            }
            
            public void when() throws Exception
            {        
                // move document to record folder
                fileFolderService.move(dmDocument, destinationRecordFolder, null);    
            }
            
            public void then()
            {
                // check for the lifecycle aspect
                assertTrue(nodeService.hasAspect(dmDocument, ASPECT_DISPOSITION_LIFECYCLE));
                
                // check the disposition action details
                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(dmDocument);
                assertNotNull(dispositionAction);
                assertNotNull(CutOffAction.NAME, dispositionAction.getName());
                assertNotNull(dispositionAction.getAsOfDate());
                assertTrue(dispositionService.isNextDispositionActionEligible(dmDocument));  
                assertNull(dispositionAction.getEventCompletionDetails(CommonRMTestUtils.DEFAULT_EVENT_NAME));
                
                // check the search aspect details
                assertTrue(nodeService.hasAspect(dmDocument, ASPECT_RM_SEARCH));
                assertEquals(CutOffAction.NAME, nodeService.getProperty(dmDocument, PROP_RS_DISPOSITION_ACTION_NAME));
                assertNotNull(nodeService.getProperty(dmDocument, PROP_RS_DISPOSITION_ACTION_AS_OF));
                assertNull(((List<String>)nodeService.getProperty(dmDocument, PROP_RS_DISPOSITION_EVENTS)));     
                assertNotNull(nodeService.getProperty(dmDocument, PROP_RS_DISPOITION_INSTRUCTIONS));
                assertNotNull(nodeService.getProperty(dmDocument, PROP_RS_DISPOITION_AUTHORITY));
                assertTrue((Boolean)nodeService.getProperty(dmDocument, PROP_RS_HAS_DISPOITION_SCHEDULE));
            }            
        });                
    }
    
    // TODO moveRecordNoDisToEventDis    
    // TODO moveRecordRecordDisToFolderDis
}
