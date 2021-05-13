
/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.opencmis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.PropertyDefinitionWrapper;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.audit.AuditComponentImpl;
import org.alfresco.repo.audit.AuditServiceImpl;
import org.alfresco.repo.audit.UserAuditFilter;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.domain.audit.AuditDAO;
import org.alfresco.repo.domain.node.ContentDataWithId;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.version.VersionableAspectTest;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ExtensionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.GUID;

/**
 * OpenCMIS tests.
 * 
 * @author steveglover
 *
 */
@Category(LuceneTests.class)
public class CMISTest
{
    private static final QName TEST_START_TASK = QName.createQName("http://www.alfresco.org/model/workflow/test/1.0", "startTaskVarScriptAssign");
    private static final QName TEST_WORKFLOW_TASK = QName.createQName("http://www.alfresco.org/model/workflow/test/1.0", "assignVarTask");
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext(new String[]{ApplicationContextHelper.CONFIG_LOCATIONS[0],"classpath:test-cmisinteger_modell-context.xml"});

    private FileFolderService fileFolderService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private ContentService contentService;
    private Repository repositoryHelper;
    private VersionService versionService;
    private LockService lockService;
    private TaggingService taggingService;
    private NamespaceService namespaceService;
    private AuthorityService authorityService;
    private AuditModelRegistryImpl auditSubsystem;
    private PermissionService permissionService;
    private DictionaryDAO dictionaryDAO;
    private CMISDictionaryService cmisDictionaryService;
    private AuditDAO auditDAO;
    private ActionService actionService;
    private RuleService ruleService;
    private NodeArchiveService nodeArchiveService;
    private DictionaryService dictionaryService;
    private WorkflowService workflowService;
    private WorkflowAdminService workflowAdminService;
    private AuthenticationContext authenticationContext;
    private TenantAdminService tenantAdminService;
    private TenantService tenantService;
    private SearchService searchService;
    private java.util.Properties globalProperties;
    private AuditComponentImpl auditComponent;
    private PersonService personService;
    private SiteService siteService;
    private MutableAuthenticationService authenticationService;

    private AlfrescoCmisServiceFactory factory;
	
    private CMISConnector cmisConnector;
    
    private NodeDAO nodeDAO;

    public static class SimpleCallContext implements CallContext
    {
    	private final Map<String, Object> contextMap = new HashMap<String, Object>();
    	private CmisVersion cmisVersion;

    	public SimpleCallContext(String user, String password, CmisVersion cmisVersion)
    	{
    		contextMap.put(USERNAME, user);
    		contextMap.put(PASSWORD, password);
    		this.cmisVersion = cmisVersion;
    	}

    	public String getBinding()
    	{
    		return BINDING_LOCAL;
    	}

    	public Object get(String key)
    	{
    		return contextMap.get(key);
    	}

    	public String getRepositoryId()
    	{
    		return (String) get(REPOSITORY_ID);
    	}

    	public String getUsername()
    	{
    		return (String) get(USERNAME);
    	}

    	public String getPassword()
    	{
    		return (String) get(PASSWORD);
    	}

    	public String getLocale()
    	{
    		return null;
    	}

    	public BigInteger getOffset()
    	{
    		return (BigInteger) get(OFFSET);
    	}

    	public BigInteger getLength()
    	{
    		return (BigInteger) get(LENGTH);
    	}

    	public boolean isObjectInfoRequired()
    	{
    		return false;
    	}

    	public File getTempDirectory()
    	{
    		return null;
    	}

    	public int getMemoryThreshold()
    	{
    		return 0;
    	}

        public long getMaxContentSize()
        {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean encryptTempFiles()
        {
            return false;
        }

        @Override
        public CmisVersion getCmisVersion()
        {
            return cmisVersion;
        }
    }

    @Before
    public void before()
    {
        this.actionService = (ActionService)ctx.getBean("actionService");
        this.ruleService = (RuleService)ctx.getBean("ruleService");
    	this.fileFolderService = (FileFolderService)ctx.getBean("FileFolderService");
    	this.transactionService = (TransactionService)ctx.getBean("transactionService");
    	this.nodeService = (NodeService)ctx.getBean("NodeService");
    	this.contentService = (ContentService)ctx.getBean("ContentService");
        this.versionService = (VersionService) ctx.getBean("versionService");
        this.lockService = (LockService) ctx.getBean("lockService");
        this.taggingService = (TaggingService) ctx.getBean("TaggingService");
        this.namespaceService = (NamespaceService) ctx.getBean("namespaceService");
        this.repositoryHelper = (Repository)ctx.getBean("repositoryHelper");
    	this.factory = (AlfrescoCmisServiceFactory)ctx.getBean("CMISServiceFactory");
        this.versionService = (VersionService) ctx.getBean("versionService");
    	this.cmisConnector = (CMISConnector) ctx.getBean("CMISConnector");
        this.nodeDAO = (NodeDAO) ctx.getBean("nodeDAO");
        this.authorityService = (AuthorityService)ctx.getBean("AuthorityService");
        this.auditSubsystem = (AuditModelRegistryImpl) ctx.getBean("Audit");
        this.permissionService = (PermissionService) ctx.getBean("permissionService");
    	this.dictionaryDAO = (DictionaryDAO)ctx.getBean("dictionaryDAO");
    	this.cmisDictionaryService = (CMISDictionaryService)ctx.getBean("OpenCMISDictionaryService1.1");
        this.auditDAO = (AuditDAO) ctx.getBean("auditDAO");
        this.nodeArchiveService = (NodeArchiveService) ctx.getBean("nodeArchiveService");
        this.dictionaryService = (DictionaryService) ctx.getBean("dictionaryService");
        this.workflowService = (WorkflowService) ctx.getBean("WorkflowService");
        this.workflowAdminService = (WorkflowAdminService) ctx.getBean("workflowAdminService");
        this.authenticationContext = (AuthenticationContext) ctx.getBean("authenticationContext");
        this.tenantAdminService = (TenantAdminService) ctx.getBean("tenantAdminService");
        this.tenantService = (TenantService) ctx.getBean("tenantService");
        this.searchService = (SearchService) ctx.getBean("SearchService");
        this.auditComponent = (AuditComponentImpl) ctx.getBean("auditComponent");
        this.personService = (PersonService) ctx.getBean("personService");
        this.siteService = (SiteService) ctx.getBean("siteService");
        this.authenticationService = (MutableAuthenticationService) ctx.getBean("AuthenticationService");

        this.globalProperties = (java.util.Properties) ctx.getBean("global-properties");
        this.globalProperties.setProperty(VersionableAspectTest.AUTO_VERSION_PROPS_KEY, "true");
    }
    
    @After
    public void after()
    {
        this.globalProperties.setProperty(VersionableAspectTest.AUTO_VERSION_PROPS_KEY, "false");
    }
    
    /**
     * MNT-10868 CMIS: Incorrect value of Latest Major version on Versions and Properties tabs.
     */
    @Test
    public void testIsLatestMajorVersionMNT10868()
    {
        CallContext context = new SimpleCallContext("admin", "admin", CmisVersion.CMIS_1_0);
        
        String repositoryId = null;
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

         CmisService cmisService = factory.getService(context);
         try
         {
             // get repository id
             List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
             assertTrue(repositories.size() > 0);
             RepositoryInfo repo = repositories.get(0);
             repositoryId = repo.getId();
             final String folderName = "testfolder" + GUID.generate();
             final String docName = "testdoc.txt" + GUID.generate();
             final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
             {
                 @Override
                 public FileInfo execute() throws Throwable
                 {
                     NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                     FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                     nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                    
                     FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                     nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);
                     nodeService.addAspect(fileInfo.getNodeRef(), ContentModel.ASPECT_VERSIONABLE, null);

                     return fileInfo;
                 }
             });
             
             ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true, IncludeRelationships.NONE, null, false, true, null);
             
             PropertyData<?> pd = getPropIsLatestMajorVersion(objectData);
             
             if (pd != null)
             {
                 assertTrue("The CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION should be true as major version was created", (Boolean) pd.getValues().get(0));
             }
                
             nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_TITLE, docName);
             
             // Create minor version   
             transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
             {
                   public Object execute() throws Throwable
                   {
                       // get an updating writer
                       ContentWriter writer = contentService.getWriter(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
                      
                       writer.setMimetype("text/plain");
                      
                       writer.putContent("New Version");
                       return null;
                   }
            });
                
            objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true, IncludeRelationships.NONE, null, false, true, null);
            
            pd = getPropIsLatestMajorVersion(objectData);
            
            if (pd != null)
            {
                assertFalse("The CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION should be false as minor version was created", (Boolean) pd.getValues().get(0));
            }
        }
        finally
        {
            cmisService.close();
        }
    }
    
    private PropertyData<?> getPropIsLatestMajorVersion(ObjectData objectData)
    {
        List<PropertyData<?>> properties = objectData.getProperties().getPropertyList();
        boolean found = false;
        PropertyData<?> propIsLatestMajorVersion = null;
        for (PropertyData<?> property : properties)
        {
            if (property.getId().equals(PropertyIds.IS_LATEST_MAJOR_VERSION))
            {
                found = true;
                propIsLatestMajorVersion = property;
                break;
            }
        }
        //properties..contains(PropertyIds.IS_LATEST_MAJOR_VERSION);
        assertTrue("The PropertyIds.IS_LATEST_MAJOR_VERSION property was not found", found);
        if (found)
        {
            return propIsLatestMajorVersion;
        }
        
        return null;
    }

    /**
     * Test for MNT-9203.
     */
    @Test
    public void testCheckIn()
    {
        String repositoryId = null;
        ObjectData objectData = null;
        Holder<String> objectId = null;
        CallContext context = new SimpleCallContext("admin", "admin", CmisVersion.CMIS_1_0);

        final String folderName = "testfolder." + GUID.generate();
        final String docName = "testdoc.txt." + GUID.generate();
        final String customModel = "cmistest.model";

        final QName testCustomTypeQName = QName.createQName(customModel, "sop");
        final QName authorisedByQname = QName.createQName(customModel, "authorisedBy");
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        try
        {
            final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
            {
                @Override
                public FileInfo execute() throws Throwable
                {
                    NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                    FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                    nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);

                    FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, testCustomTypeQName);
                    Map<QName, Serializable> customProperties = new HashMap<QName, Serializable>();

                    customProperties.put(authorisedByQname, "customPropertyString");
                    customProperties.put(ContentModel.PROP_NAME, docName);
                    nodeService.setProperties(fileInfo.getNodeRef(), customProperties);

                    return fileInfo;
                }
            });

            CmisService service = factory.getService(context);
            try
            {
                List<RepositoryInfo> repositories = service.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                repositoryId = repo.getId();
                objectData = service.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true, IncludeRelationships.NONE, null, false, true, null);

                // checkout
                objectId = new Holder<String>(objectData.getId());
                service.checkOut(repositoryId, objectId, null, new Holder<Boolean>(true));
            }
            finally
            {
                service.close();
            }

            try
            {
                service = factory.getService(context);

                PropertyStringImpl prop = new PropertyStringImpl();
                prop.setId("abc:" + authorisedByQname.toPrefixString());
                prop.setValue(null);

                Collection<PropertyData<?>> propsList = new ArrayList<PropertyData<?>>();
                propsList.add(prop);

                Properties properties = new PropertiesImpl(propsList);

                // checkIn on pwc
                service.checkIn(repositoryId, objectId, false, properties, null, null, null, null, null, null);
            }
            finally
            {
                service.close();
            }
            // check that value is null
            assertTrue(nodeService.getProperty(fileInfo.getNodeRef(), authorisedByQname) == null);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * Test for MNT-10537.
     */
    @Test
    public void testModelAvailability() throws Exception
    {
        final WorkflowDeployer testWorkflowDeployer = new WorkflowDeployer();

        // setup dependencies
        testWorkflowDeployer.setTransactionService(transactionService);
        testWorkflowDeployer.setWorkflowService(workflowService);
        testWorkflowDeployer.setWorkflowAdminService(workflowAdminService);
        testWorkflowDeployer.setAuthenticationContext(authenticationContext);
        testWorkflowDeployer.setDictionaryDAO(dictionaryDAO);
        testWorkflowDeployer.setTenantAdminService(tenantAdminService);
        testWorkflowDeployer.setTenantService(tenantService);
        testWorkflowDeployer.setNodeService(nodeService);
        testWorkflowDeployer.setNamespaceService(namespaceService);
        testWorkflowDeployer.setSearchService(searchService);

        // populate workflow parameters
        java.util.Properties props = new java.util.Properties();
        props.setProperty(WorkflowDeployer.ENGINE_ID, "activiti");
        props.setProperty(WorkflowDeployer.LOCATION, "activiti/testCustomActiviti.bpmn20.xml");
        props.setProperty(WorkflowDeployer.MIMETYPE, "text/xml");
        props.setProperty(WorkflowDeployer.REDEPLOY, Boolean.FALSE.toString());

        List<java.util.Properties> definitions = new ArrayList<java.util.Properties>(1);
        definitions.add(props);

        testWorkflowDeployer.setWorkflowDefinitions(definitions);

        List<String> models = new ArrayList<String>(1);
        models.add("activiti/testWorkflowModel.xml");

        testWorkflowDeployer.setModels(models);

        // deploy test workflow
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        txnHelper.setForceWritable(true);
        txnHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                return AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        testWorkflowDeployer.init();
                        return null;
                    }
                }, AuthenticationUtil.getSystemUserName());
            }

        }, false, true);

        org.alfresco.service.cmr.dictionary.TypeDefinition startTaskTypeDefinition = this.dictionaryService.getType(TEST_START_TASK);
        org.alfresco.service.cmr.dictionary.TypeDefinition workflowTaskTypeDefinition = this.dictionaryService.getType(TEST_WORKFLOW_TASK);

        // check that workflow types were correctly bootstrapped
        assertNotNull(startTaskTypeDefinition);
        assertNotNull(workflowTaskTypeDefinition);

        // caches are refreshed asynchronously
        Thread.sleep(5000);

        // check that loaded model is available via CMIS API
        CallContext context = new SimpleCallContext("admin", "admin", CmisVersion.CMIS_1_1);
        CmisService service = factory.getService(context);
        try
        {
            List<RepositoryInfo> repositories = service.getRepositoryInfos(null);
            assertTrue(repositories.size() > 0);
            List<TypeDefinitionContainer> container = service.getTypeDescendants(repositories.get(0).getId(), null, new BigInteger("-1"), true, null);
            assertTrue("Workflow model haven't been loaded", container.toString().contains("testwf:startTaskVarScriptAssign"));
        }
        finally
        {
            service.close();
        }
    }

    private FileInfo createContent(final String folderName, final String docName,
            final boolean isRule)
    {
        return createContent(null, folderName, docName, isRule);
    }

    private FileInfo createContent(final FileInfo parentFolder, final String folderName, final String docName, final boolean isRule)
    {
        final FileInfo folderInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
        {
            @Override
            public FileInfo execute() throws Throwable
            {
                NodeRef nodeRef;

                if (parentFolder != null)
                {
                    nodeRef = parentFolder.getNodeRef();
                }
                else
                {
                    nodeRef = repositoryHelper.getCompanyHome();
                }
                
                FileInfo folderInfo = fileFolderService.create(nodeRef, folderName, ContentModel.TYPE_FOLDER);
                nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                assertNotNull(folderInfo);

                FileInfo fileInfo;
                if (docName != null)
                {
                    fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);
                    assertNotNull(fileInfo);
                }

                if (isRule)
                {
                    Rule rule = addRule(true, folderName);

                    assertNotNull(rule);

                    // Attach the rule to the node
                    ruleService.saveRule(folderInfo.getNodeRef(), rule);

                    assertTrue(ruleService.getRules(folderInfo.getNodeRef()).size() > 0);
                }

                return folderInfo;
            }
        });

        return folderInfo;
    }

    private Rule addRule(boolean isAppliedToChildren, String title)
    {

        // Rule properties
        Map<String, Serializable> conditionProps = new HashMap<String, Serializable>();
        conditionProps.put(ComparePropertyValueEvaluator.PARAM_VALUE, ".txt");

        Map<String, Serializable> actionProps = new HashMap<String, Serializable>();
        actionProps.put(AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);

        List<String> ruleTypes = new ArrayList<String>(1);
        ruleTypes.add(RuleType.INBOUND);

        // Create the action
        org.alfresco.service.cmr.action.Action action = actionService.createAction(title);
        action.setParameterValues(conditionProps);

        ActionCondition actionCondition = actionService.createActionCondition(ComparePropertyValueEvaluator.NAME);
        actionCondition.setParameterValues(conditionProps);
        action.addActionCondition(actionCondition);

        // Create the rule
        Rule rule = new Rule();
        rule.setRuleTypes(ruleTypes);
        rule.setTitle(title);
        rule.setDescription("description");
        rule.applyToChildren(isAppliedToChildren);
        rule.setAction(action);

        return rule;
    }

    private <T extends Object> T withCmisService(CmisServiceCallback<T> callback)
    {
        return withCmisService(callback, CmisVersion.CMIS_1_0);
    }

    private <T extends Object> T withCmisService(CmisServiceCallback<T> callback, CmisVersion cmisVersion)
    {
        return withCmisService("admin", "admin", callback, cmisVersion);
    }

    private <T extends Object> T withCmisService(String username, String password, CmisServiceCallback<T> callback, CmisVersion cmisVersion)
    {
        CmisService cmisService = null;

        try
        {
            CallContext context = new SimpleCallContext(username, password, cmisVersion);
            cmisService = factory.getService(context);
            T ret = callback.execute(cmisService);
            return ret;
        }
        finally
        {
            if(cmisService != null)
            {
                cmisService.close();
            }
        }
    }
    
    private static interface CmisServiceCallback<T>
    {
    	T execute(CmisService cmisService);
    }

    /**
     * ALF-18006 Test content mimetype auto-detection into CmisStreamInterceptor when "Content-Type" is not defined.
     */
    @Test
    public void testContentMimeTypeDetection()
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        FileFolderService ffs = serviceRegistry.getFileFolderService();
        AuthenticationComponent authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        final String isoEncoding = "ISO-8859-1";
        final String utfEncoding = "UTF-8";

        // get repository id
    	List<RepositoryInfo> repositories = withCmisService(new CmisServiceCallback<List<RepositoryInfo>>()
    	{
			@Override
			public List<RepositoryInfo> execute(CmisService cmisService)
			{
	            List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
				return repositories;
			}
    	});

        assertTrue(repositories.size() > 0);
        RepositoryInfo repo = repositories.get(0);
        final String repositoryId = repo.getId();

        // create simple text plain content
        final PropertiesImpl properties = new PropertiesImpl();
        String objectTypeId = "cmis:document";
        properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
        String fileName = "textFile" + GUID.generate();
        properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));
        final ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");
        
        String objectId = withCmisService(new CmisServiceCallback<String>()
        {
			@Override
			public String execute(CmisService cmisService)
			{
				String objectId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().getId(), contentStream, VersioningState.MAJOR, null, null);
				return objectId;
			}
        });

        final Holder<String> objectIdHolder = new Holder<String>(objectId);
        final String path = "/" + fileName;

        // create content stream with undefined mimetype and file name
        {
            final ContentStreamImpl contentStreamHTML = new ContentStreamImpl(null, null, "<html><head><title> Hello </title></head><body><p> Test html</p></body></html></body></html>");
            withCmisService(new CmisServiceCallback<Void>()
            {
				@Override
				public Void execute(CmisService cmisService)
				{
					cmisService.setContentStream(repositoryId, objectIdHolder, true, null, contentStreamHTML, null);
					return null;
				}
            });

            // check mimetype
            ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
            {
				@Override
				public ObjectData execute(CmisService cmisService)
				{
					return cmisService.getObjectByPath(repositoryId, path, null, false, IncludeRelationships.NONE, null, false, false, null);
				}
            });

            final String objectId1 = objectData.getId();
            String contentType = withCmisService(new CmisServiceCallback<String>()
            {
				@Override
				public String execute(CmisService cmisService)
				{
					String contentType = cmisService.getObjectInfo(repositoryId, objectId1).getContentType();
					return contentType;
				}
            });
            assertEquals("Mimetype is not defined correctly.", MimetypeMap.MIMETYPE_HTML, contentType);

            // check that the encoding is detected correctly
            checkEncoding(ffs, authenticationComponent, objectData, utfEncoding);
        }

        // create content stream with mimetype and encoding as UTF-8
        {
            String mimeType = MimetypeMap.MIMETYPE_TEXT_PLAIN + "; charset="+isoEncoding;
            // NOTE that we intentionally specify the wrong charset here. 
            // Alfresco will detect the encoding (as UTF-8 - given by the ContentStreamImpl constructor)
            final ContentStreamImpl contentStreamHTML = new ContentStreamImpl(null, mimeType, "<html><head><title> Hello </title></head><body><p> Test html</p></body></html></body></html>");
            withCmisService(new CmisServiceCallback<Void>()
            {
				@Override
				public Void execute(CmisService cmisService)
				{
                    Holder<String> latestObjectIdHolder = getHolderOfObjectOfLatestVersion(cmisService, repositoryId, objectIdHolder);
                    cmisService.setContentStream(repositoryId, latestObjectIdHolder, true, null, contentStreamHTML, null);
					return null;
				}
            });

            // check mimetype
            final ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
            {
				@Override
				public ObjectData execute(CmisService cmisService)
				{
					ObjectData objectData = cmisService.getObjectByPath(repositoryId, path, null, false, IncludeRelationships.NONE, null, false, false, null);
					return objectData;
				}
            });
            String contentType = withCmisService(new CmisServiceCallback<String>()
            {
				@Override
				public String execute(CmisService cmisService)
				{
					String contentType = cmisService.getObjectInfo(repositoryId, objectData.getId()).getContentType();
					return contentType;
				}
            });
            assertEquals("Mimetype is not defined correctly.", MimetypeMap.MIMETYPE_TEXT_PLAIN, contentType);

            // check that the encoding is detected correctly
            checkEncoding(ffs, authenticationComponent, objectData, utfEncoding);
        }

        // create content stream with mimetype and encoding as ISO-8859-1
        {
            String mimeType = MimetypeMap.MIMETYPE_TEXT_PLAIN + "; charset=" + utfEncoding;
            // NOTE that we intentionally specify the wrong charset here.
            // Alfresco will detect the encoding (as ISO-8859-1 - given by the ContentStreamImpl with streams)
            String content = "<html><head><title>aegif Mind Share Leader Generating New Paradigms by aegif corporation</title></head><body><p> Test html</p></body></html></body></html>";
            byte[] buf = null;
            try
            {
                buf = content.getBytes(isoEncoding); // set the encoding here for the content stream
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }

            ByteArrayInputStream input = new ByteArrayInputStream(buf);

            final ContentStream contentStreamHTML = new ContentStreamImpl(null, BigInteger.valueOf(buf.length), mimeType, input);
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    Holder<String> latestObjectIdHolder = getHolderOfObjectOfLatestVersion(cmisService, repositoryId,
                            objectIdHolder);
                    cmisService.setContentStream(repositoryId, latestObjectIdHolder, true, null, contentStreamHTML, null);
                    return null;
                }
            });

            // check mimetype
            final ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
            {
                @Override
                public ObjectData execute(CmisService cmisService)
                {
                    ObjectData objectData = cmisService.getObjectByPath(repositoryId, path, null, false,
                            IncludeRelationships.NONE, null, false, false, null);
                    return objectData;
                }
            });
            String contentType = withCmisService(new CmisServiceCallback<String>()
            {
                @Override
                public String execute(CmisService cmisService)
                {
                    String contentType = cmisService.getObjectInfo(repositoryId, objectData.getId()).getContentType();
                    return contentType;
                }
            });
            assertEquals("Mimetype is not defined correctly.", MimetypeMap.MIMETYPE_TEXT_PLAIN, contentType);

            // check that the encoding is detected correctly
            checkEncoding(ffs, authenticationComponent, objectData, isoEncoding);
        }

        // checkout/checkin object with mimetype and encoding
        {
            ObjectData objectDa = withCmisService(new CmisServiceCallback<ObjectData>()
            {
                @Override
                public ObjectData execute(CmisService cmisService)
                {
                    return cmisService.getObjectByPath(repositoryId, path, null, false, IncludeRelationships.NONE, null, false, false, null);
                }
            });


            objectIdHolder.setValue(objectDa.getId());
            withCmisService(new CmisServiceCallback<Void>()
            {
    			@Override
    			public Void execute(CmisService cmisService)
    			{
    				cmisService.checkOut(repositoryId, objectIdHolder, null, new Holder<Boolean>());
    				return null;
    			}
            });
            String mimeType = MimetypeMap.MIMETYPE_HTML + "; charset=UTF-8";
            final ContentStreamImpl contentStreamHTML = new ContentStreamImpl(null, mimeType, "<html><head><title> Hello </title></head><body><p> Test html</p></body></html></body></html>");
            withCmisService(new CmisServiceCallback<Void>()
            {
    			@Override
    			public Void execute(CmisService cmisService)
    			{
    				cmisService.checkIn(repositoryId, objectIdHolder, false, null, contentStreamHTML, "checkin", null, null, null, null);
    				return null;
    			}
            });

            // check mimetype
            final ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
            {
    			@Override
    			public ObjectData execute(CmisService cmisService)
    			{
    				ObjectData objectData = cmisService.getObjectByPath(repositoryId, path, null, false, IncludeRelationships.NONE, null, false, false, null);
    				return objectData;
    			}
            });
            String contentType = withCmisService(new CmisServiceCallback<String>()
            {
    			@Override
    			public String execute(CmisService cmisService)
    			{
    				String contentType = cmisService.getObjectInfo(repositoryId, objectData.getId()).getContentType();
    				return contentType;
    			}
            });
            assertEquals("Mimetype is not defined correctly.", MimetypeMap.MIMETYPE_HTML, contentType);

            checkEncoding(ffs, authenticationComponent, objectData, utfEncoding);
        }
    }

    protected void checkEncoding(FileFolderService ffs, AuthenticationComponent authenticationComponent,
            final ObjectData objectData, String expectedEncoding)
    {
        // Authenticate as system to check the properties in alfresco
        authenticationComponent.setSystemUserAsCurrentUser();
        try
        {
            NodeRef doc1NodeRef = cmisIdToNodeRef(objectData.getId());
            doc1NodeRef.getId();

            FileInfo fileInfo = ffs.getFileInfo(doc1NodeRef);
            Map<QName, Serializable> properties2 = fileInfo.getProperties();

            ContentDataWithId contentData = (ContentDataWithId) properties2
                    .get(QName.createQName("{http://www.alfresco.org/model/content/1.0}content"));
            String encoding = contentData.getEncoding();
            
            assertEquals(expectedEncoding, encoding);
        }
        finally
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
    }
    /**
     * Turns a CMIS id into a node ref
     * @param nodeId
     * @return
     */
    private NodeRef cmisIdToNodeRef(String nodeId)
    {
        int idx = nodeId.indexOf(";");
        if(idx != -1)
        {
            nodeId = nodeId.substring(0, idx);
        }
        NodeRef nodeRef = new NodeRef(nodeId);
        return nodeRef;
    }
    private Holder<String> getHolderOfObjectOfLatestVersion(CmisService cmisService, String repositoryId, Holder<String> currentHolder)
    {
        ObjectData oData = cmisService.getObjectOfLatestVersion(repositoryId, currentHolder.getValue(), null, Boolean.FALSE, null, null, null, null, null, null, null);
        return new Holder<String>(oData.getId());
    }

    /**
     * ALF-20389 Test Alfresco cmis stream interceptor that checks content stream for mimetype. Only ContentStreamImpl extensions should take palace.
     */
    @Test
    public void testGetRepositoryInfos()
    {
        boolean cmisEx = false;
        List<RepositoryInfo> infoDataList = null;
        try
        {
            infoDataList = withCmisService(new CmisServiceCallback<List<RepositoryInfo>>()
            {
                @Override
                public List<RepositoryInfo> execute(CmisService cmisService)
                {
                    ExtensionDataImpl result = new ExtensionDataImpl();
                    List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();
                    result.setExtensions(extensions);

                    return cmisService.getRepositoryInfos(result);
                }
            });
        }
        catch (CmisRuntimeException e)
        {
            cmisEx = true;
        }

        assertNotNull(cmisEx ? "CmisRuntimeException was thrown. Please, take a look on ALF-20389" : "No CMIS repository information was retrieved", infoDataList);
    }

    private static class TestContext
    {
        private String repositoryId = null;
        @SuppressWarnings("unused")
        private ObjectData objectData = null;
        private Holder<String> objectId = null;
        
        public TestContext()
        {
            super();
        }
        
        public String getRepositoryId()
        {
            return repositoryId;
        }
        
        public void setRepositoryId(String repositoryId)
        {
            this.repositoryId = repositoryId;
        }
        
        public void setObjectData(ObjectData objectData)
        {
            this.objectData = objectData;
        }
        
        public Holder<String> getObjectId()
        {
            return objectId;
        }
        
        public void setObjectId(Holder<String> objectId)
        {
            this.objectId = objectId;
        }
    }

    /**
     * Test for ALF-16310.
     * 
     * Check that, for AtomPub binding, cancel checkout on the originating checked out document i.e. not the working
     * copy throws an exception and does not delete the document.
     */
    @SuppressWarnings("unchecked")
	@Test
    public void testCancelCheckout()
    {
        final TestContext testContext = new TestContext();

    	final String folderName = "testfolder." + GUID.generate();
    	final String docName = "testdoc.txt." + GUID.generate();

    	AuthenticationUtil.pushAuthentication();
    	AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    	try
    	{
    		final FileInfo folderInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
	    	{
				@Override
				public FileInfo execute() throws Throwable
				{
					NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

			    	FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
			    	nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
			    	
			    	FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
			    	nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);
	
					return folderInfo;
				}
			});

    		final ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
		    {
		        @Override
		        public ObjectData execute(CmisService cmisService)
		        {
		            List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
		            assertTrue(repositories.size() > 0);
		            RepositoryInfo repo = repositories.get(0);
		            String repositoryId = repo.getId();
		            testContext.setRepositoryId(repositoryId);
		            ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true,
		                    IncludeRelationships.NONE, null, false, true, null);
		            testContext.setObjectData(objectData);

		            // checkout
		            Holder<String> objectId = new Holder<String>(objectData.getId());
		            testContext.setObjectId(objectId);
		            cmisService.checkOut(repositoryId, objectId, null, new Holder<Boolean>(true));

		            return objectData;
		        }
		    });

	    	// AtomPub cancel checkout
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    try
                    {
        	    		// check allowable actions
        	        	ObjectData originalDoc = cmisService.getObject(testContext.getRepositoryId(), objectData.getId(), null, true, IncludeRelationships.NONE, null, false, true, null);
        	        	AllowableActions allowableActions = originalDoc.getAllowableActions();
        	        	assertNotNull(allowableActions);
        	        	assertFalse(allowableActions.getAllowableActions().contains(Action.CAN_DELETE_OBJECT));
        
        	        	// try to cancel the checkout
        	        	cmisService.deleteObjectOrCancelCheckOut(testContext.getRepositoryId(), objectData.getId(), Boolean.TRUE, null);
        	        	fail();
        	        }
        	        catch(CmisConstraintException e)
        	        {
        	        	// expected
        	        }

        	        return null;
                }
            });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    // cancel checkout on pwc
                    cmisService.deleteObjectOrCancelCheckOut(testContext.getRepositoryId(), testContext.getObjectId().getValue(), Boolean.TRUE, null);
                    return null;
                }
	        });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
    	        	// get original document
    	        	ObjectData originalDoc = cmisService.getObject(testContext.getRepositoryId(), objectData.getId(), null, true, IncludeRelationships.NONE, null, false, true, null);
    	        	Map<String, PropertyData<?>> properties = originalDoc.getProperties().getProperties();
    	        	PropertyData<Boolean> isVersionSeriesCheckedOutProp = (PropertyData<Boolean>)properties.get(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
    	        	assertNotNull(isVersionSeriesCheckedOutProp);
    	        	Boolean isVersionSeriesCheckedOut = isVersionSeriesCheckedOutProp.getFirstValue();
    	        	assertNotNull(isVersionSeriesCheckedOut);
    	        	assertEquals(Boolean.FALSE, isVersionSeriesCheckedOut);
    	        	
    	        	return null;
                }
            });
    		
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
    	        	// delete original document
                    cmisService.deleteObject(testContext.getRepositoryId(), objectData.getId(), true, null);
    	        	return null;
                }
            });
        	
        	List<FileInfo> children = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<FileInfo>>()
	    	{
				@Override
				public List<FileInfo> execute() throws Throwable
				{
			    	List<FileInfo> children = fileFolderService.list(folderInfo.getNodeRef());
					return children;
				}
			});
        	assertEquals(0, children.size());
    	}
    	finally
    	{
    		AuthenticationUtil.popAuthentication();
    	}
    }
   
    /**
     * Test for MNT-13366.
     */
    @Test
    public void testDeleteTree()
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        FileInfo parentFolder = null;
        FileInfo childFolder1 = null;

        try
        {
            // Create a parent folder: parentFolder:
            String parentFolderName = "parentFolder" + GUID.generate();
            parentFolder = createContent(parentFolderName, null, false);
            final NodeRef parentFolderNodeRef = parentFolder.getNodeRef();
            final String parentFolderID = parentFolderNodeRef.getId();

            // Create a child folder: parentFolder -> childFolder1:
            String childFolder1Name = "childFolder1" + GUID.generate();
            childFolder1 = createContent(parentFolder, childFolder1Name, null, false);
            final NodeRef childFolder1NodeRef = childFolder1.getNodeRef();

            // Create a child folder for previous child folder, which will contain a file:
            // parentFolder -> childFolder1 -> childFolder2 -> testdoc.txt
            String childFolder2Name = "childFolder2" + GUID.generate();
            String docName = "testdoc.txt" + GUID.generate();
            final NodeRef childFolder2NodeRef = createContent(childFolder1, childFolder2Name, docName, false).getNodeRef();

            // Store a reference to the file "testdoc.txt" contained by childFolder2:
            List<FileInfo> childFolder2FileList = fileFolderService.list(childFolder2NodeRef);
            final NodeRef childFolder2FileNodeRef = childFolder2FileList.get(0).getNodeRef();

            List<RepositoryInfo> repositories = withCmisService(new CmisServiceCallback<List<RepositoryInfo>>()
            {
                @Override
                public List<RepositoryInfo> execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    return repositories;
                }
            });

            assertTrue(repositories.size() > 0);
            RepositoryInfo repo = repositories.get(0);
            final String repositoryId = repo.getId();

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {

                    // CMIS delete tree:
                    FailedToDeleteData failedItems = cmisService.deleteTree(repositoryId, parentFolderID, Boolean.TRUE,
                        UnfileObject.DELETE, Boolean.TRUE, null);

                    assertEquals(failedItems.getIds().size(), 0);

                    // Reference to the archive root node (the trash-can):
                    NodeRef archiveRootNode = nodeArchiveService.getStoreArchiveNode(repositoryHelper.getCompanyHome().getStoreRef());

                    // Get the archived ("canned") version of folders and file and check that hirarchy is correct:
                    // ArchiveRoot -> archivedParentFolder -> archivedChildFolder1 -> archivedChildFolder2 -> archivedChildFolder2File.

                    // Check parentFolder:
                    NodeRef archivedParentFolderNodeRef = nodeArchiveService.getArchivedNode(parentFolderNodeRef);
                    assertTrue(nodeService.getPrimaryParent(archivedParentFolderNodeRef).getParentRef().equals(archiveRootNode));

                    // Check childFolder1:               
                    NodeRef archivedChildFolder1NodeRef = nodeArchiveService.getArchivedNode(childFolder1NodeRef);
                    assertTrue(nodeService.getPrimaryParent(archivedChildFolder1NodeRef).getParentRef().equals(archivedParentFolderNodeRef));
                    assertFalse(nodeService.getPrimaryParent(archivedChildFolder1NodeRef).getParentRef().equals(archiveRootNode));

                    // Check childFolder2:                    
                    NodeRef archivedChildFolder2NodeRef = nodeArchiveService.getArchivedNode(childFolder2NodeRef);
                    assertTrue(nodeService.getPrimaryParent(archivedChildFolder2NodeRef).getParentRef().equals(archivedChildFolder1NodeRef));
                    assertFalse(nodeService.getPrimaryParent(archivedChildFolder2NodeRef).getParentRef().equals(archiveRootNode));

                    // Check childFolder2's file ("testdoc.txt"):                     
                    NodeRef archivedChildFolder2FileNodeRef = nodeArchiveService.getArchivedNode(childFolder2FileNodeRef);
                    assertTrue(nodeService.getPrimaryParent(archivedChildFolder2FileNodeRef).getParentRef().equals(archivedChildFolder2NodeRef));
                    assertFalse(nodeService.getPrimaryParent(archivedChildFolder2FileNodeRef).getParentRef().equals(archiveRootNode));
                    
                    return null;
                };
            });
        }
        finally
        {
            if (parentFolder != null && fileFolderService.exists(parentFolder.getNodeRef()))
            {
                fileFolderService.delete(parentFolder.getNodeRef());
            }

            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * Test for ALF-18151.
     */
    @Test
    public void testDeleteFolder()
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        final Map<FileInfo, Boolean> testFolderMap = new HashMap<FileInfo, Boolean>(4);

        try
        {
            // create folder with file
            String folderName = "testfolder" + GUID.generate();
            String docName = "testdoc.txt" + GUID.generate();
            FileInfo folder = createContent(folderName, docName, false);
            testFolderMap.put(folder, Boolean.FALSE);

            // create empty folder
            String folderNameEmpty = "testfolder_empty1" + GUID.generate();
            FileInfo folderEmpty = createContent(folderNameEmpty, null, false);
            testFolderMap.put(folderEmpty, Boolean.TRUE);

            // create folder with file
            String folderNameRule = "testfolde_rule" + GUID.generate();
            String docNameRule = "testdoc_rule.txt" + GUID.generate();
            FileInfo folderWithRule = createContent(folderNameRule, docNameRule, true);
            testFolderMap.put(folderWithRule, Boolean.FALSE);

            // create empty folder
            String folderNameEmptyRule = "testfolde_empty_rule1" + GUID.generate();
            FileInfo folderEmptyWithRule = createContent(folderNameEmptyRule, null, true);
            testFolderMap.put(folderEmptyWithRule, Boolean.TRUE);

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
    
                    for (Map.Entry<FileInfo, Boolean> entry : testFolderMap.entrySet())
                    {
                        ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + entry.getKey().getName(), null, true, IncludeRelationships.NONE, null, false, true, null);
    
                        Holder<String> objectId = new Holder<String>(objectData.getId());
    
                        try
                        {
                            // delete folder
                            cmisService.deleteObjectOrCancelCheckOut(repositoryId, objectId.getValue(), Boolean.TRUE, null);
                        }
                        catch (CmisConstraintException ex)
                        {
                            assertTrue(!entry.getValue());
                            continue;
                        }
    
                        assertTrue(entry.getValue());
                    }

                    return null;
                }
            });
        }
        finally
        {
            for (Map.Entry<FileInfo, Boolean> entry : testFolderMap.entrySet())
            {
                if (fileFolderService.exists(entry.getKey().getNodeRef()))
                {
                    fileFolderService.delete(entry.getKey().getNodeRef());
                }
            }

            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * Test
     * <ul>
     *   <li>MNT-8825: READ_ONLYLOCK prevent getAllVersions via new CMIS enpoint.</li>
     *   <li>ACE-762: BM-0012: NodeLockedException not handled by CMIS</li>
     * </ul>
     */
    @Test
    public void testOperationsOnReadOnlyLockedNode()
    {
        final String folderName = "testfolder." + GUID.generate();
        final String docName = "testdoc.txt." + GUID.generate();

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        try
        {
            final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
            {
                @Override
                public FileInfo execute() throws Throwable
                {
                    NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                    FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                    nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);

                    FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);

                    versionService.createVersion(fileInfo.getNodeRef(), new HashMap<String, Serializable>());
                    lockService.lock(fileInfo.getNodeRef(), LockType.READ_ONLY_LOCK, 0, true);

                    return fileInfo;
                }
            });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
                    ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true,
                            IncludeRelationships.NONE, null, false, true, null);

                    // Expect no failure
                    cmisService.getAllVersions(repositoryId, objectData.getId(), fileInfo.getNodeRef().getId(), null, true, null);

                    return null;
                }
            });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
                    ObjectData objectData = cmisService.getObjectByPath(
                            repositoryId, "/" + folderName + "/" + docName, null, true,
                            IncludeRelationships.NONE, null, false, true, null);
                    String objectId = objectData.getId();

                    // Expect failure as the node is locked
                    try
                    {
                        cmisService.deleteObject(repositoryId, objectId, true, null);
                        fail("Locked node should not be deletable.");
                    }
                    catch (CmisUpdateConflictException e)
                    {
                        // Expected
                    }

                    return null;
                }
            });

        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * ALF-18455
     */
    @Test
    public void testOrderByCreationAndModificationDate()
    {
        final List<FileInfo> nodes = new ArrayList<FileInfo>(10);
        final List<FileInfo> expectedChildrenByCreationDate = new ArrayList<FileInfo>(10);
        final List<FileInfo> expectedChildrenByModificationDate = new ArrayList<FileInfo>(10);

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        try
        {
	        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
	        {
	            @Override
	            public Void execute() throws Throwable
	            {
	                NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
	
	                String folderName = GUID.generate();
	                FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
	                nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
	                assertNotNull(folderInfo);
	                nodes.add(folderInfo);

	                for(int i = 0; i < 5; i++)
	                {
	                    String docName = GUID.generate();
	                    FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
	                    assertNotNull(fileInfo);
	                    nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);

	                    expectedChildrenByCreationDate.add(0, fileInfo);
		                nodes.add(fileInfo);

	                    // make sure there is some difference in creation times
	                    Thread.sleep(400);
	                }

	                // make modifications
	                for(int i = 5; i > 0; i--)
	                {
	                	FileInfo fileInfo = nodes.get(i);
	                    assertNotNull(fileInfo);
	                    nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_DESCRIPTION, GUID.generate());

	                    // "refresh" fileInfo
	                    fileInfo = fileFolderService.getFileInfo(fileInfo.getNodeRef());
	                    assertNotNull(fileInfo);
	                    expectedChildrenByModificationDate.add(0, fileInfo);

	                    // make sure there is some difference in modification times
	                    Thread.sleep(400);
	                }
	                
	                return null;
	            }
	        });
        }
        finally
        {
        	AuthenticationUtil.popAuthentication();
        }

        withCmisService(new CmisServiceCallback<Void>()
        {
            @Override
            public Void execute(CmisService cmisService)
            {
                // get repository id
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                String repositoryId = repo.getId();
    
                String folderId = nodes.get(0).getNodeRef().getId();
                String orderBy = PropertyIds.CREATION_DATE + " DESC";
                ObjectInFolderList children = cmisService.getChildren(repositoryId, folderId, null, orderBy, false, IncludeRelationships.NONE,
                		null, false, BigInteger.valueOf(Integer.MAX_VALUE), BigInteger.valueOf(0), null);
                int i = 0;
                for(ObjectInFolderData child : children.getObjects())
                {
                	Map<String, PropertyData<?>> properties = child.getObject().getProperties().getProperties();
    
                	PropertyData<?> pObjectId = properties.get(PropertyIds.VERSION_SERIES_ID);
                	String actualObjectId = (String)pObjectId.getFirstValue();
                	PropertyData<?> pCreationDate = properties.get(PropertyIds.CREATION_DATE);
                	GregorianCalendar actualCreationDate = (GregorianCalendar)pCreationDate.getFirstValue();
    
                	FileInfo expectedChild = expectedChildrenByCreationDate.get(i++);
                	assertEquals(expectedChild.getNodeRef().toString(), actualObjectId);
                	assertEquals(expectedChild.getCreatedDate().getTime(), actualCreationDate.getTimeInMillis());
                }
    
                orderBy = PropertyIds.LAST_MODIFICATION_DATE + " DESC";
                children = cmisService.getChildren(repositoryId, folderId, null, orderBy, false, IncludeRelationships.NONE,
                		null, false, BigInteger.valueOf(Integer.MAX_VALUE), BigInteger.valueOf(0), null);
                i = 0;
                for(ObjectInFolderData child : children.getObjects())
                {
                	Map<String, PropertyData<?>> properties = child.getObject().getProperties().getProperties();
    
                	PropertyData<?> pObjectId = properties.get(PropertyIds.VERSION_SERIES_ID);
                	String actualObjectId = (String)pObjectId.getFirstValue();
                	PropertyData<?> pModificationDate = properties.get(PropertyIds.LAST_MODIFICATION_DATE);
                	GregorianCalendar actualModificationDate = (GregorianCalendar)pModificationDate.getFirstValue();
    
                	FileInfo expectedChild = expectedChildrenByModificationDate.get(i++);
                	assertEquals(expectedChild.getNodeRef().toString(), actualObjectId);
                	assertEquals(expectedChild.getModifiedDate().getTime(), actualModificationDate.getTimeInMillis());
                }

                return null;
            }
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testSecondaryTypes()
    {
        final String aspectName = "P:cm:indexControl";

        // get repository id
        final String repositoryId = withCmisService(new CmisServiceCallback<String>()
        {
            @Override
            public String execute(CmisService cmisService)
            {
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                final String repositoryId = repo.getId();
                return repositoryId;
            }
        }, CmisVersion.CMIS_1_1);

        final String objectId = withCmisService(new CmisServiceCallback<String>()
        {
            @Override
            public String execute(CmisService cmisService)
            {
                final PropertiesImpl properties = new PropertiesImpl();
                String objectTypeId = "cmis:document";
                properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
                String fileName = "textFile" + GUID.generate();
                properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));
                final ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");

                String objectId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().getId(), contentStream, VersioningState.MAJOR, null, null);
                return objectId;
            }
        }, CmisVersion.CMIS_1_1);

        final Holder<String> objectIdHolder = new Holder<String>(objectId);

        withCmisService(new CmisServiceCallback<Void>()
        {
            @Override
            public Void execute(CmisService cmisService)
            {
                final PropertiesImpl properties = new PropertiesImpl();
                properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(aspectName)));

                cmisService.updateProperties(repositoryId, objectIdHolder, null, properties, null);
                return null;
            }
        }, CmisVersion.CMIS_1_1);

        final Properties currentProperties = withCmisService(new CmisServiceCallback<Properties>()
        {
            @Override
            public Properties execute(CmisService cmisService)
            {
                Properties properties = cmisService.getProperties(repositoryId, objectIdHolder.getValue(), null, null);
                return properties;
            }
        }, CmisVersion.CMIS_1_1);

        List<String> secondaryTypeIds = (List<String>) currentProperties.getProperties().get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues();

        assertTrue(secondaryTypeIds.contains(aspectName));
        // We don't actually want to add these! (REPO-2926)
        final Set<String> sysAspectsToAdd = new HashSet<>(Arrays.asList(
                "P:sys:undeletable",
                "P:sys:hidden"));
        // Pre-condition of further test is that these aspects are not present
        assertEquals(0, secondaryTypeIds.stream().filter(sysAspectsToAdd::contains).count());
        // We also want to check that existing sys aspects aren't accidentally removed
        assertTrue(secondaryTypeIds.contains("P:sys:localized"));

        // Check we can remove an aspect - through its absence
        secondaryTypeIds.remove(aspectName);
        // Check that attempts to update/add sys:* aspects are ignored
        secondaryTypeIds.addAll(sysAspectsToAdd);
        final PropertiesImpl newProperties = new PropertiesImpl();
        newProperties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondaryTypeIds));

        final String updatedName = "My_new_name_"+UUID.randomUUID().toString();
        newProperties.replaceProperty(new PropertyStringImpl(PropertyIds.NAME, updatedName));

        withCmisService(new CmisServiceCallback<Void>()
        {
            @Override
            public Void execute(CmisService cmisService)
            {
                Holder<String> latestObjectIdHolder = getHolderOfObjectOfLatestVersion(cmisService, repositoryId, objectIdHolder);
                // This will result in aspectName being removed
                // but that shouldn't mean that, for example, a cmis:name prop update gets ignored (MNT-18340)
                cmisService.updateProperties(repositoryId, latestObjectIdHolder, null, newProperties, null);
                return null;
            }
        }, CmisVersion.CMIS_1_1);

        Properties currentProperties1 = withCmisService(new CmisServiceCallback<Properties>()
        {
            @Override
            public Properties execute(CmisService cmisService)
            {
                Holder<String> latestObjectIdHolder = getHolderOfObjectOfLatestVersion(cmisService, repositoryId, objectIdHolder);
                Properties properties = cmisService.getProperties(repositoryId, latestObjectIdHolder.getValue(), null, null);
                return properties;
            }
        }, CmisVersion.CMIS_1_1);
        secondaryTypeIds = (List<String>) currentProperties1.getProperties().get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues();

        assertFalse(secondaryTypeIds.contains(aspectName));
        assertEquals(updatedName, currentProperties1.getProperties().get(PropertyIds.NAME).getFirstValue());
        // sys aspects must not be added through CMIS (REPO-2926)
        assertEquals(0, secondaryTypeIds.stream().filter(sysAspectsToAdd::contains).count());
        // Check pre-existing sys aspects aren't accidentally removed
        assertTrue(secondaryTypeIds.contains("P:sys:localized"));
    }

    /**
     * Test for MNT-9089
     */
    @Test
    public void testIntegerBoudaries() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        try
        {
	    	final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
	        {
	            @Override
	            public FileInfo execute() throws Throwable
	            {
	                NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
	                
	                QName testIntTypeQName = QName.createQName("http://testCMISIntegersModel/1.0/", "testintegerstype");
	
	                String folderName = GUID.generate();
	                FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
	                nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
	                assertNotNull(folderInfo);
	
	                String docName = GUID.generate();
	                FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, testIntTypeQName);
	                assertNotNull(fileInfo);
	                nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);

	                return fileInfo;
	            }
	        });

            // get repository id
	    	withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();

                    String objectIdStr = fileInfo.getNodeRef().toString();
                    
                    TypeDefinition typeDef = cmisService.getTypeDefinition(repositoryId, "D:tcim:testintegerstype", null);
                    
                    PropertyIntegerDefinitionImpl intNoBoundsTypeDef = 
                            (PropertyIntegerDefinitionImpl)typeDef.getPropertyDefinitions().get("tcim:int");
                    PropertyIntegerDefinitionImpl longNoBoundsTypeDef = 
                            (PropertyIntegerDefinitionImpl)typeDef.getPropertyDefinitions().get("tcim:long");
                    
                    PropertyIntegerDefinitionImpl intWithBoundsTypeDef = 
                            (PropertyIntegerDefinitionImpl)typeDef.getPropertyDefinitions().get("tcim:intwithbounds");
                    PropertyIntegerDefinitionImpl longWithBoundsTypeDef = 
                            (PropertyIntegerDefinitionImpl)typeDef.getPropertyDefinitions().get("tcim:longwithbounds");
                    
                    BigInteger minInteger = BigInteger.valueOf(Integer.MIN_VALUE);
                    BigInteger maxInteger = BigInteger.valueOf(Integer.MAX_VALUE);
                    
                    BigInteger minLong = BigInteger.valueOf(Long.MIN_VALUE);
                    BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
                    
                    // test for default boundaries
                    assertTrue(intNoBoundsTypeDef.getMinValue().equals(minInteger));
                    assertTrue(intNoBoundsTypeDef.getMaxValue().equals(maxInteger));
                    
                    assertTrue(longNoBoundsTypeDef.getMinValue().equals(minLong));
                    assertTrue(longNoBoundsTypeDef.getMaxValue().equals(maxLong));
                    
                    // test for pre-defined boundaries
                    assertTrue(intWithBoundsTypeDef.getMinValue().equals(BigInteger.valueOf(-10L)));
                    assertTrue(intWithBoundsTypeDef.getMaxValue().equals(BigInteger.valueOf(10L)));
                    
                    assertTrue(longWithBoundsTypeDef.getMinValue().equals(BigInteger.valueOf(-10L)));
                    assertTrue(longWithBoundsTypeDef.getMaxValue().equals(BigInteger.valueOf(10L)));
                    
                    try // try to overfloat long without boundaries
                    {
                        BigInteger aValue = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(1L));
                        setProperiesToObject(cmisService, repositoryId, objectIdStr, "tcim:long", aValue);
                        fail();
                    }
                    catch(Exception e)
                    {
                        assertTrue(e instanceof CmisConstraintException);
                    }
                    
                    try // try to overfloat int without boundaries
                    {
                        BigInteger aValue = BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.valueOf(1L));
                        setProperiesToObject(cmisService, repositoryId, objectIdStr, "tcim:int", aValue);
                        fail();
                    }
                    catch(Exception e)
                    {
                        assertTrue(e instanceof CmisConstraintException);
                    }
                    
                    try // try to overfloat int with boundaries
                    {
                        BigInteger aValue = BigInteger.valueOf( 11l );
                        setProperiesToObject(cmisService, repositoryId, objectIdStr, "tcim:intwithbounds", aValue);
                        fail();
                    }
                    catch(Exception e)
                    {
                        assertTrue(e instanceof CmisConstraintException);
                    }
                    
                    try // try to overfloat long with boundaries
                    {
                        BigInteger aValue = BigInteger.valueOf( 11l );
                        setProperiesToObject(cmisService, repositoryId, objectIdStr, "tcim:longwithbounds", aValue);
                        fail();
                    }
                    catch(Exception e)
                    {
                        assertTrue(e instanceof CmisConstraintException);
                    }

                    return null;
                }
            }, CmisVersion.CMIS_1_0);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    private void setProperiesToObject(CmisService cmisService, String repositoryId, String objectIdStr, String propertyStr, BigInteger bigIntValue) throws CmisConstraintException{
        Properties properties = cmisService.getProperties(repositoryId, objectIdStr, null, null);
        PropertyIntegerImpl pd = (PropertyIntegerImpl)properties.getProperties().get(propertyStr);
        pd.setValue(bigIntValue);
        
        Collection<PropertyData<?>> propsList = new ArrayList<PropertyData<?>>();
        propsList.add(pd);
        
        Properties newProps = new PropertiesImpl(propsList);
        
        cmisService.updateProperties(repositoryId, new Holder<String>(objectIdStr), null, newProps, null);
    }
    
    @Test
    public void testMNT9090() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        try
        {
	    	final FileInfo fileInfo = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
	        {
	            @Override
	            public FileInfo execute() throws Throwable
	            {
	                NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
	
	                String folderName = GUID.generate();
	                FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
	                nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
	                assertNotNull(folderInfo);
	
	                String docName = GUID.generate();
	                FileInfo fileInfo = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
	                assertNotNull(fileInfo);
	                nodeService.setProperty(fileInfo.getNodeRef(), ContentModel.PROP_NAME, docName);
	                
	                QName ASPECT_AUDIO = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, "audio");
	                Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
	                nodeService.addAspect(fileInfo.getNodeRef(), ASPECT_AUDIO, aspectProperties);

	                return fileInfo;
	            }
	        });

	    	withCmisService(new CmisServiceCallback<Void>()
	    	{
	    	    @Override
	    	    public Void execute(CmisService cmisService)
	    	    {
	    	        // get repository id
	    	        List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
	    	        assertTrue(repositories.size() > 0);
	    	        RepositoryInfo repo = repositories.get(0);
	    	        String repositoryId = repo.getId();

	    	        String objectIdStr = fileInfo.getNodeRef().toString();
	    	        Holder<String> objectId = new Holder<String>(objectIdStr);

	    	        // try to overflow the value
	    	        Object value = BigInteger.valueOf(Integer.MAX_VALUE + 1l);

	    	        Properties properties = new PropertiesImpl();
	    	        List<CmisExtensionElement> extensions = new ArrayList<CmisExtensionElement>();

	    	        CmisExtensionElement valueElem = new CmisExtensionElementImpl(CMISConnector.ALFRESCO_EXTENSION_NAMESPACE, "value", null, value.toString());
	    	        List<CmisExtensionElement> valueElems = new ArrayList<CmisExtensionElement>();
	    	        valueElems.add(valueElem);

	    	        List<CmisExtensionElement> children = new ArrayList<CmisExtensionElement>();
	    	        Map<String, String> attributes = new HashMap<String, String>();
	    	        attributes.put("propertyDefinitionId", "audio:trackNumber");
	    	        children.add(new CmisExtensionElementImpl(CMISConnector.ALFRESCO_EXTENSION_NAMESPACE, "propertyInteger", attributes, valueElems));

	    	        List<CmisExtensionElement> propertyValuesExtension = new ArrayList<CmisExtensionElement>();
	    	        propertyValuesExtension.add(new CmisExtensionElementImpl(CMISConnector.ALFRESCO_EXTENSION_NAMESPACE, CMISConnector.PROPERTIES, null, children));

	    	        CmisExtensionElement setAspectsExtension = new CmisExtensionElementImpl(CMISConnector.ALFRESCO_EXTENSION_NAMESPACE, CMISConnector.SET_ASPECTS, null, propertyValuesExtension);
	    	        extensions.add(setAspectsExtension);
	    	        properties.setExtensions(extensions);

	    	        // should throw a CMISConstraintException
	    	        cmisService.updateProperties(repositoryId, objectId, null, properties, null);
	    	        fail();

	    	        return null;
	    	    }
	    	}, CmisVersion.CMIS_1_0);
        }
        catch(CmisConstraintException e)
        {
        	assertTrue(e.getMessage().startsWith("Value is out of range for property"));
        	// ok
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * MNT-20139
     * CmisConnector returns wrong values for changeLogToken and hasMoreItems
     */
    @Test
    public void testGetContentChanges()
    {
        setupAudit();

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        try
        {
            // create folders with files
            createContent("testfolder" + GUID.generate(), "testdoc.txt" + GUID.generate(), false);
            createContent("testfolder" + GUID.generate(), "testdoc.txt" + GUID.generate(), false);
            createContent("testfolder" + GUID.generate(), "testdoc.txt" + GUID.generate(), false);

            Holder<String> changeLogToken = new Holder<String>();

            /*
             * GetContentChanges with maxitems = 2 and null changeLogToken
             * Check that changeLogToken should be the latest from the retrieved entries
             */
            ObjectList ol = this.cmisConnector.getContentChanges(changeLogToken, new BigInteger("2"));
            assertEquals(2, ol.getObjects().size());
            assertEquals("ChangeLogToken should be latest from retrieved entries.", "2", changeLogToken.getValue());
            assertTrue(ol.hasMoreItems());

            /*
             * GetContentChanges with maxitems = 2 and changeLogToken = 0
             * Check that changeLogToken should be the latest from the retrieved entries
             */
            changeLogToken.setValue(Integer.toString(0));
            ol = this.cmisConnector.getContentChanges(changeLogToken, new BigInteger("2"));
            assertEquals(2, ol.getObjects().size());
            assertEquals("ChangeLogToken should be latest from retrieved entries.", "2", changeLogToken.getValue());
            assertTrue(ol.hasMoreItems());

            /*
             * GetContentChanges with changeLogToken = maxChangeLogToken - 2
             * Check that changeLogToken is not null when the latest entries (fromToken) are retrieved
             */
            Long latestToken = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Long>()
            {
                public Long execute() throws Exception
                {
                    return Long.parseLong(cmisConnector.getRepositoryInfo(CmisVersion.CMIS_1_1).getLatestChangeLogToken());
                }
            }, true, false);

            Long fromToken = latestToken - 2;
            changeLogToken.setValue(fromToken.toString());

            ol = this.cmisConnector.getContentChanges(changeLogToken, new BigInteger("20"));
            assertEquals(3, ol.getObjects().size());
            assertNotNull(changeLogToken.getValue());
            assertEquals("ChangeLogToken should be the latest from all entries.", latestToken.toString(), changeLogToken.getValue());
            assertFalse(ol.hasMoreItems());
        }
        finally
        {
            auditSubsystem.destroy();
            AuthenticationUtil.popAuthentication();
        };
    }

    /**
     * MNT-10223
     * Check the IsLatestMajorVersion for a doc with minor version.
     */
    @SuppressWarnings("unused")
    @Test
    public void testIsLatestMajorVersion()
    {
        final TestContext testContext = new TestContext();

        // create simple text plain content
        final PropertiesImpl properties = new PropertiesImpl();
        String objectTypeId = "cmis:document";
        properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
        String fileName = "textFile" + GUID.generate();
        properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));
        final ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");

        withCmisService(new CmisServiceCallback<String>() {
            @Override
            public String execute(CmisService cmisService) {
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                String repositoryId = repo.getId();

                String objectId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().getId(), contentStream, VersioningState.MINOR, null, null);

                ObjectData cmidDoc = cmisService.getObject(repositoryId, objectId, null, true, IncludeRelationships.NONE, null, false, false, null);
                List<PropertyData<?>> properties = cmidDoc.getProperties().getPropertyList();
                boolean found = false;
                PropertyData<?> propIsLatestMajorVersion = null;
                for (PropertyData<?> property : properties)
                {
                    if (property.getId().equals(PropertyIds.IS_LATEST_MAJOR_VERSION))
                    {
                        found = true;
                        propIsLatestMajorVersion = property;
                        break;
                    }
                }
                //properties..contains(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION);
                assertTrue("The CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION property was not found", found);
                if (found)
                {
                    assertFalse("The CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION should be false as minor version was created", (Boolean) propIsLatestMajorVersion.getValues().get(0));
                }
                return objectId;
            }
        });
    }
    
    /**
     * ACE-33
     * 
     * Cmis Item support
     */
    @Test
    public void testItems()
    {

        withCmisService(new CmisServiceCallback<String>() {
            @Override
            public String execute(CmisService cmisService) {
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                String repositoryId = repo.getId();
                
            	TypeDefinition def = cmisService.getTypeDefinition(repositoryId, "cmis:item", null);
            	assertNotNull("the cmis:item type is not defined", def); 
                
            	@SuppressWarnings("unused")
                TypeDefinition p = cmisService.getTypeDefinition(repositoryId, "I:cm:person", null);
            	assertNotNull("the I:cm:person type is not defined", def); 
            	
            	ObjectList result = cmisService.query(repositoryId, "select * from cm:person", Boolean.FALSE, Boolean.TRUE, IncludeRelationships.NONE, "", BigInteger.TEN, BigInteger.ZERO, null);
            	assertTrue("", result.getNumItems().intValue() > 0);
            	return "";
        
            };
        }, CmisVersion.CMIS_1_1);
    	
    }
    
    /**
     * MNT-11339 related test :
     * Unable to create relationship between cmis:document and cmis:item
     */
    @Test
    public void testItemRelations() 
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        final String TEST_NAME = "testItemRelations-";
        final String FOLDER_NAME = TEST_NAME + "FOLDER" + GUID.generate();
        final String DOCUMENT_NAME = TEST_NAME + "DOCUMENT" + GUID.generate();
        final String CLIENT_NAME = "Some Test Client " + GUID.generate();
        
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<Void>()
                    {
                        @Override
                        public Void execute() throws Throwable
                        {
                            NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
                            
                            /* Create folder within companyHome */
                            FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, FOLDER_NAME, ContentModel.TYPE_FOLDER);
                            nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, FOLDER_NAME);
                            assertNotNull(folderInfo);
                            
                            // and document
                            FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), DOCUMENT_NAME, ContentModel.TYPE_CONTENT);
                            assertNotNull(document);
                            nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, DOCUMENT_NAME);
                            
                            return null;
                        }
                    });
            
            withCmisService(new CmisServiceCallback<String>() {
                @SuppressWarnings("unchecked")
                @Override
                public String execute(CmisService cmisService) {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
                    
                    // ensure there are custom type, aspect and association defined
                    TypeDefinition tpdfn = cmisService.getTypeDefinition(repositoryId, "I:sctst:client", null);
                    assertNotNull("the I:sctst:client type is not defined", tpdfn);
                    TypeDefinition aspectDfn = cmisService.getTypeDefinition(repositoryId, "P:sctst:clientRelated", null);
                    assertNotNull("the P:sctst:clientRelated aspect is not defined", aspectDfn);
                    TypeDefinition relDfn = cmisService.getTypeDefinition(repositoryId, "R:sctst:relatedClients", null);
                    assertNotNull("the R:sctst:relatedClients association is not defined", relDfn);
                    
                    // create cmis:item within test folder
                    PropertiesImpl properties = new PropertiesImpl();
                    properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, tpdfn.getId()));
                    properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, CLIENT_NAME));
                    properties.addProperty(new PropertyStringImpl("sctst:clientId", "id" + GUID.generate()));
                    properties.addProperty(new PropertyStringImpl("sctst:clientName", CLIENT_NAME));
                    
                    ObjectData folderData = cmisService.getObjectByPath(repositoryId, "/" + FOLDER_NAME, null, null, null, null, null, null, null);
                    
                    cmisService.createItem(repositoryId, properties, folderData.getId(), null, null, null, null);
                    
                    ObjectData contentData = cmisService.getObjectByPath(repositoryId, "/" + FOLDER_NAME + "/" + DOCUMENT_NAME, null, null, null, null, null, null, null);
                    
                    // add test aspect sctst:clientRelated to document
                    Properties props = cmisService.getProperties(repositoryId, contentData.getId(), null, null);
                    
                    PropertyData<?> propAspects = props.getProperties().get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
                    
                    @SuppressWarnings("rawtypes")
                    List aspects = propAspects.getValues();
                    aspects.add("P:sctst:clientRelated");
                    
                    properties = new PropertiesImpl();
                    properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, aspects));
                    cmisService.updateProperties(repositoryId, new Holder<String>(contentData.getId()), null, properties, null);
                    // ensure document has sctst:clientRelated aspect applied
                    aspects = cmisService.getProperties(repositoryId, contentData.getId(), null, null).getProperties().get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues();
                    assertTrue("P:sctst:clientRelated excpected", aspects.contains("P:sctst:clientRelated"));
                    
                    ObjectData itemData = cmisService.getObjectByPath(repositoryId, "/" + FOLDER_NAME + "/" + CLIENT_NAME, null, null, null, null, null, null, null);
                    // create relationship between cmis:document and cmis:item 
                    properties = new PropertiesImpl();
                    properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "R:sctst:relatedClients"));
                    properties.addProperty(new PropertyIdImpl(PropertyIds.SOURCE_ID, contentData.getId()));
                    properties.addProperty(new PropertyIdImpl(PropertyIds.TARGET_ID, itemData.getId()));
                    cmisService.createRelationship(repositoryId, properties, null, null, null, null);
                    
                    return "";
            
                };
            }, CmisVersion.CMIS_1_1);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    @Test
    public void testMNT10529() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        try
        {
            final Pair<FileInfo, FileInfo> folderAndDocument = transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<Pair<FileInfo, FileInfo>>()
                    {
                        @Override
                        public Pair<FileInfo, FileInfo> execute() throws Throwable
                        {
                            NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                            String folderName = GUID.generate();
                            FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                            nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                            assertNotNull(folderInfo);

                            String docName = GUID.generate();
                            FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                            assertNotNull(document);
                            nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, docName);

                            return new Pair<FileInfo, FileInfo>(folderInfo, document);
                        }
                    });

            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertNotNull(repositories);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.iterator().next();
                    String repositoryId = repo.getId();

                    String objectIdStr = folderAndDocument.getFirst().getNodeRef().toString();

                    ObjectInFolderList children = cmisService.getChildren(repositoryId, objectIdStr, null, "cmis:name ASC", false, IncludeRelationships.NONE, null, false, null,
                            null, null);
                    assertChildren(folderAndDocument, children);

                    children = cmisService.getChildren(repositoryId, objectIdStr, null, "cmis:name ASC, cmis:creationDate ASC", false, IncludeRelationships.NONE, null, false,
                            null, null, null);
                    assertChildren(folderAndDocument, children);

                    children = cmisService.getChildren(repositoryId, objectIdStr, null, "    cmis:name ASC", false, IncludeRelationships.NONE, null, false, null, null, null);
                    assertChildren(folderAndDocument, children);

                    children = cmisService.getChildren(repositoryId, objectIdStr, null, "    cmis:name ASC, cmis:creationDate ASC   ", false, IncludeRelationships.NONE, null,
                            false, null, null, null);
                    assertChildren(folderAndDocument, children);

                    return null;
                }

                private void assertChildren(final Pair<FileInfo, FileInfo> folderAndDocument, ObjectInFolderList children)
                {
                    assertNotNull(children);
                    assertTrue(1 == children.getNumItems().longValue());

                    PropertyData<?> nameData = children.getObjects().iterator().next().getObject().getProperties().getProperties().get("cmis:name");
                    assertNotNull(nameData);
                    Object name = nameData.getValues().iterator().next();
                    assertEquals(folderAndDocument.getSecond().getName(), name);
                }
            }, CmisVersion.CMIS_1_0);
        }
        catch (CmisConstraintException e)
        {
            fail(e.toString());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    @Category(RedundantTests.class)
    @Test
    public void mnt10548test() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        final Pair<FileInfo, FileInfo> folderAndDocument = transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Pair<FileInfo, FileInfo>>()
                {
                    private final static String TEST_NAME = "mnt10548test-";
                    
                    @Override
                    public Pair<FileInfo, FileInfo> execute() throws Throwable
                    {
                        NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
                        
                        /* Create folder within companyHome */
                        String folderName = TEST_NAME + GUID.generate();
                        FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                        nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                        assertNotNull(folderInfo);
                        
                        /* Create content */
                        String docName = TEST_NAME + GUID.generate();
                        FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                        assertNotNull(document);
                        nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, docName);
                        
                        /* Add some tags */
                        NodeRef nodeRef = document.getNodeRef();
                        taggingService.addTag(nodeRef, "tag1");
                        taggingService.addTag(nodeRef, "tag2");
                        taggingService.addTag(nodeRef, "tag3");

                        return new Pair<FileInfo, FileInfo>(folderInfo, document);
                    }
                });
        
        ObjectData objData = withCmisService(
                new CmisServiceCallback<ObjectData>() 
                {
                    private static final String FILE_FOLDER_SEPARATOR = "/";
                    
                    @Override
                    public ObjectData execute(CmisService cmisService) 
                    {
                        List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                        assertTrue(repositories.size() > 0);
                        RepositoryInfo repo = repositories.get(0);
                        String repositoryId = repo.getId();
                        
                        String path = FILE_FOLDER_SEPARATOR + folderAndDocument.getFirst().getName() + FILE_FOLDER_SEPARATOR + folderAndDocument.getSecond().getName();
                        /* get CMIS object of document */
                        ObjectData objData = cmisService.getObjectByPath(repositoryId, path, null, false, null, null, false, false, null);
                        return objData;
                    }
                }, CmisVersion.CMIS_1_1);
        
        Map<String, PropertyData<?>> cmisProps = objData.getProperties().getProperties();
        
        String taggable = ContentModel.ASPECT_TAGGABLE.getPrefixedQName(namespaceService).toPrefixString();
        PropertyData<?> propData = cmisProps.get(taggable);
        assertNotNull(propData);
        
        List<?> props = propData.getValues();
        assertTrue(props.size() == 3);
        
        /* MNT-10548 fix : CMIS should return List of String, not List of NodeRef */
        for(Object o : props)
        {
            assertTrue(o.getClass() + " found but String expected", o instanceof String);
        }
    }

    /**
     * CMIS 1.0 aspect properties should provide the following CMIS attributes:
     * propertyDefinitionId, displayName, localName, queryName
     */
    @Test
    public void testMNT10021() throws Exception
    {
        final String folderName = "testfolder." + GUID.generate();
        final String docName = "testdoc.txt." + GUID.generate();
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                    FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                    nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                    assertNotNull(folderInfo);

                    FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                    assertNotNull(document);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, docName);

                    // lock adds aspects to the document with properties: lockIsDeep, lockOwner, lockType, expiryDate, lockLifetime
                    lockService.lock(document.getNodeRef(), LockType.READ_ONLY_LOCK, 0, true);

                    return null;
                }
            });

            final ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
            {
                @Override
                public ObjectData execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
                    ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true,
                            IncludeRelationships.NONE, null, false, true, null);
                    return objectData;
                }
            }, CmisVersion.CMIS_1_0);

            List<CmisExtensionElement> propertyExtensionList = objectData.getProperties().getExtensions();
            assertEquals("propertyExtensionList should be singletonList", propertyExtensionList.size(), 1);
            List<CmisExtensionElement> extensions = propertyExtensionList.iterator().next().getChildren();
            for (CmisExtensionElement extension : extensions)
            {
                if ("properties".equals(extension.getName()))
                {
                    // check properties extension
                    List<CmisExtensionElement> propExtensions = extension.getChildren();
                    assertTrue("cmisObject should contain aspect properties", propExtensions.size() > 0);
                    for (CmisExtensionElement prop : propExtensions)
                    {
                        Map<String, String> cmisAspectProperty = prop.getAttributes();
                        Set<String> cmisAspectPropertyNames = cmisAspectProperty.keySet();
                        assertTrue("propertyDefinitionId attribute should be present", cmisAspectPropertyNames.contains("propertyDefinitionId"));
                        assertTrue("queryName attribute should be present", cmisAspectPropertyNames.contains("queryName"));
                        // optional values that are present for test document
                        assertTrue("displayName attribute should be present for property of test node", cmisAspectPropertyNames.contains("displayName"));
                        assertTrue("localName attribute should be present for property of test node", cmisAspectPropertyNames.contains("localName"));
                        assertEquals(cmisAspectPropertyNames.size(), 4);
                        // check values
                        for (String aspectPropertyName : cmisAspectPropertyNames)
                        {
                            String value = cmisAspectProperty.get(aspectPropertyName);
                            assertTrue("value for " + aspectPropertyName + " should be present", value != null && value.length() > 0);
                        }
                    }
                }
            }
        }
        catch (CmisConstraintException e)
        {
            fail(e.toString());
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
	@Test
	public void dictionaryTest()
	{
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                M2Model customModel = M2Model.createModel(
                        Thread.currentThread().getContextClassLoader().
                        getResourceAsStream("dictionary/dictionarydaotest_model1.xml"));
                dictionaryDAO.putModel(customModel);

                assertNotNull(cmisDictionaryService.findType("P:cm:dublincore"));
                TypeDefinitionWrapper td = cmisDictionaryService.findType("D:daotest1:type1");
                assertNotNull(td);
                return null;
            }
        }, "user1", "tenant1");

        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                assertNotNull(cmisDictionaryService.findType("P:cm:dublincore"));
                TypeDefinitionWrapper td = cmisDictionaryService.findType("D:daotest1:type1");
                assertNull(td);
                return null;
            }
        }, "user2", "tenant2");
    }

    /**
     * MNT-13529: Just-installed Alfresco does not return a CMIS latestChangeLogToken
     * 
     * @throws Exception
     */
    @Test
    public void testMNT13529() throws Exception
    {
        setupAudit();

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        try
        {
            // Delete the entries, it simulates just installed Alfresco for reproduce the issue
            final Long appId = auditSubsystem.getAuditApplicationByName("CMISChangeLog").getApplicationId();
            RetryingTransactionCallback<Void> deletedCallback = new RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    auditDAO.deleteAuditEntries(appId, null, null);
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(deletedCallback);

            // Retrieve initial latestChangeLogToken
            final String initialChangeLogToken = withCmisService(new CmisServiceCallback<String>()
            {
                @Override
                public String execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertNotNull(repositories);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.iterator().next();

                    return repo.getLatestChangeLogToken();
                }
            }, CmisVersion.CMIS_1_1);

            assertNotNull(initialChangeLogToken);
            assertEquals("0", initialChangeLogToken);
        }
        finally
        {
            auditSubsystem.destroy();
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * MNT-11726: Test that {@link CMISChangeEvent} contains objectId of node in short form (without StoreRef).
     */
    @Test
    public void testCMISChangeLogObjectIds() throws Exception
    {
        // setUp audit subsystem
        setupAudit();
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        try
        {
            final String changeToken = withCmisService(new CmisServiceCallback<String>()
            {
                @Override
                public String execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertNotNull(repositories);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.iterator().next();

                    return repo.getLatestChangeLogToken();
                }
            }, CmisVersion.CMIS_1_1);

            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                    // perform CREATED, UPDATED, SECURITY, DELETED CMIS change type actions
                    String folder = GUID.generate();
                    FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folder, ContentModel.TYPE_FOLDER);
                    nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folder);
                    assertNotNull(folderInfo);

                    String content = GUID.generate();
                    FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), content, ContentModel.TYPE_CONTENT);
                    assertNotNull(document);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, content);

                    permissionService.setPermission(document.getNodeRef(), "SomeAuthority", PermissionService.EXECUTE_CONTENT, true);

                    fileFolderService.delete(document.getNodeRef());
                    fileFolderService.delete(folderInfo.getNodeRef());

                    return null;
                }
            });
            
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertNotNull(repositories);
                    assertTrue(repositories.size() > 0);
                    String repositoryId = repositories.iterator().next().getId();

                    ObjectList changes = cmisService.getContentChanges(repositoryId, new Holder<String>(changeToken), Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, BigInteger.valueOf(1000), null);

                    for (ObjectData od : changes.getObjects())
                    {
                        ChangeType changeType = od.getChangeEventInfo().getChangeType();
                        Object objectId = od.getProperties().getProperties().get("cmis:objectId").getValues().get(0);
                        
                        assertFalse("CMISChangeEvent " + changeType + " should store short form of objectId " + objectId, 
                                objectId.toString().contains(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.toString()));
                    }
                    int expectAtLeast = changes.getObjects().size();

                    // We should also be able to query without passing in any limit
                    changes = cmisService.getContentChanges(repositoryId, new Holder<String>(changeToken), Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, null, null);
                    assertTrue("Expected to still get changes", changes.getObjects().size() >= expectAtLeast);
                    // and zero
                    changes = cmisService.getContentChanges(repositoryId, new Holder<String>(changeToken), Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, BigInteger.valueOf(0), null);
                    assertTrue("Expected to still get changes", changes.getObjects().size() >= expectAtLeast);
                    // and one
                    changes = cmisService.getContentChanges(repositoryId, new Holder<String>(changeToken), Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, BigInteger.valueOf(1), null);
                    assertEquals("Expected to still get changes", changes.getObjects().size(), 1);
                    // Integery.MAX_VALUE must be handled
                    //      This will limit the number to a sane value
                    changes = cmisService.getContentChanges(repositoryId, new Holder<String>(changeToken), Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, BigInteger.valueOf(Integer.MAX_VALUE), null);
                    assertTrue("Expected to still get changes", changes.getObjects().size() >= expectAtLeast);
                    // but not negative
                    try
                    {
                        changes = cmisService.getContentChanges(repositoryId, new Holder<String>(changeToken), Boolean.TRUE, null, Boolean.FALSE, Boolean.FALSE, BigInteger.valueOf(-1), null);
                        fail("Negative maxItems is expected to fail");
                    }
                    catch (CmisInvalidArgumentException e)
                    {
                        // Expected
                    }

                    return null;
                }
            }, CmisVersion.CMIS_1_1);
        }
        finally
        {
            auditSubsystem.destroy();
            AuthenticationUtil.popAuthentication();
        }
    }
    
    private void setupAudit()
    {
        UserAuditFilter userAuditFilter = new UserAuditFilter();
        userAuditFilter.setUserFilterPattern("System;.*");
        userAuditFilter.afterPropertiesSet();
        AuditComponent auditComponent = (AuditComponent) ctx.getBean("auditComponent");
        auditComponent.setUserAuditFilter(userAuditFilter);
        AuditServiceImpl auditServiceImpl = (AuditServiceImpl) ctx.getBean("auditService");
        auditServiceImpl.setAuditComponent(auditComponent);
        
        RetryingTransactionCallback<Void> initAudit = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                auditSubsystem.stop();
                auditSubsystem.setProperty("audit.enabled", "true");
                auditSubsystem.setProperty("audit.cmischangelog.enabled", "true");
                auditSubsystem.start();
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(initAudit, false, true); 
    }

    /**
     * MNT-11727: move and rename operations should be shown as an UPDATE in the CMIS change log
     */
    @Test
    public void testMoveRenameWithCMISshouldBeAuditedAsUPDATE() throws Exception
    {
        // setUp audit subsystem
        setupAudit();

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        try
        {
            assertTrue("Audit is not enabled", auditSubsystem.isAuditEnabled());
            assertNotNull("CMIS audit is not enabled", auditSubsystem.getAuditApplicationByName("CMISChangeLog"));

            NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

            String folder = GUID.generate();
            FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folder, ContentModel.TYPE_FOLDER);

            final String actualToken = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>()
            {
                public String execute() throws Exception
                {
                    return cmisConnector.getRepositoryInfo(CmisVersion.CMIS_1_1).getLatestChangeLogToken();
                }
            }, true, false);

            String content = GUID.generate();
            FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), content, ContentModel.TYPE_CONTENT);
            assertNotNull(document);
            nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, content);

            Holder<String> changeLogToken = new Holder<String>();
            changeLogToken.setValue(actualToken);
            ObjectList changeLog = CMISTest.this.cmisConnector.getContentChanges(changeLogToken, new BigInteger("10"));
            List<ObjectData> events = changeLog.getObjects();
            int count = events.size();
            // it should be 3 entries: 1 for previous folder create, 1 new CREATE (for document create)
            // and 1 NEW UPDATE
            assertEquals(3, count);

            assertEquals(events.get(0).getProperties().getPropertyList().get(0).getValues().get(0), folderInfo.getNodeRef().getId());
            assertEquals(events.get(0).getChangeEventInfo().getChangeType(), ChangeType.CREATED);

            assertTrue(((String) events.get(1).getProperties().getPropertyList().get(0).getValues().get(0)).contains(document.getNodeRef().getId()));
            assertEquals(events.get(1).getChangeEventInfo().getChangeType(), ChangeType.CREATED);

            assertTrue(((String) events.get(2).getProperties().getPropertyList().get(0).getValues().get(0)).contains(document.getNodeRef().getId()));
            assertEquals(events.get(2).getChangeEventInfo().getChangeType(), ChangeType.UPDATED);

            // test rename
            final String actualToken2 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>()
            {
                public String execute() throws Exception
                {
                    return cmisConnector.getRepositoryInfo(CmisVersion.CMIS_1_1).getLatestChangeLogToken();
                }
            }, true, false);
            nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, content + "-updated");

            changeLogToken = new Holder<String>();
            changeLogToken.setValue(actualToken2);
            changeLog = CMISTest.this.cmisConnector.getContentChanges(changeLogToken, new BigInteger("10"));
            events = changeLog.getObjects();
            count = events.size();
            assertEquals(2, count);
            assertEquals("Rename operation should be shown as an UPDATE in the CMIS change log", events.get(1).getChangeEventInfo().getChangeType(), ChangeType.UPDATED);

            // test move
            String targetFolder = GUID.generate();
            FileInfo targetFolderInfo = fileFolderService.create(companyHomeNodeRef, targetFolder, ContentModel.TYPE_FOLDER);

            final String actualToken3 = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>()
            {
                public String execute() throws Exception
                {
                    return cmisConnector.getRepositoryInfo(CmisVersion.CMIS_1_1).getLatestChangeLogToken();
                }
            }, true, false);
            nodeService.moveNode(document.getNodeRef(), targetFolderInfo.getNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);

            changeLogToken = new Holder<String>();
            changeLogToken.setValue(actualToken3);
            changeLog = CMISTest.this.cmisConnector.getContentChanges(changeLogToken, new BigInteger("10"));
            events = changeLog.getObjects();
            count = events.size();
            assertEquals(2, count);
            assertEquals("Move operation should be shown as an UPDATE in the CMIS change log", events.get(1).getChangeEventInfo().getChangeType(), ChangeType.UPDATED);
        }
        finally
        {
            auditSubsystem.destroy();
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * MNT-11304: Test that Alfresco has no default boundaries for decimals
     * @throws Exception
     */
    @Test
    public void testDecimalDefaultBoundaries() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        try
        {
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
                    
                    TypeDefinition decimalTypeDef = cmisService.getTypeDefinition(repositoryId, "D:tcdm:testdecimalstype", null);
                    
                    PropertyDecimalDefinitionImpl floatNoBoundsTypeDef = 
                            (PropertyDecimalDefinitionImpl)decimalTypeDef.getPropertyDefinitions().get("tcdm:float");
                    PropertyDecimalDefinitionImpl doubleNoBoundsTypeDef = 
                            (PropertyDecimalDefinitionImpl)decimalTypeDef.getPropertyDefinitions().get("tcdm:double");
                    
                    PropertyDecimalDefinitionImpl floatWithBoundsTypeDef = 
                            (PropertyDecimalDefinitionImpl)decimalTypeDef.getPropertyDefinitions().get("tcdm:floatwithbounds");
                    PropertyDecimalDefinitionImpl doubleWithBoundsTypeDef = 
                            (PropertyDecimalDefinitionImpl)decimalTypeDef.getPropertyDefinitions().get("tcdm:doublewithbounds");
                    
                    // test that there is not default boundaries for decimals
                    assertNull(floatNoBoundsTypeDef.getMinValue());
                    assertNull(floatNoBoundsTypeDef.getMaxValue());
                    
                    assertNull(doubleNoBoundsTypeDef.getMinValue());
                    assertNull(doubleNoBoundsTypeDef.getMaxValue());
                    
                    // test for pre-defined boundaries
                    assertTrue(floatWithBoundsTypeDef.getMinValue().equals(BigDecimal.valueOf(-10f)));
                    assertTrue(floatWithBoundsTypeDef.getMaxValue().equals(BigDecimal.valueOf(10f)));
                    
                    assertTrue(doubleWithBoundsTypeDef.getMinValue().equals(BigDecimal.valueOf(-10d)));
                    assertTrue(doubleWithBoundsTypeDef.getMaxValue().equals(BigDecimal.valueOf(10d)));

                    return null;
                }
            }, CmisVersion.CMIS_1_1);
            
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * MNT-11876 : Test that Alfresco CMIS 1.1 Implementation is returning aspect and aspect properties as extension data
     * @throws Exception
     */
    @Test
    public void testExtensionDataIsReturnedViaCmis1_1() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        final String FOLDER = "testExtensionDataIsReturnedViaCmis1_1-" + GUID.generate();
        final String CONTENT   = FOLDER + "-file";
        
        try
        {
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // create folder
                    FileInfo folderInfo = fileFolderService.create(repositoryHelper.getCompanyHome(), FOLDER, ContentModel.TYPE_FOLDER);
                    nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, FOLDER);
                    assertNotNull(folderInfo);

                    // create document
                    FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), CONTENT, ContentModel.TYPE_CONTENT);
                    assertNotNull(document);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, CONTENT);

                    // apply aspect with properties
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_LATITUDE, Double.valueOf(1.0d));
                    props.put(ContentModel.PROP_LONGITUDE, Double.valueOf(1.0d));
                    nodeService.addAspect(document.getNodeRef(), ContentModel.ASPECT_GEOGRAPHIC, props);

                    return null;
                }
            });
            
            final ObjectData objectData = withCmisService(new CmisServiceCallback<ObjectData>()
            {
                @Override
                public ObjectData execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();
                    // get object data 
                    ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + FOLDER + "/" + CONTENT, null, true,
                            IncludeRelationships.NONE, null, false, true, null);
                    return objectData;
                }
            }, CmisVersion.CMIS_1_1);

            // get extension data from object properties
            List<CmisExtensionElement> extensions = objectData.getProperties().getExtensions().iterator().next().getChildren();
            
            Set<String> appliedAspects   = new HashSet<String>();
            Set<String> aspectProperties = new HashSet<String>();
            
            for (CmisExtensionElement extension : extensions)
            {
                if (CMISConnector.PROPERTIES.equals(extension.getName()))
                {
                    // check properties extension
                    List<CmisExtensionElement> propExtensions = extension.getChildren();
                    assertTrue("cmisObject should contain aspect properties", propExtensions.size() > 0);
                    for (CmisExtensionElement prop : propExtensions)
                    {
                        Map<String, String> cmisAspectProperty = prop.getAttributes();
                        Set<String> cmisAspectPropertyNames = cmisAspectProperty.keySet();
                        assertTrue("propertyDefinitionId attribute should be present", cmisAspectPropertyNames.contains("propertyDefinitionId"));
                        aspectProperties.add(cmisAspectProperty.get("propertyDefinitionId"));
                    }
                }
                else if (CMISConnector.APPLIED_ASPECTS.equals(extension.getName()))
                {
                    appliedAspects.add(extension.getValue());
                }
            }
            
            // extension data should contain applied aspects and aspect properties
            assertTrue("Extensions should contain " + ContentModel.ASPECT_GEOGRAPHIC, appliedAspects.contains("P:cm:geographic"));
            assertTrue("Extensions should contain " + ContentModel.PROP_LATITUDE, aspectProperties.contains("cm:latitude"));
            assertTrue("Extensions should contain " + ContentModel.PROP_LONGITUDE, aspectProperties.contains("cm:longitude"));
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * MNT-10165: Check that all concomitant basic CMIS permissions are deleted
     * when permission is deleted vai CMIS 1.1 API. For Atom binding it applies
     * new set of permissions instead of deleting the old ones.
     */
    @Test
    public void testRemoveACL() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        final String groupName = "group" + GUID.generate();
        final String testGroup = PermissionService.GROUP_PREFIX + groupName;
        try
        {
            // preconditions: create test document
            if (!authorityService.authorityExists(testGroup))
            {
                authorityService.createAuthority(AuthorityType.GROUP, groupName);
            }
            
            final FileInfo document = transactionService.getRetryingTransactionHelper().doInTransaction(
                    new RetryingTransactionCallback<FileInfo>()
                    {
                        @Override
                        public FileInfo execute() throws Throwable
                        {
                            NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                            String folderName = GUID.generate();
                            FileInfo folderInfo = fileFolderService.create(companyHomeNodeRef, folderName, ContentModel.TYPE_FOLDER);
                            nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, folderName);
                            assertNotNull(folderInfo);

                            String docName = GUID.generate();
                            FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), docName, ContentModel.TYPE_CONTENT);
                            assertNotNull(document);
                            nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, docName);

                            return document;
                        }
                    });

            Set<AccessPermission> permissions = permissionService.getAllSetPermissions(document.getNodeRef());
            assertEquals(permissions.size(), 1);
            AccessPermission current = permissions.iterator().next();
            assertEquals(current.getAuthority(), "GROUP_EVERYONE");
            assertEquals(current.getPermission(), "Consumer");

            // add group1 with Coordinator permissions
            permissionService.setPermission(document.getNodeRef(), testGroup, PermissionService.COORDINATOR, true);
            permissions = permissionService.getAllSetPermissions(document.getNodeRef());

            Map<String , String> docPermissions = new HashMap<String, String>();
            for (AccessPermission permission : permissions)
            {
                docPermissions.put(permission.getAuthority(), permission.getPermission());
            }
            assertTrue(docPermissions.keySet().contains(testGroup));
            assertEquals(docPermissions.get(testGroup), PermissionService.COORDINATOR);

            // update permissions for group1 via CMIS 1.1 API 
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertNotNull(repositories);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.iterator().next();
                    String repositoryId = repo.getId();
                    String docIdStr = document.getNodeRef().toString();

                    // when removing Coordinator ACE there are only inherited permissions
                    // so empty list of direct permissions is sent to be set
                    AccessControlListImpl acesToPut = new AccessControlListImpl();
                    List<Ace> acesList = Collections.emptyList();
                    acesToPut.setAces(acesList);
                    cmisService.applyAcl(repositoryId, docIdStr, acesToPut, AclPropagation.REPOSITORYDETERMINED);

                    return null;
                }
            }, CmisVersion.CMIS_1_1);

            // check that permissions are the same as they were before Coordinator was added
            permissions = permissionService.getAllSetPermissions(document.getNodeRef());
            docPermissions = new HashMap<String, String>();
            for (AccessPermission permission : permissions)
            {
                docPermissions.put(permission.getAuthority(), permission.getPermission());
            }
            assertFalse(docPermissions.keySet().contains(testGroup));
            assertEquals(permissions.size(), 1);
            current = permissions.iterator().next();
            assertEquals(current.getAuthority(), "GROUP_EVERYONE");
            assertEquals(current.getPermission(), "Consumer");
        }
        catch (CmisConstraintException e)
        {
            fail(e.toString());
        }
        finally
        {
            if (authorityService.authorityExists(testGroup))
            {
                authorityService.deleteAuthority(testGroup);
            }
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * ACE-2904
     */
    @Test
    public void testACE2904()
    {                                   // Basic CMIS Types                                                               // Additional types from Content Model
        final String[] types =        { "cmis:document", "cmis:relationship", "cmis:folder", "cmis:policy", "cmis:item", "R:cm:replaces", "P:cm:author", "I:cm:cmobject" };
        final String[] displayNames = { "Document",      "Relationship",      "Folder",      "Policy",      "Item Type", "Replaces",      "Author",      "Object" };
        final String[] descriptions = { "Document Type", "Relationship Type", "Folder Type", "Policy Type", "CMIS Item", "Replaces",      "Author",      "I:cm:cmobject" };

        CmisServiceCallback<String> callback = new CmisServiceCallback<String>()
        {
            @Override
            public String execute(CmisService cmisService)
            {
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                assertTrue(repositories.size() > 0);
                RepositoryInfo repo = repositories.get(0);
                String repositoryId = repo.getId();

                for (int i = 0; i < types.length; i++)
                {
                    TypeDefinition def = cmisService.getTypeDefinition(repositoryId, types[i], null);
                    assertNotNull("The " + types[i] + " type is not defined", def);
                    assertNotNull("The display name is incorrect. Please, refer to ACE-2904.", def.getDisplayName());
                    assertEquals("The display name is incorrect. Please, refer to ACE-2904.", def.getDisplayName(), displayNames[i]);
                    assertEquals("The description is incorrect. Please, refer to ACE-2904.", def.getDescription(), descriptions[i]);
                    
                    for (PropertyDefinition<?> property : def.getPropertyDefinitions().values())
                    {
                        assertNotNull("Property definition dispaly name is incorrect. Please, refer to ACE-2904.", property.getDisplayName());
                        assertNotNull("Property definition description is incorrect. Please, refer to ACE-2904.", property.getDescription());
                    }
                }

                return "";
            };
        };

        // Lets test types for cmis 1.1 and cmis 1.0
        withCmisService(callback, CmisVersion.CMIS_1_1);
        withCmisService(callback, CmisVersion.CMIS_1_0);
    }
    
    /**
     * ACE-3322
     */
    @Test
    public void testACE3322()
    {
        final String[] types = { "cmis:document", "cmis:relationship", "cmis:folder", "cmis:item" };
        
        CmisServiceCallback<String> callback = new CmisServiceCallback<String>()
        {
            @Override
            public String execute(CmisService cmisService)
            {
                for (int i = 0; i < types.length; i++)
                {
                    
                    List<TypeDefinitionWrapper> baseTypes = cmisDictionaryService.getBaseTypes();
                    assertNotNull(baseTypes);
                    checkDefs(baseTypes);
                    
                    List<TypeDefinitionWrapper> children = cmisDictionaryService.getChildren(types[i]);
                    assertNotNull(children);

                    // Check that children were updated
                    checkDefs(children);
                }
                return "";
            };
            
            private void checkDefs(List<TypeDefinitionWrapper> defs)
            {
                for (TypeDefinitionWrapper def : defs)
                {
                    assertNotNull("Type definition was not updated. Please refer to ACE-3322", def.getTypeDefinition(false).getDisplayName());
                    assertNotNull("Type definition was not updated. Please refer to ACE-3322", def.getTypeDefinition(false).getDescription());

                    // Check that property's display name and description were updated
                    for (PropertyDefinitionWrapper property : def.getProperties())
                    {
                        assertNotNull("Display name is null", property.getPropertyDefinition().getDisplayName());
                        assertNotNull("Description is null", property.getPropertyDefinition().getDescription());
                    }
                }
            }
        };

        withCmisService(callback, CmisVersion.CMIS_1_1);
    }

    /**
     * Test auto version behavior for setContentStream, deleteContentStream and appendContentStream according to ALF-21852.
     */
    @Test
    public void testSetDeleteAppendContentStreamVersioning() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        final String DOC1 = "documentProperties1-" + GUID.generate();

        try
        {
            final FileInfo doc = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<FileInfo>()
            {
                @Override
                public FileInfo execute() throws Throwable
                {
                    FileInfo document;
                    // create document
                    document = fileFolderService.create(repositoryHelper.getCompanyHome(), DOC1, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, DOC1);

                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_TITLE, "Initial Title");
                    props.put(ContentModel.PROP_DESCRIPTION, "Initial Description");

                    nodeService.addAspect(document.getNodeRef(), ContentModel.ASPECT_TITLED, props);

                    // apply versionable aspect with properties
                    props = new HashMap<QName, Serializable>();
                    // ContentModel.PROP_INITIAL_VERSION always true
                    props.put(ContentModel.PROP_INITIAL_VERSION, true);
                    props.put(ContentModel.PROP_AUTO_VERSION, true);
                    props.put(ContentModel.PROP_AUTO_VERSION_PROPS, true);
                    versionService.ensureVersioningEnabled(document.getNodeRef(), props);

                    return document;
                }
            });
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override public Void execute(CmisService cmisService)
                {
                    final String documentNodeRefId = doc.getNodeRef().toString();
                    final String repositoryId = cmisService.getRepositoryInfos(null).get(0).getId();

                    ObjectInfo objInfoInitialVersion = cmisService.getObjectInfo(repositoryId, documentNodeRefId);
                    assertTrue("We had just created the document - it should be version 1.0", objInfoInitialVersion.getId().endsWith("1.0"));

                    ContentStreamImpl contentStream = new ContentStreamImpl(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Content " + GUID.generate());
                    Holder<String> objectIdHolder = new Holder<>(documentNodeRefId);
                    // Test setContentStream
                    cmisService.setContentStream(repositoryId, objectIdHolder, true, null, contentStream, null);
                    assertTrue("The \"output\" parameter should returns the newly created version id: 1.1", objectIdHolder.getValue().endsWith("1.1"));
                    // we can use this new version id to get information about the cmis object
                    ObjectInfo objInfoAfterSetContentStream = cmisService.getObjectInfo(repositoryId, objectIdHolder.getValue());
                    assertTrue("The object info should reflect the version requested: 1.1", objInfoAfterSetContentStream.getId().endsWith("1.1"));

                    // Test deleteContentStream
                    cmisService.deleteContentStream(repositoryId, objectIdHolder, null, null);
                    assertTrue("The \"output\" parameter should returns the newly created version id: 1.2", objectIdHolder.getValue().endsWith("1.2"));
                    // we can use this new version id to get information about the cmis object
                    objInfoAfterSetContentStream = cmisService.getObjectInfo(repositoryId, objectIdHolder.getValue());
                    assertTrue("The object info should reflect the version requested: 1.2", objInfoAfterSetContentStream.getId().endsWith("1.2"));

                    // Test appendContentStream
                    cmisService.appendContentStream(repositoryId, objectIdHolder, null, contentStream, true, null);
                    assertTrue("The \"output\" parameter should returns the newly created version id: 1.3", objectIdHolder.getValue().endsWith("1.3"));
                    // we can use this new version id to get information about the cmis object
                    objInfoAfterSetContentStream = cmisService.getObjectInfo(repositoryId, objectIdHolder.getValue());
                    assertTrue("The object info should reflect the version requested: 1.3", objInfoAfterSetContentStream.getId().endsWith("1.3"));

                    return null;
                }
            }, CmisVersion.CMIS_1_1);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * Test to ensure auto version behavior for update properties, set and delete content using both Alfresco and CMIS perspectives.
     * Testing different combinations of <b>cm:initialVersion</b>, <b>cm:autoVersion</b> and <b>cm:autoVersionOnUpdateProps</b> properties 
     * <br>
     * OnUpdateProperties MINOR version should be created if <b>cm:initialVersion</b> and <b>cm:autoVersionOnUpdateProps</b> are both TRUE
     * <br>
     * OnContentUpdate MINOR version should be created if <b>cm:initialVersion</b> and <b>cm:autoVersion</b> are both TRUE
     * 
     * @throws Exception
     */
    @Test
    public void testUpdatePropertiesSetDeleteContentVersioning() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        final String FOLDER = "testUpdatePropertiesSetDeleteContentVersioning-" + GUID.generate();
        final String DOC1 = "documentProperties1-" + GUID.generate();
        final String DOC2 = "documentProperties2-" + GUID.generate();
        final String DOC3 = "documentProperties3-" + GUID.generate();
        final String DOC4 = "documentProperties4-" + GUID.generate();
        
        try
        {
            final List<FileInfo> docs = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<FileInfo>>()
            {
                @Override
                public List<FileInfo> execute() throws Throwable
                {
                    // create folder
                    FileInfo folderInfo = fileFolderService.create(repositoryHelper.getCompanyHome(), FOLDER, ContentModel.TYPE_FOLDER);
                    nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, FOLDER);
                    assertNotNull(folderInfo);

                    FileInfo document;
                    List<FileInfo> docs = new ArrayList<FileInfo>();
                    // create documents
                    document = fileFolderService.create(folderInfo.getNodeRef(), DOC1, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, DOC1);
                    docs.add(document);
                    document = fileFolderService.create(folderInfo.getNodeRef(), DOC2, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, DOC2);
                    docs.add(document);
                    document = fileFolderService.create(folderInfo.getNodeRef(), DOC3, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, DOC3);
                    docs.add(document);
                    document = fileFolderService.create(folderInfo.getNodeRef(), DOC4, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, DOC4);
                    docs.add(document);
                    
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_TITLE, "Initial Title");
                    props.put(ContentModel.PROP_DESCRIPTION, "Initial Description");
                    
                    for (FileInfo fileInfo : docs)
                    {
                        nodeService.addAspect(fileInfo.getNodeRef(), ContentModel.ASPECT_TITLED, props);
                    }
                    
                    // apply versionable aspect with properties
                    props = new HashMap<QName, Serializable>();
                    // ContentModel.PROP_INITIAL_VERSION always true
                    props.put(ContentModel.PROP_INITIAL_VERSION, true);
                    
                    props.put(ContentModel.PROP_AUTO_VERSION, false);
                    props.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
                    versionService.ensureVersioningEnabled(docs.get(0).getNodeRef(), props);
                    
                    props.put(ContentModel.PROP_AUTO_VERSION, false);
                    props.put(ContentModel.PROP_AUTO_VERSION_PROPS, true);
                    versionService.ensureVersioningEnabled(docs.get(1).getNodeRef(), props);
                    
                    props.put(ContentModel.PROP_AUTO_VERSION, true);
                    props.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
                    versionService.ensureVersioningEnabled(docs.get(2).getNodeRef(), props);
                    
                    props.put(ContentModel.PROP_AUTO_VERSION, true);
                    props.put(ContentModel.PROP_AUTO_VERSION_PROPS, true);
                    versionService.ensureVersioningEnabled(docs.get(3).getNodeRef(), props);

                    return docs;
                }
            });
            
            assertVersions(docs.get(0).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(1).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(2).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(3).getNodeRef(), "1.0", VersionType.MAJOR);
            
            // update node properties using Alfresco
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<Void>>()
            {
                @Override
                public List<Void> execute() throws Throwable
                {
                    for (FileInfo fileInfo : docs)
                    {
                        Map<QName, Serializable> props = nodeService.getProperties(fileInfo.getNodeRef());
                        
                        props.put(ContentModel.PROP_DESCRIPTION, "description-" + GUID.generate());
                        props.put(ContentModel.PROP_TITLE, "title-" + GUID.generate());
                        
                        nodeService.setProperties(fileInfo.getNodeRef(), props);
                    }
                    return null;
                }
            });
            
            assertVersions(docs.get(0).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(1).getNodeRef(), "1.1", VersionType.MINOR);
            assertVersions(docs.get(2).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(3).getNodeRef(), "1.1", VersionType.MINOR);
            
            // update properties using CMIS perspective 
            final String repositoryId = withCmisService(new CmisServiceCallback<String>()
            {
                @Override
                public String execute(CmisService cmisService)
                {
                    String repositoryId = cmisService.getRepositoryInfos(null).get(0).getId();
                    
                    for (FileInfo fileInfo : docs)
                    {
                        PropertiesImpl properties = new PropertiesImpl();
                        properties.addProperty(new PropertyStringImpl(PropertyIds.DESCRIPTION, "description-" + GUID.generate()));
                        
                        cmisService.updateProperties(repositoryId, new Holder<String>(fileInfo.getNodeRef().toString()), null, properties, null);
                    }
                    //This extra check was added due to MNT-16641.
                    {
                        PropertiesImpl properties = new PropertiesImpl();
                        properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, "P:cm:lockable"));

                        Set<QName> existingAspects = nodeService.getAspects(docs.get(0).getNodeRef());
                        cmisService.updateProperties(repositoryId,new Holder<String>(docs.get(0).getNodeRef().toString()), null, properties, null);
                        Set<QName> updatedAspects = nodeService.getAspects(docs.get(0).getNodeRef());
                        updatedAspects.removeAll(existingAspects);

                        assertEquals(ContentModel.ASPECT_LOCKABLE, updatedAspects.iterator().next());

                    }
                    return repositoryId;
                }
            }, CmisVersion.CMIS_1_1);
            
            assertVersions(docs.get(0).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(1).getNodeRef(), "1.2", VersionType.MINOR);
            assertVersions(docs.get(2).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(3).getNodeRef(), "1.2", VersionType.MINOR);
            
            // CMIS setContentStream
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    for (FileInfo fileInfo : docs)
                    {
                        ContentStreamImpl contentStream = new ContentStreamImpl(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Content " + GUID.generate());
                        
                        cmisService.setContentStream(repositoryId, new Holder<String>(fileInfo.getNodeRef().toString()), true, null, contentStream, null);
                    }
                    return null;
                }
            }, CmisVersion.CMIS_1_1);
            
            assertVersions(docs.get(0).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(1).getNodeRef(), "1.2", VersionType.MINOR);
            assertVersions(docs.get(2).getNodeRef(), "1.1", VersionType.MINOR);
            assertVersions(docs.get(3).getNodeRef(), "1.3", VersionType.MINOR);
            
            // update content using Alfresco
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<Void>>()
            {
                @Override
                public List<Void> execute() throws Throwable
                {
                    for (FileInfo fileInfo : docs)
                    {
                        ContentWriter writer = contentService.getWriter(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT, true);
                        writer.putContent("Content " + GUID.generate());
                    }
                    return null;
                }
            });
            
            assertVersions(docs.get(0).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(1).getNodeRef(), "1.2", VersionType.MINOR);
            assertVersions(docs.get(2).getNodeRef(), "1.2", VersionType.MINOR);
            assertVersions(docs.get(3).getNodeRef(), "1.4", VersionType.MINOR);
            
            // CMIS deleteContentStream
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    for (FileInfo fileInfo : docs)
                    {
                        cmisService.deleteContentStream(repositoryId, new Holder<String>(fileInfo.getNodeRef().toString()), null, null);
                    }
                    return null;
                }
            }, CmisVersion.CMIS_1_1);
            
            assertVersions(docs.get(0).getNodeRef(), "1.0", VersionType.MAJOR);
            assertVersions(docs.get(1).getNodeRef(), "1.2", VersionType.MINOR);
            assertVersions(docs.get(2).getNodeRef(), "1.3", VersionType.MINOR);
            assertVersions(docs.get(3).getNodeRef(), "1.5", VersionType.MINOR);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    private void assertVersions(final NodeRef nodeRef, final String expectedVersionLabel, final VersionType expectedVersionType)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<Void>>()
        {
            @Override
            public List<Void> execute() throws Throwable
            {
                assertTrue("Node should be versionable", nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE));
                
                Version version = versionService.getCurrentVersion(nodeRef);
                
                assertNotNull(version);
                assertEquals(expectedVersionLabel, version.getVersionLabel());
                assertEquals(expectedVersionType, version.getVersionType());
                
                return null;
            }
        });
        
        withCmisService(new CmisServiceCallback<Void>()
        {
            @Override
            public Void execute(CmisService cmisService)
            {
                String repositoryId = cmisService.getRepositoryInfos(null).get(0).getId();
                
                ObjectData data = 
                    cmisService.getObjectOfLatestVersion(repositoryId, nodeRef.toString(), null, Boolean.FALSE, null, null, null, null, null, null, null);
                
                assertNotNull(data);
                
                PropertyData<?> prop = data.getProperties().getProperties().get(PropertyIds.VERSION_LABEL);
                Object versionLabelCmisValue = prop.getValues().get(0);
                
                assertEquals(expectedVersionLabel, versionLabelCmisValue);
                
                return null;
            }
        }, CmisVersion.CMIS_1_1);
    }
    
    /**
     * Test to ensure that set of aspect returned by cmisService#getAllVersions for latest version is the same 
     * as for the object returned by cmisService#getObjectByPath.
     * 
     * See <a href="https://issues.alfresco.com/jira/browse/MNT-9557">MNT-9557</a>
     */
    @Test
    public void testLastVersionOfVersionSeries()
    {
        CallContext context = new SimpleCallContext("admin", "admin", CmisVersion.CMIS_1_0);
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        final String FOLDER = "testUpdatePropertiesSetDeleteContentVersioning-" + GUID.generate(),
                     DOC = "documentProperties-" + GUID.generate();
        
        try
        {
            final NodeRef nodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
            {
                @Override
                public NodeRef execute() throws Throwable
                {
                    // create folder
                    FileInfo folderInfo = fileFolderService.create(repositoryHelper.getCompanyHome(), FOLDER, ContentModel.TYPE_FOLDER);
                    nodeService.setProperty(folderInfo.getNodeRef(), ContentModel.PROP_NAME, FOLDER);
                    assertNotNull(folderInfo);

                    // create documents
                    FileInfo document = fileFolderService.create(folderInfo.getNodeRef(), DOC, ContentModel.TYPE_CONTENT);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_NAME, DOC);
                    nodeService.setProperty(document.getNodeRef(), ContentModel.PROP_DESCRIPTION, "Initial doc");

                    return document.getNodeRef();
                }
            });

            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // make sure that there is no version history yet
                    assertNull(versionService.getVersionHistory(nodeRef));

                    // create a version
                    // turn off auto-versioning
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    props.put(ContentModel.PROP_INITIAL_VERSION, false);
                    props.put(ContentModel.PROP_AUTO_VERSION, false);
                    props.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);

                    versionService.ensureVersioningEnabled(nodeRef, props);

                    return null;
                }
            });

            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    assertNotNull(versionService.getVersionHistory(nodeRef));
                    // create another one version
                    versionService.createVersion(nodeRef, null);

                    return null;
                }
            });

            final String NEW_DOC_NAME = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<String>()
            {
                @Override
                public String execute() throws Throwable
                {
                    // add aspect to the node
                    String newDocName = DOC + GUID.generate();
                    nodeService.addAspect(nodeRef, ContentModel.ASPECT_AUTHOR, null);
                    nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, newDocName);

                    return newDocName;
                }
            });

            CmisService cmisService = factory.getService(context);
            String repositoryId = cmisService.getRepositoryInfos(null).get(0).getId();
            
            List<ObjectData> versions = 
                cmisService.getAllVersions(repositoryId, nodeRef.toString(), null, null, null, null);
            assertNotNull(versions);
            // get the latest version
            ObjectData latestVersion = versions.get(0);
            // get the object
            ObjectData object = 
                // cmisService.getObjectOfLatestVersion(repositoryId, nodeRef.toString(), null, false, null, null, null, null, false, false, null);
                cmisService.getObjectByPath(repositoryId, "/" + FOLDER + "/" + NEW_DOC_NAME, null, null, null, null, false, false, null);
            
            assertNotNull(latestVersion);
            assertNotNull(object);
            
            Object objectDescriptionString = object.getProperties().getProperties().get("cmis:name").getValues().get(0);
            Object latestVersionDescriptionString = latestVersion.getProperties().getProperties().get("cmis:name").getValues().get(0);
            // ensure that node and latest version both have same description
            assertEquals(objectDescriptionString, latestVersionDescriptionString);
            
            Set<String> documentAspects = new HashSet<String>();
            for (CmisExtensionElement cmisEE : object.getProperties().getExtensions().get(0).getChildren())
            {
                documentAspects.add(cmisEE.getValue());
            }
            Set<String> latestVersionAspects = new HashSet<String>();
            for (CmisExtensionElement cmisEE : latestVersion.getProperties().getExtensions().get(0).getChildren())
            {
                latestVersionAspects.add(cmisEE.getValue());
            }
            // ensure that node and latest version both have the same set of aspects 
            assertEquals(latestVersionAspects, documentAspects);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }

    /**
     * Test to ensure that versioning properties have default values defined in Alfresco content model.
     * Testing  <b>cm:initialVersion</b>, <b>cm:autoVersion</b> and <b>cm:autoVersionOnUpdateProps</b> properties 
     * 
     * @throws Exception
     */
    @Test
    public void testVersioningPropertiesHaveDefaultValue() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        try
        {
            // Create document via CMIS
            final NodeRef documentNodeRef = withCmisService(new CmisServiceCallback<NodeRef>()
            {
                @Override
                public NodeRef execute(CmisService cmisService)
                {
                    String repositoryId = cmisService.getRepositoryInfos(null).get(0).getId();

                    String rootNodeId = cmisService.getObjectByPath(repositoryId, "/", null, true, IncludeRelationships.NONE, null, false, true, null).getId();

                    Collection<PropertyData<?>> propsList = new ArrayList<PropertyData<?>>();
                    propsList.add(new PropertyStringImpl(PropertyIds.NAME, "Folder-" + GUID.generate()));
                    propsList.add(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:folder"));

                    String folderId = cmisService.createFolder(repositoryId, new PropertiesImpl(propsList), rootNodeId, null, null, null, null);

                    propsList = new ArrayList<PropertyData<?>>();
                    propsList.add(new PropertyStringImpl(PropertyIds.NAME, "File-" + GUID.generate()));
                    propsList.add(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:document"));

                    String nodeId = cmisService.createDocument(repositoryId, new PropertiesImpl(propsList), folderId, null, null, null, null, null, null);

                    return new NodeRef(nodeId.substring(0, nodeId.indexOf(';')));
                }
            }, CmisVersion.CMIS_1_1);

            // check versioning properties
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<Void>>()
            {
                @Override
                public List<Void> execute() throws Throwable
                {
                    assertTrue(nodeService.exists(documentNodeRef));
                    assertTrue(nodeService.hasAspect(documentNodeRef, ContentModel.ASPECT_VERSIONABLE));

                    AspectDefinition ad = dictionaryService.getAspect(ContentModel.ASPECT_VERSIONABLE);
                    Map<QName, org.alfresco.service.cmr.dictionary.PropertyDefinition> properties = ad.getProperties();

                    for (QName qName : new QName[] {ContentModel.PROP_INITIAL_VERSION, ContentModel.PROP_AUTO_VERSION, ContentModel.PROP_AUTO_VERSION_PROPS})
                    {
                        Serializable property = nodeService.getProperty(documentNodeRef, qName);

                        assertNotNull(property);

                        org.alfresco.service.cmr.dictionary.PropertyDefinition pd = properties.get(qName);
                        assertNotNull(pd.getDefaultValue());

                        assertEquals(property, Boolean.parseBoolean(pd.getDefaultValue()));
                    }

                    return null;
                }
            });
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    @Test
    public void testCreateDocWithVersioningStateNone() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        try
        {
            // get repository id
            final String repositoryId = withCmisService(new CmisServiceCallback<String>()
            {
                @Override
                public String execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    final String repositoryId = repo.getId();
                    return repositoryId;
                }
            }, CmisVersion.CMIS_1_1);

            final NodeRef documentNodeRef = withCmisService(new CmisServiceCallback<NodeRef>()
            {
                @Override
                public NodeRef execute(CmisService cmisService)
                {
                    final PropertiesImpl properties = new PropertiesImpl();
                    String objectTypeId = "cmis:document";
                    properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
                    String fileName = "textFile" + GUID.generate();
                    properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));
                    final ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");

                    String nodeId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().getId(), contentStream, VersioningState.NONE, null, null);
                    return new NodeRef(nodeId.substring(0, nodeId.indexOf(';')));
                }
            }, CmisVersion.CMIS_1_1);

            // check versioning properties
            transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<Void>>()
            {
                @Override
                public List<Void> execute() throws Throwable
                {
                    assertTrue(nodeService.exists(documentNodeRef));
                    assertFalse(nodeService.hasAspect(documentNodeRef, ContentModel.ASPECT_VERSIONABLE));

                    return null;
                }
            });
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
    }
    
    /**
     * MNT-14951: Test that the list of parents can be retrieved for a folder.
     */
    @Test
    public void testCMISGetObjectParents() throws Exception
    {
        // setUp audit subsystem
        setupAudit();
        
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        try
        {
            final NodeRef folderWithTwoParents = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
            {
                @Override
                public NodeRef execute() throws Throwable
                {
                    NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();

                    String folder1 = GUID.generate();
                    FileInfo folderInfo1 = fileFolderService.create(companyHomeNodeRef, folder1, ContentModel.TYPE_FOLDER);
                    assertNotNull(folderInfo1);
                    
                    String folder2 = GUID.generate();
                    FileInfo folderInfo2 = fileFolderService.create(companyHomeNodeRef, folder2, ContentModel.TYPE_FOLDER);
                    assertNotNull(folderInfo2);
                    
                    // Create folder11 as a subfolder of folder1
                    String folder11 = GUID.generate();
                    FileInfo folderInfo11 = fileFolderService.create(folderInfo1.getNodeRef(), folder11, ContentModel.TYPE_FOLDER);
                    assertNotNull(folderInfo11);
                    
                    // Add folder2 as second parent for folder11
                    nodeService.addChild(folderInfo2.getNodeRef(), folderInfo11.getNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
                    
                    return folderInfo11.getNodeRef();
                }
            });
            
            withCmisService(new CmisServiceCallback<Void>()
            {
                @Override
                public Void execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertNotNull(repositories);
                    assertTrue(repositories.size() > 0);
                    String repositoryId = repositories.iterator().next().getId();

                    List<ObjectParentData> parents = cmisService.getObjectParents(repositoryId, folderWithTwoParents.getId(), null, Boolean.FALSE, IncludeRelationships.NONE,
                                                                                  "cmis:none", Boolean.FALSE, null);
                    // Check if the second parent was also returned.
                    assertEquals(2, parents.size());

                    return null;
                }
            }, CmisVersion.CMIS_1_1);
        }
        finally
        {
            auditSubsystem.destroy();
            AuthenticationUtil.popAuthentication();
        }
    }
    /*
     * REPO-3627 / MNT-19630: CMIS: Unable to call getAllVersions() if node is checked out and if binding type is WSDL
     * 
     * For WS binding the getAllVersions call is made with null objectId
     */
    @Test
    public void getAllVersionsWithNullObjectId()
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Create folder with file
        String folderName = "testfolder" + GUID.generate();
        String docName = "testdoc.txt" + GUID.generate();
        NodeRef folderRef = createContent(folderName, docName, false).getNodeRef();
        List<FileInfo> folderFileList = fileFolderService.list(folderRef);
        final NodeRef fileRef = folderFileList.get(0).getNodeRef();

        // Create new version for file
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // create a new version
                versionService.createVersion(fileRef, null);

                return null;
            }
        });

        // Checkout document and get all versions
        List<ObjectData> versions = withCmisService(new CmisServiceCallback<List<ObjectData>>()
        {
            @Override
            public List<ObjectData> execute(CmisService cmisService)
            {
                String repositoryId = cmisService.getRepositoryInfos(null).get(0).getId();
                ObjectData objectData = cmisService.getObjectByPath(repositoryId, "/" + folderName + "/" + docName, null, true, IncludeRelationships.NONE, null,
                        false, true, null);

                // Checkout
                Holder<String> objectId = new Holder<String>(objectData.getId());
                cmisService.checkOut(repositoryId, objectId, null, new Holder<Boolean>(true));

                // Call get all versions with null objectId
                List<ObjectData> versions = cmisService.getAllVersions(repositoryId, null, fileRef.toString(), null, null, null);

                return versions;
            }
        });

        // Check that the correct versions are retrieved
        assertEquals(2, versions.size());
        assertEquals(versions.get(0).getProperties().getProperties().get("cmis:versionLabel").getFirstValue(), "pwc");
        assertEquals(versions.get(1).getProperties().getProperties().get("cmis:versionLabel").getFirstValue(), "0.1");
    }

    /**
     *  Related to REPO-4613.
     *  This test makes sure that once a copy is checked out, the private copy contains the aspect P:cm:workingcopy
     */
    @Test
    public void aPrivateCopyMustContainTheWorkingCopyAspect_CMIS_1_1_Version()
    {

        // get repository id
        final String repositoryId = withCmisService(new CmisServiceCallback<String>()
        {
            @Override
            public String execute(CmisService cmisService)
            {
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                RepositoryInfo repo = repositories.get(0);
                final String repositoryId = repo.getId();
                return repositoryId;
            }
        }, CmisVersion.CMIS_1_1);

        final Properties currentProperties = withCmisService(new CmisServiceCallback<Properties>()
        {
            @Override
            public Properties execute(CmisService cmisService)
            {
                PropertiesImpl properties = new PropertiesImpl();
                String objectTypeId = "cmis:document";
                properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
                String fileName = "textFile" + GUID.generate();
                properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));
                final ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");

                String objectId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().getId(), contentStream, VersioningState.MAJOR, null, null);

                final Holder<String> objectIdHolder = new Holder<String>(objectId);
                cmisService.checkOut(repositoryId, objectIdHolder, null, null);
                ObjectData pwc = cmisService.getObject(repositoryId, objectIdHolder.getValue(), null, null, null, null, null, null, null);
                pwc.getProperties();

                Properties propertiesValues = pwc.getProperties();
                return propertiesValues;
            }
        }, CmisVersion.CMIS_1_1);

        List<String> secondaryTypeIds = (List<String>) currentProperties.getProperties().get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues();
        assertTrue(secondaryTypeIds.contains("P:cm:workingcopy"));
    }

    /**
     *  Related to REPO-4613.
     *  This test makes sure that once a copy is checked out, updateProperties method can be called
     *  and properly adds the new properties.
     */
    @Test
    public void aPrivateCopyMustAllowTheAdditionOfAspects_CMIS_1_1_Version()
    {
        final String aspectName = "P:cm:summarizable";
        final String propertyName = "cm:summary";
        final String propertyValue = "My summary";

        // get repository id
        final String repositoryId = withCmisService(new CmisServiceCallback<String>()
        {
            @Override
            public String execute(CmisService cmisService)
            {
                List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                RepositoryInfo repo = repositories.get(0);
                final String repositoryId = repo.getId();
                return repositoryId;
            }
        }, CmisVersion.CMIS_1_1);

        final Properties currentProperties = withCmisService(new CmisServiceCallback<Properties>()
        {
            @Override
            public Properties execute(CmisService cmisService)
            {
                PropertiesImpl properties = new PropertiesImpl();
                String objectTypeId = "cmis:document";
                properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, objectTypeId));
                String fileName = "textFile" + GUID.generate();
                properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, fileName));
                final ContentStreamImpl contentStream = new ContentStreamImpl(fileName, MimetypeMap.MIMETYPE_TEXT_PLAIN, "Simple text plain document");

                String objectId = cmisService.create(repositoryId, properties, repositoryHelper.getCompanyHome().getId(), contentStream, VersioningState.MAJOR, null, null);

                final Holder<String> objectIdHolder = new Holder<String>(objectId);
                cmisService.checkOut(repositoryId, objectIdHolder, null, null);
                cmisService.getObject(repositoryId, objectIdHolder.getValue(), null, null, null, null, null, null, null);

                properties = new PropertiesImpl();
                properties.addProperty(new PropertyStringImpl(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(aspectName)));
                properties.addProperty(new PropertyStringImpl(propertyName, propertyValue));

                cmisService.updateProperties(repositoryId, objectIdHolder, null, properties, null);

                cmisService.checkIn(repositoryId, objectIdHolder, false, null, null, "checkin", null, null, null, null);

                Properties propertiesValues = cmisService.getProperties(repositoryId, objectIdHolder.getValue(), null, null);
                return propertiesValues;
            }
        }, CmisVersion.CMIS_1_1);

        List<String> secondaryTypeIds = (List<String>) currentProperties.getProperties().get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues();
        assertTrue(secondaryTypeIds.contains(aspectName));
        assertEquals(currentProperties.getProperties().get(propertyName).getValues().get(0),  propertyValue);
    }

    @Test
    public void testSearchPreviousDelete()
    {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        try
        {
            final NodeRef companyHome = repositoryHelper.getCompanyHome();
            // create parentFolder
            RetryingTransactionCallback<Object> testCallbackFolder = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    NodeRef parentFolder = createFolder(companyHome, "testCreateParent" + GUID.generate(), ContentModel.TYPE_FOLDER);
                    return parentFolder;
                }
            };
            final NodeRef parentFolder = (NodeRef) transactionService.getRetryingTransactionHelper().doInTransaction(testCallbackFolder, false, true);

            // create children Folders
            final List<NodeRef> folders = new ArrayList<NodeRef>();
            RetryingTransactionCallback<Object> testCallbackChilds = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Throwable
                {
                    for (int i = 0; i < 10; i++)
                    {
                        folders.add(createFolder(parentFolder, "testCreateList-" + GUID.generate() + i, ContentModel.TYPE_FOLDER));
                    }
                    return folders;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(testCallbackChilds, false, true);

            // remove children nodes
            executorService.submit(new Runnable()
            {
                public void run()
                {
                    for (final NodeRef node : folders)
                    {
                        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
                        {
                            public Void doWork() throws Exception
                            {
                                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                                {
                                    public Void execute() throws Throwable
                                    {
                                        nodeService.deleteNode(node);
                                        return null;
                                    }
                                }, false, true);
                                return null;
                            }
                        }, AuthenticationUtil.getAdminUserName());
                    }
                }
            });

            // select children nodes removed
            withCmisService(new CmisServiceCallback<String>()
            {
                @Override
                public String execute(CmisService cmisService)
                {
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();

                    // prepare cmis query
                    String queryString = "SELECT cmis:name, cmis:objectId FROM cmis:folder WHERE IN_FOLDER('" + parentFolder + "')";
                    cmisService.query(repositoryId, queryString, Boolean.FALSE, Boolean.TRUE, IncludeRelationships.NONE, "", BigInteger.TEN,
                            BigInteger.ZERO, null);
                    return "";
                };
            }, CmisVersion.CMIS_1_1);
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
        finally
        {
            executorService.shutdownNow();
        }
    }

    /**
     * This test ensures that a non member user of a private site, can edit metadata on a document (where the non member user
     * has "SiteCollaborator" role) placed on the site.
     *
     * @throws Exception
     */
    @Test
    public void testMNT20006() throws Exception
    {
        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        final String nonMemberUsername = "user" + System.currentTimeMillis();
        final String nonMemberPassword = "pass" + System.currentTimeMillis();
        final String siteId = "site" + System.currentTimeMillis();
        final String originalDescription = "my description";

        NodeRef fileNode;

        try
        {
            fileNode = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    // Create user
                    authenticationService.createAuthentication(nonMemberUsername, nonMemberPassword.toCharArray());
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    String email = nonMemberUsername + "@testcmis.com";
                    props.put(ContentModel.PROP_USERNAME, nonMemberUsername);
                    props.put(ContentModel.PROP_FIRSTNAME, nonMemberUsername);
                    props.put(ContentModel.PROP_LASTNAME, nonMemberUsername);
                    props.put(ContentModel.PROP_EMAIL, email);
                    PersonInfo personInfo = personService.getPerson(personService.createPerson(props));
                    assertNotNull("Null person info", personInfo);

                    // Create site
                    SiteInfo siteInfo = siteService.createSite("myPreset", siteId, "myTitle", "myDescription", SiteVisibility.PRIVATE);
                    assertNotNull("Null site info", siteInfo);
                    NodeRef siteDocLib = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
                    assertNotNull("Null site doclib", siteDocLib);

                    // Create node in site
                    String nodeName = "node" + System.currentTimeMillis() + ".txt";
                    NodeRef fileNode = nodeService.createNode(siteDocLib, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT).getChildRef();
                    ContentWriter writer = contentService.getWriter(fileNode, ContentModel.PROP_CONTENT, true);
                    writer.putContent("my node content");
                    nodeService.setProperty(fileNode, ContentModel.PROP_TITLE, nodeName);
                    nodeService.setProperty(fileNode, ContentModel.PROP_DESCRIPTION, originalDescription);
                    assertNotNull("Null file node", fileNode);
                    assertTrue(nodeService.exists(fileNode));

                    // Sets node permissions to the user who is not member of the site and get site activities
                    permissionService.setPermission(fileNode, nonMemberUsername, SiteModel.SITE_COLLABORATOR, true);

                    return fileNode;
                }
            });
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }

        // Edit metadata
        final String newDescription = "new node description";

        Boolean updated = withCmisService(nonMemberUsername, nonMemberPassword, new CmisServiceCallback<Boolean>()
        {
            @Override
            public Boolean execute(CmisService cmisService)
            {
                Boolean updated = true;

                try
                {
                    // Obtain repository id
                    List<RepositoryInfo> repositories = cmisService.getRepositoryInfos(null);
                    assertTrue(repositories.size() > 0);
                    RepositoryInfo repo = repositories.get(0);
                    String repositoryId = repo.getId();

                    // Id holder
                    Holder<String> objectIdHolder = new Holder<String>(fileNode.toString());

                    // New Properties
                    PropertiesImpl newProperties = new PropertiesImpl();
                    newProperties.addProperty(new PropertyStringImpl(PropertyIds.DESCRIPTION, newDescription));
                    cmisService.updateProperties(repositoryId, objectIdHolder, null, newProperties, null);
                }
                catch (Exception e)
                {
                    updated = false;
                }

                return updated;
            };
        }, CmisVersion.CMIS_1_1);

        assertTrue("Document metadata not updated", updated);
    }

    private NodeRef createFolder(NodeRef parentNodeRef, String folderName, QName folderType) throws IOException
    {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, folderName);
        NodeRef nodeRef = nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, folderName);
        if (nodeRef != null)
        {
            nodeService.deleteNode(nodeRef);
        }
        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(folderName));
        nodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, folderType, properties)
                .getChildRef();
        return nodeRef;
    }
}
