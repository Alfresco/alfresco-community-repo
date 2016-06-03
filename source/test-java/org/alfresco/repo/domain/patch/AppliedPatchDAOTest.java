package org.alfresco.repo.domain.patch;

import java.util.Date;

import junit.framework.TestCase;

import org.alfresco.repo.admin.patch.AppliedPatch;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

/**
 * @see AppliedPatchDAO
 * 
 * @author Derek Hulley
 * @since 3.4
 */
@Category(OwnJVMTestsCategory.class)
public class AppliedPatchDAOTest extends TestCase
{
    private ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private TransactionService transactionService;
    private RetryingTransactionHelper txnHelper;
    private AppliedPatchDAO appliedPatchDAO;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        transactionService = serviceRegistry.getTransactionService();
        txnHelper = transactionService.getRetryingTransactionHelper();
        
        appliedPatchDAO = (AppliedPatchDAO) ctx.getBean("appliedPatchDAO");
    }
    
    private AppliedPatch create(String id, boolean allNull) throws Exception
    {
        final AppliedPatch appliedPatch = new AppliedPatchEntity();
        appliedPatch.setId(id);
        if (!allNull)
        {
            appliedPatch.setDescription(id);
            appliedPatch.setFixesFromSchema(0);
            appliedPatch.setFixesToSchema(1);
            appliedPatch.setTargetSchema(2);
            appliedPatch.setAppliedOnDate(new Date());
            appliedPatch.setAppliedToSchema(1);
            appliedPatch.setAppliedToServer("blah");
            appliedPatch.setWasExecuted(true);
            appliedPatch.setSucceeded(true);
            appliedPatch.setReport("All good in test " + getName());
        }
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                appliedPatchDAO.createAppliedPatch(appliedPatch);
                return null;
            }
        };
        txnHelper.doInTransaction(callback);
        return appliedPatch;
    }
    
    private AppliedPatch get(final String id) throws Exception
    {
        RetryingTransactionCallback<AppliedPatch> callback = new RetryingTransactionCallback<AppliedPatch>()
        {
            public AppliedPatch execute() throws Throwable
            {
                return appliedPatchDAO.getAppliedPatch(id);
            }
        };
        return txnHelper.doInTransaction(callback);
    }
    
    public void testCreateEmpty() throws Exception
    {
        final String id = getName() + "-" + System.currentTimeMillis();
        create(id, true);
    }
    
    public void testCreatePopulated() throws Exception
    {
        final String id = getName() + "-" + System.currentTimeMillis();
        create(id, false);
    }
    
    public void testCreateWithRollback() throws Exception
    {
        final String id = getName() + "-" + System.currentTimeMillis();
        // Create an encoding
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                create(id, false);
                // Now force a rollback
                throw new RuntimeException("Forced");
            }
        };
        try
        {
            txnHelper.doInTransaction(callback);
            fail("Transaction didn't roll back");
        }
        catch (RuntimeException e)
        {
            // Expected
        }
        // Check that it doesn't exist
        get(id);
    }
//    
//    public void testCaseInsensitivity() throws Exception
//    {
//        String encoding = "AAA-" + GUID.generate();
//        Pair<Long, String> lowercasePair = get(encoding.toLowerCase(), true, true);
//        // Check that the same pair is retrievable using uppercase
//        Pair<Long, String> uppercasePair = get(encoding.toUpperCase(), true, true);
//        assertNotNull(uppercasePair);
//        assertEquals(
//                "Upper and lowercase encoding instance IDs were not the same",
//                lowercasePair.getFirst(), uppercasePair.getFirst());
//    }
}
