package org.alfresco.repo.tenant;

import junit.framework.TestCase;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * @see MultiTNodeServiceInterceptor 
 * 
 * @since 3.0 
 * @author Derek Hulley
 */
@Category(OwnJVMTestsCategory.class)
public class MultiTNodeServiceInterceptorTest extends TestCase
{
    public static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private String tenant1 = "tenant-" + GUID.generate();
    private String tenant1Pwd = "pwd1";
    private boolean enableTest = true;
    private TransactionService transactionService;
    private TenantAdminService tenantAdminService;
    private TenantService tenantService;
    private MultiTNodeServiceInterceptor interceptor;
    
    @Override
    public void setUp() throws Exception
    {
        transactionService = (TransactionService) ctx.getBean("TransactionService");
        tenantAdminService = (TenantAdminService) ctx.getBean("tenantAdminService");
        tenantService = (TenantService) ctx.getBean("tenantService");
        interceptor = (MultiTNodeServiceInterceptor) ctx.getBean("multiTNodeServiceInterceptor");
        
        // If MT is disabled, then disable all tests
        if (!tenantAdminService.isEnabled())
        {
            enableTest = false;
            return;
        }
        
        // Create a tenant
        RetryingTransactionCallback<Object> createTenantCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                tenantAdminService.createTenant(tenant1, tenant1Pwd.toCharArray());
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(createTenantCallback, false, true);
    }
    
    @Override
    public void tearDown() throws Exception
    {
        // If MT is disabled, then disable all tests
        if (!tenantAdminService.isEnabled())
        {
            return;
        }
        
        // Delete a tenant
        RetryingTransactionCallback<Object> createTenantCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                tenantAdminService.deleteTenant(tenant1);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(createTenantCallback, false, true);
    }
    
    /**
     * Control case.
     */
    public void testSetUp()
    {
    }
}
