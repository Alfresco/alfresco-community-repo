
package org.alfresco.update.packaging;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Before;
import org.junit.Test;

/**
 * Update Package sanity tests.
 * <p>
 * To run these tests in Eclipse, add the following to the "VM arguments" for the junit Run Configuration:
 * <pre>
 *   -Dalfresco.update.package=target/alfresco-enterprise-update-package-2015-1-EA-SNAPSHOT.zip
 *   -Dalfresco.contents.package=target/update-contents-2015-1-EA-SNAPSHOT.zip
 * </pre>
 * 
 * ...or similar, depending on current version etc. There probably is a better way.
 * 
 * @author Matt Ward
 */
public class PackagingIntegrationTest
{
    private File updatePackage;

    
    @Before
    public void setUp() throws Exception
    {
        String pkgName = System.getProperty("alfresco.update.package");
        assertNotNull("Could not determine package name.", pkgName);
        updatePackage = new File(pkgName);   
    }

    @Test
    public void testPackageStructureIsAsExpected() throws ZipException, IOException
    {
        // Check the package exists before we go any further
        assertTrue("Update package does not exist.", updatePackage.exists());
        
        Set<String> paths = listFiles(updatePackage);
        
        // Are the binaries present?
        assertPathPresent(paths, "lib/alfresco-update-tool.jar");
        assertPathPresent(paths, "apply_updates.sh");
        assertPathPresent(paths, "apply_updates.bat");

        // Is the content sub-package present?
        assertPathPresent(paths, "resources");
         assertPathPresent(paths, "alfresco.war");
    }
    
    private void assertPathPresent(Set<String> pathsToCheck, String expectedPath)
    {
        assertTrue("Expected path to be present, but was not: "+expectedPath,
                    pathsToCheck.contains(expectedPath));
    }
    
    private Set<String> listFiles(File file) throws ZipException, IOException
    {
        Set<String> paths = new TreeSet<String>();
            
        File[] files = file.listFiles();
            
        for(File x : files)
        {
            if(x.isFile())
            {
                paths.add(x.getAbsolutePath());
            }
            if(x.isDirectory())
            {
                paths.addAll(listFiles(x));
            }
        }
              
        return paths;
    }
}
