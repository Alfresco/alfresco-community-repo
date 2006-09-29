/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
package org.alfresco.repo.node.index;

import junit.framework.TestCase;

import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Checks that full index recovery is possible
 * 
 * @author Derek Hulley
 */
public class FullIndexRecoveryComponentTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private FullIndexRecoveryComponent indexRecoverer;
    public void setUp() throws Exception
    {
        indexRecoverer = (FullIndexRecoveryComponent) ctx.getBean("indexRecoveryComponent");
    }
    
    public void testSetup() throws Exception
    {
        
    }
    
    public synchronized void testReindexing() throws Exception
    {
        indexRecoverer.setRecoveryMode(FullIndexRecoveryComponent.RecoveryMode.FULL.name());
        // reindex
        Thread reindexThread = new Thread()
        {
            public void run()
            {
                indexRecoverer.reindex();
            }
        };
        reindexThread.setDaemon(true);
        reindexThread.start();
//        reindexThread.run();
        
        // wait a bit and then terminate
        wait(10000);
        indexRecoverer.setShutdown(true);
        wait(10000);
    }
}
