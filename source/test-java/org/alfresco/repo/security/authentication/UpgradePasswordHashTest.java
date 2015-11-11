/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

@SuppressWarnings("unchecked")
@Category(OwnJVMTestsCategory.class)
public class UpgradePasswordHashTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private UserTransaction userTransaction;
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private RepositoryAuthenticationDao repositoryAuthenticationDao;
    private CompositePasswordEncoder compositePasswordEncoder;
    private UpgradePasswordHashWorker upgradePasswordHashWorker;
    private List<String> testUserNames;
    private List<NodeRef> testUsers;
    public UpgradePasswordHashTest()
    {
        super();
    }

    public UpgradePasswordHashTest(String arg0)
    {
        super(arg0);
    }

    public void setUp() throws Exception
    {
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_NONE)
        {
            throw new AlfrescoRuntimeException(
                    "A previous tests did not clean up transaction: " +
                    AlfrescoTransactionSupport.getTransactionId());
        }
        
        serviceRegistry = (ServiceRegistry)ctx.getBean("ServiceRegistry");

        SimpleCache<String, RepositoryAuthenticationDao.CacheEntry> authenticationCache = (SimpleCache<String, RepositoryAuthenticationDao.CacheEntry>) ctx.getBean("authenticationCache");
        SimpleCache<String, NodeRef> immutableSingletonCache = (SimpleCache<String, NodeRef>) ctx.getBean("immutableSingletonCache");
        TenantService tenantService = (TenantService) ctx.getBean("tenantService");
        compositePasswordEncoder = (CompositePasswordEncoder) ctx.getBean("compositePasswordEncoder");
        PolicyComponent policyComponent = (PolicyComponent) ctx.getBean("policyComponent");

        repositoryAuthenticationDao = new RepositoryAuthenticationDao();
        repositoryAuthenticationDao.setTransactionService(serviceRegistry.getTransactionService());
        repositoryAuthenticationDao.setAuthorityService(serviceRegistry.getAuthorityService());
        repositoryAuthenticationDao.setTenantService(tenantService);
        repositoryAuthenticationDao.setNodeService(serviceRegistry.getNodeService());
        repositoryAuthenticationDao.setNamespaceService(serviceRegistry.getNamespaceService());
        repositoryAuthenticationDao.setCompositePasswordEncoder(compositePasswordEncoder);
        repositoryAuthenticationDao.setPolicyComponent(policyComponent);
        repositoryAuthenticationDao.setAuthenticationCache(authenticationCache);
        repositoryAuthenticationDao.setSingletonCache(immutableSingletonCache);

        upgradePasswordHashWorker = (UpgradePasswordHashWorker)ctx.getBean("upgradePasswordHashWorker");
        nodeService = serviceRegistry.getNodeService();
        userTransaction = serviceRegistry.getTransactionService().getUserTransaction();
        userTransaction.begin();
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        createTestUsers("md4");
        
        userTransaction.commit();
    }
    
    protected void createTestUsers(String encoding) throws Exception
    {

        // create 50 users and change their properties back to how
        // they would have been pre-upgrade.
        testUsers = new ArrayList<NodeRef>(50);
        testUsers.add(createUser("king"+encoding, "king".toCharArray(), encoding));
        testUsers.add(createUser("kin" +encoding, "Kong".toCharArray(), encoding));
        testUsers.add(createUser("ding"+encoding, "dong".toCharArray(), encoding));
        testUsers.add(createUser("ping"+encoding, "pong".toCharArray(),encoding));
        testUsers.add(createUser("pin" +encoding, "pop".toCharArray(), encoding));
    }

    private NodeRef createUser(String caseSensitiveUserName, char[] password, String encoding)
    {
        try
        {
            repositoryAuthenticationDao.createUser(caseSensitiveUserName,password);
        } catch (AuthenticationException e)
        {
           if (!e.getMessage().contains("User already exists")) { throw e; }
        }

        NodeRef userNodeRef = repositoryAuthenticationDao.getUserOrNull(caseSensitiveUserName);
        if (userNodeRef == null)
        {
            throw new AuthenticationException("User name does not exist: " + caseSensitiveUserName);
        }
        Map<QName, Serializable> properties = nodeService.getProperties(userNodeRef);
        properties.remove(ContentModel.PROP_PASSWORD_HASH);
        properties.remove(ContentModel.PROP_HASH_INDICATOR);
        properties.remove(ContentModel.PROP_PASSWORD);
        properties.remove(ContentModel.PROP_PASSWORD_SHA256);
        properties.put(ContentModel.PROP_PASSWORD,  compositePasswordEncoder.encode(encoding,new String(password), null));
        nodeService.setProperties(userNodeRef, properties);
        return userNodeRef;
    }

    protected void deleteTestUsers() throws Exception
    {
        for (NodeRef testUser : testUsers)
        {
            try
            {
                nodeService.deleteNode(testUser);
            }
            catch (InvalidNodeRefException e)
            {
                //Just ignore it.
            }
        }
        testUsers.clear();

    }

    @Override
    protected void tearDown() throws Exception
    {
        // remove all the test users we created
        deleteTestUsers();
        
        // cleanup transaction if necessary so we don't effect subsequent tests
        if ((userTransaction.getStatus() == Status.STATUS_ACTIVE) || (userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK))
        {
            userTransaction.rollback();
        }
        
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }
    
    public void testWorkerWithDefaultConfiguration() throws Exception
    {
        userTransaction = serviceRegistry.getTransactionService().getUserTransaction();
        userTransaction.begin();
        
        for (NodeRef testUser : testUsers)
        {
            assertNull("The hash indicator should not be set",nodeService.getProperty(testUser, ContentModel.PROP_HASH_INDICATOR));
            assertNull("The password hash should not be set",nodeService.getProperty(testUser, ContentModel.PROP_PASSWORD_HASH));
        }
        // execute the worker to upgrade all users
        this.upgradePasswordHashWorker.execute();

        userTransaction.commit();
        userTransaction = serviceRegistry.getTransactionService().getUserTransaction();
        userTransaction.begin();
        
        // ensure all the test users have been upgraded to use the preferred encoding
        List<String> doubleHashed = Arrays.asList("md4", "bcrypt10");
        for (NodeRef testUser : testUsers)
        {
            assertNotNull("The password hash should be set",  nodeService.getProperty(testUser, ContentModel.PROP_PASSWORD_HASH));
            assertEquals(doubleHashed,nodeService.getProperty(testUser, ContentModel.PROP_HASH_INDICATOR));
            assertNull("The md4 password should not be set",  nodeService.getProperty(testUser, ContentModel.PROP_PASSWORD));
            assertNull("The sh256 password should not be set",nodeService.getProperty(testUser, ContentModel.PROP_PASSWORD_SHA256));
        }
    }
    
    public void xxxtestWorkerWithLegacyConfiguration() throws Exception
    {
        // execute the worker to upgrade all users 
        this.upgradePasswordHashWorker.execute();
        
        // ensure all the test users have been upgraded but maintain the MD4 encoding
    }
}
