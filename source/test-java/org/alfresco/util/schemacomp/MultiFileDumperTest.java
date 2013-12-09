/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.util.schemacomp;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.util.TempFileProvider;
import org.alfresco.util.schemacomp.MultiFileDumper.DbToXMLFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the MultiFileDumper class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiFileDumperTest
{
    private @Mock DbToXMLFactory dbToXMLFactory;
    private @Mock DbToXML dbToXMLForA;
    private @Mock DbToXML dbToXMLForB;
    private @Mock DbToXML dbToXMLForC;
    
    
    @Test(expected=IllegalArgumentException.class)
    public void exceptionThrownWhenZeroPrefixesUsed()
    {
        // Shouldn't be able to construct a dumper with no prefixes to dump.
        new MultiFileDumper(new String[] {}, TempFileProvider.getTempDir(), "", dbToXMLFactory, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void exceptionThrownWhenNullPrefixListUsed()
    {
        // Shouldn't be able to construct a dumper with no prefixes to dump.
        new MultiFileDumper(null, TempFileProvider.getTempDir(), "", dbToXMLFactory, null);
    }
    
    
    @Test
    public void canDumpSchemaToFiles()
    {
        String[] prefixes = new String[] { "a_", "b_", "c_" };
        File directory = TempFileProvider.getTempDir();
        String fileNamePattern = "SchemaDump-MySQL-{0}-";
        
        MultiFileDumper dumper = new MultiFileDumper(prefixes, directory, fileNamePattern, dbToXMLFactory, null);
        
        when(dbToXMLFactory.create(argThat(isFileNameStartingWith("SchemaDump-MySQL-a_-")), eq("a_"))).
            thenReturn(dbToXMLForA);
        when(dbToXMLFactory.create(argThat(isFileNameStartingWith("SchemaDump-MySQL-b_-")), eq("b_"))).
            thenReturn(dbToXMLForB);
        when(dbToXMLFactory.create(argThat(isFileNameStartingWith("SchemaDump-MySQL-c_-")), eq("c_"))).
            thenReturn(dbToXMLForC);

        
        List<File> files = dumper.dumpFiles();
        Iterator<File> it = files.iterator();
        assertPathCorrect("SchemaDump-MySQL-a_-", directory, it.next());
        assertPathCorrect("SchemaDump-MySQL-b_-", directory, it.next());
        assertPathCorrect("SchemaDump-MySQL-c_-", directory, it.next());
        
        verify(dbToXMLForA).execute();
        verify(dbToXMLForB).execute();
        verify(dbToXMLForC).execute();
    }
    
    @Test
    public void canDumpSchemaToFilesForDefaultDBPrefixes()
    {
        File directory = TempFileProvider.getTempDir();
        String fileNamePattern = "SchemaDump-MySQL-{0}-";
        
        MultiFileDumper dumper = new MultiFileDumper(directory, fileNamePattern, dbToXMLFactory, null);
        
        Map<String, DbToXML> xmlExporters = new HashMap<String, DbToXML>(MultiFileDumper.DEFAULT_PREFIXES.length);
        
        // Each of the prefixes will be used to call DbToXMLFactory.create(...)
        for (String prefix : MultiFileDumper.DEFAULT_PREFIXES)
        {
            DbToXML dbToXML = mock(DbToXML.class); 
            xmlExporters.put(prefix, dbToXML);
            when(dbToXMLFactory.create(any(File.class), eq(prefix))).thenReturn(dbToXML);
        }

        dumper.dumpFiles();
        
        // Check that each DEFAULT_PREFIX prefix resulted in its associated DbToXML object being used.
        for (DbToXML dbToXML : xmlExporters.values())
        {            
            verify(dbToXML).execute();
        }
    }
    
    private ArgumentMatcher<File> isFileNameStartingWith(String startOfName)
    {
        return new FileNameBeginsWith(startOfName);
    }
    
    private static class FileNameBeginsWith extends ArgumentMatcher<File>
    {
        private final String startOfName;
        
        public FileNameBeginsWith(String startOfName)
        {
            this.startOfName = startOfName;
        }

        @Override
        public boolean matches(Object arg)
        {
            if (arg != null)
            {
                File fileArg = (File) arg;
                return fileArg.getName().startsWith(startOfName);
            }
            return false;
        }
    }
    
    /**
     * Check that actualFile has the expected directory and file name prefix, e.g. if the actual file
     * is /temp/my_file_123.xml and we call:
     * <pre>
     *    assertPathCorrect("my_file_", new File("/tmp"), actualFile)
     * </pre>
     * Then the assertion should hold true.
     * 
     * @param expectedFileNamePrefix
     * @param expectedDirectory
     * @param actualFile
     */
    private void assertPathCorrect(String expectedFileNamePrefix, File expectedDirectory, File actualFile)
    {
        File expectedPath = new File(expectedDirectory, expectedFileNamePrefix);
        if (!actualFile.getAbsolutePath().startsWith(expectedPath.getAbsolutePath()))
        {
            String failureMsg = "File path " + actualFile.getAbsolutePath() +
                " does not start as expected: " + expectedPath.getAbsolutePath();
            Assert.fail(failureMsg);
        }
    }
}
