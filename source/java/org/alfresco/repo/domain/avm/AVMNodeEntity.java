/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.domain.avm;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.util.EqualsHelper;

/**
 * Entity bean for <b>avm_nodes</b> table.
 * <p>
 * 
 * @author janv
 * @since 3.2
 */
public class AVMNodeEntity implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long version;
    private Integer type;
    private String classType;
    private Long storeNewId;
    private Long aclId;
    private Long layerId;
    private String guid;
    private boolean isRoot;
    private Integer deletedType;
    private String indirection;
    private Integer indirectionVersion;
    private boolean primaryIndirection;
    private boolean opacity;
    private String contentUrl;
    private String mimetype;
    private String encoding;
    private Long length;
    
    // basic attributes
    private String owner;
    private String creator;
    private Long createdDate;
    private String modifier;
    private Long modifiedDate;
    private Long accessDate;
    
    private Long vers; // for concurrency control
    
    
    public Long getId()
    {
        return id;
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public String getClassType()
    {
        return classType;
    }
    
    public void setClassType(String classType)
    {
        this.classType = classType;
        
        this.type = null;
        if (classType != null)
        {
            if (classType.equals("plainfile"))
            {
                type = AVMNodeType.PLAIN_FILE;
            }
            else if (classType.equals("plaindirectory"))
            {
                 type = AVMNodeType.PLAIN_DIRECTORY;
            }
            else if (classType.equals("layeredfile"))
            {
                type = AVMNodeType.LAYERED_FILE;
            }
            else if (classType.equals("layereddirectory"))
            {
                 type = AVMNodeType.LAYERED_DIRECTORY;
            }
            else if (classType.equals("deletednode"))
            {
                 type = AVMNodeType.DELETED_NODE;
            }
            else
            {
                // belts-and-braces
                throw new AlfrescoRuntimeException("Unexpected node class_type: "+classType);
            }
        }
    }
    
    public int getType()
    {
        return type;
    }
    
    public void setType(Integer type)
    {
        this.type = type;
        
        this.classType = null;
        if (type != null)
        {
            switch (type)
            {
                case AVMNodeType.PLAIN_FILE :
                    classType = "plainfile";
                    break;
                case AVMNodeType.PLAIN_DIRECTORY :
                    classType = "plaindirectory";
                    break;
                case AVMNodeType.LAYERED_FILE :
                    classType = "layeredfile";
                    break;
                case AVMNodeType.LAYERED_DIRECTORY :
                    classType = "layereddirectory";
                    break;
                case AVMNodeType.DELETED_NODE :
                    classType = "deletednode";
                    break;
                default:
                    // belts-and-braces
                    throw new AlfrescoRuntimeException("Unexpected node type: "+type);
            }
        }
    }
    
    public Long getVersion()
    {
        return version;
    }
    
    public void setVersion(Long version)
    {
        this.version = version;
    }
    
    public Long getStoreNewId()
    {
        return storeNewId;
    }
    
    public void setStoreNewId(Long storeNewId)
    {
        this.storeNewId = storeNewId;
    }
    
    public Long getAclId()
    {
        return aclId;
    }
    
    public void setAclId(Long aclId)
    {
        this.aclId = aclId;
    }
    
    public Long getLayerId()
    {
        return layerId;
    }
    
    public void setLayerId(Long layerId)
    {
        this.layerId = layerId;
    }
    
    public String getGuid()
    {
        return guid;
    }
    
    public void setGuid(String guid)
    {
        this.guid = guid;
    }
    
    public boolean isRoot()
    {
        return isRoot;
    }
    
    public void setRoot(Boolean isRoot)
    {
        this.isRoot = (isRoot == null ? false : isRoot);
    }
    
    public Integer getDeletedType()
    {
        return deletedType;
    }
    
    public void setDeletedType(Integer deletedType)
    {
        this.deletedType = deletedType;
    }
    
    public String getIndirection()
    {
        return indirection;
    }
    
    public void setIndirection(String indirection)
    {
        this.indirection = indirection;
    }
    
    public Integer getIndirectionVersion()
    {
        return indirectionVersion;
    }
    
    public void setIndirectionVersion(Integer indirectionVersion)
    {
        this.indirectionVersion = indirectionVersion;
    }
    
    public boolean isPrimaryIndirection()
    {
        return primaryIndirection;
    }
    
    public void setPrimaryIndirection(Boolean primaryIndirection)
    {
        this.primaryIndirection = (primaryIndirection == null ? false : primaryIndirection);
    }
    
    public boolean getOpacity()
    {
        return opacity;
    }
    
    public void setOpacity(Boolean opacity)
    {
        this.opacity = (opacity == null ? false : opacity);
    }
    
    public String getContentUrl()
    {
        return contentUrl;
    }
    
    public void setContentUrl(String contentUrl)
    {
        this.contentUrl = contentUrl;
    }
    
    public String getMimetype()
    {
        return mimetype;
    }
    
    public void setMimetype(String mimetype)
    {
        this.mimetype = mimetype;
    }
    
    public String getEncoding()
    {
        return encoding;
    }
    
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }
    
    public Long getLength()
    {
        return length;
    }
    
    public void setLength(Long length)
    {
        this.length = length;
    }
    
    public String getOwner()
    {
        return owner;
    }
    
    public void setOwner(String owner)
    {
        this.owner = owner;
    }
    
    public String getCreator()
    {
        return creator;
    }
    
    public void setCreator(String creator)
    {
        this.creator = creator;
    }
    
    public Long getCreatedDate()
    {
        return createdDate;
    }
    
    public void setCreatedDate(Long createdDate)
    {
        this.createdDate = createdDate;
    }
    
    public String getModifier()
    {
        return modifier;
    }
    
    public void setModifier(String modifier)
    {
        this.modifier = modifier;
    }
    
    public Long getModifiedDate()
    {
        return modifiedDate;
    }
    
    public void setModifiedDate(Long modifiedDate)
    {
        this.modifiedDate = modifiedDate;
    }
    
    public Long getAccessDate()
    {
        return accessDate;
    }
    
    public void setAccessDate(Long accessDate)
    {
        this.accessDate = accessDate;
    }
    
    public Long getVers()
    {
        return vers;
    }
    
    public void setVers(Long vers)
    {
        this.vers = vers;
    }
    
    public void incrementVers()
    {
        if (this.vers >= Long.MAX_VALUE)
        {
            this.vers = 0L;
        }
        else
        {
            this.vers++;
        }
    }
    
    @Override
    public int hashCode()
    {
        return (id == null ? 0 : id.hashCode());
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj instanceof AVMNodeEntity)
        {
            AVMNodeEntity that = (AVMNodeEntity) obj;
            return EqualsHelper.nullSafeEquals(this.id, that.id);
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("AVMNodeEntity")
          .append("[ ID=").append(id)
          .append(", nextVersion=").append(version)
          .append("]");
        return sb.toString();
    }
}
