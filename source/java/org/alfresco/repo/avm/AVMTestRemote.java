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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.alfresco.repo.remote.ClientTicketHolder;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import junit.framework.TestCase;

/**
 * Tests of Remote interface to AVM.
 * @author britt
 */
public class AVMTestRemote extends TestCase
{
    /**
     * The AVMRemote.
     */
    private AVMRemote fAVMRemote;
    
    /**
     * The Sync service.
     */
    private AVMSyncService fAVMSync;
    
    /**
     * The Authentication Service.
     */
    private AuthenticationService fAuthService;
    
    /**
     * The application context.
     */
    private FileSystemXmlApplicationContext fContext;
    
    @Override
    protected void setUp() throws Exception
    {
        fContext = new FileSystemXmlApplicationContext("config/alfresco/remote-avm-test-context.xml");
        fAVMRemote = (AVMRemote)fContext.getBean("avmRemote");
        fAVMSync = (AVMSyncService)fContext.getBean("avmSyncService");
        fAuthService = (AuthenticationService)fContext.getBean("authenticationService");
        fAuthService.authenticate("admin", "admin".toCharArray());
        String ticket = fAuthService.getCurrentTicket();
        ClientTicketHolder.SetTicket(ticket);
    }

    @Override
    protected void tearDown() throws Exception
    {
        List<AVMStoreDescriptor> stores = fAVMRemote.getAVMStores();
        for (AVMStoreDescriptor desc : stores)
        {
            fAVMRemote.purgeAVMStore(desc.getName());
        }
        fContext.close();
    }
    
    /**
     * Do a simple hello world test.
     */
    public void testSimple()
    {
        try
        {
            List<AVMStoreDescriptor> stores = fAVMRemote.getAVMStores();
            for (AVMStoreDescriptor store : stores)
            {
                System.out.println(store);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test reading and writing.
     */
    public void testReadWrite()
    {
        try
        {
            // Create a test store.
            fAVMRemote.createAVMStore("test2933");
            // Create a directory.
            fAVMRemote.createDirectory("test2933:/", "a");
            // Write out a file.
            OutputStream out = 
                fAVMRemote.createFile("test2933:/a", "foo.txt");
            byte [] buff = "This is a plain old text file.\n".getBytes();
            out.write(buff);
            buff = "It contains text.\n".getBytes();
            out.write(buff);
            out.close();
            // Read back that file.
            InputStream in = 
                fAVMRemote.getFileInputStream(-1, "test2933:/a/foo.txt");
            buff = new byte[1024];
            assertEquals(49, in.read(buff));
            System.out.print(new String(buff));
            assertEquals(-1, in.read(buff));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }

    /**
     * Another test of reading.
     */
    public void testRead()
    {
        try
        {
            fAVMRemote.createAVMStore("froo");
            // Create a file.
            byte [] buff = new byte[64];
            for (int i = 0; i < 64; i++)
            {
                buff[i] = (byte)i;
            }
            OutputStream out =
                fAVMRemote.createFile("froo:/", "foo.dat");
            out.write(buff, 32, 32);
            out.close();
            // Read it back in.
            InputStream in =
                fAVMRemote.getFileInputStream(-1, "froo:/foo.dat");
            buff = new byte[1024];
            assertEquals(32, in.read(buff));
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test a call that should return null;
     */
    public void testErrorState()
    {
        try
        {
            assertNull(fAVMRemote.lookup(-1, "main:/fizz/fazz"));
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
    
    /**
     * Test the sync service.
     */
    public void testSyncService()
    {
        try
        {
            // Create a store.
            fAVMRemote.createAVMStore("froo");
            // Create a directory.
            fAVMRemote.createDirectory("froo:/", "a");
            // Create a file.
            fAVMRemote.createFile("froo:/a", "foo").close();
            // Create another store.
            fAVMRemote.createAVMStore("broo");
            // Create a branch.
            fAVMRemote.createBranch(-1, "froo:/a", "broo:/", "a");
            List<AVMDifference> diffs = fAVMSync.compare(-1, "froo:/a", -1, "broo:/a");
            assertEquals(0, diffs.size());
            fAVMRemote.createFile("froo:/a", "bar").close();
            diffs = fAVMSync.compare(-1, "froo:/a", -1, "broo:/a");
            assertEquals(1, diffs.size());
            // Update.
            fAVMSync.update(diffs, false, false, false, false, "flippy", "Stuff");
            diffs = fAVMSync.compare(-1, "froo:/a", -1, "broo:/a");
            assertEquals(0, diffs.size());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
}
