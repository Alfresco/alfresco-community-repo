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
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.bulkimport.AnalysedDirectory;
import org.alfresco.repo.bulkimport.DirectoryAnalyser;
import org.alfresco.repo.bulkimport.ImportFilter;
import org.alfresco.repo.bulkimport.ImportableItem;
import org.alfresco.repo.bulkimport.ImportableItem.FileType;
import org.alfresco.repo.bulkimport.MetadataLoader;
import org.alfresco.repo.dictionary.constraint.NameChecker;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;

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
    private NameChecker nameChecker;
    private DictionaryService dictionaryService;

    
    public DirectoryAnalyserImpl(MetadataLoader metadataLoader, BulkImportStatusImpl importStatus, List<ImportFilter> importFilters,
            NameChecker nameChecker)
    {
        this.metadataLoader = metadataLoader;
        this.importStatus = importStatus;
        this.importFilters = importFilters;
        this.nameChecker = nameChecker;
    }
    
    public DirectoryAnalyserImpl()
    {
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setNameChecker(NameChecker nameChecker)
    {
        this.nameChecker = nameChecker;
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
     * @see org.alfresco.repo.bulkimport.DirectoryAnalyser#analyseDirectory(org.alfresco.repo.bulkimport.ImportableItem, java.nio.file.DirectoryStream.Filter)
     */
    public AnalysedDirectory analyseDirectory(ImportableItem directory, DirectoryStream.Filter<Path> filter)
    {
    	Path directoryFile = directory.getHeadRevision().getContentFile();
    	AnalysedDirectory result = new AnalysedDirectory(listFiles(directoryFile, filter));
        
        if (log.isDebugEnabled())
        {
        	log.debug("Analysing directory " + FileUtils.getFileName(directoryFile) + "...");
        }

        // Build up the list of ImportableItems from the directory listing
        for (Path file : result.getOriginalPaths())
        {
            // MNT-9763 bulkimport fails when there is a very large LastModified timestamp.
            String isoDate = null;
            try
            {
                isoDate = ISO8601DateFormat.format(new Date(Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS).toMillis()));
                ISO8601DateFormat.parse(isoDate);
            }
            catch (PlatformRuntimeException | IOException e)
            {
                log.warn("Failed to convert date " + isoDate + " to string for " + file.getFileName(), e);
                importStatus.incrementNumberOfUnreadableEntries();
                continue;
            }
            
            if (log.isTraceEnabled())
            {
            	log.trace("Scanning file " + FileUtils.getFileName(file) + "...");
            }
            
            if (Files.isReadable(file))
            {
                try
                {
                    nameChecker.evaluate(file.getFileName().toString());
                }
                catch (ConstraintException e)
                {
                    if (log.isWarnEnabled())
                    {
                        log.warn("Skipping file with invalid name: '" + FileUtils.getFileName(file) + "'.");
                    }
                    // mark file with invalid name as unreadable
                    importStatus.incrementNumberOfUnreadableEntries();
                    
                    continue;
                }
                
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

            if (!importableItem.isValid() || !isMetadataValid(importableItem))
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
    
    private List<Path> listFiles(Path sourceDirectory, DirectoryStream.Filter<Path> filter)
    {
        List<Path> files = new ArrayList<Path>();
        try (DirectoryStream<Path> paths = (filter != null) ? Files.newDirectoryStream(sourceDirectory, filter) : Files.newDirectoryStream(sourceDirectory))
        {
            for (Iterator<Path> it = paths.iterator(); it.hasNext();) 
            {
               files.add(it.next());
            }
        }
        catch (IOException e)
        {
            log.error(e.getMessage());
        }
        return files;
    }

    private boolean isMetadataValid(ImportableItem importableItem)
    {
        if (!importableItem.getHeadRevision().metadataFileExists())
        {
            return true;
        }
        
        if (metadataLoader != null)
        {
            MetadataLoader.Metadata result = new MetadataLoader.Metadata();
            metadataLoader.loadMetadata(importableItem.getHeadRevision(), result);
            
            Map<QName, Serializable> metadataProperties = result.getProperties();
            for (QName propertyName : metadataProperties.keySet())
            {
                PropertyDefinition propDef = dictionaryService.getProperty(propertyName);
                if (propDef != null)
                {
                    for (ConstraintDefinition constraintDef : propDef.getConstraints())
                    {
                        Constraint constraint = constraintDef.getConstraint();
                        if (constraint != null)
                        {
                            try
                            {
                                constraint.evaluate(metadataProperties.get(propertyName));
                            }
                            catch (ConstraintException e)
                            {
                                if (log.isWarnEnabled())
                                {
                                    log.warn("Skipping file '" + FileUtils.getFileName(importableItem.getHeadRevision().getContentFile())
                                       +"' with invalid metadata: '" + FileUtils.getFileName(importableItem.getHeadRevision().getMetadataFile()) + "'.", e);
                                }
                                return false;
                            }
                        }
                    }
                }
            }
        }
        
        return true;
    }

    private boolean isVersionFile(Path file)
    {
        Matcher matcher = VERSION_SUFFIX_PATTERN.matcher(file.getFileName().toString());

        return matcher.matches();
    }


    private boolean isMetadataFile(Path file)
    {
        boolean result = false;
        
        if (metadataLoader != null)
        {
            String name = file.getFileName().toString();
            result = name.endsWith(MetadataLoader.METADATA_SUFFIX + metadataLoader.getMetadataFileExtension());
        }
        
        return(result);
    }

    private void addVersionFile(ImportableItem parent, AnalysedDirectory analysedDirectory, Path versionFile)
    {
        Path parentContentFile = getParentOfVersionFile(versionFile);
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


    private void addMetadataFile(ImportableItem parent, AnalysedDirectory analysedDirectory, Path metadataFile)
    {
        Path parentContentfile = getParentOfMetadatafile(metadataFile);

        ImportableItem importableItem = findOrCreateImportableItem(parent, analysedDirectory, parentContentfile);

        importableItem.getHeadRevision().setMetadataFile(metadataFile);
    }


    private boolean addParentFile(ImportableItem parent, AnalysedDirectory analysedDirectory, Path contentFile)
    {
        ImportableItem importableItem = findOrCreateImportableItem(parent, analysedDirectory, contentFile);

        importableItem.getHeadRevision().setContentFile(contentFile);
        
        return(importableItem.getHeadRevision().getContentFileType() == FileType.DIRECTORY);
    }

    private ImportableItem findOrCreateImportableItem(ImportableItem parent, AnalysedDirectory analysedDirectory, Path contentFile)
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


    private ImportableItem findImportableItem(AnalysedDirectory analysedDirectory, Path contentFile)
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


    private int getVersionNumber(Path versionFile)
    {
        int result = -1;

        if (!isVersionFile(versionFile))
        {
            throw new IllegalStateException(FileUtils.getFileName(versionFile) + " is not a version file.");
        }

        Matcher matcher = VERSION_SUFFIX_PATTERN.matcher(versionFile.getFileName().toString());
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


    private Path getParentOfVersionFile(Path versionFile)
    {
        Path result = null;

        if (!isVersionFile(versionFile))
        {
            throw new IllegalStateException(FileUtils.getFileName(versionFile) + " is not a version file.");
        }

        String parentFilename = versionFile.getFileName().toString().replaceFirst(VERSION_SUFFIX_REGEX, "");

        result = versionFile.getParent().resolve(parentFilename);
        
        return(result);
    }


    private Path getParentOfMetadatafile(Path metadataFile)
    {
        Path result = null;

        if (!isMetadataFile(metadataFile))
        {
            throw new IllegalStateException(FileUtils.getFileName(metadataFile) + " is not a metadata file.");
        }

        String name = metadataFile.getFileName().toString();
        String contentName = name.substring(0, name.length() - (MetadataLoader.METADATA_SUFFIX + metadataLoader.getMetadataFileExtension()).length());

        result = metadataFile.getParent().resolve(contentName);

        return(result);
    }

}
