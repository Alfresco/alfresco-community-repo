/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
