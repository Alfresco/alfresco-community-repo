/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.apache.abdera.ext.cmis;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ExtensibleElementWrapper;


/**
 * CMIS Object for the Abdera ATOM library.
 * 
 * Encapsulates access and modification of CMIS extension values to CMIS
 * Object.
 * 
 * NOTE: Potentially, this extension can be contributed to Abdera upon
 *       publication of CMIS.  This is why it is organised under a
 *       non-Alfresco Java package.  It follows the conventions of all
 *       other Abdera extensions.
 * 
 * @author davidc
 */
public class CMISObject extends ExtensibleElementWrapper
{
    /**
     * @param internal
     */
    public CMISObject(Element internal)
    {
        super(internal);
    }

    /**
     * @param factory
     */
    public CMISObject(Factory factory)
    {
        super(factory, CMISConstants.OBJECT);
    }

    /**
     * Gets all Properties for this CMIS Object
     * 
     * @return  properties
     */
    public CMISProperties getProperties()
    {
        Element child = getFirstChild(CMISConstants.PROPERTIES);
        if (child == null)
        {
            child = addExtension(CMISConstants.PROPERTIES); 
        }
        return (CMISProperties)child;
    }

    /**
     * Gets name
     * 
     * @return  name property
     */
    public CMISProperty getName()
    {
        return getProperties().find(CMISConstants.PROP_NAME);
    }

    /**
     * Gets id
     * 
     * @return  id property 
     */
    public CMISProperty getObjectId()
    {
        return getProperties().find(CMISConstants.PROP_OBJECT_ID);
    }

    /**
     * Gets base type
     * 
     * @return  base type property
     */
    public CMISProperty getBaseType()
    {
        return getProperties().find(CMISConstants.PROP_BASETYPE);
    }

    /**
     * Gets object type
     * 
     * @return  object type property
     */
    public CMISProperty getObjectTypeId()
    {
        return getProperties().find(CMISConstants.PROP_OBJECT_TYPE_ID);
    }

    /**
     * Is immutable?
     * 
     * @return  isImmutable property
     */
    public CMISProperty isImmutable()
    {
        return getProperties().find(CMISConstants.PROP_IS_IMMUTABLE);
    }

    /**
     * Gets Latest Version
     * 
     * @return  latest version property
     */
    public CMISProperty isLatestVersion()
    {
        return getProperties().find(CMISConstants.PROP_IS_LATEST_VERSION);
    }

    /**
     * Is Major Version?
     * 
     * @return  is major version property
     */
    public CMISProperty isMajorVersion()
    {
        return getProperties().find(CMISConstants.PROP_IS_MAJOR_VERSION);
    }

    /**
     * Is Latest Major Version?
     * 
     * @return  is latest major version property
     */
    public CMISProperty isLatestMajorVersion()
    {
        return getProperties().find(CMISConstants.PROP_IS_LATEST_MAJOR_VERSION);
    }

    /**
     * Version label
     * 
     * @return  version label property
     */
    public CMISProperty getVersionLabel()
    {
        return getProperties().find(CMISConstants.PROP_VERSION_LABEL);
    }

    /**
     * Version series id
     * 
     * @return  version series id property
     */
    public CMISProperty getVersionSeriesId()
    {
        return getProperties().find(CMISConstants.PROP_VERSION_SERIES_ID);
    }

    /**
     * Version Series Checked Out
     * 
     * @return  version series checked out property
     */
    public CMISProperty isVersionSeriesCheckedOut()
    {
        return getProperties().find(CMISConstants.PROP_IS_VERSION_SERIES_CHECKED_OUT);
    }
    
    /**
     * Version Series Checked Out By
     * 
     * @return  version series checked out by property
     */
    public CMISProperty getVersionSeriesCheckedOutBy()
    {
        return getProperties().find(CMISConstants.PROP_VERSION_SERIES_CHECKED_OUT_BY);
    }

    /**
     * Version Series Checked Out Id
     * 
     * @return  version series checked out id property
     */
    public CMISProperty getVersionSeriesCheckedOutId()
    {
        return getProperties().find(CMISConstants.PROP_VERSION_SERIES_CHECKED_OUT_ID);
    }

    /**
     * Checkin Comment
     * 
     * @return  checkin comment property
     */
    public CMISProperty getCheckinComment()
    {
        return getProperties().find(CMISConstants.PROP_CHECKIN_COMMENT);
    }
}
