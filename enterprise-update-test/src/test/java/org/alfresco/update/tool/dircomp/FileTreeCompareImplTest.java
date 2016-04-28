/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;

/**
 * Tests for the {@link FileTreeCompareImpl} class.
 * 
 * @author Matt Ward
 */
public class FileTreeCompareImplTest
{
    FileTreeCompareImpl comparator;
    
    @Before
    public void setUp() throws Exception
    {
        comparator = new FileTreeCompareImpl(new HashSet<String>(), new HashSet<String>());
    }

    @Test
    public void canGetSortedPathSet() throws IOException
    {
        Path tree = pathFromClasspath("dir_compare/simple_file_folders/tree1");
        SortedPathSet paths = comparator.sortedPaths(tree);
        Iterator<Path> it = paths.iterator();
        
        System.out.println("Paths:");
        for (Path p : paths)
        {
            System.out.println("\t"+p);
        }
        
        assertEquals(11, paths.size());
        
        assertEquals("a", unixPathStr(tree, it.next()));
        assertEquals("b", unixPathStr(tree, it.next()));
        assertEquals("b/blah.txt", unixPathStr(tree, it.next()));
        assertEquals("c", unixPathStr(tree, it.next()));
        assertEquals("c/c1", unixPathStr(tree, it.next()));
        assertEquals("c/c1/commands.bat", unixPathStr(tree, it.next()));
        assertEquals("c/c1/commands.sh", unixPathStr(tree, it.next()));
        assertEquals("c/c2", unixPathStr(tree, it.next()));
        assertEquals("c/c2/Aardvark.java", unixPathStr(tree, it.next()));
        assertEquals("c/c2/Banana.java", unixPathStr(tree, it.next()));
        assertEquals("d", unixPathStr(tree, it.next()));
    }
    
    private String unixPathStr(Path root, Path path)
    {
        // Allow test to run on Windows also
        String pathStr = path.toString();
        pathStr = pathStr.replace(File.separatorChar, '/');
        return pathStr;
    }

    @Test
    public void canDiffSimpleTreesOfFilesAndFolders()
    {
        Path tree1 = pathFromClasspath("dir_compare/simple_file_folders/tree1");
        Path tree2 = pathFromClasspath("dir_compare/simple_file_folders/tree2");
        
        ResultSet resultSet = comparator.compare(tree1, tree2);
        
        System.out.println("Comparison results:");
        for (Result r : resultSet.results)
        {
            System.out.println("\t"+r);
        }
        
        // One result for each relative file/folder
        assertEquals(13, resultSet.results.size());
        assertEquals(13, resultSet.stats.resultCount);

        Iterator<Result> rit = resultSet.results.iterator();
        // TODO: currently all of the files are in one, other or both but where they
        // are in both, the file *contents* are identical.
        // TODO: evolve test data and functionality to cope with different file contents.
        assertResultEquals(tree1.resolve("a"), tree2.resolve("a"), true, rit.next());
        assertResultEquals(null, tree2.resolve("a/story.txt"), false, rit.next());
        assertResultEquals(tree1.resolve("b"), tree2.resolve("b"), true, rit.next());
        assertResultEquals(tree1.resolve("b/blah.txt"), tree2.resolve("b/blah.txt"), true, rit.next());
        assertResultEquals(tree1.resolve("c"), tree2.resolve("c"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c1"), tree2.resolve("c/c1"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c1/commands.bat"), tree2.resolve("c/c1/commands.bat"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c1/commands.sh"), tree2.resolve("c/c1/commands.sh"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c2"), tree2.resolve("c/c2"), true, rit.next());
        // Aardvark.java appears in both trees but is not the same!
        assertResultEquals(tree1.resolve("c/c2/Aardvark.java"), tree2.resolve("c/c2/Aardvark.java"), false, rit.next());
        assertResultEquals(tree1.resolve("c/c2/Banana.java"), null, false, rit.next());
        assertResultEquals(tree1.resolve("d"), null, false, rit.next());
        assertResultEquals(null, tree2.resolve("e"), false, rit.next());
    }

