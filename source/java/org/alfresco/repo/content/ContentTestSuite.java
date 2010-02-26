/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.content.cleanup.ContentStoreCleanerTest;
import org.alfresco.repo.content.encoding.CharsetFinderTest;
import org.alfresco.repo.content.filestore.FileContentStoreTest;
import org.alfresco.repo.content.filestore.NoRandomAccessFileContentStoreTest;
import org.alfresco.repo.content.filestore.ReadOnlyFileContentStoreTest;
import org.alfresco.repo.content.replication.ContentStoreReplicatorTest;
import org.alfresco.repo.content.replication.ReplicatingContentStoreTest;

/**
 * Suite for content-related tests.
 * 
 * @author Derek Hulley
 */
public class ContentTestSuite extends TestSuite
{
    @SuppressWarnings("unchecked")
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(ContentStoreCleanerTest.class);
        suite.addTestSuite(CharsetFinderTest.class);
        suite.addTestSuite(FileContentStoreTest.class);
        suite.addTestSuite(NoRandomAccessFileContentStoreTest.class);
        suite.addTestSuite(ReadOnlyFileContentStoreTest.class);
        suite.addTestSuite(ContentStoreReplicatorTest.class);
        suite.addTestSuite(ReplicatingContentStoreTest.class);
        suite.addTestSuite(ContentDataTest.class);
        suite.addTestSuite(MimetypeMapTest.class);
        suite.addTestSuite(RoutingContentServiceTest.class);
        suite.addTestSuite(RoutingContentStoreTest.class);
        
        try
        {
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
