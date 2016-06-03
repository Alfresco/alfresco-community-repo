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