    /**
     * A "learning test" allowing me to check my assumptions and document the expected behaviour.
     */
    @Test
    public void testAntPathMatcher()
    {
        AntPathMatcher matcher = new AntPathMatcher();
        assertTrue(matcher.match("**/common/lib/**/*.pc", "prefix/common/lib/pkgconfig/ImageMagick++-6.Q16.pc"));
        assertFalse(matcher.match("**/common/lib/**/*.pc", "/absolute/prefix/common/lib/pkgconfig/ImageMagick++-6.Q16.pc"));
        assertTrue(matcher.match("/**/common/lib/**/*.pc", "/absolute/prefix/common/lib/pkgconfig/ImageMagick++-6.Q16.pc"));
        assertTrue(matcher.match("common/lib/**/*.pc", "common/lib/pkgconfig/Wand.pc"));
        assertTrue(matcher.match("**/*.pc", "common/lib/pkgconfig/Wand.pc"));
        assertFalse(matcher.match("*.pc", "common/lib/pkgconfig/Wand.pc"));

        assertTrue(matcher.match("libreoffice.app/Contents/Resources/bootstraprc", "libreoffice.app/Contents/Resources/bootstraprc"));
        assertTrue(matcher.match("*.sh", "alfresco.sh"));
        assertFalse(matcher.match("*.sh", "a/different/alfresco.sh"));

        // Windows matcher
        // It seems that changing the path separator on an instance that's already been
        // used isn't a good idea due to pattern caching.
        matcher = new AntPathMatcher("\\");
        assertTrue(matcher.match("**\\common\\lib\\**\\*.pc", "prefix\\common\\lib\\pkgconfig\\ImageMagick++-6.Q16.pc"));
        assertTrue(matcher.match("\\**\\common\\lib\\**\\*.pc", "\\absolute\\prefix\\common\\lib\\pkgconfig\\ImageMagick++-6.Q16.pc"));

        assertTrue(matcher.match("b\\blah.txt", "b\\blah.txt"));
    }

