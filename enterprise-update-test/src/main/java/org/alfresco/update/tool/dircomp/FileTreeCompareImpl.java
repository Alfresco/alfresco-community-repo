/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import org.alfresco.update.tool.dircomp.exception.FileTreeCompareException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.AntPathMatcher;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class capable of comparing two trees of files to determine which directories or
 * files appear in one tree and not the other, or whether a file that appears in
 * both has differences in its content.
 * 
 * @author Matt Ward
 */
public class FileTreeCompareImpl implements FileTreeCompare
{
    private static final Logger log = LogManager.getLogger(FileTreeCompareImpl.class);
    private final Set<String> ignorePaths = new HashSet<>();
    private final Set<String> allowedDiffsPaths = new HashSet<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher(File.separator);

    public FileTreeCompareImpl()
    {
        this(null, null);
    }
    
    public FileTreeCompareImpl(Set<String> ignorePaths, Set<String> allowedDiffsPaths)
    {
        // This config MUST be present before any TFile objects etc. are created.
        TConfig config = TConfig.get();
        config.setArchiveDetector(new TArchiveDetector("war|jar|amp", new ZipDriver(IOPoolLocator.SINGLETON)));
        if (ignorePaths == null)
        {
            // Add default ignores
            ignorePaths = new HashSet<>();
            ignorePaths.add(toPlatformPath("alf_data/postgresql/**"));
            ignorePaths.add(toPlatformPath("alf_data/oouser/user/**"));
            ignorePaths.add(toPlatformPath("alf_data/solr/*.war"));
            ignorePaths.add(toPlatformPath("common/**"));
            ignorePaths.add(toPlatformPath("META-INF/MANIFEST.MF"));
            ignorePaths.add(toPlatformPath("META-INF/maven/**"));
            ignorePaths.add(toPlatformPath("licenses/notice.txt"));
            ignorePaths.add(toPlatformPath("uninstall.app/**"));
            ignorePaths.add(toPlatformPath("uninstall/**"));
            ignorePaths.add(toPlatformPath("uninstall.exe"));
            ignorePaths.add(toPlatformPath("uninstall.dat"));
            ignorePaths.add(toPlatformPath("libreoffice.app/**"));
            ignorePaths.add(toPlatformPath("libreoffice/**"));
            ignorePaths.add(toPlatformPath("java/**"));
            ignorePaths.add(toPlatformPath("applied-updates/**"));
            ignorePaths.add(toPlatformPath("~build/**"));
            ignorePaths.add(toPlatformPath("properties.ini"));
            ignorePaths.add(toPlatformPath("**/log.txt"));
            ignorePaths.add(toPlatformPath("**/solrcore.properties"));
            ignorePaths.add(toPlatformPath("**/modifications.install"));
            ignorePaths.add(toPlatformPath("tomcat/webapps/ROOT.war"));
            
            // Ignore for 5.1 MNT-14307
            ignorePaths.add(toPlatformPath("tomcat/shared/classes/alfresco/web-extension/share-config-custom.xml"));
            
           
        }
        if (allowedDiffsPaths == null)
        {
            // Add default paths where certain differences are allowed, e.g. absolute path references.
            allowedDiffsPaths = new HashSet<>();
            allowedDiffsPaths.add(toPlatformPath("common/bin/**"));
            allowedDiffsPaths.add(toPlatformPath("common/include/**/*.h"));
            allowedDiffsPaths.add(toPlatformPath("common/lib/**/*.pc"));
            allowedDiffsPaths.add(toPlatformPath("common/lib/**/*.la"));
            allowedDiffsPaths.add(toPlatformPath("libreoffice.app/Contents/Resources/bootstraprc"));
            allowedDiffsPaths.add(toPlatformPath("postgresql/bin/**"));
            allowedDiffsPaths.add(toPlatformPath("**/*.sh"));
            allowedDiffsPaths.add(toPlatformPath("**/*.bat"));
            allowedDiffsPaths.add(toPlatformPath("**/*.ini"));
            allowedDiffsPaths.add(toPlatformPath("**/*.properties"));
            allowedDiffsPaths.add(toPlatformPath("**/*.xml"));
            allowedDiffsPaths.add(toPlatformPath("**/*.sample"));
            allowedDiffsPaths.add(toPlatformPath("**/*.txt"));
            allowedDiffsPaths.add(toPlatformPath("tomcat/conf/Catalina/localhost/solr4.xml"));
            allowedDiffsPaths.add(toPlatformPath("tomcat/conf/Catalina/localhost/solr.xml"));
        }
        this.ignorePaths.addAll(ignorePaths);
        this.allowedDiffsPaths.addAll(allowedDiffsPaths);
    }

