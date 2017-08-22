/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MiscContextTestSuite
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        AllRepositoryTestsCatalogue.minimalContext(suite);
        AllRepositoryTestsCatalogue.miscContext(suite);
        // the following tests can be extracted in a separate test suite 
        // if/when we decide to move the transformations in a separate component
        AllRepositoryTestsCatalogue.applicationContext_testThumnailContext(suite);
        return suite;
    }
}