    @Test
    public void canIgnoreSpecifiedPaths()
    {
        Path tree1 = pathFromClasspath("dir_compare/simple_file_folders/tree1");
        Path tree2 = pathFromClasspath("dir_compare/simple_file_folders/tree2");
        
        Set<String> ignorePaths = new HashSet<>();
        ignorePaths.add(toPlatformPath("b/blah.txt"));
        ignorePaths.add(toPlatformPath("c/c2/**"));
        ignorePaths.add(toPlatformPath("d/**"));
        ignorePaths.add(toPlatformPath("e/**"));
        comparator = new FileTreeCompareImpl(ignorePaths, new HashSet<String>());

        // Perform the comparison
        ResultSet resultSet = comparator.compare(tree1, tree2);
        
        System.out.println("Comparison results:");
        for (Result r : resultSet.results)
        {
            System.out.println("\t"+r);
        }
        
        Iterator<Result> rit = resultSet.results.iterator();
        assertResultEquals(tree1.resolve("a"), tree2.resolve("a"), true, rit.next());
        assertResultEquals(null, tree2.resolve("a/story.txt"), false, rit.next());
        assertResultEquals(tree1.resolve("b"), tree2.resolve("b"), true, rit.next());
        // No b/blah.txt here.
        assertResultEquals(tree1.resolve("c"), tree2.resolve("c"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c1"), tree2.resolve("c/c1"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c1/commands.bat"), tree2.resolve("c/c1/commands.bat"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c1/commands.sh"), tree2.resolve("c/c1/commands.sh"), true, rit.next());
        // No c/c2, c/c2/Aardvark.java, c/c2/Banana.java, d or e here.

        List<Result> results = resultSet.results;
        assertResultNotPresent(tree1.resolve("b/blah.txt"), tree2.resolve("b/blah.txt"), true, results);
        assertResultNotPresent(tree1.resolve("c/c2"), tree2.resolve("c/c2"), true, results);
        assertResultNotPresent(tree1.resolve("c/c2/Aardvark.java"), tree2.resolve("c/c2/Aardvark.java"), false, results);
        assertResultNotPresent(tree1.resolve("c/c2/Banana.java"), null, false, results);
        assertResultNotPresent(tree1.resolve("d"), null, false, results);
        assertResultNotPresent(null, tree2.resolve("e"), false, results);
        assertEquals(7, results.size());
        
        // TODO: What about paths within war/jar/zip files?
        // ...at the moment, if we specify a path of "mydir/README.txt" to be ignored,
        // this will be ignored in the main tree, e.g. <tree1>/mydir/README.txt but also
        // within sub-trees if there is a match, e.g. <expanded alfresco.war>/mydir/README.txt
    }

    @Test
    public void canSpecifyFilesThatShouldHaveCertainDifferencesAllowed() throws IOException
    {
        Path tree1 = pathFromClasspath("dir_compare/allowed_differences/tree1");
        Path tree2 = pathFromClasspath("dir_compare/allowed_differences/tree2");

        // Check that two identical trees are... identical!
        ResultSet resultSet = comparator.compare(tree1, tree2);
        
        System.out.println("Comparison results:");
        for (Result r : resultSet.results)
        {
            System.out.println("\t"+r);
        }
        assertEquals(0, resultSet.stats.differenceCount);
        assertEquals(0, resultSet.stats.ignoredFileCount);
        assertEquals(4, resultSet.stats.resultCount);
        assertEquals(4, resultSet.results.size());

        // Now add files that are different only in there use of tree1 and tree2's absolute paths.
        File t1File = new File(tree1.toFile(), "different.txt");
        t1File.deleteOnExit();
        FileUtils.write(t1File, sampleText(tree1.toAbsolutePath().toString()));

        File t2File = new File(tree2.toFile(), "different.txt");
        t2File.deleteOnExit();
        FileUtils.write(t2File, sampleText(tree2.toAbsolutePath().toString()));
        
        // Now add a module.properties that are different in their "installDate" property only.
        File t3File = new File(tree1.toFile(), "module.properties");
        t3File.deleteOnExit();
        Date date = new Date();
        FileUtils.write(t3File, sampleModuleProperties("2016-02-29T16\\:26\\:18.053Z"));
        
        File t4File = new File(tree2.toFile(), "module.properties");
        t4File.deleteOnExit();
        FileUtils.write(t4File, sampleModuleProperties("2016-02-28T14\\:30\\:14.035Z"));
        

        // Perform the comparison
        comparator = new FileTreeCompareImpl(new HashSet<String>(), new HashSet<String>());
        resultSet = comparator.compare(tree1, tree2);
        System.out.println("Comparison results:");
        for (Result r : resultSet.results)
        {
            System.out.println("\t"+r);
        }

        // We should see a difference
        assertEquals(0, resultSet.stats.suppressedDifferenceCount);
        assertEquals(2, resultSet.stats.differenceCount);
        assertEquals(0, resultSet.stats.ignoredFileCount);
        assertEquals(6, resultSet.stats.resultCount);
        assertEquals(6, resultSet.results.size());

        Iterator<Result> rit = resultSet.results.iterator();
        assertResultEquals(tree1.resolve("different.txt"), tree2.resolve("different.txt"), false, rit.next());

        // Perform the comparison again, but after allowing the files to be different.
        Set<String> allowedDiffsPaths = new HashSet<>();
        allowedDiffsPaths.add(toPlatformPath("**/*.txt"));
        allowedDiffsPaths.add(toPlatformPath("**/module.properties"));

        // Perform the comparison, this time with some allowed differences
        comparator = new FileTreeCompareImpl(new HashSet<String>(), allowedDiffsPaths);
        resultSet = comparator.compare(tree1, tree2);

        // We should see a difference - but it is in the 'suppressed' list.
        assertEquals(2, resultSet.stats.suppressedDifferenceCount);
        assertEquals(0, resultSet.stats.differenceCount);
        assertEquals(0, resultSet.stats.ignoredFileCount);
        assertEquals(6, resultSet.stats.resultCount);
        assertEquals(6, resultSet.results.size());

        rit = resultSet.results.iterator();
        assertResultEquals(tree1.resolve("different.txt"), tree2.resolve("different.txt"), true, rit.next());
    }

    private String sampleText(String absPath)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("This is some example text\n");
        sb.append("...in tree: "+absPath);
        sb.append(" ...and here is some more text.\n");
        sb.append("...but wait! here's an absolute path again:"+absPath+", yes.");
        sb.append("The End.");
        return sb.toString();
    }
    
    private String sampleModuleProperties(String installDateAsString)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("# " + installDateAsString + "\n");
        sb.append("module.id=org.alfresco.integrations.share.google.docs\n");
        sb.append("module.version=3.0.3\n");
        sb.append("module.buildnumber=4ent\n");
        sb.append("module.title=Alfresco / Google Docs Share Module\n");
        sb.append("module.description=The Share side artifacts of the Alfresco / Google Docs Integration.\n");
        sb.append("module.repo.version.min=5.0.0\n");
        sb.append("module.repo.version.max=5.99.99\n");
        sb.append("module.installState=INSTALLED\n");
        // this is the problem we are trying to solve
        sb.append("module.installDate=" + installDateAsString + "\n");

        return sb.toString();
    }

