/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import java.util.ArrayList;

import org.alfresco.repo.avm.hibernate.HibernateHelper;
import org.alfresco.repo.avm.impl.AVMServiceImpl;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import junit.framework.TestCase;

/**
 * Big test of AVM behavior.
 * @author britt
 */
public class AVMServiceTest extends TestCase
{
    /**
     * The AVMService we are testing.
     */
    private AVMService fService;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        Configuration cfg = HibernateHelper.GetConfiguration();
        SchemaExport se = new SchemaExport(cfg);
        se.drop(false, true);
        AVMServiceImpl service = new AVMServiceImpl();
        service.setStorage("storage");
        service.init(true);
        fService = service;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        HibernateHelper.Reset();
    }
    
    /**
     * Test Nothing.  Just make sure set up works.
     */
    public void testNothing()
    {
    }
    
    /**
     * Test making a simple directory.
     */
    public void testCreateDirectory()
    {
        try
        {
            fService.createDirectory("main:/", "testdir");
            ArrayList<String> toSnapshot = new ArrayList<String>();
            toSnapshot.add("main");
            fService.createSnapshot(toSnapshot);
            AVMNode node = fService.lookup(-1, "main:/");
            assertTrue(node instanceof PlainDirectoryNode);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test creating a file.
     */
    public void testCreateFile()
    {
        try
        {
            testCreateDirectory();
            fService.createFile("main:testdir", "testfile");
            ArrayList<String> toSnapshot = new ArrayList<String>();
            toSnapshot.add("main");
            fService.createSnapshot(toSnapshot);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
}
