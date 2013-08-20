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
