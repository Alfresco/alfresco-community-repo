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

    @Ignore
    @Test
    public void bigDiff() throws IOException
    {
        Path path1 = Paths.get("/Users/MWard/dev2/alf-installs/alf-5.1-b667");
        Path path2 = Paths.get("/Users/MWard/dev2/alf-installs/alf-5.1-b669");
        
        Set<String> ignores = new HashSet<>();
        ignores.add("alf_data/postgresql/**");
        ignores.add("META-INF/MANIFEST.MF");
        ignores.add("META-INF/maven/**");
        ignores.add("README.txt");
        ignores.add("uninstall.app/**");

        // All the patterns will be applied to these files, e.g. they will all have differences
        // in absolute path references ignored.
        Set<String> ignoreSpecialDifferences = new HashSet<>();
        ignoreSpecialDifferences.add("common/bin/**");
        ignoreSpecialDifferences.add("common/include/**/*.h");
        ignoreSpecialDifferences.add("common/lib/**/*.pc");
        ignoreSpecialDifferences.add("common/lib/**/*.la");
        ignoreSpecialDifferences.add("libreoffice.app/Contents/Resources/bootstraprc");
        ignoreSpecialDifferences.add("postgresql/bin/**");
        ignoreSpecialDifferences.add("**/*.sh");
        ignoreSpecialDifferences.add("**/*.bat");
        ignoreSpecialDifferences.add("**/*.ini");
        ignoreSpecialDifferences.add("**/*.properties");
        ignoreSpecialDifferences.add("**/*.xml");
        ignoreSpecialDifferences.add("**/*.sample");
        ignoreSpecialDifferences.add("**/*.txt");

        FileTreeCompare comparator = new FileTreeCompareImpl(ignores, ignoreSpecialDifferences);
        ResultSet resultSet = comparator.compare(path1, path2);
        
        Path file = Files.createTempFile(getClass().getSimpleName(), ".html");
        HtmlResultFormatter formatter = new HtmlResultFormatter();
        formatter.setDifferencesOnly(true);
        try(FileOutputStream fos = new FileOutputStream(file.toFile());
            BufferedOutputStream bos = new BufferedOutputStream(fos))
        {
            formatter.format(resultSet, bos);
        }
        System.out.println("File: "+file);
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
