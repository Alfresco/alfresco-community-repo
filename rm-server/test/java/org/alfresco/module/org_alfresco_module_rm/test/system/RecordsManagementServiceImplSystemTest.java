/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FileAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestUtilities;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;

/**
 * System test for records management service.  
 * 
 * Awaiting refactoring into records management test.
 * 
 * @author Roy Wetherall
 */
public class RecordsManagementServiceImplSystemTest extends BaseSpringTest implements RecordsManagementModel
{    
	protected static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
	
	private NodeRef filePlan;
	
    private FileFolderService fileFolderService;
	private NodeService nodeService;
	private NodeService unprotectedNodeService;
    private RecordsManagementActionService rmActionService;
    private RecordsManagementService rmService;
	private SearchService searchService;
	private TransactionService transactionService;
	private RetryingTransactionHelper transactionHelper;
	private DispositionService dispositionService;
	private VitalRecordService vitalRecordService;
	
	@Override
	protected void onSetUpInTransaction() throws Exception 
	{
		super.onSetUpInTransaction();

		// Get the service required in the tests
        this.fileFolderService = (FileFolderService)this.applicationContext.getBean("FileFolderService"); 
        this.nodeService = (NodeService)this.applicationContext.getBean("NodeService"); 
		this.unprotectedNodeService = (NodeService)this.applicationContext.getBean("nodeService"); 
		this.transactionService = (TransactionService)this.applicationContext.getBean("TransactionService");
		this.searchService = (SearchService)this.applicationContext.getBean("searchService");
        this.rmActionService = (RecordsManagementActionService)this.applicationContext.getBean("recordsManagementActionService");
        this.rmService = (RecordsManagementService)this.applicationContext.getBean("recordsManagementService");
		this.transactionHelper = (RetryingTransactionHelper)this.applicationContext.getBean("retryingTransactionHelper");
		this.dispositionService = (DispositionService)this.applicationContext.getBean("dispositionService");
		vitalRecordService = (VitalRecordService)applicationContext.getBean("VitalRecordService");

		// Set the current security context as admin
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
		
		// Get the test data
		setUpTestData();    
	}
	
