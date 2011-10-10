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
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.builder.ToStringBuilder;

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

    private ContentAndMetadata headRevision = new ContentAndMetadata();
    private SortedSet<VersionedContentAndMetadata> versionEntries = null;
    private NodeRef nodeRef;
    private ImportableItem parent;
    private long numChildren = 0;
    
    public boolean isValid()
    {
        return(headRevision.contentFileExists() || headRevision.metadataFileExists());
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
    	if(numChildren <= 0)
    	{
    		numChildren = 0;
    		parent = null;
    	}
    }

    public void setParent(ImportableItem parent)
	{
    	if(parent == null)
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
        return(versionEntries != null && versionEntries.size() > 0);
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
        private File     contentFile           = null;
        private boolean  contentFileExists     = false;
        private boolean  contentFileIsReadable = false;
        private FileType contentFileType       = null;
        private long     contentFileSize       = -1;
        private Date     contentFileCreated    = null;
        private Date     contentFileModified   = null;
        private File     metadataFile          = null;
        private long     metadataFileSize      = -1;

        
        public final File getContentFile()
        {
            return(contentFile);
        }
        
        public final void setContentFile(final File contentFile)
        {
            this.contentFile = contentFile;
            
            if (contentFile != null)
            {
                // stat the file, to find out a few key details
                contentFileExists = contentFile.exists();
                
                if (contentFileExists)
                {
                    contentFileIsReadable = contentFile.canRead();
                    contentFileSize       = contentFile.length();
                    contentFileModified   = new Date(contentFile.lastModified());
                    contentFileCreated    = contentFileModified;    //TODO: determine proper file creation time (awaiting JDK 1.7 NIO2 library)
                    
                    if (contentFile.isFile())
                    {
                        contentFileType = FileType.FILE;
                    }
                    else if (contentFile.isDirectory())
                    {
                        contentFileType = FileType.DIRECTORY;
                    }
                    else
                    {
                        contentFileType = FileType.OTHER;
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
        
        public final File getMetadataFile()
        {
            return(metadataFile);
        }
        
        public final void setMetadataFile(final File metadataFile)
        {
            if (metadataFile != null && metadataFile.exists())
            {
                this.metadataFile     = metadataFile;
                this.metadataFileSize = metadataFile.length();
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
