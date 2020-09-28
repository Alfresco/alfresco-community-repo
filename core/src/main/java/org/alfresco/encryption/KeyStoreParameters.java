/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.encryption;

import org.alfresco.util.PropertyCheck;

/**
 * Stores Java keystore initialisation parameters.
 * 
 * @since 4.0
 *
 */
public class KeyStoreParameters
{
    private String name;
    private String type;
    private String provider;
    private String keyMetaDataFileLocation;
    private String location;

    public KeyStoreParameters()
    {
    }

    public KeyStoreParameters(String name, String type, String keyStoreProvider,
            String keyMetaDataFileLocation, String location)
    {
        super();
        this.name = name;
        this.type = type;
        this.provider = keyStoreProvider;
        this.keyMetaDataFileLocation = keyMetaDataFileLocation;
        this.location = location;
    }

    public void init()
    {
        if (!PropertyCheck.isValidPropertyString(getLocation()))
        {
            setLocation(null);
        }
        if (!PropertyCheck.isValidPropertyString(getProvider()))
        {
            setProvider(null);
        }
        if (!PropertyCheck.isValidPropertyString(getType()))
        {
            setType(null);
        }
        if (!PropertyCheck.isValidPropertyString(getKeyMetaDataFileLocation()))
        {
            setKeyMetaDataFileLocation(null);
        }        
    }

    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public String getProvider()
    {
        return provider;
    }

    public String getKeyMetaDataFileLocation()
    {
        return keyMetaDataFileLocation;
    }

    public String getLocation()
    {
        return location;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    public void setKeyMetaDataFileLocation(String keyMetaDataFileLocation)
    {
        this.keyMetaDataFileLocation = keyMetaDataFileLocation;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }
}
