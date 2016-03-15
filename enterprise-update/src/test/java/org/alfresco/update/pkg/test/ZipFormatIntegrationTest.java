/*
 * Copyright 2005-2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.pkg.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.CompressorException;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the tgz format packaging.
 * 
 * To run these tests in Eclipse, add the following to the "VM arguments" for the junit Run Configuration:
 * <pre>
 *   -Dalfresco.update.package.zip=target/alfresco-enterprise-update-package-2015-1-EA-SNAPSHOT.zip
 * </pre>
 * 
 * @author Matt Ward
 */
public class ZipFormatIntegrationTest extends AbstractIntegrationTest
{   
    public final String ARTIFACT_NAME="alfresco-enterprise-update-package-";

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }
    
    @Test
    public void applyUpdatesScriptHasExecutableBitsSet() throws FileNotFoundException, ArchiveException, IOException, CompressorException
    {	    
        File archive = new File(targetDir, ARTIFACT_NAME+version+".zip");
        assertTrue("File does not exist: "+archive, archive.exists());
        try (ZipFile zipFile = new ZipFile(archive))
        {
            Enumeration<ZipArchiveEntry> e = zipFile.getEntries();
            boolean found = false;
            while (!found && e.hasMoreElements())
            {
                ZipArchiveEntry entry = e.nextElement();
                File f = new File(entry.getName());
                if (f.getName().equalsIgnoreCase("apply_updates.sh"))
                {
                    found = true;
                    System.out.println("Found the unix shell wrapper script.");
                    final int expectedPerms = 0755;
                    // Other bits may be set, but check 755 octal are set.
                    System.out.println("File has permissions: "+Integer.toString(entry.getUnixMode(), 8));  
                    assertEquals(expectedPerms, entry.getUnixMode() & expectedPerms);
                }
            }          
        }
    }
}
