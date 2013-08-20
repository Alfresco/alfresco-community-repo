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
package org.alfresco.repo.security.permissions.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.permissions.ACEType;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.AccessControlListProperties;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.permissions.SimpleAccessControlEntry;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.EqualsHelper;
import org.springframework.context.ApplicationContext;

public class AclDaoComponentTest extends TestCase
{
    private static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    
    protected NodeService nodeService;

    protected DictionaryService dictionaryService;

    protected PermissionServiceSPI permissionService;

    protected MutableAuthenticationService authenticationService;
    
    private MutableAuthenticationDao authenticationDAO;

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

    public AclDaoComponentTest()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public void setUp() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        aclDaoComponent = (AclDAO) applicationContext.getBean("aclDAO");
        
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        dictionaryService = (DictionaryService) applicationContext.getBean(ServiceRegistry.DICTIONARY_SERVICE
                .getLocalName());
        permissionService = (PermissionServiceSPI) applicationContext.getBean("permissionService");
        namespacePrefixResolver = (NamespacePrefixResolver) applicationContext
                .getBean(ServiceRegistry.NAMESPACE_SERVICE.getLocalName());
        authenticationService = (MutableAuthenticationService) applicationContext.getBean("authenticationService");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        permissionModelDAO = (ModelDAO) applicationContext.getBean("permissionsModelDAO");
        personService = (PersonService) applicationContext.getBean("personService");
        authorityService = (AuthorityService) applicationContext.getBean("authorityService");
        
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
        if(authenticationDAO.userExists("andy"))
        {
            authenticationService.deleteAuthentication("andy");
        }
        authenticationService.createAuthentication("andy", "andy".toCharArray());

        if(authenticationDAO.userExists("lemur"))
        {
            authenticationService.deleteAuthentication("lemur");
        }
        authenticationService.createAuthentication("lemur", "lemur".toCharArray());
        
        if(authenticationDAO.userExists(AuthenticationUtil.getAdminUserName()))
        {
            authenticationService.deleteAuthentication(AuthenticationUtil.getAdminUserName());
        }
        authenticationService.createAuthentication(AuthenticationUtil.getAdminUserName(), "admin".toCharArray());
        
