/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.version.common;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

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


    /**
     * Helper method to get the created date from the version property data.
     * 
     * @return the date the version was created
     */
    public Date getCreatedDate()
    {
        return (Date)this.versionProperties.get(VersionModel.PROP_CREATED_DATE);
    }
    
    public String getCreator()
    {
        return (String)this.versionProperties.get(VersionModel.PROP_CREATOR);
    }

    /**
     * Helper method to get the version label from the version property data.
     * 
     * @return the version label
     */
    public String getVersionLabel()
    {
        return (String)this.versionProperties.get(VersionModel.PROP_VERSION_LABEL);
    }    
    
    /**
     * Helper method to get the version type.
     * 
     * @return  the value of the version type as an enum value
     */
    public VersionType getVersionType()
    {
        return (VersionType)this.versionProperties.get(VersionModel.PROP_VERSION_TYPE);
    }
    
    /**
     * Helper method to get the version description.
     * 
     * @return the version description
     */
    public String getDescription()
    {
        return (String)this.versionProperties.get(PROP_DESCRIPTION);
    }
    
    /**
     * @see org.alfresco.service.cmr.version.Version#getVersionProperties()
     */
    public Map<String, Serializable> getVersionProperties()
    {
        return this.versionProperties;
    }

    /**
     * @see org.alfresco.service.cmr.version.Version#getVersionProperty(java.lang.String)
     */
    public Serializable getVersionProperty(String name)
    {
        Serializable result = null;
        if (this.versionProperties != null)
        {
            result = this.versionProperties.get(name);
        }
        return result;
    }
    
    /**
     * @see org.alfresco.service.cmr.version.Version#getVersionedNodeRef()
     */
    public NodeRef getVersionedNodeRef()
    {
        String storeProtocol = (String)this.versionProperties.get(VersionModel.PROP_FROZEN_NODE_STORE_PROTOCOL);
        String storeId = (String)this.versionProperties.get(VersionModel.PROP_FROZEN_NODE_STORE_ID);
        String nodeId = (String)this.versionProperties.get(VersionModel.PROP_FROZEN_NODE_ID);
        return new NodeRef(new StoreRef(storeProtocol, storeId), nodeId);
    }

    /**
     * @see org.alfresco.service.cmr.version.Version#getFrozenStateNodeRef()
     */
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
