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
