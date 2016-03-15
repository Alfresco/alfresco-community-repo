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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the tgz format packaging.
 * 
 * @author Matt Ward
 */
public class TgzFormatIntegrationTest extends AbstractIntegrationTest
{
    private boolean found;
    
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }

    @Test
    public void applyUpdatesScriptHasExecutableBitsSet() throws FileNotFoundException, ArchiveException, IOException, CompressorException
    {
        if (runningOnWindows())
        {
            // This is a Unix only test.
            return;
        }
        
        File archive = new File(targetDir, "alfresco-enterprise-update-package-"+version+".tgz");
        assertTrue("File does not exist: "+archive, archive.exists());
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(archive);

            handleArchiveEntries(fis, new ArchiveEntryHandler()
            {                
                @Override
                public boolean handle(TarArchiveEntry entry)
                {
                    System.out.println("Handling tar entry: "+entry.getName());
                    if (entry.getName().equals("bin/apply_updates.sh"))
                    {
                        System.out.println("Found the unix shell wrapper script.");
                        
                        final int expectedPerms = 0755;
                        // Other bits may be set, but check 755 octal are set.
                        System.out.println("File has permissions: "+Integer.toString(entry.getMode(), 8));
                        assertEquals(expectedPerms, entry.getMode() & expectedPerms);          
                        found = true;
                    }
                    return !found;
                }
            });
            
            assertTrue("bin/apply_updates.sh is a required file.", found);
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
        boolean handle(TarArchiveEntry entry);
    }
    
    private void handleArchiveEntries(InputStream raw, ArchiveEntryHandler handler) throws ArchiveException, IOException, CompressorException
    {
        try
        (
            BufferedInputStream bis = new BufferedInputStream(raw);
            CompressorInputStream gzIs = new CompressorStreamFactory().createCompressorInputStream(bis);
            BufferedInputStream bgzIs = new BufferedInputStream(gzIs);
            TarArchiveInputStream aris = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(bgzIs);
        )
        {
            TarArchiveEntry entry = null;
            boolean carryOn = true;
            while (carryOn && (entry = aris.getNextTarEntry()) != null)
            {
                carryOn = handler.handle(entry);
            }
        }
    }
}
