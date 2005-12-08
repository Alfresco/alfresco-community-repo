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
package org.alfresco.repo.content.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import junit.framework.TestCase;

/**
 * Some tests to check out the </code>java.lang.nio</code> functionality
 * 
 * @author Derek Hulley
 */
public class FileIOTest extends TestCase
{
    private static final String TEST_CONTENT = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private File file;
    
    public FileIOTest(String name)
    {
        super(name);
    }
    
    public void setUp() throws Exception
    {
        file = File.createTempFile(getName(), ".txt");
        OutputStream os = new FileOutputStream(file);
        os.write(TEST_CONTENT.getBytes());
        os.flush();
        os.close();
    }
    
    /**
     * Attempt to read the same file using multiple channels concurrently
     */
    public void testConcurrentFileReads() throws Exception
    {
        // open the file for a read
        FileInputStream isA = new FileInputStream(file);
        FileInputStream isB = new FileInputStream(file);
        
        // get the channels
        FileChannel channelA = isA.getChannel();
        FileChannel channelB = isB.getChannel();
        
        // buffers for reading
        ByteBuffer bufferA = ByteBuffer.allocate(10);
        ByteBuffer bufferB = ByteBuffer.allocate(10);
        
        // read file into both buffers
        int countA = 0;
        int countB = 0;
        do
        {
            countA = channelA.read((ByteBuffer)bufferA.clear());
            countB = channelB.read((ByteBuffer)bufferB.clear());
            assertEquals("Should read same number of bytes", countA, countB);
        } while (countA > 6);
        
        // both buffers should be at the same marker 6
        assertEquals("BufferA marker incorrect", 6, bufferA.position());
        assertEquals("BufferB marker incorrect", 6, bufferB.position());
    }
    
    public void testConcurrentReadWrite() throws Exception
    {
        // open file for a read
        FileInputStream isRead = new FileInputStream(file);
        // open file for write
        FileOutputStream osWrite = new FileOutputStream(file);
        
        // get channels
        FileChannel channelRead = isRead.getChannel();
        FileChannel channelWrite = osWrite.getChannel();
        
        // buffers
        ByteBuffer bufferRead = ByteBuffer.allocate(26);
        ByteBuffer bufferWrite = ByteBuffer.wrap(TEST_CONTENT.getBytes());
        
        // read - nothing will be read
        int countRead = channelRead.read(bufferRead);
        assertEquals("Expected nothing to be read", -1, countRead);
        // write
        int countWrite = channelWrite.write(bufferWrite);
        assertEquals("Not all characters written", 26, countWrite);
        
        // close the write side
        channelWrite.close();
        
        // reread
        countRead = channelRead.read(bufferRead);
        assertEquals("Expected full read", 26, countRead);
    }
}
