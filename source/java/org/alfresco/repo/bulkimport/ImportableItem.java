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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.repo.bulkimport.impl.FileUtils;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a DTO that represents an "importable item" - a series of files
 * that represent a single node (content OR space) in the repository.
 *
 * @since 4.0
 *
 */
public final class ImportableItem
{
    public enum FileType
    {
        FILE,
        DIRECTORY,
        OTHER
    };

    protected static final Log logger = LogFactory.getLog(ImportableItem.class);
    
    private ContentAndMetadata headRevision = new ContentAndMetadata();
    private SortedSet<VersionedContentAndMetadata> versionEntries = null;
    private NodeRef nodeRef;
    private ImportableItem parent;
    private long numChildren = 0;
    
    public boolean isValid()
    {
        return(headRevision.contentFileExists() || headRevision.metadataFileExists() || hasVersionEntries());
    }
    
    public ContentAndMetadata getHeadRevision()
    {
        return(headRevision);
    }
    
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public void clearParent()
    {
        numChildren--;
        if (numChildren <= 0)
        {
            numChildren = 0;
            parent = null;
        }
    }

    public void setParent(ImportableItem parent)
    {
        if (parent == null)
        {
            throw new IllegalArgumentException("Parent cannot be null");
        }
        this.parent = parent;
    }
    
    public ImportableItem getParent()
    {
        return parent;
    }

    /**
     * @return True if this ImportableItem has version entries.
     */
    public boolean hasVersionEntries()
    {
        return (versionEntries != null && versionEntries.size() > 0);
    }
    
    public Set<VersionedContentAndMetadata> getVersionEntries()
    {
        return(Collections.unmodifiableSet(versionEntries));
    }
    
    public void addVersionEntry(final VersionedContentAndMetadata versionEntry)
    {
        if (versionEntry != null)
        {
            if (versionEntries == null)
            {
                versionEntries = new TreeSet<VersionedContentAndMetadata>();
            }
                
            versionEntries.add(versionEntry);
        }
    }
    
    @Override
    public String toString()
    {
        return(new ToStringBuilder(this)
               .append("HeadRevision", headRevision)
               .append("Versions", versionEntries)
               .toString());
    }
    
    public class ContentAndMetadata
    {
        private Path     contentFile           = null;
        private boolean  contentFileExists     = false;
        private boolean  contentFileIsReadable = false;
        private FileType contentFileType       = null;
        private long     contentFileSize       = -1;
        private Date     contentFileCreated    = null;
        private Date     contentFileModified   = null;
        private Path     metadataFile          = null;
        private long     metadataFileSize      = -1;

        
        public final Path getContentFile()
        {
            return contentFile;
        }
        
        public final void setContentFile(final Path contentFile)
        {
            this.contentFile = contentFile;
            
            if (contentFile != null)
            {
                // stat the file, to find out a few key details
                contentFileExists = Files.exists(contentFile, LinkOption.NOFOLLOW_LINKS);
                
                if (contentFileExists)
                {
                    try
                    {
                        BasicFileAttributes attrs = Files.readAttributes(contentFile, BasicFileAttributes.class);

                        contentFileIsReadable = Files.isReadable(contentFile);
                        contentFileSize       = attrs.size();
                        contentFileModified   = new Date(attrs.lastModifiedTime().toMillis());
                        contentFileCreated    = new Date(attrs.creationTime().toMillis());

                        if (Files.isRegularFile(contentFile, LinkOption.NOFOLLOW_LINKS))
                        {
                            contentFileType = FileType.FILE;
                        }
                        else if (Files.isDirectory(contentFile, LinkOption.NOFOLLOW_LINKS))
                        {
                            contentFileType = FileType.DIRECTORY;
                        }
                        else
                        {
                            contentFileType = FileType.OTHER;
                        }
                    }
                    catch (IOException e)
                    {
                        logger.error("Attributes for file '" + FileUtils.getFileName(contentFile) + "' could not be read.", e);
                    }
                }
            }
        }
        
        public final boolean contentFileExists()
        {
            return(contentFileExists);
        }
        
        public final boolean isContentFileReadable()
        {
            return(contentFileIsReadable);
        }
        
        public final FileType getContentFileType()
        {
            if (!contentFileExists())
            {
                throw new IllegalStateException("Cannot determine content file type if content file doesn't exist.");
            }
            
            return(contentFileType);
        }
        
        public final long getContentFileSize()
        {
            if (!contentFileExists())
            {
                throw new IllegalStateException("Cannot determine content file size if content file doesn't exist.");
            }
            
            return(contentFileSize);
        }
        
        public final Date getContentFileCreatedDate()
        {
            if (!contentFileExists())
            {
                throw new IllegalStateException("Cannot determine content file creation date if content file doesn't exist.");
            }
            
            return(contentFileCreated);
        }
        
        public final Date getContentFileModifiedDate()
        {
            if (!contentFileExists())
            {
                throw new IllegalStateException("Cannot determine content file modification date if content file doesn't exist.");
            }
            
            return(contentFileModified);
        }
        
        public final boolean metadataFileExists()
        {
            return(metadataFile != null);
        }
        
        public final Path getMetadataFile()
        {
            return metadataFile;
        }
        
        public final void setMetadataFile(final Path metadataFile)
        {
            if (metadataFile != null && Files.exists(metadataFile))
            {
                this.metadataFile = metadataFile;
                try
                {
                    this.metadataFileSize = Files.size(metadataFile);
                }
                catch (IOException e)
                {
                    if (logger.isWarnEnabled()) 
                    {
                        logger.warn("Size for the metadata file '" + FileUtils.getFileName(metadataFile) + "' could not be retrieved.", e);
                    }
                }
            }
        }
        
        public final long getMetadataFileSize()
        {
            if (!metadataFileExists())
            {
                throw new IllegalStateException("Cannot determine metadata file size if metadata file doesn't exist.");
            }
            
            return(metadataFileSize);
        }
        
        public final int weight()
        {
            return((contentFile   == null || !contentFileExists ? 0 : 1) +
                   (metadataFile == null ? 0 : 1));
        }

        @Override
        public String toString()
        {
            return(new ToStringBuilder(this)
                   .append("contentFile", (contentFileExists ? contentFile : null))
                   .append("metadatafile", metadataFile)
                   .toString());
        }
    }
    
    /**
     * 
     * @since 4.0
     *
     */
    public class VersionedContentAndMetadata extends ContentAndMetadata implements Comparable<VersionedContentAndMetadata>
    {
        private int version;

        public VersionedContentAndMetadata(final int version)
        {
            this.version = version;
        }
        
        public final int getVersion()
        {
            return(version);
        }
        
        @Override
        public String toString()
        {
            return(new ToStringBuilder(this)
                   .append("version", version)
                   .appendSuper("")
                   .toString());
        }

        public int compareTo(final VersionedContentAndMetadata other)
        {
            return(this.version < other.version ? -1 :
                   this.version == other.version ? 0 : 1);
        }

        @Override
        public boolean equals(final Object other)
        {
            if (this == other)
            {
                return(true);
            }

            if (!(other instanceof VersionedContentAndMetadata))
            {
                return(false);
            }

            VersionedContentAndMetadata otherVCAM = (VersionedContentAndMetadata)other;

            return(this.version == otherVCAM.version);
        }

        @Override
        public int hashCode()
        {
            return(version);
        }
    }
}
