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

package org.alfresco.repo.bulkimport;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;


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
    public AnalysedDirectory analyseDirectory(ImportableItem directory, DirectoryStream.Filter<Path> filter);
    
}
