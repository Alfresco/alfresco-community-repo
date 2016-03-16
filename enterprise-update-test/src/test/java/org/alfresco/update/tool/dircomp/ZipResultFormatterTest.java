package org.alfresco.update.tool.dircomp;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link ZipResultFormatter} class.
 * <p>
 * 
 * @author Mark Rogers
 */
public class ZipResultFormatterTest
{
    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void canFormatToZip() throws IOException
    {
        ResultSet resultSet = new ResultSet();
        List<Result> results = resultSet.results;
        
        URL srcDir = getClass().getClassLoader().getResource("dir_compare/allowed_differences/tree1");
        assertNotNull(srcDir.getPath());
        
        File f = new File(srcDir.getPath());
        
        File[] files = f.listFiles();
        for(File file : files)
        {
            addResult(results, file.getAbsolutePath(), null, false);
        }
        
        resultSet.stats.suppressedDifferenceCount = 2;
        resultSet.stats.differenceCount = 4;
        resultSet.stats.ignoredFileCount = 0;
        resultSet.stats.resultCount = results.size();
        
        ZipResultFormatter zof = new ZipResultFormatter();
        
        Path file = Files.createTempFile(getClass().getSimpleName(), ".zip");
        
        File zipFile = file.toFile();
        zipFile.createNewFile();
        zipFile.deleteOnExit();
        
        ZipResultFormatter zformatter = new ZipResultFormatter();
        
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos))
        {
            zof.format(resultSet, zos);
        }
        
        assertTrue(zipFile.length() > 0);
        
    }
    
    private void addResult(List<Result> results, String p1, String p2, boolean contentMatch)
    {
        Result r = new Result();
        r.p1 = p1 != null ? Paths.get(p1) : null;
        r.p2 = p2 != null ? Paths.get(p2) : null;
        r.equal = contentMatch;
        results.add(r);
    }

}
