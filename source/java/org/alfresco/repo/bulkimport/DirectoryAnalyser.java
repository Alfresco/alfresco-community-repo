
package org.alfresco.repo.bulkimport;

import java.io.FileFilter;


/**
 * This interface defines a directory analyser. This is the process by which
 * the contents of a source directory are grouped together into a list of
 * <code>ImportableItem</code>s. 
 * 
 * Please note that this interface is not intended to have more than one implementation
 * (<code>DirectoryAnalyserImpl</code>) - it exists solely for dependency injection purposes.
 *
 * @since 4.0
 */
public interface DirectoryAnalyser
{
    /**
     * Regex string for the version filename suffix
     */
    public final static String VERSION_SUFFIX_REGEX = "\\.v([0-9]+)\\z";
    
    /**
     * Analyses the given directory.
     * 
     * @param directory The directory to analyse (note: <u>must</u> be a directory) <i>(must not be null)</i>.
     * @return An <code>AnalysedDirectory</code> object <i>(will not be null)</i>.
     */
    public AnalysedDirectory analyseDirectory(ImportableItem directory, FileFilter filter);
    
}
