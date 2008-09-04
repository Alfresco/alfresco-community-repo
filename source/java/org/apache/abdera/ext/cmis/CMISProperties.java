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
import org.apache.abdera.model.ElementWrapper;


/**
 * CMIS Object Element Wrapper for the Abdera ATOM library.
 * 
 * Encapsulates access and modification of CMIS extension values to ATOM.
 * 
 * NOTE: Potentially, this extension can be contributed to Abdera upon
 *       publication of CMIS.  This is why it is organised under a
 *       non-Alfresco Java package.  It follows the conventions of all
 *       other Abdera extensions.
 * 
 * @author davidc
 */
public class CMISProperties extends ElementWrapper
{
    public CMISProperties(Element internal)
    {
        super(internal);
    }

    public CMISProperties(Factory factory)
    {
        super(factory, CMISConstants.PROPERTIES);
    }

    public String getObjectId()
    {
        return findPropertyID(CMISConstants.PROP_OBJECT_ID);
    }

    public String getBaseType()
    {
        return findPropertyString(CMISConstants.PROP_BASETYPE);
    }
    
    public boolean isImmutable()
    {
        return findPropertyBoolean(CMISConstants.PROP_IS_IMMUTABLE);
    }

    public boolean isLatestVersion()
    {
        return findPropertyBoolean(CMISConstants.PROP_IS_LATEST_VERSION);
    }

    public boolean isMajorVersion()
    {
        return findPropertyBoolean(CMISConstants.PROP_IS_MAJOR_VERSION);
    }

    public boolean isLatestMajorVersion()
    {
        return findPropertyBoolean(CMISConstants.PROP_IS_LATEST_MAJOR_VERSION);
    }

    public String getVersionLabel()
    {
        return findPropertyString(CMISConstants.PROP_VERSION_LABEL);
    }

    public String getVersionSeriesId()
    {
        return findPropertyID(CMISConstants.PROP_VERSION_SERIES_ID);
    }

    public boolean isVersionSeriesCheckedOut()
    {
        return findPropertyBoolean(CMISConstants.PROP_VERSION_SERIES_IS_CHECKED_OUT);
    }
    
    public String getVersionSeriesCheckedOutBy()
    {
        return findPropertyString(CMISConstants.PROP_VERSION_SERIES_CHECKED_OUT_BY);
    }

    public String getVersionSeriesCheckedOutId()
    {
        return findPropertyID(CMISConstants.PROP_VERSION_SERIES_CHECKED_OUT_ID);
    }

    public String getCheckinComment()
    {
        return findPropertyString(CMISConstants.PROP_CHECKIN_COMMENT);
    }

    
    private String findPropertyString(String name)
    {
        Element child = getFirstChild(CMISConstants.PROPERTY_STRING);
        while(child != null)
        {
            if (name.equals(child.getAttributeValue(CMISConstants.PROPERTY_NAME)))
            {
                return child.getText();
            }
            child = child.getNextSibling(CMISConstants.PROPERTY_STRING);
        }
        return null;
    }

    private String findPropertyID(String name)
    {
        Element child = getFirstChild(CMISConstants.PROPERTY_ID);
        while(child != null)
        {
            if (name.equals(child.getAttributeValue(CMISConstants.PROPERTY_NAME)))
            {
                return child.getText();
            }
            child = child.getNextSibling(CMISConstants.PROPERTY_ID);
        }
        return null;
    }

    private boolean findPropertyBoolean(String name)
    {
        Element child = getFirstChild(CMISConstants.PROPERTY_BOOLEAN);
        while(child != null)
        {
            if (name.equals(child.getAttributeValue(CMISConstants.PROPERTY_NAME)))
            {
                return Boolean.valueOf(child.getText());
            }
            child = child.getNextSibling(CMISConstants.PROPERTY_BOOLEAN);
        }
        return false;
    }


}
