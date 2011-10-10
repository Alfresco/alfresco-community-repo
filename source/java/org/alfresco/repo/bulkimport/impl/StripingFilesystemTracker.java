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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.bulkimport.AnalysedDirectory;
import org.alfresco.repo.bulkimport.DirectoryAnalyser;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A filesystem walker that returns all files and directories in subsequent levels of a filesystem tree; it returns all directories
 * and files in a given level, at which point it moves on to the next level and starts returning directories and files in that level.
 * 
 * @since 4.0
 *
 */
public class StripingFilesystemTracker extends AbstractFilesystemTracker
{
	private ImportableItem rootFolder;
	private int currentLevel = 0;
	private int batchSize;

	// TODO choose most appropriate list type
	private Map<Integer, List<ImportableItem>> directoriesToProcess = new HashMap<Integer, List<ImportableItem>>(10);
	private List<ImportableItem> toProcess = new ArrayList<ImportableItem>();

	public StripingFilesystemTracker(DirectoryAnalyser directoryAnalyser, NodeRef target, File sourceFolder, int batchSize)
	{
		this.directoryAnalyser = directoryAnalyser;
		this.batchSize = batchSize;

		// not really an importable item but the interface requires it to be in this form
		rootFolder = new ImportableItem();
		rootFolder.getHeadRevision().setContentFile(sourceFolder);
		rootFolder.setNodeRef(target);

		addDirectoryToProcess(rootFolder, currentLevel);
	}
	
	protected void addDirectoriesToProcess(Collection<ImportableItem> dirsToAdd, int level)
	{
		List<ImportableItem> dirs = getDirectoriesToProcess(level);
		dirs.addAll(dirsToAdd);
	}

	protected void addDirectoryToProcess(ImportableItem dir, int level)
	{
		List<ImportableItem> dirs = getDirectoriesToProcess(level);
		dirs.add(dir);
	}

	protected List<ImportableItem> getDirectoriesToProcess(int level)
	{
		List<ImportableItem> dirs = directoriesToProcess.get(new Integer(level));
		if(dirs == null)
		{
			dirs = new ArrayList<ImportableItem>();
			directoriesToProcess.put(new Integer(level), dirs);
		}

		return dirs;
	}

	public int count()
	{
		// Note: this is an estimate of the number of directories and files in the current level
		// TODO guess - multiplier of number of directories to process in the current directory
		return numDirectoriesToProcess() * 100;
	}
	
	protected void incrementLevel()
	{
		currentLevel++;
	}
	
	public void itemImported(NodeRef nodeRef, ImportableItem importableItem)
	{
		// nothing to do
	}
	
	protected void addItemsToProcess(Collection<ImportableItem> items)
	{
		toProcess.addAll(items);
	}

	protected ImportableItem getDirectoryToProcess()
	{
		List<ImportableItem> dirs = getDirectoriesToProcess(currentLevel);
		if(dirs.size() > 0)
		{
			return dirs.remove(0);
		}
		else
		{
			return null;
		}
	}
	
	public boolean moreLevels()
	{
		return getDirectoriesToProcess(currentLevel).size() > 0;
	}
	
	public int numDirectoriesToProcess()
	{
		return getDirectoriesToProcess(currentLevel).size();
	}

	protected List<ImportableItem> getImportableItems(int count)
	{
		while(toProcess.size() < count)
		{
			ImportableItem directory = getDirectoryToProcess();
			if(directory != null)
			{
				AnalysedDirectory analysedDirectory = getImportableItemsInDirectory(directory);
				addItemsToProcess(analysedDirectory.getImportableDirectories());
				addItemsToProcess(analysedDirectory.getImportableItems());

				// add new directories to process in next level
				getDirectoriesToProcess(currentLevel+1).addAll(analysedDirectory.getImportableDirectories());
			}
			else
			{
				break;
			}
		}

		int size = (toProcess.size() >= count ? count : toProcess.size());
		List<ImportableItem> result = new ArrayList<ImportableItem>(size);
		int i = size;
		while(i > 0)
		{
			// we can assume that there are items in toProcess to remove because the size has been pre-calculated above
			ImportableItem importableItem = toProcess.remove(0);
			if(importableItem != null)
			{
				result.add(importableItem);
				i--;
			}
			else
			{
				logger.warn("Unexpected empty toProcess queue");
			}
		}

		if(result.size() == 0)
		{
			// this level has been exhausted, increment level
			incrementLevel();
		}
		
		return result;
	}
	
	@Override
	public BatchProcessWorkProvider<ImportableItem> getWorkProvider()
	{
    	BatchProcessWorkProvider<ImportableItem> provider = new BatchProcessWorkProvider<ImportableItem>()
		{
			@Override
			public int getTotalEstimatedWorkSize()
			{
				return count();
			}

			@Override
			public Collection<ImportableItem> getNextWork()
			{
				// TODO perhaps some multiple of the batchSize to limit calls
				// to getNextWork? Add this to repository.properties.
				return getImportableItems(batchSize*1000);
			}
		};
		
		return provider;
	}
}