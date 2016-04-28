/*
 * Copyright 2015-2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
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
 *   -Dalfresco.update.package.zip=target/alfresco-enterprise-update-package-2015-1-EA-SNAPSHOT.zip
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
        String pkgName = System.getProperty("alfresco.update.package.zip");
        assertNotNull("Could not determine package name.", pkgName);
        updatePackage = new File(pkgName);        
    }

    @Test
    public void testPackageStructureIsAsExpected() throws ZipException, IOException
    {
        // Check the package exists before we go any further
        assertTrue("Update package does not exist.", updatePackage.exists());
        
        Set<String> paths = listFiles(updatePackage); 

        assertTrue("too few paths in the update package", paths.size() > 3);
        
        String firstPath = (String)paths.toArray()[0];
        String dirs[] = firstPath.split("/");
       
        // Are the binaries present?
        assertPathPresent(paths, dirs[0] + "/lib/alfresco-update-tool.jar");
        assertPathPresent(paths, dirs[0] + "/apply_updates.sh");
        assertPathPresent(paths, dirs[0] + "/apply_updates.bat");

        // Is the content sub-package present?
        assertPathPresent(paths, dirs[0] + "/resources/war/alfresco.war");
        assertPathPresent(paths, dirs[0] + "/resources/war/share.war");
        
        // Is the mmt in the correct place ?
        assertPathPresent(paths, dirs[0] + "/resources/distribution/common/bin/alfresco-mmt.jar");
        assertPathPresent(paths, dirs[0] + "/resources/distribution/common/bin/alfresco-spring-encryptor.jar");
        
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

