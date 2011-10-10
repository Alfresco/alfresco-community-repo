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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.bulkimport.AnalysedDirectory;
import org.alfresco.repo.bulkimport.DirectoryAnalyser;
import org.alfresco.repo.bulkimport.ImportFilter;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.repo.bulkimport.ImportableItem.FileType;
import org.alfresco.repo.bulkimport.MetadataLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides the implementation for directory analysis, the process by
 * which a directory listing of files is broken up into ImportableItems.
 * 
 * @since 4.0
 * 
 */
public class DirectoryAnalyserImpl implements DirectoryAnalyser
{
    private final static Log log = LogFactory.getLog(DirectoryAnalyserImpl.class);
    
    private final static Pattern VERSION_SUFFIX_PATTERN = Pattern.compile(".+" + VERSION_SUFFIX_REGEX);

    private MetadataLoader metadataLoader;
    private BulkImportStatusImpl importStatus;
    private List<ImportFilter> importFilters;
    
    public DirectoryAnalyserImpl(MetadataLoader metadataLoader, BulkImportStatusImpl importStatus, List<ImportFilter> importFilters)
    {
        this.metadataLoader = metadataLoader;
        this.importStatus = importStatus;
        this.importFilters = importFilters;
    }
    
    public DirectoryAnalyserImpl()
    {
    }
    
	public void setMetadataLoader(MetadataLoader metadataLoader)
	{
		this.metadataLoader = metadataLoader;
	}
	
	public void setImportStatus(BulkImportStatusImpl status)
	{
		importStatus = status;
	}

	public final void setImportFilters(List<ImportFilter> importFilters)
    {
        if(importFilters != null)
        {
        	this.importFilters = importFilters;
        }
        else
        {
        	this.importFilters = new ArrayList<ImportFilter>();
        }
    }

    protected boolean shouldFilter(ImportableItem importableItem)
    {
        boolean filterImportableItem = false;

        if(importFilters != null && importFilters.size() > 0)
        {
            for (ImportFilter filter : importFilters)
            {
                if (filter.shouldFilter(importableItem))
                {
                    filterImportableItem = true;
                    break;
                }
            }
        }

        return filterImportableItem;
    }

    /**
     * @see org.alfresco.extension.bulkfilesystemimport.DirectoryAnalyser#analyseDirectory(java.io.File)
     */
    public AnalysedDirectory analyseDirectory(ImportableItem directory, FileFilter filter)
    {
    	File directoryFile = directory.getHeadRevision().getContentFile();
    	AnalysedDirectory result = null;

    	if(filter == null)
    	{
    		result = new AnalysedDirectory(directoryFile.listFiles());
    	}
    	else
    	{
    		result = new AnalysedDirectory(directoryFile.listFiles(filter));
    	}
        
        if (log.isDebugEnabled())
        {
        	log.debug("Analysing directory " + FileUtils.getFileName(directoryFile) + "...");
        }

        // Build up the list of ImportableItems from the directory listing
        for (File file : result.getOriginalListing())
        {
            if (log.isTraceEnabled())
            {
            	log.trace("Scanning file " + FileUtils.getFileName(file) + "...");
            }
            
            if (file.canRead())
            {
                if (isVersionFile(file))
                {
                    addVersionFile(directory, result, file);
                    importStatus.incrementNumberOfFilesScanned();
                }
                else if (isMetadataFile(file))
                {
                    addMetadataFile(directory, result, file);
                    importStatus.incrementNumberOfFilesScanned();
                }
                else
                {
                    boolean isDirectory = addParentFile(directory, result, file);
                    
                    if (isDirectory)
                    {
                        importStatus.incrementNumberOfFoldersScanned();
                    }
                    else
                    {
                        importStatus.incrementNumberOfFilesScanned();
                    }
                }
            }
            else
            {
                if (log.isWarnEnabled())
                {
                	log.warn("Skipping unreadable file '" + FileUtils.getFileName(file) + "'.");
                }

                importStatus.incrementNumberOfUnreadableEntries();
            }
        }

        // Finally, remove any items from the list that aren't valid (don't have either a
        // contentFile or a metadataFile)
        Iterator<ImportableItem> iter = result.getImportableItems().iterator();

        while (iter.hasNext())
        {
            ImportableItem importableItem = iter.next();

            if (!importableItem.isValid())
            {
                iter.remove();
            }
        }
        
        iter = result.getImportableDirectories().iterator();
        while (iter.hasNext())
        {
            ImportableItem importableItem = iter.next();

            if (!importableItem.isValid())
            {
                iter.remove();
            }
        }

        if (log.isDebugEnabled())
        {
        	log.debug("Finished analysing directory " + FileUtils.getFileName(directoryFile) + ".");
        }

        return result;
    }

    private boolean isVersionFile(File file)
    {
        Matcher matcher = VERSION_SUFFIX_PATTERN.matcher(file.getName());

        return matcher.matches();
    }


    private boolean isMetadataFile(File file)
    {
        boolean result = false;
        
        if (metadataLoader != null)
        {
            result = file.getName().endsWith(MetadataLoader.METADATA_SUFFIX + metadataLoader.getMetadataFileExtension());
        }
        
        return(result);
    }

