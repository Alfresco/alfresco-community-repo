/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
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
