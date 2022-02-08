/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.module.tool;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.file.TVFS;
import junit.framework.TestCase;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.util.TempFileProvider;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @see org.alfresco.repo.module.tool.ModuleManagementTool
 * 
 * @author Roy Wetherall
 * @author Derek Hulley
 */
public class ModuleManagementToolTest extends TestCase
{
    private ModuleManagementTool manager = new ModuleManagementTool();
    static final int BUFFER = 2048;
    
    public void testBasicInstall()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test_v1.amp");
        String ampV2Location = getFileLocation(".amp", "module/test_v2.amp");
        
        installerSharedTests(warLocation, ampLocation, ampV2Location);        
    }

    private void installerSharedTests(String warLocation, String ampLocation, String ampV2Location)
    {
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
        assertEquals(9, installed0.getAdds().size());
        //assertEquals(1, installed0.getMkdirs().size());

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
        checkForFileNonExistance(warLocation, files3);
        
        // Check the intstalled files
        InstalledFiles installed1 = new InstalledFiles(warLocation, "test");
        installed1.load();
        assertNotNull(installed1);
        assertEquals(8, installed1.getAdds().size());
        assertEquals(1, installed1.getMkdirs().size());
        assertEquals(0, installed1.getUpdates().size());
        
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
    
    public void testBasicFolderInstall() throws Exception
    {
        manager.setVerbose(true);

        String warDirectory = extractToDir(".war", "module/test.war");
        String ampDirectory = extractToDir(".amp", "module/test_v1.amp");
        String ampV2Directory = getFileLocation(".amp", "module/test_v2.amp");
        assertNotNull(warDirectory);
        assertNotNull(ampDirectory);  
        assertNotNull(ampV2Directory);         
        installerSharedTests(warDirectory, ampDirectory, ampV2Directory);
        
        //Now try it on share
        warDirectory = extractToDir(".war", "module/share-3.4.11.war");
        assertNotNull(warDirectory);
        assertNotNull(ampDirectory);  
        this.manager.installModule(ampDirectory, warDirectory);
        
        warDirectory = extractToDir(".war", "module/share-4.2.a.war");
        assertNotNull(warDirectory);
        String ampV2Location = getFileLocation(".amp", "module/test_v6.amp");
        this.manager.installModule(ampV2Location, warDirectory);
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
    

    public void testUninstall()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test_v1.amp");
        
        // Initial install of module
        this.manager.installModule(ampLocation, warLocation, false, false, false);
        this.manager.listModules(warLocation);
        this.manager.uninstallModule("test", warLocation, false, false);
        
        List<String> files = new ArrayList<String>(10);
        files.add("/WEB-INF/classes/alfresco/module/test/module.properties");
        files.add("/WEB-INF/classes/alfresco/module/test/modifications.install");
        files.add("/WEB-INF/lib/test.jar");
        files.add("/WEB-INF/classes/alfresco/module/test/module-context.xml");
        checkForFileNonExistance(warLocation, files);  
    }
    
    public void testForcedInstall()
        throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/alfresco-4.2.c.war");
        String ampLocation = getFileLocation(".amp", "module/test_v4.amp");

        try
        {
            // Initial install of module
            this.manager.installModule(ampLocation, warLocation, false, false, false); //not forced
            fail("Failed to detect existing files in the amp");
        }
        catch (ModuleManagementToolException e)
        {
        	assertTrue(e.getMessage().contains("The amp will overwrite an existing file"));
        }
        
        String ampv2Location = getFileLocation(".amp", "module/test_v2.amp");
        warLocation = getFileLocation(".war", "module/alfresco-4.2.c.war");  //Get a new war file
        this.manager.installModule(ampLocation, warLocation, false, true, false); //install v1
        this.manager.installModule(ampv2Location, warLocation, false, true, false); //install v2
        
        //install another amp that replaces the same files
        ampLocation = getFileLocation(".amp", "module/test_v4.amp");
        warLocation = getFileLocation(".war", "module/alfresco-4.2.c.war");  //Get a new war file
        String amp5Location = getFileLocation(".amp", "module/test_v7.amp");  //new amp that overides existing files
        this.manager.installModule(ampLocation, warLocation, false, true, false);
        this.manager.installModule(amp5Location, warLocation, false, true, false);
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
    
    public void testExistingFilesInWar() throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");   //Version 4.0.1
        String ampLocation = getFileLocation(".amp", "module/test_v4.amp");
        
        try
        {
          this.manager.installModule(ampLocation, warLocation, false, false, true);
        }
        catch(ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().contains("will overwrite an existing file in the war"));
        }
        
        this.manager.installModule(ampLocation, warLocation, false, true, true);  //Now force it
        checkContentsOfFile(warLocation + "/jsp/relogin.jsp", "VERSIONONE");
        checkContentsOfFile(warLocation + "/css/main.css", "p{margin-bottom:1em;}");
        this.manager.installModule(ampLocation, warLocation, false, true, false); //install it again

    }
    
    public void testWhiteSpaceInCustomMapping()
                throws Exception
    {
        manager.setVerbose(true);
        
        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test_v3.amp");
        
        // Initial install of module
        this.manager.installModule(ampLocation, warLocation, false, false, true);
        
        List<String> files = new ArrayList<String>(10);
        files.add("/WEB-INF/classes/alfresco/module/test/module.properties");
        files.add("/WEB-INF/classes/alfresco/module/test/modifications.install");
        files.add("/WEB-INF/lib/test.jar");
        files.add("/WEB-INF/classes/alfresco/module/test/module-context.xml");
        files.add("/images/test.jpg");
        files.add("/css/test.css");
        files.add("/extra.txt");
        checkForFileExistance(warLocation, files);   
    }
    
    public void testList() throws Exception
    {
        String warLocation = getFileLocation(".war", "module/test.war");
        this.manager.listModules(warLocation);
    }

    public void testListAndInstall() throws Exception {

        String warLocation = getFileLocation(".war", "module/test.war");
        String ampLocation = getFileLocation(".amp", "module/test_v1.amp");
        String ampV2Location = getFileLocation(".amp", "module/test_v2.amp");

        TFile war = new TFile(warLocation);

        List<ModuleDetails> details = this.manager.warHelper.listModules(war);
        assertNotNull(details);
        assertEquals(details.size(), 0);

        this.manager.installModule(ampLocation, warLocation);

        details = this.manager.warHelper.listModules(war);
        assertNotNull(details);
        assertEquals(details.size(), 1);
        ModuleDetails aModule = details.get(0);
        assertEquals("test", aModule.getId());
        assertEquals("1.0", aModule.getModuleVersionNumber().toString());

        this.manager.installModule(ampV2Location, warLocation);

        details = this.manager.warHelper.listModules(war);
        assertNotNull(details);
        assertEquals(details.size(), 1);
        aModule = details.get(0);
        assertEquals("test", aModule.getId());
        assertEquals("2.0", aModule.getModuleVersionNumber().toString());

        String testAmpDepV2Location = getFileLocation(".amp", "module/dependent_on_test_v2.amp");
        String testAmp7 = getFileLocation(".amp", "module/test_v7.amp");

        this.manager.installModule(testAmpDepV2Location, warLocation, false, true, false);
        this.manager.installModule(testAmp7, warLocation, false, true, false);

        details = this.manager.warHelper.listModules(war);
        assertNotNull(details);
        assertEquals(details.size(), 3);

        //Sort them by installation date
        Collections.sort(details, new Comparator<ModuleDetails>() {
            @Override
            public int compare(ModuleDetails a, ModuleDetails b) {
                return a.getInstallDate().compareTo(b.getInstallDate());
            }
        });

        ModuleDetails installedModule = details.get(0);
        assertEquals("test", installedModule.getId());
        assertEquals("2.0", installedModule.getModuleVersionNumber().toString());

        installedModule = details.get(1);
        assertEquals("org.alfresco.module.test.dependent", installedModule.getId());
        assertEquals("2.0", installedModule.getModuleVersionNumber().toString());

        installedModule = details.get(2);
        assertEquals("forcedtest", installedModule.getId());
        assertEquals("1.0", installedModule.getModuleVersionNumber().toString());

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
    
    private String extractToDir(String extension, String location)
    {
       File tmpDir = TempFileProvider.getTempDir();

       try {
           TFile zipFile = new TFile(this.getClass().getClassLoader().getResource(location).getPath());
           TFile outDir = new TFile(tmpDir.getAbsolutePath()+"/moduleManagementToolTestDir"+System.currentTimeMillis());
           outDir.mkdir();
           zipFile.cp_rp(outDir);
           TVFS.umount(zipFile);
           return outDir.getPath();
       } catch (Exception e) {
               e.printStackTrace();
       }
       return null;
    }
    public void testNoWar() throws Exception 
    {
        String noWar = "noWar";
        String ampLocation = getFileLocation(".amp", "module/test_v1.amp");
        try
        {
            this.manager.installModule(ampLocation, noWar,false,false, false);
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().endsWith("does not exist."));
        }  
        try
        {
            this.manager.installModule(ampLocation, noWar,false,false, true);  //backup war
        }
        catch (ModuleManagementToolException exception)
        {
            assertTrue(exception.getMessage().endsWith("does not exist."));
        }       
    }
    
    private void checkForFileExistance(String warLocation, List<String> files)
    {
        for (String file : files)
        {
            File file0 = new TFile(warLocation + file);
            assertTrue("The file/dir " + file + " does not exist in the WAR.", file0.exists());
        }    
    }
    
    private void checkForFileNonExistance(String warLocation, List<String> files)
    {
        for (String file : files)
        {
            File file0 = new TFile(warLocation + file);
            assertFalse("The file/dir " + file + " does exist in the WAR.", file0.exists());
        }    
    }
    
    private void checkContentsOfFile(String location, String expectedContents)
        throws IOException
    {
        File file = new TFile(location);
        assertTrue(file.exists());  
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(new TFileInputStream(file)));
            String line = reader.readLine();
            assertNotNull(line);
            assertEquals(expectedContents, line.trim());
        }
        finally
        {
            if (reader != null)
            {
                try { reader.close(); } catch (Throwable e ) {}
            }
        }
    }
}
