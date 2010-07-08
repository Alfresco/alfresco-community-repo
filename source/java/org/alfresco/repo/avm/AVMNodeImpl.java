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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.avm.util.RawServices;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.avm.AVMHistoryLinkEntity;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.service.cmr.avm.AVMReadOnlyException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for all repository file system like objects.
 * @author britt
 */
public abstract class AVMNodeImpl implements AVMNode
{
    private static Log logger = LogFactory.getLog(AVMNodeImpl.class);
    
    protected static final boolean DEBUG = logger.isDebugEnabled();
    
    /**
     * The Object ID.
     */
    private long fID;
    
    /**
     * The Version ID.
     */
    private int fVersionID;
    
    /**
     * The basic attributes of this.  Owner, creator, mod time, etc.
     */
    private BasicAttributes fBasicAttributes;
    
    /**
     * The version number (for concurrency control).
     */
    private long fVers;
    
    /**
     * The rootness of this node.
     */
    private boolean fIsRoot;
    
    /**
     * The ACL on this node.
     */
    private Acl fACL;
    
    /**
     * The Store that we're new in.
     */
    private AVMStore fStoreNew;
    
    /**
     * The GUID for this version.
     */
    private String fGUID;
    
    /**
     * The Aspects that belong to this node.
     */
    private Set<QName> fAspects;
    
    private Map<QName, PropertyValue> fProperties;
    
    /**
     * Default constructor.
     */
    protected AVMNodeImpl()
    {
    }
    
    /**
     * Constructor used when creating a new concrete subclass instance.
     * @param store The AVMStore that owns this.
     */
    protected AVMNodeImpl(AVMStore store)
    {
        this();
        
        setVersionID(-1);
        setIsRoot(false);
        
        long time = System.currentTimeMillis();
        String user = 
            RawServices.Instance().getAuthenticationContext().getCurrentUserName();
        if (user == null)
        {
            user = RawServices.Instance().getAuthenticationContext().getSystemUserName();
        }
        setBasicAttributes(new BasicAttributesImpl(user,
                                                   user,
                                                   user,
                                                   time,
                                                   time,
                                                   time));
        setStoreNew(store);
        setGuid(GUID.generate());
    }
    
    /**
     * Set the ancestor of this node.
     * @param ancestor The ancestor to set.
     */
    public void setAncestor(AVMNode ancestor)
    {
        if (ancestor == null)
        {
            return;
        }
        AVMDAOs.Instance().newAVMNodeLinksDAO.createHistoryLink(ancestor.getId(), this.getId());
    }
    
    /**
     * Change the ancestor of this node.
     * @param ancestor The new ancestor to give it.
     */
    public void changeAncestor(AVMNode ancestor)
    {
        AVMHistoryLinkEntity hlEntity = AVMDAOs.Instance().newAVMNodeLinksDAO.getHistoryLinkByDescendent(this.getId());
        if (hlEntity != null)
        {
            AVMDAOs.Instance().newAVMNodeLinksDAO.deleteHistoryLink(hlEntity.getAncestorNodeId(), hlEntity.getDescendentNodeId());
        }
        setAncestor(ancestor);
    }
    
    /**
     * Get the ancestor of this node.
     * @return The ancestor of this node.
     */
    public AVMNode getAncestor()
    {
        return AVMDAOs.Instance().fAVMNodeDAO.getAncestor(this);
    }
    
    /**
     * Set the node that was merged into this.
     * @param mergedFrom The node that was merged into this.
     */
    public void setMergedFrom(AVMNode mergedFrom)
    {
        if (mergedFrom == null)
        {
            return;
        }
        AVMDAOs.Instance().newAVMNodeLinksDAO.createMergeLink(mergedFrom.getId(), this.getId());
    }
    
    /**
     * Get the node that was merged into this.
     * @return The node that was merged into this.
     */
    public AVMNode getMergedFrom()
    {
        return AVMDAOs.Instance().fAVMNodeDAO.getMergedFrom(this);
    }
    
    /**
     * Equality based on object ids.
     * @param obj The thing to compare against.
     * @return Equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof AVMNode))
        {
            return false;
        }
        return getId() == ((AVMNode)obj).getId();
    }

    /**
     * Get a reasonable hash value.
     * @return The hash code.
     */
    @Override
    public int hashCode()
    {
        return (int)getId();
    }
    
