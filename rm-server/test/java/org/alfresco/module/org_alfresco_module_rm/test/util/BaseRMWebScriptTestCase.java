/**
 * 
 */
package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.RmSiteType;
import org.alfresco.module.org_alfresco_module_rm.search.RecordsManagementSearchService;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.context.ApplicationContext;

/**
 * @author Roy Wetherall
 */
public class BaseRMWebScriptTestCase extends BaseWebScriptTest
{
	/** Site id */
    protected static final String SITE_ID = "mySite";
    
	/** Common test utils */
	protected CommonRMTestUtils utils;
	
	 /** Services */
    protected NodeService nodeService;
    protected ContentService contentService;
    protected DictionaryService dictionaryService;
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected PolicyComponent policyComponent;
    protected NamespaceService namespaceService;
    protected SearchService searchService;
    protected SiteService siteService;
    protected MutableAuthenticationService authenticationService;
    protected AuthorityService authorityService;
    protected PersonService personService;
    
    /** RM Services */
    protected RecordsManagementService rmService;
    protected DispositionService dispositionService;
    protected RecordsManagementEventService eventService;
    protected RecordsManagementAdminService adminService;    
    protected RecordsManagementActionService actionService;
    protected RecordsManagementSearchService rmSearchService;
    protected RecordsManagementSecurityService securityService;
    protected RecordsManagementAuditService auditService;
    protected CapabilityService capabilityService;
    protected VitalRecordService vitalRecordService;
    
    /** test data */
    protected StoreRef storeRef;
    protected NodeRef rootNodeRef;   
    protected SiteInfo siteInfo;
    protected NodeRef folder;
    protected NodeRef filePlan;
    protected NodeRef recordSeries;			// A category with no disposition schedule
    protected NodeRef recordCategory;
    protected DispositionSchedule dispositionSchedule;
    protected NodeRef recordFolder;
    protected NodeRef recordFolder2;
	
    @Override
    protected void setUp() throws Exception
    {
    	super.setUp();
    	
        // Initialise the service beans
        initServices();    
        
        // Setup test data
        setupTestData();
    }
    
    /**
     * Initialise the service beans.
     */
    protected void initServices()
    {
    	ApplicationContext applicationContext = getServer().getApplicationContext();
    	
    	// Common test utils
    	utils = new CommonRMTestUtils(applicationContext);
    	
        // Get services
        nodeService = (NodeService)applicationContext.getBean("NodeService");
        contentService = (ContentService)applicationContext.getBean("ContentService");
        retryingTransactionHelper = (RetryingTransactionHelper)applicationContext.getBean("retryingTransactionHelper");
        namespaceService = (NamespaceService)applicationContext.getBean("NamespaceService");
        searchService = (SearchService)applicationContext.getBean("SearchService");
        policyComponent = (PolicyComponent)applicationContext.getBean("policyComponent");  
        dictionaryService = (DictionaryService)applicationContext.getBean("DictionaryService");
        siteService = (SiteService)applicationContext.getBean("SiteService");
        authorityService = (AuthorityService)applicationContext.getBean("AuthorityService");
        authenticationService = (MutableAuthenticationService)applicationContext.getBean("AuthenticationService");
        personService = (PersonService)applicationContext.getBean("PersonService");
        
        // Get RM services
        rmService = (RecordsManagementService)applicationContext.getBean("RecordsManagementService");
        dispositionService = (DispositionService)applicationContext.getBean("DispositionService");
        eventService = (RecordsManagementEventService)applicationContext.getBean("RecordsManagementEventService");
        adminService = (RecordsManagementAdminService)applicationContext.getBean("RecordsManagementAdminService");
        actionService = (RecordsManagementActionService)applicationContext.getBean("RecordsManagementActionService");
        rmSearchService = (RecordsManagementSearchService)applicationContext.getBean("RecordsManagementSearchService");
        securityService = (RecordsManagementSecurityService)applicationContext.getBean("RecordsManagementSecurityService");
        auditService = (RecordsManagementAuditService)applicationContext.getBean("RecordsManagementAuditService");
        capabilityService = (CapabilityService)applicationContext.getBean("CapabilityService");
        vitalRecordService = (VitalRecordService)applicationContext.getBean("VitalRecordService");
    }
    
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
                
                // Do the tear down 
                tearDownImpl();
                
                return null;
            }
        });       
    }
    
    /**
     * Tear down implementation
     */
    protected void tearDownImpl() 
    {
        // Delete the folder
        nodeService.deleteNode(folder);
        
        // Delete the site
        siteService.deleteSite(SITE_ID);
    }
    
    /**
     * Setup test data for tests
     */
    protected void setupTestData()
    {
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
                setupTestDataImpl();
                return null;
            }
        });
    }
    
    /**
     * Impl of test data setup
     */
    protected void setupTestDataImpl()
    {
        storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        rootNodeRef = nodeService.getRootNode(storeRef);
        
        // Create folder
        String containerName = "RM2_" + System.currentTimeMillis();
        Map<QName, Serializable> containerProps = new HashMap<QName, Serializable>(1);
        containerProps.put(ContentModel.PROP_NAME, containerName);
        folder = nodeService.createNode(
              rootNodeRef, 
              ContentModel.ASSOC_CHILDREN, 
              QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, containerName), 
              ContentModel.TYPE_FOLDER,
              containerProps).getChildRef();
        assertNotNull("Could not create base folder", folder);
        
        // Create the site
        siteInfo = siteService.createSite("preset", SITE_ID, "title", "descrition", SiteVisibility.PUBLIC, RecordsManagementModel.TYPE_RM_SITE);
        filePlan = siteService.getContainer(SITE_ID, RmSiteType.COMPONENT_DOCUMENT_LIBRARY);
        assertNotNull("Site document library container was not created successfully.", filePlan);
                        
        recordSeries = rmService.createRecordCategory(filePlan, "recordSeries");
        assertNotNull("Could not create record category with no disposition schedule", recordSeries);
        
        recordCategory = rmService.createRecordCategory(recordSeries, "rmContainer");
        assertNotNull("Could not create record category", recordCategory);
        
        // Make vital record
        vitalRecordService.setVitalRecordDefintion(recordCategory, true, new Period("week|1"));
        
        // Create disposition schedule
        dispositionSchedule = utils.createBasicDispositionSchedule(recordCategory);
        
        // Create RM folder
        recordFolder = rmService.createRecordFolder(recordCategory, "rmFolder");
        assertNotNull("Could not create rm folder", recordFolder);
        recordFolder2 = rmService.createRecordFolder(recordCategory, "rmFolder2");
        assertNotNull("Could not create rm folder 2", recordFolder2);
    }
}