        authenticationComponent.clearCurrentSecurityContext();
    }

    protected void tearDown() throws Exception
    {

        if ((testTX.getStatus() == Status.STATUS_ACTIVE) || (testTX.getStatus() == Status.STATUS_MARKED_ROLLBACK))
        {
            testTX.rollback();
        }
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
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
    
    public void testCreateDefault()
    {
        // Create default ACL (type=DEFINING, inherits=true, versioned=false)
        Long id = aclDaoComponent.createAccessControlList();
        
        AccessControlListProperties aclProps = aclDaoComponent.getAccessControlListProperties(id);
        assertEquals(aclProps.getAclType(), ACLType.DEFINING);
        assertEquals(aclProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclProps.getInherits(), Boolean.TRUE);
        assertEquals(aclProps.isVersioned(), Boolean.FALSE);
    }
    
    public void testCreateDefining()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setVersioned(true);
        
        AccessControlListProperties aclProps = aclDaoComponent.createAccessControlList(properties);
        assertEquals(aclProps.getAclType(), ACLType.DEFINING);
        assertEquals(aclProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclProps.getInherits(), Boolean.TRUE);
        assertEquals(aclDaoComponent.getAccessControlListProperties(aclProps.getId()), aclProps);
    }
    
    public void testCreateShared()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setVersioned(true);
        
        AccessControlListProperties aclProps = aclDaoComponent.createAccessControlList(properties);
        assertEquals(aclProps.getAclType(), ACLType.DEFINING);
        assertEquals(aclProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclProps.getInherits(), Boolean.TRUE);
        assertEquals(aclDaoComponent.getAccessControlListProperties(aclProps.getId()), aclProps);
        
        Long shared = aclDaoComponent.getInheritedAccessControlList(aclProps.getId());
        AccessControlListProperties sharedProps = aclDaoComponent.getAccessControlListProperties(shared);
        assertEquals(sharedProps.getAclType(), ACLType.SHARED);
        assertEquals(sharedProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(sharedProps.getInherits(), Boolean.TRUE);
        assertEquals(aclDaoComponent.getInheritedAccessControlList(aclProps.getId()), shared);
    }
    
    public void testCreateOld()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.OLD);
        properties.setVersioned(false);
        
        AccessControlListProperties aclProps = aclDaoComponent.createAccessControlList(properties);
        assertEquals(aclProps.getAclType(), ACLType.OLD);
        assertEquals(aclProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclProps.getInherits(), Boolean.TRUE);
        assertEquals(aclDaoComponent.getAccessControlListProperties(aclProps.getId()), aclProps);
        assertEquals(aclDaoComponent.getInheritedAccessControlList(aclProps.getId()), null);
    }
    
    public void testFixed()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.FIXED);
        properties.setVersioned(true);
        
        AccessControlListProperties aclProps = aclDaoComponent.createAccessControlList(properties);
        Long id = aclProps.getId();
        assertEquals(aclProps.getAclType(), ACLType.FIXED);
        assertEquals(aclProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclProps.getInherits(), Boolean.TRUE);
        assertEquals(aclDaoComponent.getAccessControlListProperties(aclProps.getId()), aclProps);
        assertEquals(aclDaoComponent.getInheritedAccessControlList(id), id);
    }
    
    public void testGlobal()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.GLOBAL);
        properties.setVersioned(false);
        
        AccessControlListProperties aclProps = aclDaoComponent.createAccessControlList(properties);
        Long id = aclProps.getId();
        assertEquals(aclProps.getAclType(), ACLType.GLOBAL);
        assertEquals(aclProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclProps.getInherits(), Boolean.TRUE);
        assertEquals(aclDaoComponent.getAccessControlListProperties(aclProps.getId()), aclProps);
        assertEquals(aclDaoComponent.getInheritedAccessControlList(id), id);
    }
    
    public void testSimpleInheritFromDefining()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setVersioned(true);
        
        AccessControlListProperties aclProps = aclDaoComponent.createAccessControlList(properties);
        long id = aclProps.getId();
        assertEquals(aclProps.getAclType(), ACLType.DEFINING);
        assertEquals(aclProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclProps.getInherits(), Boolean.TRUE);
        assertEquals(aclDaoComponent.getAccessControlListProperties(id), aclProps);
        
        SimpleAccessControlEntry ace1 = new SimpleAccessControlEntry();
        ace1.setAccessStatus(AccessStatus.ALLOWED);
        ace1.setAceType(ACEType.ALL);
        ace1.setAuthority("andy");
        ace1.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Read"));
        ace1.setPosition(null);
        aclDaoComponent.setAccessControlEntry(id, ace1);
        
        AccessControlList defined = aclDaoComponent.getAccessControlList(id);
        assertEquals(defined.getProperties().getAclType(), ACLType.DEFINING);
        assertEquals(defined.getProperties().getAclVersion(), Long.valueOf(1l));
        assertEquals(defined.getProperties().getInherits(), Boolean.TRUE);
        assertEquals(defined.getEntries().size(), 1);
        assertTrue(hasAce(defined.getEntries(), ace1, 0));
        
        
        Long sharedId = aclDaoComponent.getInheritedAccessControlList(id);
        AccessControlListProperties sharedProps = aclDaoComponent.getAccessControlListProperties(sharedId);
        assertEquals(sharedProps.getAclType(), ACLType.SHARED);
        assertEquals(sharedProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(sharedProps.getInherits(), Boolean.TRUE);
        assertEquals(aclDaoComponent.getInheritedAccessControlList(id), sharedId);
        
        AccessControlList shared = aclDaoComponent.getAccessControlList(sharedId);
        assertEquals(shared.getProperties().getAclType(), ACLType.SHARED);
        assertEquals(shared.getProperties().getAclVersion(), Long.valueOf(1l));
        assertEquals(shared.getProperties().getInherits(), Boolean.TRUE);
        assertEquals(shared.getEntries().size(), 1);
        assertTrue(hasAce(shared.getEntries(), ace1, 1));
        
        SimpleAccessControlEntry ace2 = new SimpleAccessControlEntry();
        ace2.setAccessStatus(AccessStatus.ALLOWED);
        ace2.setAceType(ACEType.ALL);
        ace2.setAuthority("paul");
        ace2.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Write"));
        ace2.setPosition(null);
        aclDaoComponent.setAccessControlEntry(id, ace2);
        
        defined = aclDaoComponent.getAccessControlList(id);
        assertEquals(defined.getProperties().getAclType(), ACLType.DEFINING);
        assertEquals(defined.getProperties().getAclVersion(), Long.valueOf(1l));
        assertEquals(defined.getProperties().getInherits(), Boolean.TRUE);
        assertEquals(defined.getEntries().size(), 2);
        assertTrue(hasAce(defined.getEntries(), ace1, 0));
        assertTrue(hasAce(defined.getEntries(), ace2, 0));
        
        sharedId = aclDaoComponent.getInheritedAccessControlList(id);
        shared = aclDaoComponent.getAccessControlList(sharedId);
        assertEquals(shared.getProperties().getAclType(), ACLType.SHARED);
        assertEquals(shared.getProperties().getAclVersion(), Long.valueOf(1l));
        assertEquals(shared.getProperties().getInherits(), Boolean.TRUE);
        assertEquals(shared.getEntries().size(), 2);
        assertTrue(hasAce(shared.getEntries(), ace1, 1));
        assertTrue(hasAce(shared.getEntries(), ace2, 1));
    }
    
    public void testInheritanceChainDefShared()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setVersioned(true);
        Long def1 = aclDaoComponent.createAccessControlList(properties).getId();
        Long shared1 = aclDaoComponent.getInheritedAccessControlList(def1);
        Long def2 = aclDaoComponent.createAccessControlList(properties).getId();
        aclDaoComponent.mergeInheritedAccessControlList(shared1, def2);
        Long shared2 = aclDaoComponent.getInheritedAccessControlList(def2);
        Long def3 = aclDaoComponent.createAccessControlList(properties).getId();
        aclDaoComponent.mergeInheritedAccessControlList(shared2, def3);
        Long shared3 = aclDaoComponent.getInheritedAccessControlList(def3);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 0);
        
        SimpleAccessControlEntry ace1 = new SimpleAccessControlEntry();
        ace1.setAccessStatus(AccessStatus.ALLOWED);
        ace1.setAceType(ACEType.ALL);
        ace1.setAuthority("andy");
        ace1.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Write"));
        ace1.setPosition(null);
        aclDaoComponent.setAccessControlEntry(def1, ace1);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        
        
        Long def4 = aclDaoComponent.createAccessControlList(properties).getId();
        aclDaoComponent.mergeInheritedAccessControlList(shared3, def4);
        Long shared4 = aclDaoComponent.getInheritedAccessControlList(def4);
        
        Long def5 = aclDaoComponent.createAccessControlList(properties).getId();
        aclDaoComponent.mergeInheritedAccessControlList(shared4, def5);
        Long shared5 = aclDaoComponent.getInheritedAccessControlList(def5);
        
        Long def6_1 = aclDaoComponent.createAccessControlList(properties).getId();
        aclDaoComponent.mergeInheritedAccessControlList(shared5, def6_1);
        Long shared6_1 = aclDaoComponent.getInheritedAccessControlList(def6_1);
        
        Long def6_2 = aclDaoComponent.createAccessControlList(properties).getId();
        aclDaoComponent.mergeInheritedAccessControlList(shared5, def6_2);
        Long shared6_2 = aclDaoComponent.getInheritedAccessControlList(def6_2);
        
        Long def6_3 = aclDaoComponent.createAccessControlList(properties).getId();
        aclDaoComponent.mergeInheritedAccessControlList(shared5, def6_3);
        Long shared6_3 = aclDaoComponent.getInheritedAccessControlList(def6_3);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace1, 6));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace1, 7));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 8));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 9));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 10));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 11));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 10));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 11));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 10));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 11));
       
        
     
        SimpleAccessControlEntry ace2 = new SimpleAccessControlEntry();
        ace2.setAccessStatus(AccessStatus.ALLOWED);
        ace2.setAceType(ACEType.ALL);
        ace2.setAuthority("paul");
        ace2.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Write"));
        ace2.setPosition(null);
        aclDaoComponent.setAccessControlEntry(def4, ace2);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace1, 6));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace1, 7));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 8));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 9));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.disableInheritance(def4, false);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.enableInheritance(def4, shared3);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace1, 6));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace1, 7));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 8));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 9));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.enableInheritance(def4, shared2);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace1, 4));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace1, 5));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 6));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 7));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 8));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 9));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 8));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 9));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 8));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 9));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.disableInheritance(def4, true);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace1, 0));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace1, 1));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 2));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 3));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 4));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 5));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 4));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 5));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 4));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 5));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.deleteAccessControlEntries(def4, ace1);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 1);;
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.deleteLocalAccessControlEntries(def4);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(),0);
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 0);
        
        aclDaoComponent.enableInheritance(def4, shared3);
        aclDaoComponent.setAccessControlEntry(def4, ace2);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace1, 6));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace1, 7));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 8));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 9));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.deleteLocalAccessControlEntries(def4);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace1, 6));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace1, 7));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 8));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 9));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 10));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 11));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 10));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 11));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 10));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 11));
        
        aclDaoComponent.setAccessControlEntry(def4, ace2);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace1, 6));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace1, 7));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 8));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 9));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.deleteInheritedAccessControlEntries(def4);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.deleteLocalAccessControlEntries(def4);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 0);
        
        aclDaoComponent.enableInheritance(def4, shared3);
        aclDaoComponent.setAccessControlEntry(def4, ace2);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace1, 6));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def4).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared4).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace1, 7));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared4).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 8));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 9));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace2, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace2, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 10));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace2, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 11));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace2, 5));
        
        aclDaoComponent.deleteAccessControlList(def4);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def5).getEntries(), ace1, 6));
        assertEquals(aclDaoComponent.getAccessControlList(shared5).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared5).getEntries(), ace1, 7));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 8));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 9));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 8));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 9));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 8));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 9));
        
        aclDaoComponent.deleteAccessControlList(def5);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 6));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 7));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 6));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 7));
        assertEquals(aclDaoComponent.getAccessControlList(def6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_3).getEntries(), ace1, 6));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_3).getEntries(), ace1, 7));
        
        aclDaoComponent.deleteAccessControlList(def6_3);
        
        assertEquals(aclDaoComponent.getAccessControlList(def1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def3).getEntries(), ace1, 4));
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared3).getEntries(), ace1, 5));
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_1).getEntries(), ace1, 6));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_1).getEntries(), ace1, 7));
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(def6_2).getEntries(), ace1, 6));
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared6_2).getEntries(), ace1, 7));
        
        aclDaoComponent.deleteAccessControlList(def1);
        
        assertEquals(aclDaoComponent.getAccessControlList(def2).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def3).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared3).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def6_1).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared6_1).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(def6_2).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(shared6_2).getEntries().size(), 0);
        
    }
    
    public void testDeleteAuthority()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setVersioned(true);
        Long defined = aclDaoComponent.createAccessControlList(properties).getId();
        
        SimpleAccessControlEntry ace1 = new SimpleAccessControlEntry();
        ace1.setAccessStatus(AccessStatus.ALLOWED);
        ace1.setAceType(ACEType.ALL);
        ace1.setAuthority("offski");
        ace1.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "P1"));
        ace1.setPosition(null);
          
        SimpleAccessControlEntry ace2 = new SimpleAccessControlEntry();
        ace2.setAccessStatus(AccessStatus.ALLOWED);
        ace2.setAceType(ACEType.ALL);
        ace2.setAuthority("offski");
        ace2.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "P2"));
        ace2.setPosition(null);
        
        SimpleAccessControlEntry ace3 = new SimpleAccessControlEntry();
        ace3.setAccessStatus(AccessStatus.ALLOWED);
        ace3.setAceType(ACEType.ALL);
        ace3.setAuthority("keepski");
        ace3.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "P3"));
        ace3.setPosition(null);
        
        Long shared = aclDaoComponent.getInheritedAccessControlList(defined);
        
        properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.FIXED);
        properties.setVersioned(true);
        Long fixed = aclDaoComponent.createAccessControlList(properties).getId();
        
        properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.GLOBAL);
        properties.setVersioned(true);
        Long global = aclDaoComponent.createAccessControlList(properties).getId();
        
        properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.OLD);
        properties.setVersioned(false);
        Long old = aclDaoComponent.createAccessControlList(properties).getId();
        
     
        aclDaoComponent.setAccessControlEntry(defined, ace1);
        aclDaoComponent.setAccessControlEntry(defined, ace2);
        aclDaoComponent.setAccessControlEntry(defined, ace3);
        
        aclDaoComponent.setAccessControlEntry(fixed, ace1);
        aclDaoComponent.setAccessControlEntry(fixed, ace2);
        aclDaoComponent.setAccessControlEntry(fixed, ace3);
        
        aclDaoComponent.setAccessControlEntry(global, ace1);
        aclDaoComponent.setAccessControlEntry(global, ace2);
        aclDaoComponent.setAccessControlEntry(global, ace3);
        
        aclDaoComponent.setAccessControlEntry(old, ace1);
        aclDaoComponent.setAccessControlEntry(old, ace2);
        aclDaoComponent.setAccessControlEntry(old, ace3);
        
        assertEquals(aclDaoComponent.getAccessControlList(defined).getEntries().size(), 3);
        assertEquals(aclDaoComponent.getAccessControlList(shared).getEntries().size(), 3);
        assertEquals(aclDaoComponent.getAccessControlList(fixed).getEntries().size(), 3);
        assertEquals(aclDaoComponent.getAccessControlList(global).getEntries().size(), 3);
        assertEquals(aclDaoComponent.getAccessControlList(old).getEntries().size(), 3);
        
        aclDaoComponent.deleteAccessControlEntries("offski");
        
        assertEquals(aclDaoComponent.getAccessControlList(defined).getEntries().size(), 1);
        assertEquals(aclDaoComponent.getAccessControlList(shared).getEntries().size(), 1);
        assertEquals(aclDaoComponent.getAccessControlList(fixed).getEntries().size(), 1);
        assertEquals(aclDaoComponent.getAccessControlList(global).getEntries().size(), 1);
        assertEquals(aclDaoComponent.getAccessControlList(old).getEntries().size(), 1);
        
    }
    
    public void testSimpleCow() throws Exception
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setVersioned(true);
        Long id = aclDaoComponent.createAccessControlList(properties).getId();
        
        AccessControlListProperties aclProps = aclDaoComponent.getAccessControlListProperties(id);
        assertEquals(aclProps.getAclType(), ACLType.DEFINING);
        assertEquals(aclProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclProps.getInherits(), Boolean.TRUE);
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        AccessControlListProperties aclPropsBefore = aclDaoComponent.getAccessControlListProperties(id);
        assertEquals(aclPropsBefore.getAclType(), ACLType.DEFINING);
        assertEquals(aclPropsBefore.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclPropsBefore.getInherits(), Boolean.TRUE);
        
        SimpleAccessControlEntry ace1 = new SimpleAccessControlEntry();
        ace1.setAccessStatus(AccessStatus.ALLOWED);
        ace1.setAceType(ACEType.ALL);
        ace1.setAuthority("andy");
        ace1.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Read"));
        ace1.setPosition(null);
        List<AclChange> changes = aclDaoComponent.setAccessControlEntry(id, ace1);
        assertEquals(changes.size(), 1);
        assertEquals(changes.get(0).getBefore(), id);
        assertFalse(changes.get(0).getBefore().equals(changes.get(0).getAfter()));
        
        aclPropsBefore = aclDaoComponent.getAccessControlListProperties(changes.get(0).getBefore());
        assertEquals(aclPropsBefore.getAclType(), ACLType.DEFINING);
        assertEquals(aclPropsBefore.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclPropsBefore.getInherits(), Boolean.TRUE);
        assertEquals(aclPropsBefore.isLatest(), Boolean.FALSE);
        assertEquals(aclPropsBefore.isVersioned(), Boolean.TRUE);
        
        AccessControlListProperties aclPropsAfter = aclDaoComponent.getAccessControlListProperties(changes.get(0).getAfter());
        assertEquals(aclPropsAfter.getAclType(), aclPropsBefore.getAclType());
        assertEquals(aclPropsAfter.getAclVersion(), Long.valueOf(aclPropsBefore.getAclVersion()+1));
        assertEquals(aclPropsAfter.getInherits(), aclPropsBefore.getInherits());
        assertEquals(aclPropsAfter.getAclId(), aclPropsBefore.getAclId());
        assertEquals(aclPropsAfter.isVersioned(), aclPropsBefore.isVersioned());
        assertEquals(aclPropsAfter.isLatest(), Boolean.TRUE);   
        
        assertEquals(aclDaoComponent.getAccessControlList(changes.get(0).getBefore()).getEntries().size(), 0);
        assertEquals(aclDaoComponent.getAccessControlList(changes.get(0).getAfter()).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(changes.get(0).getAfter()).getEntries(), ace1, 0));
        
    }
    
    public void testSimpleCowHerd1() throws Exception
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setVersioned(true);
        Long i_1 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1 = aclDaoComponent.getInheritedAccessControlList(i_1);
        
        Long i_1_2 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_2 = aclDaoComponent.getInheritedAccessControlList(i_1_2);
        aclDaoComponent.mergeInheritedAccessControlList(s_1, i_1_2);
        Long i_1_3 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_3 = aclDaoComponent.getInheritedAccessControlList(i_1_3);
        aclDaoComponent.mergeInheritedAccessControlList(s_1, i_1_3);
        
        Long i_1_2_4 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_2_4 = aclDaoComponent.getInheritedAccessControlList(i_1_2_4);
        aclDaoComponent.mergeInheritedAccessControlList(s_1_2, i_1_2_4);
        Long i_1_2_4_5 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_2_4_5 = aclDaoComponent.getInheritedAccessControlList(i_1_2_4_5);
        aclDaoComponent.mergeInheritedAccessControlList(s_1_2_4, i_1_2_4_5);
        
        Long i_1_3_6 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_3_6 = aclDaoComponent.getInheritedAccessControlList(i_1_3_6);
        aclDaoComponent.mergeInheritedAccessControlList(s_1_3, i_1_3_6);
        Long i_1_3_6_7 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_3_6_7 = aclDaoComponent.getInheritedAccessControlList(i_1_3_6_7);
        aclDaoComponent.mergeInheritedAccessControlList(s_1_3_6, i_1_3_6_7);
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        SimpleAccessControlEntry ace1 = new SimpleAccessControlEntry();
        ace1.setAccessStatus(AccessStatus.ALLOWED);
        ace1.setAceType(ACEType.ALL);
        ace1.setAuthority("andy");
        ace1.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Read"));
        ace1.setPosition(null);
        List<AclChange> changes = aclDaoComponent.setAccessControlEntry(i_1, ace1);
        
        // All should have changed
        
        Set<Long> changed = new HashSet<Long>(changes.size()); 
        for(AclChange change : changes)
        {
            changed.add(change.getBefore());
            assertFalse(change.getBefore().equals(change.getAfter()));
        }
        
        assertTrue(changed.contains(i_1));
        assertTrue(changed.contains(s_1));
        assertTrue(changed.contains(i_1_2));
        assertTrue(changed.contains(s_1_2));
        assertTrue(changed.contains(i_1_3));
        assertTrue(changed.contains(s_1_3));
        assertTrue(changed.contains(i_1_2_4));
        assertTrue(changed.contains(s_1_2_4));
        assertTrue(changed.contains(i_1_2_4_5));
        assertTrue(changed.contains(s_1_2_4_5));
        assertTrue(changed.contains(i_1_3_6));
        assertTrue(changed.contains(s_1_3_6));
        assertTrue(changed.contains(i_1_3_6_7));
        assertTrue(changed.contains(s_1_3_6_7));
    }
    
    
    public void testSimpleCowHerd2() throws Exception
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setVersioned(true);
        Long i_1 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1 = aclDaoComponent.getInheritedAccessControlList(i_1);
        
        Long i_1_2 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_2 = aclDaoComponent.getInheritedAccessControlList(i_1_2);
        aclDaoComponent.mergeInheritedAccessControlList(s_1, i_1_2);
        Long i_1_3 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_3 = aclDaoComponent.getInheritedAccessControlList(i_1_3);
        aclDaoComponent.mergeInheritedAccessControlList(s_1, i_1_3);
        
        Long i_1_2_4 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_2_4 = aclDaoComponent.getInheritedAccessControlList(i_1_2_4);
        aclDaoComponent.mergeInheritedAccessControlList(s_1_2, i_1_2_4);
        Long i_1_2_4_5 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_2_4_5 = aclDaoComponent.getInheritedAccessControlList(i_1_2_4_5);
        assertNotNull(s_1_2_4_5);
        aclDaoComponent.mergeInheritedAccessControlList(s_1_2_4, i_1_2_4_5);
        
        Long i_1_3_6 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_3_6 = aclDaoComponent.getInheritedAccessControlList(i_1_3_6);
        aclDaoComponent.mergeInheritedAccessControlList(s_1_3, i_1_3_6);
        Long i_1_3_6_7 = aclDaoComponent.createAccessControlList(properties).getId();
        Long s_1_3_6_7 = aclDaoComponent.getInheritedAccessControlList(i_1_3_6_7);
        aclDaoComponent.mergeInheritedAccessControlList(s_1_3_6, i_1_3_6_7);
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        SimpleAccessControlEntry ace1 = new SimpleAccessControlEntry();
        ace1.setAccessStatus(AccessStatus.ALLOWED);
        ace1.setAceType(ACEType.ALL);
        ace1.setAuthority("andy");
        ace1.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Read"));
        ace1.setPosition(null);
        List<AclChange> changes = aclDaoComponent.setAccessControlEntry(i_1_3, ace1);
        
        // All should have changed
        
        Set<Long> changed = new HashSet<Long>(changes.size()); 
        for(AclChange change : changes)
        {
            changed.add(change.getBefore());
            assertFalse(change.getBefore().equals(change.getAfter()));
        }
        
        assertTrue(changed.contains(i_1_3));
        assertTrue(changed.contains(s_1_3));
        assertTrue(changed.contains(i_1_3_6));
        assertTrue(changed.contains(s_1_3_6));
        assertTrue(changed.contains(i_1_3_6_7));
        assertTrue(changed.contains(s_1_3_6_7));
    }
    
    
    public void testOldDoesNotCow() throws Exception
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.OLD);
        properties.setVersioned(false);
        Long id = aclDaoComponent.createAccessControlList(properties).getId();
        
        AccessControlListProperties aclProps = aclDaoComponent.getAccessControlListProperties(id);
        assertEquals(aclProps.getAclType(), ACLType.OLD);
        assertEquals(aclProps.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclProps.getInherits(), Boolean.TRUE);
        
        testTX.commit();
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();
        
        AccessControlListProperties aclPropsBefore = aclDaoComponent.getAccessControlListProperties(id);
        assertEquals(aclPropsBefore.getAclType(), ACLType.OLD);
        assertEquals(aclPropsBefore.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclPropsBefore.getInherits(), Boolean.TRUE);
        
        SimpleAccessControlEntry ace1 = new SimpleAccessControlEntry();
        ace1.setAccessStatus(AccessStatus.ALLOWED);
        ace1.setAceType(ACEType.ALL);
        ace1.setAuthority("andy");
        ace1.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Read"));
        ace1.setPosition(null);
        List<AclChange> changes = aclDaoComponent.setAccessControlEntry(id, ace1);
        assertEquals(changes.size(), 1);
        assertEquals(changes.get(0).getBefore(), id);
        assertTrue(changes.get(0).getBefore().equals(changes.get(0).getAfter()));
        
        aclPropsBefore = aclDaoComponent.getAccessControlListProperties(changes.get(0).getBefore());
        assertEquals(aclPropsBefore.getAclType(), ACLType.OLD);
        assertEquals(aclPropsBefore.getAclVersion(), Long.valueOf(1l));
        assertEquals(aclPropsBefore.getInherits(), Boolean.TRUE);
        assertEquals(aclPropsBefore.isLatest(), Boolean.TRUE);
        assertEquals(aclPropsBefore.isVersioned(), Boolean.FALSE);
        
        assertEquals(aclDaoComponent.getAccessControlList(changes.get(0).getBefore()).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(changes.get(0).getBefore()).getEntries(), ace1, 0));
        
    }
    
    public void testAddSimilar()
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setVersioned(false);
        Long id1 = aclDaoComponent.createAccessControlList(properties).getId();
        Long shared1 = aclDaoComponent.getInheritedAccessControlList(id1);
        Long id2 = aclDaoComponent.createAccessControlList(properties).getId();
        Long shared2 = aclDaoComponent.getInheritedAccessControlList(id2);
        aclDaoComponent.mergeInheritedAccessControlList(shared1, id2);
        
        SimpleAccessControlEntry ace1 = new SimpleAccessControlEntry();
        ace1.setAccessStatus(AccessStatus.ALLOWED);
        ace1.setAceType(ACEType.ALL);
        ace1.setAuthority("andy");
        ace1.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Read"));
        ace1.setPosition(null);
        
        SimpleAccessControlEntry ace2 = new SimpleAccessControlEntry();
        ace2.setAccessStatus(AccessStatus.ALLOWED);
        ace2.setAceType(ACEType.ALL);
        ace2.setAuthority("andy");
        ace2.setPermission(new SimplePermissionReference(QName.createQName("uri", "local"), "Read"));
        ace2.setPosition(null);
        
        aclDaoComponent.setAccessControlEntry(id1, ace1);
        aclDaoComponent.setAccessControlEntry(id2, ace1);
        
        assertEquals(aclDaoComponent.getAccessControlList(id1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(id2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace1, 0));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 1));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        
        aclDaoComponent.setAccessControlEntry(id1, ace2);
        
        assertEquals(aclDaoComponent.getAccessControlList(id1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id1).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(id2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace2, 0));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace2, 1));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
        
        aclDaoComponent.setAccessControlEntry(id1, ace1);
        aclDaoComponent.setAccessControlEntry(id2, ace2);
        
        assertEquals(aclDaoComponent.getAccessControlList(id1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(id2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace1, 0));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 1));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace2, 3));
        
        aclDaoComponent.setAccessControlEntry(id1, ace2);
        
        assertEquals(aclDaoComponent.getAccessControlList(id1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id1).getEntries(), ace2, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace2, 1));
        assertEquals(aclDaoComponent.getAccessControlList(id2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace2, 0));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace2, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace2, 1));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace2, 3));
        
        aclDaoComponent.setAccessControlEntry(id1, ace1);
        aclDaoComponent.setAccessControlEntry(id2, ace1);
        
        assertEquals(aclDaoComponent.getAccessControlList(id1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id1).getEntries(), ace1, 0));
        assertEquals(aclDaoComponent.getAccessControlList(shared1).getEntries().size(), 1);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared1).getEntries(), ace1, 1));
        assertEquals(aclDaoComponent.getAccessControlList(id2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace1, 0));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(id2).getEntries(), ace1, 2));
        assertEquals(aclDaoComponent.getAccessControlList(shared2).getEntries().size(), 2);
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 1));
        assertTrue(hasAce(aclDaoComponent.getAccessControlList(shared2).getEntries(), ace1, 3));
    }
    
    private boolean hasAce(List<AccessControlEntry> aces, AccessControlEntry ace, int position)
    {
        for(AccessControlEntry test : aces)
        {
            if(!EqualsHelper.nullSafeEquals(test.getAccessStatus(), ace.getAccessStatus()))
            {
                continue;
            }
            if(!EqualsHelper.nullSafeEquals(test.getAceType(), ace.getAceType()))
            {
                continue;
            }
            if(!EqualsHelper.nullSafeEquals(test.getAuthority(), ace.getAuthority()))
            {
                continue;
            }
            if(!EqualsHelper.nullSafeEquals(test.getContext(), ace.getContext()))
            {
                continue;
            }
            if(!EqualsHelper.nullSafeEquals(test.getPermission(), ace.getPermission()))
            {
                continue;
            }
            if(!EqualsHelper.nullSafeEquals(test.getPosition(), Integer.valueOf(position)))
            {
                continue;
            }
            return true;
            
        }
        return false;
    }
}
