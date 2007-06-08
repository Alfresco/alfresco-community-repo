/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.service.cmr.avm;

import java.io.Serializable;

import org.alfresco.repo.avm.AVMNodeType;

/**
 * This class describes an AVM node object.  
 * It serves a similar purpose to the data structure
 * returned by the stat() system call in UNIX.
 *
 * @author britt
 */
public class AVMNodeDescriptor implements Serializable
{
    private static final long serialVersionUID = -7959606980486852184L;

    /**
     * The path that this was looked up with.
     */
    private String fPath;
    
    /**
     * The base name of the path.
     */
    private String fName;
    
    /**
     * The type of this node.  AVMNodeType constants.
     */
    private int fType;
    
    /**
     * The Owner.
     */
    private String fOwner;
    
    /**
     * The Creator.
     */
    private String fCreator;
    
    /**
     * The last modifier.
     */
    private String fLastModifier;
    
    /**
     * The Create date.
     */
    private long fCreateDate;
    
    /**
     * The Modification date.
     */
    private long fModDate;
    
    /**
     * The Access date.
     */
    private long fAccessDate;

    /**
     * The object id.
     */
    private long fID;
    
    /**
     * The version number.
     */
    private int fVersionID;
    
    /**
     * The indirection if this is a layer.
     */
    private String fIndirection;
    
    /**
     * The indirection version if this is a layer.
     */
    private int fIndirectionVersion;
    
    /**
     * Is this a primary indirection node.
     */
    private boolean fIsPrimary;

    /**
     * The layer id or -1 if this is not a layered node.
     */
    private long fLayerID;
    
    /**
     * The length, if this is a file or -1 otherwise.
     */
    private long fLength;
    
    /**
     * The opacity for layered directories.
     */
    private boolean fOpacity;

    /**
     * The type of node that this is a deleted node for.
     * Only for DeletedNode.
     */
    private int fDeletedType;
    
    /**
     * The GUID for the node.
     */
    private String fGuid;
    
    /**
     * Make one up.
     * @param path The looked up path.
     * @param type The type of the node.
     * @param creator The creator of the node.
     * @param owner The owner of the node.
     * @param lastModifier The last modifier of the node.
     * @param createDate The creation date.
     * @param modDate The modification date.
     * @param accessDate The access date.
     * @param id The object id.
     * @param versionID The version id.
     * @param guid The GUID.
     * @param indirection The indirection.
     * @param indirectionVersion The indirection version.
     * @param isPrimary Whether this is a primary indirection.
     * @param layerID The layer id.
     * @param length The file length.
     * @param deletedType The type of node that was deleted.
     */
    public AVMNodeDescriptor(String path,
                             String name,
                             int type,
                             String creator,
                             String owner,
                             String lastModifier,
                             long createDate,
                             long modDate,
                             long accessDate,
                             long id,
                             String guid,
                             int versionID,
                             String indirection,
                             int indirectionVersion,
                             boolean isPrimary,
                             long layerID,
                             boolean opacity,
                             long length, 
                             int deletedType)
    {
        fPath = path;
        fName = name;
        fType = type;
        fCreator = creator;
        fOwner = owner;
        fLastModifier = lastModifier;
        fCreateDate = createDate;
        fModDate = modDate;
        fAccessDate = accessDate;
        fID = id;
        fGuid = guid;
        fVersionID = versionID;
        fIndirection = indirection;
        fIndirectionVersion = indirectionVersion;
        fIsPrimary = isPrimary;
        fLayerID = layerID;
        fLength = length;
        fOpacity = opacity;
        fDeletedType = deletedType;
    }
    
    /**
     * Get the last access date in java milliseconds.
     * @return The last access date.
     */
    public long getAccessDate()
    {
        return fAccessDate;
    }

    /**
     * Get the creation date in java milliseconds.
     * @return The creation date.
     */
    public long getCreateDate()
    {
        return fCreateDate;
    }

    /**
     * Get the user who created this.
     * @return The creator.
     */
    public String getCreator()
    {
        return fCreator;
    }

    /**
     * Get the indirection path if this is layered or null.
     * @return The indirection path or null.
     */
    public String getIndirection()
    {
        return fIndirection;
    }

    /**
     * Get the indirection version.
     * @return The indirection version.
     */
    public int getIndirectionVersion()
    {
        return fIndirectionVersion;
    }
    
    /**
     * Is this a primary indirection node.  Will always
     * be false for non-layered nodes.
     * @return Whether this is a primary indirection node.
     */
    public boolean isPrimary()
    {
        return fIsPrimary;
    }

    /**
     * Determines whether this node corresponds to
     * either a plain or layered file.   
     * <p>
     * NOTE:  A deleted file node is <em>not</em> considered a file 
     * (i.e.: isFile() returns false when isDeleted() returns true).  
     * Therefore, use isDeletedFile() to determine if a deleted node 
     * was a file, not isFile().
     *
     * @return true if AVMNodeDescriptor is a plain or layered file,
     *         otherwise false.
     */
    public boolean isFile()
    {
        return ( fType == AVMNodeType.PLAIN_FILE || 
                 fType == AVMNodeType.LAYERED_FILE
               );
    }

    /**
     * Determines whether this node corresponds to
     * a plain (non-layered) file.
     *
     * @return true if AVMNodeDescriptor is a plain file, otherwise false. 
     */
    public boolean isPlainFile()
    {
        return (fType == AVMNodeType.PLAIN_FILE);
    }

