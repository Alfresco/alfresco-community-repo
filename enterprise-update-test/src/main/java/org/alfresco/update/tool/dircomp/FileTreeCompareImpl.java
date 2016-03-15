/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.update.tool.dircomp.exception.FileTreeCompareException;
import org.apache.commons.io.FileUtils;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TVFS;
import de.schlichtherle.truezip.fs.archive.zip.ZipDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.AntPathMatcher;

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
    private final AntPathMatcher pathMatcher = new AntPathMatcher(File.separator);

    public FileTreeCompareImpl()
    {
        this(new HashSet<String>());
    }
    
    public FileTreeCompareImpl(Set<String> ignorePaths)
    {
        // This config MUST be present before any TFile objects etc. are created.
        TConfig config = TConfig.get();
        config.setArchiveDetector(new TArchiveDetector("war|jar", new ZipDriver(IOPoolLocator.SINGLETON)));
        this.ignorePaths.addAll(ignorePaths);
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
                    if (!contentMatches && isSpecialArchive(pathToFind))
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

    private boolean isSpecialArchive(Path pathToFind)
    {
        return pathToFind.getFileName().toString().toLowerCase().endsWith(".war") ||
                pathToFind.getFileName().toString().toLowerCase().endsWith(".jar");
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
        for (String pattern : ignorePaths)
        {
            if (pathMatcher.match(pattern, path))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @see #pathMatchesIgnorePattern(String)
     */
    private boolean pathMatchesIgnorePattern(Path path)
    {
        return pathMatchesIgnorePattern(path.toString());
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
