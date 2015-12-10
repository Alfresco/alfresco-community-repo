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

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.service.ServiceRegistry;
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
    private UpgradePasswordHashWorker upgradePasswordHashWorker;
    private List<String> testUserNames;

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
        
        upgradePasswordHashWorker = (UpgradePasswordHashWorker)ctx.getBean("upgradePasswordHashWorker");
        
        userTransaction = serviceRegistry.getTransactionService().getUserTransaction();
        userTransaction.begin();
        
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        
        createTestUsers();
    }
    
    protected void createTestUsers() throws Exception
    {
        // create 50 users and change their properties back to how
        // they would have been pre-upgrade.
        
        testUserNames = new ArrayList<String>(50);
    }
    
    protected void deleteTestUsers() throws Exception
    {
        // delete all the test users.
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
        // execute the worker to upgrade all users
        this.upgradePasswordHashWorker.execute();
        
        // ensure all the test users have been upgraded to use the preferred encoding
    }
    
    public void xxxtestWorkerWithLegacyConfiguration() throws Exception
    {
        // execute the worker to upgrade all users 
        this.upgradePasswordHashWorker.execute();
        
        // ensure all the test users have been upgraded but maintain the MD4 encoding
    }
}
