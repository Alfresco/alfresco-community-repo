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

package org.alfresco.util.test.junitrules;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Neil Mc Erlean
 * @since 4.2
 */
public abstract class AbstractAlfrescoPersonTest
{
    /** Gets the username of the test user to be created. */
    protected abstract String createTestUserName();
    
    /** A hookpoint to allow subclasses to add addition validation. */
    protected void additionalValidations(String username, boolean userExists)
    {
        // Intentionally empty
    }
    
    // Rule to initialise the default Alfresco spring configuration
    @ClassRule public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();
    
    // Rule to create a test user
    protected final String testUsername         = createTestUserName();
    public final AlfrescoPerson testUserRule = new AlfrescoPerson(APP_CONTEXT_INIT, testUsername);
    
    // A rule to allow individual test methods all to be run as "admin".
    public RunAsFullyAuthenticatedRule runAsRule = new RunAsFullyAuthenticatedRule(AuthenticationUtil.getAdminUserName());
    
    @Rule public final RuleChain ruleChain = RuleChain.outerRule(runAsRule).around(testUserRule);
    
    protected static PersonService               PERSON_SERVICE;
    protected static RetryingTransactionHelper   TRANSACTION_HELPER;
    
    @BeforeClass public static void initStaticData() throws Exception
    {
        PERSON_SERVICE     = APP_CONTEXT_INIT.getApplicationContext().getBean("PersonService", PersonService.class);
        TRANSACTION_HELPER = APP_CONTEXT_INIT.getApplicationContext().getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
    }
    
    @Test public void ensureTestUserWasCreated() throws Exception
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                validateCmPersonNode(testUsername, true);
                additionalValidations(testUsername, true);
                
                return null;
            }
        });
    }
    
    /** Validate that the person is correctly persisted (or not). */
    protected abstract void validateCmPersonNode(String username, boolean exists);
    
    @Test public void ensureUserIsCleanedUp() throws Throwable
    {
        // Note that because we need to test that the Rule's 'after' behaviour has worked correctly, we cannot
        // use the Rule that has been declared in the normal way - otherwise nothing would be cleaned up until
        // after our test method.
        // Therefore we have to manually poke the Rule to get it to cleanup during test execution.
        // NOTE! This is *not* how a JUnit Rule would normally be used.
        
        // First manually run the 'after' part of the rule on this class - so that it does not interfere.
        this.testUserRule.after();
        
        final String testUserForThisMethodOnly = createTestUserName();
        
        AlfrescoPerson myTestUser = new AlfrescoPerson(APP_CONTEXT_INIT, testUserForThisMethodOnly);
        
        // Manually trigger the execution of the 'before' part of the rule.
        myTestUser.before();
        
        // Now trigger the Rule's cleanup behaviour.
        myTestUser.after();
        
        // and ensure that the nodes are all gone.
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                validateCmPersonNode(testUserForThisMethodOnly, false);
                additionalValidations(testUserForThisMethodOnly, false);
                return null;
            }
        });
    }
}
