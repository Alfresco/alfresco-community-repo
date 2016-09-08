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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionResult;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.EditDispositionActionAsOfDateAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.EditReviewAsOfDateAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FileAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.FreezeAction;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigService;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint.MatchLogic;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.event.EventCompletionDetails;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementSearchBehaviour;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestUtilities;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.fts.FullTextSearchIndexer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PublicServiceAccessService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;

/**
 * DOD System Test
 * 
 * @author Roy Wetherall, Neil McErlean
 */
public class DOD5015SystemTest extends BaseSpringTest implements RecordsManagementModel, DOD5015Model
{    
	private static final Period weeklyReview = new Period("week|1");
    private static final Period dailyReview = new Period("day|1");
    public static final long TWENTY_FOUR_HOURS_IN_MS = 24 * 60 * 60 * 1000; // hours * minutes * seconds * millis

	protected static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
	
	private NodeRef filePlan;
	
	private NodeService unprotectedNodeService;
	private NodeService nodeService;
	private SearchService searchService;
	private ImporterService importService;
	private ContentService contentService;
    private RecordsManagementService rmService;
    private RecordsManagementActionService rmActionService;
    private ServiceRegistry serviceRegistry;
	private TransactionService transactionService;
	private RecordsManagementAdminService rmAdminService;
	private RMCaveatConfigService caveatConfigService;
	private DispositionService dispositionService;
	private VitalRecordService vitalRecordService;
	
	private MutableAuthenticationService authenticationService;
	private PersonService personService;
	private AuthorityService authorityService;
	private PermissionService permissionService;
	private RetryingTransactionHelper transactionHelper;

    private PublicServiceAccessService publicServiceAccessService;
    private FullTextSearchIndexer luceneFTS;
	
	// example base test data for supplemental markings list (see also recordsModel.xml)
	protected final static String NOFORN     = "NOFORN";     // Not Releasable to Foreign Nationals/Governments/Non-US Citizens
	protected final static String NOCONTRACT = "NOCONTRACT"; // Not Releasable to Contractors or Contractor/Consultants
	protected final static String FOUO       = "FOUO";       // For Official Use Only 
	protected final static String FGI        = "FGI";        // Foreign Government Information
	
	// example user-defined field
	protected final static QName CONSTRAINT_CUSTOM_PRJLIST = QName.createQName(RM_CUSTOM_URI, "prjList");
	protected final static QName PROP_CUSTOM_PRJLIST = QName.createQName(RM_CUSTOM_URI, "projectNameList");
	
	protected final static String PRJ_A = "Project A";
	protected final static String PRJ_B = "Project B";
	protected final static String PRJ_C = "Project C";
	
	@Override
	protected void onSetUpInTransaction() throws Exception 
	{
		super.onSetUpInTransaction();

		// Get the service required in the tests
		this.unprotectedNodeService = (NodeService)applicationContext.getBean("nodeService");
		this.nodeService = (NodeService)this.applicationContext.getBean("NodeService"); // use upper 'N'odeService (to test access config interceptor)		                NodeService unprotectedNodeService = (NodeService)applicationContext.getBean("nodeService");
		this.authenticationService = (MutableAuthenticationService)this.applicationContext.getBean("AuthenticationService");
		this.personService = (PersonService)this.applicationContext.getBean("PersonService");
		this.authorityService = (AuthorityService)this.applicationContext.getBean("AuthorityService");
		this.permissionService = (PermissionService)this.applicationContext.getBean("PermissionService");		
		this.searchService = (SearchService)this.applicationContext.getBean("SearchService"); // use upper 'S'earchService (to test access config interceptor)
		this.importService = (ImporterService)this.applicationContext.getBean("importerComponent");
		this.contentService = (ContentService)this.applicationContext.getBean("ContentService");
        this.rmService = (RecordsManagementService)this.applicationContext.getBean("RecordsManagementService");
        this.rmActionService = (RecordsManagementActionService)this.applicationContext.getBean("RecordsManagementActionService");
        this.serviceRegistry = (ServiceRegistry)this.applicationContext.getBean("ServiceRegistry");
		this.transactionService = (TransactionService)this.applicationContext.getBean("TransactionService");
		this.rmAdminService = (RecordsManagementAdminService)this.applicationContext.getBean("RecordsManagementAdminService");
		this.caveatConfigService = (RMCaveatConfigService)this.applicationContext.getBean("caveatConfigService");
		this.publicServiceAccessService = (PublicServiceAccessService)this.applicationContext.getBean("PublicServiceAccessService");
		this.transactionHelper = (RetryingTransactionHelper)this.applicationContext.getBean("retryingTransactionHelper");
		this.dispositionService = (DispositionService)this.applicationContext.getBean("DispositionService");
        this.luceneFTS = (FullTextSearchIndexer)this.applicationContext.getBean("LuceneFullTextSearchIndexer");
        this.vitalRecordService = (VitalRecordService)applicationContext.getBean("VitalRecordService");
        
		
		// Set the current security context as admin
		AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
		
		// Get the test data
		filePlan = TestUtilities.loadFilePlanData(applicationContext);
        
        File file = new File(System.getProperty("user.dir")+"/test-resources/testCaveatConfig1.json"); // from test-resources
        assertTrue(file.exists());
        
        caveatConfigService.updateOrCreateCaveatConfig(file);
        
        // set/reset allowed values (empty list by default)
        List<String> newValues = new ArrayList<String>(4);
        newValues.add(NOFORN);
        newValues.add(NOCONTRACT);
        newValues.add(FOUO);
        newValues.add(FGI);
        
        rmAdminService.changeCustomConstraintValues(RecordsManagementCustomModel.CONSTRAINT_CUSTOM_SMLIST, newValues);
        
        // We pause FTS during this test, as it moves around records in intermediate places, and otherwise FTS may not
        // finish clearing up its mess before each test finishes
        this.luceneFTS.pause();        
	}
	
	

	/* (non-Javadoc)
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onTearDown()
     */
    @Override
    protected void onTearDown() throws Exception
    {
        super.onTearDown();

        // Let FTS catch up again.
        this.luceneFTS.resume();
    }



    /**
	 * Tests that the test data has been loaded correctly
	 */
	public void xtestTestData() throws Exception
	{
	    // make sure the folders that should have disposition schedules do so
	    NodeRef janAuditRecordsFolder = TestUtilities.getRecordFolder(rmService, nodeService, "Reports", "AIS Audit Records", "January AIS Audit Records");
	    assertNotNull(janAuditRecordsFolder);
	    
	    // ensure the folder has the disposition lifecycle aspect
	    assertTrue("Expected 'January AIS Audit Records' folder to have disposition lifecycle aspect applied", 
	                nodeService.hasAspect(janAuditRecordsFolder, ASPECT_DISPOSITION_LIFECYCLE));
	    
	    // ensure the folder has the correctly setup search aspect
	    checkSearchAspect(janAuditRecordsFolder);
	    
	    // check another folder that has events as part of the disposition schedule
	    NodeRef equalOppCoordFolder = TestUtilities.getRecordFolder(rmService, nodeService, "Military Files", "Personnel Security Program Records", "Equal Opportunity Coordinator");
	    assertNotNull(equalOppCoordFolder);
	    assertTrue("Expected 'Equal Opportunity Coordinator' folder to have disposition lifecycle aspect applied", 
                    nodeService.hasAspect(equalOppCoordFolder, ASPECT_DISPOSITION_LIFECYCLE));
	    checkSearchAspect(equalOppCoordFolder);
	}
	
    /**
     * This test method creates a non-vital record and then moves it to a vital folder
     * (triggering a refile) and then moves it a second time to another vital record
     * having different metadata.
     * 
     * Moving a Record within the FilePlan should trigger a "refile". Refiling a record
     * will lead to the reconsideration of its disposition, vital and transfer/accession
     * metadata, with potential changes therein.
     */
    public void testMoveRefileRecord() throws Exception
    {
        // Commit in order to trigger the setUpRecordFolder behaviour
        setComplete();
        endTransaction();
        
        final NodeRef nonVitalFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Create a record folder under a "non-vital" category
                NodeRef nonVitalRecordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "Unit Manning Documents");    
                assertNotNull(nonVitalRecordCategory);