    /**
     * Set the object id.
     * @param id The id to set.
     */
    public void setId(long id)
    {
        fID = id;
    }
    
    /**
     * Get the id of this node.
     * @return The object id.
     */
    public long getId()
    {
        return fID;
    }
 
    /**
     * Set the versionID for this node.  
     * @param versionID The id to set.
     */
    public void setVersionID(int versionID)
    {
        fVersionID = versionID;
    }
    
    /**
     * Get the version id of this node.
     * @return The version id.
     */
    public int getVersionID()
    {
        return fVersionID;
    }
    
    /**
     * Set the basic attributes.
     * @param attrs
     */
    public void setBasicAttributes(BasicAttributes attrs)
    {
        fBasicAttributes = attrs;
    }
    
    /**
     * Get the basic attributes.
     * @return The basic attributes.
     */
    public BasicAttributes getBasicAttributes()
    {
        return fBasicAttributes;
    }
    
    /**
     * Get whether this is a new node.
     * @return Whether this is new.
     */
    public boolean getIsNew()
    {
        return getStoreNew() != null;
    }
 
    /**
     * Set the version (for concurrency control).
     * @param The version for optimistic locks.
     */
    public void setVers(long vers)
    {
        fVers = vers;
    }
    
    /**
     * Get the version (for concurrency control).
     * @return vers  The version for optimistic locks.
     */
    public long getVers()
    {
        return fVers;
    }

    /**
     * Get whether this is a root node.
     * @return Whether this is a root node.
     */
    public boolean getIsRoot()
    {
        return fIsRoot;
    }

    /**
     * @param isRoot
     */
    public void setIsRoot(boolean isRoot)
    {
        fIsRoot = isRoot;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNode#updateModTime()
     */
    public void updateModTime()
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        String user = 
            RawServices.Instance().getAuthenticationContext().getCurrentUserName();
        if (user == null)
        {
            user = RawServices.Instance().getAuthenticationContext().getSystemUserName();
        }
        getBasicAttributes().setModDate(System.currentTimeMillis());
        getBasicAttributes().setLastModifier(user);
    }
    
    /**
     * Copy all properties from another node.
     * @param other The other node.
     */
    protected void copyProperties(AVMNode other)
    {
        Map<QName, PropertyValue> props = new HashMap<QName, PropertyValue>();
        for (Map.Entry<QName, PropertyValue> entry : other.getProperties().entrySet())
        {
            props.put(entry.getKey(), entry.getValue());
        }
        
        setProperties(props);
    }
    
    /**
     * Copy all aspects from another node.
     * @param other The other node.
     */
    protected void copyAspects(AVMNode other)
    {
        Set<QName> aspects = new HashSet<QName>(other.getAspects());
        setAspects(aspects);
    }
    
    protected void copyCreationAndOwnerBasicAttributes(AVMNode other)
    {
        getBasicAttributes().setCreateDate(other.getBasicAttributes().getCreateDate());
        getBasicAttributes().setCreator(other.getBasicAttributes().getCreator());
        getBasicAttributes().setOwner(other.getBasicAttributes().getOwner());
    }
    
    public void copyACLs(AVMNode other, ACLCopyMode mode)
    {
        Acl otherAcl = other.getAcl();
        Long otherAclId = (otherAcl == null ? null : otherAcl.getId());
        copyACLs(otherAclId, otherAclId, mode);
    }
    
    public void copyACLs(Acl otherAcl, Acl parentAcl, ACLCopyMode mode)
    {
        Long otherAclId = (otherAcl == null ? null : otherAcl.getId());
        Long parentAclId = (parentAcl == null ? null : parentAcl.getId());
        
        copyACLs(otherAclId, parentAclId, mode);
    }
    
    protected void copyACLs(AVMNode other, Long parentAcl, ACLCopyMode mode)
    {
        Acl otherAcl = other.getAcl();
        copyACLs((otherAcl == null ? null : otherAcl.getId()), parentAcl, mode);
    }
    
    protected void copyACLs(Long otherAcl, Long parentAcl, ACLCopyMode mode)
    {
        if (otherAcl != null)
        {
            Acl aclCopy = AVMDAOs.Instance().fAclDAO.getAclCopy(otherAcl, parentAcl, mode);
            setAcl(aclCopy);
        }
        else
        {
            setAcl(null);
        }
    }
    
