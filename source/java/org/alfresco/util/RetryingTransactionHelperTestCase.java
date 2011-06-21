/**
 * 
 */
package org.alfresco.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Test case base class with helper methods for transactional tests.
 * 
 * @author Roy Wetherall
 */
public abstract class RetryingTransactionHelperTestCase extends TestCase
{
    /**
     * @return  retrying transaction helper
     */
    public abstract RetryingTransactionHelper getRetryingTransactionHelper();
    
    /**
     * Executes a test in a retrying transaction as the admin user.
     * 
     * @param <A>   type of the object resulting from the test, can be set to {@link Void} if none.
     * @param test  test object to be executed within a retrying transaction
     * @return A    the result of the test
     */
    protected <A> A doTestInTransaction(final Test<A> test)
    {
        return doTestInTransaction(test, AuthenticationUtil.getAdminUserName());
    }
    
    /**
     * Executes a test in a retrying transaction as the admin user.
     * 
     * @param <A>    type of the object resulting from the test, can be set to {@link Void} if none.
     * @param test   test object to be executed within a retrying transaction
     * @param asUser user to execute the test as
     * @return A     the result of the test
     */
    protected <A> A doTestInTransaction(final Test<A> test, final String asUser)
    {    
        String origUser = AuthenticationUtil.getFullyAuthenticatedUser();
        AuthenticationUtil.setFullyAuthenticatedUser(asUser);
        try
        {        
            // Execute the run() method within a retrying transaction
            RetryingTransactionCallback<A> doRun = new RetryingTransactionCallback<A>()
            {
                @Override
                public A execute() throws Throwable
                {
                    // Run test as user
                    return test.run();              
                }            
            };                
            final A result = getRetryingTransactionHelper().doInTransaction(doRun);
            
            // Execute the test() method within a retrying transaction
            RetryingTransactionCallback<Void> doTest = new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    // pass the result of the run into the test
                    test.test(result);
                    return null;                    
                }
                
            };
            getRetryingTransactionHelper().doInTransaction(doTest);
            
            return result;
        }
        finally
        {
            if (origUser != null)
            {
                AuthenticationUtil.setFullyAuthenticatedUser(origUser);
            }
            else
            {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        }                   
    }
    
    /**
     * Executes a test in a retrying transaction.  Run as admin user.
     * 
     * @param test  failure test object
     */
    protected void doTestInTransaction(final FailureTest test)
    {
        doTestInTransaction(test, AuthenticationUtil.getAdminUserName());
    }
        
    /**
     * Executes a test in a retrying transaction. 
     * 
     * @param test   failure test object
     * @param asUser user to run test as
     */
    protected void doTestInTransaction(final FailureTest test, final String asUser)
    {
        String origUser = AuthenticationUtil.getFullyAuthenticatedUser();
        AuthenticationUtil.setFullyAuthenticatedUser(asUser);
        try
        {
            RetryingTransactionCallback<Void> doRun = new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {         
                    Class<?> eType = test.getExpectedExceptionClass();                
                    try
                    {
                        test.run();
                    }
                    catch (Throwable exception)
                    {   
                        if (eType.isInstance(exception) == false)
                        {
                            // Genuine error so re-throw
                            throw exception;
                        }
                        
                        // Otherwise, it's an expected failure 
                        return null;
                    }               
                    
                    // Fail since not expected to succeed
                    fail(test.getMessage());
                    return null;
                }            
            };        
            getRetryingTransactionHelper().doInTransaction(doRun);
        }
        finally
        {
            if (origUser != null)
            {
                AuthenticationUtil.setFullyAuthenticatedUser(origUser);
            }
            else
            {
                AuthenticationUtil.clearCurrentSecurityContext();
            }
        } 
    }    
    
    /**
     * Class containing the code to run as test and the optional code to check the results of the
     * test in a separate transaction if required.
     */
    protected abstract class Test<A>
    {   
        /** Map containing model values.  Used to pass data between run and test methods. */
        protected Map<String, Object> model = new HashMap<String, Object>(5);
        
        /**
         * Helper method to set a string vaule in the model
         * @param key   model key
         * @param value model value
         * @return String   model value
         */
        protected String setString(String key, String value)
        {
            if (value != null)
            {
                model.put(key, value);         
            }
            return value;
        }
        
        /**
         * Helper method to get a string value from the model
         * @param key   model key
         * @return String   model value, null if none
         */
        protected String getString(String key)
        {
            return (String)model.get(key);
        }
        
        /**
         * Helper method to set node reference value in model
         * @param key   model key
         * @param nodeRef   node reference
         * @return {@link NodeRef}  node reference
         */
        protected NodeRef setNodeRef(String key, NodeRef nodeRef)
        {
            if (nodeRef != null)
            {
                model.put(key, nodeRef);
            }
            return nodeRef;
        }
        
        /**
         * Helper method to get node reference value from model
         * @param key   mode key
         * @return {@link NodeRef}  node reference
         */
        protected NodeRef getNodeRef(String key)
        {
            return (NodeRef)model.get(key);
        }
        
        /**
         * Body of the test is implemented here.
         * @return A    result of the test
         * @throws Exception
         */
        public abstract A run() throws Exception; 
        
        /**
         * If you wish to test the results of the above method within a new and separate 
         * transaction then implement the tests here.
         * @param result    result of the above method
         * @throws Exception
         */
        public void test(A result) throws Exception
        {
            // Default implementation does nothing.
            // Override this if you want to test the results of the 
            // run method in a new transaction.
        }
    }
    
    /**
     * Class containing test code that is expected to fail.
     */
    protected abstract class FailureTest
    {   
        /** Failure message */
        private String message = "This test was expected to fail.";
        
        /** Expected failure exception */
        private Class<?> expectedExceptionClass = AlfrescoRuntimeException.class;
        
        /**
         * @param message   failure message
         */
        public FailureTest(String message)
        {
            this.message = message;
        }
        
        /**
         * @param expectedExceptionClass    expected exception class
         */
        public FailureTest(Class<?> expectedExceptionClass)
        {
            this.expectedExceptionClass = expectedExceptionClass;
        }
        
        /**
         * @param message   failure message
         * @param expectedExceptionClass    expected exception class
         */
        public FailureTest(String message, Class<?> expectedExceptionClass)
        {
            this.message = message;
            this.expectedExceptionClass = expectedExceptionClass;
        }
        
        /**
         * @return  expected exception class
         */
        public Class<?> getExpectedExceptionClass()
        {
            return expectedExceptionClass;
        }
        
        /**
         * @return  failure message
         */
        public String getMessage()
        {
            return message;
        }
        
        /**
         * Default constructor
         */
        public FailureTest()
        {
            super();
        }
        
        /**
         * Code to test for failure
         * 
         * @throws Exception
         */
        public abstract void run() throws Exception;
    }
}
