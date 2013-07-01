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
