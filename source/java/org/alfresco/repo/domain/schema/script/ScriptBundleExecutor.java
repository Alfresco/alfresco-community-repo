/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.domain.schema.script;

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