    private String toPlatformPath(String path)
    {
        return path.replace("/", File.separator);
    }

    @Override
    public ResultSet compare(Path p1, Path p2)
    {
        ResultSet resultSet = new ResultSet();
        try
        {
            compare(resultSet.stats, resultSet.results, p1, p2);
        }
        catch (Exception e)
        {
            throw new FileTreeCompareException("Unable to compare file trees.", e);
        }
        return resultSet;
    }

    private void compare(ResultSet.Stats stats, List<Result> results, Path tree1, Path tree2) throws IOException
    {
        SortedPathSet set1 = sortedPaths(tree1);
        SortedPathSet set2 = sortedPaths(tree2);
        
        SortedPathSet all = new SortedPathSet();
        all.addAll(set1);
        all.addAll(set2);
        
        for (Path pathToFind : all)
        {
            if (pathMatchesIgnorePattern(pathToFind))
            {
                // Skip paths that we don't want to examine, e.g. tomcat/temp
                log.debug("Skipping path: "+pathToFind);
                stats.ignoredFileCount++;
                continue;
            }

            Result result = new Result();
            results.add(result);
            stats.resultCount++;

            if (set1.contains(pathToFind) && set2.contains(pathToFind))
            {
                log.debug("In both: "+pathToFind);
                // Set the results, translating paths back to absolute as required.
                result.p1 = tree1.resolve(pathToFind);
                result.p2 = tree2.resolve(pathToFind);
                boolean contentMatches = false;
                if (Files.isRegularFile(result.p1) && Files.isRegularFile(result.p2))
                {
                    contentMatches = FileUtils.contentEquals(result.p1.toFile(), result.p2.toFile());
                    if (!contentMatches)
                    {
                        if (pathMatchesAllowedDiffsPattern(pathToFind))
                        {
                            File f1 = preprocessFile(tree1, result.p1.toFile());
                            File f2 = preprocessFile(tree2, result.p2.toFile());
                            contentMatches = FileUtils.contentEquals(f1, f2);
                            // Delete the files now that we no longer need them. The originals are still available.
                            f1.delete();
                            f2.delete();
                            if (contentMatches)
                            {
                                // If the preprocessed files match, then although the files didn't
                                // match when first compared byte-for-byte, they do match as far as we are concerned.
                                // But add to the stats that this is what has happened.
                                stats.suppressedDifferenceCount++;
                            }
                        }
                        else if (isSpecialArchive(pathToFind))
                        {
                            Path archive1 = extract(result.p1);
                            Path archive2 = extract(result.p2);
                            result.subTree1 = archive1;
                            result.subTree2 = archive2;
                            final int diffBefore = stats.differenceCount;
                            compare(stats, result.subResults, archive1, archive2);
                            final int diffAfter = stats.differenceCount;
                            if (diffAfter == diffBefore)
                            {
                                // No significant differences were found in the (recursive) subtree comparison.
                                // We can therefore mark the special archive files matching in both trees.
                                contentMatches = true;
                            }
                        }
                    }

                }
                else if (Files.isDirectory(result.p1) && Files.isDirectory(result.p2))
                {
                    // Two directories are counted as the same.
                    contentMatches = true;
                }
                result.equal = contentMatches;
            }
            else if (set1.contains(pathToFind))
            {
                log.debug("In tree1 only: "+pathToFind);
                result.p1 = tree1.resolve(pathToFind);
                result.p2 = null;
            }
            else if (set2.contains(pathToFind))
            {
                log.debug("In tree2 only: "+pathToFind);
                result.p1 = null;
                result.p2 = tree2.resolve(pathToFind);
            }
            else
            {
                throw new IllegalStateException(
                            "Something went wrong. The path is not found in either tree: "+pathToFind);
            }

            if (!result.equal)
            {
                stats.differenceCount++;
            }
        }
    }

