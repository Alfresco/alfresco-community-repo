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
package org.alfresco.repo.module.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.util.TempFileProvider;
import org.springframework.util.FileCopyUtils;

import de.schlichtherle.io.DefaultRaesZipDetector;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;
import de.schlichtherle.io.ZipDetector;

/**
 * @see org.alfresco.repo.module.tool.ModuleManagementTool
 * 
 * @author Roy Wetherall
 * @author Derek Hulley
 */
public class ModuleManagementToolTest extends TestCase
{
    private ModuleManagementTool manager = new ModuleManagementTool();

    ZipDetector defaultDetector = new DefaultRaesZipDetector("amp|war");
    
    public void testBasicInstall()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test_v1.amp");
        String ampV2Location = getFileLocation(".amp", "module/test_v2.amp");
        
        // Initial install of module
        this.manager.installModule(ampLocation, warLocation);
        
        // Check that the war has been modified correctly
        List<String> files = new ArrayList<String>(10);
        files.add("/WEB-INF/classes/alfresco/module/test/module.properties");
        files.add("/WEB-INF/classes/alfresco/module/test/modifications.install");
        files.add("/WEB-INF/lib/test.jar");
        files.add("/WEB-INF/classes/alfresco/module/test/module-context.xml");
        files.add("/WEB-INF/classes/alfresco/module/test");
        files.add("/WEB-INF/licenses/license.txt");
        files.add("/scripts/test.js");
        files.add("/images/test.jpg");
        files.add("/jsp/test.jsp");
        files.add("/css/test.css");
        files.add("/extra.txt");
        checkForFileExistance(warLocation, files);   
        
        // Check the intstalled files
        InstalledFiles installed0 = new InstalledFiles(warLocation, "test");
        installed0.load();
        assertNotNull(installed0);
        assertEquals(8, installed0.getAdds().size());
        assertEquals(1, installed0.getMkdirs().size());
        assertEquals(1, installed0.getUpdates().size());
        String backup = null;
        String orig = null;
        for (Map.Entry<String, String> update : installed0.getUpdates().entrySet())
        {
            checkContentsOfFile(warLocation + update.getKey(), "VERSIONONE");
            checkContentsOfFile(warLocation + update.getValue(), "ORIGIONAL");
            backup = update.getValue();
            orig = update.getKey();
        }
        
        // Try and install same version
        try
        {
            this.manager.installModule(ampLocation, warLocation);
            // Already installed is now a Warning rather than an Error and is now non fatal
            // fail("The module is already installed so an exception should have been raised since we are not forcing an overwite");
        }
        catch(ModuleManagementToolException exception)
        {
            // Pass
        }
        
        // Install a later version
        this.manager.installModule(ampV2Location, warLocation);
        
        // Check that the war has been modified correctly
        List<String> files2 = new ArrayList<String>(12);
        files.add("/WEB-INF/classes/alfresco/module/test/module.properties");
        files.add("/WEB-INF/classes/alfresco/module/test/modifications.install");
        files2.add("/WEB-INF/lib/test.jar");
        files2.add("/WEB-INF/classes/alfresco/module/test/module-context.xml");
        files2.add("/WEB-INF/classes/alfresco/module/test");
        files2.add("/WEB-INF/licenses/license.txt");
        files2.add("/scripts/test2.js");
        files2.add("/scripts/test3.js");
        files2.add("/images/test.jpg");        
        files2.add("/css/test.css");
        files2.add("/WEB-INF/classes/alfresco/module/test/version2");
        files2.add("/WEB-INF/classes/alfresco/module/test/version2/version2-context.xml");
        checkForFileExistance(warLocation, files2);      
        
        List<String> files3 = new ArrayList<String>(2);
        files3.add("/scripts/test.js");
        files3.add("/jsp/test.jsp");
        files3.add("/extra.txt");
        files3.add(backup);
        checkForFileNonExistance(warLocation, files3);
        
        // Check the intstalled files
        InstalledFiles installed1 = new InstalledFiles(warLocation, "test");
        installed1.load();
        assertNotNull(installed1);
        assertEquals(8, installed1.getAdds().size());
        assertEquals(1, installed1.getMkdirs().size());
        assertEquals(0, installed1.getUpdates().size());

