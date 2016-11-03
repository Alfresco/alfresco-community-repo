/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.content;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.content.cleanup.ContentStoreCleanerTest;
import org.alfresco.repo.content.filestore.FileContentStoreTest;
import org.alfresco.repo.content.filestore.NoRandomAccessFileContentStoreTest;
import org.alfresco.repo.content.filestore.ReadOnlyFileContentStoreTest;
import org.alfresco.repo.content.filestore.SpoofedTextContentReaderTest;

/**
 * Suite for content-related tests.
 * 
 * This includes all the tests that need a full context, the
 *  rest are in {@link ContentMinimalContextTestSuite}
 * 
 * @author Derek Hulley
 */
public class ContentFullContextTestSuite extends TestSuite
{
    @SuppressWarnings("unchecked")
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        
        // These tests need a full context, at least for now
        suite.addTestSuite(ContentStoreCleanerTest.class);
        //suite.addTestSuite(CharsetFinderTest.class);
        suite.addTest(new JUnit4TestAdapter(SpoofedTextContentReaderTest.class));
        suite.addTest(new JUnit4TestAdapter(FileContentStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(NoRandomAccessFileContentStoreTest.class));
        suite.addTest(new JUnit4TestAdapter(ReadOnlyFileContentStoreTest.class));
        suite.addTestSuite(ContentDataTest.class);
        //suite.addTestSuite(MimetypeMapTest.class);
        suite.addTestSuite(RoutingContentServiceTest.class);
        suite.addTest(new JUnit4TestAdapter(RoutingContentStoreTest.class));
        suite.addTestSuite(GuessMimetypeTest.class);
        
        try
        {
            @SuppressWarnings("rawtypes")
            Class clazz = Class.forName("org.alfresco.repo.content.routing.StoreSelectorAspectContentStoreTest");
            suite.addTestSuite(clazz);
        }
        catch (Throwable e)
        {
            // Ignore
        }
        
                
        return suite;
    }
}