	private void setUpTestData()
	{
        filePlan = TestUtilities.loadFilePlanData(applicationContext);
	}

    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        try
        {
            UserTransaction txn = transactionService.getUserTransaction(false);
            txn.begin();
            this.nodeService.deleteNode(filePlan);
            txn.commit();
        }
        catch (Exception e)
        {
            // Nothing
            //System.out.println("DID NOT DELETE FILE PLAN!");
        }
    }
    
    public void testDispositionPresence() throws Exception
    {
        setComplete();
        endTransaction();

        // create a record category node in 
        final NodeRef nodeRef = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        NodeRef rootNode = nodeService.getRootNode(SPACES_STORE);
                        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                        String recordCategoryName = "Test Record Category";
                        props.put(ContentModel.PROP_NAME, recordCategoryName);
                        NodeRef result = nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN, 
                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(recordCategoryName)), 
                                    TYPE_RECORD_CATEGORY, props).getChildRef();
                        return result;
                    }          
                });        

        
        // ensure the record category node has the scheduled aspect and the disposition schedule association
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        assertTrue(nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_SCHEDULED));
                        List<ChildAssociationRef> scheduleAssocs = nodeService.getChildAssocs(nodeRef, ASSOC_DISPOSITION_SCHEDULE, RegexQNamePattern.MATCH_ALL);
                        
                        
                        assertNotNull(scheduleAssocs);
                        assertEquals(1, scheduleAssocs.size());
                        
                        // test retrieval of the disposition schedule via RM service
                        DispositionSchedule schedule = dispositionService.getDispositionSchedule(nodeRef);
                        assertNotNull(schedule);
                        return null;
                    }          
                });        
    }
    
    /**
     * This test method contains a subset of the tests in TC 7-2 of the DoD doc.
     * @throws Exception
     */
    public void testRescheduleRecord_IsNotCutOff() throws Exception
    {
         final NodeRef recCat = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");
        // This RC has disposition instructions "Cut off monthly, hold 1 month, then destroy."
        
        setComplete();
        endTransaction();

        // Create a suitable folder for this test.
        final NodeRef testFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
                        String folderName = "testFolder" + System.currentTimeMillis();
                        folderProps.put(ContentModel.PROP_NAME, folderName);
                        NodeRef recordFolder = nodeService.createNode(recCat,
                                                                      ContentModel.ASSOC_CONTAINS, 
                                                                      QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderName), 
                                                                      TYPE_RECORD_FOLDER).getChildRef();
                        return recordFolder;
                    }          
                });
        
        // Create a record in the test folder. File it and declare it.
        final NodeRef testRecord = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        final NodeRef result = nodeService.createNode(testFolder, ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                        "Record" + System.currentTimeMillis() + ".txt"),
                                ContentModel.TYPE_CONTENT).getChildRef();
                        
                        rmActionService.executeRecordsManagementAction(result, "file");
                        TestUtilities.declareRecord(result, unprotectedNodeService, rmActionService);
                        return result;
                    }          
                });        

        assertTrue("recCat missing scheduled aspect", nodeService.hasAspect(recCat, RecordsManagementModel.ASPECT_SCHEDULED));
        assertFalse("folder should not have scheduled aspect", nodeService.hasAspect(testFolder, RecordsManagementModel.ASPECT_SCHEDULED));
        assertFalse("record should not have scheduled aspect", nodeService.hasAspect(testRecord, RecordsManagementModel.ASPECT_SCHEDULED));

        assertFalse("recCat should not have dispositionLifecycle aspect", nodeService.hasAspect(recCat, RecordsManagementModel.ASPECT_DISPOSITION_LIFECYCLE));
        assertTrue("testFolder missing dispositionLifecycle aspect", nodeService.hasAspect(testFolder, RecordsManagementModel.ASPECT_DISPOSITION_LIFECYCLE));
        assertFalse("testRecord should not have dispositionLifecycle aspect", nodeService.hasAspect(testRecord, RecordsManagementModel.ASPECT_DISPOSITION_LIFECYCLE));

        // Change the cutoff conditions for the associated record category
        final Date dateBeforeChange = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Date>()
                {
                    public Date execute() throws Throwable
                    {
                        Date asOfDate = dispositionService.getNextDispositionAction(testFolder).getAsOfDate();
                        System.out.println("Going to change the disposition asOf Date.");
                        System.out.println(" - Original value: " + asOfDate);

                        // Now change "Cut off monthly, hold 1 month, then destroy."
                        //    to "Cut off yearly, hold 1 month, then destroy."
                        List<DispositionActionDefinition> dads = dispositionService.getDispositionSchedule(testFolder).getDispositionActionDefinitions();
                        DispositionActionDefinition firstDAD = dads.get(0);
                        assertEquals("cutoff", firstDAD.getName());
                        NodeRef dadNode = firstDAD.getNodeRef();
                        
                        nodeService.setProperty(dadNode, PROP_DISPOSITION_PERIOD, new Period("year|1"));
                        
                        List<QName> updatedProps = new ArrayList<QName>(1);
                        updatedProps.add(PROP_DISPOSITION_PERIOD);
                        refreshDispositionActionDefinition(dadNode, updatedProps);

                        return asOfDate;
                    }          
                });

        // view the record metadata to verify that the record has been rescheduled.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        DispositionAction nextDispositionAction = dispositionService.getNextDispositionAction(testFolder);
                        
                        assertEquals("cutoff", nextDispositionAction.getName());
                        Date asOfDateAfterChange = nextDispositionAction.getAsOfDate();
                        System.out.println(" - Updated  value: " + asOfDateAfterChange);
                        
                        assertFalse("Expected disposition asOf date to change.", asOfDateAfterChange.equals(dateBeforeChange));
                        return null;
                    }          
                });

        // Change the disposition type (e.g. time-based to event-based)
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        List<RecordsManagementEvent> rmes = dispositionService.getNextDispositionAction(testFolder).getDispositionActionDefinition().getEvents();
                        System.out.println("Going to change the RMEs.");
                        System.out.println(" - Original value: " + rmes);

                        List<DispositionActionDefinition> dads = dispositionService.getDispositionSchedule(testFolder).getDispositionActionDefinitions();
                        DispositionActionDefinition firstDAD = dads.get(0);
                        assertEquals("cutoff", firstDAD.getName());
                        NodeRef dadNode = firstDAD.getNodeRef();
                        