    private void addVersionFile(ImportableItem parent, AnalysedDirectory analysedDirectory, File versionFile)
    {
        File parentContentFile = getParentOfVersionFile(versionFile);
        boolean isContentVersion  = false;

        if (isMetadataFile(parentContentFile))
        {
            parentContentFile = getParentOfMetadatafile(parentContentFile);
            isContentVersion  = false;
        }
        else
        {
            isContentVersion = true;
        }

        ImportableItem importableItem = findOrCreateImportableItem(parent, analysedDirectory, parentContentFile);
        int version = getVersionNumber(versionFile);
        ImportableItem.VersionedContentAndMetadata versionEntry = findOrCreateVersionEntry(importableItem, version);

        if (isContentVersion)
        {
            versionEntry.setContentFile(versionFile);
        }
        else
        {
            versionEntry.setMetadataFile(versionFile);
        }
    }


    private void addMetadataFile(ImportableItem parent, AnalysedDirectory analysedDirectory, File metadataFile)
    {
        File parentContentfile = getParentOfMetadatafile(metadataFile);

        ImportableItem importableItem = findOrCreateImportableItem(parent, analysedDirectory, parentContentfile);

        importableItem.getHeadRevision().setMetadataFile(metadataFile);
    }


    private boolean addParentFile(ImportableItem parent, AnalysedDirectory analysedDirectory, File contentFile)
    {
        ImportableItem importableItem = findOrCreateImportableItem(parent, analysedDirectory, contentFile);

        importableItem.getHeadRevision().setContentFile(contentFile);
        
        return(importableItem.getHeadRevision().getContentFileType() == FileType.DIRECTORY);
    }

    private ImportableItem findOrCreateImportableItem(ImportableItem parent, AnalysedDirectory analysedDirectory, File contentFile)
    {
        ImportableItem result = findImportableItem(analysedDirectory, contentFile);

        // We didn't find it, so create it
        if (result == null)
        {
            result = new ImportableItem();
            result.setParent(parent);
            result.getHeadRevision().setContentFile(contentFile);
            if(!shouldFilter(result))
            {
            	analysedDirectory.addImportableItem(result);
            }
        }

        return(result);
    }


    private ImportableItem findImportableItem(AnalysedDirectory analysedDirectory, File contentFile)
    {
        ImportableItem result = null;

        if (contentFile == null)
        {
            throw new IllegalStateException("Cannot call findOrCreateImportableItem with null key");
        }

        result = analysedDirectory.findImportableItem(contentFile);

        return(result);
    }


    private ImportableItem.VersionedContentAndMetadata findOrCreateVersionEntry(ImportableItem importableItem, int version)
    {
        ImportableItem.VersionedContentAndMetadata result = findVersionEntry(importableItem, version);

        if (result == null)
        {
            result = importableItem.new VersionedContentAndMetadata(version);
            
            importableItem.addVersionEntry(result);
        }

        return (result);
    }


    private ImportableItem.VersionedContentAndMetadata findVersionEntry(ImportableItem importableItem, int version)
    {
        ImportableItem.VersionedContentAndMetadata result = null;

        if (importableItem.hasVersionEntries())
        {
            for (ImportableItem.VersionedContentAndMetadata versionEntry : importableItem.getVersionEntries())
            {
                if (version == versionEntry.getVersion())
                {
                    result = versionEntry;
                    break;
                }
            }
        }

        return(result);
    }


    private int getVersionNumber(File versionFile)
    {
        int result = -1;

        if (!isVersionFile(versionFile))
        {
            throw new IllegalStateException(FileUtils.getFileName(versionFile) + " is not a version file.");
        }

        Matcher matcher = VERSION_SUFFIX_PATTERN.matcher(versionFile.getName());
        String versionStr = null;

        if (matcher.matches())
        {
            versionStr = matcher.group(1);
        }
        else
        {
            throw new IllegalStateException(""); // ####TODO!!!!
        }

        result = Integer.parseInt(versionStr);

        return(result);
    }


    private File getParentOfVersionFile(File versionFile)
    {
        File result = null;

        if (!isVersionFile(versionFile))
        {
            throw new IllegalStateException(FileUtils.getFileName(versionFile) + " is not a version file.");
        }

        String parentFilename = versionFile.getName().replaceFirst(VERSION_SUFFIX_REGEX, "");

        result = new File(versionFile.getParent(), parentFilename);
        
        return(result);
    }


    private File getParentOfMetadatafile(File metadataFile)
    {
        File result = null;

        if (!isMetadataFile(metadataFile))
        {
            throw new IllegalStateException(FileUtils.getFileName(metadataFile) + " is not a metadata file.");
        }

        String name = metadataFile.getName();
        String contentName = name.substring(0, name.length() - (MetadataLoader.METADATA_SUFFIX + metadataLoader.getMetadataFileExtension()).length());

        result = new File(metadataFile.getParent(), contentName);

        return(result);
    }

}
