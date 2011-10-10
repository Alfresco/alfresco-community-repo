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
package org.alfresco.repo.bulkimport.impl;

import java.io.File;
import java.io.FileFilter;

import org.alfresco.repo.bulkimport.AnalysedDirectory;
import org.alfresco.repo.bulkimport.DirectoryAnalyser;
import org.alfresco.repo.bulkimport.FilesystemTracker;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.util.PropertyCheck;
import org.apache.log4j.Logger;

/**
 * 
 * @since 4.0
 *
 */
public abstract class AbstractFilesystemTracker implements FilesystemTracker
{
	protected static Logger logger = Logger.getLogger(FilesystemTracker.class);

    protected DirectoryAnalyser directoryAnalyser = null;

    public final void setDirectoryAnalyser(DirectoryAnalyser directoryAnalyser)
    {
        this.directoryAnalyser = directoryAnalyser;
    }

	public void afterPropertiesSet() throws Exception
	{
        PropertyCheck.mandatory(this, "directoryAnalyser", directoryAnalyser);		
	}

    protected final AnalysedDirectory getImportableItemsInDirectory(ImportableItem directory)
    {
        AnalysedDirectory analysedDirectory = directoryAnalyser.analyseDirectory(directory, null);
        return analysedDirectory;
    }

    protected final AnalysedDirectory getImportableDirectoriesInDirectory(ImportableItem directory, final int count)
    {
    	FileFilter filter = null;

    	if(count != -1)
    	{
    		filter = new FileFilter()
    		{
				private int i = count;
	
				@Override
				public boolean accept(File file)
				{
					return file.isDirectory() && i-- > 0;
				}
			};
    	}
    	else
    	{
    		filter = new FileFilter()
    		{
				@Override
				public boolean accept(File file)
				{
					return file.isDirectory();
				}
			};
    	}

        AnalysedDirectory analysedDirectory = directoryAnalyser.analyseDirectory(directory, filter);
        return analysedDirectory;
    }
}
