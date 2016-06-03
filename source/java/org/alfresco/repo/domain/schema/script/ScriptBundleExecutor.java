package org.alfresco.repo.domain.schema.script;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Executes a set of zero or more SQL scripts.
 * 
 * @author Matt Ward
 */
public interface ScriptBundleExecutor
{
    /**
     * Runs a bundle of scripts. If any script within the bundle fails, then the rest of the files are not run.
     * 
     * @param logOnly   <tt>true</tt> to catch and log any exceptions or <tt>false</tt> to rethrow 
     * @param dir        Directory where the script bundle may be found.
     * @param scripts    Names of the SQL scripts to run, relative to the specified directory.
     * @throws AlfrescoRuntimeException if a script fails and the <tt>logOnly</tt> flag is <tt>false</tt>
     */
    void exec(boolean logOnly, String dir, String... scripts);
    
    /**
     * Runs a bundle of scripts. If any script within the bundle fails, then the rest of the files are not run.
     *  
     * @param dir        Directory where the script bundle may be found.
     * @param scripts    Names of the SQL scripts to run, relative to the specified directory.
     */
    void exec(String dir, String... scripts);
    
    /**
     * Runs a bundle of scripts. If any script within the bundle fails, then the rest of the files
     * are not run, with the exception of postScript - which is always run (a clean-up script for example).
     *  
     * @param dir        Directory where the script bundle may be found.
     * @param postScript A script that is always run after the other scripts.
     * @param scripts    Names of the SQL scripts to run, relative to the specified directory.
     */
    void execWithPostScript(String dir, String postScript, String... scripts);
}
