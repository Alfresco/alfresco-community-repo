/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.test.util.bdt;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;

/**
 * Helper class that provides an simple way to write behaviour integration tests.
 * <p>
 * Note that initBehaviourTest() must be called before given() is called.
 * 
 * @author Roy Wetherall
 * @since 2.5
 */
public class BehaviourTest
{
    /** retrying transaction helper */
    private static RetryingTransactionHelper retryingTransactionHelper;
    
    /** current execution user */
    private String asUser = AuthenticationUtil.getAdminUserName();
    
    /**
     * Initialise behaviour tests for execution with retrying transaction helper
     * 
     * @param retryingTransactionHelper retrying transaction helper
     */
    public static void initBehaviourTests(RetryingTransactionHelper retryingTransactionHelper)
    {
        BehaviourTest.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * Start a test
     * 
     * @return  BehaviourTest   new test instance
     */
    public static BehaviourTest test()
    {
        return new BehaviourTest();
    }
    
    /**
     * Helper method to get the retrying transaction helper
     * 
     * @return  RetryingTransactionHelper   retrying transaction helper
     */
    /*package*/ RetryingTransactionHelper getRetryingTransactionHelper()
    {
        return retryingTransactionHelper;
    }
    
    /**
     * Helper method to get the execution user
     * 
     * @return  String  execution user
     */
    /* package*/ String getAsUser()
    {
        return asUser;
    }
    
    /**
     * Helper method to switch the current execution user to admin.
     * 
     * @return  BehaviourTest   test instance
     */
    public BehaviourTest asAdmin()
    {
        return as(AuthenticationUtil.getAdminUserName());
    }
    
    /**
     * Set execution user
     * 
     * @param asUser            execution user
     * @return BehaviourTest    test instance
     */
    public BehaviourTest as(String asUser)
    {
        this.asUser = asUser;
        return this;
    }
    
    /**
     * Given.
     * <p>
     * Used to group together given conditions.
     * 
     * @return  BehaviourTest   test instance  
     */
    public BehaviourTest given()
    {
        return this;
    }
    
    /**
     * Given.
     * <p>
     * Performs work.
     * 
     * @param given             work to do
     * @return BehaviourTest    test instance
     */
    public BehaviourTest given(Work given)
    {
        return perform(given);
    }
    
    /**
     * When.
     * <p>
     * Used to group together when actions.
     * 
     * @return BehaviourTest    test instance
     */
    public BehaviourTest when()
    {
        return this;
    }
    
    /**
     * When.
     * <p>
     * Performs work.
     * 
     * @param when              work to do
     * @return BehaviourTest    test instance
     */
    public BehaviourTest when(Work when) 
    {
        return perform(when);
    }
    
    /**
     * Then.
     * <p>
     * Used to group together then actions.
     * 
     * @return BehaviourTest    test instance
     */
    public BehaviourTest then()
    {
        return this;
    }
    
    /**
     * Then.
     * <p>
     * Performs work.
     * 
     * @param then              work to do
     * @return BehaviourTest    test instance
     */
    public BehaviourTest then(Work then)
    {
        return perform(then);        
    }

    /**
     * Expect a value.
     * 
     * @param value             value
     * @return ExpectedValue    expected value evaluator
     */
    public ExpectedValue<Boolean> expect(boolean value)
    {
        return new ExpectedValue<>(this, value);
    }
    
    /**
     * Expect a value.
     * 
     * @param value             value
     * @return ExpectedValue    expected value evaluator
     */
    public ExpectedValue<String> expect(String value)
    {
        return new ExpectedValue<>(this, value);
    }
    
    /**
     * Expect a value.
     * 
     * @param value             value
     * @return ExpectedValue    expected value evaluator
     */
    public ExpectedValue<Object> expect(Object value)
    {
        return new ExpectedValue<>(this, value);
    }

    
    /**
     * Expect a failure.
     * 
     * @param exceptionClass    expected exception
     * @return ExpectedFailure  expected failure evaluator
     */
    public ExpectedFailure expectException(Class<? extends Exception> exceptionClass)
    {
        return new ExpectedFailure(this, exceptionClass);
    }
    
    /**
     * Perform work a number of times
     * 
     * @param count           number of times to perform the work
     * @param work            work to perform
     * @return BehaviourTest  test instance  
     */
    public BehaviourTest perform(int count, Work work)
    {
        for (int i = 0; i < count; i++)
        {
            perform(work);
        }
        
        return this;
    }
    
    /**
     * Perform work
     * 
     * @param work            work to perform
     * @return BehaviourTest  test instance  
     */
    public BehaviourTest perform(Work work)
    {
        return AuthenticationUtil.runAs(() -> 
        {
            return retryingTransactionHelper.doInTransaction(() -> 
            {
                work.doIt();                
                return this;
            });
        },
        this.asUser);  
    }
    
    /**
     * Work Interface
     */
    public interface Work
    {
        /**
         * Do the work.
         */
        void doIt() throws Exception;
    }
}
