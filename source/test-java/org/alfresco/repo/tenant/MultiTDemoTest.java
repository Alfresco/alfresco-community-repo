/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.tenant;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.repo.node.index.FullIndexRecoveryComponent;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.usage.UsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.FixMethodOrder;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author janv
 * since 3.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(OwnJVMTestsCategory.class)
public class MultiTDemoTest extends TestCase
{
    private static Log logger = LogFactory.getLog(MultiTDemoTest.class);
    
    private static ApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {ApplicationContextHelper.CONFIG_LOCATIONS[0], "classpath:tenant/mt-*context.xml"}
            );
    
    private NodeService nodeService;
    private NodeArchiveService nodeArchiveService;
    private NamespaceService namespaceService;
    private MutableAuthenticationService authenticationService;
    private PersonService personService;
    private SiteService siteService;
    private SearchService searchService;
    private ContentService contentService;
    private PermissionService permissionService;
    private OwnableService ownableService;
    private TenantAdminService tenantAdminService;
    private TenantService tenantService;
    private AuthorityService authorityService;
    private CategoryService categoryService;
    private CheckOutCheckInService cociService;
    private RepoAdminService repoAdminService;
    private DictionaryService dictionaryService;
    private UsageService usageService;
    private TransactionService transactionService;
    private FileFolderService fileFolderService;
    private Repository repositoryHelper;
    private FullIndexRecoveryComponent indexRecoverer;
    
    public static int NUM_TENANTS = 2;
    
    public static final String TEST_RUN = System.currentTimeMillis()+"";
    public static final String TEST_TENANT_DOMAIN = TEST_RUN+".my.test";
    public static final String TEST_TENANT_DOMAIN2 = TEST_TENANT_DOMAIN+"2";
    
    public static List<String> tenants;
    
    static 
    {
        tenants = new ArrayList<String>(NUM_TENANTS);
        for (int i = 1; i <= NUM_TENANTS; i++)
        {
            tenants.add(TEST_TENANT_DOMAIN+i);
        }
    }
    
    public static final String ROOT_DIR = "./tenantstores";
    
    public static final String DEFAULT_ADMIN_PW = "admin";
    
    public static final String DEFAULT_GUEST_UN = "guest";
    public static final String DEFAULT_GUEST_PW = "thiscanbeanything";
    
    public static final String TEST_USER1 = "alice";
    public static final String TEST_USER2 = "bob";
    public static final String TEST_USER3 = "eve";
    public static final String TEST_USER4 = "fred";
    
    private static Set<StoreRef> DEFAULT_STORES = new HashSet<StoreRef>(Arrays.asList(new StoreRef[]
    {
        new StoreRef("workspace://lightWeightVersionStore"), new StoreRef("system://system"),
        new StoreRef("workspace://version2Store"), new StoreRef("user://alfrescoUserStore"),
        new StoreRef("workspace://SpacesStore"), new StoreRef("archive://SpacesStore")
    }));
    private static final int DEFAULT_STORE_COUNT = DEFAULT_STORES.size();
        
    public static StoreRef SPACES_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
    
    
    public MultiTDemoTest()
    {
        super();
    }
    
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        nodeService = (NodeService) ctx.getBean("NodeService");
        nodeArchiveService = (NodeArchiveService) ctx.getBean("nodeArchiveService");
        namespaceService = (NamespaceService) ctx.getBean("NamespaceService");
        authenticationService = (MutableAuthenticationService) ctx.getBean("AuthenticationService");
        tenantAdminService = (TenantAdminService) ctx.getBean("tenantAdminService");
        tenantService = (TenantService) ctx.getBean("tenantService");
        personService = (PersonService) ctx.getBean("PersonService");
        searchService = (SearchService) ctx.getBean("SearchService");
        contentService = (ContentService) ctx.getBean("ContentService");
        permissionService = (PermissionService) ctx.getBean("PermissionService");
        ownableService = (OwnableService) ctx.getBean("OwnableService");
        authorityService = (AuthorityService) ctx.getBean("AuthorityService");
        categoryService = (CategoryService) ctx.getBean("CategoryService");
        cociService = (CheckOutCheckInService) ctx.getBean("CheckoutCheckinService");
        repoAdminService = (RepoAdminService) ctx.getBean("RepoAdminService");
        dictionaryService = (DictionaryService) ctx.getBean("DictionaryService");
        usageService = (UsageService) ctx.getBean("usageService");
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        fileFolderService = (FileFolderService) ctx.getBean("FileFolderService");
        ownableService = (OwnableService) ctx.getBean("OwnableService");
        repositoryHelper = (Repository) ctx.getBean("repositoryHelper");
        siteService = (SiteService) ctx.getBean("SiteService");
        
        ChildApplicationContextFactory luceneSubSystem = (ChildApplicationContextFactory) ctx.getBean("lucene");
        indexRecoverer = (FullIndexRecoveryComponent) luceneSubSystem.getApplicationContext().getBean("search.indexRecoveryComponent");
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName()); // authenticate as super-admin
        
        createTenants();
        createUsers();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    private void createTenants()
    {
        for (final String tenantDomain : tenants)
        {
            createTenant(tenantDomain);
        }
    }
    
    private void createTenant(final String tenantDomain)
    {
        // create tenants (if not already created)
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                if (! tenantAdminService.existsTenant(tenantDomain))
                {
                    //tenantAdminService.createTenant(tenantDomain, DEFAULT_ADMIN_PW.toCharArray(), ROOT_DIR + "/" + tenantDomain);
                    tenantAdminService.createTenant(tenantDomain, (DEFAULT_ADMIN_PW+" "+tenantDomain).toCharArray(), null); // use default root dir
                    
                    logger.info("Created tenant " + tenantDomain);
                }
                
                return null;
            }
        }, TenantService.DEFAULT_DOMAIN);
    }
    
    private void deleteTenant(final String tenantDomain)
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                // delete tenant (if it exists)
                TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        if (tenantAdminService.existsTenant(tenantDomain))
                        {
                            // TODO: WARNING: HACK for ALF-19155: MT deleteTenant does not work
                            //       PersonService prevents 'guest' authorities from being deleted
                            {
                                BehaviourFilter behaviourFilter = (BehaviourFilter) ctx.getBean("policyBehaviourFilter");
                                behaviourFilter.disableBehaviour(ContentModel.TYPE_PERSON);
                                behaviourFilter.disableBehaviour(ContentModel.ASPECT_UNDELETABLE);
                            }
                            tenantAdminService.deleteTenant(tenantDomain);
                            
                            logger.info("Deleted tenant " + tenantDomain);
                        }
                        
                        return null;
                    }
                }, TenantService.DEFAULT_DOMAIN);
                return null;
            }
        });
    }
    
    private void createUsers()
    {
        for (final String tenantDomain : tenants)
        {
            String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    createUser(TEST_USER1, tenantDomain, TEST_USER1+" "+tenantDomain);
                    createUser(TEST_USER2, tenantDomain, TEST_USER2+" "+tenantDomain);
                    
                    if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                    {
                        createUser(TEST_USER3, tenantDomain, TEST_USER3+" "+tenantDomain);
                    }
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain);
        }
    }
    
    // note: needs to come before test10CreateCategories & test15COCIandSearch ?
    public synchronized void test00_ALF_17681() throws Exception
    {
        // The issue was found on Lucene
        final String tenantDomain = TEST_RUN+".alf17681";
        final String query = "PATH:\"/app:company_home/app:dictionary\"";
        
        // Create tenant
        createTenant(tenantDomain);
        
        final String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
        // Search for Data dictionary by tenant admin
        int count = searchForDataDictionary(tenantAdminName, query);
        
        assertEquals("Data dictionary should be found for tenant. ", 1, count);
        
        indexRecoverer.setRecoveryMode(FullIndexRecoveryComponent.RecoveryMode.FULL.name());
        
        // reindex
        Thread reindexThread = new Thread()
        {
            public void run()
            {
                indexRecoverer.reindex();
            }
        };
        
        reindexThread.start();
        
        // must allow the rebuild to complete or the test after this one will fail to validate their indexes 
        // - as they now will be deleted.
        reindexThread.join();
        
        // wait a bit and then terminate
        wait(20000);
        indexRecoverer.setShutdown(true);
        wait(20000);
        
        // Search for Data dictionary by tenant admin
        int countAfter = searchForDataDictionary(tenantAdminName, query);
        
        assertEquals("Data dictionary should be found for tenant after FULL reindex. ", 1, countAfter);
    }
    
    /*
    public void test00_Setup() throws Throwable
    {
        // test common setup (see "setUp")
    }
    */
    
    public void test01CreateTenants() throws Throwable
    {
        // ignore common setup - test here explicitly
        
        logger.info("Create tenants");
        
        final String tenantDomain1 = TEST_RUN+".one.createTenant";
        final String tenantDomain2 = TEST_RUN+".two.createTenant";
        
        String[] tenantDomains = new String[] {tenantDomain1, tenantDomain2 };
        
        for (final String tenantDomain : tenantDomains)
        {
            createTenant(tenantDomain);
        }
        
        // check default (super-tenant) domain
        
        List<PersonInfo> persons = personService.getPeople(null, true, null, new PagingRequest(0, Integer.MAX_VALUE, null)).getPage();
        
        //assertEquals(2, personRefs.size()); // super-tenant: admin, guest (note: checking for 2 assumes that this test is run in a fresh bootstrap env)
        
        for (PersonInfo person : persons)
        {
            String userName = person.getUserName();
            for (final String tenantDomain : tenantDomains)
            {
                assertFalse("Unexpected (tenant) user: "+userName, userName.endsWith(tenantDomain));
            }
        }
    }
    
    
    private void deleteTestAuthoritiesForTenant(final String[] uniqueGroupNames, final String userName)
    {
        String tenantDomain = tenantService.getUserDomain(userName);
        
        // Check deletion for tenant1
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // find person
                NodeRef personNodeRef = personService.getPerson(userName);
                NodeRef homeSpaceRef = (NodeRef)nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
                assertNotNull(homeSpaceRef);
                
                // Delete authorities
                for (int i = 0; i < uniqueGroupNames.length; i++)
                {
                    authorityService.deleteAuthority("GROUP_" + uniqueGroupNames[i]);
                }
                return null;
            }
        }, userName, tenantDomain);
    }
    
    private void createTestAuthoritiesForTenant(final String[] uniqueGroupNames, final String userName)
    {
        String tenantDomain = tenantService.getUserDomain(userName);
        
        // Create groups for tenant
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // find person
                NodeRef personNodeRef = personService.getPerson(userName);
                NodeRef homeSpaceRef = (NodeRef)nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
                assertNotNull(homeSpaceRef);
                
                for (int i = 0; i < uniqueGroupNames.length; i++)
                {
                    authorityService.createAuthority(AuthorityType.GROUP, uniqueGroupNames[i]);
                    permissionService.setPermission(homeSpaceRef, "GROUP_" + uniqueGroupNames[i], "Consumer", true);
                }
                
                return null;
            }
        }, userName, tenantDomain);
    }
    
    private void checkTestAuthoritiesPresence(final String[] uniqueGroupNames, final String userName, final boolean shouldPresent)
    {
        String tenantDomain = tenantService.getUserDomain(userName);
        
        // Check that created permissions are not visible to tenant 2
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                NodeRef personNodeRef = personService.getPerson(userName);
                NodeRef homeSpaceRef = (NodeRef)nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
                Set<AccessPermission> perms = permissionService.getAllSetPermissions(homeSpaceRef);
                Set<String> auths = authorityService.getAllAuthorities(AuthorityType.GROUP);
                
                for (int i = 0; i < uniqueGroupNames.length; i++)
                {
                    AccessPermission toCheck = new AccessPermissionImpl("Consumer", AccessStatus.ALLOWED, "GROUP_" + uniqueGroupNames[i], 0);
                    if (shouldPresent)
                    {
                        assertTrue(auths.contains("GROUP_" + uniqueGroupNames[i]));
                        assertTrue(perms.contains(toCheck));
                    }
                    else
                    {
                        assertTrue(!auths.contains("GROUP_" + uniqueGroupNames[i]));
                        assertTrue(!perms.contains(toCheck));
                    }
                }
                
                return null;
            }
        }, userName, tenantDomain);
    }
    
    private void createGroup(String shortName, String parentShortName)
    {
        // create new Group using authority Service
        String groupName = this.authorityService.getName(AuthorityType.GROUP, shortName);
        if (this.authorityService.authorityExists(groupName) == false)
        {
           String parentGroupName = null;
           if (parentShortName != null)
           {
               parentGroupName = this.authorityService.getName(AuthorityType.GROUP, parentShortName);
               if (this.authorityService.authorityExists(parentGroupName) == false)
               {
                   logger.warn("Parent group does not exist: " + parentShortName);
                   return;
               }
           }
           
           this.authorityService.createAuthority(AuthorityType.GROUP, shortName);
           
           if (parentGroupName != null)
           {
               addToGroup(parentShortName, groupName);
           }
        }
        else
        {
            logger.warn("Group already exists: " + shortName);
        }
    }
    
    private void addToGroup(String parentGroupShortName, String authorityName)
    {
        String parentGroupName = this.authorityService.getName(AuthorityType.GROUP, parentGroupShortName);
        authorityService.addAuthority(parentGroupName, authorityName);
    }
    
    private NodeRef createUser(String baseUserName, String tenantDomain, String password)
    {
        String userName = tenantService.getDomainUser(baseUserName, tenantDomain);
        
        NodeRef personNodeRef = null;
        
        if (! this.authenticationService.authenticationExists(userName))
        {
            NodeRef baseHomeFolder = getUserHomesNodeRef(SPACES_STORE);
            
            // Create the users home folder
            NodeRef homeFolder = createHomeSpaceFolderNode(
                                                baseHomeFolder,
                                                baseUserName,
                                                userName);
            
            // Create the authentication
            this.authenticationService.createAuthentication(userName, password.toCharArray());
            
            // Create the person
            Map<QName, Serializable> personProperties = new HashMap<QName, Serializable>();
            personProperties.put(ContentModel.PROP_USERNAME, userName);
            personProperties.put(ContentModel.PROP_HOMEFOLDER, homeFolder);
            personProperties.put(ContentModel.PROP_FIRSTNAME, baseUserName);
            personProperties.put(ContentModel.PROP_LASTNAME, baseUserName+"-"+tenantDomain); // add domain suffix here for demo only
            personProperties.put(ContentModel.PROP_EMAIL, userName);
            
            personNodeRef = this.personService.createPerson(personProperties);
            
            // ensure the user can access their own Person object
            this.permissionService.setPermission(personNodeRef, userName, permissionService.getAllPermission(), true);
            
            NodeRef checkHomeSpaceRef = (NodeRef)nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
            assertNotNull(checkHomeSpaceRef);
            
            logger.info("Created user " + userName);
        }
        else
        {
            personNodeRef = personService.getPerson(userName);
            
            logger.info("Found existing user " + userName);
        }
        
        return personNodeRef;
    }
    
    private void loginLogoutUser(String username, String password)
    {
        // authenticate via the authentication service
        authenticationService.authenticate(username, password.toCharArray());
        
        // set the user name as stored by the back end 
        username = authenticationService.getCurrentUserName();
        
        NodeRef personRef = personService.getPerson(username);
        NodeRef homeSpaceRef = (NodeRef)nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
        
        // check that the home space node exists - else user cannot login
        if (nodeService.exists(homeSpaceRef) == false)
        {
           throw new InvalidNodeRefException(homeSpaceRef);
        }
        
        // logout
        authenticationService.clearCurrentSecurityContext();
    }
    
    private NodeRef getUserHomesNodeRef(StoreRef storeRef)
    {
        // get the "User Homes" location
        return findFolderNodeRef(storeRef, "/app:company_home/app:user_homes");
    }
    
    private NodeRef getWebClientExtensionNodeRef(StoreRef storeRef)
    {
        // get the "Web Client Extensions" location
        return findFolderNodeRef(storeRef, "/app:company_home/app:dictionary/app:webclient_extension");
    }
    
    private NodeRef findFolderNodeRef(StoreRef storeRef, String folderXPath)
    {
        NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);
        
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef, folderXPath, null, namespaceService, false);
        
        NodeRef folderNodeRef = null;
        if (nodeRefs.size() != 1)
        {
            throw new AlfrescoRuntimeException("Cannot find folder location: " + folderXPath);
        }
        else
        {
            folderNodeRef = nodeRefs.get(0);
        }
        return folderNodeRef;
    }
    
    private NodeRef createFolderNode(NodeRef parentFolderNodeRef, String nameValue)
    {
        if (nameValue != null)
        {       
            Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>();
            folderProps.put(ContentModel.PROP_NAME, nameValue);
            
            return this.nodeService.createNode(
                    parentFolderNodeRef, 
                    ContentModel.ASSOC_CONTAINS, 
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nameValue),
                    ContentModel.TYPE_FOLDER,
                    folderProps).getChildRef();
        }
        
        return null;
    }
    
    private NodeRef createCategory(StoreRef storeRef, NodeRef parentCategoryRef, String name, String description)
    {
        // create category using categoryservice
        NodeRef ref;
        if (parentCategoryRef == null)
        {
           ref = this.categoryService.createRootCategory(storeRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, name);
        }
        else
        {
           ref = categoryService.createCategory(parentCategoryRef, name);
        }
        
        // apply the titled aspect - for description
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(1, 1.0f);
        titledProps.put(ContentModel.PROP_DESCRIPTION, description);
        this.nodeService.addAspect(ref, ContentModel.ASPECT_TITLED, titledProps);
        
        return ref;
    }
    
    private NodeRef createHomeSpaceFolderNode(NodeRef folderNodeRef, String spaceName, String userName)
    {
        if (spaceName != null)
        {       
            Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>();
            folderProps.put(ContentModel.PROP_NAME, spaceName);
            
            NodeRef nodeRef = this.nodeService.createNode(
                                                folderNodeRef, 
                                                ContentModel.ASSOC_CONTAINS, 
                                                QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, spaceName),
                                                ContentModel.TYPE_FOLDER,
                                                folderProps).getChildRef();
            
            // apply the uifacets aspect - icon and title props
            Map<QName, Serializable> uiFacetsProps = new HashMap<QName, Serializable>(3);
            uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-default");
            uiFacetsProps.put(ContentModel.PROP_TITLE, spaceName);
            this.nodeService.addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);
            
            setupHomeSpacePermissions(nodeRef, userName);
            
            return nodeRef;
        }
        
        return null;
    }
    
    private void setupHomeSpacePermissions(NodeRef homeSpaceRef, String userName)
    {
       // Admin Authority has full permissions by default (automatic - set in the permission config)
       // give full permissions to the new user
       this.permissionService.setPermission(homeSpaceRef, userName, permissionService.getAllPermission(), true);
       
       // by default other users will only have GUEST access to the space contents
       String permission = "Consumer";
       
       if (permission != null && permission.length() != 0)
       {
          this.permissionService.setPermission(homeSpaceRef, permissionService.getAllAuthorities(), permission, true);
       }
       
       // the new user is the OWNER of their own space and always has full permissions
       this.ownableService.setOwner(homeSpaceRef, userName);
       this.permissionService.setPermission(homeSpaceRef, permissionService.getOwnerAuthority(), permissionService.getAllPermission(), true);
       
       // now detach (if we did this first we could not set any permissions!)
       this.permissionService.setInheritParentPermissions(homeSpaceRef, false);
    }
    
    private NodeRef getHomeSpaceFolderNode(String userName)
    {
        return (NodeRef)this.nodeService.getProperty(personService.getPerson(userName), ContentModel.PROP_HOMEFOLDER);
    }
    
    private NodeRef addContent(NodeRef spaceRef, String name, String textData, String mimeType)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);
        
        ChildAssociationRef association = nodeService.createNode(spaceRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                contentProps);
        
        NodeRef content = association.getChildRef();
        
        // add titled aspect (for Web Client display)
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        titledProps.put(ContentModel.PROP_TITLE, name);
        titledProps.put(ContentModel.PROP_DESCRIPTION, name);
        this.nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);
        
        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
        
        writer.setMimetype(mimeType);
        writer.setEncoding("UTF-8");
        
        writer.putContent(textData);
        
        return content;
    }
    
    private NodeRef addContent(NodeRef spaceRef, String name, InputStream is, String mimeType)
    {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        contentProps.put(ContentModel.PROP_NAME, name);
        
        ChildAssociationRef association = nodeService.createNode(spaceRef,
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name), 
                ContentModel.TYPE_CONTENT,
                contentProps);
        
        NodeRef content = association.getChildRef();
        
        // add titled aspect (for Web Client display)
        Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>();
        titledProps.put(ContentModel.PROP_TITLE, name);
        titledProps.put(ContentModel.PROP_DESCRIPTION, name);
        this.nodeService.addAspect(content, ContentModel.ASPECT_TITLED, titledProps);
        
        ContentWriter writer = contentService.getWriter(content, ContentModel.PROP_CONTENT, true);
        
        writer.setMimetype(mimeType);
        writer.setEncoding("UTF-8");
        
        writer.putContent(is);
        
        return content;
    }
    
    private SiteInfo createSite(String siteId)
    {
        SiteInfo siteInfo = siteService.createSite(null, siteId, "title - "+siteId, "description - "+siteId, SiteVisibility.PRIVATE);
        
        // ensure that the Document Library folder is pre-created so that test code can start creating content straight away.
        // At the time of writing V4.1 does not create this folder automatically, but Thor does.
        NodeRef result = siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
        if (result == null)
        {
            result = siteService.createContainer(siteId, SiteService.DOCUMENT_LIBRARY, ContentModel.TYPE_FOLDER, null);
        }
        
        return siteInfo;
    }
    
    public void test02NonSharedGroupDeletion()
    {
        // ignore common setup - test here explicitly
        
        final String tenantDomain1 = TEST_RUN+".groupdel1";
        final String tenantDomain2 = TEST_RUN+".groupdel2";
        
        try
        {
            final String[] tenantUniqueGroupNames = new String[10];
            final String[] superadminUniqueGroupNames = new String[10];
            for (int i = 0; i < tenantUniqueGroupNames.length; i++)
            {
                tenantUniqueGroupNames[i] = TEST_RUN + "test_group" + i;
                superadminUniqueGroupNames[i] = TEST_RUN + "test_group_sa" + i;
            }
            
            clearUsage(AuthenticationUtil.getAdminUserName());
            
            createTenant(tenantDomain1);
            createTenant(tenantDomain2);
            
            final String tenantAdminName1 = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain1);
            final String tenantAdminName2 = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain2);
            final String superAdmin = "admin";
            
            // Create test authorities that are visible only to tenant1
            clearUsage(tenantDomain1);
            createTestAuthoritiesForTenant(tenantUniqueGroupNames, tenantAdminName1);
            // Check that tenant1's authorities are visible to tenant1
            clearUsage(tenantDomain1);
            checkTestAuthoritiesPresence(tenantUniqueGroupNames, tenantAdminName1, true);
            // Check that tenant1's authorities are not visible to tenant2
            clearUsage(tenantDomain2);
            checkTestAuthoritiesPresence(tenantUniqueGroupNames, tenantAdminName2, false);
            // Check that tenant1's authorities are not visible to super-admin
            checkTestAuthoritiesPresence(tenantUniqueGroupNames, superAdmin, false);
            
            
            // Create test authorities that are visible only to super-admin
            createTestAuthoritiesForTenant(superadminUniqueGroupNames, superAdmin);
            // Check that super-admin's authorities are not visible to tenant1
            clearUsage(tenantDomain1);
            checkTestAuthoritiesPresence(superadminUniqueGroupNames, tenantAdminName1, false);
            // Check that super-admin's authorities are not visible to tenant2
            clearUsage(tenantDomain2);
            checkTestAuthoritiesPresence(superadminUniqueGroupNames, tenantAdminName2, false);
            // Check that super-admin's authorities are visible to super-admin
            checkTestAuthoritiesPresence(superadminUniqueGroupNames, superAdmin, true);
            
            
            // Delete tenant1's authorities
            clearUsage(tenantDomain1);
            deleteTestAuthoritiesForTenant(tenantUniqueGroupNames, tenantAdminName1);
            // Check that tenant1's authorities are not visible to tenant1
            checkTestAuthoritiesPresence(tenantUniqueGroupNames, tenantAdminName1, false);
            
            // Delete super-admin's authorities
            deleteTestAuthoritiesForTenant(superadminUniqueGroupNames, superAdmin);
            // Check that super-admin's authorities are not visible to super-admin
            checkTestAuthoritiesPresence(superadminUniqueGroupNames, superAdmin, false);
        }
        finally
        {
            deleteTenant(tenantDomain1);
            deleteTenant(tenantDomain2);
        }
    }
    
    public void test03SharedGroupDeletion()
    {
        // ignore common setup - test here explicitly
        
        final String tenantDomain1 = TEST_RUN+".groupdel3";
        final String tenantDomain2 = TEST_RUN+".groupdel4";
        
        try
        {
            final String[] commonTenantUniqueGroupNames = new String[10];
            for (int i = 0; i < commonTenantUniqueGroupNames.length; i++)
            {
                commonTenantUniqueGroupNames[i] = TEST_RUN + "test_group" + i;
            }
            
            clearUsage(AuthenticationUtil.getAdminUserName());
            
            createTenant(tenantDomain1);
            createTenant(tenantDomain2);
            
            final String tenantAdminName1 = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain1);
            final String tenantAdminName2 = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain2);
            final String superAdmin = "admin";
            
            // Create test common authorities for tenant1
            clearUsage(tenantDomain1);
            createTestAuthoritiesForTenant(commonTenantUniqueGroupNames, tenantAdminName1);
            // Create test common authorities for tenant2
            clearUsage(tenantDomain2);
            createTestAuthoritiesForTenant(commonTenantUniqueGroupNames, tenantAdminName2);
            // Create test common authorities for super-admin
            createTestAuthoritiesForTenant(commonTenantUniqueGroupNames, superAdmin);
            
            // Check that authorities are visible to tenant1
            clearUsage(tenantDomain1);
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, tenantAdminName1, true);
            // Check that authorities are visible to tenant2
            clearUsage(tenantDomain2);
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, tenantAdminName2, true);
            // Check that authorities are visible to super-admin
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, superAdmin, true);
            
            // Delete tenant1's authorities
            clearUsage(tenantDomain1);
            deleteTestAuthoritiesForTenant(commonTenantUniqueGroupNames, tenantAdminName1);
            // Check that authorities are not visible to tenant1
            clearUsage(tenantDomain1);
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, tenantAdminName1, false);
            // Check that authorities are visible to tenant2
            clearUsage(tenantDomain2);
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, tenantAdminName2, true);
            // Check that authorities are visible to super-admin
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, superAdmin, true);
            
            // Create test common authorities for tenant1
            clearUsage(tenantDomain1);
            createTestAuthoritiesForTenant(commonTenantUniqueGroupNames, tenantAdminName1);
            // Delete tenant2's authorities
            clearUsage(tenantDomain2);
            deleteTestAuthoritiesForTenant(commonTenantUniqueGroupNames, tenantAdminName2);
            // Check that authorities are visible to tenant1
            clearUsage(tenantDomain1);
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, tenantAdminName1, true);
            // Check that authorities are not visible to tenant2
            clearUsage(tenantDomain2);
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, tenantAdminName2, false);
            // Check that authorities are visible to super-admin
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, superAdmin, true);
            
            // Create test common authorities for tenant2
            clearUsage(tenantDomain2);
            createTestAuthoritiesForTenant(commonTenantUniqueGroupNames, tenantAdminName2);
            // Delete super-admin's authorities
            deleteTestAuthoritiesForTenant(commonTenantUniqueGroupNames, superAdmin);
            // Check that authorities are visible to tenant1
            clearUsage(tenantDomain1);
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, tenantAdminName1, true);
            // Check that authorities are visible to tenant2
            clearUsage(tenantDomain2);
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, tenantAdminName2, true);
            // Check that authorities are not visible to super-admin
            checkTestAuthoritiesPresence(commonTenantUniqueGroupNames, superAdmin, false);
        }
        finally
        {
            AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
            
            deleteTenant(tenantDomain1);
            deleteTenant(tenantDomain2);
        }
    }
    
    
    
    public void test04_ETHREEOH_2015()
    {
        // ignore common setup - test here explicitly
        
        final String tenantDomain1 = TEST_RUN+".one.ethreeoh2015";
        final String tenantDomain2 = TEST_RUN+".two.ethreeoh2015";
        
        clearUsage(AuthenticationUtil.getAdminUserName());
        
        createTenant(tenantDomain1);
        
        String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain1);
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                createUser(TEST_USER1, tenantDomain1, TEST_USER1+" "+tenantDomain1);
                
                return null;
            }
        }, tenantAdminName, tenantDomain1);
        
        createTenant(tenantDomain2);
    }
    
    private void clearUsage(String userName)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName()); // authenticate as super-admin
        
        // find person
        NodeRef personNodeRef = personService.getPerson(userName);
        // clear user usage
        nodeService.setProperty(personNodeRef, ContentModel.PROP_SIZE_CURRENT, null);
        usageService.deleteDeltas(personNodeRef);
    }
    
    
    public void test05ValidateUsers() throws Throwable
    {
        // use common setup
        logger.info("Validate demo users");
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                List<PersonInfo> persons = personService.getPeople(null, true, null, new PagingRequest(0, Integer.MAX_VALUE, null)).getPage();
                //assertEquals(2, personRefs.size()); // super-tenant: admin, guest (note: checking for 2 assumes that this test is run in a fresh bootstrap env)
                for (PersonInfo person : persons)
                {
                    String userName = person.getUserName();
                    for (final String tenantDomain : tenants)
                    {
                        assertFalse("Unexpected (tenant) user: "+userName, userName.endsWith(tenantDomain));
                    }
                }
                
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
        
        try
        {
            for (final String tenantDomain : tenants)
            {
                String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
                
                TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork() throws Exception
                    {
                        List<PersonInfo> persons = personService.getPeople(null, null, null, new PagingRequest(0, Integer.MAX_VALUE, null)).getPage();
                        
                        for (PersonInfo person : persons)
                        {
                            NodeRef personRef = person.getNodeRef();
                            
                            String userName = (String)nodeService.getProperty(personRef, ContentModel.PROP_USERNAME); 
                            assertTrue(userName.endsWith(tenantDomain));
                            
                            logger.info("Validate users: get all people - found user: "+userName);
                            
                            NodeRef homeSpaceRef = (NodeRef)nodeService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
                            assertNotNull(homeSpaceRef);
                        }
                        
                        if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                        {
                            assertEquals(5, persons.size()); // admin@tenant, guest@tenant, alice@tenant, bob@tenant, eve@tenant
                        }
                        else
                        {
                            assertEquals(4, persons.size()); // admin@tenant, guest@tenant, alice@tenant, bob@tenant
                        }
                        
                        return null;
                    }
                }, tenantAdminName, tenantDomain);
            }
        }   
        catch (Throwable t)
        {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));
            System.err.println(stackTrace.toString());
            throw t;
        } 
    }
    
    public void test06LoginTenantUsers() throws Throwable
    {
        // use common setup
        logger.info("Login tenant users");
        
        try
        {
            AuthenticationUtil.clearCurrentSecurityContext();
            
            for (final String tenantDomain : tenants)
            {
                loginLogoutUser(tenantService.getDomainUser(TEST_USER1, tenantDomain), TEST_USER1+" "+tenantDomain);
                
                loginLogoutUser(tenantService.getDomainUser(TEST_USER2, tenantDomain), TEST_USER2+" "+tenantDomain);
                
                if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                {
                    loginLogoutUser(tenantService.getDomainUser(TEST_USER3, tenantDomain), TEST_USER3+" "+tenantDomain);
                }
            }
        }   
        catch (Throwable t)
        {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));
            System.err.println(stackTrace.toString());
            throw t;
        }
    }
    
    public void test07LoginTenantGuests() throws Throwable
    {
        logger.info("Login tenant guests");
        
        try
        {
            AuthenticationUtil.clearCurrentSecurityContext();
            
            for (final String tenantDomain : tenants)
            {
                loginLogoutUser(tenantService.getDomainUser(DEFAULT_GUEST_UN, tenantDomain), DEFAULT_GUEST_UN);
            }
        }   
        catch (Throwable t)
        {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));
            System.err.println(stackTrace.toString());
            throw t;
        }
    }
    
    public void test08LoginTenantAdmin() throws Throwable
    {
        logger.info("Login tenant admins");
        
        try
        {
            AuthenticationUtil.clearCurrentSecurityContext();
            
            for (final String tenantDomain : tenants)
            {
                loginLogoutUser(tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain), DEFAULT_ADMIN_PW+" "+tenantDomain);
            }
        }   
        catch (Throwable t)
        {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));
            System.err.println(stackTrace.toString());
            throw t;
        }
    }
    
    public void test09CreateGroups()
    {
        logger.info("Create demo groups");
        
        assertTrue(tenants.size() > 0);
        
        final int rootGrpsOrigCnt = TenantUtil.runAsUserTenant(new TenantRunAsWork<Integer>()
        {
            public Integer doWork() throws Exception
            {
                return authorityService.getAllRootAuthorities(AuthorityType.GROUP).size();
            }
        }, tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenants.get(0)), tenants.get(0));
        
        // create groups and add users
        for (final String tenantDomain : tenants)
        {
            final String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    createGroup("GrpA", null);
                    createGroup("SubGrpA", "GrpA");
                    
                    createGroup("GrpB", null);
                    
                    createGroup("GrpC", null);
                    
                    if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                    {
                        createGroup("SubGrpC", "GrpC");
                    }
                    
                    createGroup("GrpD", null);
                    addToGroup("GrpD", tenantAdminName);
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain);
        }
        
        // check groups/users
        for (final String tenantDomain : tenants)
        {
            final String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    Set<String> rootGrps = authorityService.getAllRootAuthorities(AuthorityType.GROUP);
                    assertEquals(rootGrpsOrigCnt+4, rootGrps.size());
                    
                    Set<String> auths = authorityService.getContainedAuthorities(null, "GROUP_GrpA", true);
                    assertEquals(1, auths.size());
                    
                    auths = authorityService.getContainedAuthorities(null, "GROUP_GrpB", true);
                    assertEquals(0, auths.size());
                    
                    auths = authorityService.getContainedAuthorities(null, "GROUP_GrpC", true);
                    if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                    {
                        assertEquals(1, auths.size());
                    }
                    else
                    {
                        assertEquals(0, auths.size());
                    }
                    
                    auths = authorityService.getContainedAuthorities(null, "GROUP_GrpD", true);
                    assertEquals(1, auths.size());
                    assertTrue(auths.toArray()[0].equals(tenantAdminName));
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain);
        }
    }
    
    public void test10CreateCategories()
    {
        logger.info("Create demo categories");
        
        // super admin
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                logger.info("Create demo categories - super tenant");
                createCategoriesImpl("");
                
                return null;
            }
        }, AuthenticationUtil.getAdminUserName(), TenantService.DEFAULT_DOMAIN);
        
        for (final String tenantDomain : tenants)
        {
            String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    logger.info("Create demo categories - "+tenantDomain);
                    
                    createCategoriesImpl(tenantDomain);
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain);
            
        }
    }
    
    @SuppressWarnings("unchecked")
    private void createCategoriesImpl(String tenantDomain)
    {
        if (tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
        {
            Collection<ChildAssociationRef> childAssocs = categoryService.getRootCategories(SPACES_STORE, ContentModel.ASPECT_GEN_CLASSIFIABLE);
            
            for (ChildAssociationRef childAssoc : childAssocs)
            {
                if (nodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_NAME).equals("CatA"))
                {
                    return; // re-runnable, else we need to delete the created categories
                }
            }
        }
        
        // Find all root categories
        String query = "PATH:\"/cm:generalclassifiable/*\"";
        ResultSet resultSet = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);
        int cnt = resultSet.length();
        
        NodeRef catA = createCategory(SPACES_STORE, null, "CatA", "CatA-"+tenantDomain);
        createCategory(SPACES_STORE, catA, "SubCatA", "SubCatA-"+tenantDomain); // ignore return
        
        NodeRef catB = createCategory(SPACES_STORE, null, "CatB", "CatB-"+tenantDomain);
        createCategory(SPACES_STORE, catB, "SubCatB", "SubCatB-"+tenantDomain); // ignore return
        
        cnt = cnt + 2;
        
        if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
        {
            NodeRef catC = createCategory(SPACES_STORE, null, "CatC", "CatC-"+tenantDomain);
            createCategory(SPACES_STORE, catC, "SubCatC", "SubCatC-"+tenantDomain); // ignore return
            
            cnt = cnt + 1;
        }
        resultSet.close();
        
        // Find all root categories
        resultSet = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);
        assertEquals(cnt, resultSet.length());
        resultSet.close();
        
        String queryMembers = "PATH:\"/cm:generalclassifiable//cm:catA/member\"";
        resultSet = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, queryMembers);
        assertEquals(0, resultSet.length());
        resultSet.close();
        
        NodeRef homeSpaceRef = getHomeSpaceFolderNode(AuthenticationUtil.getRunAsUser());
        NodeRef contentRef = addContent(homeSpaceRef, "tqbfjotld.txt", "The quick brown fox jumps over the lazy dog (tenant " + tenantDomain + ")", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        assertFalse(nodeService.hasAspect(contentRef, ContentModel.ASPECT_GEN_CLASSIFIABLE));
        
        List<NodeRef> categories = (List<NodeRef>)nodeService.getProperty(contentRef, ContentModel.PROP_CATEGORIES);
        assertNull(categories);
        
        // Classify the node (ie. assign node to a particular category in a classification)
        categories = new ArrayList<NodeRef>(1);
        categories.add(catA);
        
        HashMap<QName, Serializable> catProps = new HashMap<QName, Serializable>();
        catProps.put(ContentModel.PROP_CATEGORIES, (Serializable)categories);
        nodeService.addAspect(contentRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, catProps);
        
        assertTrue(nodeService.hasAspect(contentRef, ContentModel.ASPECT_GEN_CLASSIFIABLE));
        
        categories = (List<NodeRef>)nodeService.getProperty(contentRef, ContentModel.PROP_CATEGORIES);
        assertEquals(1, categories.size());
        
        // test ETHREEOH-210
        queryMembers = "PATH:\"/cm:generalclassifiable//cm:CatA/member\"";
        resultSet = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, queryMembers);
        assertEquals(1, resultSet.length());
        resultSet.close();
    }
    
    public void test11CreateFolders()
    {
        logger.info("Create demo folders");
        
        List<String> users = new ArrayList<String>(3);
        users.add(TEST_USER1);
        users.add(TEST_USER2);
        users.add(TEST_USER3);
        
        for (final String tenantDomain : tenants)
        {
            for (String baseUserName : users)
            {
                if ((! baseUserName.equals(TEST_USER3)) || (tenantDomain.equals(TEST_TENANT_DOMAIN2)))
                {
                    final String tenantUserName = tenantService.getDomainUser(baseUserName, tenantDomain);
                    
                    TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            NodeRef homeSpaceRef = getHomeSpaceFolderNode(tenantUserName);
                            
                            NodeRef folderRef = createFolderNode(homeSpaceRef, "myfolder1");
                            createFolderNode(folderRef, "mysubfolder1"); // ignore return
                            
                            folderRef = createFolderNode(homeSpaceRef, "myfolder2"); 
                            createFolderNode(folderRef, "mysubfolder2"); // ignore return
                            
                            if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                            {
                                folderRef = createFolderNode(homeSpaceRef, "myfolder3"); 
                                createFolderNode(folderRef, "mysubfolder3"); // ignore return
                            }
                            
                            return null;
                        }
                    }, tenantUserName, tenantDomain);
                }
            }
        }
    }
    
    public void test12CreateVersionableUserContent()
    {
        logger.info("Create demo content");
        
        List<String> users = new ArrayList<String>(3);
        users.add(TEST_USER1);
        users.add(TEST_USER2);
        users.add(TEST_USER3);
        
        for (final String tenantDomain : tenants)
        {
            for (String baseUserName : users)
            {
                if ((! baseUserName.equals(TEST_USER3)) || (tenantDomain.equals(TEST_TENANT_DOMAIN2)))
                {
                    final String tenantUserName = tenantService.getDomainUser(baseUserName, tenantDomain);
                    
                    TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            NodeRef homeSpaceRef = getHomeSpaceFolderNode(tenantUserName);
                            
                            NodeRef contentRef = addContent(homeSpaceRef, tenantUserName+" quick brown fox.txt", "The quick brown fox jumps over the lazy dog (tenant " + tenantDomain + ")", MimetypeMap.MIMETYPE_TEXT_PLAIN);
                            nodeService.addAspect(contentRef, ContentModel.ASPECT_VERSIONABLE, null);
                            
                            if (tenantDomain.equals(TEST_TENANT_DOMAIN2))
                            {
                                contentRef = addContent(homeSpaceRef, tenantUserName+" quick brown fox ANO.txt", "The quick brown fox jumps over the lazy dog ANO (tenant " + tenantDomain + ")", MimetypeMap.MIMETYPE_TEXT_PLAIN);
                                nodeService.addAspect(contentRef, ContentModel.ASPECT_VERSIONABLE, null);
                            }
                            
                            return null;
                        }
                    }, tenantUserName, tenantDomain);
                }
            }
        }
    }
    
    public void test13GetStores()
    {
        logger.info("Get tenant stores");
        
        // system
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                for (StoreRef storeRef : nodeService.getStores())
                {
                    System.out.println("StoreRef: "+storeRef);
                }
                
                assertTrue("System: "+nodeService.getStores().size()+", "+(tenants.size()+1), (nodeService.getStores().size() >= (DEFAULT_STORE_COUNT * (tenants.size()+1))));
                return null;
            }
        }, TenantService.DEFAULT_DOMAIN);
        
        // super admin
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                assertTrue("Super admin: "+nodeService.getStores().size(), (nodeService.getStores().size() >= DEFAULT_STORE_COUNT));
                return null;
            }
        }, AuthenticationUtil.getAdminUserName(), TenantService.DEFAULT_DOMAIN);
        
        for (final String tenantDomain : tenants)
        {
            String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    Set<StoreRef> stores = new HashSet<StoreRef>(nodeService.getStores());
                    assertEquals("Tenant: "+tenantDomain, DEFAULT_STORES, stores);
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain);
        }
    }
    
    public void test14GetProperty()
    {
        logger.info("Test get property");
        
        for (final String tenantDomain : tenants)
        {
            String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    NodeRef personNodeRef = createUser(TEST_USER4, tenantDomain, TEST_USER4+" "+tenantDomain);
                    
                    // Test nodeRef property
                    NodeRef homeFolderNodeRef = (NodeRef)nodeService.getProperty(personNodeRef, ContentModel.PROP_HOMEFOLDER);
                    assertFalse(homeFolderNodeRef.toString().contains(tenantDomain));
                    
                    Map<QName, Serializable> props = (Map<QName, Serializable>)nodeService.getProperties(personNodeRef);
                    assertFalse(props.get(ContentModel.PROP_HOMEFOLDER).toString().contains(tenantDomain));
                    
                    // Test "store-identifier" property
                    String storeId = (String)nodeService.getProperty(personNodeRef, ContentModel.PROP_STORE_IDENTIFIER);
                    assertFalse(storeId.contains(tenantDomain));
                    
                    assertFalse(props.get(ContentModel.PROP_STORE_IDENTIFIER).toString().contains(tenantDomain));
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain);
        }
    }
    
    public void test15COCIandSearch()
    {
        logger.info("Test checkout/checkin and search");
        
        for (final String tenantDomain : tenants)
        {
            final String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    // search for local copy of bootstrapped file 'invite_user_email.ftl' (see email_templates.acp)
                    String origText = "You have been invited to";
                    String query = "+PATH:\"/app:company_home/app:dictionary/app:email_templates/app:invite_email_templates/*\" +TEXT:\""+origText+"\"";
                    ResultSet resultSet = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);
                    assertEquals(1, resultSet.length());
                    
                    NodeRef nodeRef = resultSet.getNodeRef(0);
                    resultSet.close();
                    
                    // checkout, update and checkin
                    
                    NodeRef workingCopyNodeRef = cociService.checkout(nodeRef);
                    
                    ContentWriter writer = contentService.getWriter(workingCopyNodeRef, ContentModel.PROP_CONTENT, true);
                    
                    writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                    writer.setEncoding("UTF-8");
                    
                    String updateText = "Updated by "+tenantAdminName;
                    writer.putContent(updateText);
                    
                    cociService.checkin(workingCopyNodeRef, null);
                    
                    query = "+PATH:\"/app:company_home/app:dictionary/app:email_templates/app:invite_email_templates/*\" +TEXT:\""+origText+"\"";
                    resultSet = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);
                    assertEquals(0, resultSet.length());
                    resultSet.close();
                    
                    query = "+PATH:\"/app:company_home/app:dictionary/app:email_templates/app:invite_email_templates/*\" +TEXT:\""+updateText+"\"";
                    resultSet = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, query);
                    assertEquals(1, resultSet.length());
                    resultSet.close();
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain);
        }
    }
    
    public void test16DeleteArchiveAndRestoreContent()
    {
        logger.info("test delete/archive & restore content");
        
        // note: CLOUD-1349 - ownership is based on fully authenticated user (else restoreNode fails for non-Admin user)
        AuthenticationUtil.clearCurrentSecurityContext();
        
        final String superAdmin = AuthenticationUtil.getAdminUserName();
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                // super tenant - admin user
                deleteArchiveAndRestoreContent(superAdmin, TenantService.DEFAULT_DOMAIN);
                return null;
            }
            
        }, superAdmin);
        
        final String superAnoUser = "superAnoUser";
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                createUser(superAnoUser, TenantService.DEFAULT_DOMAIN, superAnoUser);
                return null;
            }
        }, superAdmin);
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                // super tenant - ano user
                deleteArchiveAndRestoreContent(superAnoUser, TenantService.DEFAULT_DOMAIN);
                
                return null;
            }
        }, superAnoUser);
        
        for (final String tenantDomain : tenants)
        {
            final String tenantUserName = tenantService.getDomainUser(TEST_USER1, tenantDomain);
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    deleteArchiveAndRestoreContent(tenantUserName, tenantDomain);
                    
                    return null;
                }
            }, tenantUserName, tenantDomain);
        }
    }
    
    private void deleteArchiveAndRestoreContent(String userName, String tenantDomain)
    {
        NodeRef homeSpaceRef = getHomeSpaceFolderNode(userName);
        NodeRef contentRef = addContent(homeSpaceRef, userName+" "+System.currentTimeMillis()+" tqbfjotld.txt", "The quick brown fox jumps over the lazy dog (tenant " + tenantDomain + ")", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        
        assertEquals(userName, ownableService.getOwner(contentRef));
        permissionService.hasPermission(contentRef, PermissionService.DELETE_NODE);
        
        NodeRef storeArchiveNode = nodeService.getStoreArchiveNode(contentRef.getStoreRef());
        
        nodeService.deleteNode(contentRef);
        
        // deduce archived nodeRef
        StoreRef archiveStoreRef = storeArchiveNode.getStoreRef();
        NodeRef archivedContentRef = new NodeRef(archiveStoreRef, contentRef.getId());
        
        assertEquals(userName, ownableService.getOwner(archivedContentRef));
        permissionService.hasPermission(archivedContentRef, PermissionService.DELETE_NODE);
        
        //nodeService.restoreNode(archivedContentRef, null, null, null);
        RestoreNodeReport report = nodeArchiveService.restoreArchivedNode(archivedContentRef);
        assertNotNull(report);
        
        NodeRef restoredNodeRef = report.getRestoredNodeRef();
        assertNotNull(restoredNodeRef);
        
        archivedContentRef = new NodeRef(archiveStoreRef, restoredNodeRef.getId());
        
        nodeService.deleteNode(restoredNodeRef);
        
        nodeArchiveService.purgeArchivedNode(archivedContentRef);
    }
    
    public void test17CustomModels()
    {
        logger.info("test custom models");
        
        final int defaultModelCnt = dictionaryService.getAllModels().size();
        
        for (final String tenantDomain : tenants)
        {
            final String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    // no custom models should be deployed yet (note: will fail if re-run)
                    assertEquals(0, repoAdminService.getModels().size());
                    assertEquals(defaultModelCnt, dictionaryService.getAllModels().size());
                    assertNull(dictionaryService.getClass(QName.createQName("{my.new.model}sop")));
                    
                    // deploy custom model
                    InputStream modelStream = getClass().getClassLoader().getResourceAsStream("tenant/exampleModel.xml");
                    repoAdminService.deployModel(modelStream, "exampleModel.xml");
                    
                    assertEquals(1, repoAdminService.getModels().size());
                    assertEquals(defaultModelCnt+1, dictionaryService.getAllModels().size());
                    
                    ClassDefinition myType = dictionaryService.getClass(QName.createQName("{my.new.model}sop"));
                    assertNotNull(myType);
                    assertEquals(QName.createQName("{my.new.model}mynewmodel"),myType.getModel().getName());
                    
                    // deactivate model
                    repoAdminService.deactivateModel("exampleModel.xml");
                    
                    assertEquals(1, repoAdminService.getModels().size()); // still deployed, although not active
                    assertEquals(defaultModelCnt, dictionaryService.getAllModels().size());
                    assertNull(dictionaryService.getClass(QName.createQName("{my.new.model}sop")));
                    
                    // re-activate model
                    repoAdminService.activateModel("exampleModel.xml");
                    
                    assertEquals(1, repoAdminService.getModels().size());
                    assertEquals(defaultModelCnt+1, dictionaryService.getAllModels().size());
                    
                    myType = dictionaryService.getClass(QName.createQName("{my.new.model}sop"));
                    assertNotNull(myType);
                    assertEquals(QName.createQName("{my.new.model}mynewmodel"),myType.getModel().getName());
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain);
        }
    }
    
    public void test18AddCustomWebClient()
    {
        // note: add as demo files - need to re-start Alfresco to see custom web client config / messages 
        logger.info("test add custom web client config");
        
        for (final String tenantDomain : tenants)
        {    
            final String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain);
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    NodeRef webClientExtFolder = getWebClientExtensionNodeRef(SPACES_STORE);
                    
                    InputStream is = getClass().getClassLoader().getResourceAsStream("tenant/webclient.properties");
                    addContent(webClientExtFolder, "webclient.properties", is, MimetypeMap.MIMETYPE_TEXT_PLAIN);
                    
                    is = getClass().getClassLoader().getResourceAsStream("tenant/web-client-config-custom.xml");
                    addContent(webClientExtFolder, "web-client-config-custom.xml", is, MimetypeMap.MIMETYPE_XML);
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain);
        }
    }
    
    public void test19FileFolder()
    {
        logger.info("test file/folder list");
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        for (final String tenantDomain : tenants)
        {
            final String tenantUserName = tenantService.getDomainUser(TEST_USER1, tenantDomain);
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    List<String> pathElements = new ArrayList<String>(2);
                    pathElements.add("Data Dictionary");
                    pathElements.add("Presentation Templates");
                    
                    NodeRef chRef = repositoryHelper.getCompanyHome();
                    NodeRef ddRef = fileFolderService.resolveNamePath(chRef, pathElements).getNodeRef();
                    
                    assertTrue(fileFolderService.list(ddRef).size() > 0);
                    
                    return null;
                }
            }, tenantUserName, tenantDomain);
        }
    }
    
    public void test20_ALF_12732()
    {
        final String tenantDomain1 = TEST_RUN+".one.alf12732";
        
        createTenant(tenantDomain1);
        
        String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain1);
        
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(tenantAdminName); // note: since SiteServiceImpl.setupSitePermissions currently uses getCurrentUserName (rather than runAs)
            
            TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    createSite("site1");
                    
                    NodeRef docLib1Ref = siteService.getContainer("site1", SiteService.DOCUMENT_LIBRARY);
                    NodeRef contentRef = addContent(docLib1Ref, "tqbfjotld.txt", "The quick brown fox jumps over the lazy dog", MimetypeMap.MIMETYPE_TEXT_PLAIN);
                    
                    createSite("site2");
                    
                    NodeRef docLib2Ref = siteService.getContainer("site2", SiteService.DOCUMENT_LIBRARY);
                    
                    nodeService.moveNode(contentRef, docLib2Ref, ContentModel.ASSOC_CONTAINS, QName.createQName("tqbfjotld.txt"));
                    
                    // for Share, called via "move-to.post.json.js" -> ScriptSiteService.cleanSitePermissions
                    siteService.cleanSitePermissions(contentRef, null);
                    
                    return null;
                }
            }, tenantAdminName, tenantDomain1);
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }
    
    public void test21_ALF_14354()
    {
        final String tenantDomain1 = TEST_RUN+".one.alf14354";
        final String tenantDomain2 = TEST_RUN+".two.alf14354";
        
        createTenant(tenantDomain1);
        createTenant(tenantDomain2);
        
        String tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain1);
        
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                NodeRef rootNodeRef = repositoryHelper.getRootHome();
                
                assertTrue(nodeService.exists(rootNodeRef));
                
                return null;
            }
        }, tenantAdminName, tenantDomain1);
        
        tenantAdminName = tenantService.getDomainUser(AuthenticationUtil.getAdminUserName(), tenantDomain2);
        
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                NodeRef rootNodeRef = repositoryHelper.getRootHome();
                
                assertTrue(nodeService.exists(rootNodeRef));
                
                return null;
            }
        }, tenantAdminName, tenantDomain2);
    }
    

    private int searchForDataDictionary(String tenantAdminName, final String query)
    {
        return AuthenticationUtil.runAs(new RunAsWork<Integer>()
        {
            public Integer doWork() throws Exception
            {
                ResultSet resultSet = searchService.query(SPACES_STORE, SearchService.LANGUAGE_LUCENE, query, null);
                return resultSet.length();
            }
        }, tenantAdminName);
    }
    
    // pseudo cleanup - if this test runs last
    public void test22DeleteAllTenants()
    {
        logger.info("test delete tenants");
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        List<Tenant> allTenants = tenantAdminService.getAllTenants();
        for (final Tenant tenant : allTenants)
        {    
            deleteTenant(tenant.getTenantDomain());
        }
    }
    
    /*
    public static void main(String args[]) 
    {
        System.out.println(new Date());
        junit.textui.TestRunner.run(MultiTDemoTest.class);
        System.out.println(new Date());
    }
    */
}
