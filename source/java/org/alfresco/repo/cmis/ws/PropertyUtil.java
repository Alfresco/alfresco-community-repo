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
package org.alfresco.repo.cmis.ws;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.cmis.dictionary.CMISMapping;

/**
 * Class for accessing CMIS properties
 *
 * @author Dmitry Lazurkin
 *
 */
public class PropertyUtil
{
    private static Map<String, String> cmisToRepoPropertiesNamesMapping = new HashMap<String, String>();
    private static Map<String, String> repoToCmisPropertiesNamesMapping = new HashMap<String, String>();

    static
    {
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_IS_IMMUTABLE, "IsImmutable");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_IS_LATEST_VERSION, "IsLatestVersion");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_IS_MAJOR_VERSION, "IsMajorVersion");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION, "IsLatestMajorVersion");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_IS_VERSION_SERIES_CHECKED_OUT, "IsVersionSeriesCheckedOut");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_CREATION_DATE, "CreationDate");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_LAST_MODIFICATION_DATE, "LastModificationDate");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_OBJECT_ID, "ObjectId");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_VERSION_SERIES_ID, "VersionSeriesId");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID, "VersionSeriesCheckedOutId");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_CONTENT_STREAM_LENGTH, "ContentStreamLength");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_NAME, "Name");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_OBJECT_TYPE_ID, "ObjectTypeId");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_CREATED_BY, "CreatedBy");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_LAST_MODIFIED_BY, "LastModifiedBy");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_CONTENT_STREAM_MIME_TYPE, "ContentStreamMimeType");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_CONTENT_STREAM_FILENAME, "ContentStreamFilename");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_VERSION_LABEL, "VersionLabel");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_CHECKIN_COMMENT, "checkinComment");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_CONTENT_STREAM_URI, "contentStreamURI");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY, "VersionSeriesCheckedOutBy");
        cmisToRepoPropertiesNamesMapping.put(CMISMapping.PROP_PARENT_ID, "ParentId");

        for (Map.Entry<String, String> entry : cmisToRepoPropertiesNamesMapping.entrySet())
        {
            repoToCmisPropertiesNamesMapping.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Get property name in response
     *
     * @param internalName internal property name
     * @return property name in response
     */
    public static String getCMISPropertyName(String internalName)
    {
        return cmisToRepoPropertiesNamesMapping.get(internalName);
    }

    /**
     * Get property name in repository
     *
     * @param cmisName internal property name
     * @return property name in repository
     */
    public static String getRepositoryPropertyName(String cmisName)
    {
        return repoToCmisPropertiesNamesMapping.get(cmisName);
    }

    public static Serializable getProperty(CmisPropertiesType cmisProperties, String property)
    {
        String cmisPropertyName = getCMISPropertyName(property);

        for (CmisProperty cmisProperty : cmisProperties.getProperty())
        {
            if (cmisProperty.getName().equals(cmisPropertyName))
            {
                return getValue(cmisProperty);
            }
        }

        return null;
    }

    public static Serializable getValue(CmisProperty cmisProperty)
    {
        Serializable value = null;

        if (cmisProperty instanceof CmisPropertyBoolean)
        {
            value = ((CmisPropertyBoolean) cmisProperty).isValue();
        }
        else if (cmisProperty instanceof CmisPropertyDateTime)
        {
//            value = ((CmisPropertyDateTime) cmisProperty).getValue().;
        }
        else if (cmisProperty instanceof CmisPropertyDecimal)
        {
            value = ((CmisPropertyDecimal) cmisProperty).getValue().doubleValue();
        }
        else if (cmisProperty instanceof CmisPropertyHtml)
        {
        }
        else if (cmisProperty instanceof CmisPropertyId)
        {
            value = ((CmisPropertyId) cmisProperty).getValue();
        }
        else if (cmisProperty instanceof CmisPropertyInteger)
        {
            value = ((CmisPropertyInteger) cmisProperty).getValue();
        }
        else if (cmisProperty instanceof CmisPropertyString)
        {
            value = ((CmisPropertyString) cmisProperty).getValue();
        }
        else if (cmisProperty instanceof CmisPropertyUri)
        {
        }
        else if (cmisProperty instanceof CmisPropertyXml)
        {
        }

        return value;
    }

}
