/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.avm;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.config.JNDIConstants;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.permissions.AccessControlListDAO;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Specifically test AVM permissions with the updated ACL schema
 * 
 * @author andyh
 */
public class AVMServicePermissionsTest extends TestCase
{
    private static ApplicationContext applicationContext = AVMTestSuite.getContext();
    
    protected NodeService nodeService;

    protected DictionaryService dictionaryService;

    protected PermissionServiceSPI permissionService;

    protected MutableAuthenticationService authenticationService;

    private MutableAuthenticationDao authenticationDAO;

    protected LocalSessionFactoryBean sessionFactory;

    protected NodeRef rootNodeRef;

    protected NamespacePrefixResolver namespacePrefixResolver;

    protected ServiceRegistry serviceRegistry;

    protected NodeRef systemNodeRef;

    protected AuthenticationComponent authenticationComponent;

    protected ModelDAO permissionModelDAO;

    protected PersonService personService;

    protected AuthorityService authorityService;

    private AclDAO aclDaoComponent;

    private UserTransaction testTX;

    private TransactionService transactionService;

    private AVMService avmService;

    private AccessControlListDAO avmACLDAO;

    private AVMNodeDAO avmNodeDAO;

    private AVMSyncService avmSyncService;

    private ContentService contentService;

    public AVMServicePermissionsTest()
    {
        super();
    }

    @Override
    protected void setUp() throws Exception
    {
        avmNodeDAO = (AVMNodeDAO) applicationContext.getBean("avmNodeDAO");

        avmACLDAO = (AccessControlListDAO) applicationContext.getBean("avmNodeACLDAO");

        aclDaoComponent = (AclDAO) applicationContext.getBean("aclDAO");
        avmService = (AVMService) applicationContext.getBean("avmService");
        avmSyncService = (AVMSyncService) applicationContext.getBean("AVMSyncService");

        nodeService = (NodeService) applicationContext.getBean("nodeService");
        dictionaryService = (DictionaryService) applicationContext.getBean(ServiceRegistry.DICTIONARY_SERVICE.getLocalName());
        permissionService = (PermissionServiceSPI) applicationContext.getBean("permissionService");
        namespacePrefixResolver = (NamespacePrefixResolver) applicationContext.getBean(ServiceRegistry.NAMESPACE_SERVICE.getLocalName());
        authenticationService = (MutableAuthenticationService) applicationContext.getBean("authenticationService");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        permissionModelDAO = (ModelDAO) applicationContext.getBean("permissionsModelDAO");
        personService = (PersonService) applicationContext.getBean("personService");
        authorityService = (AuthorityService) applicationContext.getBean("authorityService");
        contentService = (ContentService) applicationContext.getBean("contentService");

        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        authenticationDAO = (MutableAuthenticationDao) applicationContext.getBean("authenticationDao");
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");

        testTX = transactionService.getUserTransaction();
        testTX.begin();
        this.authenticationComponent.setSystemUserAsCurrentUser();

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.nanoTime());
        rootNodeRef = nodeService.getRootNode(storeRef);

