
package org.alfresco.update.packaging;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
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
    private File contentsPackage;
    
    @Before
    public void setUp() throws Exception
    {
        String pkgName = System.getProperty("alfresco.update.package");
        assertNotNull("Could not determine package name.");
        updatePackage = new File(pkgName);
        
        String contentsPkgName = System.getProperty("alfresco.contents.package");
        assertNotNull("Could not determine content package name.");
        contentsPackage = new File(contentsPkgName);
    }

    @Test
    public void testPackageStructureIsAsExpected() throws ZipException, IOException
    {
        // Check the package exists before we go any further
        assertTrue("Update package does not exist.", updatePackage.exists());
        
        Set<String> paths = listFiles(updatePackage);
        
        // Are the binaries present?
        assertPathPresent(paths, "bin/alfresco-update-tool.jar");
        assertPathPresent(paths, "bin/apply_updates.sh");
        assertPathPresent(paths, "bin/apply_updates.bat");

        // Is the content sub-package present?
        assertPathPresent(paths, "update/"+contentsPackage.getName());
    }
    
    @Test
    public void testContentsPackageStructureIsAsExpected() throws ZipException, IOException
    {
        // Check the package exists before we go any further
        assertTrue("Contents package does not exist.", contentsPackage.exists());
        
        Set<String> paths = listFiles(contentsPackage);
        
        // Are the webapps present?
        assertPathPresent(paths, "assets/web-server/webapps/alfresco.war");
        assertPathPresent(paths, "assets/web-server/webapps/share.war");
    }
    
    private void assertPathPresent(Set<String> pathsToCheck, String expectedPath)
    {
        assertTrue("Expected path to be present, but was not: "+expectedPath,
                    pathsToCheck.contains(expectedPath));
    }
    
    private Set<String> listFiles(File file) throws ZipException, IOException
    {
        Set<String> paths = new TreeSet<String>();
        
        ZipFile zipFile = new ZipFile(file);
        try
        {
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements())
            {
                ZipEntry entry = e.nextElement();
                paths.add(entry.getName());
            }
        }
        finally
        {
            zipFile.close();
        }
        return paths;
    }
}
