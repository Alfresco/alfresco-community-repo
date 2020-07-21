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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
        DirectoryStream.Filter<Path> filter = null;

    	if (count != -1)
    	{
            filter = new DirectoryStream.Filter<Path>()
            {
                private int i = count;

                @Override
                public boolean accept(Path entry) throws IOException
                {
                    return Files.isDirectory(entry) && i-- > 0;
                }
            };
    	}
    	else
    	{
            filter = new DirectoryStream.Filter<Path>()
            {
                @Override
                public boolean accept(Path entry) throws IOException
                {
                    return Files.isDirectory(entry);
                }
            };
    	}

        AnalysedDirectory analysedDirectory = directoryAnalyser.analyseDirectory(directory, filter);
        return analysedDirectory;
    }
}
