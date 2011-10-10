/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

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
