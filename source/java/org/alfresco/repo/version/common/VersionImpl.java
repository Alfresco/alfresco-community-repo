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
package org.alfresco.repo.version.common;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConverter;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionServiceException;
import org.alfresco.service.cmr.version.VersionType;


/**
 * Version class implementation.
 * 
 * Used to represent the data about a version stored in a version store.
 * 
 * @author Roy Wetherall
 */

public class VersionImpl implements Version
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3257567304324888881L;
    
    /**
     * Error message(s)
     */
    private static final String ERR_NO_NODE_REF = "A valid node reference must be supplied when creating a verison.";       
    
    /**
     * The properties of the version
     */
    private Map<String, Serializable> versionProperties = null;
    
    /**
     * The node reference that represents the frozen state of the versioned object
     */
    private NodeRef nodeRef = null;        
    
    /**
     * Constructor that initialises the state of the version object.
     * 
     * @param  versionProperties  the version properties
     * @param  nodeRef            the forzen state node reference
     */
    public VersionImpl(
            Map<String, Serializable> versionProperties, 
            NodeRef nodeRef)
    {
        if (nodeRef == null)
        {
            // Exception - a node ref must be specified
            throw new VersionServiceException(VersionImpl.ERR_NO_NODE_REF);
        }
        
        this.versionProperties = versionProperties;
        this.nodeRef = nodeRef;        
    }
    

    @Override
    public String toString()
    {
        return versionProperties.toString();
    }
    
    public Date getFrozenModifiedDate()
    {
        Date modifiedDate = (Date)this.versionProperties.get(Version2Model.PROP_FROZEN_MODIFIED);
        if (modifiedDate == null)
        {
            // Assume deprecated V1 version store
            modifiedDate = (Date)this.versionProperties.get(VersionBaseModel.PROP_CREATED_DATE);
        }
        return modifiedDate;
    }
    
    public String getFrozenModifier()
    {
        String modifier = (String)this.versionProperties.get(Version2Model.PROP_FROZEN_MODIFIER);
        if (modifier == null)
        {
            // Assume deprecated V1 version store
            modifier = (String)this.versionProperties.get(VersionBaseModel.PROP_CREATOR);
        }
        return modifier;
    }
    
    public Date getCreatedDate()
    {
        // note: internal version node created date can be retrieved via standard node service
        return getFrozenModifiedDate();
    }
    
    public String getCreator()
    {
        // note: internal version node creator can be retrieved via standard node service
        return getFrozenModifier();
    }
    
    public String getVersionLabel()
    {
        return (String)this.versionProperties.get(VersionBaseModel.PROP_VERSION_LABEL);
    }    
    
    public VersionType getVersionType()
    {
        return DefaultTypeConverter.INSTANCE.convert(
                VersionType.class,
                this.versionProperties.get(VersionBaseModel.PROP_VERSION_TYPE));
    }
    
    public String getDescription()
    {
        return (String)this.versionProperties.get(Version.PROP_DESCRIPTION);
    }
    
    public Map<String, Serializable> getVersionProperties()
    {
        return this.versionProperties;
    }
    
    public Serializable getVersionProperty(String name)
    {
        Serializable result = null;
        if (this.versionProperties != null)
        {
            result = this.versionProperties.get(name);
        }
        return result;
    }
    
    public NodeRef getVersionedNodeRef()
    {
        NodeRef versionedNodeRef = null;
        
        // Switch VersionStore depending on configured impl
        if (nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID))
        {
            // V2 version store (eg. workspace://version2Store)
            versionedNodeRef = (NodeRef)this.versionProperties.get(Version2Model.PROP_FROZEN_NODE_REF);
        } 
        else if (nodeRef.getStoreRef().getIdentifier().equals(VersionModel.STORE_ID))
        {
            // Deprecated V1 version store (eg. workspace://lightWeightVersionStore)
            String storeProtocol = (String)this.versionProperties.get(VersionModel.PROP_FROZEN_NODE_STORE_PROTOCOL);
            String storeId = (String)this.versionProperties.get(VersionModel.PROP_FROZEN_NODE_STORE_ID);
            String nodeId = (String)this.versionProperties.get(VersionModel.PROP_FROZEN_NODE_ID);
            versionedNodeRef = new NodeRef(new StoreRef(storeProtocol, storeId), nodeId);
        }
        
        return versionedNodeRef;
    }
    
    public NodeRef getFrozenStateNodeRef()
    {
        return this.nodeRef;
    }
    
    /**
     * Static block to register the version type converters
     */
    static
    {
        DefaultTypeConverter.INSTANCE.addConverter(
                String.class, 
                VersionType.class, 
                new TypeConverter.Converter<String, VersionType>()
                {
                    public VersionType convert(String source)
                    {
                        return VersionType.valueOf(source);
                    }
        
                });
        
        DefaultTypeConverter.INSTANCE.addConverter(
                VersionType.class,
                String.class,
                new TypeConverter.Converter<VersionType, String>()
                {
                    public String convert(VersionType source)
                    {
                        return source.toString();
                    }
        
                });
    }
 }
