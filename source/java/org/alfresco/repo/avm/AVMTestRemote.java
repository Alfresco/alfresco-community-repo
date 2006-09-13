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

import java.util.List;

import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
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
     * The application context.
     */
    private FileSystemXmlApplicationContext fContext;
    
    @Override
    protected void setUp() throws Exception
    {
        fContext = new FileSystemXmlApplicationContext("config/alfresco/remote-avm-test-context.xml");
        fAVMRemote = (AVMRemote)fContext.getBean("avmRemote");
    }

    @Override
    protected void tearDown() throws Exception
    {
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
            assertTrue(stores.size() > 0);
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
           // Create a directory.
           fAVMRemote.createDirectory("main:/", "a");
           // Write out a file.
           String outHandle = fAVMRemote.createFile("main:/a", "foo.txt");
           byte [] buff = "This is a plain old text file.\n".getBytes();
           fAVMRemote.writeOutput(outHandle, buff, buff.length);
           buff = "It contains text.\n".getBytes();
           fAVMRemote.writeOutput(outHandle, buff, buff.length);
           fAVMRemote.closeOutputHandle(outHandle);
           // Read back that file.
           String inHandle = fAVMRemote.getInputHandle(-1, "main:/a/foo.txt");
           buff = fAVMRemote.readInput(inHandle, 1024);
           fAVMRemote.closeInputHandle(inHandle);
           System.out.print(new String(buff));
           fAVMRemote.removeNode("main:/", "a");
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
            // Create a file.
            byte [] buff = new byte[64];
            for (int i = 0; i < 64; i++)
            {
                buff[i] = (byte)i;
            }
            String outHandle = fAVMRemote.createFile("main:/", "foo.dat");
            fAVMRemote.writeOutput(outHandle, buff, 64);
            fAVMRemote.closeOutputHandle(outHandle);
            // Read it back in.
            String inHandle = fAVMRemote.getInputHandle(-1, "main:/foo.dat");
            buff = fAVMRemote.readInput(inHandle, 64);
            fAVMRemote.closeInputHandle(inHandle);
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
}
