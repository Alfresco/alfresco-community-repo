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

import java.nio.file.Path;
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
    private List<Path> originalPaths = null;
    private Map<Path, ImportableItem> importableItems = null;
    private Map<Path, ImportableItem> importableDirectories = null;

    public AnalysedDirectory(List<Path> paths)
    {
        originalPaths = paths;
        // Sort the files/directories so that the *.metadata.properties.xml found later, see ALF-17965 for details.
        Collections.sort(originalPaths);
        importableItems = new HashMap<Path, ImportableItem>();
        importableDirectories = new HashMap<Path, ImportableItem>();            
    }
    
    public List<Path> getOriginalPaths()
    {
        return originalPaths;
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

    public ImportableItem findImportableItem(Path contentFile)
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