        QName children = ContentModel.ASSOC_CHILDREN;
        QName system = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
        QName container = ContentModel.TYPE_CONTAINER;
        QName types = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "people");

        systemNodeRef = nodeService.createNode(rootNodeRef, children, system, container).getChildRef();
        NodeRef typesNodeRef = nodeService.createNode(systemNodeRef, children, types, container).getChildRef();
        Map<QName, Serializable> props = createPersonProperties("andy");
        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();
        props = createPersonProperties("lemur");
        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();

        // create an authentication object e.g. the user
        if (authenticationDAO.userExists("andy"))
        {
            authenticationService.deleteAuthentication("andy");
        }
        authenticationService.createAuthentication("andy", "andy".toCharArray());

        if (authenticationDAO.userExists("lemur"))
        {
            authenticationService.deleteAuthentication("lemur");
        }
        authenticationService.createAuthentication("lemur", "lemur".toCharArray());

        if (authenticationDAO.userExists(AuthenticationUtil.getAdminUserName()))
        {
            authenticationService.deleteAuthentication(AuthenticationUtil.getAdminUserName());
        }
        authenticationService.createAuthentication(AuthenticationUtil.getAdminUserName(), "admin".toCharArray());

        if (authenticationDAO.userExists("manager"))
        {
            authenticationService.deleteAuthentication("manager");
        }
        authenticationService.createAuthentication("manager", "manager".toCharArray());

        if (authenticationDAO.userExists("publisher"))
        {
            authenticationService.deleteAuthentication("publisher");
        }
        authenticationService.createAuthentication("publisher", "publisher".toCharArray());

        if (authenticationDAO.userExists("contributor"))
        {
            authenticationService.deleteAuthentication("contributor");
        }
        authenticationService.createAuthentication("contributor", "contributor".toCharArray());

        if (authenticationDAO.userExists("reviewer"))
        {
            authenticationService.deleteAuthentication("reviewer");
        }
        authenticationService.createAuthentication("reviewer", "reviewer".toCharArray());

        authenticationComponent.clearCurrentSecurityContext();
        
        removeStore("main");
    }

    @Override
    protected void tearDown() throws Exception
    {

        try
        {
            testTX.commit();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
            super.tearDown();
        }
    }

    protected void runAs(String userName)
    {
        authenticationService.authenticate(userName, userName.toCharArray());
        assertNotNull(authenticationService.getCurrentUserName());
        // for(GrantedAuthority authority : woof.getAuthorities())
        // {
        // System.out.println("Auth = "+authority.getAuthority());
        // }

    }

    private Map<QName, Serializable> createPersonProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return properties;
    }

    protected PermissionReference getPermission(String permission)
    {
        return permissionModelDAO.getPermissionReference(null, permission);
    }

    private void buildBaseStructure(String base) throws IOException
    {
        avmService.createStore(base);
        avmService.createDirectory(base + ":/", "base");
        avmService.createDirectory(base + ":/base", "d-a");
        avmService.createDirectory(base + ":/base/d-a", "d-aa");
        avmService.createDirectory(base + ":/base/d-a", "d-ab");
        avmService.createDirectory(base + ":/base/d-a", "d-ac");
        avmService.createFile(base + ":/base/d-a", "f-aa").close();
        avmService.createDirectory(base + ":/base", "d-b");
        avmService.createDirectory(base + ":/base/d-b", "d-ba");
        avmService.createDirectory(base + ":/base/d-b", "d-bb");
        avmService.createDirectory(base + ":/base/d-b", "d-bc");
        avmService.createFile(base + ":/base/d-b", "f-ba").close();
        avmService.createDirectory(base + ":/base", "d-c");
        avmService.createDirectory(base + ":/base/d-c", "d-ca");
        avmService.createDirectory(base + ":/base/d-c", "d-cb");
        avmService.createDirectory(base + ":/base/d-c", "d-cc");
        avmService.createFile(base + ":/base/d-c", "f-ca").close();
        avmService.createFile(base + ":/base", "f-a").close();

        avmService.createDirectory(base + ":/base", "d-d");
        avmService.createLayeredDirectory(base + ":/base/d-a", base + ":/base/d-d", "layer-d-a");
        avmService.createLayeredDirectory(base + ":/base/d-b", base + ":/base/d-d", "layer-d-b");
        avmService.createLayeredDirectory(base + ":/base/d-c", base + ":/base/d-d", "layer-d-c");
        avmService.createLayeredFile(base + ":/base/f-a", base + ":/base/d-d", "layer-fa");

        avmService.createLayeredDirectory(base + ":/base", base + ":/", "layer");

        String layeredStore1 = base + "-layer-base";
        avmService.createStore(layeredStore1);
        avmService.createLayeredDirectory(base + ":/base", layeredStore1 + ":/", "layer-to-base");

        String layeredStore2 = base + "-layer-a";
        avmService.createStore(layeredStore2);
        avmService.createLayeredDirectory(base + ":/base/d-a", layeredStore2 + ":/", "layer-to-d-a");

        String layeredStore3 = base + "-layer-b";
        avmService.createStore(layeredStore3);
        avmService.createLayeredDirectory(base + ":/base/d-b", layeredStore3 + ":/", "layer-to-d-b");

        String layeredStore4 = base + "-layer-c";
        avmService.createStore(layeredStore4);
        avmService.createLayeredDirectory(base + ":/base/d-c", layeredStore4 + ":/", "layer-to-d-c");

        String layeredStore5 = base + "-layer-d";
        avmService.createStore(layeredStore5);
        avmService.createLayeredDirectory(base + ":/base/d-d", layeredStore5 + ":/", "layer-to-d-d");

        String layeredStore6 = base + "-layer-layer-base";
        avmService.createStore(layeredStore6);
        avmService.createLayeredDirectory(layeredStore1 + ":/layer-to-base", layeredStore6 + ":/", "layer-to-layer-to-base");

        String layeredStore7 = base + "-layer-layer-layer-base";
        avmService.createStore(layeredStore7);
        avmService.createLayeredDirectory(layeredStore6 + ":/layer-to-layer-to-base", layeredStore7 + ":/", "layer-to-layer-to-layer-to-base");
    }

    private boolean checkPermission(String user, String path, String permission, boolean allowed)
    {
        String curentUser = AuthenticationUtil.getRunAsUser();
        try
        {
            runAs(user);
            AVMNodeDescriptor desc = avmService.lookup(-1, path);
            AVMNode node = avmNodeDAO.getByID(desc.getId());
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, path);
            AVMStore store = AVMDAOs.Instance().fAVMStoreDAO.getByName(nodeRef.getStoreRef().getIdentifier());
            boolean can = AVMRepository.GetInstance().can(store, node, permission, AVMRepository.GetInstance().lookup(-1, path, false).getDirectlyContained());
            return allowed ? can : !can;
        }
        finally
        {
            runAs(curentUser);
        }
    }

    private boolean checkCanPerformance(String user, String path, String permission, boolean allowed, int count)
    {
        String curentUser = AuthenticationUtil.getRunAsUser();
        try
        {
            runAs(user);
            AVMNodeDescriptor desc = avmService.lookup(-1, path);
            AVMNode node = avmNodeDAO.getByID(desc.getId());
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, path);
            AVMStore store = AVMDAOs.Instance().fAVMStoreDAO.getByName(nodeRef.getStoreRef().getIdentifier());
            boolean can = AVMRepository.GetInstance().can(store, node, permission, AVMRepository.GetInstance().lookup(-1, path, false).getDirectlyContained());
            long start = System.nanoTime();
            for (int i = 0; i < count; i++)
            {
                can = AVMRepository.GetInstance().can(null, node, permission, AVMRepository.GetInstance().lookup(-1, path, false).getDirectlyContained());
            }
            long end = System.nanoTime();
            System.out.println("Can in " + ((end - start) / 10e9f / count));
            System.out.println("Can per second " + (1 / ((end - start) / 10e9f / count)));
            return allowed ? can : !can;
        }
        finally
        {
            runAs(curentUser);
        }
    }

    private boolean checkHasPermissionsPerformance(String user, String path, String permission, boolean allowed, int count)
    {
        String curentUser = AuthenticationUtil.getRunAsUser();
        try
        {
            runAs(user);
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, path);
            boolean can = permissionService.hasPermission(nodeRef, permission) == AccessStatus.ALLOWED;
            long start = System.nanoTime();
            for (int i = 0; i < count; i++)
            {
                can = permissionService.hasPermission(nodeRef, permission) == AccessStatus.ALLOWED;
            }
            long end = System.nanoTime();
            System.out.println("Has Permission in " + ((end - start) / 10e9f / count));
            System.out.println("Has Permission per second " + (1 / ((end - start) / 10e9f / count)));
            return allowed ? can : !can;
        }
        finally
        {
            runAs(curentUser);
        }
    }

    public boolean checkHasPermission(String user, NodeRef nodeRef, String permission, boolean allowed)
    {
        String curentUser = AuthenticationUtil.getRunAsUser();
        try
        {
            runAs(user);
            boolean can = permissionService.hasPermission(nodeRef, permission) == AccessStatus.ALLOWED;
            return allowed ? can : !can;
        }
        finally
        {
            runAs(curentUser);
        }
    }
    
    public void testSetup() throws Exception
    {
        // test setUp & tearDown
    }
    
    
    public void test_ETWOTWO_457_NPE1() throws Exception
    {
        try
        {
            // run as system (null)
            authenticationService.clearCurrentSecurityContext();
            
            avmService.createStore("main");
            avmService.createDirectory("main:/", "a");

            // java.lang.NullPointerException - at org.alfresco.service.cmr.security.AuthorityType.getAuthorityType(AuthorityType.java:254)
            Set<AccessPermission> perms = permissionService.getPermissions(AVMNodeConverter.ToNodeRef(-1, "main:/a"));
            for (AccessPermission permission : perms)
            {
                System.out.println(permission);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            avmService.purgeStore("main");
        }
    }
    
    public void test_ETWOTWO_457_NPE2() throws Exception
    {
        try
        {
            // run as admin
            authenticationService.authenticate("admin", "admin".toCharArray());
            
            avmService.createStore("main");
            avmService.createDirectory("main:/", "a");

            Set<AccessPermission> perms = permissionService.getPermissions(AVMNodeConverter.ToNodeRef(-1, "main:/a"));
            for (AccessPermission permission : perms)
            {
                System.out.println(permission);
            }
            
            // java.lang.NullPointerException - at org.alfresco.repo.domain.hibernate.AbstractPermissionsDaoComponentImpl.deletePermission(AbstractPermissionsDaoComponentImpl.java:383)
            permissionService.deletePermission(AVMNodeConverter.ToNodeRef(-1, "main:/a"), PermissionService.ADMINISTRATOR_AUTHORITY, PermissionService.ALL_PERMISSIONS);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            avmService.purgeStore("main");
        }
    }
    
    public void test_ETWOTWO_457_NPE3() throws Exception
    {
        try
        {
            // run as system (null)
            authenticationService.clearCurrentSecurityContext();
            authenticationComponent.setSystemUserAsCurrentUser();
            
            avmService.createStore("main");
            avmService.createFile("main:/", "foo").close();
            
            permissionService.setPermission(AVMNodeConverter.ToNodeRef(-1, "main:/").getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
            
            // java.lang.NullPointerException - at org.alfresco.repo.security.permissions.impl.PermissionServiceImpl.hasPermission(PermissionServiceImpl.java:494)
            ContentReader cr = contentService.getReader(AVMNodeConverter.ToNodeRef(-1, "main:/foo"), ContentModel.PROP_CONTENT);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            throw e;
        }
        finally
        {
            avmService.purgeStore("main");
        }
    }

    
    public void testStoreAcls() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            buildBaseStructure(storeName);

            AVMNodeDescriptor nodeDesc = avmService.lookup(-1, storeName + ":/base");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, nodeDesc.getPath());
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);

            assertTrue(checkPermission("andy", storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission("andy", nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkPermission("lemur", storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission("lemur", nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission(AuthenticationUtil.getAdminUserName(), nodeRef, PermissionService.ALL_PERMISSIONS, true));

            permissionService.setPermission(nodeRef.getStoreRef(), "andy", PermissionService.ALL_PERMISSIONS, true);

            assertTrue(checkPermission("andy", storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission("andy", nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkPermission("lemur", storeName + ":/base", PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkHasPermission("lemur", nodeRef, PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission(AuthenticationUtil.getAdminUserName(), nodeRef, PermissionService.ALL_PERMISSIONS, true));

            permissionService.deletePermission(nodeRef.getStoreRef(), "andy", PermissionService.ALL_PERMISSIONS);

            assertTrue(checkPermission("andy", storeName + ":/base", PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkHasPermission("andy", nodeRef, PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkPermission("lemur", storeName + ":/base", PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkHasPermission("lemur", nodeRef, PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission(AuthenticationUtil.getAdminUserName(), nodeRef, PermissionService.ALL_PERMISSIONS, true));

            permissionService.deletePermissions(nodeRef.getStoreRef());

            assertTrue(checkPermission("andy", storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission("andy", nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkPermission("lemur", storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission("lemur", nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission(AuthenticationUtil.getAdminUserName(), nodeRef, PermissionService.ALL_PERMISSIONS, true));

            permissionService.setPermission(nodeRef.getStoreRef(), "andy", PermissionService.ALL_PERMISSIONS, true);
            assertTrue(checkHasPermission("andy", nodeRef, PermissionService.ALL_PERMISSIONS, true));
            permissionService.setPermission(nodeRef.getStoreRef(), "andy", PermissionService.READ, true);
            permissionService.setPermission(nodeRef.getStoreRef(), "lemur", PermissionService.ALL_PERMISSIONS, true);

            assertTrue(checkPermission("andy", storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission("andy", nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkPermission("lemur", storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission("lemur", nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission(AuthenticationUtil.getAdminUserName(), nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertEquals(permissionService.getAllSetPermissions(nodeRef.getStoreRef()).size(), 3);

            permissionService.clearPermission(nodeRef.getStoreRef(), "andy");

            assertTrue(checkPermission("andy", storeName + ":/base", PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkHasPermission("andy", nodeRef, PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkPermission("lemur", storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission("lemur", nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission(AuthenticationUtil.getAdminUserName(), nodeRef, PermissionService.ALL_PERMISSIONS, true));
            assertEquals(permissionService.getAllSetPermissions(nodeRef.getStoreRef()).size(), 1);

            permissionService.clearPermission(nodeRef.getStoreRef(), "lemur");

            assertTrue(checkPermission("andy", storeName + ":/base", PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkHasPermission("andy", nodeRef, PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkPermission("lemur", storeName + ":/base", PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkHasPermission("lemur", nodeRef, PermissionService.ALL_PERMISSIONS, false));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + ":/base", PermissionService.ALL_PERMISSIONS, true));
            assertTrue(checkHasPermission(AuthenticationUtil.getAdminUserName(), nodeRef, PermissionService.ALL_PERMISSIONS, true));

        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-layer-base");
            removeStore(storeName + "-layer-a");
            removeStore(storeName + "-layer-b");
            removeStore(storeName + "-layer-c");
            removeStore(storeName + "-layer-d");
            removeStore(storeName + "-layer-layer-base");
            removeStore(storeName + "-layer-layer-layer-base");
            
            //System.out.println(avmService.getStores());
        }

    }

    public void testSimpleUpdate() throws Throwable
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            buildBaseStructure(storeName);
            avmService.createDirectory(storeName + "-layer-base:/layer-to-base", "update-dir");
            avmService.createFile(storeName + "-layer-base:/layer-to-base/update-dir", "update-file").close();

            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base");
            AVMNode node = avmNodeDAO.getByID(desc.getId());
            Acl acl = node.getAcl();
            assertNotNull(acl);
            acl = aclDaoComponent.getAcl(aclDaoComponent.getInheritedAccessControlList(acl.getId()));
            assertNotNull(acl);

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/update-dir");
            node = avmNodeDAO.getByID(desc.getId());
            Acl dirAcl = node.getAcl();
            assertNotNull(dirAcl);
            assertEquals(acl.getId(), dirAcl.getId());

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/update-dir/update-file");
            node = avmNodeDAO.getByID(desc.getId());
            Acl fileAcl = node.getAcl();
            assertNotNull(fileAcl);
            assertEquals(acl.getId(), fileAcl.getId());

            avmService.createSnapshot(storeName, "store", "store");
            avmService.createSnapshot(storeName + "-layer-base", "store", "store");

            List<AVMDifference> diffs = avmSyncService.compare(-1, storeName + "-layer-base:/layer-to-base", -1, storeName + ":/base", null);
            assertEquals(1, diffs.size());
            assertEquals("[" + storeName + "-layer-base:/layer-to-base/update-dir[-1] > " + storeName + ":/base/update-dir[-1]]", diffs.toString());
            avmSyncService.update(diffs, null, false, false, false, false, "A", "A");

            desc = avmService.lookup(-1, storeName + ":/base/update-dir");
            node = avmNodeDAO.getByID(desc.getId());
            dirAcl = node.getAcl();
            assertNull(dirAcl);

            desc = avmService.lookup(-1, storeName + ":/base/update-dir/update-file");
            node = avmNodeDAO.getByID(desc.getId());
            fileAcl = node.getAcl();
            assertNull(fileAcl);

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/update-dir");
            node = avmNodeDAO.getByID(desc.getId());
            dirAcl = node.getAcl();
            assertNotNull(dirAcl);
            assertEquals(acl.getId(), dirAcl.getId());

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/update-dir/update-file");
            node = avmNodeDAO.getByID(desc.getId());
            fileAcl = node.getAcl();
            assertNull(fileAcl);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-layer-base");
            removeStore(storeName + "-layer-a");
            removeStore(storeName + "-layer-b");
            removeStore(storeName + "-layer-c");
            removeStore(storeName + "-layer-d");
            removeStore(storeName + "-layer-layer-base");
            removeStore(storeName + "-layer-layer-layer-base");
        }
    }

    public void testUpdateWithPermissions() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            buildBaseStructure(storeName);

            AVMNodeDescriptor nodeDesc = avmService.lookup(-1, storeName + ":/base");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, nodeDesc.getPath());
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
            Long baseAcl = avmNodeDAO.getByID(nodeDesc.getId()).getAcl().getId();
            Long inheritedBaseAcl = aclDaoComponent.getInheritedAccessControlList(baseAcl);

            avmService.createDirectory(storeName + "-layer-base:/layer-to-base", "update-dir");
            avmService.createFile(storeName + "-layer-base:/layer-to-base/update-dir", "update-file").close();

            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base");
            AVMNode node = avmNodeDAO.getByID(desc.getId());
            Acl acl = node.getAcl();
            assertNotNull(acl);
            acl = aclDaoComponent.getAcl(aclDaoComponent.getInheritedAccessControlList(acl.getId()));
            assertNotNull(acl);

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/update-dir");
            node = avmNodeDAO.getByID(desc.getId());
            Acl dirAcl = node.getAcl();
            assertNotNull(dirAcl);
            assertEquals(acl.getId(), dirAcl.getId());

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/update-dir/update-file");
            node = avmNodeDAO.getByID(desc.getId());
            Acl fileAcl = node.getAcl();
            assertNotNull(fileAcl);
            assertEquals(acl.getId(), fileAcl.getId());

            avmService.createSnapshot(storeName, "store", "store");
            avmService.createSnapshot(storeName + "-layer-base", "store", "store");

            List<AVMDifference> diffs = avmSyncService.compare(-1, storeName + "-layer-base:/layer-to-base", -1, storeName + ":/base", null);
            assertEquals(1, diffs.size());
            assertEquals("[" + storeName + "-layer-base:/layer-to-base/update-dir[-1] > " + storeName + ":/base/update-dir[-1]]", diffs.toString());
            avmSyncService.update(diffs, null, false, false, false, false, "A", "A");

            desc = avmService.lookup(-1, storeName + ":/base/update-dir");
            node = avmNodeDAO.getByID(desc.getId());
            dirAcl = node.getAcl();
            assertNotNull(dirAcl);
            assertEquals(inheritedBaseAcl, dirAcl.getId());

            desc = avmService.lookup(-1, storeName + ":/base/update-dir/update-file");
            node = avmNodeDAO.getByID(desc.getId());
            fileAcl = node.getAcl();
            assertNotNull(fileAcl);
            assertEquals(inheritedBaseAcl, fileAcl.getId());

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/update-dir");
            node = avmNodeDAO.getByID(desc.getId());
            dirAcl = node.getAcl();
            assertNotNull(dirAcl);
            assertEquals(acl.getId(), dirAcl.getId());

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/update-dir/update-file");
            node = avmNodeDAO.getByID(desc.getId());
            fileAcl = node.getAcl();
            assertNotNull(fileAcl);
            assertEquals(inheritedBaseAcl, fileAcl.getId());
        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-layer-base");
            removeStore(storeName + "-layer-a");
            removeStore(storeName + "-layer-b");
            removeStore(storeName + "-layer-c");
            removeStore(storeName + "-layer-d");
            removeStore(storeName + "-layer-layer-base");
            removeStore(storeName + "-layer-layer-layer-base");
        }
    }

    public void testComplexStore_AlterInheritance() throws IOException
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            buildBaseStructure(storeName);

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, null);

            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);

            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + ":/base", PermissionService.READ, true));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + "-layer-base:/layer-to-base", PermissionService.READ, true));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + "-layer-base:/layer-to-base", PermissionService.ALL_PERMISSIONS, true));
            // True as unset defaults to allow
            assertTrue(checkPermission("lemur", storeName + ":/base", PermissionService.READ, true));
            assertTrue(checkPermission("lemur", storeName + "-layer-base:/layer-to-base", PermissionService.READ, true));
            assertTrue(checkPermission("lemur", storeName + "-layer-base:/layer-to-base", PermissionService.ALL_PERMISSIONS, true));

            desc = avmService.lookup(-1, storeName + ":/base");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
            permissionService.deletePermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS);

            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + ":/base", PermissionService.READ, true));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + "-layer-base:/layer-to-base", PermissionService.READ, true));
            assertTrue(checkPermission(AuthenticationUtil.getAdminUserName(), storeName + "-layer-base:/layer-to-base", PermissionService.ALL_PERMISSIONS, true));
            // True as unset defaults to allow
            assertTrue(checkPermission("lemur", storeName + ":/base", PermissionService.READ, false));
            assertTrue(checkPermission("lemur", storeName + "-layer-base:/layer-to-base", PermissionService.READ, true));
            assertTrue(checkPermission("lemur", storeName + "-layer-base:/layer-to-base", PermissionService.ALL_PERMISSIONS, true));

            // performance

            checkCanPerformance("lemur", storeName + ":/base", PermissionService.READ, false, 10000);
            checkHasPermissionsPerformance("lemur", storeName + ":/base", PermissionService.READ, false, 10000);

            String[] excludeL = new String[] { storeName + "-layer-base:/layer-to-base/d-d/layer-d-a" };
            String[] excludeLL = new String[] { storeName + "-layer-layer-base:/layer-to-layer-to-base/d-d/layer-d-a" };
            String[] excludeLLL = new String[] { storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-d/layer-d-a" };

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, excludeL);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    excludeLL);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, excludeLLL);

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/d-a");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setInheritParentPermissions(nodeRef, false);

            String[] excludeL2 = new String[] { storeName + "-layer-base:/layer-to-base/d-d/layer-d-a", storeName + "-layer-base:/layer-to-base/d-a" };
            String[] excludeLL2 = new String[] { storeName + "-layer-layer-base:/layer-to-layer-to-base/d-d/layer-d-a", storeName + "-layer-layer-base:/layer-to-layer-to-base/d-a" };
            String[] excludeLLL2 = new String[] { storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-d/layer-d-a",
                    storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-a" };

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, excludeL2);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base/d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    excludeLL2);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, excludeLLL2);

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base/d-a");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setInheritParentPermissions(nodeRef, true);

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, excludeL);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    excludeLL);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, excludeLLL);

        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-layer-base");
            removeStore(storeName + "-layer-a");
            removeStore(storeName + "-layer-b");
            removeStore(storeName + "-layer-c");
            removeStore(storeName + "-layer-d");
            removeStore(storeName + "-layer-layer-base");
            removeStore(storeName + "-layer-layer-layer-base");
        }
    }

    public void testComplexStore_AddPermissionsToMiddle() throws IOException
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            buildBaseStructure(storeName);

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, null);

            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + "-layer-a:/layer-to-d-a");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, null);

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());

            // debugPermissions(storeName + ":/base");
            // debugPermissions(storeName + "-layer-base:/layer-to-base");
            //
            // Acl acl = avmACLDAO.getAccessControlList(nodeRef);
            // List<Long> nodes = aclDaoComponent.getAvmNodesByACL(acl.getId());
            // for (Long id : nodes)
            // {
            // AVMNodeDescriptor layerDesc = new AVMNodeDescriptor(null, null, 0, null, null, null, 0, 0, 0, id, null,
            // 0, null, 0, false, 0, false, 0, 0);
            // List<Pair<Integer, String>> paths = avmService.getHeadPaths(layerDesc);
            // for(Pair<Integer, String> path : paths)
            // {
            // NodeRef testRef = AVMNodeConverter.ToNodeRef(-1, path.getSecond());
            // System.out.println("--> "+id +" "+path.getSecond()+ " "+path.getFirst()+ "
            // "+avmACLDAO.getAccessControlList(testRef));
            // }
            // }

            permissionService.setPermission(nodeRef, "loon", PermissionService.ALL_PERMISSIONS, true);

            // debugPermissions(storeName + ":/base");
            // debugPermissions(storeName + "-layer-base:/layer-to-base");
            //
            // acl = avmACLDAO.getAccessControlList(nodeRef);
            // nodes = aclDaoComponent.getAvmNodesByACL(acl.getId());
            // for (Long id : nodes)
            // {
            // AVMNodeDescriptor layerDesc = new AVMNodeDescriptor(null, null, 0, null, null, null, 0, 0, 0, id, null,
            // 0, null, 0, false, 0, false, 0, 0);
            // List<Pair<Integer, String>> paths = avmService.getHeadPaths(layerDesc);
            // for(Pair<Integer, String> path : paths)
            // {
            // NodeRef testRef = AVMNodeConverter.ToNodeRef(-1, path.getSecond());
            // System.out.println("--> "+id +" "+path.getSecond()+ " "+path.getFirst()+ "
            // "+avmACLDAO.getAccessControlList(testRef));
            // }
            // }
            //

            checkHeadPermissionNotSetForPath(storeName + ":/base", "loon", PermissionService.ALL_PERMISSIONS, true, null);
            String[] excludeL = new String[] { storeName + "-layer-base:/layer-to-base/d-d/layer-d-a" };
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", "loon", PermissionService.ALL_PERMISSIONS, true, excludeL);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", "loon", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", "loon", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", "loon", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", "loon", PermissionService.ALL_PERMISSIONS, true, null);
            String[] excludeLL = new String[] { storeName + "-layer-layer-base:/layer-to-layer-to-base/d-d/layer-d-a" };
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", "loon", PermissionService.ALL_PERMISSIONS, true, excludeLL);
            String[] excludeLLL = new String[] { storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-d/layer-d-a" };
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", "loon", PermissionService.ALL_PERMISSIONS, true, excludeLLL);

            desc = avmService.lookup(-1, storeName + "-layer-base:/layer-to-base");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "monkey", PermissionService.ALL_PERMISSIONS, true);

            debugPermissions(storeName + ":/base");
            debugPermissions(storeName + "-layer-base:/layer-to-base");

            // acl = avmACLDAO.getAccessControlList(nodeRef);
            // nodes = aclDaoComponent.getAvmNodesByACL(acl.getId());
            // for (Long id : nodes)
            // {
            // // need to fix up inheritance as is has changed
            // AVMNodeDescriptor layerDesc = new AVMNodeDescriptor(null, null, 0, null, null, null, 0, 0, 0, id, null,
            // 0, null, 0, false, 0, false, 0, 0);
            // List<Pair<Integer, String>> paths = avmService.getHeadPaths(layerDesc);
            // for(Pair<Integer, String> path : paths)
            // {
            // NodeRef testRef = AVMNodeConverter.ToNodeRef(-1, path.getSecond());
            // System.out.println("--> "+id +" "+path.getSecond()+ " "+path.getFirst()+ "
            // "+avmACLDAO.getAccessControlList(testRef));
            // }
            // }

            checkHeadPermissionNotSetForPath(storeName + ":/base", "monkey", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", "monkey", PermissionService.ALL_PERMISSIONS, true, excludeL);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", "monkey", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", "monkey", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", "monkey", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", "monkey", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", "monkey", PermissionService.ALL_PERMISSIONS, true, excludeLL);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", "monkey", PermissionService.ALL_PERMISSIONS, true, excludeLLL);

            debugPermissions(storeName + ":/base");
            debugPermissions(storeName + "-layer-base:/layer-to-base");

            // acl = avmACLDAO.getAccessControlList(nodeRef);
            // nodes = aclDaoComponent.getAvmNodesByACL(acl.getId());
            // for (Long id : nodes)
            // {
            // // need to fix up inheritance as is has changed
            // AVMNodeDescriptor layerDesc = new AVMNodeDescriptor(null, null, 0, null, null, null, 0, 0, 0, id, null,
            // 0, null, 0, false, 0, false, 0, 0);
            // List<Pair<Integer, String>> paths = avmService.getHeadPaths(layerDesc);
            // for(Pair<Integer, String> path : paths)
            // {
            // NodeRef testRef = AVMNodeConverter.ToNodeRef(-1, path.getSecond());
            // System.out.println("--> "+id +" "+path.getSecond()+ " "+path.getFirst()+ "
            // "+avmACLDAO.getAccessControlList(testRef));
            // }
            // }
            //
            desc = avmService.lookup(-1, storeName + ":/base");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "base", PermissionService.ALL_PERMISSIONS, true);

            debugPermissions(storeName + ":/base");
            debugPermissions(storeName + "-layer-base:/layer-to-base");
            checkHeadPermissionForPath(storeName + ":/base", "base", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base", "base", PermissionService.ALL_PERMISSIONS, true, excludeL);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", "base", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-b:/layer-to-d-b", "base", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-c:/layer-to-d-c", "base", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-d:/layer-to-d-d", "base", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", "base", PermissionService.ALL_PERMISSIONS, true, excludeLL);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", "base", PermissionService.ALL_PERMISSIONS, true, excludeLLL);

        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-layer-base");
            removeStore(storeName + "-layer-a");
            removeStore(storeName + "-layer-b");
            removeStore(storeName + "-layer-c");
            removeStore(storeName + "-layer-d");
            removeStore(storeName + "-layer-layer-base");
            removeStore(storeName + "-layer-layer-layer-base");
        }
    }

    public void testComplexStore_AddPermissionsToBottom() throws IOException
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            buildBaseStructure(storeName);

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, null);

            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + ":/base");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);

            checkHeadPermissionForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, null);

            permissionService.setPermission(nodeRef, "squid", PermissionService.ALL_PERMISSIONS, true);

            checkHeadPermissionForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, null);

            checkHeadPermissionForPath(storeName + ":/base", "squid", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", "squid", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-a:/layer-to-d-a", "squid", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-b:/layer-to-d-b", "squid", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-c:/layer-to-d-c", "squid", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-d:/layer-to-d-d", "squid", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", "squid", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", "squid", PermissionService.ALL_PERMISSIONS, true, null);

            permissionService.deletePermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS);

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, null);

            permissionService.setPermission(nodeRef, "me", PermissionService.ALL_PERMISSIONS, true);

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, null);

            checkHeadPermissionForPath(storeName + ":/base", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-a:/layer-to-d-a", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-b:/layer-to-d-b", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-c:/layer-to-d-c", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-d:/layer-to-d-d", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", "me", PermissionService.ALL_PERMISSIONS, true, null);

            desc = avmService.lookup(-1, storeName + ":/base/d-a");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "lemon", PermissionService.READ, true);

            checkHeadPermissionNotSetForPath(storeName + ":/base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-a:/layer-to-d-a", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true,
                    null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", PermissionService.ALL_AUTHORITIES,
                    PermissionService.ALL_PERMISSIONS, true, null);

            checkHeadPermissionForPath(storeName + ":/base", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-a:/layer-to-d-a", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-b:/layer-to-d-b", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-c:/layer-to-d-c", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-d:/layer-to-d-d", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base", "me", PermissionService.ALL_PERMISSIONS, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base", "me", PermissionService.ALL_PERMISSIONS, true, null);

            checkHeadPermissionForPath(storeName + ":/base/d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + ":/base/d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + ":/base/d-c", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionForPath(storeName + ":/base/d-d/layer-d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + ":/base/d-d/layer-d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + ":/base/d-d/layer-d-c", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base/d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base/d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base/d-c", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionForPath(storeName + "-layer-base:/layer-to-base/d-d/layer-d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base/d-d/layer-d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-base:/layer-to-base/d-d/layer-d-c", "lemon", PermissionService.READ, true, null);

            checkHeadPermissionForPath(storeName + "-layer-a:/layer-to-d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-b:/layer-to-d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-c:/layer-to-d-c", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionForPath(storeName + "-layer-d:/layer-to-d-d/layer-d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d/layer-d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-d:/layer-to-d-d/layer-d-c", "lemon", PermissionService.READ, true, null);

            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base/d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base/d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base/d-c", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base/d-d/layer-d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base/d-d/layer-d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-base:/layer-to-layer-to-base/d-d/layer-d-c", "lemon", PermissionService.READ, true, null);

            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-c", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-d/layer-d-a", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-d/layer-d-b", "lemon", PermissionService.READ, true, null);
            checkHeadPermissionNotSetForPath(storeName + "-layer-layer-layer-base:/layer-to-layer-to-layer-to-base/d-d/layer-d-c", "lemon", PermissionService.READ, true, null);
        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-layer-base");
            removeStore(storeName + "-layer-a");
            removeStore(storeName + "-layer-b");
            removeStore(storeName + "-layer-c");
            removeStore(storeName + "-layer-d");
            removeStore(storeName + "-layer-layer-base");
            removeStore(storeName + "-layer-layer-layer-base");
        }

    }
    
    /*
     * create directories & file in main
     * set file permission in layer
     * update back to main
     * flatten
     */
    public void testSimpleFilePermissionDiff() throws Throwable
    {
        runAs(AuthenticationUtil.getAdminUserName());
        
        String prefix = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        String baseStore = prefix+"-main";
        String layeredStore = prefix+"-layer";
        
        try
        {
            System.out.println("create store: " + baseStore);
            avmService.createStore(baseStore);
            
            // create directories and file in main
            System.out.println("create D: " + baseStore + ":/base");
            avmService.createDirectory(baseStore + ":/", "base");
            System.out.println("create D: " + baseStore + ":/base/d-a");
            avmService.createDirectory(baseStore + ":/base", "d-a");
            System.out.println("create F: " + baseStore + ":/base/d-a/f-aa");
            avmService.createFile(baseStore + ":/base/d-a", "f-aa").close();          
            
            AVMNodeDescriptor baseNodeDesc = avmService.lookup(-1, baseStore + ":/base/d-a/f-aa");
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            System.out.println("create store: " + layeredStore);
            avmService.createStore(layeredStore);
            
            System.out.println("create LD: " + layeredStore + ":/layer-to-base -> " + baseStore + ":/base");
            avmService.createLayeredDirectory(baseStore + ":/base", layeredStore + ":/", "layer-to-base");          
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            List<AVMDifference> diffs = avmSyncService.compare(-1, layeredStore + ":/layer-to-base", -1, baseStore + ":/base", null);
            assertEquals(0, diffs.size());          
            
            AVMNodeDescriptor layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer-to-base/d-a/f-aa");
            NodeRef layeredNodeRef = AVMNodeConverter.ToNodeRef(-1, layeredNodeDesc.getPath());
            
            // set DELETE permission on file in layer
            System.out.println("set P (DELETE): " + layeredStore + ":/layer-to-base/d-a/f-aa");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer-to-base", -1, baseStore + ":/base", null);
            assertEquals("["+layeredStore+":/layer-to-base/d-a/f-aa[-1] > "+baseStore+":/base/d-a/f-aa[-1]]", diffs.toString());
            assertEquals(1, diffs.size());          
            
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/base/d-a/f-aa");
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            // update main from layer
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/base/d-a/f-aa");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer-to-base", -1, baseStore + ":/base", null);
            assertEquals(0, diffs.size());
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer-to-base", baseStore + ":/base");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer-to-base", -1, baseStore + ":/base", null);
            assertEquals(0, diffs.size());
            
            // check that the DELETE permission is still set in main & layer
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/base/d-a/f-aa");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer-to-base/d-a/f-aa");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // check that file in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            removeStore(baseStore);
            removeStore(layeredStore);
        }
    }

    /*
     * create directory in main
     * set directory permission in main
     */
    public void testSimpleDirectoryPermissionDiff0() throws Throwable
    {
        runAs(AuthenticationUtil.getAdminUserName());
        
        String prefix = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        String baseStore = prefix+"-main";
        String layeredStore = prefix+"-layer";
        
        try
        {
            System.out.println("create store: " + baseStore);
            avmService.createStore(baseStore);
            
            // create directory in main
            System.out.println("create D: " + baseStore + ":/d-a");
            avmService.createDirectory(baseStore + ":/", "d-a");

            AVMNodeDescriptor baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);

            System.out.println("create store: " + layeredStore);
            avmService.createStore(layeredStore);
            
            System.out.println("create LD: " + layeredStore + ":/layer -> " + baseStore + ":/");
            avmService.createLayeredDirectory(baseStore + ":/", layeredStore + ":/", "layer");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            List<AVMDifference> diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());

            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            NodeRef baseNodeRef = AVMNodeConverter.ToNodeRef(-1, baseNodeDesc.getPath());
            
            // set DELETE permission on directory in main
            System.out.println("set P (DELETE): " + baseStore + ":/d-a");
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.setPermission(baseNodeRef, "andy", PermissionService.DELETE, true);
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
  
            // update main from layer - NOOP
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // flatten - NOOP
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the DELETE permission is still set in main (and appears in layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            AVMNodeDescriptor layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // check that directory in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            removeStore(baseStore);
            removeStore(layeredStore);
        }
    }
    
    /*
     * create directory in main
     * set directory permission in layer
     * update back to main
     * flatten
     */
    public void testSimpleDirectoryPermissionDiff1() throws Throwable
    {
        runAs(AuthenticationUtil.getAdminUserName());
        
        String prefix = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        String baseStore = prefix+"-main";
        String layeredStore = prefix+"-layer";
        
        try
        {
            System.out.println("create store: " + baseStore);
            avmService.createStore(baseStore);
            
            // create directory in main
            System.out.println("create D: " + baseStore + ":/d-a");
            avmService.createDirectory(baseStore + ":/", "d-a");

            AVMNodeDescriptor baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);

            System.out.println("create store: " + layeredStore);
            avmService.createStore(layeredStore);
            
            System.out.println("create LD: " + layeredStore + ":/layer -> " + baseStore + ":/");
            avmService.createLayeredDirectory(baseStore + ":/", layeredStore + ":/", "layer");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            List<AVMDifference> diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());

            AVMNodeDescriptor layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            NodeRef layeredNodeRef = AVMNodeConverter.ToNodeRef(-1, layeredNodeDesc.getPath());
            
            // set DELETE permission on directory in layer
            System.out.println("set P (DELETE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
  
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            // update main from layer
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the DELETE permission is still set in main (and appears in layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // check that directory in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            removeStore(baseStore);
            removeStore(layeredStore);
        }
    }
    
    /*
     * create directory in layer
     * set directory permission in layer
     * update back to main
     * flatten
     */
    public void testSimpleDirectoryPermissionDiff2() throws Throwable
    {
        runAs(AuthenticationUtil.getAdminUserName());
        
        String prefix = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        String baseStore = prefix+"-main";
        String layeredStore = prefix+"-layer";
        
        try
        {
            System.out.println("create store: " + baseStore);
            avmService.createStore(baseStore);
 
            System.out.println("create store: " + layeredStore);
            avmService.createStore(layeredStore);
            
            System.out.println("create LD: " + layeredStore + ":/layer -> " + baseStore + ":/");
            avmService.createLayeredDirectory(baseStore + ":/", layeredStore + ":/", "layer");
            
            // create directory in layer
            System.out.println("create D: " + layeredStore + ":/layer/d-a");
            avmService.createDirectory(layeredStore + ":/layer", "d-a");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            AVMNodeDescriptor layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            AVMNodeDescriptor baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            NodeRef layeredNodeRef = AVMNodeConverter.ToNodeRef(-1, layeredNodeDesc.getPath());
            
            // set DELETE permission on directory in layer
            System.out.println("set P (DELETE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            List<AVMDifference> diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            // update main from layer
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the DELETE permission is now set in main
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            layeredNodeRef = AVMNodeConverter.ToNodeRef(-1, layeredNodeDesc.getPath());
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the DELETE permission is still set in main (and appears in layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // check that directory in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            removeStore(baseStore);
            removeStore(layeredStore);
        }
    }
    
    /*
     * create directory in layer
     * set directory permission in layer
     * update back to main
     * flatten
     * set another directory permission in layer
     * update back to main
     * flatten
     */
    public void testSimpleDirectoryPermissionDiff3() throws Throwable
    {
        runAs(AuthenticationUtil.getAdminUserName());
        
        String prefix = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        String baseStore = prefix+"-main";
        String layeredStore = prefix+"-layer";
        
        try
        {
            System.out.println("create store: " + baseStore);
            avmService.createStore(baseStore);
 
            System.out.println("create store: " + layeredStore);
            avmService.createStore(layeredStore);
            
            System.out.println("create LD: " + layeredStore + ":/layer -> " + baseStore + ":/");
            avmService.createLayeredDirectory(baseStore + ":/", layeredStore + ":/", "layer");
            
            // create directory in layer
            System.out.println("create D: " + layeredStore + ":/layer/d-a");
            avmService.createDirectory(layeredStore + ":/layer", "d-a");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            AVMNodeDescriptor layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            AVMNodeDescriptor baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            NodeRef layeredNodeRef = AVMNodeConverter.ToNodeRef(-1, layeredNodeDesc.getPath());
            
            // set DELETE permission on directory in layer
            System.out.println("set P (DELETE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            List<AVMDifference> diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            // update main from layer
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the DELETE permission is now set in main
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the DELETE permission is still set in main (and appears in layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // check that directory in layer is the same as the one from main
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            assertEquals(baseNodeDesc, layeredNodeDesc);
            
            // repeat with another directory permission
            
            // set WRITE directory permission in layer
            System.out.println("set P (WRITE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.WRITE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            // update main from layer
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);      

            // check that the WRITE permission is now set in main
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.WRITE, true);
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the WRITE and DELETE permissions are still set in main (and appears in layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.WRITE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            
            // check that directory in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            removeStore(baseStore);
            removeStore(layeredStore);
        }
    }
    
    /*
     * create directory in layer
     * set directory permission in layer
     * snapshot layer, update back to main, snapshot main
     * create file in layer
     * snapshot layer, update back to main, snapshot main
     * flatten
     * set another directory permission in layer
     * snapshot layer, update back to main, snapshot main
     * flatten
     */
    public void testSimpleDirectoryPermissionDiff4() throws Throwable
    {
        runAs(AuthenticationUtil.getAdminUserName());
        
        String prefix = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        String baseStore = prefix+"-main";
        String layeredStore = prefix+"-layer";
        
        try
        {
            System.out.println("create store: " + baseStore);
            avmService.createStore(baseStore);
 
            System.out.println("create store: " + layeredStore);
            avmService.createStore(layeredStore);
            
            System.out.println("create LD: " + layeredStore + ":/layer -> " + baseStore + ":/");
            avmService.createLayeredDirectory(baseStore + ":/", layeredStore + ":/", "layer");
            
            // create directory in layer
            System.out.println("create D: " + layeredStore + ":/layer/d-a");
            avmService.createDirectory(layeredStore + ":/layer", "d-a");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            AVMNodeDescriptor layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            AVMNodeDescriptor baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            NodeRef layeredNodeRef = AVMNodeConverter.ToNodeRef(-1, layeredNodeDesc.getPath());
            
            // set DELETE permission on directory in layer
            System.out.println("set P (DELETE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            List<AVMDifference> diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            // snapshot layer, update main from layer, snapshot main
            System.out.println("snapshot: layer");
            avmService.createSnapshot(layeredStore, null, null);
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            System.out.println("snapshot: main");
            avmService.createSnapshot(baseStore, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the DELETE permission is now set in main
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            // create file in layer
            System.out.println("create F: " + layeredStore + ":/layer/d-a/f-aa");
            avmService.createFile(layeredStore + ":/layer/d-a", "f-aa").close();
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a/f-aa[-1] > "+baseStore+":/d-a/f-aa[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            // snapshot layer, update main from layer, snapshot main
            System.out.println("snapshot: layer");
            avmService.createSnapshot(layeredStore, null, null);
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            System.out.println("snapshot: main");
            avmService.createSnapshot(baseStore, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the DELETE permission is still set in main (and appears in layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // check that directory in layer is the same as the one from main
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            assertEquals(baseNodeDesc, layeredNodeDesc);
            
            // set another directory permission
            
            // set WRITE directory permission in layer
            System.out.println("set P (WRITE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.WRITE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            // snapshot layer, update main from layer, snapshot main
            System.out.println("snapshot: layer");
            avmService.createSnapshot(layeredStore, null, null);
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            System.out.println("snapshot: main");
            avmService.createSnapshot(baseStore, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);      

            // check that the WRITE permission is now set in main
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.WRITE, true);
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the WRITE and DELETE permissions are still set in main (and appears in layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.WRITE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            
            // check that directory in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            removeStore(baseStore);
            removeStore(layeredStore);
        }
    }
    
    /*
     * create directory in layer
     * set two directory permissions in layer
     * snapshot layer, update back to main, snapshot main
     * flatten
     * remove one of the directory permissions in layer
     * snapshot layer, update back to main, snapshot main
     * flatten
     */
    public void testSimpleDirectoryPermissionDiff5() throws Throwable
    {
        runAs(AuthenticationUtil.getAdminUserName());
        
        String prefix = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        String baseStore = prefix+"-main";
        String layeredStore = prefix+"-layer";
        
        try
        {
            System.out.println("create store: " + baseStore);
            avmService.createStore(baseStore);
 
            System.out.println("create store: " + layeredStore);
            avmService.createStore(layeredStore);
            
            System.out.println("create LD: " + layeredStore + ":/layer -> " + baseStore + ":/");
            avmService.createLayeredDirectory(baseStore + ":/", layeredStore + ":/", "layer");
            
            // create directory in layer
            System.out.println("create D: " + layeredStore + ":/layer/d-a");
            avmService.createDirectory(layeredStore + ":/layer", "d-a");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            AVMNodeDescriptor layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            AVMNodeDescriptor baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            NodeRef layeredNodeRef = AVMNodeConverter.ToNodeRef(-1, layeredNodeDesc.getPath());
            
            // set DELETE permission on directory in layer
            System.out.println("set P (DELETE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // set WRITE permission on directory in layer
            System.out.println("set P (WRITE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.WRITE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            
            List<AVMDifference> diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            // snapshot layer, update main from layer, snapshot main
            System.out.println("snapshot: layer");
            avmService.createSnapshot(layeredStore, null, null);
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            System.out.println("snapshot: main");
            avmService.createSnapshot(baseStore, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the WRITE and DELETE permissions are still set in main (and appears in layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.WRITE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            
            // check that directory in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
                     
            // remove one of the directory permissions
            
            // delete DELETE directory permission in layer
            System.out.println("delete P (DELETE): " + layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.deletePermission(layeredNodeRef, "andy", PermissionService.DELETE);
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            // snapshot layer, update main from layer, snapshot main
            System.out.println("snapshot: layer");
            avmService.createSnapshot(layeredStore, null, null);
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            System.out.println("snapshot: main");
            avmService.createSnapshot(baseStore, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);      

            // check that the DELETE permission is no longer set in main
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the WRITE permission is still set in main (and appears in layer)
            // check that the DELETE permission is not set in main (or layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.WRITE, true);
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // check that directory in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            removeStore(baseStore);
            removeStore(layeredStore);
        }
    }
    
    /*
     * create directory in layer
     * set two directory permissions in layer
     * snapshot layer, update back to main, snapshot main
     * flatten
     * remove one of the existing directory permissions in layer
     * set a different directory permission in layer
     * snapshot layer, update back to main, snapshot main
     * flatten
     */
    public void testSimpleDirectoryPermissionDiff6() throws Throwable
    {
        runAs(AuthenticationUtil.getAdminUserName());
        
        String prefix = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        String baseStore = prefix+"-main";
        String layeredStore = prefix+"-layer";
        
        try
        {
            System.out.println("create store: " + baseStore);
            avmService.createStore(baseStore);
 
            System.out.println("create store: " + layeredStore);
            avmService.createStore(layeredStore);
            
            System.out.println("create LD: " + layeredStore + ":/layer -> " + baseStore + ":/");
            avmService.createLayeredDirectory(baseStore + ":/", layeredStore + ":/", "layer");
            
            // create directory in layer
            System.out.println("create D: " + layeredStore + ":/layer/d-a");
            avmService.createDirectory(layeredStore + ":/layer", "d-a");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            AVMNodeDescriptor layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            AVMNodeDescriptor baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            NodeRef layeredNodeRef = AVMNodeConverter.ToNodeRef(-1, layeredNodeDesc.getPath());
            
            // set DELETE permission on directory in layer
            System.out.println("set P (DELETE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // set WRITE permission on directory in layer
            System.out.println("set P (WRITE): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.WRITE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            
            List<AVMDifference> diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            // snapshot layer, update main from layer, snapshot main
            System.out.println("snapshot: layer");
            avmService.createSnapshot(layeredStore, null, null);
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            System.out.println("snapshot: main");
            avmService.createSnapshot(baseStore, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the WRITE and DELETE permissions are still set in main (and appears in layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.DELETE, true);
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.WRITE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            
            // check that directory in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
                     
            // remove one of the directory permissions
            
            // delete DELETE directory permission in layer
            System.out.println("delete P (DELETE): " + layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            permissionService.deletePermission(layeredNodeRef, "andy", PermissionService.DELETE);
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // set READ permission on directory in layer
            System.out.println("set P (READ): " + layeredStore + ":/layer/d-a");
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.READ, true);
            permissionService.setPermission(layeredNodeRef, "andy", PermissionService.READ, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.READ, true);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals("["+layeredStore+":/layer/d-a[-1] > "+baseStore+":/d-a[-1]]", diffs.toString());
            assertEquals(1, diffs.size());
            
            // snapshot layer, update main from layer, snapshot main
            System.out.println("snapshot: layer");
            avmService.createSnapshot(layeredStore, null, null);
            System.out.println("update: main from layer");
            avmSyncService.update(diffs, null, false, false, false, false, null, null);
            System.out.println("snapshot: main");
            avmService.createSnapshot(baseStore, null, null);
            
            recursiveList(baseStore);
            recursiveList(layeredStore);      

            // check that the DELETE permission is no longer set in main
            // check that the READ permission is now set in main
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.READ, true);
            
            // flatten
            System.out.println("flatten: layer to main");
            avmSyncService.flatten(layeredStore + ":/layer", baseStore + ":/");
            
            recursiveList(baseStore);
            recursiveList(layeredStore);
            
            diffs = avmSyncService.compare(-1, layeredStore + ":/layer", -1, baseStore + ":/", null);
            assertEquals(0, diffs.size());
            
            // check that the WRITE & READ permissions are set in main (and appear in layer)
            // check that the DELETE permission is not set in main (or layer)
            baseNodeDesc = avmService.lookup(-1, baseStore + ":/d-a");
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.WRITE, true);
            checkHeadPermission(baseNodeDesc, "andy", PermissionService.READ, true);
            checkHeadPermissionNotSet(baseNodeDesc, "andy", PermissionService.DELETE, true);
            
            layeredNodeDesc = avmService.lookup(-1, layeredStore + ":/layer/d-a");
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            checkHeadPermission(layeredNodeDesc, "andy", PermissionService.WRITE, true);
            checkHeadPermissionNotSet(layeredNodeDesc, "andy", PermissionService.DELETE, true);
            
            // check that directory in layer is the same as the one from main
            assertEquals(baseNodeDesc, layeredNodeDesc);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            throw t;
        }
        finally
        {
            removeStore(baseStore);
            removeStore(layeredStore);
        }
    }

    private void checkHeadPermissionForPath(String path, String authority, String permission, boolean allow, String[] excludes)
    {
        AVMNodeDescriptor desc = avmService.lookup(-1, path);
        checkHeadPermission(desc, authority, permission, allow);
        if (desc.isDirectory())
        {
            Map<String, AVMNodeDescriptor> children = avmService.getDirectoryListing(desc);
            for (String child : children.keySet())
            {
                String newPath = path + "/" + child;
                if (excludes != null)
                {
                    for (String exclude : excludes)
                    {
                        if (newPath.startsWith(exclude))
                        {
                            return;
                        }
                    }
                }
                checkHeadPermissionForPath(newPath, authority, permission, allow, excludes);
            }
        }
    }

    private void checkHeadPermissionNotSetForPath(String path, String authority, String permission, boolean allow, String[] excludes)
    {
        AVMNodeDescriptor desc = avmService.lookup(-1, path);
        checkHeadPermissionNotSet(desc, authority, permission, allow);
        if (desc.isDirectory())
        {
            Map<String, AVMNodeDescriptor> children = avmService.getDirectoryListing(desc);
            for (String child : children.keySet())
            {
                String newPath = path + "/" + child;
                if (excludes != null)
                {
                    for (String exclude : excludes)
                    {
                        if (newPath.startsWith(exclude))
                        {
                            return;
                        }
                    }
                }
                checkHeadPermissionNotSetForPath(newPath, authority, permission, allow, excludes);
            }
        }
    }

    private void checkHeadPermission(AVMNodeDescriptor desc, String authority, String permission, boolean allow)
    {
        NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
        Set<AccessPermission> set = permissionService.getAllSetPermissions(nodeRef);
        for (AccessPermission p : set)
        {
            if (p.getAuthority().equals(authority))
            {
                if (p.getPermission().equals(permission))
                {
                    // debugPermissions(desc.getPath());
                    return;
                }
            }
        }
        debugPermissions(desc.getPath());
        fail("Permisssions not found at " + desc.getPath());
        // System.err.println("Permisssions not found at "+desc.getPath());
    }

    private void checkHeadPermissionNotSet(AVMNodeDescriptor desc, String authority, String permission, boolean allow)
    {
        NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
        Set<AccessPermission> set = permissionService.getAllSetPermissions(nodeRef);
        for (AccessPermission p : set)
        {
            if (p.getAuthority().equals(authority))
            {
                if (p.getPermission().equals(permission))
                {
                    debugPermissions(desc.getPath());
                    fail("Permisssions found at " + desc.getPath());
                }
            }
        }
        // debugPermissions(desc.getPath());
        // fail("Permisssions not found at "+desc.getPath());
        // System.err.println("Permisssions not found at "+desc.getPath());
    }

    public void testRedirectLayeredDirectory() throws IOException
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            avmService.createStore(storeName);
            avmService.createDirectory(storeName + ":/", "www");
            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            avmService.createFile(storeName + ":/www", "dog").close();
            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            avmService.createFile(storeName + ":/www/avm-web-apps", "cat").close();

            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + ":/www");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
            assertTrue(checkPermission("lemur", storeName + ":/www", PermissionService.READ, true));
            assertTrue(checkPermission("manager", storeName + ":/www", PermissionService.READ, true));
            assertTrue(checkPermission("publisher", storeName + ":/www", PermissionService.READ, true));
            assertTrue(checkPermission("contributor", storeName + ":/www", PermissionService.READ, true));
            assertTrue(checkPermission("reviewer", storeName + ":/www", PermissionService.READ, true));
            assertTrue(checkPermission("lemur", storeName + ":/www", "Coordinator", false));
            assertTrue(checkPermission("manager", storeName + ":/www", "Coordinator", false));
            assertTrue(checkPermission("publisher", storeName + ":/www", "Coordinator", false));
            assertTrue(checkPermission("contributor", storeName + ":/www", "Coordinator", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www", "Coordinator", false));
            assertTrue(checkPermission("lemur", storeName + ":/www", "Collaborator", false));
            assertTrue(checkPermission("manager", storeName + ":/www", "Collaborator", false));
            assertTrue(checkPermission("publisher", storeName + ":/www", "Collaborator", false));
            assertTrue(checkPermission("contributor", storeName + ":/www", "Collaborator", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www", "Collaborator", false));
            assertTrue(checkPermission("lemur", storeName + ":/www", "Contributor", false));
            assertTrue(checkPermission("manager", storeName + ":/www", "Contributor", false));
            assertTrue(checkPermission("publisher", storeName + ":/www", "Contributor", false));
            assertTrue(checkPermission("contributor", storeName + ":/www", "Contributor", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www", "Contributor", false));
            assertTrue(checkPermission("lemur", storeName + ":/www", "Editor", false));
            assertTrue(checkPermission("manager", storeName + ":/www", "Editor", false));
            assertTrue(checkPermission("publisher", storeName + ":/www", "Editor", false));
            assertTrue(checkPermission("contributor", storeName + ":/www", "Editor", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www", "Editor", false));
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "manager", "ContentManager", true);
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps", PermissionService.READ, true));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps", PermissionService.READ, true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps", PermissionService.READ, true));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps", PermissionService.READ, true));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps", PermissionService.READ, true));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps", "ContentManager", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps", "ContentManager", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps", "ContentManager", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps", "ContentManager", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps", "ContentManager", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps", "ContentPublisher", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps", "ContentPublisher", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps", "ContentPublisher", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps", "ContentPublisher", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps", "ContentPublisher", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps", "ContentContributor", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps", "ContentContributor", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps", "ContentContributor", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps", "ContentContributor", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps", "ContentContributor", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps", "ContentReviewer", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps", "ContentReviewer", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps", "ContentReviewer", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps", "ContentReviewer", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps", "ContentReviewer", false));
            desc = avmService.lookup(-1, storeName + ":/www/dog");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "publisher", "Collaborator", true);
            assertTrue(checkPermission("lemur", storeName + ":/www/dog", PermissionService.READ, true));
            assertTrue(checkPermission("manager", storeName + ":/www/dog", PermissionService.READ, true));
            assertTrue(checkPermission("publisher", storeName + ":/www/dog", PermissionService.READ, true));
            assertTrue(checkPermission("contributor", storeName + ":/www/dog", PermissionService.READ, true));
            assertTrue(checkPermission("reviewer", storeName + ":/www/dog", PermissionService.READ, true));
            assertTrue(checkPermission("lemur", storeName + ":/www/dog", "Coordinator", false));
            assertTrue(checkPermission("manager", storeName + ":/www/dog", "Coordinator", false));
            assertTrue(checkPermission("publisher", storeName + ":/www/dog", "Coordinator", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/dog", "Coordinator", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/dog", "Coordinator", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/dog", "Collaborator", false));
            assertTrue(checkPermission("manager", storeName + ":/www/dog", "Collaborator", false));
            assertTrue(checkPermission("publisher", storeName + ":/www/dog", "Collaborator", true));
            assertTrue(checkPermission("contributor", storeName + ":/www/dog", "Collaborator", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/dog", "Collaborator", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/dog", "Contributor", false));
            assertTrue(checkPermission("manager", storeName + ":/www/dog", "Contributor", false));
            assertTrue(checkPermission("publisher", storeName + ":/www/dog", "Contributor", true));
            assertTrue(checkPermission("contributor", storeName + ":/www/dog", "Contributor", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/dog", "Contributor", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/dog", "Editor", false));
            assertTrue(checkPermission("manager", storeName + ":/www/dog", "Editor", false));
            assertTrue(checkPermission("publisher", storeName + ":/www/dog", "Editor", true));
            assertTrue(checkPermission("contributor", storeName + ":/www/dog", "Editor", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/dog", "Editor", false));
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "contributor", "Coordinator", true);
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/ROOT", PermissionService.READ, true));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/ROOT", PermissionService.READ, true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/ROOT", PermissionService.READ, true));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/ROOT", PermissionService.READ, true));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/ROOT", PermissionService.READ, true));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/ROOT", "Coordinator", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/ROOT", "Coordinator", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/ROOT", "Coordinator", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/ROOT", "Coordinator", true));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/ROOT", "Coordinator", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/ROOT", "Collaborator", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/ROOT", "Collaborator", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/ROOT", "Collaborator", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/ROOT", "Collaborator", true));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/ROOT", "Collaborator", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/ROOT", "Contributor", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/ROOT", "Contributor", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/ROOT", "Contributor", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/ROOT", "Contributor", true));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/ROOT", "Contributor", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/ROOT", "Editor", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/ROOT", "Editor", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/ROOT", "Editor", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/ROOT", "Editor", true));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/ROOT", "Editor", false));
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/cat");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "reviewer", "Editor", true);
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/cat", PermissionService.READ, true));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/cat", PermissionService.READ, true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/cat", PermissionService.READ, true));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/cat", PermissionService.READ, true));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/cat", PermissionService.READ, true));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/cat", "Coordinator", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/cat", "Coordinator", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/cat", "Coordinator", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/cat", "Coordinator", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/cat", "Coordinator", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/cat", "Collaborator", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/cat", "Collaborator", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/cat", "Collaborator", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/cat", "Collaborator", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/cat", "Collaborator", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/cat", "Contributor", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/cat", "Contributor", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/cat", "Contributor", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/cat", "Contributor", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/cat", "Contributor", false));
            assertTrue(checkPermission("lemur", storeName + ":/www/avm-web-apps/cat", "Editor", false));
            assertTrue(checkPermission("manager", storeName + ":/www/avm-web-apps/cat", "Editor", true));
            assertTrue(checkPermission("publisher", storeName + ":/www/avm-web-apps/cat", "Editor", false));
            assertTrue(checkPermission("contributor", storeName + ":/www/avm-web-apps/cat", "Editor", false));
            assertTrue(checkPermission("reviewer", storeName + ":/www/avm-web-apps/cat", "Editor", true));

            avmService.createSnapshot(storeName, null, null);
            avmService.createLayeredDirectory(storeName + ":/www", storeName + ":/", "layer");

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/layer/dog");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/cat");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);

            avmService.retargetLayeredDirectory(storeName + ":/layer", storeName + ":/www/avm-web-apps");

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/layer/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/cat");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);

            avmService.retargetLayeredDirectory(storeName + ":/layer", storeName + ":/www");

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/layer/dog");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/cat");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "monkey", PermissionService.READ, true);
            avmService.createDirectory(storeName + ":/layer", "l-d");
            desc = avmService.lookup(-1, storeName + ":/layer/l-d");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "directory-monkey", PermissionService.READ, true);
            avmService.createFile(storeName + ":/layer", "l-f").close();
            desc = avmService.lookup(-1, storeName + ":/layer/l-f");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "file-monkey", PermissionService.READ, true);

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/layer/l-d");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/l-f");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/dog");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/cat");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            debugPermissions(storeName + ":/layer/avm-web-apps/cat");

            // As we have set permissions on this node it has done COW and now defined its own permissions.
            // Changing the target does not change the permissions just the content and locations
            // Some underlying nodes have not been COWed - and so pick up underlygin changes
            // - it is only layer and its direct children that will now hace fixed permissions.
            // Joy all round

            // Note copy on writed nodes will move taking context.... so cat appears in two places
            // / over layed as cat .... and also as avm-web-apps from the previous copy on write and then move ....

            avmService.retargetLayeredDirectory(storeName + ":/layer", storeName + ":/www/avm-web-apps");

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/layer/l-d");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/l-f");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/layer/cat");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/cat");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);

            avmService.retargetLayeredDirectory(storeName + ":/layer", storeName + ":/www");

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/layer/l-d");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/l-f");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/dog");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/cat");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);

        }
        finally
        {
            removeStore(storeName);
        }
    }

    public void testCopy()
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            avmService.createStore(storeName);
            avmService.createDirectory(storeName + ":/", "www");
            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + ":/www");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            Map<String, Integer> s1 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
            Map<String, Integer> s2 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "manager", "ContentManager", true);
            Map<String, Integer> s3 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "publisher", "ContentPublisher", true);
            Map<String, Integer> s4 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "contributor", "ContentContributor", true);
            Map<String, Integer> s5 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "reviewer", "ContentReviewer", true);
            Map<String, Integer> s6 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");

            avmService.copy(-1, storeName + ":/www", storeName + ":/", "head");
            desc = avmService.lookup(-1, storeName + ":/head");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, storeName + ":/head/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            avmService.copy(s1.get(storeName), storeName + ":/www", storeName + ":/", "s1");
            desc = avmService.lookup(-1, storeName + ":/s1");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 0);
            desc = avmService.lookup(-1, storeName + ":/s1/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 0);

            avmService.copy(s2.get(storeName), storeName + ":/www", storeName + ":/", "s2");
            desc = avmService.lookup(-1, storeName + ":/s2");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);
            desc = avmService.lookup(-1, storeName + ":/s2/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.copy(s3.get(storeName), storeName + ":/www", storeName + ":/", "s3");
            desc = avmService.lookup(-1, storeName + ":/s3");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/s3/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);

            avmService.copy(s4.get(storeName), storeName + ":/www", storeName + ":/", "s4");
            desc = avmService.lookup(-1, storeName + ":/s4");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/s4/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);

            avmService.copy(s5.get(storeName), storeName + ":/www", storeName + ":/", "s5");
            desc = avmService.lookup(-1, storeName + ":/s5");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/s5/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);

            avmService.copy(s6.get(storeName), storeName + ":/www", storeName + ":/", "s6");
            desc = avmService.lookup(-1, storeName + ":/s6");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, storeName + ":/s6/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "monkey", "ContentReviewer", true);

            avmService.copy(s6.get(storeName), storeName + ":/www", storeName + ":/", "s6");
            desc = avmService.lookup(-1, storeName + ":/s6");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, storeName + ":/s6/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "monkey", "ContentReviewer", true);

            avmService.copy(s6.get(storeName), storeName + ":/www", storeName + ":/", "s6");
            desc = avmService.lookup(-1, storeName + ":/s6");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, storeName + ":/s6/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

        }
        finally
        {
            removeStore(storeName);
        }
    }

    public void testBranches()
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            avmService.createStore(storeName);
            avmService.createDirectory(storeName + ":/", "www");
            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + ":/www");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            Map<String, Integer> s1 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
            Map<String, Integer> s2 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "manager", "ContentManager", true);
            Map<String, Integer> s3 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "publisher", "ContentPublisher", true);
            Map<String, Integer> s4 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "contributor", "ContentContributor", true);
            Map<String, Integer> s5 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "reviewer", "ContentReviewer", true);
            Map<String, Integer> s6 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");

            avmService.createBranch(-1, storeName + ":/www", storeName + ":/", "head");
            desc = avmService.lookup(-1, storeName + ":/head");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, storeName + ":/head/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            avmService.createBranch(s1.get(storeName), storeName + ":/www", storeName + ":/", "s1");
            desc = avmService.lookup(-1, storeName + ":/s1");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 0);
            desc = avmService.lookup(-1, storeName + ":/s1/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 0);

            avmService.createBranch(s2.get(storeName), storeName + ":/www", storeName + ":/", "s2");
            desc = avmService.lookup(-1, storeName + ":/s2");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);
            desc = avmService.lookup(-1, storeName + ":/s2/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createBranch(s3.get(storeName), storeName + ":/www", storeName + ":/", "s3");
            desc = avmService.lookup(-1, storeName + ":/s3");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/s3/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);

            avmService.createBranch(s4.get(storeName), storeName + ":/www", storeName + ":/", "s4");
            desc = avmService.lookup(-1, storeName + ":/s4");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/s4/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);

            avmService.createBranch(s5.get(storeName), storeName + ":/www", storeName + ":/", "s5");
            desc = avmService.lookup(-1, storeName + ":/s5");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/s5/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);

            avmService.createBranch(s6.get(storeName), storeName + ":/www", storeName + ":/", "s6");
            desc = avmService.lookup(-1, storeName + ":/s6");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, storeName + ":/s6/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "monkey", "ContentReviewer", true);

            avmService.createBranch(s6.get(storeName), storeName + ":/www", storeName + ":/", "s6");
            desc = avmService.lookup(-1, storeName + ":/s6");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, storeName + ":/s6/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "monkey", "ContentReviewer", true);

            avmService.createBranch(s6.get(storeName), storeName + ":/www", storeName + ":/", "s6");
            desc = avmService.lookup(-1, storeName + ":/s6");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, storeName + ":/s6/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

        }
        finally
        {
            removeStore(storeName);
        }
    }

    @SuppressWarnings("unused")
    public void testWCMStyleTemplateAsBranch()
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        String branchName = storeName + "-Branch";
        try
        {
            avmService.createStore(storeName);
            avmService.createDirectory(storeName + ":/", "www");
            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + ":/www");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            Map<String, Integer> s1 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
            Map<String, Integer> s2 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "manager", "ContentManager", true);
            Map<String, Integer> s3 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "publisher", "ContentPublisher", true);
            Map<String, Integer> s4 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "contributor", "ContentContributor", true);
            Map<String, Integer> s5 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            permissionService.setPermission(nodeRef, "reviewer", "ContentReviewer", true);
            Map<String, Integer> s6 = avmService.createSnapshot(storeName, null, null);
            desc = avmService.lookup(-1, storeName + ":/www");
            
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            
            avmService.createStore(branchName);
            avmService.createBranch(-1, storeName + ":/www", branchName + ":/", "www");
            avmService.createSnapshot(branchName, null, null);
            
            
            desc = avmService.lookup(-1, branchName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, branchName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            
            
            // Check the branch remains unchanged when the template is changed
            
            debugPermissions(storeName + ":/www");
            debugPermissions(branchName + ":/www");
            debugPermissions(branchName + ":/www/avm-web-apps");
            
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "template", "ContentReviewer", true);
            
            desc = avmService.lookup(-1, branchName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            desc = avmService.lookup(-1, branchName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            
            debugPermissions(storeName + ":/www");
            debugPermissions(branchName + ":/www");
            debugPermissions(branchName + ":/www/avm-web-apps");
            
            desc = avmService.lookup(-1, branchName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "new", "ContentReviewer", true);
            
            desc = avmService.lookup(-1, branchName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            desc = avmService.lookup(-1, branchName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            debugPermissions(storeName + ":/www");
            debugPermissions(branchName + ":/www");
            debugPermissions(branchName + ":/www/avm-web-apps");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            
            
        }
        finally
        {
            removeStore(storeName);
            removeStore(branchName);
        }
    }

    /*
     * Test the basic permission model where
     */
    public void testSimpleExternalLayer() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            avmService.createStore(storeName);
            avmService.createDirectory(storeName + ":/", "www");
            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + ":/www");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);
            Long definingId = avmACLDAO.getAccessControlList(nodeRef).getId();
            String definingGuid = aclDaoComponent.getAccessControlListProperties(definingId).getAclId();

            permissionService.setPermission(nodeRef, "manager", "ContentManager", true);
            permissionService.setPermission(nodeRef, "publisher", "ContentPublisher", true);
            permissionService.setPermission(nodeRef, "contributor", "ContentContributor", true);
            permissionService.setPermission(nodeRef, "reviewer", "ContentReviewer", true);

            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(definingId, avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            avmService.createDirectory(storeName + ":/www/avm-web-apps/ROOT", "directory");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            avmService.createFile(storeName + ":/www/avm-web-apps/ROOT", "file").close();
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            // simple layer

            avmService.createSnapshot(storeName, null, null);
            avmService.createStore(storeName + "-a-");
            avmService.createLayeredDirectory(storeName + ":/www", storeName + "-a-:/", "www");
            avmService.createSnapshot(storeName, null, null);
            avmService.createSnapshot(storeName + "-a-", null, null);

            desc = avmService.lookup(-1, storeName + "-a-:/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            // Add permissions beneath and check they appear up
            // Check version has not moved and the id is the same as they are in the same TX and will not have COWed

            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());

            // debugPermissionsut.println("BEFORE:");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/file");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/directory");
            // debugPermissions(storeName + "-a-:/");
            // debugPermissions(storeName + "-a-:/www");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT/file");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT/directory");

            testTX.commit();
            testTX = transactionService.getUserTransaction();
            testTX.begin();

            permissionService.setPermission(nodeRef, "andy", "ContentReviewer", true);

            // System.out.println("AFTER:");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/file");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/directory");
            // debugPermissions(storeName + "-a-:/");
            // debugPermissions(storeName + "-a-:/www");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT/file");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT/directory");

            Long newId = avmACLDAO.getAccessControlList(nodeRef).getId();
            assertFalse(newId.equals(definingId));
            assertEquals(definingGuid, aclDaoComponent.getAccessControlListProperties(newId).getAclId());
            assertEquals(aclDaoComponent.getAccessControlListProperties(definingId).getAclVersion().longValue() + 1, aclDaoComponent.getAccessControlListProperties(newId)
                    .getAclVersion().longValue());

            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(newId, avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            // Add permissions to the layer

            desc = avmService.lookup(-1, storeName + "-a-:/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "bob", "ContentReviewer", true);

            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(newId, avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            newId = avmACLDAO.getAccessControlList(nodeRef).getId();

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-a-");

        }
    }
    
    // Comment-out for now due to intermittent failure: expected:<6> but was:<7>
    public void x_testSimpleInternalLayer() throws IOException
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            avmService.createStore(storeName);
            avmService.createDirectory(storeName + ":/", "www");
            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + ":/www");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);
            Long definingId = avmACLDAO.getAccessControlList(nodeRef).getId();
            String definingGuid = aclDaoComponent.getAccessControlListProperties(definingId).getAclId();

            permissionService.setPermission(nodeRef, "manager", "ContentManager", true);
            permissionService.setPermission(nodeRef, "publisher", "ContentPublisher", true);
            permissionService.setPermission(nodeRef, "contributor", "ContentContributor", true);
            permissionService.setPermission(nodeRef, "reviewer", "ContentReviewer", true);

            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(definingId, avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            avmService.createDirectory(storeName + ":/www/avm-web-apps/ROOT", "directory");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            avmService.createFile(storeName + ":/www/avm-web-apps/ROOT", "file").close();
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            // simple layer

            avmService.createSnapshot(storeName, null, null);

            avmService.createLayeredDirectory(storeName + ":/www", storeName + ":/", "layer");
            avmService.createSnapshot(storeName, null, null);
            avmService.createSnapshot(storeName, null, null);

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(definingId), avmACLDAO.getAccessControlList(nodeRef).getId());

            // Add permissions beneath and check they appear up
            // Check version has not moved and the id is the same as they are in the same TX and will not have COWed

            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());

            // System.out.println("BEFORE:");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/file");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/directory");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/layer");
            // debugPermissions(storeName + ":/layer/avm-web-apps");
            // debugPermissions(storeName + ":/layer/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/layer/avm-web-apps/ROOT/file");
            // debugPermissions(storeName + ":/layer/avm-web-apps/ROOT/directory");

            permissionService.setPermission(nodeRef, "andy", "ContentReviewer", true);

            // System.out.println("AFTER:");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/file");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/directory");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/layer");
            // debugPermissions(storeName + ":/layer/avm-web-apps");
            // debugPermissions(storeName + ":/layer/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/layer/avm-web-apps/ROOT/file");
            // debugPermissions(storeName + ":/layer/avm-web-apps/ROOT/directory");

            Long newId = avmACLDAO.getAccessControlList(nodeRef).getId();
            assertFalse(newId.equals(definingId));
            assertEquals(definingGuid, aclDaoComponent.getAccessControlListProperties(newId).getAclId());
            assertEquals(aclDaoComponent.getAccessControlListProperties(definingId).getAclVersion().longValue() + 1, aclDaoComponent.getAccessControlListProperties(newId)
                    .getAclVersion().longValue());

            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(newId, avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            // Add permissions to the layer

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "bob", "ContentReviewer", true);

            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(newId, avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 6);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            newId = avmACLDAO.getAccessControlList(nodeRef).getId();

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

            desc = avmService.lookup(-1, storeName + ":/layer/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);
            assertEquals(aclDaoComponent.getInheritedAccessControlList(newId), avmACLDAO.getAccessControlList(nodeRef).getId());

        }
        finally
        {
            removeStore(storeName);
        }
    }

    private void debugPermissions(String path)
    {
        AVMNodeDescriptor desc = avmService.lookup(-1, path);
        NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
        Acl acl = avmACLDAO.getAccessControlList(nodeRef);
        System.out.println(path);
        System.out.println("\t => Ind="
                + desc.getIndirection() + ",Deleted=" + desc.isDeleted() + ",LD=" + desc.isLayeredDirectory() + ",LF=" + desc.isLayeredFile() + ",PD=" + desc.isPlainDirectory()
                + ",PF=" + desc.isPlainFile() + ",Primary=" + desc.isPrimary());
        System.out.println("\t => " + acl);
    }

    public void testMutationsWithSimpleLayers() throws IOException
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            avmService.createStore(storeName);
            avmService.createDirectory(storeName + ":/", "www");
            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");

            AVMNodeDescriptor desc = avmService.lookup(-1, storeName + ":/www");
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());

            // debugPermissions(storeName + ":/");
            // /debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            permissionService.setPermission(nodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            permissionService.setPermission(nodeRef, "manager", "ContentManager", true);
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            permissionService.setPermission(nodeRef, "publisher", "ContentPublisher", true);
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            permissionService.setPermission(nodeRef, "contributor", "ContentContributor", true);
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            permissionService.setPermission(nodeRef, "reviewer", "ContentReviewer", true);
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");

            avmService.createSnapshot(storeName, null, null);

            // System.out.println("Snapshot");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");

            avmService.createStore(storeName + "-a-");
            avmService.createLayeredDirectory(storeName + ":/www", storeName + "-a-:/", "www");

            // System.out.println("Layered");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");

            avmService.createDirectory(storeName + ":/www/avm-web-apps/ROOT", "directory");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/directory");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            // System.out.println("New Dir");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/directory");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            avmService.createDirectory(storeName + "-a-:/www/avm-web-apps/ROOT", "directory2");
            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT/directory2");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 5);

            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            // System.out.println("Before Andy");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/directory");
            permissionService.setPermission(nodeRef, "andy", "ContentReviewer", true);
            // System.out.println("Before Lemur");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/directory");
            permissionService.setPermission(nodeRef, "lemur", "ContentReviewer", true);
            // System.out.println("After");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");

            desc = avmService.lookup(-1, storeName + "-a-:/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "bob", "ContentReviewer", true);
            permissionService.setPermission(nodeRef, "jim", "ContentReviewer", true);
            permissionService.setPermission(nodeRef, "dave", "ContentReviewer", true);

            avmService.createFile(storeName + ":/www/avm-web-apps/ROOT", "file").close();
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/file");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            // TODO: Check this
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 7);

            avmService.createFile(storeName + "-a-:/www/avm-web-apps/ROOT", "file2").close();
            desc = avmService.lookup(-1, storeName + "-a-:/www/avm-web-apps/ROOT/file2");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 10);

        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-a-");
        }
    }

    public void testRenamePlainDirectory()
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            AVMNodeDescriptor desc;
            NodeRef nodeRef;

            avmService.createStore(storeName);

            avmService.createDirectory(storeName + ":/", "www");
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "one", PermissionService.ALL_PERMISSIONS, true);
            // System.out.println("After One");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "two", PermissionService.ALL_PERMISSIONS, true);
            // System.out.println("After Two");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "three", PermissionService.ALL_PERMISSIONS, true);
            // System.out.println("After Three");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www/avm-web-apps/ROOT", "test");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/test");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "four", PermissionService.ALL_PERMISSIONS, true);
            // System.out.println("After Four");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/test");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.rename(storeName + ":/www/avm-web-apps/ROOT", "test", storeName + ":/www/avm-web-apps/ROOT", "lemon");
            // System.out.println("After Rename to lemon");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/lemon");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/lemon");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.rename(storeName + ":/www/avm-web-apps/ROOT", "lemon", storeName + ":/www/avm-web-apps", "orange");
            // System.out.println("After move up and rename 1");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/orange");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/orange");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);

            avmService.rename(storeName + ":/www/avm-web-apps", "orange", storeName + ":/www", "blue");
            // System.out.println("After move up and rename 2");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/blue");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/orange");
            desc = avmService.lookup(-1, storeName + ":/www/blue");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);

        }
        finally
        {
            removeStore(storeName);
        }
    }

    public void testRenamePlainFile() throws IOException
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            AVMNodeDescriptor desc;
            NodeRef nodeRef;

            avmService.createStore(storeName);

            avmService.createDirectory(storeName + ":/", "www");
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "one", PermissionService.ALL_PERMISSIONS, true);
            // System.out.println("After One");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "two", PermissionService.ALL_PERMISSIONS, true);
            // System.out.println("After Two");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "three", PermissionService.ALL_PERMISSIONS, true);
            // System.out.println("After Three");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createFile(storeName + ":/www/avm-web-apps/ROOT", "test").close();
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/test");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "four", PermissionService.ALL_PERMISSIONS, true);
            // System.out.println("After Four");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/test");
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.rename(storeName + ":/www/avm-web-apps/ROOT", "test", storeName + ":/www/avm-web-apps/ROOT", "lemon");
            // System.out.println("After Rename to lemon");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/lemon");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/lemon");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.rename(storeName + ":/www/avm-web-apps/ROOT", "lemon", storeName + ":/www/avm-web-apps", "orange");
            // System.out.println("After move up and rename 1");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/orange");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/orange");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);

            avmService.rename(storeName + ":/www/avm-web-apps", "orange", storeName + ":/www", "blue");
            // System.out.println("After move up and rename 2");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/blue");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/orange");
            desc = avmService.lookup(-1, storeName + ":/www/blue");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
        }
        finally
        {
            removeStore(storeName);
        }
    }

    public void testRenamePlainDirectoryIntoLayer()
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            AVMNodeDescriptor desc;
            NodeRef nodeRef;

            avmService.createStore(storeName);

            avmService.createDirectory(storeName + ":/", "www");
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "one", PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "two", PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "three", PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www/avm-web-apps/ROOT", "test");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/test");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "four", PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createSnapshot(storeName, null, null);
            avmService.createStore(storeName + "-a-");
            avmService.createLayeredDirectory(storeName + ":/www", storeName + "-a-:/", "www");

            avmService.rename(storeName + "-a-:/www/avm-web-apps/ROOT", "test", storeName + "-a-:/www/avm-web-apps/ROOT", "banana");
            // System.out.println("In Source");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/test");
            // System.out.println("In Layer");
            // debugPermissions(storeName + "-a-:/");
            // debugPermissions(storeName + "-a-:/www");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT/banana");
        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-a-");
        }
    }

    public void testRenamePlainFileIntoLayer() throws IOException
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            AVMNodeDescriptor desc;
            NodeRef nodeRef;

            avmService.createStore(storeName);

            avmService.createDirectory(storeName + ":/", "www");
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "one", PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www", "avm-web-apps");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "two", PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createDirectory(storeName + ":/www/avm-web-apps", "ROOT");
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "three", PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createFile(storeName + ":/www/avm-web-apps/ROOT", "test").close();
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT/test");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            permissionService.setPermission(nodeRef, "four", PermissionService.ALL_PERMISSIONS, true);
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 4);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps/ROOT");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 3);
            desc = avmService.lookup(-1, storeName + ":/www/avm-web-apps");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 2);
            desc = avmService.lookup(-1, storeName + ":/www");
            nodeRef = AVMNodeConverter.ToNodeRef(-1, desc.getPath());
            assertEquals(permissionService.getSetPermissions(nodeRef).getPermissionEntries().size(), 1);

            avmService.createSnapshot(storeName, null, null);
            avmService.createStore(storeName + "-a-");
            avmService.createLayeredDirectory(storeName + ":/www", storeName + "-a-:/", "www");

            avmService.rename(storeName + "-a-:/www/avm-web-apps/ROOT", "test", storeName + "-a-:/www/avm-web-apps/ROOT", "banana");
            // System.out.println("File In Source");
            // debugPermissions(storeName + ":/");
            // debugPermissions(storeName + ":/www");
            // debugPermissions(storeName + ":/www/avm-web-apps");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + ":/www/avm-web-apps/ROOT/test");
            // System.out.println("File In Layer");
            // debugPermissions(storeName + "-a-:/");
            // debugPermissions(storeName + "-a-:/www");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT");
            // debugPermissions(storeName + "-a-:/www/avm-web-apps/ROOT/banana");
        }
        finally
        {
            removeStore(storeName);
            removeStore(storeName + "-a-");
        }
    }
    
    /**
     * Helper to write a recursive listing of latest version of an AVMStore.
     * @param repoName The name of the AVMStore.
     */
    protected void recursiveList(String store)
    {
        String list = recursiveList(store, -1, true);
        System.out.println(store+":");
        System.out.println(list);
    }
    
    /**
     * Helper to write a recursive listing of an AVMStore at a given version.
     * @param repoName The name of the AVMStore.
     * @param version The version to look under.
     */
    protected String recursiveList(String repoName, int version, boolean followLinks)
    {
        return recursiveList(repoName + ":/", version, 0, followLinks);
    }
    
    /**
     * Recursive list the given path.
     * @param path The path.
     * @param version The version.
     * @param indent The current indent level.
     */
    protected String recursiveList(String path, int version, int indent, boolean followLinks)
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < indent; i++)
        {
            builder.append(' ');
        }
        builder.append(path.substring(path.lastIndexOf('/') + 1));
        builder.append(' ');
        AVMNodeDescriptor desc = avmService.lookup(version, path, true);
        builder.append(desc.toString());
        builder.append(" - ");
        
        AVMNode layeredNode = avmNodeDAO.getByID(desc.getId());
        Acl acl = layeredNode.getAcl();
        builder.append(acl);
        
        builder.append('\n');
        if (desc.getType() == AVMNodeType.PLAIN_DIRECTORY ||
            (desc.getType() == AVMNodeType.LAYERED_DIRECTORY && followLinks))
        {
            String basename = path.endsWith("/") ? path : path + "/";
            Map<String, AVMNodeDescriptor> listing = avmService.getDirectoryListing(version, path);
            for (String name : listing.keySet())
            {
                builder.append(recursiveList(basename + name, version, indent + 2, followLinks));
            }
        }
        return builder.toString();
    }
    
    
    private static final String FILE_NAME = "fileForExport";
    private static final String ROOT = "ROOT";

    private void createStagingWithSnapshots(String storeName) throws IOException
    {
        removeStore(storeName);
        
        avmService.createStore(storeName);
        assertNotNull(avmService.getStore(storeName));
        
        avmService.createDirectory(storeName + ":/", JNDIConstants.DIR_DEFAULT_WWW);
        avmService.createSnapshot(storeName, "first", "first");
        assertNotNull(avmService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW));
        avmService.createDirectory(storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW, JNDIConstants.DIR_DEFAULT_APPBASE);
        avmService.createSnapshot(storeName, "second", "second");
        assertNotNull(avmService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE));
        avmService.createDirectory(storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE, ROOT);
        avmService.createSnapshot(storeName, "third", "third");
        assertNotNull(avmService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT));
        avmService.createFile(storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT, FILE_NAME).close();
        avmService.createSnapshot(storeName, "fourth", "fourth");
        assertNotNull(avmService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT + "/" + FILE_NAME));

    }
    
    private void removeStore(String storeName)
    {
        if (avmService.getStore(storeName) != null)
        {
            avmService.purgeStore(storeName);
        }
        assertNull(avmService.getStore(storeName));
    }
    
    public void testSetInheritParentPermissions() throws Exception
    {
        runAs(AuthenticationUtil.getAdminUserName());
        String storeName = "PermissionsTest-" + getName() + "-" + (new Date().getTime());
        try
        {
            createStagingWithSnapshots(storeName);
            
            AVMNodeDescriptor nodeDescriptor = avmService.lookup(-1, storeName + ":/" + JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE + "/" + ROOT + "/"
                    + FILE_NAME);
            assertNotNull(nodeDescriptor);
            NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, nodeDescriptor.getPath());
            assertNotNull(nodeRef);
            
            permissionService.setInheritParentPermissions(nodeRef, false);
            assertFalse(permissionService.getInheritParentPermissions(nodeRef));
            permissionService.setInheritParentPermissions(nodeRef, true);
            assertTrue(permissionService.getInheritParentPermissions(nodeRef));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
        finally
        {
            removeStore(storeName);
        }
    }
}
