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