//                        nodeService.setProperty(dadNode, PROP_DISPOSITION_PERIOD, null);
                        List<String> eventNames= new ArrayList<String>();
                        eventNames.add("study_complete");
                        nodeService.setProperty(dadNode, PROP_DISPOSITION_EVENT, (Serializable)eventNames);

                        return null;
                    }          
                });
        // Now add a second event to the same 
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        DispositionAction nextDispositionAction = dispositionService.getNextDispositionAction(testFolder);
                        StringBuilder buf = new StringBuilder();
                        for (RecordsManagementEvent e : nextDispositionAction.getDispositionActionDefinition().getEvents()) {
                            buf.append(e.getName()).append(',');
                        }

                        System.out.println("Going to change the RMEs again.");
                        System.out.println(" - Original value: " + buf.toString());

                        List<DispositionActionDefinition> dads = dispositionService.getDispositionSchedule(testFolder).getDispositionActionDefinitions();
                        DispositionActionDefinition firstDAD = dads.get(0);
                        assertEquals("cutoff", firstDAD.getName());
                        NodeRef dadNode = firstDAD.getNodeRef();
                        
                        List<String> eventNames= new ArrayList<String>();
                        eventNames.add("study_complete");
                        eventNames.add("case_complete");
                        nodeService.setProperty(dadNode, PROP_DISPOSITION_EVENT, (Serializable)eventNames);

                        return null;
                    }          
                });

        // View the record metadata to verify that the record has been rescheduled.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        DispositionAction nextDispositionAction = dispositionService.getNextDispositionAction(testFolder);
                        
                        assertEquals("cutoff", nextDispositionAction.getName());
                        StringBuilder buf = new StringBuilder();
                        for (RecordsManagementEvent e : nextDispositionAction.getDispositionActionDefinition().getEvents()) {
                            buf.append(e.getName()).append(',');
                        }
                        System.out.println(" - Updated  value: " + buf.toString());
                        
                        assertFalse("Disposition should not be eligible.", nextDispositionAction.isEventsEligible());
                        return null;
                    }          
                });

        // Tidy up test nodes.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        nodeService.deleteNode(testRecord);

                        // Change the disposition Period back to what it was.
                        List<DispositionActionDefinition> dads = dispositionService.getDispositionSchedule(testFolder).getDispositionActionDefinitions();
                        DispositionActionDefinition firstDAD = dads.get(0);
                        assertEquals("cutoff", firstDAD.getName());
                        NodeRef dadNode = firstDAD.getNodeRef();
                        nodeService.setProperty(dadNode, PROP_DISPOSITION_PERIOD, new Period("month|1"));

                        nodeService.deleteNode(testFolder);

                        return null;
                    }          
                });
    }
    
    private void refreshDispositionActionDefinition(NodeRef nodeRef, List<QName> updatedProps)
    {
        if (updatedProps != null)
        {
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put(BroadcastDispositionActionDefinitionUpdateAction.CHANGED_PROPERTIES, (Serializable)updatedProps);
            rmActionService.executeRecordsManagementAction(nodeRef, BroadcastDispositionActionDefinitionUpdateAction.NAME, params);            
        }

        // Remove the unpublished update aspect
        nodeService.removeAspect(nodeRef, ASPECT_UNPUBLISHED_UPDATE);
    }
    
	public void testGetDispositionInstructions() throws Exception
	{	
	    setComplete();
	    endTransaction();
	    
	    // Get a record
	    // TODO
	    
	    // Get a record folder
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        NodeRef folderRecord = TestUtilities.getRecordFolder(rmService, nodeService, "Reports", "AIS Audit Records", "January AIS Audit Records");
                        assertNotNull(folderRecord);
                        assertEquals("January AIS Audit Records", nodeService.getProperty(folderRecord, ContentModel.PROP_NAME));
                        
                        assertFalse(rmService.isRecord(folderRecord));
                        assertTrue(rmService.isRecordFolder(folderRecord));
                        assertFalse(rmService.isRecordCategory(folderRecord));   
                        
                        DispositionSchedule di = dispositionService.getDispositionSchedule(folderRecord);
                        assertNotNull(di);
                        assertEquals("N1-218-00-4 item 023", di.getDispositionAuthority());
                        assertEquals("Cut off monthly, hold 1 month, then destroy.", di.getDispositionInstructions());
                        assertFalse(di.isRecordLevelDisposition());
                        
                        // Get a record category
                        NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");    
                        assertNotNull(recordCategory);
                        assertEquals("AIS Audit Records", nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
                                
                        assertFalse(rmService.isRecord(recordCategory));
                        assertFalse(rmService.isRecordFolder(recordCategory));
                        assertTrue(rmService.isRecordCategory(recordCategory));   
                        
                        di = dispositionService.getDispositionSchedule(recordCategory);
                        assertNotNull(di);
                        assertEquals("N1-218-00-4 item 023", di.getDispositionAuthority());
                        assertEquals("Cut off monthly, hold 1 month, then destroy.", di.getDispositionInstructions());
                        assertFalse(di.isRecordLevelDisposition());
                        
                        List<DispositionActionDefinition> das = di.getDispositionActionDefinitions();
                        assertNotNull(das);
                        assertEquals(2, das.size());
                        assertEquals("cutoff", das.get(0).getName());
                        assertEquals("destroy", das.get(1).getName());
                        return null;
                    }
                });
    }
	
    public void testMoveRecordWithinFileplan()
    {
        setComplete();
        endTransaction();

        // We need record folders for test-filing as follows:
        // 1. A 'clean' record folder with no disposition schedult and no review period.
        // 2. A 'vital' record folder which has a review period defined.
        // 3. A 'dispositionable' record folder which has an applicable disposition schedule.
        //
        // The example fileplan includes a folder which covers [2] and [3] together.
        
        final NodeRef cleanRecordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        NodeRef result = TestUtilities.getRecordFolder(rmService, nodeService, "Civilian Files", "Case Files and Papers", "Gilbert Competency Hearing");
                        assertNotNull("cleanRecordFolder was null", result);
                        
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(result);
                        assertNull("cleanRecordFolder had non-null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertTrue("cleanRecordFolder had non-empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(result);
                        assertEquals("cleanRecordFolder had wrong review period.", "0", vitalRecordDefinition.getReviewPeriod().getExpression());
                        assertNull("cleanRecordFolder had non-null review date.", vitalRecordDefinition.getNextReviewDate());
                        return result;
                    }
                });
        final NodeRef dispAndVitalRecordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        NodeRef result = TestUtilities.getRecordFolder(rmService, nodeService, "Reports", "AIS Audit Records", "January AIS Audit Records");
                        assertNotNull("dispositionAndVitalRecordFolder was null", result);
                        
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(result);
                        assertNotNull("dispositionAndVitalRecordFolder had null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertFalse("dispositionAndVitalRecordFolder had empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(result);
                        assertFalse("dispositionAndVitalRecordFolder had wrong review period.", "none|0".equals(vitalRecordDefinition.getReviewPeriod().getExpression()));
                        assertNotNull("dispositionAndVitalRecordFolder had null review date.", vitalRecordDefinition.getNextReviewDate());
                        return result;
                    }
                });
        
        // Create a record in the 'clean' folder.
        final NodeRef testRecord = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        final NodeRef result = nodeService.createNode(cleanRecordFolder, ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                        "Record" + System.currentTimeMillis() + ".txt"),
                                ContentModel.TYPE_CONTENT).getChildRef();
                        
                        rmActionService.executeRecordsManagementAction(result, "file");
                        TestUtilities.declareRecord(result, unprotectedNodeService, rmActionService);
                        return result;
                    }
                });

        // Ensure it's devoid of all disposition and review-related state.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(testRecord);
                        assertNull("testRecord had non-null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertTrue("testRecord had non-empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(testRecord);
                        assertEquals("testRecord had wrong review period.", "0", vitalRecordDefinition.getReviewPeriod().getExpression());
                        assertNull("testRecord had non-null review date.", vitalRecordDefinition.getNextReviewDate());
                        return null;
                    }
                });
        
        // Move from non-vital to vital - also non-dispositionable to dispositionable at the same time.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        nodeService.moveNode(testRecord, dispAndVitalRecordFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
                        return null;
                    }
                });

        // Assert that the disposition and review-related data are correct after the move.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(testRecord);
                        assertNotNull("testRecord had null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertFalse("testRecord had empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(testRecord);
                        assertFalse("testRecord had wrong review period.", "0".equals(vitalRecordDefinition.getReviewPeriod().getExpression()));
                        assertNotNull("testRecord had null review date.", vitalRecordDefinition.getNextReviewDate());
                        return null;
                    }
                });

        // Move the test record back from vital to non-vital - also dispositionable to non-dispositionable at the same time.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        nodeService.moveNode(testRecord, cleanRecordFolder, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
                        return null;
                    }
                });
        
        // Assert that the disposition and review-related data are correct after the move.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(testRecord);
                        assertNull("testRecord had non-null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertTrue("testRecord had non-empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(testRecord);
                        assertEquals("testRecord had wrong review period.", "0", vitalRecordDefinition.getReviewPeriod().getExpression());
                        assertNull("testRecord had non-null review date.", vitalRecordDefinition.getNextReviewDate());
                        return null;
                    }
                });

        //TODO check the search aspect
        
        // Tidy up.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        nodeService.deleteNode(testRecord);

                        return null;
                    }
                });
    }
   
    public void testCopyRecordWithinFileplan()
    {
        setComplete();
        endTransaction();

        // We need record folders for test-filing as follows:
        // 1. A 'clean' record folder with no disposition schedule and no review period.
        // 2. A 'vital' record folder which has a review period defined.
        // 3. A 'dispositionable' record folder which has an applicable disposition schedule.
        //
        // The example fileplan includes a folder which covers [2] and [3] together.
        
        final NodeRef cleanRecordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        NodeRef result = TestUtilities.getRecordFolder(rmService, nodeService, "Civilian Files", "Case Files and Papers", "Gilbert Competency Hearing");
                        assertNotNull("cleanRecordFolder was null", result);
                        
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(result);
                        assertNull("cleanRecordFolder had non-null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertTrue("cleanRecordFolder had non-empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(result);
                        assertEquals("cleanRecordFolder had wrong review period.", "0", vitalRecordDefinition.getReviewPeriod().getExpression());
                        assertNull("cleanRecordFolder had non-null review date.", vitalRecordDefinition.getNextReviewDate());
                        return result;
                    }
                });
        final NodeRef dispAndVitalRecordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        NodeRef result = TestUtilities.getRecordFolder(rmService, nodeService, "Reports", "AIS Audit Records", "January AIS Audit Records");
                        assertNotNull("dispositionAndVitalRecordFolder was null", result);
                        
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(result);
                        assertNotNull("dispositionAndVitalRecordFolder had null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertFalse("dispositionAndVitalRecordFolder had empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(result);
                        assertFalse("dispositionAndVitalRecordFolder had wrong review period.", "none|0".equals(vitalRecordDefinition.getReviewPeriod().getExpression()));
                        assertNotNull("dispositionAndVitalRecordFolder had null review date.", vitalRecordDefinition.getNextReviewDate());
                        return result;
                    }
                });
        
        // Create a record in the 'clean' folder.
        final NodeRef testRecord = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        final NodeRef result = nodeService.createNode(cleanRecordFolder, ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                        "Record" + System.currentTimeMillis() + ".txt"),
                                ContentModel.TYPE_CONTENT).getChildRef();
                        
                        rmActionService.executeRecordsManagementAction(result, "file");
                        TestUtilities.declareRecord(result, unprotectedNodeService, rmActionService);
                        return result;
                    }
                });

        // Ensure it's devoid of all disposition and review-related state.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(testRecord);
                        assertNull("testRecord had non-null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertTrue("testRecord had non-empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(testRecord);
                        assertEquals("testRecord had wrong review period.", "0", vitalRecordDefinition.getReviewPeriod().getExpression());
                        assertNull("testRecord had non-null review date.", vitalRecordDefinition.getNextReviewDate());
                        return null;
                    }
                });
        
        // Copy from non-vital to vital - also non-dispositionable to dispositionable at the same time.
        final NodeRef copiedNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        FileInfo fileInfo = fileFolderService.copy(testRecord, dispAndVitalRecordFolder, null);
                        NodeRef n = fileInfo.getNodeRef();
                        return n;
                    }
                });

        // Assert that the disposition and review-related data are correct after the copy.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(copiedNode);
                        assertNotNull("copiedNode had null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertFalse("copiedNode had empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(copiedNode);
                        assertFalse("copiedNode had wrong review period.", "0".equals(vitalRecordDefinition.getReviewPeriod().getExpression()));
                        assertNotNull("copiedNode had null review date.", vitalRecordDefinition.getNextReviewDate());
                        return null;
                    }
                });

        // Create a record in the 'vital and disposition' folder.
        final NodeRef testRecord2 = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        final NodeRef result = nodeService.createNode(dispAndVitalRecordFolder, ContentModel.ASSOC_CONTAINS,
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                        "Record2" + System.currentTimeMillis() + ".txt"),
                                ContentModel.TYPE_CONTENT).getChildRef();
                        
                        rmActionService.executeRecordsManagementAction(result, "file");
                        TestUtilities.declareRecord(result, unprotectedNodeService, rmActionService);
                        return result;
                    }
                });

        // Check the vital and disposition status.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(testRecord2);
                        assertNotNull("testRecord2 had null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertFalse("testRecord2 had empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(testRecord2);
                        assertFalse("testRecord2 had wrong review period.", "0".equals(vitalRecordDefinition.getReviewPeriod().getExpression()));
                        assertNotNull("testRecord2 had null review date.", vitalRecordDefinition.getNextReviewDate());
                        return null;
                    }
                });

        // copy the record back from vital to non-vital - also dispositionable to non-dispositionable at the same time.
        final NodeRef copiedBackNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        FileInfo fileInfo = fileFolderService.copy(testRecord2, cleanRecordFolder, null); // TODO Something wrong here.
                        NodeRef n = fileInfo.getNodeRef();
                        return n;
                    }
                });
        
        // Assert that the disposition and review-related data are correct after the copy-back.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        final DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(copiedBackNode);
                        assertNull("copiedBackNode had non-null disposition instructions.", dispositionSchedule.getDispositionInstructions());
                        assertTrue("copiedBackNode had non-empty disposition instruction definitions.", dispositionSchedule.getDispositionActionDefinitions().isEmpty());

                        final VitalRecordDefinition vitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(copiedBackNode);
                        assertEquals("copiedBackNode had wrong review period.", "0", vitalRecordDefinition.getReviewPeriod().getExpression());
                        assertNull("copiedBackNode had non-null review date.", vitalRecordDefinition.getNextReviewDate());
                        return null;
                    }
                });

        //TODO check the search aspect
        
        // Tidy up.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        nodeService.deleteNode(copiedBackNode);
                        nodeService.deleteNode(testRecord2);
                        nodeService.deleteNode(copiedNode);
                        nodeService.deleteNode(testRecord);

                        return null;
                    }
                });
    }
   
	public void xxxtestUpdateNextDispositionAction()
	{
        setComplete();
        endTransaction();

        final FileAction fileAction = (FileAction)applicationContext.getBean("file");
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        // Get a record folder
                        NodeRef folderRecord = TestUtilities.getRecordFolder(rmService, nodeService, "Reports", "AIS Audit Records", "January AIS Audit Records");
                        assertNotNull(folderRecord);
                        assertEquals("January AIS Audit Records", nodeService.getProperty(folderRecord, ContentModel.PROP_NAME));
                        
                        DispositionSchedule di = dispositionService.getDispositionSchedule(folderRecord);
                        assertNotNull(di);
                        assertEquals("N1-218-00-4 item 023", di.getDispositionAuthority());
                        assertEquals("Cut off monthly, hold 1 month, then destroy.", di.getDispositionInstructions());
                        assertFalse(di.isRecordLevelDisposition());
                        
                        assertFalse(nodeService.hasAspect(folderRecord, ASPECT_DISPOSITION_LIFECYCLE));
                        
                        fileAction.updateNextDispositionAction(folderRecord);
                        
                        
                        // Check the next disposition action
                        assertTrue(nodeService.hasAspect(folderRecord, ASPECT_DISPOSITION_LIFECYCLE));
                        NodeRef ndNodeRef = nodeService.getChildAssocs(folderRecord, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                        assertNotNull(ndNodeRef);
                        assertEquals("cutoff", nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                        assertEquals(di.getDispositionActionDefinitions().get(0).getId(), nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                        assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                        
                        // Check the history is empty
                        // TODO        
                        
                        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                        props.put(PROP_CUT_OFF_DATE, new Date());
                        unprotectedNodeService.addAspect(folderRecord, ASPECT_CUT_OFF, props);        
                        fileAction.updateNextDispositionAction(folderRecord);
                        
                        assertTrue(nodeService.hasAspect(folderRecord, ASPECT_DISPOSITION_LIFECYCLE));
                        ndNodeRef = nodeService.getChildAssocs(folderRecord, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                        assertNotNull(ndNodeRef);
                        assertEquals("destroy", nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                        assertEquals(di.getDispositionActionDefinitions().get(1).getId(), nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                        assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                        
                        // Check the history has an action
                        // TODO
                        
                        fileAction.updateNextDispositionAction(folderRecord);
                        
                        assertTrue(nodeService.hasAspect(folderRecord, ASPECT_DISPOSITION_LIFECYCLE));
                        assertTrue(nodeService.getChildAssocs(folderRecord, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).isEmpty());
                        
                        // Check the history has both actions
                        // TODO
                        return null;
                    }
                });
	}
}