        // Ensure the file has been reverted as it isnt updated in the v2.0
        checkContentsOfFile(warLocation + orig, "ORIGIONAL");
        
        /**
         *  Try and install an earlier version over a later version
         */
        try
        {
            this.manager.installModule(ampLocation, warLocation);
            //fail("A later version of this module is already installed so an exception should have been raised since we are not forcing an overwite");
            //this is now a warning rather than an error
            
            // Check that the war has not been modified
            checkForFileExistance(warLocation, files2);      
            checkForFileNonExistance(warLocation, files3);
        }
        catch(ModuleManagementToolException exception)
        {
            // Pass
        }        
    }
    
    public void testDependencySuccess() throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String testAmpV1Location = getFileLocation(".amp", "module/test_v1.amp");
        String testAmpV2Location = getFileLocation(".amp", "module/test_v2.amp");
        String testAmpDepV1Location = getFileLocation(".amp", "module/dependent_on_test_v1.amp");
        String testAmpDepV2Location = getFileLocation(".amp", "module/dependent_on_test_v2.amp");
        
        // Install V1
        this.manager.installModule(testAmpV1Location, warLocation, false, false, false);
        
        // Install the module dependent on test_v1
        this.manager.installModule(testAmpDepV1Location, warLocation, false, false, false);
        
        try
        {
            // Attempt to upgrade the dependent module
            this.manager.installModule(testAmpDepV2Location, warLocation, false, false, false);
            fail("Failed to detect inadequate dependency on the test amp");
        }
        catch (ModuleManagementToolException e)
        {
            System.out.println("Expected: " + e.getMessage());
        }
        
        // Install the test_v2
        this.manager.installModule(testAmpV2Location, warLocation, false, false, false);
        
        // The dependent module should now go in
        this.manager.installModule(testAmpDepV2Location, warLocation, false, false, false);
    }

    public void testPreviewInstall()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test_v1.amp");
        
        // Initial install of module
        this.manager.installModule(ampLocation, warLocation, true, false, true);
        
        // TODO need to prove that the war file has not been updated in any way
    }
    
    public void testForcedInstall()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test_v1.amp");
        
        // Initial install of module
        this.manager.installModule(ampLocation, warLocation, false, false, false);
        this.manager.installModule(ampLocation, warLocation, false, true, false);
    }
    
    public void testInstallFromDir()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test_v1.amp");
        String ampV2Location = getFileLocation(".amp", "module/test_v2.amp");

        int index = ampV2Location.lastIndexOf(File.separator);
        System.out.println(index);
        String directoryLocation = ampV2Location.substring(0, index);
        
        try
        {
            this.manager.installModules(directoryLocation, warLocation);
        }
        catch (ModuleManagementToolException exception)
        {
            exception.printStackTrace();
            System.out.println("Expected failure: " + exception.getMessage());
        }
    }
    
    public void testList()
        throws Exception
    {
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test_v1.amp");
        
        this.manager.listModules(warLocation);
        
        this.manager.installModule(ampLocation, warLocation);
        
        this.manager.listModules(warLocation);        
    }
    
    private String getFileLocation(String extension, String location)
        throws IOException
    {
        File file = TempFileProvider.createTempFile("moduleManagementToolTest-", extension);        
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location);
        assertNotNull(is);
        OutputStream os = new FileOutputStream(file);        
        FileCopyUtils.copy(is, os);        
        return file.getPath();
    }
    
    private void checkForFileExistance(String warLocation, List<String> files)
    {
        for (String file : files)
        {
            File file0 = new de.schlichtherle.io.File(warLocation + file, this.defaultDetector);
            assertTrue("The file/dir " + file + " does not exist in the WAR.", file0.exists());
        }    
    }
    
    private void checkForFileNonExistance(String warLocation, List<String> files)
    {
        for (String file : files)
        {
            File file0 = new de.schlichtherle.io.File(warLocation + file, this.defaultDetector);
            assertFalse("The file/dir " + file + " does exist in the WAR.", file0.exists());
        }    
    }
    
    private void checkContentsOfFile(String location, String expectedContents)
        throws IOException
    {
        File file = new de.schlichtherle.io.File(location, this.defaultDetector);
        assertTrue(file.exists());        
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = reader.readLine();
        assertNotNull(line);
        assertEquals(expectedContents, line.trim());
    }
}
