
package org.alfresco.util.test.junitrules;

import static org.junit.Assert.assertEquals;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.GUID;

/**
 * Test class for {@link AlfrescoPerson}.
 * 
 * @author Neil Mc Erlean
 * @since 4.2
 */
public class AlfrescoPersonTest extends AbstractAlfrescoPersonTest
{
    @Override protected String createTestUserName()
    {
        // In Community/Enterprise Alfresco, usernames are "just Strings" - e.g. they need not be email addresses.
        return GUID.generate();
    }
    
    @Override protected void validateCmPersonNode(final String username, final boolean exists)
    {
        TRANSACTION_HELPER.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                assertEquals("Test person's existence was wrong", exists, PERSON_SERVICE.personExists(username));
                return null;
            }
        });
    }
}
