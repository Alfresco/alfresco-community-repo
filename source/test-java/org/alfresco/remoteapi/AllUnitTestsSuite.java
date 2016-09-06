/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.remoteapi;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.web.scripts.solr.StatsGetTest;
import org.alfresco.repo.web.util.PagingCursorTest;
import org.alfresco.repo.web.util.paging.PagingTest;
import org.alfresco.repo.webdav.GetMethodTest;
import org.alfresco.repo.webdav.LockInfoImplTest;
import org.alfresco.repo.webdav.RenameShuffleDetectionTest;
import org.alfresco.repo.webdav.WebDAVHelperTest;
import org.alfresco.repo.webdav.WebDAVLockServiceImplTest;
import org.alfresco.rest.api.search.AllSearchApiTests;
import org.alfresco.rest.api.tests.ModulePackageTest;
import org.alfresco.rest.framework.tests.core.AllRestFrameworkTest;
import org.alfresco.rest.framework.tests.metadata.WriterTests;

/**
 * All Remote API project UNIT test classes should be added to this test suite.
 */
public class AllUnitTestsSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        publicApiTests(suite);
        webdavTests(suite);
        pagingTests(suite);

        suite.addTest(new JUnit4TestAdapter(StatsGetTest.class));
        return suite;
    }
    
    static void publicApiTests(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(AllRestFrameworkTest.class));
        suite.addTest(new JUnit4TestAdapter(AllSearchApiTests.class));
        suite.addTest(new JUnit4TestAdapter(WriterTests.class));
        suite.addTest(new JUnit4TestAdapter(ModulePackageTest.class));
    }
    
    static void webdavTests(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(GetMethodTest.class));
        suite.addTest(new JUnit4TestAdapter(LockInfoImplTest.class));
        suite.addTest(new JUnit4TestAdapter(RenameShuffleDetectionTest.class));
        suite.addTest(new JUnit4TestAdapter(WebDAVHelperTest.class));
        suite.addTest(new JUnit4TestAdapter(WebDAVLockServiceImplTest.class));
    }
    
    static void pagingTests(TestSuite suite)
    {
        suite.addTest(new JUnit4TestAdapter(PagingTest.class));
        suite.addTest(new JUnit4TestAdapter(PagingCursorTest.class));
    }
}
