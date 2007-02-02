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

import org.springframework.util.FileCopyUtils;

import de.schlichtherle.io.DefaultRaesZipDetector;
import de.schlichtherle.io.FileInputStream;
import de.schlichtherle.io.FileOutputStream;
import de.schlichtherle.io.ZipDetector;

/**
 * Module management tool unit test
 * 
 * @author Roy Wetherall
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
        String ampLocation = getFileLocation(".amp", "module/test.amp");
        String ampV2Location = getFileLocation(".amp", "module/test_v2.amp");
        
        System.out.println(warLocation);
        
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
        checkForFileExistance(warLocation, files);   
        
        // Check the intstalled files
        InstalledFiles installed0 = new InstalledFiles(warLocation, "test");
        installed0.load();
        assertNotNull(installed0);
        assertEquals(7, installed0.getAdds().size());
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
            fail("The module is already installed so an exception should have been raised since we are not forcing an overwite");
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
        
        // Try and install and earlier version
        try
        {
            this.manager.installModule(ampLocation, warLocation);
            fail("An earlier version of this module is already installed so an exception should have been raised since we are not forcing an overwite");
        }
        catch(ModuleManagementToolException exception)
        {
            // Pass
        }        
    }
    
    public void testPreviewInstall()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test.amp");
        
        System.out.println(warLocation);
        
        // Initial install of module
        this.manager.installModule(ampLocation, warLocation, true, false, true);
        
        // TODO need to prove that the war file has not been updated in any way
    }
    
    public void testForcedInstall()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test.amp");
        
        System.out.println(warLocation);
        
        // Initial install of module
        this.manager.installModule(ampLocation, warLocation, false, false, false);
        this.manager.installModule(ampLocation, warLocation, false, true, false);
    }
    
    public void testInstallFromDir()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test.amp");
        String ampV2Location = getFileLocation(".amp", "module/test_v2.amp");

        int index = ampV2Location.lastIndexOf(File.separator);
        System.out.println(index);
        String directoryLocation = ampV2Location.substring(0, index);
        
        System.out.println(warLocation);
        System.out.println(directoryLocation);
        try
        {
            this.manager.installModules(directoryLocation, warLocation);
        }
        catch (ModuleManagementToolException exception)
        {
            // ignore since we are expecting this
        }
    }
    
    public void testList()
        throws Exception
    {
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test.amp");
        
        this.manager.listModules(warLocation);
        
        this.manager.installModule(ampLocation, warLocation);
        
        this.manager.listModules(warLocation);        
    }

    private String getFileLocation(String extension, String location)
        throws IOException
    {
        File file = File.createTempFile("moduleManagementToolTest-", extension);        
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
