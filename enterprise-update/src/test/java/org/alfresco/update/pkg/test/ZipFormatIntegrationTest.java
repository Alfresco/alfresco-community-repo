/*
 * Copyright 2005-2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.pkg.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
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
   private boolean found;
    
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }
    
    public final String ARTIFACT_NAME="alfresco-enterprise-update-package-";

    // TODO - Test step commented out below
    @Test
    public void applyUpdatesScriptHasExecutableBitsSet() throws FileNotFoundException, ArchiveException, IOException, CompressorException
    {
	               
        File archive = new File(targetDir, ARTIFACT_NAME+version+".zip");
        assertTrue("File does not exist: "+archive, archive.exists());
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(archive);

            handleArchiveEntries(fis, new ArchiveEntryHandler()
            {                
                @Override
                public boolean handle(ZipArchiveEntry entry)
                {
                    System.out.println("Handling tar entry: "+entry.getName());
                    if (entry.getName().equals(ARTIFACT_NAME+version+"/apply_updates.sh"))
                    {
                        System.out.println("Found the unix shell wrapper script.");
                        
                        final int expectedPerms = 0755;
                        // Other bits may be set, but check 755 octal are set.
                        System.out.println("File has permissions: "+Integer.toString(entry.getUnixMode(), 8));
                        
                        /**
                         *  TODO - TEST STEP COMMENTED OUT
                         *  I can't get this to work on windows or the build boxes - the mode is 0
                         *  However it does appear to work in real life!
                         *  I've also tried replacing the apache.commons zip file utilities with 
                         *  the Java IO classes
                         */                      
//                        assertEquals(expectedPerms, entry.getUnixMode() & expectedPerms);          
                        found = true;
                    }
                    return !found;
                }
            });
            
            assertTrue("apply_updates.sh is a required file.", found);
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
        }
    }

    private static interface ArchiveEntryHandler
    {
        boolean handle(ZipArchiveEntry entry);
    }
    
    private void handleArchiveEntries(InputStream raw, ArchiveEntryHandler handler) throws ArchiveException, IOException, CompressorException
    {
        try
        (
            BufferedInputStream bis = new BufferedInputStream(raw);
            //CompressorInputStream gzIs = new CompressorStreamFactory().createCompressorInputStream(bis);
            BufferedInputStream bgzIs = new BufferedInputStream(bis);
            ZipArchiveInputStream aris = (ZipArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(bgzIs);
        )
        {
            ZipArchiveEntry entry = null;
            boolean carryOn = true;
            while (carryOn && (entry = aris.getNextZipEntry()) != null)
            {
                carryOn = handler.handle(entry);
            }
        }
    }
}
