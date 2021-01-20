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
package org.alfresco.repo.security.permissions.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.permissions.AclDAO;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PublicServiceAccessService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

public class AbstractPermissionTest extends TestCase
{
    protected static final String USER2_LEMUR = "lemur";

    protected static final String USER1_ANDY = "andy";
    
    protected static final String USER3_PAUL ="paul";

    protected static ApplicationContext applicationContext = ApplicationContextHelper.getApplicationContext();
    
    protected static final String ROLE_AUTHENTICATED = "ROLE_AUTHENTICATED";

    protected NodeService nodeService;

    protected DictionaryService dictionaryService;

    protected PermissionServiceSPI permissionService;

    protected MutableAuthenticationService authenticationService;
    
    private MutableAuthenticationDao authenticationDAO;

    protected StoreRef testStoreRef;

    protected NodeRef rootNodeRef;

    protected NamespacePrefixResolver namespacePrefixResolver;

    protected ServiceRegistry serviceRegistry;

    protected NodeRef systemNodeRef;

    protected AuthenticationComponent authenticationComponent;

    protected ModelDAO permissionModelDAO;
    
    protected PersonService personService;
    
    protected AuthorityService authorityService;
    
    protected AuthorityDAO authorityDAO;

    protected NodeDAO nodeDAO;
    
    protected AclDAO aclDaoComponent;

    protected RetryingTransactionHelper retryingTransactionHelper;

    private TransactionService transactionService;

    private UserTransaction testTX;

    protected PermissionServiceImpl permissionServiceImpl;

    protected PublicServiceAccessService publicServiceAccessService;

    protected PolicyComponent policyComponent;
    
    protected SiteService siteService;
    
    public AbstractPermissionTest()
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
        
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        dictionaryService = (DictionaryService) applicationContext.getBean(ServiceRegistry.DICTIONARY_SERVICE
                .getLocalName());
        permissionService = (PermissionServiceSPI) applicationContext.getBean("permissionService");
        permissionServiceImpl = (PermissionServiceImpl) applicationContext.getBean("permissionServiceImpl");
        namespacePrefixResolver = (NamespacePrefixResolver) applicationContext
                .getBean(ServiceRegistry.NAMESPACE_SERVICE.getLocalName());
        authenticationService = (MutableAuthenticationService) applicationContext.getBean("authenticationService");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        permissionModelDAO = (ModelDAO) applicationContext.getBean("permissionsModelDAO");
        personService = (PersonService) applicationContext.getBean("personService");
        authorityService = (AuthorityService) applicationContext.getBean("authorityService");
        authorityDAO = (AuthorityDAO) applicationContext.getBean("authorityDAO");
        siteService = (SiteService) applicationContext.getBean("SiteService"); // Big 'S'
        
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        authenticationDAO = (MutableAuthenticationDao) applicationContext.getBean("authenticationDao");
        nodeDAO = (NodeDAO) applicationContext.getBean("nodeDAO");
        aclDaoComponent = (AclDAO) applicationContext.getBean("aclDAO");
        
        publicServiceAccessService = (PublicServiceAccessService) applicationContext.getBean("publicServiceAccessService");
        policyComponent = (PolicyComponent) applicationContext.getBean("policyComponent");
        
        retryingTransactionHelper = (RetryingTransactionHelper) applicationContext.getBean("retryingTransactionHelper");
        
        transactionService = (TransactionService) applicationContext.getBean("transactionComponent");
        
        testTX = transactionService.getUserTransaction();
        testTX.begin();


        testStoreRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.nanoTime());
        rootNodeRef = nodeService.getRootNode(testStoreRef);

        QName children = ContentModel.ASSOC_CHILDREN;
        QName system = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
        QName container = ContentModel.TYPE_CONTAINER;
        QName types = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "people");

        systemNodeRef = nodeService.createNode(rootNodeRef, children, system, container).getChildRef();
        NodeRef typesNodeRef = nodeService.createNode(systemNodeRef, children, types, container).getChildRef();
        Map<QName, Serializable> props = createPersonProperties(USER1_ANDY);
        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();
        props = createPersonProperties(USER2_LEMUR);
        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();

        // create an authentication object e.g. the user
        if(authenticationDAO.userExists(USER1_ANDY))
        {
            authenticationService.deleteAuthentication(USER1_ANDY);
        }
        authenticationService.createAuthentication(USER1_ANDY, USER1_ANDY.toCharArray());

        if(authenticationDAO.userExists(USER2_LEMUR))
        {
            authenticationService.deleteAuthentication(USER2_LEMUR);
        }
        authenticationService.createAuthentication(USER2_LEMUR, USER2_LEMUR.toCharArray());
        
        if(authenticationDAO.userExists(USER3_PAUL))
        {
            authenticationService.deleteAuthentication(USER3_PAUL);
        }
        authenticationService.createAuthentication(USER3_PAUL, USER3_PAUL.toCharArray());
        
        if(authenticationDAO.userExists(AuthenticationUtil.getAdminUserName()))
        {
            authenticationService.deleteAuthentication(AuthenticationUtil.getAdminUserName());
        }
        authenticationService.createAuthentication(AuthenticationUtil.getAdminUserName(), "admin".toCharArray());
        
        authenticationComponent.clearCurrentSecurityContext();
        
        assertTrue(permissionServiceImpl.getAnyDenyDenies());
    }

    @Override
    protected void tearDown() throws Exception
    {
        try
        {
            testTX.rollback();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
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

}
