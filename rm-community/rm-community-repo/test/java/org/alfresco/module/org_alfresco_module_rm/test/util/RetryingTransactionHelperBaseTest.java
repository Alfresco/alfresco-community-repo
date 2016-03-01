 
package org.alfresco.module.org_alfresco_module_rm.test.util;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.RetryingTransactionHelperTestCase;
import org.springframework.context.ApplicationContext;

/**
 * Base unit test for a simple retrying transaction helper test case.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RetryingTransactionHelperBaseTest extends RetryingTransactionHelperTestCase
{
    /** Application context */
    protected static final String[] CONFIG_LOCATIONS = new String[]
    {
        "classpath:alfresco/application-context.xml",
        "classpath:test-context.xml"
    };
    protected ApplicationContext applicationContext;
    
    /** retrying transaction helper */
    protected RetryingTransactionHelper retryingTransactionHelper;

    /**
     * @see org.alfresco.util.RetryingTransactionHelperTestCase#getRetryingTransactionHelper()
     */
    @Override
    public RetryingTransactionHelper getRetryingTransactionHelper()
    {
        return retryingTransactionHelper;
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        // Get the application context
        applicationContext = ApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);
        
        // get the retrying transaction helper
        retryingTransactionHelper = (RetryingTransactionHelper)applicationContext.getBean("retryingTransactionHelper");
    }

}
