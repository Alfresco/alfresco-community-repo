
package org.alfresco.repo.bulkimport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the analysed contents of a directory.
 *
 * @since 4.0
 */
public class AnalysedDirectory
{
    private List<File> originalListing = null;
    private Map<File, ImportableItem> importableItems = null;
    private Map<File, ImportableItem> importableDirectories = null;

    public AnalysedDirectory(File[] files)
    {
        originalListing = Arrays.asList(files);
        // Sort the files/directories so that the *.metadata.properties.xml found later, see ALF-17965 for details.
        Collections.sort(originalListing);
        importableItems = new HashMap<File, ImportableItem>();
        importableDirectories = new HashMap<File, ImportableItem>();    	
    }
    
    public List<File> getOriginalListing()
	{
		return originalListing;
	}
    
	public Collection<ImportableItem> getImportableItems()
	{
		return importableItems.values();
	}

	public Collection<ImportableItem> getImportableDirectories()
	{
		return importableDirectories.values();
	}

	public void addImportableItem(ImportableItem importableItem)
    {
        if(importableItem.getHeadRevision().contentFileExists() &&
        		ImportableItem.FileType.DIRECTORY.equals(importableItem.getHeadRevision().getContentFileType()))
        {
        	importableDirectories.put(importableItem.getHeadRevision().getContentFile(), importableItem);
        }
        else
        {
    		importableItems.put(importableItem.getHeadRevision().getContentFile(), importableItem);        	
        }
    }

    public ImportableItem findImportableItem(File contentFile)
    {
    	ImportableItem result = null;
    	result = importableItems.get(contentFile);
    	if(result == null)
    	{
    		result = importableDirectories.get(contentFile);
    	}
        return result;
    }
}