                return createRecFolderNode(nonVitalRecordCategory);
            }          
        });        
        
        final NodeRef recordUnderTest = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Create a (non-vital) record under the above folder
                NodeRef recordUnderTest = createRecordNode(nonVitalFolder);

                rmActionService.executeRecordsManagementAction(recordUnderTest, "file");

                TestUtilities.declareRecord(recordUnderTest, unprotectedNodeService, rmActionService);
                
                return recordUnderTest;
            }          
        });        
        
        final NodeRef vitalFolder =transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {                
            	// No need to commit the transaction here as the record is non-vital and
                // there is no metadata to copy down.
                
                NodeRef vitalFolder = retrieveJanuaryAISVitalFolder();
                
                // Move the non-vital record under the vital folder.
                serviceRegistry.getFileFolderService().move(recordUnderTest, vitalFolder, null);
                
                return vitalFolder;
            }          
        });
        
        final NodeRef secondVitalFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // At this point, the formerly nonVitalRecord is now actually vital.
                assertTrue("Expected record.", rmService.isRecord(recordUnderTest));
                assertTrue("Expected declared.", rmService.isRecordDeclared(recordUnderTest));
                
                final VitalRecordDefinition recordVrd = vitalRecordService.getVitalRecordDefinition(recordUnderTest);
                assertNotNull("Moved record should now have a Vital Rec Defn", recordVrd);
                assertEquals("Moved record had wrong review period",
                        vitalRecordService.getVitalRecordDefinition(vitalFolder).getReviewPeriod(), recordVrd.getReviewPeriod());
                assertNotNull("Moved record should now have a review-as-of date", nodeService.getProperty(recordUnderTest, PROP_REVIEW_AS_OF));
                
                // Create another folder with different vital/disposition instructions
                //TODO Change disposition instructions
                NodeRef vitalRecordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");    
                assertNotNull(vitalRecordCategory);
                return createRecFolderNode(vitalRecordCategory);
            }          
        });
        
        final Date reviewDate = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Date>()
        {
            public Date execute() throws Throwable
            {
                Map<QName, Serializable> props = nodeService.getProperties(secondVitalFolder);
                final Serializable secondVitalFolderReviewPeriod = props.get(PROP_REVIEW_PERIOD);
                assertEquals("Unexpected review period.", weeklyReview, secondVitalFolderReviewPeriod);
                
                // We are changing the review period of this second record folder.
                nodeService.setProperty(secondVitalFolder, PROP_REVIEW_PERIOD, dailyReview);
                
                Date reviewDate = (Date)nodeService.getProperty(recordUnderTest, PROP_REVIEW_AS_OF);
                
                // Move the newly vital record under the second vital folder. I expect the reviewPeriod
                // for the record to be changed again.
                serviceRegistry.getFileFolderService().move(recordUnderTest, secondVitalFolder, null);
                
                return reviewDate;
            }          
        });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                Period newReviewPeriod = vitalRecordService.getVitalRecordDefinition(recordUnderTest).getReviewPeriod();
                assertEquals("Unexpected review period.", dailyReview, newReviewPeriod);
                
                Date updatedReviewDate = (Date)nodeService.getProperty(recordUnderTest, PROP_REVIEW_AS_OF);
                // The reviewAsOf date should have changed to "24 hours from now".
                assertFalse("reviewAsOf date was unchanged", reviewDate.equals(updatedReviewDate));
                long millisecondsUntilNextReview = updatedReviewDate.getTime() - new Date().getTime();
                assertTrue("new reviewAsOf date was not within 24 hours of now.",
                        millisecondsUntilNextReview <= TWENTY_FOUR_HOURS_IN_MS);

                nodeService.deleteNode(recordUnderTest);
                nodeService.deleteNode(nonVitalFolder);
                nodeService.deleteNode(secondVitalFolder);
                
                return null;
            }          
        });
    }

    public void off_testMoveRefileRecordFolder() throws Exception
    {
        //TODO Impl me
        fail("Not yet impl'd.");
    }

    public void off_testCopyRefileRecordFolder() throws Exception
    {
        //TODO Impl me
        fail("Not yet impl'd.");
    }

    public void off_testCopyRefileRecord() throws Exception
    {
        //TODO Impl me
        fail("Not yet impl'd.");
    }

    private NodeRef createRecordCategoryNode(NodeRef parentRecordSeries)
    {
        NodeRef newCategory = this.nodeService.createNode(parentRecordSeries, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Test category " + System.currentTimeMillis()),
                    TYPE_RECORD_CATEGORY).getChildRef();
        
        return newCategory;
    }
    
    private NodeRef createRecFolderNode(NodeRef parentRecordCategory)
    {
        NodeRef newFolder = this.nodeService.createNode(parentRecordCategory,
                                   ContentModel.ASSOC_CONTAINS,
                                   QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Test folder " + System.currentTimeMillis()),
                                   TYPE_RECORD_FOLDER).getChildRef();
        return newFolder;
    }

    private NodeRef createRecordNode(NodeRef parentFolder)
    {
        NodeRef newRecord = this.nodeService.createNode(parentFolder,
                                    ContentModel.ASSOC_CONTAINS,
                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                            "Record" + System.currentTimeMillis() + ".txt"),
                                    ContentModel.TYPE_CONTENT).getChildRef();
        ContentWriter writer = this.contentService.getWriter(newRecord, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("Irrelevant content");
        return newRecord;
    }
    
    private NodeRef retrieveJanuaryAISVitalFolder()
    {
        final List<NodeRef> resultNodeRefs = retrieveJanuaryAISVitalFolders();
        final int folderCount = resultNodeRefs.size();
//        assertTrue("There should only be one 'January AIS Audit Records' folder. Were " + folderCount, folderCount == 1);
        
        // This nodeRef should have rma:VRI=true, rma:reviewPeriod=week|1, rma:isClosed=false
        return resultNodeRefs.get(0);
    }

    private List<NodeRef> retrieveJanuaryAISVitalFolders()
    {
        String typeQuery = "TYPE:\"" + TYPE_RECORD_FOLDER + "\" AND @cm\\:name:\"January AIS Audit Records\"";
        ResultSet types = this.searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, typeQuery);
        
        final List<NodeRef> resultNodeRefs = types.getNodeRefs();
        types.close();
        return resultNodeRefs;
    }
    
    /**
     * Test duplicate id's
     */
    public void xxtestDuplicateIDs()
    {
        List<NodeRef> roots = rmService.getFilePlans();
        final NodeRef root = roots.get(0);
        setComplete();
        endTransaction();
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                String name1 = GUID.generate();
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
                props.put(ContentModel.PROP_NAME, name1);
                props.put(PROP_IDENTIFIER, "bob");
                ChildAssociationRef assoc = nodeService.createNode(
                                            root, 
                                            ContentModel.ASSOC_CONTAINS, 
                                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name1), 
                                            TYPE_RECORD_CATEGORY,
                                            props);
                
                return assoc.getChildRef();
            }          
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                try
                {
                    String name1 = GUID.generate();   
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
                    props.put(ContentModel.PROP_NAME, name1);
                    props.put(PROP_IDENTIFIER, "bob");
                    ChildAssociationRef assoc = nodeService.createNode(
                                                root, 
                                                ContentModel.ASSOC_CONTAINS, 
                                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name1), 
                                                TYPE_RECORD_CATEGORY,
                                                props);
                    fail("Cant duplicate series id");
                }
                catch (Exception e)
                {
                    // expected
                }
                
                return null;
            }          
        });
    }

    public void testDispositionLifecycle_0318_01_basictest() throws Exception
	{	   
        final NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records"); 
        setComplete();
        endTransaction();
        	    
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                   
                assertNotNull(recordCategory);
                assertEquals("AIS Audit Records", nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
                        
                return createRecordFolder(recordCategory, "March AIS Audit Records");                        
            }          
        });
        
        final NodeRef recordOne = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Check the folder to ensure everything has been inherited correctly
                assertTrue(((Boolean)nodeService.getProperty(recordFolder, PROP_VITAL_RECORD_INDICATOR)).booleanValue());
                assertEquals(nodeService.getProperty(recordCategory, PROP_REVIEW_PERIOD),
                             nodeService.getProperty(recordFolder, PROP_REVIEW_PERIOD));
                
                // Create the document
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, "MyRecord.txt");
                NodeRef recordOne = nodeService.createNode(recordFolder, 
                                                           ContentModel.ASSOC_CONTAINS, 
                                                           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyRecord.txt"), 
                                                           ContentModel.TYPE_CONTENT).getChildRef();
                
                // Set the content
                ContentWriter writer = contentService.getWriter(recordOne, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.setEncoding("UTF-8");
                writer.putContent("There is some content in this record");
                
                return recordOne;
            }          
        });
        
	    // Checked that the document has been marked as incomplete
	    System.out.println("recordOne ...");
 
	    transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_RECORD));
                assertNotNull(nodeService.getProperty(recordOne, PROP_IDENTIFIER));
                System.out.println("Record id: " + nodeService.getProperty(recordOne, PROP_IDENTIFIER));
                assertNotNull(nodeService.getProperty(recordOne, PROP_DATE_FILED));
                System.out.println("Date filed: " + nodeService.getProperty(recordOne, PROP_DATE_FILED));
                
                // Check the review schedule
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_VITAL_RECORD));
                assertNotNull(nodeService.getProperty(recordOne, PROP_REVIEW_AS_OF));
                System.out.println("Review as of: " + nodeService.getProperty(recordOne, PROP_REVIEW_AS_OF));
                
                // Change the review asOf date
                Date nowDate = new Date();
                assertFalse(nowDate.equals(nodeService.getProperty(recordOne, PROP_REVIEW_AS_OF)));
                Map<String, Serializable> reviewAsOfParams = new HashMap<String, Serializable>(1);
                reviewAsOfParams.put(EditReviewAsOfDateAction.PARAM_AS_OF_DATE, nowDate);
                rmActionService.executeRecordsManagementAction(recordOne, "editReviewAsOfDate", reviewAsOfParams);
                assertTrue(nowDate.equals(nodeService.getProperty(recordOne, PROP_REVIEW_AS_OF)));

                // NOTE the disposition is being managed at a folder level ...
                
                // Check the disposition action
                assertFalse(nodeService.hasAspect(recordOne, ASPECT_DISPOSITION_LIFECYCLE));
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                
                NodeRef ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);        
                
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                System.out.println("Disposition action id: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                assertEquals("cutoff", nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                System.out.println("Disposition action: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                System.out.println("Disposition as of: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                
                // Check for the search properties having been populated
                checkSearchAspect(recordFolder);
                
                // Test the declaration of a record by editing properties
                Map<QName, Serializable> propValues = new HashMap<QName, Serializable>();   
                propValues.put(RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());       
                List<String> smList = new ArrayList<String>(2);
                smList.add(FOUO);
                smList.add(NOFORN);
                propValues.put(RecordsManagementModel.PROP_SUPPLEMENTAL_MARKING_LIST, (Serializable)smList);        
                propValues.put(RecordsManagementModel.PROP_MEDIA_TYPE, "mediaTypeValue"); 
                propValues.put(RecordsManagementModel.PROP_FORMAT, "formatValue"); 
                propValues.put(RecordsManagementModel.PROP_DATE_RECEIVED, new Date());
                nodeService.addProperties(recordOne, propValues);
                
                return null;
            }          
        });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Try and declare, expected failure
                try
                {
                    rmActionService.executeRecordsManagementAction(recordOne, "declareRecord");
                    fail("Should not be able to declare a record that still has mandatory properties unset");
                }
                catch (Exception e)
                {
                    // Expected
                }
                
                return null;
            }          
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @SuppressWarnings("deprecation")
            public Object execute() throws Throwable
            {
                assertTrue("Before test DECLARED aspect was set", 
                           nodeService.hasAspect(recordOne, ASPECT_DECLARED_RECORD) == false);    
                                     
                nodeService.setProperty(recordOne, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
                nodeService.setProperty(recordOne, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
                nodeService.setProperty(recordOne, ContentModel.PROP_TITLE, "titleValue");
                
                // Declare the record as we have set everything we should have
                rmActionService.executeRecordsManagementAction(recordOne, "declareRecord");
                assertTrue(" the record is not declared", nodeService.hasAspect(recordOne, ASPECT_DECLARED_RECORD));
                
                // check that the declaredAt and declaredBy properties are set
                assertNotNull(nodeService.getProperty(recordOne, PROP_DECLARED_BY));
                assertEquals("admin", nodeService.getProperty(recordOne, PROP_DECLARED_BY));
                assertNotNull(nodeService.getProperty(recordOne, PROP_DECLARED_AT));
                Date dateNow = new Date();
                Date declaredDate = (Date)nodeService.getProperty(recordOne, PROP_DECLARED_AT);
                assertEquals(declaredDate.getDate(), dateNow.getDate());
                assertEquals(declaredDate.getMonth(), dateNow.getMonth());
                assertEquals(declaredDate.getYear(), dateNow.getYear());
                
                // Check that the history is empty
                List<DispositionAction> history = dispositionService.getCompletedDispositionActions(recordFolder);
                assertNotNull(history);
                assertEquals(0, history.size());
                
                return null;
            }          
        });     
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Execute the cutoff action (should fail because this is being done at the record level)
                try
                {
                    rmActionService.executeRecordsManagementAction(recordFolder, "cutoff", null);
                    fail(("Shouldn't have been able to execute cut off at the record level"));
                }
                catch (Exception e)
                {
                    // expected
                }
                
                // Execute the cutoff action (should fail becuase it is not yet eligiable)
                try
                {
                    rmActionService.executeRecordsManagementAction(recordFolder, "cutoff", null);
                    fail(("Shouldn't have been able to execute because it is not yet eligiable"));
                }
                catch (Exception e)
                {
                    // expected
                }
                
                return null;
            }          
        });
        
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Clock the asOf date back to ensure eligibility
                NodeRef ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();     
                Date nowDate = calendar.getTime();
                assertFalse(nowDate.equals(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF)));
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(EditDispositionActionAsOfDateAction.PARAM_AS_OF_DATE, nowDate);                
                rmActionService.executeRecordsManagementAction(recordFolder, "editDispositionActionAsOfDate", params);
                assertTrue(nowDate.equals(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF)));
                
                // Cut off
                rmActionService.executeRecordsManagementAction(recordFolder, "cutoff", null);
                
                return null;
            }          
        });
      
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Check the disposition action
                assertFalse(nodeService.hasAspect(recordOne, ASPECT_DISPOSITION_LIFECYCLE));
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                
                NodeRef ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);                
                
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                System.out.println("Disposition action id: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                assertEquals("destroy", nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                System.out.println("Disposition action: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                System.out.println("Disposition as of: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                assertNull(nodeService.getProperty(recordFolder, RecordsManagementSearchBehaviour.PROP_RS_DISPOSITION_EVENTS));
                                 
                // Check the previous action details
                checkLastDispositionAction(recordFolder, "cutoff", 1);
         
                // Check for the search properties having been populated
                checkSearchAspect(recordFolder);
                
                // Clock the asOf date back to ensure eligibility
                ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();     
                Date nowDate = calendar.getTime();
                assertFalse(nowDate.equals(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF)));
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(EditDispositionActionAsOfDateAction.PARAM_AS_OF_DATE, nowDate);                
                rmActionService.executeRecordsManagementAction(recordFolder, "editDispositionActionAsOfDate", params);
                assertTrue(nowDate.equals(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF)));
                
                rmActionService.executeRecordsManagementAction(recordFolder, "destroy", null);
                
                // Check that the node has been destroyed (ghosted)
                //assertFalse(nodeService.exists(recordFolder));
                //assertFalse(nodeService.exists(recordOne));
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_GHOSTED));
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_GHOSTED));
                
                // Check the history
                if (nodeService.exists(recordFolder) == true)
                {
                    checkLastDispositionAction(recordFolder, "destroy", 2);
                }
                
                return null;
            }          
        });
    }
    
    /**
     * Tests the re-scheduling of disposition lifecycles when the schedule changes 
     */
    public void testDispositionLifecycle_0318_reschedule_folderlevel() throws Exception
    {
        final NodeRef recordSeries = TestUtilities.getRecordSeries(rmService, nodeService, "Reports"); 
        setComplete();
        endTransaction();
        
        // create a category
        final NodeRef recordCategory = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordSeries);
                assertEquals("Reports", nodeService.getProperty(recordSeries, ContentModel.PROP_NAME));
                        
                return createRecordCategoryNode(recordSeries);
            }          
        });
        
        // define the disposition schedule for the category (Cut off monthly, hold 1 month, then destroy)
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertNotNull(recordCategory);
                
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_INSTRUCTIONS, "Cutoff after 1 month then destroy after 1 month");
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_AUTHORITY, "Alfresco");
                
                // define properties for both steps
                Map<QName, Serializable> step1 = new HashMap<QName, Serializable>();
                step1.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "cutoff");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Cutoff after 1 month");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|1");
                
                Map<QName, Serializable> step2 = new HashMap<QName, Serializable>();
                step2.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "destroy");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Destroy after 1 month");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|1");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY, PROP_CUT_OFF_DATE);
                
                // add the action definitions to the schedule
                dispositionService.addDispositionActionDefinition(schedule, step1);
                dispositionService.addDispositionActionDefinition(schedule, step2);
                
                return null;
            }          
        });
        
        // create a record folder
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecordFolder(recordCategory, "Folder1");
            }          
        });
        
        // make sure the disposition lifecycle is present and correct
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @SuppressWarnings("deprecation")
            public Object execute() throws Throwable
            {
                assertNotNull(recordFolder);
                
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);        
                
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                System.out.println("Disposition action id: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                assertEquals("cutoff", nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                System.out.println("Disposition action: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                System.out.println("Disposition as of: " + asOfDate);
                
                // make sure the as of date is a month in the future
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 1);
                int monthThen = cal.get(Calendar.MONTH);
                assertEquals(asOfDate.getMonth(), monthThen);;
                
                // make sure there aren't any events
                List<ChildAssociationRef> events = nodeService.getChildAssocs(ndNodeRef, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL);
                assertEquals(0, events.size());
                
                // Check for the search properties having been populated
                checkSearchAspect(recordFolder);
                
                return null;
            }          
        });
        
        // change the period on the 1st step of the disposition schedule and make sure it perculates down
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // define changes for schedule
                Map<QName, Serializable> changes = new HashMap<QName, Serializable>();
                changes.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|3");
                
                // update the second dispostion action definition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Adding 3 months to period for 1st step: " + actionDefs.get(0).getName());
                updateDispositionActionDefinition(schedule, actionDefs.get(0), changes);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle asOf date has been updated
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @SuppressWarnings("deprecation")
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);        
                
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                System.out.println("Disposition as of: " + asOfDate);
                
                // make sure the as of date is a month in the future
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, 3);        
                System.out.println("Test date: " + calendar.getTime());
                Calendar asOfCalendar = Calendar.getInstance();
                asOfCalendar.setTime(asOfDate);
                assertEquals(calendar.get(Calendar.MONTH), asOfCalendar.get(Calendar.MONTH));
                
                // Check for the search properties having been populated
                checkSearchAspect(recordFolder);
                
                return null;
            }          
        });
        
        // change the period on the 2nd step of the disposition schedule and make sure it DOES NOT perculate down
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // define changes for schedule
                Map<QName, Serializable> changes = new HashMap<QName, Serializable>();
                changes.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|6");
                
                // update the first dispostion action definition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Adding 6 months to period for 2nd step: " + actionDefs.get(1).getName());
                updateDispositionActionDefinition(schedule, actionDefs.get(1), changes);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle asOf date has NOT been updated as the period was 
        // changed for a step other than the current one
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @SuppressWarnings("deprecation")
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);        
                
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                System.out.println("Disposition as of: " + asOfDate);
                
                // make sure the as of date is a month in the future
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, 3);
                assertEquals("Expecting the asOf date to be unchanged",asOfDate.getMonth(), calendar.get(Calendar.MONTH));                
                
                // Check for the search properties having been populated
                checkSearchAspect(recordFolder);
                
                return null;
            }          
        });
        
        // change the disposition schedule to be event based rather than time based i.e.
        // remove the period properties and supply 2 events in its place.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // define changes for schedule
                Map<QName, Serializable> changes = new HashMap<QName, Serializable>();
                changes.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, null);
                List<String> events = new ArrayList<String>(2);
                events.add("no_longer_needed");
                events.add("case_complete");
                changes.put(RecordsManagementModel.PROP_DISPOSITION_EVENT, (Serializable)events);
                
                // update the first dispostion action definition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Removing period and adding no_longer_needed and case_complete to 1st step: " + 
                            actionDefs.get(0).getName());
                updateDispositionActionDefinition(schedule, actionDefs.get(0), changes);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle asOf date has been reset and there are now
        // events hanging off the nextdispositionaction node
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);
                
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                System.out.println("New disposition as of: " + asOfDate);
                assertNull("Expecting asOfDate to be null", asOfDate);
                
                // make sure the 2 events are present
                List<ChildAssociationRef> events = nodeService.getChildAssocs(ndNodeRef, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL);
                assertEquals(2, events.size());
                NodeRef event1 = events.get(0).getChildRef();
                assertEquals("no_longer_needed", nodeService.getProperty(event1, PROP_EVENT_EXECUTION_NAME));
                NodeRef event2 = events.get(1).getChildRef();
                assertEquals("case_complete", nodeService.getProperty(event2, PROP_EVENT_EXECUTION_NAME));
                
                // Check for the search properties having been populated
                checkSearchAspect(recordFolder, false);
                
                return null;
            }          
        });
        
        // remove one of the events just added
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // define changes for schedule
                Map<QName, Serializable> changes = new HashMap<QName, Serializable>();
                List<String> events = new ArrayList<String>(2);
                events.add("case_complete");
                changes.put(RecordsManagementModel.PROP_DISPOSITION_EVENT, (Serializable)events);
                
                // update the first dispostion action definition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Removing no_longer_needed event from 1st step: " + 
                            actionDefs.get(0).getName());
                updateDispositionActionDefinition(schedule, actionDefs.get(0), changes);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle asOf date is still null and ensure there is only one event
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);
                
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                assertNull("Expecting asOfDate to be null", asOfDate);
                
                // make sure only 1 event is present
                List<ChildAssociationRef> events = nodeService.getChildAssocs(ndNodeRef, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL);
                assertEquals(1, events.size());
                NodeRef event = events.get(0).getChildRef();
                assertEquals("case_complete", nodeService.getProperty(event, PROP_EVENT_EXECUTION_NAME));
                
                // Check for the search properties having been populated
                checkSearchAspect(recordFolder, false);
                
                return null;
            }          
        });
    }
    
    /**
     * Tests the re-scheduling of disposition lifecycles when the schedule changes 
     */
    public void testDispositionLifecycle_0318_reschedule_recordlevel() throws Exception
    {
        final NodeRef recordSeries = TestUtilities.getRecordSeries(rmService, nodeService, "Reports"); 
        setComplete();
        endTransaction();
        
        // create a category
        final NodeRef recordCategory = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordSeries);
                assertEquals("Reports", nodeService.getProperty(recordSeries, ContentModel.PROP_NAME));
                        
                return createRecordCategoryNode(recordSeries);
            }          
        });
        
        // define the disposition schedule for the category (Cut off monthly, hold 1 month, then destroy)
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertNotNull(recordCategory);
                
                // get the disposition schedule and turn on record level disposition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_INSTRUCTIONS, "Cutoff after 1 month then destroy after 1 month");
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_AUTHORITY, "Alfresco");
                nodeService.setProperty(schedule.getNodeRef(), PROP_RECORD_LEVEL_DISPOSITION, true);
                
                // define properties for both steps
                Map<QName, Serializable> step1 = new HashMap<QName, Serializable>();
                step1.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "cutoff");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Cutoff after 1 month");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|1");
                
                Map<QName, Serializable> step2 = new HashMap<QName, Serializable>();
                step2.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "destroy");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Destroy after 1 month");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|1");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY, PROP_CUT_OFF_DATE);
                
                // add the action definitions to the schedule
                dispositionService.addDispositionActionDefinition(schedule, step1);
                dispositionService.addDispositionActionDefinition(schedule, step2);
                
                return null;
            }          
        });
        
        // create a record folder
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecordFolder(recordCategory, "Folder1");
            }          
        });
        
        // create a record
        final NodeRef record = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordFolder);
                
                // Create the document
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, "MyRecord.txt");
                NodeRef record = nodeService.createNode(recordFolder, 
                                                           ContentModel.ASSOC_CONTAINS, 
                                                           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyRecord.txt"), 
                                                           ContentModel.TYPE_CONTENT).getChildRef();
                
                // Set the content
                ContentWriter writer = contentService.getWriter(record, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.setEncoding("UTF-8");
                writer.putContent("There is some content in this record");
                
                return record;
            }          
        });
        
        // make sure the disposition lifecycle is present and correct on the record and not on the folder
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @SuppressWarnings("deprecation")
            public Object execute() throws Throwable
            {
                assertNotNull(record);
                
                assertFalse(nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(record, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);        
                
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                System.out.println("Disposition action id: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION_ID));
                assertEquals("cutoff", nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                System.out.println("Disposition action: " + nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION));
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                System.out.println("Disposition as of: " + asOfDate);
                
                // make sure the as of date is a month in the future
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 1);
                int monthThen = cal.get(Calendar.MONTH);
                assertEquals(asOfDate.getMonth(), monthThen);
                
                // make sure there aren't any events
                List<ChildAssociationRef> events = nodeService.getChildAssocs(ndNodeRef, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL);
                assertEquals(0, events.size());
                
                // Check for the search properties having been populated
                checkSearchAspect(record);
                
                return null;
            }          
        });
        
        // change the period on the 1st step of the disposition schedule and make sure it perculates down
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // define changes for schedule
                Map<QName, Serializable> changes = new HashMap<QName, Serializable>();
                changes.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|3");
                
                // update the second dispostion action definition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Adding 3 months to period for 1st step: " + actionDefs.get(0).getName());
                updateDispositionActionDefinition(schedule, actionDefs.get(0), changes);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle asOf date has been updated
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @SuppressWarnings("deprecation")
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(record, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);        
                
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                System.out.println("Disposition as of: " + asOfDate);
                
                // make sure the as of date is a month in the future
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, 3);
                assertEquals(asOfDate.getMonth(), calendar.get(Calendar.MONTH));
                
                // Check for the search properties having been populated
                checkSearchAspect(record);
                
                return null;
            }          
        });
        
        // change the period on the 2nd step of the disposition schedule and make sure it DOES NOT perculate down
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // define changes for schedule
                Map<QName, Serializable> changes = new HashMap<QName, Serializable>();
                changes.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|6");
                
                // update the first dispostion action definition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Adding 6 months to period for 2nd step: " + actionDefs.get(1).getName());
                updateDispositionActionDefinition(schedule, actionDefs.get(1), changes);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle asOf date has NOT been updated as the period was 
        // changed for a step other than the current one
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @SuppressWarnings("deprecation")
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(record, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);        
                
                assertNotNull(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF));
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                System.out.println("Disposition as of: " + asOfDate);
                
                // make sure the as of date is a month in the future
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, 3);
                assertEquals("Expecting the asOf date to be unchanged",  asOfDate.getMonth(), calendar.get(Calendar.MONTH));                
                
                // Check for the search properties having been populated
                checkSearchAspect(record);
                
                return null;
            }          
        });
        
        // change the disposition schedule to be event based rather than time based i.e.
        // remove the period properties and supply 2 events in its place.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // define changes for schedule
                Map<QName, Serializable> changes = new HashMap<QName, Serializable>();
                changes.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, null);
                List<String> events = new ArrayList<String>(2);
                events.add("no_longer_needed");
                events.add("case_complete");
                changes.put(RecordsManagementModel.PROP_DISPOSITION_EVENT, (Serializable)events);
                
                // update the first dispostion action definition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Removing period and adding no_longer_needed and case_complete to 1st step: " + 
                            actionDefs.get(0).getName());
                updateDispositionActionDefinition(schedule, actionDefs.get(0), changes);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle asOf date has been reset and there are now
        // events hanging off the nextdispositionaction node
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(record, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);
                
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                System.out.println("New disposition as of: " + asOfDate);
                assertNull("Expecting asOfDate to be null", asOfDate);
                
                // make sure the 2 events are present
                List<ChildAssociationRef> events = nodeService.getChildAssocs(ndNodeRef, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL);
                assertEquals(2, events.size());
                NodeRef event1 = events.get(0).getChildRef();
                assertEquals("no_longer_needed", nodeService.getProperty(event1, PROP_EVENT_EXECUTION_NAME));
                NodeRef event2 = events.get(1).getChildRef();
                assertEquals("case_complete", nodeService.getProperty(event2, PROP_EVENT_EXECUTION_NAME));
                
                // Check for the search properties having been populated
                checkSearchAspect(record, false);
                
                return null;
            }          
        });
        
        // remove one of the events just added
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // define changes for schedule
                Map<QName, Serializable> changes = new HashMap<QName, Serializable>();
                List<String> events = new ArrayList<String>(2);
                events.add("case_complete");
                changes.put(RecordsManagementModel.PROP_DISPOSITION_EVENT, (Serializable)events);
                
                // update the first dispostion action definition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Removing no_longer_needed event from 1st step: " + 
                            actionDefs.get(0).getName());
                updateDispositionActionDefinition(schedule, actionDefs.get(0), changes);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle asOf date is still null and ensure there is only one event
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(record, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);
                
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                assertNull("Expecting asOfDate to be null", asOfDate);
                
                // make sure only 1 event is present
                List<ChildAssociationRef> events = nodeService.getChildAssocs(ndNodeRef, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL);
                assertEquals(1, events.size());
                NodeRef event = events.get(0).getChildRef();
                assertEquals("case_complete", nodeService.getProperty(event, PROP_EVENT_EXECUTION_NAME));
                
                // Check for the search properties having been populated
                checkSearchAspect(record, false);
                
                return null;
            }          
        });
        
        // change the action on the first step from 'cutoff' to 'retain'
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // define changes for schedule
                Map<QName, Serializable> changes = new HashMap<QName, Serializable>();
                changes.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "retain");
                
                // update the first dispostion action definition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Changing action of 1st step from '" + 
                            actionDefs.get(0).getName() + "' to 'retain'");
                updateDispositionActionDefinition(schedule, actionDefs.get(0), changes);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle asOf date is still null, ensure there is still only one event
        // and most importantly that the action name is now 'retain'
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef ndNodeRef = nodeService.getChildAssocs(record, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(ndNodeRef);
                
                Date asOfDate = (Date)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF);
                assertNull("Expecting asOfDate to be null", asOfDate);
                
                // make sure only 1 event is present
                List<ChildAssociationRef> events = nodeService.getChildAssocs(ndNodeRef, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL);
                assertEquals(1, events.size());
                NodeRef event = events.get(0).getChildRef();
                assertEquals("case_complete", nodeService.getProperty(event, PROP_EVENT_EXECUTION_NAME));
                
                String actionName = (String)nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_ACTION);
                assertEquals("retain", actionName);
                
                // Check for the search properties having been populated
                checkSearchAspect(record, false);
                
                return null;
            }          
        });
    }
    
    private void updateDispositionActionDefinition(DispositionSchedule schedule, DispositionActionDefinition actionDefinition, Map<QName, Serializable> actionDefinitionParams)
    {
        NodeRef nodeRef = actionDefinition.getNodeRef();
        Map<QName, Serializable> before = nodeService.getProperties(nodeRef);
        nodeService.addProperties(nodeRef, actionDefinitionParams);
        Map<QName, Serializable> after = nodeService.getProperties(nodeRef);
        List<QName> updatedProps = determineChangedProps(before, after);
        
        refreshDispositionActionDefinition(nodeRef, updatedProps);
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
    
    private List<QName> determineChangedProps(Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        List<QName> result = new ArrayList<QName>();
        for (QName qn : oldProps.keySet())
        {
            if (newProps.get(qn) == null ||
                newProps.get(qn).equals(oldProps.get(qn)) == false)
            {
                result.add(qn);
            }
        }
        for (QName qn : newProps.keySet())
        {
            if (oldProps.get(qn) == null)
            {
                result.add(qn);
            }
        }
        
        return result;
    }
    
    /**
     * Tests the re-scheduling of disposition lifecycles when steps from the schedule are deleted
     * (when using folder level disposition)
     */
    public void testDispositionLifecycle_0318_reschedule_deletion_folderlevel() throws Exception
    {
        final NodeRef recordSeries = TestUtilities.getRecordSeries(rmService, nodeService, "Reports"); 
        setComplete();
        endTransaction();
        
        // create a category
        final NodeRef recordCategory = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordSeries);
                assertEquals("Reports", nodeService.getProperty(recordSeries, ContentModel.PROP_NAME));
                        
                return createRecordCategoryNode(recordSeries);
            }          
        });
        
        // define the disposition schedule for the category with several steps
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertNotNull(recordCategory);
                
                // define properties for both steps
                Map<QName, Serializable> step1 = new HashMap<QName, Serializable>();
                step1.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "cutoff");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Cutoff when no longer needed");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_EVENT, "no_longer_needed");
                
                Map<QName, Serializable> step2 = new HashMap<QName, Serializable>();
                step2.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "transfer");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Transfer after 1 month");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|1");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY, PROP_DISPOSITION_AS_OF);
                
                Map<QName, Serializable> step3 = new HashMap<QName, Serializable>();
                step3.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "destroy");
                step3.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Destroy after 1 year");
                step3.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "year|1");
                step3.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY, PROP_DISPOSITION_AS_OF);
                
                // add the action definitions to the schedule
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                dispositionService.addDispositionActionDefinition(schedule, step1);
                dispositionService.addDispositionActionDefinition(schedule, step2);
                dispositionService.addDispositionActionDefinition(schedule, step3);
                
                return null;
            }          
        });
        
        // create first record folder
        final NodeRef recordFolder1 = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecordFolder(recordCategory, "Folder1");
            }          
        });
        
        // create second record folder
        final NodeRef recordFolder2 = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecordFolder(recordCategory, "Folder2");
            }          
        });
        
        // make sure the disposition lifecycle is present and correct
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertNotNull(recordFolder1);
                assertNotNull(recordFolder2);
                
                assertTrue(nodeService.hasAspect(recordFolder1, ASPECT_DISPOSITION_LIFECYCLE));
                assertTrue(nodeService.hasAspect(recordFolder2, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef folder1NextAction = nodeService.getChildAssocs(recordFolder1, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder1NextAction);
                NodeRef folder2NextAction = nodeService.getChildAssocs(recordFolder2, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder2NextAction);
                
                // make sure both folders are on the cutoff step
                assertEquals("cutoff", nodeService.getProperty(folder1NextAction, PROP_DISPOSITION_ACTION));
                assertEquals("cutoff", nodeService.getProperty(folder2NextAction, PROP_DISPOSITION_ACTION));
                
                // make sure both folders have 1 event
                assertEquals(1, nodeService.getChildAssocs(folder1NextAction, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL).size());
                assertEquals(1, nodeService.getChildAssocs(folder2NextAction, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL).size());
                
                // move folder 2 onto next step
                Map<String, Serializable> params = new HashMap<String, Serializable>(3);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, "no_longer_needed");
                params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
                params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, "gavinc");
                
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                rmActionService.executeRecordsManagementAction(recordFolder2, "completeEvent", params);
                rmActionService.executeRecordsManagementAction(recordFolder2, "cutoff");
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                
                return null;
            }          
        });
        
        // check the second folder is at step 2 and then attempt to remove a step from the disposition schedule
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                NodeRef folder2NextAction = nodeService.getChildAssocs(recordFolder2, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder2NextAction);
                assertEquals("transfer", (String)nodeService.getProperty(folder2NextAction, PROP_DISPOSITION_ACTION));
                
                // check there are 3 steps to the schedule
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(3, actionDefs.size());
                
                // attempt to remove step 1 from the schedule
                try
                {
                    dispositionService.removeDispositionActionDefinition(schedule, actionDefs.get(0));
                    fail("Expecting the step deletion to be unsuccessful as record folders are present");
                }
                catch (AlfrescoRuntimeException are)
                {
                    // expected as steps are present, deletion not allowed
                }
                
                return null;
            }          
        });
        
        // remove both record folders
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // remove record folders
                nodeService.removeChild(recordCategory, recordFolder1);
                nodeService.removeChild(recordCategory, recordFolder2);
                return null;
            }          
        });
        
        // try removing last schedule step 
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // make sure there are 3 steps
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(3, actionDefs.size());
                
                // remove last step, should be successful this time
                dispositionService.removeDispositionActionDefinition(schedule, actionDefs.get(2));
                
                // make sure there are now 2 steps
                schedule = dispositionService.getDispositionSchedule(recordCategory);
                actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                
                return null;
            }          
        });
        
        // *** NOTE: The commented out code below is potential tests for the step deletion behaviour ***
        // ***       we also need to add tests for deleting the step in the process where records or ***
        // ***       folders are on the last step i.e. what state should they be in if the last step ***
        // ***       is removed?                                                                     ***
        
        /*
        // check the second folder is at step 2 and remove the first step from the schedule
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                NodeRef folder2NextAction = nodeService.getChildAssocs(recordFolder2, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder2NextAction);
                assertEquals("transfer", (String)nodeService.getProperty(folder2NextAction, PROP_DISPOSITION_ACTION));
                
                // remove step 1 from the schedule
                DispositionSchedule schedule = rmService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(3, actionDefs.size());
                System.out.println("Removing schedule step 1 named: " + actionDefs.get(0).getName());
                rmService.removeDispositionActionDefinition(schedule, actionDefs.get(0));
                
                return null;
            }          
        });
        
        // make sure the next action for folder 1 has moved on and folder 2 is unchanged, then delete last step
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                NodeRef folder1NextAction = nodeService.getChildAssocs(recordFolder1, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder1NextAction);
                NodeRef folder2NextAction = nodeService.getChildAssocs(recordFolder2, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder2NextAction);
                
                // make sure both folders are on the cutoff step
                assertEquals("transfer", nodeService.getProperty(folder1NextAction, PROP_DISPOSITION_ACTION));
                assertEquals("transfer", nodeService.getProperty(folder2NextAction, PROP_DISPOSITION_ACTION));
                
                // Check for the search properties having been populated
                checkSearchAspect(folder1NextAction);
                checkSearchAspect(folder2NextAction);
                
                // remove the step in the last position from the schedule
                DispositionSchedule schedule = rmService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                System.out.println("Removing schedule last step named: " + actionDefs.get(1).getName());
                rmService.removeDispositionActionDefinition(schedule, actionDefs.get(1));
                
                return null;
            }          
        });
        
        // check there were no changes, then remove the only remaining step
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                NodeRef folder1NextAction = nodeService.getChildAssocs(recordFolder1, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder1NextAction);
                assertEquals("transfer", (String)nodeService.getProperty(folder1NextAction, PROP_DISPOSITION_ACTION));
                
                NodeRef folder2NextAction = nodeService.getChildAssocs(recordFolder2, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder2NextAction);
                assertEquals("transfer", (String)nodeService.getProperty(folder2NextAction, PROP_DISPOSITION_ACTION));
                
                // remove last remaining step from the schedule
                DispositionSchedule schedule = rmService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(1, actionDefs.size());
                System.out.println("Removing last remaining schedule step named: " + actionDefs.get(0).getName());
                rmService.removeDispositionActionDefinition(schedule, actionDefs.get(0));
                
                return null;
            }          
        });
        
        // check there are no schedule steps left and that both folders no longer have the disposition lifecycle aspect,
        // then add a new step
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertEquals(0, nodeService.getChildAssocs(recordFolder1, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).size());
                assertEquals(0, nodeService.getChildAssocs(recordFolder2, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).size());
                assertFalse(nodeService.hasAspect(recordFolder1, ASPECT_DISPOSITION_LIFECYCLE));
                assertFalse(nodeService.hasAspect(recordFolder2, ASPECT_DISPOSITION_LIFECYCLE));
                
                // ensure schedule is empty
                DispositionSchedule schedule = rmService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(0, actionDefs.size());
                
                // add a new step
                Map<QName, Serializable> step1 = new HashMap<QName, Serializable>();
                step1.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "retain");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Retain for 25 years");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "year|25");
                rmService.addDispositionActionDefinition(schedule, step1);
                
                return null;
            }          
        });
        
        // check both folders now have the retain action
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                NodeRef folder1NextAction = nodeService.getChildAssocs(recordFolder1, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder1NextAction);
                assertEquals("retain", (String)nodeService.getProperty(folder1NextAction, PROP_DISPOSITION_ACTION));
                assertNotNull(nodeService.getProperty(folder1NextAction, PROP_DISPOSITION_AS_OF));
                
                NodeRef folder2NextAction = nodeService.getChildAssocs(recordFolder2, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(folder2NextAction);
                assertEquals("retain", (String)nodeService.getProperty(folder2NextAction, PROP_DISPOSITION_ACTION));
                assertNotNull(nodeService.getProperty(folder2NextAction, PROP_DISPOSITION_AS_OF));
                
                return null;
            }          
        });
        */
    }
    
    /**
     * Tests the re-scheduling of disposition lifecycles when steps from the schedule are deleted
     * (when using record level disposition)
     */
    public void testDispositionLifecycle_0318_reschedule_deletion_recordlevel() throws Exception
    {
        final NodeRef recordSeries = TestUtilities.getRecordSeries(rmService, nodeService, "Reports"); 
        setComplete();
        endTransaction();
        
        // create a category
        final NodeRef recordCategory = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordSeries);
                assertEquals("Reports", nodeService.getProperty(recordSeries, ContentModel.PROP_NAME));
                        
                return createRecordCategoryNode(recordSeries);
            }          
        });
        
        // define the disposition schedule for the category with several steps
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertNotNull(recordCategory);
                
                // get the disposition schedule and turn on record level disposition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                nodeService.setProperty(schedule.getNodeRef(), PROP_RECORD_LEVEL_DISPOSITION, true);
                
                // define properties for both steps
                Map<QName, Serializable> step1 = new HashMap<QName, Serializable>();
                step1.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "cutoff");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Cutoff when no longer needed");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_EVENT, "no_longer_needed");
                
                Map<QName, Serializable> step2 = new HashMap<QName, Serializable>();
                step2.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "transfer");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Transfer after 1 month");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|1");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY, PROP_DISPOSITION_AS_OF);
                
                Map<QName, Serializable> step3 = new HashMap<QName, Serializable>();
                step3.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "destroy");
                step3.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Destroy after 1 year");
                step3.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "year|1");
                step3.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY, PROP_DISPOSITION_AS_OF);
                
                // add the action definitions to the schedule
                dispositionService.addDispositionActionDefinition(schedule, step1);
                dispositionService.addDispositionActionDefinition(schedule, step2);
                dispositionService.addDispositionActionDefinition(schedule, step3);
                
                return null;
            }          
        });
        
        // create first record folder
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecordFolder(recordCategory, "Record Folder");
            }
        });
        
        // create a record
        final NodeRef record = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordFolder);
                
                // Create the document
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, "MyRecord.txt");
                NodeRef record = nodeService.createNode(recordFolder, 
                                                           ContentModel.ASSOC_CONTAINS, 
                                                           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyRecord.txt"), 
                                                           ContentModel.TYPE_CONTENT).getChildRef();
                
                // Set the content
                ContentWriter writer = contentService.getWriter(record, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.setEncoding("UTF-8");
                writer.putContent("There is some content in this record");
                
                return record;
            }          
        });
        
        // make sure the disposition lifecycle is present and correct
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertNotNull(record);
                
                assertTrue(nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE));
                NodeRef recordNextAction = nodeService.getChildAssocs(record, ASSOC_NEXT_DISPOSITION_ACTION, 
                            RegexQNamePattern.MATCH_ALL).get(0).getChildRef();
                assertNotNull(recordNextAction);
                
                // make sure the record is on the cutoff step
                assertEquals("cutoff", nodeService.getProperty(recordNextAction, PROP_DISPOSITION_ACTION));
                
                // make sure the record has 1 event
                assertEquals(1, nodeService.getChildAssocs(recordNextAction, ASSOC_EVENT_EXECUTIONS,
                            RegexQNamePattern.MATCH_ALL).size());
                
                return null;
            }          
        });
        
        // check for steps in schedule then attempt to delete one
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // check there are 3 steps to the schedule
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(3, actionDefs.size());
                
                // attempt to remove step 1 from the schedule
                try
                {
                    dispositionService.removeDispositionActionDefinition(schedule, actionDefs.get(0));
                    fail("Expecting the step deletion to be unsuccessful as records are present");
                }
                catch (AlfrescoRuntimeException are)
                {
                    // expected as steps are present, deletion not allowed
                }
                
                return null;
            }          
        });
        
        // remove the record (the folder can stay)
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // remove record folders
                nodeService.removeChild(recordFolder, record);
                return null;
            }          
        });
        
        // try removing last schedule step 
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // make sure there are 3 steps
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                List<DispositionActionDefinition> actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(3, actionDefs.size());
                
                // remove last step, should be successful this time
                dispositionService.removeDispositionActionDefinition(schedule, actionDefs.get(2));
                
                // make sure there are now 2 steps
                schedule = dispositionService.getDispositionSchedule(recordCategory);
                actionDefs = schedule.getDispositionActionDefinitions();
                assertEquals(2, actionDefs.size());
                
                return null;
            }          
        });
    }
    
    /**
     * test a dispostion schedule being setup after a record folder and record
     */
    public void testDispositionLifecycle_0318_existingfolders() throws Exception
    {
        final NodeRef recordSeries = TestUtilities.getRecordSeries(rmService, nodeService, "Reports"); 
        setComplete();
        endTransaction();
        
        // create a category
        final NodeRef recordCategory = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordSeries);
                assertEquals("Reports", nodeService.getProperty(recordSeries, ContentModel.PROP_NAME));
                        
                return createRecordCategoryNode(recordSeries);
            }          
        });
        
        // create a record folder
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordCategory);
                return createRecordFolder(recordCategory, "Folder1");
            }          
        });
        
        // define the disposition schedule for the category (Cut off monthly, hold 1 month, then destroy)
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertNotNull(recordFolder);
                
                // define properties for both steps
                Map<QName, Serializable> step1 = new HashMap<QName, Serializable>();
                step1.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "cutoff");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Cutoff after 1 month");
                step1.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|1");
                
                Map<QName, Serializable> step2 = new HashMap<QName, Serializable>();
                step2.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME, "destroy");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_DESCRIPTION, "Destroy after 1 month");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD, "month|1");
                step2.put(RecordsManagementModel.PROP_DISPOSITION_PERIOD_PROPERTY, PROP_CUT_OFF_DATE);
                
                // add the action definitions to the schedule
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                
                NodeRef temp = dispositionService.addDispositionActionDefinition(schedule, step1).getNodeRef();
                List<QName> updatedProps = new ArrayList<QName>(step1.keySet());
                refreshDispositionActionDefinition(temp, updatedProps);
                
                temp = dispositionService.addDispositionActionDefinition(schedule, step2).getNodeRef();
                updatedProps = new ArrayList<QName>(step2.keySet());
                refreshDispositionActionDefinition(temp, updatedProps);
                
                return null;
            }          
        });
        
        // make sure the disposition lifecycle is present and correct
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            @SuppressWarnings("deprecation")
            public Object execute() throws Throwable
            {
                assertNotNull(recordFolder);
                
                DispositionAction da = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(da);
                
                assertNotNull(da.getDispositionActionDefinition());
                assertNotNull(da.getDispositionActionDefinition().getId());
                assertEquals("cutoff", da.getName());
                Date asOfDate = da.getAsOfDate();
                assertNotNull(asOfDate);
                
                // make sure the as of date is a month in the future
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, 1);
                int monthThen = cal.get(Calendar.MONTH);
                assertEquals(asOfDate.getMonth(), monthThen);
                
                // make sure there aren't any events
                assertEquals(0, da.getEventCompletionDetails().size());
                
                // Check for the search properties having been populated
                checkSearchAspect(recordFolder);
                
                return null;
            }          
        });
    }
    
    /**
     * Test the updating of a disposition schedule using folder level disposition
     */
    public void testFolderLevelDispositionScheduleUpdate() throws Exception
    {
        final NodeRef recordSeries = TestUtilities.getRecordSeries(rmService, nodeService, "Reports"); 
        setComplete();
        endTransaction();
        
        // create a category
        final NodeRef recordCategory = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordSeries);
                assertEquals("Reports", nodeService.getProperty(recordSeries, ContentModel.PROP_NAME));
                        
                return createRecordCategoryNode(recordSeries);
            }          
        });
        
        // define the disposition schedule for the category (Cut off monthly, hold 1 month, then destroy)
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertNotNull(recordCategory);
                
                // get the disposition schedule and turn on record level disposition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_INSTRUCTIONS, "Cutoff after 1 month then destroy after 1 month");
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_AUTHORITY, "Alfresco");
                
                return null;
            }          
        });
        
        // create a record folder
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecordFolder(recordCategory, "Folder1");
            }          
        });
        
        // check the created folder has the correctly populated search aspect, then update the schedule
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // check the folder has the search aspect
                assertNotNull(recordFolder);
                checkSearchAspect(recordFolder, false);
                
                // update the disposition schedule
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_INSTRUCTIONS, "Cutoff immediately when case is closed then destroy after 1 year");
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_AUTHORITY, "DoD");
                
                return null;
            }          
        });
        
        // check the search aspect has been kept in sync
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // check the folder has the search aspect
                checkSearchAspect(recordFolder, false);
                
                return null;
            }          
        });
    }
    
    /**
     * Test the updating of a disposition schedule using record level disposition
     */
    public void testRecordLevelDispositionScheduleUpdate() throws Exception
    {
        final NodeRef recordSeries = TestUtilities.getRecordSeries(rmService, nodeService, "Reports"); 
        setComplete();
        endTransaction();
        
        // create a category
        final NodeRef recordCategory = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordSeries);
                assertEquals("Reports", nodeService.getProperty(recordSeries, ContentModel.PROP_NAME));
                        
                return createRecordCategoryNode(recordSeries);
            }          
        });
        
        // define the disposition schedule for the category (Cut off monthly, hold 1 month, then destroy)
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertNotNull(recordCategory);
                
                // get the disposition schedule and turn on record level disposition
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_INSTRUCTIONS, "Cutoff after 1 month then destroy after 1 month");
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_AUTHORITY, "Alfresco");
                nodeService.setProperty(schedule.getNodeRef(), PROP_RECORD_LEVEL_DISPOSITION, true);
                
                return null;
            }          
        });
        
        // create a record folder
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecordFolder(recordCategory, "Folder1");
            }          
        });
        
        // create a record
        final NodeRef record = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertNotNull(recordFolder);
                
                // Create the document
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, "MyRecord.txt");
                NodeRef record = nodeService.createNode(recordFolder, 
                                                           ContentModel.ASSOC_CONTAINS, 
                                                           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyRecord.txt"), 
                                                           ContentModel.TYPE_CONTENT).getChildRef();
                
                // Set the content
                ContentWriter writer = contentService.getWriter(record, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                writer.setEncoding("UTF-8");
                writer.putContent("There is some content in this record");
                
                return record;
            }          
        });
        
        // check the created folder has the correctly populated search aspect, then update the schedule
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // check the record has the search aspect
                assertNotNull(record);
                checkSearchAspect(record, false);
                
                // update the disposition schedule
                DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordCategory);
                assertNotNull(schedule);
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_INSTRUCTIONS, "Cutoff immediately when case is closed then destroy after 1 year");
                nodeService.setProperty(schedule.getNodeRef(), PROP_DISPOSITION_AUTHORITY, "DoD");
                
                return null;
            }          
        });
        
        // check the search aspect has been kept in sync
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // check the record has the search aspect
                checkSearchAspect(record, false);
                
                return null;
            }          
        });
    }
    
    public void testUnCutoff()
    {      
        final NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records"); 
        setComplete();
        endTransaction();
                
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                   
                assertNotNull(recordCategory);
                assertEquals("AIS Audit Records", nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
                        
                return createRecordFolder(recordCategory, "March AIS Audit Records");                        
            }          
        });
        
        final NodeRef recordOne = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder);
            }          
        }); 
        
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                TestUtilities.declareRecord(recordOne, unprotectedNodeService, rmActionService);
                
                // Clock the asOf date back to ensure eligibility
                NodeRef ndNodeRef = nodeService.getChildAssocs(recordFolder, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();     
                Date nowDate = calendar.getTime();
                assertFalse(nowDate.equals(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF)));
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(EditDispositionActionAsOfDateAction.PARAM_AS_OF_DATE, nowDate);                
                rmActionService.executeRecordsManagementAction(recordFolder, "editDispositionActionAsOfDate", params);
                assertTrue(nowDate.equals(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF)));
                
                // Cut off
                rmActionService.executeRecordsManagementAction(recordFolder, "cutoff", null);
                
                // Check that everything appears to be cutoff
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_CUT_OFF));
                List<NodeRef> records = rmService.getRecords(recordFolder);
                for (NodeRef record : records)
                {
                    assertTrue(nodeService.hasAspect(record, ASPECT_CUT_OFF));
                }
                DispositionAction da = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(da);
                assertFalse("cutoff".equals(da.getName()));
                checkLastDispositionAction(recordFolder, "cutoff", 1);
                                
                // Revert the cutoff
                rmActionService.executeRecordsManagementAction(recordFolder, "unCutoff", null);
                
                // Check that everything has been reverted
                assertFalse(nodeService.hasAspect(recordFolder, ASPECT_CUT_OFF));
                records = rmService.getRecords(recordFolder);
                for (NodeRef record : records)
                {
                    assertFalse(nodeService.hasAspect(record, ASPECT_CUT_OFF));
                }
                da = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(da);
                assertTrue("cutoff".equals(da.getName()));
                assertNull(da.getStartedAt());
                assertNull(da.getStartedBy());
                assertNull(da.getCompletedAt());
                assertNull(da.getCompletedBy());
                List<DispositionAction> history = dispositionService.getCompletedDispositionActions(recordFolder);
                assertNotNull(history);
                assertEquals(0, history.size());
                
                return null;
            }          
        });

    }
    
    private void checkLastDispositionAction(NodeRef nodeRef, String daName, int expectedCount)
    {
        // Check the previous action details
        List<DispositionAction> history = dispositionService.getCompletedDispositionActions(nodeRef);
        assertNotNull(history);
        assertEquals(expectedCount, history.size());
        DispositionAction lastDA = history.get(history.size()-1);
        assertEquals(daName, lastDA.getName());
        assertNotNull(lastDA.getStartedAt());
        assertNotNull(lastDA.getStartedBy());
        assertNotNull(lastDA.getCompletedAt());
        assertNotNull(lastDA.getCompletedBy());
        // Check the "get last" method
        lastDA = dispositionService.getLastCompletedDispostionAction(nodeRef);
        assertEquals(daName, lastDA.getName());        
    }
    
    public void testFreeze() throws Exception
    {      
        final NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");    
        assertNotNull(recordCategory);
        assertEquals("AIS Audit Records", this.nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
        
        // Before we start just remove any outstanding holds
        final NodeRef rootNode = this.rmService.getFilePlan(recordCategory);
        List<ChildAssociationRef> tempAssocs = this.nodeService.getChildAssocs(rootNode, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef tempAssoc : tempAssocs)
        {
            this.nodeService.deleteNode(tempAssoc.getChildRef());
        }
        
        final NodeRef recordFolder = createRecordFolder(recordCategory, "March AIS Audit Records");
        
        setComplete();
        endTransaction();
        
        final NodeRef recordOne = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder, "one.txt");
            }          
        });
        final NodeRef recordTwo = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder, "two.txt");
            }          
        });
        final NodeRef recordThree = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder, "three.txt");
            }          
        });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_RECORD));
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_FILE_PLAN_COMPONENT));
                
                // Freeze the record
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FreezeAction.PARAM_REASON, "reason1");
                rmActionService.executeRecordsManagementAction(recordOne, "freeze", params);
                
                return null;
            }          
        });        
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Check the hold exists 
                List<ChildAssociationRef> holdAssocs = nodeService.getChildAssocs(rootNode, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
                assertNotNull(holdAssocs);
                assertEquals(1, holdAssocs.size());        
                NodeRef holdNodeRef = holdAssocs.get(0).getChildRef();
                assertEquals("reason1", nodeService.getProperty(holdNodeRef, PROP_HOLD_REASON));
                List<ChildAssociationRef> freezeAssocs = nodeService.getChildAssocs(holdNodeRef);
                assertNotNull(freezeAssocs);
                assertEquals(1, freezeAssocs.size());
                
                // Check the nodes are frozen
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_FROZEN));
                assertNotNull(nodeService.getProperty(recordOne, PROP_FROZEN_AT));
                assertNotNull(nodeService.getProperty(recordOne, PROP_FROZEN_BY));
                assertFalse(nodeService.hasAspect(recordTwo, ASPECT_FROZEN));
                assertFalse(nodeService.hasAspect(recordThree, ASPECT_FROZEN));
                
                // check the records have the hold reason reflected on the search aspect
                assertEquals("reason1", nodeService.getProperty(recordOne, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertNull(nodeService.getProperty(recordTwo, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertNull(nodeService.getProperty(recordThree, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                
                // Update the freeze reason
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FreezeAction.PARAM_REASON, "reason1changed");
                rmActionService.executeRecordsManagementAction(holdNodeRef, "editHoldReason", params);
                
                // Check the hold has been updated
                String updatedHoldReason = (String)nodeService.getProperty(holdNodeRef, PROP_HOLD_REASON);
                assertEquals("reason1changed", updatedHoldReason);
                
                return null;
            }
        });
           
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // check the search fields on the records have also been updated 
                assertEquals("reason1changed", nodeService.getProperty(recordOne, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertNull(nodeService.getProperty(recordTwo, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertNull(nodeService.getProperty(recordThree, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                
                // Freeze a number of records
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(FreezeAction.PARAM_REASON, "reason2");
                List<NodeRef> records = new ArrayList<NodeRef>(2);
                records.add(recordOne);
                records.add(recordTwo);
                records.add(recordThree);
                rmActionService.executeRecordsManagementAction(records, "freeze", params);
                
                return null;
            }          
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Check the holds exist
                List<ChildAssociationRef> holdAssocs = nodeService.getChildAssocs(rootNode, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
                assertNotNull(holdAssocs);
                assertEquals(2, holdAssocs.size());
                for (ChildAssociationRef holdAssoc : holdAssocs)
                {
                    String reason = (String)nodeService.getProperty(holdAssoc.getChildRef(), PROP_HOLD_REASON);
                    if (reason.equals("reason2") == true)
                    {
                        List<ChildAssociationRef> freezeAssocs = nodeService.getChildAssocs(holdAssoc.getChildRef());
                        assertNotNull(freezeAssocs);
                        assertEquals(3, freezeAssocs.size());
                    }
                    else if (reason.equals("reason1changed") == true)
                    {
                        List<ChildAssociationRef> freezeAssocs = nodeService.getChildAssocs(holdAssoc.getChildRef());
                        assertNotNull(freezeAssocs);
                        assertEquals(1, freezeAssocs.size());
                    }
                }
                
                // Check the nodes are frozen
                final List<NodeRef> testRecords = Arrays.asList(new NodeRef[]{recordOne, recordTwo, recordThree});
                for (NodeRef nr : testRecords)
                {
                    assertTrue(nodeService.hasAspect(nr, ASPECT_FROZEN));
                    assertNotNull(nodeService.getProperty(nr, PROP_FROZEN_AT));
                    assertNotNull(nodeService.getProperty(nr, PROP_FROZEN_BY));
                    assertNotNull(nodeService.getProperty(nr, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                }
                
                // Unfreeze a node
                rmActionService.executeRecordsManagementAction(recordThree, "unfreeze");
                
                return null;
            }
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Check the holds
                List<ChildAssociationRef> holdAssocs = nodeService.getChildAssocs(rootNode, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
                assertNotNull(holdAssocs);
                assertEquals(2, holdAssocs.size());
                for (ChildAssociationRef holdAssoc : holdAssocs)
                {
                    String reason = (String)nodeService.getProperty(holdAssoc.getChildRef(), PROP_HOLD_REASON);
                    if (reason.equals("reason2") == true)
                    {
                        List<ChildAssociationRef> freezeAssocs = nodeService.getChildAssocs(holdAssoc.getChildRef());
                        assertNotNull(freezeAssocs);
                        assertEquals(2, freezeAssocs.size());
                    }
                    else if (reason.equals("reason1changed") == true)
                    {
                        List<ChildAssociationRef> freezeAssocs = nodeService.getChildAssocs(holdAssoc.getChildRef());
                        assertNotNull(freezeAssocs);
                        assertEquals(1, freezeAssocs.size());
                    }
                }
                
                // Check the nodes are frozen
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_FROZEN));
                assertNotNull(nodeService.getProperty(recordOne, PROP_FROZEN_AT));
                assertNotNull(nodeService.getProperty(recordOne, PROP_FROZEN_BY));
                assertEquals("reason2", nodeService.getProperty(recordOne, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertTrue(nodeService.hasAspect(recordTwo, ASPECT_FROZEN));
                assertNotNull(nodeService.getProperty(recordTwo, PROP_FROZEN_AT));
                assertNotNull(nodeService.getProperty(recordTwo, PROP_FROZEN_BY));
                assertEquals("reason2", nodeService.getProperty(recordTwo, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertFalse(nodeService.hasAspect(recordThree, ASPECT_FROZEN));
                assertNull(nodeService.getProperty(recordThree, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                
                return null;
            }
        });

        // Put the relinquish hold request into its own transaction
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
                {
                    public Object execute() throws Throwable
                    {
                        // Check the holds
                        List<ChildAssociationRef> holdAssocs = nodeService.getChildAssocs(rootNode, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
                        assertNotNull(holdAssocs);
                        assertEquals(2, holdAssocs.size());
                        // Relinquish the first hold
                        NodeRef holdNodeRef = holdAssocs.get(0).getChildRef();
                        assertEquals("reason1changed", nodeService.getProperty(holdNodeRef, PROP_HOLD_REASON));
                        
                        rmActionService.executeRecordsManagementAction(holdNodeRef, "relinquishHold");
                        
                        // Check the holds
                        holdAssocs = nodeService.getChildAssocs(rootNode, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
                        assertNotNull(holdAssocs);
                        assertEquals(1, holdAssocs.size());
                        holdNodeRef = holdAssocs.get(0).getChildRef();
                        assertEquals("reason2", nodeService.getProperty(holdNodeRef, PROP_HOLD_REASON));
                        List<ChildAssociationRef> freezeAssocs = nodeService.getChildAssocs(holdNodeRef);
                        assertNotNull(freezeAssocs);
                        assertEquals(2, freezeAssocs.size());
                        
                        return null;
                    }
                });

        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Check the nodes are frozen
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_FROZEN));
                assertNotNull(nodeService.getProperty(recordOne, PROP_FROZEN_AT));
                assertNotNull(nodeService.getProperty(recordOne, PROP_FROZEN_BY));
                // TODO: record one is still linked to a hold so should have the original hold reason 
                //       on the search aspect but we're presuming just one hold for now so the search hold
                //       reason will remain unchanged
                assertEquals("reason2", nodeService.getProperty(recordOne, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertTrue(nodeService.hasAspect(recordTwo, ASPECT_FROZEN));
                assertNotNull(nodeService.getProperty(recordTwo, PROP_FROZEN_AT));
                assertNotNull(nodeService.getProperty(recordTwo, PROP_FROZEN_BY));
                assertEquals("reason2", nodeService.getProperty(recordTwo, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertFalse(nodeService.hasAspect(recordThree, ASPECT_FROZEN));
                assertNull(nodeService.getProperty(recordThree, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                
                // Unfreeze
                rmActionService.executeRecordsManagementAction(recordOne, "unfreeze");
                
                return null;
            }
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Check the holds
                List<ChildAssociationRef> holdAssocs = nodeService.getChildAssocs(rootNode, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
                assertNotNull(holdAssocs);
                assertEquals(1, holdAssocs.size());
                NodeRef holdNodeRef = holdAssocs.get(0).getChildRef();
                assertEquals("reason2", nodeService.getProperty(holdNodeRef, PROP_HOLD_REASON));
                List<ChildAssociationRef> freezeAssocs = nodeService.getChildAssocs(holdNodeRef);
                assertNotNull(freezeAssocs);
                assertEquals(1, freezeAssocs.size());
                
                // Check the nodes are frozen
                assertFalse(nodeService.hasAspect(recordOne, ASPECT_FROZEN));
                assertNull(nodeService.getProperty(recordOne, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertTrue(nodeService.hasAspect(recordTwo, ASPECT_FROZEN));
                assertNotNull(nodeService.getProperty(recordTwo, PROP_FROZEN_AT));
                assertNotNull(nodeService.getProperty(recordTwo, PROP_FROZEN_BY));
                assertEquals("reason2", nodeService.getProperty(recordTwo, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertFalse(nodeService.hasAspect(recordThree, ASPECT_FROZEN));
                assertNull(nodeService.getProperty(recordThree, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                
                // Unfreeze
                rmActionService.executeRecordsManagementAction(recordTwo, "unfreeze");
                
                return null;
            }
        });
                
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // Check the holds
                List<ChildAssociationRef> holdAssocs = nodeService.getChildAssocs(rootNode, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
                assertNotNull(holdAssocs);
                assertEquals(0, holdAssocs.size());
                
                // Check the nodes are unfrozen
                assertFalse(nodeService.hasAspect(recordOne, ASPECT_FROZEN));
                assertFalse(nodeService.hasAspect(recordTwo, ASPECT_FROZEN));
                assertFalse(nodeService.hasAspect(recordThree, ASPECT_FROZEN));
                
                // check the search hold reason is null on all records
                assertNull(nodeService.getProperty(recordOne, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertNull(nodeService.getProperty(recordTwo, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                assertNull(nodeService.getProperty(recordThree, RecordsManagementSearchBehaviour.PROP_RS_HOLD_REASON));
                
                return null;
            }          
        });                  
    }
    
    public void testAutoSuperseded()
    {
        final NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Civilian Files", "Employee Performance File System Records");    
        assertNotNull(recordCategory);
        assertEquals("Employee Performance File System Records", this.nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
        
        final NodeRef recordFolder = createRecordFolder(recordCategory, "Test Record Folder");
        
        // Before we start just remove any outstanding transfers
        final NodeRef rootNode = this.rmService.getFilePlan(recordCategory);
        List<ChildAssociationRef> tempAssocs = this.nodeService.getChildAssocs(rootNode, ASSOC_TRANSFERS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef tempAssoc : tempAssocs)
        {
            this.nodeService.deleteNode(tempAssoc.getChildRef());
        }
        
        setComplete();
        endTransaction();
        
        final NodeRef recordOne = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder, "one.txt");
            }          
        });
        final NodeRef recordTwo = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder, "two.txt");
            }          
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_RECORD));
                
                TestUtilities.declareRecord(recordOne, unprotectedNodeService, rmActionService);
                TestUtilities.declareRecord(recordTwo, unprotectedNodeService, rmActionService);  
                
                return null;
            }
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                
                DispositionAction da = dispositionService.getNextDispositionAction(recordTwo);
                assertNotNull(da);
                assertEquals("cutoff", da.getName());
                assertFalse(da.isEventsEligible());
                List<EventCompletionDetails> events = da.getEventCompletionDetails();
                assertNotNull(events);
                assertEquals(1, events.size());
                EventCompletionDetails event = events.get(0);
                assertEquals("superseded", event.getEventName());
                assertFalse(event.isEventComplete());
                assertNull(event.getEventCompletedAt());
                assertNull(event.getEventCompletedBy());
                
                rmAdminService.addCustomReference(recordOne, recordTwo, QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "supersedes"));
                
                return null;
            }          
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                DispositionAction da = dispositionService.getNextDispositionAction(recordTwo);
                assertNotNull(da);
                assertEquals("cutoff", da.getName());
                assertTrue(da.isEventsEligible());
                List<EventCompletionDetails> events = da.getEventCompletionDetails();
                assertNotNull(events);
                assertEquals(1, events.size());
                EventCompletionDetails event = events.get(0);
                assertEquals("superseded", event.getEventName());
                assertTrue(event.isEventComplete());
                assertNotNull(event.getEventCompletedAt());
                assertNotNull(event.getEventCompletedBy());
                
                return null;
            }          
        });
    }
    
    public void testVersioned()
    {
        final NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Civilian Files", "Employee Performance File System Records");    
        assertNotNull(recordCategory);
        assertEquals("Employee Performance File System Records", this.nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
        
        final NodeRef recordFolder = createRecordFolder(recordCategory, "Test Record Folder");
        
        // Before we start just remove any outstanding transfers
        final NodeRef rootNode = this.rmService.getFilePlan(recordCategory);
        List<ChildAssociationRef> tempAssocs = this.nodeService.getChildAssocs(rootNode, ASSOC_TRANSFERS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef tempAssoc : tempAssocs)
        {
            this.nodeService.deleteNode(tempAssoc.getChildRef());
        }
        
        setComplete();
        endTransaction();
        
        final NodeRef recordOne = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder, "one.txt");
            }          
        });
        final NodeRef recordTwo = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder, "two.txt");
            }          
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_RECORD));
                
                TestUtilities.declareRecord(recordOne, unprotectedNodeService, rmActionService);
                TestUtilities.declareRecord(recordTwo, unprotectedNodeService, rmActionService);  
                
                assertFalse(nodeService.hasAspect(recordOne, ASPECT_VERSIONED_RECORD));
                
                rmAdminService.addCustomReference(recordOne, recordTwo, QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "versions"));
                
                return null;
            }
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_VERSIONED_RECORD));
                
                rmAdminService.removeCustomReference(recordOne, recordTwo, QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "versions"));
                
                return null;
            }
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                assertFalse(nodeService.hasAspect(recordOne, ASPECT_VERSIONED_RECORD));
                
                return null;
            }
        });
    }
    
    public void testDispositionLifecycle_0430_02_transfer() throws Exception
    {
        final NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Civilian Files", "Foreign Employee Award Files");    
        assertNotNull(recordCategory);
        assertEquals("Foreign Employee Award Files", this.nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
        
        final NodeRef recordFolder = createRecordFolder(recordCategory, "Test Record Folder");
        
        // Before we start just remove any outstanding transfers
        final NodeRef rootNode = this.rmService.getFilePlan(recordCategory);
        List<ChildAssociationRef> tempAssocs = this.nodeService.getChildAssocs(rootNode, ASSOC_TRANSFERS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef tempAssoc : tempAssocs)
        {
            this.nodeService.deleteNode(tempAssoc.getChildRef());
        }
        
        setComplete();
        endTransaction();
        
        final NodeRef recordOne = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder, "one.txt");
            }          
        });
        final NodeRef recordTwo = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                return createRecord(recordFolder, "two.txt");
            }          
        });
        final NodeRef recordThree = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Create the document
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, "three.pdf");
                NodeRef recordThree = nodeService.createNode(recordFolder, 
                                                                ContentModel.ASSOC_CONTAINS, 
                                                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "three.pdf"), 
                                                                ContentModel.TYPE_CONTENT,
                                                                props).getChildRef();
                
                // Set the content
                ContentWriter writer = contentService.getWriter(recordOne, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
                writer.setEncoding("UTF-8");
                writer.putContent("asdas");
                
                return recordThree;
            }          
        });

        final DispositionAction da = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<DispositionAction>()
        {
            public DispositionAction execute() throws Throwable
            {
                // Declare the records
                TestUtilities.declareRecord(recordOne, unprotectedNodeService, rmActionService);
                TestUtilities.declareRecord(recordTwo, unprotectedNodeService, rmActionService);
                TestUtilities.declareRecord(recordThree, unprotectedNodeService, rmActionService);
                
                // Cutoff
                Map<String, Serializable> params = new HashMap<String, Serializable>(3);
                params.put(CompleteEventAction.PARAM_EVENT_NAME, "case_complete");
                params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
                params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, "roy");
                rmActionService.executeRecordsManagementAction(recordFolder, "completeEvent", params);
                rmActionService.executeRecordsManagementAction(recordFolder, "cutoff");
                
                checkLastDispositionAction(recordFolder, "cutoff", 1);
                
                DispositionAction da = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(da);
                assertEquals("transfer", da.getName());
                
                assertFalse(nodeService.hasAspect(recordFolder, ASPECT_TRANSFERRED));
                
                return da;
            }          
        });
        
        // Do the transfer
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        final Object actionResult = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {        
                // Clock the asOf date back to ensure eligibility
                Date nowDate = calendar.getTime();
                assertFalse(nowDate.equals(nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_AS_OF)));
                Map<String, Serializable> params = new HashMap<String, Serializable>(1);
                params.put(EditDispositionActionAsOfDateAction.PARAM_AS_OF_DATE, nowDate);                
                rmActionService.executeRecordsManagementAction(recordFolder, "editDispositionActionAsOfDate", params);
                assertTrue(nowDate.equals(nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_AS_OF)));    
                
                return rmActionService.executeRecordsManagementAction(recordFolder, "transfer", null);
            }          
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertFalse(nodeService.hasAspect(recordFolder, ASPECT_TRANSFERRED));
                assertFalse(nodeService.hasAspect(recordOne, ASPECT_TRANSFERRED));
                assertFalse(nodeService.hasAspect(recordTwo, ASPECT_TRANSFERRED));
                assertFalse(nodeService.hasAspect(recordThree, ASPECT_TRANSFERRED));
                
                // Check that the next disposition action is still in the correct state
                DispositionAction da = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(da);
                assertEquals("transfer", da.getName());
                assertNotNull(da.getStartedAt());
                assertNotNull(da.getStartedBy());
                assertNull(da.getCompletedAt());
                assertNull(da.getCompletedBy());
                
                checkLastDispositionAction(recordFolder, "cutoff", 1);
                
                // Check that the transfer object is created
                assertNotNull(rootNode);
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(rootNode, ASSOC_TRANSFERS, RegexQNamePattern.MATCH_ALL);
                assertNotNull(assocs);
                assertEquals(1, assocs.size());
                NodeRef transferNodeRef = assocs.get(0).getChildRef();
                assertEquals(TYPE_TRANSFER, nodeService.getType(transferNodeRef));
                assertTrue(((Boolean)nodeService.getProperty(transferNodeRef, PROP_TRANSFER_PDF_INDICATOR)).booleanValue());
                assertEquals("Offline Storage", (String)nodeService.getProperty(transferNodeRef, PROP_TRANSFER_LOCATION));
                assertNotNull(actionResult);
                assertEquals(transferNodeRef, ((RecordsManagementActionResult)actionResult).getValue());
                List<ChildAssociationRef> children = nodeService.getChildAssocs(transferNodeRef, ASSOC_TRANSFERRED, RegexQNamePattern.MATCH_ALL);
                assertNotNull(children);
                assertEquals(1, children.size());
                
                
                // Complete the transfer
                rmActionService.executeRecordsManagementAction(assocs.get(0).getChildRef(), "transferComplete");
                
                // Check nodes have been marked correctly
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_TRANSFERRED));
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_TRANSFERRED));
                assertTrue(nodeService.hasAspect(recordTwo, ASPECT_TRANSFERRED));
                assertTrue(nodeService.hasAspect(recordThree, ASPECT_TRANSFERRED));
                
                // Check the transfer object is deleted
                assocs = nodeService.getChildAssocs(rootNode, ASSOC_TRANSFERS, RegexQNamePattern.MATCH_ALL);
                assertNotNull(assocs);
                assertEquals(0, assocs.size());
                
                // Check the disposition action has been moved on        
                da = dispositionService.getNextDispositionAction(recordFolder);
                assertNotNull(da);
                assertEquals("transfer", da.getName());
                assertNull(da.getStartedAt());
                assertNull(da.getStartedBy());
                assertNull(da.getCompletedAt());
                assertNull(da.getCompletedBy());    
                assertFalse(dispositionService.isNextDispositionActionEligible(recordFolder));
                
                checkLastDispositionAction(recordFolder, "transfer", 2);
                
                return null;
            }          
        });
    }
	
    private void checkSearchAspect(NodeRef record)
    {
        checkSearchAspect(record, true);
    }
    
	private void checkSearchAspect(NodeRef record, boolean isPeriodSet)
	{
	    DispositionAction da = dispositionService.getNextDispositionAction(record);
	    if (da != null)
	    {
	        assertTrue(nodeService.hasAspect(record, RecordsManagementSearchBehaviour.ASPECT_RM_SEARCH));
            assertEquals(da.getName(),
                         nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOSITION_ACTION_NAME));
            assertEquals(da.getAsOfDate(),
                         nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOSITION_ACTION_AS_OF));
            assertEquals(nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE),
                         nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOSITION_EVENTS_ELIGIBLE));
            
            int eventCount = da.getEventCompletionDetails().size();
            Collection<String> events = (Collection<String>)nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOSITION_EVENTS);
            if (eventCount == 0)
            {
                assertNull(events);
            }
            else
            {
                assertEquals(eventCount, events.size());
            }
            
            DispositionActionDefinition daDef = da.getDispositionActionDefinition();
            assertNotNull(daDef);
            Period period = daDef.getPeriod();
            if (isPeriodSet)
            {
                assertNotNull(period);
                assertEquals(period.getPeriodType(), nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOSITION_PERIOD));
                assertEquals(period.getExpression(), nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOSITION_PERIOD_EXPRESSION));
            }
            else
            {
                assertNull(period);
                assertNull(nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOSITION_PERIOD));
                assertNull(nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOSITION_PERIOD_EXPRESSION));
            }
	    }
	    
	    DispositionSchedule ds = dispositionService.getDispositionSchedule(record);
	    Boolean value = (Boolean)nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_HAS_DISPOITION_SCHEDULE);
	    String dsInstructions = (String)nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOITION_INSTRUCTIONS);
	    String dsAuthority = (String)nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_DISPOITION_AUTHORITY);
	    if (ds != null)
	    {
	        assertTrue(value);
	        assertEquals(ds.getDispositionInstructions(), dsInstructions);
	        assertEquals(ds.getDispositionAuthority(), dsAuthority);
	    }
	    else
	    {
	        assertFalse(value);
	    }
        
        VitalRecordDefinition vrd = vitalRecordService.getVitalRecordDefinition(record);
        if (vrd == null)
        {
            assertNull(nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_VITAL_RECORD_REVIEW_PERIOD));
            assertNull(nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION));
        }
        else
        {
            assertEquals(vrd.getReviewPeriod().getPeriodType(),
                         nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_VITAL_RECORD_REVIEW_PERIOD));
            assertEquals(vrd.getReviewPeriod().getExpression(),
                         nodeService.getProperty(record, RecordsManagementSearchBehaviour.PROP_RS_VITAL_RECORD_REVIEW_PERIOD_EXPRESSION));            
        }
	}

	
	public void testDispositionLifecycle_0430_01_recordleveldisposition() throws Exception
    {
	    NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Civilian Files", "Employee Performance File System Records");    
        assertNotNull(recordCategory);
        assertEquals("Employee Performance File System Records", this.nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
        
        NodeRef recordFolder = createRecordFolder(recordCategory, "My Record Folder");
        
        setComplete();
        endTransaction();
        
        UserTransaction txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        NodeRef recordOne = createRecord(recordFolder, "one.txt");
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        TestUtilities.declareRecord(recordOne, unprotectedNodeService, rmActionService);
        
        // Check the disposition action
        assertTrue(this.nodeService.hasAspect(recordOne, ASPECT_DISPOSITION_LIFECYCLE));
        assertFalse(this.nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
        
        // Check the dispostion action
        DispositionAction da = dispositionService.getNextDispositionAction(recordOne);
        assertNotNull(da);
        assertEquals("cutoff", da.getDispositionActionDefinition().getName());
        assertNull(da.getAsOfDate());
        assertFalse((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        assertEquals(true, da.getDispositionActionDefinition().eligibleOnFirstCompleteEvent());
        List<EventCompletionDetails> events = da.getEventCompletionDetails();
        assertNotNull(events);
        assertEquals(1, events.size());
        EventCompletionDetails event = events.get(0);
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(3);
        params.put(CompleteEventAction.PARAM_EVENT_NAME, event.getEventName());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, "roy");
        
        this.rmActionService.executeRecordsManagementAction(recordOne, "completeEvent", params);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        assertTrue((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        
        // Do the commit action
        this.rmActionService.executeRecordsManagementAction(recordOne, "cutoff", null);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        // Check events are gone
        da = dispositionService.getNextDispositionAction(recordOne);
        
        assertNotNull(da);
        assertEquals("destroy", da.getDispositionActionDefinition().getName());
        assertNotNull(da.getAsOfDate());
        assertFalse((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        events = da.getEventCompletionDetails();
        assertNotNull(events);
        assertEquals(0, events.size());

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        // Clock the asOf date back to ensure eligibility for destruction
        NodeRef ndNodeRef = nodeService.getChildAssocs(recordOne, ASSOC_NEXT_DISPOSITION_ACTION, RegexQNamePattern.MATCH_ALL).get(0).getChildRef();     
        Date nowDate = calendar.getTime();
        assertFalse(nowDate.equals(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF)));
        params.clear();
        params.put(EditDispositionActionAsOfDateAction.PARAM_AS_OF_DATE, nowDate);                
        rmActionService.executeRecordsManagementAction(recordOne, "editDispositionActionAsOfDate", params);
        assertTrue(nowDate.equals(nodeService.getProperty(ndNodeRef, PROP_DISPOSITION_AS_OF)));
        

        assertNotNull(nodeService.getProperty(recordOne, ContentModel.PROP_CONTENT));

        rmActionService.executeRecordsManagementAction(recordOne, "destroy", null);
        
        // Check that the node has been ghosted
        assertTrue(nodeService.exists(recordOne));
        assertTrue(nodeService.hasAspect(recordOne, RecordsManagementModel.ASPECT_GHOSTED));
        assertNull(nodeService.getProperty(recordOne, ContentModel.PROP_CONTENT));
        
        txn.commit();
    }
	
	public void testDispositionLifecycle_0412_03_eventtest() throws Exception
    {
	    NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Military Files", "Personnel Security Program Records");    
        assertNotNull(recordCategory);
        assertEquals("Personnel Security Program Records", this.nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
                
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, "My Folder");
        NodeRef recordFolder = this.nodeService.createNode(recordCategory, 
                                                           ContentModel.ASSOC_CONTAINS, 
                                                           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "My Folder"), 
                                                           TYPE_RECORD_FOLDER).getChildRef();        
        setComplete();
        endTransaction();
        
        UserTransaction txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        NodeRef recordOne = createRecord(recordFolder);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        TestUtilities.declareRecord(recordOne, unprotectedNodeService, rmActionService);
        
        // NOTE the disposition is being managed at a folder level ...
        
        // Check the disposition action
        assertFalse(this.nodeService.hasAspect(recordOne, ASPECT_DISPOSITION_LIFECYCLE));
        assertTrue(this.nodeService.hasAspect(recordFolder, ASPECT_DISPOSITION_LIFECYCLE));
        
        // Check the dispostion action
        DispositionAction da = dispositionService.getNextDispositionAction(recordFolder);
        assertNotNull(da);
        assertEquals("cutoff", da.getDispositionActionDefinition().getName());
        assertNull(da.getAsOfDate());
        assertFalse((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        assertEquals(false, da.getDispositionActionDefinition().eligibleOnFirstCompleteEvent());
        List<EventCompletionDetails> events = da.getEventCompletionDetails();
        assertNotNull(events);
        assertEquals(3, events.size());
        
        checkSearchAspect(recordFolder);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        EventCompletionDetails ecd = events.get(0);
        assertFalse(ecd.isEventComplete());
        assertNull(ecd.getEventCompletedBy());
        assertNull(ecd.getEventCompletedAt());
        
        Map<String, Serializable> params = new HashMap<String, Serializable>(3);
        params.put(CompleteEventAction.PARAM_EVENT_NAME, events.get(0).getEventName());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, "roy");
        
        checkSearchAspect(recordFolder);
        
        this.rmActionService.executeRecordsManagementAction(recordFolder, "completeEvent", params);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        assertFalse((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        assertEquals(false, da.getDispositionActionDefinition().eligibleOnFirstCompleteEvent());
        events = da.getEventCompletionDetails();
        assertNotNull(events);
        assertEquals(3, events.size());
        
        params = new HashMap<String, Serializable>(3);
        params.put(CompleteEventAction.PARAM_EVENT_NAME, events.get(1).getEventName());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, "roy");
        
        checkSearchAspect(recordFolder);
        
        this.rmActionService.executeRecordsManagementAction(recordFolder, "completeEvent", params);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        assertFalse((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        
        params = new HashMap<String, Serializable>(3);
        params.put(CompleteEventAction.PARAM_EVENT_NAME, events.get(2).getEventName());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, "roy");
        
        checkSearchAspect(recordFolder);
        
        this.rmActionService.executeRecordsManagementAction(recordFolder, "completeEvent", params);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        assertTrue((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        
        events = da.getEventCompletionDetails();
        assertNotNull(events);
        assertEquals(3, events.size());        
        for (EventCompletionDetails e : events)
        {
            assertTrue(e.isEventComplete());
            assertEquals("roy", e.getEventCompletedBy());
            assertNotNull(e.getEventCompletedAt());
        }
        
        checkSearchAspect(recordFolder);
        
        // Test undo
        
        params = new HashMap<String, Serializable>(1);
        params.put(CompleteEventAction.PARAM_EVENT_NAME, events.get(2).getEventName());
        this.rmActionService.executeRecordsManagementAction(recordFolder, "undoEvent", params);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        assertFalse((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        
        params = new HashMap<String, Serializable>(3);
        params.put(CompleteEventAction.PARAM_EVENT_NAME, events.get(2).getEventName());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_AT, new Date());
        params.put(CompleteEventAction.PARAM_EVENT_COMPLETED_BY, "roy");
                
        this.rmActionService.executeRecordsManagementAction(recordFolder, "completeEvent", params);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        assertTrue((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        
        // Do the commit action
        this.rmActionService.executeRecordsManagementAction(recordFolder, "cutoff", null);
        
        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        // Check events are gone
        da = dispositionService.getNextDispositionAction(recordFolder);
        
        assertNotNull(da);
        assertEquals("destroy", da.getDispositionActionDefinition().getName());
        assertNotNull(da.getAsOfDate());
        assertFalse((Boolean)this.nodeService.getProperty(da.getNodeRef(), PROP_DISPOSITION_EVENTS_ELIGIBLE));
        events = da.getEventCompletionDetails();
        assertNotNull(events);
        assertEquals(0, events.size());
        
        checkSearchAspect(recordFolder);
        
        txn.commit();
    }
	
	private NodeRef createRecord(NodeRef recordFolder)
	{
	    return createRecord(recordFolder, "MyRecord.txt");
	}
	
	private NodeRef createRecord(NodeRef recordFolder, String name)
    {
	    return createRecord(recordFolder, name, "There is some content in this record");
    }
	
	private NodeRef createRecord(NodeRef recordFolder, String name, String someTextContent)
	{
    	// Create the document
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_NAME, name);
        NodeRef recordOne = this.nodeService.createNode(recordFolder, 
                                                        ContentModel.ASSOC_CONTAINS, 
                                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                                                        ContentModel.TYPE_CONTENT,
                                                        props).getChildRef();
        
        // Set the content
        ContentWriter writer = this.contentService.getWriter(recordOne, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(someTextContent);
        
        return recordOne;
	}   
      
    /**
     * This method tests the filing of a custom type, as defined in DOD 5015.
     */
    public void testFileDOD5015CustomTypes() throws Exception
    {
        NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");    
                
        NodeRef recordFolder = createRecordFolder(recordCategory, "March AIS Audit Records");
        setComplete();
        endTransaction();
        
        UserTransaction txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        NodeRef testDocument = this.nodeService.createNode(recordFolder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "CustomType"), 
                ContentModel.TYPE_CONTENT).getChildRef();

        // It's not necessary to set content for this test.
        
        // File the record.
        rmActionService.executeRecordsManagementAction(testDocument, "file");

        assertTrue("testDocument should be a record.", rmService.isRecord(testDocument));

        // Have the customType aspect applied..
        Map<String, Serializable> props = new HashMap<String, Serializable>();
        props.put(PROP_SCANNED_FORMAT.toPrefixString(serviceRegistry.getNamespaceService()), "f");
        props.put(PROP_SCANNED_FORMAT_VERSION.toPrefixString(serviceRegistry.getNamespaceService()), "1.0");
        props.put(PROP_RESOLUTION_X.toPrefixString(serviceRegistry.getNamespaceService()), "100");
        props.put(PROP_RESOLUTION_Y.toPrefixString(serviceRegistry.getNamespaceService()), "100");
        props.put(PROP_SCANNED_BIT_DEPTH.toPrefixString(serviceRegistry.getNamespaceService()), "10");
        rmActionService.executeRecordsManagementAction(testDocument, "applyScannedRecord", props);

        assertTrue("Custom type should have ScannedRecord aspect.", nodeService.hasAspect(testDocument, DOD5015Model.ASPECT_SCANNED_RECORD));
        
        txn.rollback();
    }
    
    public void testFileDOD5015CustomTypes2() throws Exception
    {
        NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");    
                
        NodeRef recordFolder = createRecordFolder(recordCategory, "March AIS Audit Records");
        setComplete();
        endTransaction();
        
        UserTransaction txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        NodeRef testDocument = this.nodeService.createNode(recordFolder, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "CustomType"), 
                ContentModel.TYPE_CONTENT).getChildRef();

        // It's not necessary to set content for this test.
        
        // File the record
        List<QName> aspects = new ArrayList<QName>(1);
        aspects.add(DOD5015Model.ASPECT_SCANNED_RECORD);
        Map<String, Serializable> props = new HashMap<String, Serializable>(1);
        props.put(FileAction.PARAM_RECORD_METADATA_ASPECTS, (Serializable)aspects);
        rmActionService.executeRecordsManagementAction(testDocument, "file", props);

        assertTrue("testDocument should be a record.", rmService.isRecord(testDocument));
        assertTrue("Custom type should have ScannedRecord aspect.", nodeService.hasAspect(testDocument, DOD5015Model.ASPECT_SCANNED_RECORD));
        
        txn.rollback();
    }

    /**
     * This method tests the filing of an already existing document i.e. one that is
     * already contained within the document library.
     */
    public void testFileFromDoclib() throws Exception
    {
        // Get the relevant RecordCategory and create a RecordFolder underneath it.
        NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");    
                
        NodeRef recordFolder = createRecordFolder(recordCategory, "March AIS Audit Records");
        setComplete();
        endTransaction();
        
        UserTransaction txn = transactionService.getUserTransaction(false);
        txn.begin();
        
        // Unlike testBasicFilingTest, we now create a normal Alfresco content node
        // rather than a fully-fledged record. The content must also be outside the
        // fileplan.

        // Create a site - to put the content in.
        final String rmTestSiteShortName = "rmTest" + System.currentTimeMillis();
        this.serviceRegistry.getSiteService().createSite("RMTestSite", rmTestSiteShortName,
                "Test site for Records Management", "", SiteVisibility.PUBLIC);

        NodeRef siteRoot = this.serviceRegistry.getSiteService().getSite(rmTestSiteShortName).getNodeRef();
        NodeRef siteDocLib = this.nodeService.createNode(siteRoot, 
                                                   ContentModel.ASSOC_CONTAINS, 
                                                   QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "documentLibrary"), 
                                                   ContentModel.TYPE_FOLDER).getChildRef();
        // Create the test document
        NodeRef testDocument = this.nodeService.createNode(siteDocLib,
                                                    ContentModel.ASSOC_CONTAINS, 
                                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "PreexistingDocument.txt"), 
                                                    ContentModel.TYPE_CONTENT).getChildRef();
        // Set some content
        ContentWriter writer = this.contentService.getWriter(testDocument, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("Some dummy content.");

        txn.commit();
        txn = transactionService.getUserTransaction(false);
        txn.begin();

        // Clearly, this should not be a record at this point.
        assertFalse(this.nodeService.hasAspect(testDocument, ASPECT_RECORD));

        // Now we want to file this document as a record within the RMA.
        // To do this we simply move a document into the fileplan and file
        this.serviceRegistry.getFileFolderService().move(testDocument, recordFolder, null);
        rmActionService.executeRecordsManagementAction(testDocument, "file");

        assertTrue("testDocument should be a record.", rmService.isRecord(testDocument));
        assertNotNull(this.nodeService.getProperty(testDocument, PROP_IDENTIFIER));
        assertNotNull(this.nodeService.getProperty(testDocument, PROP_DATE_FILED));
        
        // Check the review schedule
        assertTrue(this.nodeService.hasAspect(testDocument, ASPECT_VITAL_RECORD));
        assertNotNull(this.nodeService.getProperty(testDocument, PROP_REVIEW_AS_OF));
        
        txn.commit();
    }

    /**
     * This method tests the filing of non-electronic record.
     */
    public void testFileNonElectronicRecord() throws Exception
    {
        setComplete();
        endTransaction();
        
        // Create a record folder
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Get the relevant RecordCategory and create a RecordFolder underneath it.
                NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");
                NodeRef result = createRecordFolder(recordCategory, "March AIS Audit Records" + System.currentTimeMillis());
                
                return result;
            }          
        });
        
        // Create a non-electronic record
        final NodeRef nonElectronicTestRecord = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Create the document
                NodeRef result = nodeService.createNode(recordFolder,
                                            ContentModel.ASSOC_CONTAINS,
                                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Non-electronic Record" + System.currentTimeMillis()),
                                            RecordsManagementModel.TYPE_NON_ELECTRONIC_DOCUMENT).getChildRef();

                // There is no content on a non-electronic record.

                // These properties are required in order to declare the record.
                Map<QName, Serializable> props = nodeService.getProperties(result);
                props.put(RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "alfresco");
                props.put(RecordsManagementModel.PROP_ORIGINATOR, "admin");
                props.put(RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());
                
                Calendar fileCalendar = Calendar.getInstance();
                String year = Integer.toString(fileCalendar.get(Calendar.YEAR));
                props.put(RecordsManagementModel.PROP_DATE_FILED, fileCalendar.getTime());

                String recordId = year + "-" + nodeService.getProperty(result, ContentModel.PROP_NODE_DBID).toString();
                props.put(RecordsManagementModel.PROP_IDENTIFIER, recordId);             

                
                nodeService.setProperties(result, props);
                
                return result;
            }          
        });

        // File and declare the record
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertTrue("Expected non-electronic record to be a record.", rmService.isRecord(nonElectronicTestRecord));
                assertFalse("Expected non-electronic record not to be declared yet.", rmService.isRecordDeclared(nonElectronicTestRecord));
                
                rmActionService.executeRecordsManagementAction(nonElectronicTestRecord, "declareRecord");
                
                assertTrue("Non-electronic record should now be declared.", rmService.isRecordDeclared(nonElectronicTestRecord));
                
                // These properties are added automatically when the record is filed
                assertNotNull(nodeService.getProperty(nonElectronicTestRecord, RecordsManagementModel.PROP_IDENTIFIER));
                assertNotNull(nodeService.getProperty(nonElectronicTestRecord, RecordsManagementModel.PROP_DATE_FILED));
                
//                      assertNotNull(nodeService.getProperty(testRecord, ContentModel.PROP_TITLE));
//                      assertNotNull(nodeService.getProperty(testRecord, RecordsManagementModel.PROP_SUPPLEMENTAL_MARKING_LIST));
//                      assertNotNull(nodeService.getProperty(testRecord, RecordsManagementModel.PROP_MEDIA_TYPE));
//                      assertNotNull(nodeService.getProperty(testRecord, RecordsManagementModel.PROP_FORMAT));
//                      assertNotNull(nodeService.getProperty(testRecord, RecordsManagementModel.PROP_DATE_RECEIVED));
//                      assertEquals("foo", nodeService.getProperty(testRecord, RecordsManagementModel.PROP_ADDRESS));
//                      assertEquals("foo", nodeService.getProperty(testRecord, RecordsManagementModel.PROP_OTHER_ADDRESS));
//                      assertNotNull(nodeService.getProperty(testRecord, RecordsManagementModel.PROP_LOCATION));
//                      assertEquals("foo", nodeService.getProperty(testRecord, RecordsManagementModel.PROP_PROJECT_NAME));
                
                //TODO Add links to other records as per test doc.
                return null;
            }          
        });
    }

    private NodeRef createRecordFolder(NodeRef recordCategory, String folderName)
    {
        Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(1);
        folderProps.put(ContentModel.PROP_NAME, folderName);
        NodeRef recordFolder = this.nodeService.createNode(recordCategory, 
                                                           ContentModel.ASSOC_CONTAINS, 
                                                           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderName), 
                                                           TYPE_RECORD_FOLDER).getChildRef();
        return recordFolder;
    }
    
    /**
     * Caveat Config
     * 
     * @throws Exception
     */
    public void testCaveatConfig() throws Exception
    {
        setComplete();
        endTransaction();
        
        cleanCaveatConfigData();
        setupCaveatConfigData();
        
        // set/reset allowed values (empty list by default)
        
        final List<String> newValues = new ArrayList<String>(4);
        newValues.add(NOFORN);
        newValues.add(NOCONTRACT);
        newValues.add(FOUO);
        newValues.add(FGI);
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {                
                rmAdminService.changeCustomConstraintValues(RecordsManagementCustomModel.CONSTRAINT_CUSTOM_SMLIST, newValues);

                return null;
            }          
        });
        
        final NodeRef recordFolder = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Test list of allowed values for caveats
                
                List<String> allowedValues = AuthenticationUtil.runAs(new RunAsWork<List<String>>()
                {
                    public List<String> doWork()
                    {
                        // get allowed values for given caveat (for current user)
                        return caveatConfigService.getRMAllowedValues("rmc:smList");
                    }
                }, "dfranco");
                
                assertEquals(2, allowedValues.size());
                assertTrue(allowedValues.contains(NOFORN));
                assertTrue(allowedValues.contains(FOUO));
                
                
                allowedValues = AuthenticationUtil.runAs(new RunAsWork<List<String>>()
                {
                    public List<String> doWork()
                    {
                        // get allowed values for given caveat (for current user)
                        return caveatConfigService.getRMAllowedValues("rmc:smList");
                    }
                }, "dmartinz");
                
                assertEquals(4, allowedValues.size());
                assertTrue(allowedValues.contains(NOFORN));
                assertTrue(allowedValues.contains(NOCONTRACT));
                assertTrue(allowedValues.contains(FOUO));
                assertTrue(allowedValues.contains(FGI));
                
                
                // Create record category / record folder
                
                final NodeRef recordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");
                assertNotNull(recordCategory);
                assertEquals("AIS Audit Records", nodeService.getProperty(recordCategory, ContentModel.PROP_NAME));
                
                NodeRef recordFolder = createRecordFolder(recordCategory, "March AIS Audit Records");
                assertNotNull(recordFolder);
                assertEquals(TYPE_RECORD_FOLDER, nodeService.getType(recordFolder));
                
                // set RM capabilities on the file plan - to view & read records
                setPermission(filePlan, PermissionService.ALL_AUTHORITIES, RMPermissionModel.VIEW_RECORDS, true);
                setPermission(filePlan, PermissionService.ALL_AUTHORITIES, RMPermissionModel.READ_RECORDS, true);
                
                // set RM capabilities on the record folder - to read records
                setPermission(recordFolder, PermissionService.ALL_AUTHORITIES, RMPermissionModel.READ_RECORDS, true);
                

                return recordFolder;
            }          
        });
                       
        final String RECORD_NAME = "MyRecord"+System.currentTimeMillis()+".txt";
        final String SOME_CONTENT = "There is some content in this record";
        
        final NodeRef recordOne = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {                        
                int expectedChildCount = nodeService.getChildAssocs(recordFolder).size();
                
                NodeRef recordOne = createRecord(recordFolder, RECORD_NAME, SOME_CONTENT);
                
                assertEquals(expectedChildCount+1, nodeService.getChildAssocs(recordFolder).size());

                return recordOne;
            }          
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                assertTrue(nodeService.hasAspect(recordOne, ASPECT_RECORD));
                
                int expectedChildCount = nodeService.getChildAssocs(recordFolder).size()-1;
                
                //
                // Test caveats (security interceptors) BEFORE setting properties
                //
                
                sanityCheckAccess("dmartinz", recordFolder, recordOne, RECORD_NAME, SOME_CONTENT, true, expectedChildCount);
                sanityCheckAccess("gsmith", recordFolder, recordOne, RECORD_NAME, SOME_CONTENT, true, expectedChildCount);
                sanityCheckAccess("dsandy", recordFolder, recordOne, RECORD_NAME, SOME_CONTENT, true, expectedChildCount);
                
                // Test setting properties (with restricted set of allowed values)
                
                // Set supplemental markings list (on record)
                // TODO - set supplemental markings list (on record folder)
                
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        // set RM capabilities on the file plan - to file records and add/edit properties (ie. edit record)
                        setPermission(filePlan, "dfranco", RMPermissionModel.FILING, true);
                        setPermission(filePlan, "dfranco", RMPermissionModel.EDIT_RECORD_METADATA, true);
                        return null;
                    }
                }, "admin");
                
                
                AuthenticationUtil.setFullyAuthenticatedUser("dfranco");
                assertEquals(AccessStatus.ALLOWED, publicServiceAccessService.hasAccess("NodeService", "exists", recordFolder));

                return null;
            }          
        });
        
        try
        {
        	transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
	        {
	            public Object execute() throws Throwable
	            {
            
		            // Set smList
		            
		            Map<QName, Serializable> propValues = new HashMap<QName, Serializable>(1);
		            List<String> smList = new ArrayList<String>(3);
		            smList.add(FOUO);
		            smList.add(NOFORN);
		            smList.add(NOCONTRACT);
		            propValues.put(RecordsManagementModel.PROP_SUPPLEMENTAL_MARKING_LIST, (Serializable)smList);
		            nodeService.addProperties(recordOne, propValues);
		            
		            return null;
	            }
	        });
            
            fail("Should fail with integrity exception"); // user 'dfranco' not allowed 'NOCONTRACT'
        }
        catch (IntegrityException ie)
        {
            // expected
        }
        
        try
        {
        	transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
	        {
	            public Object execute() throws Throwable
	            {            
		            // Set smList
		            
		            Map<QName, Serializable> propValues = new HashMap<QName, Serializable>(1);
		            List<String> smList = new ArrayList<String>(2);
		            smList.add(FOUO);
		            smList.add(NOFORN);
		            propValues.put(RecordsManagementModel.PROP_SUPPLEMENTAL_MARKING_LIST, (Serializable)smList);
		            nodeService.addProperties(recordOne, propValues);
		            
		            return null;
	            }
	        });            
        }
        catch (IntegrityException ie)
        {
            fail(""+ie);
        }
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {        
		        @SuppressWarnings("unchecked")
		        List<String> smList = (List<String>)nodeService.getProperty(recordOne, RecordsManagementModel.PROP_SUPPLEMENTAL_MARKING_LIST);
		        assertEquals(2, smList.size());
		        assertTrue(smList.contains(NOFORN));
		        assertTrue(smList.contains(FOUO));
        
		        return null;
            }
        });
        
        // User-defined field (in this case, "rmc:prjList" on record)
        
        // Create custom constraint (or reset values if it already exists)
        
        // create new custom constraint
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                try
                {
                    List<String> emptyList = new ArrayList<String>(0);
                    rmAdminService.addCustomConstraintDefinition(CONSTRAINT_CUSTOM_PRJLIST, "Some Projects", true, emptyList, MatchLogic.AND);
                }
                catch (AlfrescoRuntimeException e)
                {
                    // ignore - ie. assume exception is due to the fact that it already exists
                }
                
                return null;
            }
        });
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
        	public Object execute() throws Throwable
        	{     
        		List<String> newerValues = new ArrayList<String>(3);
                newerValues.add(PRJ_A);
                newerValues.add(PRJ_B);
                newerValues.add(PRJ_C);
                
        		rmAdminService.changeCustomConstraintValues(CONSTRAINT_CUSTOM_PRJLIST, newerValues);
        		
        		return null;
        	}
        });
        
        // define custom property and reference custom constraint
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                try
                {
                    // Define a custom "project list" property (for records) - note: multi-valued
                    rmAdminService.addCustomPropertyDefinition(
                    		PROP_CUSTOM_PRJLIST, 
                    		ASPECT_RECORD,
                            PROP_CUSTOM_PRJLIST.getLocalName(), 
                            DataTypeDefinition.TEXT, "Projects",
                            null, 
                            null, 
                            true, 
                            false, 
                            false, 
                            CONSTRAINT_CUSTOM_PRJLIST);
                } 
                catch (AlfrescoRuntimeException e)
                {
                    // ignore - ie. assume exception is due to the fact that it already exists
                }
                
                return null;
            }
        });
        
        try
        {
        	transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
	        {
	            public Object execute() throws Throwable
	            {
        
		            // Set prjList
		            
		            Map<QName, Serializable> propValues = new HashMap<QName, Serializable>(1);
		            List<String> prjList = new ArrayList<String>(3);
		            prjList.add(PRJ_A);
		            prjList.add(PRJ_B);
		            propValues.put(PROP_CUSTOM_PRJLIST, (Serializable)prjList);
		            nodeService.addProperties(recordOne, propValues);
		            
		            return null;
	            }
	        });         
        
            fail("Should fail with integrity exception"); // user 'dfranco' not allowed 'Project B'
        }
        catch (IntegrityException ie)
        {
            // expected
        }
        
        try
        {
        	transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
	        {
	            public Object execute() throws Throwable
	            {            
		            // Set prjList		           
		            Map<QName, Serializable> propValues = new HashMap<QName, Serializable>(1);
		            List<String> prjList = new ArrayList<String>(3);
		            prjList.add(PRJ_A);
		            propValues.put(PROP_CUSTOM_PRJLIST, (Serializable)prjList);
		            nodeService.addProperties(recordOne, propValues);
            
		            return null;
	            }
	        });
        }
        catch (IntegrityException ie)
        {
            fail(""+ie);
        }
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {        
		        @SuppressWarnings("unchecked")
		        List<String> prjList = (List<String>)nodeService.getProperty(recordOne, PROP_CUSTOM_PRJLIST);
		        assertEquals(1, prjList.size());
		        assertTrue(prjList.contains(PRJ_A));
		        
		        return null;
            }
        });
        
        //
        // Test caveats (security interceptors) AFTER setting properties
        //

        int expectedChildCount = nodeService.getChildAssocs(recordFolder).size()-1;
        sanityCheckAccess("dmartinz", recordFolder, recordOne, RECORD_NAME, SOME_CONTENT, true, expectedChildCount);
        sanityCheckAccess("gsmith", recordFolder, recordOne, RECORD_NAME, SOME_CONTENT, false, expectedChildCount); // denied by rma:prjList ("Project A")
        
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {        
            	AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());        
            	addToGroup("gsmith", "Engineering");
            	
            	return null;
            }
        });
        
        sanityCheckAccess("gsmith", recordFolder, recordOne, RECORD_NAME, SOME_CONTENT, true, expectedChildCount);
        sanityCheckAccess("dsandy", recordFolder, recordOne, RECORD_NAME, SOME_CONTENT, false, expectedChildCount); // denied by rma:smList  ("NOFORN", "FOUO")
        
        cleanCaveatConfigData();
    }
    
    private void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow)
    {
        permissionService.setPermission(nodeRef, authority, permission, allow);
        if (permission.equals(RMPermissionModel.FILING))
        {
            if (rmService.isRecordCategory(nodeRef) == true)
            {
                List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef assoc : assocs)
                {
                    NodeRef child = assoc.getChildRef();
                    if (rmService.isRecordFolder(child) == true || rmService.isRecordCategory(child) == true)
                    {
                        setPermission(child, authority, permission, allow);
                    }
                }
            }
        }
    }
    
    private void cleanCaveatConfigData()
    {
        startNewTransaction();
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        deleteUser("jrangel");
        deleteUser("dmartinz");
        deleteUser("jrogers");
        deleteUser("hmcneil");
        deleteUser("dfranco");
        deleteUser("gsmith");
        deleteUser("eharris");
        deleteUser("bbayless");
        deleteUser("mhouse");
        deleteUser("aly");
        deleteUser("dsandy");
        deleteUser("driggs");
        deleteUser("test1");
        
        deleteGroup("Engineering");
        deleteGroup("Finance");
        deleteGroup("test1");
        
        caveatConfigService.updateOrCreateCaveatConfig("{}"); // empty config !
        
        setComplete();
        endTransaction();
    }
    
    private void setupCaveatConfigData()
    {
        startNewTransaction();
        
        // Switch to admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Create test users/groups (if they do not already exist)
        
        createUser("jrangel");
        createUser("dmartinz");
        createUser("jrogers");
        createUser("hmcneil");
        createUser("dfranco");
        createUser("gsmith");
        createUser("eharris");
        createUser("bbayless");
        createUser("mhouse");
        createUser("aly");
        createUser("dsandy");
        createUser("driggs");
        createUser("test1");
        
        createGroup("Engineering");
        createGroup("Finance");
        createGroup("test1");
        
        addToGroup("jrogers", "Engineering");
        addToGroup("dfranco", "Finance");
        
        // not in grouo to start with - added later
        //addToGroup("gsmith", "Engineering");
        
        File file = new File(System.getProperty("user.dir")+"/test-resources/testCaveatConfig2.json"); // from test-resources
        assertTrue(file.exists());
        
        caveatConfigService.updateOrCreateCaveatConfig(file);
        
        setComplete();
        endTransaction();
    }
    
    protected void createUser(String userName)
    {
        if (! authenticationService.authenticationExists(userName))
        {
            authenticationService.createAuthentication(userName, "PWD".toCharArray());
        }
        
        if (! personService.personExists(userName))
        {
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            personService.createPerson(ppOne);
        }
    }
    
    protected void deleteUser(String userName)
    {
        if (personService.personExists(userName))
        {
            personService.deletePerson(userName);
        }
    }
    
    protected void createGroup(String groupShortName)
    {
        createGroup(null, groupShortName);
    }
    
    protected void createGroup(String parentGroupShortName, String groupShortName)
    {
        if (parentGroupShortName != null)
        {
            String parentGroupFullName = authorityService.getName(AuthorityType.GROUP, parentGroupShortName);
            if (authorityService.authorityExists(parentGroupFullName) == false)
            {
                authorityService.createAuthority(AuthorityType.GROUP, groupShortName, groupShortName, null);
                authorityService.addAuthority(parentGroupFullName, groupShortName);
            }
        }
        else
        {
            authorityService.createAuthority(AuthorityType.GROUP, groupShortName, groupShortName, null);
        }
    }
    
    protected void deleteGroup(String groupShortName)
    {
        String groupFullName = authorityService.getName(AuthorityType.GROUP, groupShortName);
        if (authorityService.authorityExists(groupFullName) == true)
        {
            authorityService.deleteAuthority(groupFullName);
        }
    }
    
    protected void addToGroup(String authorityName, String groupShortName)
    {
        authorityService.addAuthority(authorityService.getName(AuthorityType.GROUP, groupShortName), authorityName);
    }
    
    protected void removeFromGroup(String authorityName, String groupShortName)
    {
        authorityService.removeAuthority(authorityService.getName(AuthorityType.GROUP, groupShortName), authorityName);
    }
    
    private void sanityCheckAccess(String user, NodeRef recordFolder, NodeRef record, String expectedName, String expectedContent, boolean expectedAllowed, int baseCount)
    {
        //startNewTransaction();
        
        AuthenticationUtil.setFullyAuthenticatedUser(user);
        
        // Sanity check search service - eg. query
        
        String query = "ID:"+AbstractLuceneQueryParser.escape(record.toString());
        ResultSet rs = this.searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);
        
        if (expectedAllowed)
        {
            assertEquals(1, rs.length());
            assertEquals(record.toString(), rs.getNodeRef(0).toString());
        }
        else
        {
            assertEquals(0, rs.length());
        }
        rs.close();
        
        // Sanity check node service - eg. getProperty, getChildAssocs
        
        try
        {
            Serializable value = this.nodeService.getProperty(record, ContentModel.PROP_NAME);
            
            if (expectedAllowed)
            {
                assertNotNull(value);
                assertEquals(expectedName, (String)value);
            }
            else
            {
                fail("Unexpected - access should be denied by caveats");
            }
        }
        catch (AccessDeniedException ade)
        {
            if (expectedAllowed)
            {
                fail("Unexpected - access should be allowed by caveats");
            }
            
            // expected
        }
        
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(recordFolder);
        
        if (expectedAllowed)
        {
            assertEquals(baseCount+1, childAssocs.size());
            assertEquals(record.toString(), childAssocs.get(baseCount).getChildRef().toString());
        }
        else
        {
            assertEquals(baseCount, childAssocs.size());
        }
        
        // Sanity check content service - eg. getReader
        
        try
        {
            ContentReader reader = this.contentService.getReader(record, ContentModel.PROP_CONTENT);
            
            if (expectedAllowed)
            {
                assertNotNull(reader);
                assertEquals(expectedContent, reader.getContentString());
            }
            else
            {
                fail("Unexpected - access should be denied by caveats");
            }
        }
        catch (AccessDeniedException ade)
        {
            if (expectedAllowed)
            {
                fail("Unexpected - access should be allowed by caveats");
            }
            
            // expected
        }
        
        //setComplete();
        //endTransaction();
    }
   
    /**
     * https://issues.alfresco.com/jira/browse/ETHREEOH-3587
     */
    public void testETHREEOH3587()
    {
        NodeRef recordFolder = TestUtilities.getRecordFolder(rmService, nodeService, "Reports", "AIS Audit Records", "January AIS Audit Records");
        assertNotNull(recordFolder);
        
        // Create a record
        final NodeRef record = createRecord(recordFolder, GUID.generate());
        
        // Commit in order to trigger the setUpRecordFolder behaviour
        setComplete();
        endTransaction();
        
        // Now try and update the id, this should fail
        try 
        {          
            transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    // Lets just check the record identifier has been set
                    String id = (String)nodeService.getProperty(record, RecordsManagementModel.PROP_IDENTIFIER);
                    assertNotNull(id);
                    
                    nodeService.setProperty(record, RecordsManagementModel.PROP_IDENTIFIER, "randomValue");
    
                    return null;
                }          
            }); 
            
            fail("You should not be allowed to update the identifier of a record once it has been created.");
        }
        catch(AlfrescoRuntimeException e)
        {
            // Expected
        }
        
        // TODO set the identifier of the second record to be the same as the first ....
    }

    /**
     * Vital Record Test
     * 
     * @throws Exception
     */
    public void testVitalRecords() throws Exception
    {
        //
        // Create a record folder under a "vital" category
        //
        
        // TODO Don't think I need to do this. Can I reuse the existing January one?
        
        NodeRef vitalRecCategory =
            TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "AIS Audit Records");    
        
        assertNotNull(vitalRecCategory);
        assertEquals("AIS Audit Records",
                this.nodeService.getProperty(vitalRecCategory, ContentModel.PROP_NAME));

        NodeRef vitalRecFolder = this.nodeService.createNode(vitalRecCategory, 
                                                    ContentModel.ASSOC_CONTAINS, 
                                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                            "March AIS Audit Records"), 
                                                    TYPE_RECORD_FOLDER).getChildRef();
        setComplete();
        endTransaction();
        UserTransaction txn1 = transactionService.getUserTransaction(false);
        txn1.begin();
        
        // Check the Vital Record data
        VitalRecordDefinition vitalRecCatDefinition = vitalRecordService.getVitalRecordDefinition(vitalRecCategory);
        assertNotNull("This record category should have a VitalRecordDefinition", vitalRecCatDefinition);
        assertTrue(vitalRecCatDefinition.isEnabled());
        
        VitalRecordDefinition vitalRecFolderDefinition = vitalRecordService.getVitalRecordDefinition(vitalRecFolder);
        assertNotNull("This record folder should have a VitalRecordDefinition", vitalRecFolderDefinition);
        assertTrue(vitalRecFolderDefinition.isEnabled());
        
        assertEquals("The Vital Record reviewPeriod in the folder did not match its parent category",
                vitalRecFolderDefinition.getReviewPeriod(),
                vitalRecCatDefinition.getReviewPeriod());
        
        // check the search aspect for both the category and folder
        checkSearchAspect(vitalRecFolder);
        
        // Create a vital record
        NodeRef vitalRecord = this.nodeService.createNode(vitalRecFolder, 
                                                        ContentModel.ASSOC_CONTAINS, 
                                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                                                "MyVitalRecord" + System.currentTimeMillis() +".txt"), 
                                                        ContentModel.TYPE_CONTENT).getChildRef();
        
        // Set the content
        ContentWriter writer = this.contentService.getWriter(vitalRecord, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("There is some content in this record");
        
        rmActionService.executeRecordsManagementAction(vitalRecord, "file");
        
        txn1.commit();
        
        UserTransaction txn2 = transactionService.getUserTransaction(false);
        txn2.begin();
        
        // Check the review schedule
        
        assertTrue(this.nodeService.hasAspect(vitalRecord, ASPECT_VITAL_RECORD));
        VitalRecordDefinition vitalRecDefinition = vitalRecordService.getVitalRecordDefinition(vitalRecord);
        assertTrue(vitalRecDefinition.isEnabled());
        Date vitalRecordAsOfDate = (Date)this.nodeService.getProperty(vitalRecord, PROP_REVIEW_AS_OF);
        assertNotNull("vitalRecord should have a reviewAsOf date.", vitalRecordAsOfDate);
        
        // check the search aspect for the vital record
        checkSearchAspect(vitalRecord);
        
        //
        // Create a record folder under a "non-vital" category
        //
        NodeRef nonVitalRecordCategory = TestUtilities.getRecordCategory(rmService, nodeService, "Reports", "Unit Manning Documents");    
        assertNotNull(nonVitalRecordCategory);
        assertEquals("Unit Manning Documents", this.nodeService.getProperty(nonVitalRecordCategory, ContentModel.PROP_NAME));

        NodeRef nonVitalFolder = this.nodeService.createNode(nonVitalRecordCategory,
                                                           ContentModel.ASSOC_CONTAINS, 
                                                           QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "4th Quarter Unit Manning Documents"), 
                                                           TYPE_RECORD_FOLDER).getChildRef();
        txn2.commit();

        UserTransaction txn3 = transactionService.getUserTransaction(false);
        txn3.begin();
        
        // Check the Vital Record data
        assertFalse(vitalRecordService.getVitalRecordDefinition(nonVitalRecordCategory).isEnabled());
        assertFalse(vitalRecordService.getVitalRecordDefinition(nonVitalFolder).isEnabled());
        assertEquals("The Vital Record reviewPeriod in the folder did not match its parent category",
                vitalRecordService.getVitalRecordDefinition(nonVitalFolder).getReviewPeriod(),
                vitalRecordService.getVitalRecordDefinition(nonVitalRecordCategory).getReviewPeriod());
        
        // Create a record
        NodeRef nonVitalRecord = this.nodeService.createNode(nonVitalFolder, 
                                                        ContentModel.ASSOC_CONTAINS, 
                                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "MyNonVitalRecord.txt"), 
                                                        ContentModel.TYPE_CONTENT).getChildRef();
        
        // Set content
        writer = this.contentService.getWriter(nonVitalRecord, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent("There is some content in this record");
        
        this.rmActionService.executeRecordsManagementAction(nonVitalRecord, "file");
        
        txn3.commit();
        
        UserTransaction txn4 = transactionService.getUserTransaction(false);
        txn4.begin();
        
        // Check the review schedule
        assertFalse(this.nodeService.hasAspect(nonVitalRecord, ASPECT_VITAL_RECORD));
        assertFalse(vitalRecordService.getVitalRecordDefinition(nonVitalRecord).isEnabled());
        assertEquals("The Vital Record reviewPeriod did not match its parent category",
                vitalRecordService.getVitalRecordDefinition(nonVitalRecord).getReviewPeriod(),
                vitalRecordService.getVitalRecordDefinition(nonVitalFolder).getReviewPeriod());

        // Declare as a record
        assertTrue(this.nodeService.hasAspect(nonVitalRecord, ASPECT_RECORD)); 
 
        assertTrue("Declared record already on prior to test",  
            this.nodeService.hasAspect(nonVitalRecord, ASPECT_DECLARED_RECORD) == false);  

               
        this.nodeService.setProperty(nonVitalRecord, RecordsManagementModel.PROP_PUBLICATION_DATE, new Date());       
        List<String> smList = new ArrayList<String>(2);
        smList.add(FOUO);
        smList.add(NOFORN);
        this.nodeService.setProperty(nonVitalRecord, RecordsManagementModel.PROP_SUPPLEMENTAL_MARKING_LIST, (Serializable)smList);        
        this.nodeService.setProperty(nonVitalRecord, RecordsManagementModel.PROP_MEDIA_TYPE, "mediaTypeValue"); 
        this.nodeService.setProperty(nonVitalRecord, RecordsManagementModel.PROP_FORMAT, "formatValue"); 
        this.nodeService.setProperty(nonVitalRecord, RecordsManagementModel.PROP_DATE_RECEIVED, new Date());
        this.nodeService.setProperty(nonVitalRecord, RecordsManagementModel.PROP_ORIGINATOR, "origValue");
        this.nodeService.setProperty(nonVitalRecord, RecordsManagementModel.PROP_ORIGINATING_ORGANIZATION, "origOrgValue");
        this.nodeService.setProperty(nonVitalRecord, ContentModel.PROP_TITLE, "titleValue");

        this.rmActionService.executeRecordsManagementAction(nonVitalRecord, "declareRecord");
        assertTrue(this.nodeService.hasAspect(nonVitalRecord, ASPECT_RECORD));    
        assertTrue("Declared aspect not set", this.nodeService.hasAspect(nonVitalRecord, ASPECT_DECLARED_RECORD));  
        
        //
        // Now we will change the vital record indicator in the containers above these records
        // and ensure that the change is reflected down to the record.
        //
        
        // 1. Switch parent folder from non-vital to vital.
        this.nodeService.setProperty(nonVitalFolder, PROP_VITAL_RECORD_INDICATOR, true);
        this.nodeService.setProperty(nonVitalFolder, PROP_REVIEW_PERIOD, "week|1");
        
        txn4.commit();
        
        UserTransaction txn5 = transactionService.getUserTransaction(false);
        txn5.begin();
        
        // check the folder search aspect
        checkSearchAspect(nonVitalFolder);
        
        NodeRef formerlyNonVitalRecord = nonVitalRecord;

        assertTrue("Expected VitalRecord aspect not present", nodeService.hasAspect(formerlyNonVitalRecord, ASPECT_VITAL_RECORD));
        VitalRecordDefinition formerlyNonVitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(formerlyNonVitalRecord);
        assertNotNull(formerlyNonVitalRecordDefinition);
        
        assertEquals("The Vital Record reviewPeriod is wrong.", new Period("week|1"),
                vitalRecordService.getVitalRecordDefinition(formerlyNonVitalRecord).getReviewPeriod());
        assertNotNull("formerlyNonVitalRecord should now have a reviewAsOf date.",
                      nodeService.getProperty(formerlyNonVitalRecord, PROP_REVIEW_AS_OF));

        // check search aspect for the new vital record
        checkSearchAspect(formerlyNonVitalRecord);

        // 2. Switch parent folder from vital to non-vital.
        this.nodeService.setProperty(vitalRecFolder, PROP_VITAL_RECORD_INDICATOR, false);
        
        txn5.commit();
        
        UserTransaction txn6 = transactionService.getUserTransaction(false);
        txn6.begin();
        
        NodeRef formerlyVitalRecord = vitalRecord;

        assertTrue("Unexpected VitalRecord aspect present",
                nodeService.hasAspect(formerlyVitalRecord, ASPECT_VITAL_RECORD) == false);
        VitalRecordDefinition formerlyVitalRecordDefinition = vitalRecordService.getVitalRecordDefinition(formerlyVitalRecord);
        assertNotNull(formerlyVitalRecordDefinition);
        assertNull("formerlyVitalRecord should now not have a reviewAsOf date.",
                nodeService.getProperty(formerlyVitalRecord, PROP_REVIEW_AS_OF));
        
        // 3. override the VitalRecordDefinition between Category, Folder, Record and ensure
        // the overrides work
        
        // First switch the non-vital record folder back to vital.
        this.nodeService.setProperty(vitalRecFolder, PROP_VITAL_RECORD_INDICATOR, true);
        
        txn6.commit();
        UserTransaction txn7 = transactionService.getUserTransaction(false);
        txn7.begin();

        assertTrue("Unexpected VitalRecord aspect present",
                nodeService.hasAspect(vitalRecord, ASPECT_VITAL_RECORD));

        // The reviewAsOf date should be changing as the parent review periods are updated.
        Date initialReviewAsOfDate = (Date)nodeService.getProperty(vitalRecord, PROP_REVIEW_AS_OF);
        assertNotNull("record should have a reviewAsOf date.",
                initialReviewAsOfDate);

        // Change some of the VitalRecordDefinition in Record Category
        Map<QName, Serializable> recCatProps = this.nodeService.getProperties(vitalRecCategory);
        
        // Run this test twice (after a clean db) and it fails at the below line.
        assertEquals(new Period("week|1"), recCatProps.get(PROP_REVIEW_PERIOD));
        this.nodeService.setProperty(vitalRecCategory, PROP_REVIEW_PERIOD, new Period("day|1"));
        
        txn7.commit();
        UserTransaction txn8 = transactionService.getUserTransaction(false);
        txn8.begin();

        assertEquals(new Period("day|1"), vitalRecordService.getVitalRecordDefinition(vitalRecCategory).getReviewPeriod());
        assertEquals(new Period("day|1"), vitalRecordService.getVitalRecordDefinition(vitalRecFolder).getReviewPeriod());

        // check the search aspect of the folder after period change
        checkSearchAspect(vitalRecFolder);
        
        // Change some of the VitalRecordDefinition in Record Folder
        Map<QName, Serializable> folderProps = this.nodeService.getProperties(vitalRecFolder);
        assertEquals(new Period("day|1"), folderProps.get(PROP_REVIEW_PERIOD));
        this.nodeService.setProperty(vitalRecFolder, PROP_REVIEW_PERIOD, new Period("month|1"));

        txn8.commit();
        UserTransaction txn9 = transactionService.getUserTransaction(false);
        txn9.begin();

        assertEquals(new Period("day|1"), vitalRecordService.getVitalRecordDefinition(vitalRecCategory).getReviewPeriod());
        assertEquals(new Period("month|1"), vitalRecordService.getVitalRecordDefinition(vitalRecFolder).getReviewPeriod());

        // check the search aspect of the folder after period change
        checkSearchAspect(vitalRecFolder);
        
        // Need to commit the transaction to trigger the behaviour that handles changes to VitalRecord Definition.
        txn9.commit();
        UserTransaction txn10 = transactionService.getUserTransaction(false);
        txn10.begin();
        
        Date newReviewAsOfDate = (Date)nodeService.getProperty(vitalRecord, PROP_REVIEW_AS_OF);
        assertNotNull("record should have a reviewAsOf date.", initialReviewAsOfDate);
        assertTrue("reviewAsOfDate should have changed.",
                initialReviewAsOfDate.toString().equals(newReviewAsOfDate.toString()) == false);
        
        // check the search aspect of the record after period change
        checkSearchAspect(vitalRecord);
        
        // Now clean up after this test.
        nodeService.deleteNode(vitalRecord);
        nodeService.deleteNode(vitalRecFolder);
        nodeService.deleteNode(nonVitalRecord);
        nodeService.deleteNode(nonVitalFolder);
        nodeService.setProperty(vitalRecCategory, PROP_REVIEW_PERIOD, new Period("week|1"));
        
        txn10.commit();
    }

}
