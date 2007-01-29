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
package org.alfresco.repo.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.util.FileCopyUtils;

import de.schlichtherle.io.DefaultRaesZipDetector;
import de.schlichtherle.io.FileOutputStream;
import de.schlichtherle.io.ZipDetector;

/**
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
        
        System.out.println(warLocation);
        
        // Initial install of module
        this.manager.installModule(ampLocation, warLocation);
        
        // Check that the war has been modified correctly
        List<String> files = new ArrayList<String>(10);
        files.add("/WEB-INF/classes/alfresco/module/module-test.install");
        files.add("/WEB-INF/classes/alfresco/module/module-test-modifications.install");
        files.add("/WEB-INF/lib/test.jar");
        files.add("/WEB-INF/classes/alfresco/module/test/module-context.xml");
        files.add("/WEB-INF/classes/alfresco/module/test");
        files.add("/WEB-INF/licenses/license.txt");
        files.add("/scripts/test.js");
        files.add("/images/test.jpg");
        files.add("/jsp/test.jsp");
        files.add("/css/test.css");
        checkForFileExistance(warLocation, files);   
        
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
        // TODO
        
        // Try and install and earlier version
        // TODO          
        
        
    }

    private String getFileLocation(String extension, String location)
        throws IOException
    {
        File file = File.createTempFile("moduleManagementToolTest-", extension);        
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location);
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
}
