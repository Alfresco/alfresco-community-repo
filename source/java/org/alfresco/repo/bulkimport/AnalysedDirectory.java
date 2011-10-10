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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    // All importable items (files and directories)
//    private List<ImportableItem> importableItems = null;
    private Map<File, ImportableItem> importableItems = null;
    // keep track of directories in this list as well as importedItems
    private List<ImportableItem> importableDirectories = null;

    public AnalysedDirectory(File[] files)
    {
        originalListing = Arrays.asList(files);
//        importableItems = new ArrayList<ImportableItem>();
        importableItems = new HashMap<File, ImportableItem>();
        importableDirectories = new ArrayList<ImportableItem>();    	
    }
    
    public List<File> getOriginalListing()
	{
		return originalListing;
	}

//	public List<ImportableItem> getImportableItems()
//	{
//		return importableItems;
//	}
    
	public Collection<ImportableItem> getImportableItems()
	{
		return importableItems.values();
	}

	public List<ImportableItem> getImportableDirectories()
	{
		return importableDirectories;
	}

	public void addImportableItem(ImportableItem importableItem)
    {
//    	importableItems.add(importableItem);
        if(importableItem.getHeadRevision().contentFileExists() &&
        		ImportableItem.FileType.DIRECTORY.equals(importableItem.getHeadRevision().getContentFileType()))
        {
        	importableDirectories.add(importableItem);
        }
        else
        {
    		importableItems.put(importableItem.getHeadRevision().getContentFile(), importableItem);        	
        }
    }

    public ImportableItem findImportableItem(File contentFile)
    {
    	ImportableItem result = null;

    	// TODO perhaps a map structure here to make lookups faster?
//        for (ImportableItem importableItem : importableItems)
//        {
//            if (contentFile.equals(importableItem.getHeadRevision().getContentFile()))
//            {
//                result = importableItem;
//                break;
//            }
//        }
    	result = importableItems.get(contentFile);

        return result;
    }
}
