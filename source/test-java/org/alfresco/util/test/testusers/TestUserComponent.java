
package org.alfresco.util.test.testusers;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This interface defines a software test component, which is responsible for the creation and deletion
 * of Alfresco users - to be used when running integration tests.
 * 
 * @author Neil Mc Erlean
 * @since 4.2
 */
public interface TestUserComponent
{
    /**
     * Creates a test user with the specified username.
     */
    NodeRef createTestUser(String userName);
    
    /**
     * Deletes the test user with the specified username.
     * @param userName
     */
    void deleteTestUser(String userName);
}