    /**
     * Determines whether this node corresponds to
     * a layered file.
     *
     * @return true if AVMNodeDescriptor is a layered file, 
     *         otherwise false. 
     */
    public boolean isLayeredFile()
    {
        return (fType == AVMNodeType.LAYERED_FILE);
    }

    /**
     * Determines whether this node corresponds to
     * either a plain or layered directory. 
     * <p>
     * NOTE:  A deleted directory node is <em>not</em> considered a directory 
     * (i.e.: isDirectory() returns false when isDeleted() returns true).  
     * Therefore, use isDeletedDirectory() to determine if a deleted node 
     * was a directory, not isDirectory().
     *
     * @return true if AVMNodeDescriptor is a plain or layered directory,
     *         otherwise false.
     */
    public boolean isDirectory()
    {
        return ( fType == AVMNodeType.PLAIN_DIRECTORY   || 
                 fType == AVMNodeType.LAYERED_DIRECTORY
               );
    }

    /**
     * Determines whether this node corresponds to
     * a plain (non-layered) directory.
     *
     * @return true if AVMNodeDescriptor is a plain directory, otherwise false. 
     */
    public boolean isPlainDirectory()
    {
        return (fType == AVMNodeType.PLAIN_DIRECTORY );
    }

    /**
     * Determines whether this node corresponds to
     * a layered directory.
     *
     * @return true if AVMNodeDescriptor is a layered directory, 
     *         otherwise false. 
     */
    public boolean isLayeredDirectory()
    {
        return (fType == AVMNodeType.LAYERED_DIRECTORY );
    }

    /**
     * Is this a deleted node.
     * @return Whether this node is a deleted node.
     */
    public boolean isDeleted()
    {
        return fType == AVMNodeType.DELETED_NODE;
    }
    
    /**
     * Get the user who last modified this node.
     * @return Who last modified this node.
     */
    public String getLastModifier()
    {
        return fLastModifier;
    }

    /**
     * Get the layer id of this node.
     * @return The layer id if there is one or -1.
     */
    public long getLayerID()
    {
        return fLayerID;
    }

    /**
     * Get the modification date of this node.
     * @return The modification date.
     */
    public long getModDate()
    {
        return fModDate;
    }

    /**
     * Get the owner of this node.
     * @return The owner of this node.
     */
    public String getOwner()
    {
        return fOwner;
    }

    /**
     * Get the path that this node was looked up by.
     * @return The path by which this was looked up.
     */
    public String getPath()
    {
        return fPath;
    }

    /**
     * Get the type of this node. AVMNodeType constants.
     * @return The type node.
     */
    public int getType()
    {
        return fType;
    }

    /**
     * Get the version id of this node.
     * @return The version id of this node.
     */
    public int getVersionID()
    {
        return fVersionID;
    }
    
    /**
     * Get the object id.
     * @return The object id.
     */
    public long getId()
    {
        return fID;
    }
    
    /**
     * Get the file length if applicable.
     * @return The file length.
     */
    public long getLength()
    {
        return fLength;
    }
    
    /**
     * Get the name of the node.
     */
    public String getName()
    {
        return fName;
    }

    /**
     * @return the opacity
     */
    public boolean getOpacity()
    {
        return fOpacity;
    }

    /**
     * Get a debuggable string representation of this.
     * @return A string representation of this.
     */
    @Override
    public String toString()
    {
        switch (fType)
        {
            case AVMNodeType.PLAIN_FILE :
                return "[PF:" + fID + "]";
            case AVMNodeType.PLAIN_DIRECTORY :
                return "[PD:" + fID + "]";
            case AVMNodeType.LAYERED_FILE :
                return "[LF:" + fID + ":" + fIndirection + "]";
            case AVMNodeType.LAYERED_DIRECTORY :
                return "[LD:" + fID + ":" + fIndirection + "]";
            case AVMNodeType.DELETED_NODE :
                return "[DN:" + fID + "]";
            default :
                throw new AVMException("Internal Error.");
        }
    }

    /**
     * Equals override.
     * @param obj
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AVMNodeDescriptor))
        {
            return false;
        }
        return fID == ((AVMNodeDescriptor)obj).fID;
    }

    /**
     * Hashcode override.
     * @return The objid as hashcode.
     */
    @Override
    public int hashCode()
    {
        return (int)fID;
    }
    
    /**
     * Get the type of node that a deleted node is standing in for.
     */
    public int getDeletedType()
    {
        return fDeletedType;
    }
    
    /**
     * Is this a deleted directory?
     */
    public boolean isDeletedDirectory()
    {
        return fType == AVMNodeType.DELETED_NODE &&
               (fDeletedType == AVMNodeType.LAYERED_DIRECTORY ||
                fDeletedType == AVMNodeType.PLAIN_DIRECTORY);
    }
    
    /**
     * Is this a deleted file?
     */
    public boolean isDeletedFile()
    {
        return fType == AVMNodeType.DELETED_NODE &&
               (fDeletedType == AVMNodeType.LAYERED_FILE ||
                fDeletedType == AVMNodeType.PLAIN_FILE);
    }
    
    /**
     * Get the GUID for the node.
     */
    public String getGuid()
    {
        return fGuid;
    }
}