    @Test
    public void canDiffTreesContainingWarFiles()
    {
        Path tree1 = pathFromClasspath("dir_compare/file_folders_plus_war/tree1");
        Path tree2 = pathFromClasspath("dir_compare/file_folders_plus_war/tree2");
        
        ResultSet resultSet = comparator.compare(tree1, tree2);
        
        System.out.println("Comparison results:");
        for (Result r : resultSet.results)
        {
            System.out.println("\t"+r);
        }

        // The 14 top-level results + 17 sub-results.
        assertEquals(31, resultSet.stats.resultCount);

        // One result for each relative file/folder
        assertEquals(14, resultSet.results.size());


        Iterator<Result> rit = resultSet.results.iterator();
        // TODO: currently all of the files are in one, other or both but where they
        // are in both, the file *contents* are identical.
        // TODO: evolve test data and functionality to cope with different file contents.
        assertResultEquals(tree1.resolve("a"), tree2.resolve("a"), true, rit.next());
        assertResultEquals(null, tree2.resolve("a/story.txt"), false, rit.next());
        assertResultEquals(tree1.resolve("b"), tree2.resolve("b"), true, rit.next());
        
        // Examine the results of the war file comparison
        Result result = rit.next();
        // The WAR files are different.
        assertResultEquals(
                    tree1.resolve("b/alfresco-testdata-webapp.war"),
                    tree2.resolve("b/alfresco-testdata-webapp.war"),
                    false,
                    result);
        List<Result> subResults = result.subResults;
        System.out.println("subResults:");
        for (Result r : subResults)
        {
            System.out.println("\t"+r);
        }
        Iterator<Result> subIt = subResults.iterator();
        Path subTree1 = result.subTree1;
        Path subTree2 = result.subTree2;
        assertEquals(17, subResults.size());
        assertResultEquals(subTree1.resolve("META-INF"), subTree2.resolve("META-INF"), true, subIt.next());
        assertResultEquals(subTree1.resolve("META-INF/MANIFEST.MF"), subTree2.resolve("META-INF/MANIFEST.MF"), false, subIt.next());
        assertResultEquals(subTree1.resolve("META-INF/maven"), subTree2.resolve("META-INF/maven"), true, subIt.next());
        assertResultEquals(subTree1.resolve("META-INF/maven/org.alfresco.dummy"), subTree2.resolve("META-INF/maven/org.alfresco.dummy"), true, subIt.next());
        assertResultEquals(subTree1.resolve("META-INF/maven/org.alfresco.dummy/alfresco-testdata-webapp"), subTree2.resolve("META-INF/maven/org.alfresco.dummy/alfresco-testdata-webapp"), true, subIt.next());
        assertResultEquals(subTree1.resolve("META-INF/maven/org.alfresco.dummy/alfresco-testdata-webapp/pom.properties"), subTree2.resolve("META-INF/maven/org.alfresco.dummy/alfresco-testdata-webapp/pom.properties"), false, subIt.next());
        assertResultEquals(subTree1.resolve("META-INF/maven/org.alfresco.dummy/alfresco-testdata-webapp/pom.xml"), subTree2.resolve("META-INF/maven/org.alfresco.dummy/alfresco-testdata-webapp/pom.xml"), true, subIt.next());
        assertResultEquals(subTree1.resolve("WEB-INF"), subTree2.resolve("WEB-INF"), true, subIt.next());
        assertResultEquals(subTree1.resolve("WEB-INF/classes"), subTree2.resolve("WEB-INF/classes"), true, subIt.next());
        assertResultEquals(subTree1.resolve("WEB-INF/classes/org"), subTree2.resolve("WEB-INF/classes/org"), true, subIt.next());
        assertResultEquals(subTree1.resolve("WEB-INF/classes/org/alfresco"), subTree2.resolve("WEB-INF/classes/org/alfresco"), true, subIt.next());
        assertResultEquals(subTree1.resolve("WEB-INF/classes/org/alfresco/testdata"), subTree2.resolve("WEB-INF/classes/org/alfresco/testdata"), true, subIt.next());
        assertResultEquals(subTree1.resolve("WEB-INF/classes/org/alfresco/testdata/webapp"), subTree2.resolve("WEB-INF/classes/org/alfresco/testdata/webapp"), true, subIt.next());
        assertResultEquals(null, subTree2.resolve("WEB-INF/classes/org/alfresco/testdata/webapp/Another.class"), false, subIt.next());
        assertResultEquals(subTree1.resolve("WEB-INF/classes/org/alfresco/testdata/webapp/ExampleJavaClass.class"), subTree2.resolve("WEB-INF/classes/org/alfresco/testdata/webapp/ExampleJavaClass.class"), true, subIt.next());
        assertResultEquals(subTree1.resolve("WEB-INF/web.xml"), subTree2.resolve("WEB-INF/web.xml"), true, subIt.next());
        assertResultEquals(subTree1.resolve("index.jsp"), subTree2.resolve("index.jsp"), false, subIt.next());
        
        // Back up to the top-level comparisons
        assertResultEquals(tree1.resolve("b/blah.txt"), tree2.resolve("b/blah.txt"), true, rit.next());
        assertResultEquals(tree1.resolve("c"), tree2.resolve("c"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c1"), tree2.resolve("c/c1"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c1/commands.bat"), tree2.resolve("c/c1/commands.bat"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c1/commands.sh"), tree2.resolve("c/c1/commands.sh"), true, rit.next());
        assertResultEquals(tree1.resolve("c/c2"), tree2.resolve("c/c2"), true, rit.next());
        // Aardvark.java appears in both trees but is not the same!
        assertResultEquals(tree1.resolve("c/c2/Aardvark.java"), tree2.resolve("c/c2/Aardvark.java"), false, rit.next());
        assertResultEquals(tree1.resolve("c/c2/Banana.java"), null, false, rit.next());
        assertResultEquals(tree1.resolve("d"), null, false, rit.next());
        assertResultEquals(null, tree2.resolve("e"), false, rit.next());
    }

    private void assertResultNotPresent(Path p1, Path p2, boolean contentEqual, List<Result> results)
    {
        Result r  = new Result();
        r.p1 = p1;
        r.p2 = p2;
        r.equal = contentEqual;
        assertFalse("Result should not be present: "+r, results.contains(r));
    }
    
    private void assertResultEquals(Path p1, Path p2, boolean contentEqual, Result result)
    {
        Result expected = new Result();
        expected.p1 = p1;
        expected.p2 = p2;
        expected.equal = contentEqual;
        assertEquals(expected, result);
    }

    private Path pathFromClasspath(String path)
    {
        try
        {
            return Paths.get(getClass().getClassLoader().getResource(path).toURI());
        }
        catch (URISyntaxException error)
        {
            throw new RuntimeException("");
        }
    }
    
    private String toPlatformPath(String path)
    {
        return path.replace("/", File.separator);
    }
}