    private File preprocessFile(Path tree, File orig) throws IOException
    {
        // Create a set of replacements that we intend to make. Replacing them with
        // a known token allows us to remove differences (that we're not interested in) in the files.
        Map<String, String> replacements = new HashMap<>();
        replacements.put(tree.toRealPath().toString(), replacementToken("comparison_root"));
        
        // Create a pattern for module.installDate
        Pattern installDatePattern = Pattern.compile("module.installDate=.*[\n\r\f]*$");
        Pattern commentPattern = Pattern.compile("^#.*");

        File processed = Files.createTempFile(orig.getName(), ".tmp").toFile();
        try(Reader r = new FileReader(orig);
            BufferedReader br = new BufferedReader(r);
            Writer w = new FileWriter(processed);
            PrintWriter pw = new PrintWriter(w))
        {
            String line;
           
            while ((line = br.readLine()) != null)
            {   
                for (String replKey : replacements.keySet())
                {
                    String replVal = replacements.get(replKey);
                    line = line.replace(replKey, replVal);
                }
                Matcher m = installDatePattern.matcher(line);
                if(m.matches())
                {
                    // replace module.installDate
                    line = m.replaceFirst("module.installDate=<install-date>");
                }
                Matcher cp = commentPattern.matcher(line);
                if(cp.matches())
                {
                    // replace module.installDate
                    line = "# {comment suppressed}\n";
                }
                
                pw.println(line);
            }
        }
        return processed;
    }

    private String replacementToken(String label)
    {
        return String.format("@$@$@$@${{TREE_COMPARE_%s}}", label);
    }

    private boolean isSpecialArchive(Path pathToFind)
    {
        return pathToFind.getFileName().toString().toLowerCase().endsWith(".war") ||
                pathToFind.getFileName().toString().toLowerCase().endsWith(".jar") ||
                pathToFind.getFileName().toString().toLowerCase().endsWith(".amp");
    }

    /**
     * If the set of paths to allow <em>certain</em> differences ({@link #allowedDiffsPaths})
     * contains a pattern matching the specified path, then true is returned.
     * <p>
     * Patterns are ant-style patterns.
     *
     * @param path   The path to check
     * @return       True if there is a pattern in the allowedDiffsPaths set matching the path.
     */
    private boolean pathMatchesAllowedDiffsPattern(String path)
    {
        return pathMatchesPattern(path, allowedDiffsPaths);
    }

    /**
     * @see #pathMatchesAllowedDiffsPattern(String)
     */
    private boolean pathMatchesAllowedDiffsPattern(Path path)
    {
        return pathMatchesAllowedDiffsPattern(path.toString());
    }

    /**
     * If the set of paths to ignore ({@link #ignorePaths}) contains
     * a pattern matching the specified path, then true is returned.
     * <p>
     * Patterns are ant-style patterns.
     *
     * @param path   The path to check
     * @return       True if there is a path in the ignorePaths set that is a prefix of the path.
     */
    private boolean pathMatchesIgnorePattern(String path)
    {
        return pathMatchesPattern(path, ignorePaths);
    }

    /**
     * @see #pathMatchesIgnorePattern(String)
     */
    private boolean pathMatchesIgnorePattern(Path path)
    {
        return pathMatchesIgnorePattern(path.toString());
    }

    private boolean pathMatchesPattern(String path, Set<String> patterns)
    {
        for (String pattern : patterns)
        {
            if (pathMatcher.match(pattern, path))
            {
                return true;
            }
        }
        return false;
    }

    private Path extract(Path archivePath) throws IOException
    {
        String destDirName = archivePath.getFileName().toString();
        Path dest = Files.createTempDirectory(destDirName);
        extract(archivePath, dest);
        return dest;
    }
    
    private void extract(Path archivePath, Path destPath) throws IOException
    {
        TFile archive = new TFile(archivePath.toFile());
        TFile dest = new TFile(destPath.toFile(), TArchiveDetector.NULL);
        try
        {
            // Unzip the archive.
            archive.cp_rp(dest);
        }
        finally
        {
            TVFS.umount(archive);
            TVFS.umount(dest);
        }
    }

    /**
     * Traverse path and create a {@link SortedPathSet} containing the set
     * of paths encountered. The {@link Path} instances are relative to
     * the base path provided as a parameter to this method.
     * 
     * @param path      The path to traverse.
     * @return          SortedPathSet
     * @throws IOException
     */
    protected SortedPathSet sortedPaths(Path path) throws IOException
    {
        SortedPathSet sortedPaths = new SortedPathSet();
        collectPaths(sortedPaths, path, path.toFile());
        return sortedPaths;
    }
    
    private void collectPaths(SortedPathSet sortedSet, Path root, File path) throws IOException
    {        
        for (File f : path.listFiles())
        {
            Path relativePath = root.relativize(f.toPath());
            sortedSet.add(relativePath);
            if (f.isDirectory())
            {
                collectPaths(sortedSet, root, f);
            }
        }
    }
}
