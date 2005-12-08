/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.version.common.counter;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.BaseSpringTest;

/**
 * @author Roy Wetherall
 */
public class VersionCounterDaoServiceTest extends BaseSpringTest
{
    /*
     * Test store id's
     */
    private final static String STORE_ID_1 = "test1_" + System.currentTimeMillis();
    private final static String STORE_ID_2 = "test2_" + System.currentTimeMillis();
    private static final String STORE_NONE = "test3_" + System.currentTimeMillis();;
    
    private NodeService nodeService;
    private VersionCounterDaoService counter;
    
    @Override
    public void onSetUpInTransaction()
    {
        nodeService = (NodeService) applicationContext.getBean("dbNodeService");
        counter = (VersionCounterDaoService) applicationContext.getBean("versionCounterDaoService");
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(nodeService);
        assertNotNull(counter);
    }
    
    /**
     * Test nextVersionNumber
     */
    public void testNextVersionNumber()
    {
        // Create the store references
        StoreRef store1 = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, VersionCounterDaoServiceTest.STORE_ID_1);
        StoreRef store2 = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, VersionCounterDaoServiceTest.STORE_ID_2);
        StoreRef storeNone = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, VersionCounterDaoServiceTest.STORE_NONE);
        
        int store1Version0 = this.counter.nextVersionNumber(store1);
        assertEquals(store1Version0, 1);
        
        int store1Version1 = this.counter.nextVersionNumber(store1);
        assertEquals(store1Version1, 2);
        
        int store2Version0 = this.counter.nextVersionNumber(store2);
        assertEquals(store2Version0, 1);
        
        int store1Version2 = this.counter.nextVersionNumber(store1);
        assertEquals(store1Version2, 3);
        
        int store2Version1 = this.counter.nextVersionNumber(store2);
        assertEquals(store2Version1, 2);
        
        int store1Current = this.counter.currentVersionNumber(store1);
        assertEquals(store1Current, 3);
        
        int store2Current = this.counter.currentVersionNumber(store2);
        assertEquals(store2Current, 2);
        
        int storeNoneCurrent = this.counter.currentVersionNumber(storeNone);
        assertEquals(storeNoneCurrent, 0);
        
        // Need to clean-up since the version counter works in its own transaction
        this.counter.resetVersionNumber(store1);
        this.counter.resetVersionNumber(store2);
    }

}
