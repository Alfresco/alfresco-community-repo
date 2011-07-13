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
package org.alfresco.repo.security.person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.Tenant;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.PropertyMap;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Integration test for HomeFolderProviderSynchronizer.
 * 
 * @author Alan Davis
 */
public class HomeFolderProviderSynchronizerTest
{
    private static final QName PROP_PARENT_PATH = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "parentPath");

    private static ApplicationContext applicationContext;
    private static ServiceRegistry serviceRegistry;
    private static TransactionService transactionService;
    private static FileFolderService fileFolderService;
    private static PersonService personService;
    private static NodeService nodeService;
    private static ContentService contentService;
    private static AuthorityService authorityService;
    private static TenantAdminService tenantAdminService;
    private static TenantService tenantService;
    private static HomeFolderManager homeFolderManager;
    private static Properties properties;
    private static RegexHomeFolderProvider largeHomeFolderProvider;
    private static String largeHomeFolderProviderName;
    private static RegexHomeFolderProvider testHomeFolderProvider;
    private static String testHomeFolderProviderName;
    private static String storeUrl;
    private static String origRootPath;
    private static NodeRef rootNodeRef;
    private static HomeFolderProviderSynchronizer homeFolderProviderSynchronizer;
    private static boolean firstTest = true;

    private UserTransaction trans;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        applicationContext = ApplicationContextHelper.getApplicationContext();
        serviceRegistry = (ServiceRegistry) applicationContext.getBean("ServiceRegistry");
        transactionService = (TransactionService) applicationContext.getBean("transactionService");
        fileFolderService = (FileFolderService) applicationContext.getBean("fileFolderService");
        personService = (PersonService) applicationContext.getBean("personService");
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        contentService = (ContentService) applicationContext.getBean("contentService");
        authorityService = (AuthorityService) applicationContext.getBean("authorityService");
        tenantAdminService = (TenantAdminService) applicationContext.getBean("tenantAdminService");
        tenantService = (TenantService) applicationContext.getBean("tenantService");
        homeFolderManager = (HomeFolderManager) applicationContext.getBean("homeFolderManager");
        largeHomeFolderProvider = (RegexHomeFolderProvider) applicationContext.getBean("largeHomeFolderProvider");
        largeHomeFolderProviderName = largeHomeFolderProvider.getName();
        storeUrl = largeHomeFolderProvider.getStoreUrl();
        origRootPath = largeHomeFolderProvider.getRootPath();
        properties = (Properties) applicationContext.getBean("global-properties");

        personService.setCreateMissingPeople(true);

        // Create test home folder provider that gets its path from a property and the username
        testHomeFolderProvider = new RegexHomeFolderProvider()
        {
            @Override
            public List<String> getHomeFolderPath(NodeRef person)
            {
                String parentPath = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(person, PROP_PARENT_PATH));
                String propPath = ((parentPath == null || parentPath.length() == 0) ? "" : parentPath+'/')+
                                  homeFolderManager.getPersonProperty(person, ContentModel.PROP_USERNAME);
                return Arrays.asList(propPath.split("/"));
            }
        };
        testHomeFolderProvider.setPropertyName(ContentModel.PROP_USERNAME.getLocalName());
        testHomeFolderProvider.setPattern("(..)");
        testHomeFolderProvider.setBeanName("testHomeFolderProvider");
        testHomeFolderProvider.setHomeFolderManager(homeFolderManager);
        testHomeFolderProvider.setRootPath(origRootPath);
        testHomeFolderProvider.setStoreUrl(storeUrl);
        testHomeFolderProvider.setOnCreatePermissionsManager((PermissionsManager)applicationContext.getBean("defaultOnCreatePermissionsManager"));
        testHomeFolderProvider.setOnCreatePermissionsManager((PermissionsManager)applicationContext.getBean("defaultOnCreatePermissionsManager"));
        testHomeFolderProvider.setOnReferencePermissionsManager((PermissionsManager)applicationContext.getBean("defaultOnReferencePermissionsManager"));
        testHomeFolderProviderName = testHomeFolderProvider.getName();
        homeFolderManager.addProvider(testHomeFolderProvider);

        homeFolderProviderSynchronizer = new HomeFolderProviderSynchronizer(
                properties, transactionService, authorityService,
                personService, fileFolderService, nodeService,
                homeFolderManager, tenantAdminService);
    }
    
    @Before
    public void setUp() throws Exception
    {
        properties.setProperty("home_folder_provider_synchronizer.enabled", "true");
        properties.remove("home_folder_provider_synchronizer.override_provider");
        properties.remove("home_folder_provider_synchronizer.keep_empty_parents");

        largeHomeFolderProvider.setPattern("^(..)");
        testHomeFolderProvider.setRootPath(origRootPath);
        largeHomeFolderProvider.setRootPath(origRootPath);

        // Just in case we killed a test last time - tidy up
        if (firstTest)
        {
            firstTest = false;

            AuthenticationUtil.setRunAsUserSystem();
            trans = transactionService.getUserTransaction();
            trans.begin();
            rootNodeRef = homeFolderManager.getRootPathNodeRef(largeHomeFolderProvider);
            trans.commit();
            trans = null;

            tearDown();
        }

        AuthenticationUtil.setRunAsUserSystem();
        trans = transactionService.getUserTransaction();
        trans.begin();
        // System.out.println(NodeStoreInspector.dumpNode(nodeService, rootNodeRef));
    }

    @After
    public void tearDown() throws Exception
    {
        if (trans != null)
        {
            try
            {
                trans.commit();
            }
            catch (Exception e)
            {
                if ((trans.getStatus() == Status.STATUS_ACTIVE) ||
                    (trans.getStatus() == Status.STATUS_MARKED_ROLLBACK))
                {
                    trans.rollback();
                    trans = null;
                }
            }
        }

        trans = transactionService.getUserTransaction();
        trans.begin();
        Set<NodeRef> adminGuestUserHomeFolders = deleteNonAdminGuestUsers();
        deleteNonAdminGuestFolders(adminGuestUserHomeFolders);
        deleteAllTenants();
        trans.commit();
        trans = null;
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private Set<NodeRef> deleteNonAdminGuestUsers()
    {
        final Set<NodeRef> adminGuestUserHomeFolders = new HashSet<NodeRef>();
        for (final NodeRef nodeRef : personService.getAllPeople())
        {
            final String username = DefaultTypeConverter.INSTANCE.convert(String.class,
                    nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME));
            final String domainUsername = tenantService.getBaseNameUser(username);
            String tenantDomain = tenantService.getUserDomain(username);
            String systemUser = tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
            boolean disabled = !TenantService.DEFAULT_DOMAIN.equals(tenantDomain) &&
                               !tenantAdminService.isEnabledTenant(tenantDomain);
            try
            {
                if (disabled)
                {
                    tenantAdminService.enableTenant(tenantDomain);
                }
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                    public Object doWork() throws Exception
                    {
                        deleteUser(adminGuestUserHomeFolders, nodeRef, username, domainUsername);
                        return null;
                    }
                }, systemUser);
            }
            finally
            {
                if (disabled)
                {
                    tenantAdminService.disableTenant(tenantDomain);
                }
            }
        }
        return adminGuestUserHomeFolders;
    }

    // Delete users other than admin and guest. The home folders of
    // admin and guest are added to internalUserHomeFolders.
    private void deleteUser(final Set<NodeRef> adminGuestUserHomeFolders,
            NodeRef person, String username, String domainUsername)
    {
        if (!domainUsername.equals("admin") && !domainUsername.equals("guest"))
        {
            personService.deletePerson(person);
            System.out.println("deleted user "+username);
        }
        else
        {
            NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(
                    NodeRef.class, nodeService.getProperty(person,
                    ContentModel.PROP_HOMEFOLDER));
            adminGuestUserHomeFolders.add(homeFolder);
        }
    }

    private void deleteNonAdminGuestFolders(final Set<NodeRef> adminGuestUserHomeFolders)
    {
        // Delete folders from under the home folder root path in case they have been left over
        // from another test. Admin and Guest home folder should not be under here, but lets
        // double check.
        for (ChildAssociationRef childAssocs: nodeService.getChildAssocs(rootNodeRef))
        {
            NodeRef nodeRef = childAssocs.getChildRef();
            if (!adminGuestUserHomeFolders.contains(nodeRef))
            {
                System.out.println("TearDown remove '"+childAssocs.getQName().getLocalName()+
                        "' from under the home folder root.");
                nodeService.deleteNode(nodeRef);
            }
        }
    }
    
    private NodeRef createUser(String parentPath, String username) throws Exception
    {
        return createUser(TenantService.DEFAULT_DOMAIN, parentPath, username);
    }
        
    private NodeRef createUser(String parentPath,
            String username, String homeFolderProviderName, boolean createHomeDirectory) throws Exception
    {
        return createUser(TenantService.DEFAULT_DOMAIN, parentPath, username,
                homeFolderProviderName, createHomeDirectory);
    }

    private NodeRef createUser(String tenantDomain, String parentPath, String username) throws Exception
    {
        return createUser(tenantDomain, parentPath, username, largeHomeFolderProviderName, true);
    }
    
    private NodeRef createUser(String tenantDomain, final String parentPath,
            final String username, final String homeFolderProviderName,
            final boolean createHomeDirectory) throws Exception
    {
        final String domainUsername = tenantService.getDomainUser(username, tenantDomain);
        String systemUser = tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
        return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                String firstName = username;
                String lastName = "Smith";
                String emailAddress = String.format("%s.%s@xyz.com", firstName,
                        lastName);
                PropertyMap properties = new PropertyMap();
                properties.put(ContentModel.PROP_USERNAME, domainUsername);
                properties.put(ContentModel.PROP_FIRSTNAME, firstName);
                properties.put(ContentModel.PROP_LASTNAME, lastName);
                properties.put(ContentModel.PROP_EMAIL, emailAddress);
                properties.put(ContentModel.PROP_HOME_FOLDER_PROVIDER, testHomeFolderProviderName);
                properties.put(PROP_PARENT_PATH, parentPath);
                homeFolderManager.setEnableHomeFolderCreationAsPeopleAreCreated(createHomeDirectory);
                NodeRef person = personService.createPerson(properties);
                assertNotNull("The person nodeRef for "+domainUsername+" should have been created", person);
                NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(
                        NodeRef.class, nodeService.getProperty(person,
                        ContentModel.PROP_HOMEFOLDER));
                if (createHomeDirectory)
                {
                    assertNotNull("The homeFolder for "+domainUsername+" should have been created", homeFolder);
                }
                else
                {
                    assertNull("The homeFolder for "+domainUsername+" should NOT have been created", homeFolder);
                }

                if (!testHomeFolderProviderName.equals(homeFolderProviderName))
                {
                    if (homeFolderProviderName == null)
                    {
                        nodeService.removeProperty(person, ContentModel.PROP_HOME_FOLDER_PROVIDER);
                    }
                    else
                    {
                        nodeService.setProperty(person, ContentModel.PROP_HOME_FOLDER_PROVIDER,
                                    homeFolderProviderName);
                    }
                }
                return person;
            }
        }, systemUser);
    }
    
    private NodeRef createFolder(String path) throws Exception
    {
        NodeRef parent = rootNodeRef;
        if (path.length() > 0)
        {
            StringBuilder currentPath = new StringBuilder();
            for (String pathElement: path.split("/"))
            {
                if (currentPath.length() > 0)
                {
                    currentPath.append("/");
                }
                currentPath.append(pathElement);
                NodeRef nodeRef = nodeService.getChildByName(parent,
                        ContentModel.ASSOC_CONTAINS, pathElement);
                if (nodeRef == null)
                {
                    parent = fileFolderService.create(parent, pathElement,
                            ContentModel.TYPE_FOLDER).getNodeRef();
                }
                else
                {
                    assertTrue("Expected "+currentPath+" to be a folder",
                            fileFolderService.getFileInfo(nodeRef).isFolder());
                    parent = nodeRef;
                }
            }
        }
        return parent;
    }
    
    private NodeRef createContent(String parentPath, String name) throws Exception
    {
        NodeRef parent = createFolder(parentPath);
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put(ContentModel.PROP_CONTENT, new ContentData(null, "text/plain",
                0L, "UTF-16", Locale.ENGLISH));
        propertyMap.put(ContentModel.PROP_NAME, name);
        NodeRef content = nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                propertyMap).getChildRef();
        ContentWriter writer = contentService.getWriter(content, ContentModel.TYPE_CONTENT, true);
        writer.putContent("The cat sat on the mat.");
        
        // System.out.println(NodeStoreInspector.dumpNode(nodeService, rootNodeRef));
        return content;
    }
    
    private String toPath(NodeRef root, NodeRef homeFolder)
    {
        if (root == null || homeFolder == null)
        {
            return null;
        }
        
        Path rootPath = nodeService.getPath(root);
        Path homeFolderPath = nodeService.getPath(homeFolder);
        int rootSize = rootPath.size();
        int homeFolderSize = homeFolderPath.size();
        if (rootSize >= homeFolderSize)
        {
            return null;
        }
        
        StringBuilder sb = new StringBuilder("");

        // Check homeFolder is under root
        for (int i=0; i < rootSize; i++)
        {
            if (!rootPath.get(i).equals(homeFolderPath.get(i)))
            {
                return null;
            }
        }
        
        // Build up path of sub folders
        for (int i = rootSize; i < homeFolderSize; i++)
        {
            Path.Element element = homeFolderPath.get(i);
            if (!(element instanceof Path.ChildAssocElement))
            {
                return null;
            }
            QName folderQName = ((Path.ChildAssocElement) element).getRef().getQName();
            if (sb.length() > 0)
            {
                sb.append('/');
            }
            sb.append(folderQName.getLocalName());
        }
        return sb.toString();
    }
    
    private void createTenant(final String tenantDomain)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                if (!tenantAdminService.existsTenant(tenantDomain))
                {
                    tenantAdminService.createTenant(tenantDomain,
                            ("admin "+tenantDomain).toCharArray(), null);
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private void deleteAllTenants() throws Exception
    {
        List<Tenant> tenants = tenantAdminService.getAllTenants();
        for (Tenant tenant : tenants)
        {
            deleteTenant(tenant.getTenantDomain());
        }

    }

    // DbNodeServiceImpl does not support deleteStore() at the moment,
    // even though it supports createStore(), so just disable them for now.
    private void deleteTenant(final String tenantDomain) throws Exception
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                if (tenantAdminService.existsTenant(tenantDomain))
                {
                    // Can't delete so disable
                    // tenantAdminService.deleteTenant(tenantDomain);

                    if (tenantAdminService.isEnabledTenant(tenantDomain))
                    {
                        tenantAdminService.disableTenant(tenantDomain);
                    }
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private void assertHomeFolderLocation(String username, String expectedPath) throws Exception
    {
        assertHomeFolderLocation(TenantService.DEFAULT_DOMAIN, username, expectedPath);
    }
    
    private void assertHomeFolderLocation(String tenantDomain, final String username,
            final String expectedPath) throws Exception
    {
        try
        {
            final String domainUsername = tenantService.getDomainUser(username, tenantDomain);
            String systemUser = tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public NodeRef doWork() throws Exception
                {
                    NodeRef person = personService.getPerson(domainUsername, false);
                    NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class,
                            nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
                    assertNotNull("User: "+domainUsername+" home folder should exist", homeFolder);

                    NodeRef rootPath = homeFolderManager.getRootPathNodeRef(largeHomeFolderProvider);
                    String actualPath = toPath(rootPath, homeFolder);
                    assertEquals("User: "+domainUsername+" home folder location", expectedPath, actualPath);
                    return null;
                }
            }, systemUser);
        }
        catch (RuntimeException e)
        {
            final Throwable cause = e.getCause();
            if (cause instanceof ComparisonFailure || cause instanceof AssertionError)
            {
                throw (ComparisonFailure)cause;
            }
            else
            {
                throw e;
            }
        }
    }
    
    private boolean exists(String path) throws Exception
    {
        NodeRef parent = rootNodeRef;
        boolean exists = true;
        for (String pathElement: path.split("/"))
        {
            NodeRef nodeRef = nodeService.getChildByName(parent,
                    ContentModel.ASSOC_CONTAINS, pathElement);
            if (nodeRef == null)
            {
                exists = false;
                break;
            }
            else
            {
                parent = nodeRef;
            }
        }
        return exists;
    }

    private void moveUserHomeFolders() throws Exception
    {
        trans.commit();
        trans = null;
        
        homeFolderProviderSynchronizer.onBootstrap(null);
        
        trans = transactionService.getUserTransaction();
        trans.begin();
    }
    
    @Test
    public void testCorrectLocation() throws Exception
    {
        createUser("te", "tess");
        
        moveUserHomeFolders();

        assertHomeFolderLocation("tess", "te/tess");
    }

    @Test
    public void testCreateParentFolder() throws Exception
    {
        createUser("", "fred");
        
        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "fr/fred");
    }

    @Test
    public void testNotEnabled() throws Exception
    {
        createUser("", "fred");
        properties.remove("home_folder_provider_synchronizer.enabled");
        
        moveUserHomeFolders();

        // If performed, the home folder will have been moved to fr/fred
        // We must force the creation of the home folder as it will not
        // have been done
        personService.getPerson("fred");
        assertHomeFolderLocation("fred", "fred");
    }

    @Test
    public void testHomeFolderNotYetCreated() throws Exception
    {
        NodeRef person = createUser("", "fred", largeHomeFolderProviderName, false);
        
        moveUserHomeFolders();

        NodeRef homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class,
                nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
        assertNull("The homeFolder should NOT have been created", homeFolder);
        
        person = personService.getPerson("fred");
        homeFolder = DefaultTypeConverter.INSTANCE.convert(NodeRef.class,
                nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER));
        assertNotNull("The homeFolder should have been created", homeFolder);
    }

    @Test
    public void testCreateMultipleParentFolders() throws Exception
    {
        largeHomeFolderProvider.setPattern("^(.?)(.?)(.?)(.?)(.?)");

        createUser("", "fred");
        createUser("", "peter");
        createUser("", "tess");

        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "f/r/e/d/fred");
        assertHomeFolderLocation("peter", "p/e/t/e/r/peter");
        assertHomeFolderLocation("tess", "t/e/s/s/tess");
    }

    @Test
    public void testMoveToRoot() throws Exception
    {
        // i.e. there are no parent folders after the sync
        largeHomeFolderProvider.setPattern("");
            
        createUser("fr", "fred");

        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "fred");
    }

    @Test
    public void testRemoveEmptyParents() throws Exception
    {
        createUser("a/bb/ccc", "peter");
        
        moveUserHomeFolders();

        assertHomeFolderLocation("peter", "pe/peter");
        assertFalse("Expected the empty parent 'a' to have been removed.", exists("a"));
    }

    @Test
    public void testKeepEmptyParents() throws Exception
    {
        createUser("a/bb/ccc", "peter");
        properties.put("home_folder_provider_synchronizer.keep_empty_parents", "true");

        moveUserHomeFolders();

        assertHomeFolderLocation("peter", "pe/peter");
        assertTrue("Expected the empty parent 'a/bb/ccc' to still exist as global " +
        		"property was set.", exists("a/bb/ccc"));
    }

    @Test
    public void testKeepNonEmptyParents() throws Exception
    {
        createUser("a/bb/ccc", "peter");
        createFolder("a/bb/ddd");
        
        moveUserHomeFolders();

        assertHomeFolderLocation("peter", "pe/peter");
        assertFalse("Expected the empty parent 'a/bb/ccc' to have been removed.", exists("a/bb/ccc"));
        assertTrue("Expected the non empty parent 'a/bb' to have been kept.", exists("a/bb/ddd"));
    }

    @Test
    public void testPathAlreadyInUseByFolder() throws Exception
    {
        createUser("", "fred");
        createFolder("fr");
        
        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "fr/fred");
    }

    @Test
    public void testPathAlreadyInUseByContent() throws Exception
    {
        createUser("", "fred");
        createContent("", "fr");
        
        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "fred"); // unchanged
        assertFalse("Did not expect there to be a folder in the prefered location.", exists("fr/fred"));
        assertTrue("Expected the content to still exist.", exists("fr"));
    }

    @Test
    public void testPathInUseByUser() throws Exception
    {
        // i.e. test clash between home folder names and parent folders
        //      which requires a temporary folder to be created
        createUser("", "fr");
        createUser("", "fred");
        createUser("", "peter");
        createUser("", "pe");
        
        moveUserHomeFolders();

        assertHomeFolderLocation("fr", "fr/fr");
        assertHomeFolderLocation("fred", "fr/fred");
        assertHomeFolderLocation("peter", "pe/peter");
        assertHomeFolderLocation("pe", "pe/pe");
        
        assertFalse("The Temporary1 folder should have been removed", exists("Temporary1"));
    }

    @Test
    public void testUseFirstAvailableTemporaryFolder() throws Exception
    {
        createUser("", "fr");
        createUser("", "fred");
        createFolder("Temporary1");
        createFolder("Temporary2");
        createFolder("Temporary3");
        
        // Don't delete the temporary folder
        properties.put("home_folder_provider_synchronizer.keep_empty_parents", "true");
        
        moveUserHomeFolders();

        assertTrue("The existing Temporary1 folder should still exist", exists("Temporary1"));
        assertTrue("The existing Temporary2 folder should still exist", exists("Temporary2"));
        assertTrue("The existing Temporary3 folder should still exist", exists("Temporary3"));
        assertTrue("The existing Temporary4 folder should still exist", exists("Temporary4"));
    }

    @Test
    public void testException() throws Exception
    {
        // Force the need for a temporary folder
        createUser("", "fr");
        createUser("", "fred");
        
        // Use up all possible temporary folder names
        for (int i=1; i<=100; i++)
        {
            createFolder("Temporary"+i);
        }

        moveUserHomeFolders();

        // normally would have changed to fr/fred if there had not been an exception
        assertHomeFolderLocation("fred", "fred");
    }

    @Test
    public void testMultipleRoots() throws Exception
    {
        createFolder("root");
        String rootPath = origRootPath + "/cm:root";
        testHomeFolderProvider.setRootPath(rootPath);

        createUser("a/b/c", "tess", testHomeFolderProviderName, true);
        createUser("a/b/c", "fred", largeHomeFolderProviderName, true);

        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "fr/fred");
        assertHomeFolderLocation("tess", "root/a/b/c/tess");
   }
    
    @Test
    public void testPathNotUnderRoot() throws Exception
    {
        createUser("a/b/c", "fred");

        createFolder("root");
        String rootPath = origRootPath + "/cm:root";
        largeHomeFolderProvider.setRootPath(rootPath);

        assertHomeFolderLocation("fred", null);

        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "fr/fred");
    }
    
    @Test
    public void testMultipleUsers() throws Exception
    {
        // Tried 2000 users and the HomeFolderProviderSynchronizer.onBootstrap(null)
        // took 33 seconds. The setup and tear down takes a while too.

        // Use a value larger than the batch size of 20 and log every 100.
        int userCount = 110;
        for (int i=1; i<=userCount; i++)
        {
            String name = "f"+i+"red";
            createUser("", name);
        }
        
        moveUserHomeFolders();

        for (int i=1; i<=userCount; i++) 
        {
            String name = "f"+i+"red";
            assertHomeFolderLocation(name, name.substring(0,2)+'/'+name);
        }
    }

    @Test
    public void testOverrideProvider() throws Exception
    {
        NodeRef person = createUser("a/b/c", "fred");
        moveUserHomeFolders();
        assertHomeFolderLocation("fred", "fr/fred");
        
        properties.put("home_folder_provider_synchronizer.override_provider",
                testHomeFolderProviderName);
        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "a/b/c/fred");
        String providerName = (String) nodeService.getProperty(person,
                ContentModel.PROP_HOME_FOLDER_PROVIDER);
        assertEquals(testHomeFolderProviderName , providerName);
    }

    @Test
    public void testNoOriginalProvider() throws Exception
    {
        createUser("a/b/c", "fred", null, true);
        properties.put("home_folder_provider_synchronizer.override_provider",
                largeHomeFolderProviderName);
        
        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "fr/fred"); // unchanged
        assertTrue("Expected the empty parent 'a/b/c' to still exist as original " +
                "root was unknown, because the original home folder provider was not set.",
                exists("a/b/c"));
    }
    
    @Test
    @SuppressWarnings("deprecation")
    public void testVersion1HomeFolderProvider() throws Exception
    {
        // Should just log a message to say it can't do anything
        final String name = "v1Provider";
        HomeFolderProvider v1Provider = new HomeFolderProvider()
        {
            @Override
            public void onCreateNode(ChildAssociationRef childAssocRef)
            {
            }

            @Override
            public String getName()
            {
                return name;
            }
        };
        homeFolderManager.addProvider(v1Provider);

        createUser("a/b/c", "fred");
        
        properties.put("home_folder_provider_synchronizer.override_provider", name);
        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "a/b/c/fred");
    }
    
    @Test
    @SuppressWarnings("deprecation")
    public void testExtendsAbstractHomeFolderProvider() throws Exception
    {
        // Should work through the V2Adaptor
        final String name = "v1Provider";
        AbstractHomeFolderProvider v1Provider = new UIDBasedHomeFolderProvider();
        v1Provider.setBeanName(name);
        v1Provider.setHomeFolderManager(homeFolderManager);
        v1Provider.setOnCreatePermissionsManager(largeHomeFolderProvider.getOnCreatePermissionsManager());
        v1Provider.setOnReferencePermissionsManager(largeHomeFolderProvider.getOnReferencePermissionsManager());
        v1Provider.setOwnerOnCreate(largeHomeFolderProvider.getOwner());
        v1Provider.setPath(largeHomeFolderProvider.getRootPath());
        v1Provider.setServiceRegistry(serviceRegistry);
        v1Provider.setStoreUrl(largeHomeFolderProvider.getStoreUrl());
        v1Provider.setTenantService(tenantService);
        v1Provider.afterPropertiesSet();

        createUser("a/b/c", "fred");
        
        properties.put("home_folder_provider_synchronizer.override_provider", name);
        moveUserHomeFolders();

        assertHomeFolderLocation("fred", "fred");
    }
    
    @Test
    public void testTenantService() throws Exception
    {
        // Only test if running multi-tenant
        if (tenantAdminService.isEnabled())
        {
            long time = System.currentTimeMillis();
            final String tenant1 = time+".tenant1";
            final String tenant2 = time+".tenant2";
 
            createTenant(tenant1);
            createTenant(tenant2);

            createUser("", "fred");
            createUser(tenant1, "", "fred");
            createUser(tenant2, "", "fred");

            moveUserHomeFolders();

            assertHomeFolderLocation("fred", "fr/fred");
            assertHomeFolderLocation(tenant1, "fred", "fr/"+tenantService.getDomainUser("fred", tenant1));
            assertHomeFolderLocation(tenant2, "fred", "fr/"+tenantService.getDomainUser("fred", tenant2));
        }
    }
}
