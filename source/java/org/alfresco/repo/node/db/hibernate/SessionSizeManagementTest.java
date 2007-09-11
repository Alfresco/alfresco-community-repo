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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.node.db.hibernate;

import java.lang.reflect.Method;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.repo.transaction.TransactionResourceInterceptor;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Tests the session size limiters in the context of a full stack.
 * 
 * @see org.alfresco.util.resource.MethodResourceManager
 * @see org.alfresco.repo.transaction.TransactionResourceInterceptor
 * @see org.alfresco.repo.domain.hibernate.SessionSizeResourceManager
 * 
 * @author Derek Hulley
 */
public class SessionSizeManagementTest extends BaseNodeServiceTest
{
    private TransactionResourceInterceptor interceptor;
    private Method createNodesMethod;
    
    public SessionSizeManagementTest()
    {
        try
        {
            Class<SessionSizeManagementTest> clazz = SessionSizeManagementTest.class;
            createNodesMethod = clazz.getMethod(
                    "createNodes",
                    new Class[] {NodeService.class, Integer.TYPE, Boolean.TYPE});
        }
        catch (Exception e)
        {
            throw new RuntimeException("Instantiation failed", e);
        }
    }
    
    /**
     * Get the config locations
     * 
     * @return  an array containing the config locations
     */
    protected String[] getConfigLocations()
    {
        return new String[] {"session-size-test-context.xml"};
    }

    @Override
    protected NodeService getNodeService()
    {
        NodeService nodeService = (NodeService) applicationContext.getBean("testSessionSizeDbNodeService");
        return nodeService;
    }
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        // Get the interceptor for manual testing
        interceptor = (TransactionResourceInterceptor) applicationContext.getBean("testSessionSizeResourceInterceptor");
    }

    /** Helper to create a given number of nodes using the provided service */
    public void createNodes(NodeService nodeService, int count, boolean manualFlush)
    {
        for (int i = 0; i < count; i++)
        {
            long beforeNs = System.nanoTime();
            nodeService.createNode(
                    rootNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NamespaceService.ALFRESCO_URI, "child-" + i),
                    ContentModel.TYPE_FOLDER);
            long deltaNs = System.nanoTime() - beforeNs;
            // Perform manual flush if necessary
            if (manualFlush)
            {
                interceptor.performManualCheck(createNodesMethod, deltaNs);
            }
        }
    }

    private static final int LOAD_COUNT = 1000;
    /**
     * Create a bunch of nodes and see that the auto-clear is working
     */
    public synchronized void testBulkLoad() throws Exception
    {
        NodeService nodeService = getNodeService();
        createNodes(nodeService, LOAD_COUNT, false);
        // We can't check the session size as this is dependent on machine speed
    }
    
    /**
     * Create a bunch of nodes and see that the manual clearing is working.  The
     * original node service is used for this.
     */
    public synchronized void testManualOperation() throws Exception
    {
        NodeService nodeService = (NodeService) applicationContext.getBean("dbNodeServiceImpl");
        if (!(nodeService instanceof DbNodeServiceImpl))
        {
            fail("This test requires the unwrapped raw DbNodeServiceImpl");
        }
        
        createNodes(nodeService, LOAD_COUNT, true);
    }
}