    /**
     * Copy out metadata from another node.
     * @param other The other node.
     */
    public void copyMetaDataFrom(AVMNode other, Long parentAcl)
    {
        copyAspects(other);
        copyACLs(other, parentAcl, ACLCopyMode.COPY);
        copyProperties(other);
        copyCreationAndOwnerBasicAttributes(other);
    }
    
    /**
     * Set a property on a node. Overwrite it if it exists.
     * @param name The name of the property.
     * @param value The value to set.
     */
    public void setProperty(QName qname, PropertyValue value)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        
        getProperties().put(qname, value);
        
        AVMDAOs.Instance().fAVMNodeDAO.createOrUpdateProperty(this.getId(), qname, value);
    }
    
    public void addProperties(Map<QName, PropertyValue> properties)
    {
        for (Map.Entry<QName, PropertyValue> entry : properties.entrySet())
        {
            setProperty(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Set a collection of properties on this node.
     * @param properties The Map of QNames to PropertyValues.
     */
    public void setProperties(Map<QName, PropertyValue> properties)
    {
        fProperties = properties;
        
        for (Map.Entry<QName, PropertyValue> entry : properties.entrySet())
        {
            setProperty(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Get a property by name.
     * @param name The name of the property.
     * @return The PropertyValue or null if non-existent.
     */
    public PropertyValue getProperty(QName qname)
    {
        return getProperties().get(qname);
    }
    
    /**
     * {@inheritDoc}
     */
    public Map<QName, PropertyValue> getProperties()
    {
        if (fProperties == null)
        {
            fProperties = AVMDAOs.Instance().fAVMNodeDAO.getProperties(getId());
        }
        return fProperties;
    }

    /**
     * Delete a property from this node.
     * @param name The name of the property.
     */
    public void deleteProperty(QName qname)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        getProperties().remove(qname);
        
        AVMDAOs.Instance().fAVMNodeDAO.deleteProperty(getId(), qname);
    }
    
    /**
     * Delete all properties from this node.
     */
    public void deleteProperties()
    {
        getProperties().clear();
        
        AVMDAOs.Instance().fAVMNodeDAO.deleteProperties(getId());
    }
    
    /**
     * Set the ACL on this node.
     * @param acl The ACL to set.
     */
    public void setAcl(Acl acl)
    {
        fACL = acl;
    }
    
    /**
     * Get the ACL on this node.
     * @return The ACL on this node.
     */
    public Acl getAcl()
    {
        return fACL;
    }
    
    /**
     * Set the store we are new in.
     * @param store The store we are new in.
     */
    public void setStoreNew(AVMStore store)
    {
        fStoreNew = store;
    }
    
    /**
     * Get the possibly null store we are new in.
     * @return The store we are new in.
     */
    public AVMStore getStoreNew()
    {
        return fStoreNew;
    }
    
    protected void checkReadOnly()
    {
        if (getStoreNew() == null)
        {
            throw new AVMReadOnlyException("Write Operation on R/O Node.");
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNode#getGuid()
     */
    public String getGuid() 
    {
        return fGUID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNode#setGuid(java.lang.String)
     */
    public void setGuid(String guid) 
    {
        fGUID = guid;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNode#getAspects()
     */
    public Set<QName> getAspects()
    {
        if (fAspects == null)
        {
            fAspects = AVMDAOs.Instance().fAVMNodeDAO.getAspects(getId());
        }
        return fAspects;
    }
    
    /**
     * Set the aspects on this node.
     * @param aspects
     */
    public void setAspects(Set<QName> aspects)
    {
        fAspects = aspects;
        
        if ((aspects != null) && (aspects.size() > 0))
        {
            for (QName aspectQName : aspects)
            {
                AVMDAOs.Instance().fAVMNodeDAO.createAspect(this.getId(), aspectQName);
            }
        }
    }
    
    public void addAspect(QName aspectQName)
    {
        fAspects = null;
        AVMDAOs.Instance().fAVMNodeDAO.createAspect(this.getId(), aspectQName);
    }
    
    public void removeAspect(QName aspectQName)
    {
        fAspects = null;
        AVMDAOs.Instance().fAVMNodeDAO.deleteAspect(this.getId(), aspectQName);
    }
    
    // debug
    public String toString()
    {
        return toString(null);
    }
}
