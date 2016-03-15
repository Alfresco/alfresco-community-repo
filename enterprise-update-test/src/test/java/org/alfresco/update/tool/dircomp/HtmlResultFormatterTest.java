/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link HtmlResultFormatter} class.
 * <p>
 * TODO: currently these aren't tests so much as useful utilities to help with manual testing.
 * 
 * @author Matt Ward
 */
public class HtmlResultFormatterTest
{
    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void canFormatToHTML() throws IOException
    {
        ResultSet resultSet = new ResultSet();
        List<Result> results = resultSet.results;
        addResult(results, "/t1/a", "/t2/a", true);
        addResult(results, "/t1/a/b", "/t2/a/b", true);
        addResult(results, "/t1/a/c", "/t2/a/c", true);
        addResult(results, "/t1/a/b/c/something.txt", "/t2/a/b/c/something.txt", true);
        addResult(results, "/t1/a/b/c/another.txt", "/t2/a/b/c/another.txt", false);
        addResult(results, null, "/t2/a/b/c/blah.txt", false);
        addResult(results, "/t1/dir-only-in-p1", null, false);
        addResult(results, null, "/t2/dir-only-in-p2", false);

        resultSet.stats.suppressedDifferenceCount = 2;
        resultSet.stats.differenceCount = 4;
        resultSet.stats.ignoredFileCount = 0;
        resultSet.stats.resultCount = results.size();

        try(ByteArrayOutputStream os = new ByteArrayOutputStream())
        {
            HtmlResultFormatter formatter = new HtmlResultFormatter();
            formatter.format(resultSet, os);
            System.out.println(os.toString());
            
            // Uncomment to write to file
//            Path file = Files.createTempFile(getClass().getSimpleName(), ".html");
//            FileUtils.write(file.toFile(), os.toString());
//            System.out.println("File: "+file);
        }
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
