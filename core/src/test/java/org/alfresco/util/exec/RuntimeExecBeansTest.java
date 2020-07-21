/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.util.exec;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @see org.alfresco.util.exec.RuntimeExecBootstrapBean
 * 
 * @author Derek Hulley
 */
public class RuntimeExecBeansTest extends TestCase
{
    private static Log logger = LogFactory.getLog(RuntimeExecBeansTest.class);
    
    private static final String APP_CONTEXT_XML =
            "classpath:org/alfresco/util/exec/RuntimeExecBeansTest-context.xml";
    private static final String DIR = "dir RuntimeExecBootstrapBeanTest";
    
    private File dir;

    public void setUp() throws Exception
    {
        dir = new File(DIR);
        dir.mkdir();
        assertTrue("Directory not created", dir.exists());
    }
    
    public void testBootstrapAndShutdown() throws Exception
    {
        // now bring up the bootstrap
        ApplicationContext ctx = new ClassPathXmlApplicationContext(APP_CONTEXT_XML);
        
        // the folder should be gone
        assertFalse("Folder was not deleted by bootstrap", dir.exists());
        
        // now create the folder again
        dir.mkdir();
        assertTrue("Directory not created", dir.exists());
        
        // announce that the context is closing
        ctx.publishEvent(new ContextClosedEvent(ctx));
        
        // the folder should be gone
        assertFalse("Folder was not deleted by shutdown", dir.exists());
    }
    
    public void testSimpleSuccess() throws Exception
    {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(APP_CONTEXT_XML);
        try
        {
            RuntimeExec dirRootExec = (RuntimeExec) ctx.getBean("commandListRootDir");
            assertNotNull(dirRootExec);
            // Execute it
            dirRootExec.execute();
        }
        finally
        {
            ctx.close();
        }
    }
    
    public void testDeprecatedSetCommandMap() throws Exception
    {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(APP_CONTEXT_XML);
        try
        {
            RuntimeExec deprecatedExec = (RuntimeExec) ctx.getBean("commandCheckDeprecatedSetCommandMap");
            assertNotNull(deprecatedExec);
            // Execute it
            deprecatedExec.execute();
        }
        finally
        {
            ctx.close();
        }
        // The best we can do is look at the log manually
        logger.warn("There should be a warning re. the use of deprecated 'setCommandMap'.");
    }
    
    public void testSplitArguments() throws Exception
    {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(APP_CONTEXT_XML);
        try
        {
            RuntimeExec splitExec = (RuntimeExec) ctx.getBean("commandSplitArguments");
            assertNotNull(splitExec);
            String[] splitCommand = splitExec.getCommand();
            assertTrue(
                    "Command arguments not split into 'dir', '.' and '..' :" + Arrays.deepToString(splitCommand),
                    Arrays.deepEquals(new String[] {"dir", ".", ".."}, splitCommand));
        }
        finally
        {
            ctx.close();
        }
    }
    
    public void testSplitArgumentsAsSingleValue() throws Exception
    {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(APP_CONTEXT_XML);
        try
        {
            RuntimeExec splitExec = (RuntimeExec) ctx.getBean("commandSplitArgumentsAsSingleValue");
            assertNotNull(splitExec);
            String[] splitCommand = splitExec.getCommand();
            assertTrue(
                    "Command arguments not split into 'dir', '.' and '..' : " + Arrays.deepToString(splitCommand),
                    Arrays.deepEquals(new String[] {"dir", ".", ".."}, splitCommand));
        }
        finally
        {
            ctx.close();
        }
    }
    
    public void testFailureModeOfMissingCommand()
    {
        File dir = new File(DIR);
        dir.mkdir();
        assertTrue("Directory not created", dir.exists());
        
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(APP_CONTEXT_XML);
        try
        {
            RuntimeExec failureExec = (RuntimeExec) ctx.getBean("commandFailureGuaranteed");
            assertNotNull(failureExec);
            // Execute it
            ExecutionResult result = failureExec.execute();
            assertEquals("Expected first error code in list", 666, result.getExitValue());
        }
        finally
        {
            ctx.close();
        }
    }
    
//    /**
//     * Checks that the encoding setting feeds through to the streams.
//     */
//    public void testStreamReading() throws Exception
//    {
//        String manglingCharsetName = "UTF-16";
//
//        File dir = new File(DIR);
//        dir.mkdir();
//        assertTrue("Directory not created", dir.exists());
//        
//        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(APP_CONTEXT_XML);
//        try
//        {
//            RuntimeExec dirRootExec = (RuntimeExec) ctx.getBean("commandListRootDir");
//            assertNotNull(dirRootExec);
//            // Execute it
//            ExecutionResult result = dirRootExec.execute();
//            
//            // Get the error stream
//            String defaultStdOut = result.getStdOut();
//            
//            // Change the encoding
//            dirRootExec.setCharset(manglingCharsetName);
//            result = dirRootExec.execute();
//            String mangledStdOut = result.getStdOut();
//            // The two error strings must not be the same
//            assertNotSame("Differently encoded strings should differ", defaultStdOut, mangledStdOut);
//            
//            // Now convert the Shift-JIS string and ensure it's the same as originally expected
//            Charset defaultCharset = Charset.defaultCharset();
//            byte[] mangledBytes = mangledStdOut.getBytes(manglingCharsetName);
//            String convertedStrOut = new String(mangledBytes, defaultCharset.name());
//            // Check, catering for any mangled characters
//            assertTrue("Expected to be able to convert value back to default charset.", convertedStrOut.contains(defaultStdOut));
//        }
//        finally
//        {
//            ctx.close();
//        }
//    }
//    
    public void testExecOfNeverEndingProcess()
    {
        File dir = new File(DIR);
        dir.mkdir();
        assertTrue("Directory not created", dir.exists());
        
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(APP_CONTEXT_XML);
        try
        {
            RuntimeExec failureExec = (RuntimeExec) ctx.getBean("commandNeverEnding");
            assertNotNull(failureExec);
            // Execute it
            failureExec.execute();
            // The command is never-ending, so this should be out immediately
        }
        finally
        {
            ctx.close();
        }
    }
}
