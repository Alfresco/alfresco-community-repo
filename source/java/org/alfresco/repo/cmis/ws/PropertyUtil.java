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

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.util.Pair;

/**
 * Class for accessing CMIS properties
 *
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
public class PropertyUtil
{
    private static Map<String, Pair<String, Boolean>> cmisToRepoPropertiesNamesMapping = new HashMap<String, Pair<String, Boolean>>();
    private static Map<String, Pair<String, Boolean>> repoToCmisPropertiesNamesMapping = new HashMap<String, Pair<String, Boolean>>();

    static
    {
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_IS_IMMUTABLE, new Pair<String, Boolean>("IsImmutable", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_IS_LATEST_VERSION, new Pair<String, Boolean>("IsLatestVersion", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_IS_MAJOR_VERSION, new Pair<String, Boolean>("IsMajorVersion", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION, new Pair<String, Boolean>("IsLatestMajorVersion", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT, new Pair<String, Boolean>("IsVersionSeriesCheckedOut", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_CREATION_DATE, new Pair<String, Boolean>("CreationDate", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE, new Pair<String, Boolean>("LastModificationDate", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_OBJECT_ID, new Pair<String, Boolean>("ObjectId", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_VERSION_SERIES_ID, new Pair<String, Boolean>("VersionSeriesId", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID, new Pair<String, Boolean>("VersionSeriesCheckedOutId", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH, new Pair<String, Boolean>("ContentStreamLength", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_NAME, new Pair<String, Boolean>("Name", false));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_OBJECT_TYPE_ID, new Pair<String, Boolean>("ObjectTypeId", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_CREATED_BY, new Pair<String, Boolean>("CreatedBy", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_LAST_MODIFIED_BY, new Pair<String, Boolean>("LastModifiedBy", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE, new Pair<String, Boolean>("ContentStreamMimeType", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME, new Pair<String, Boolean>("ContentStreamFilename", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_VERSION_LABEL, new Pair<String, Boolean>("VersionLabel", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_CHECKIN_COMMENT, new Pair<String, Boolean>("checkinComment", false));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_CONTENT_STREAM_URI, new Pair<String, Boolean>("contentStreamUri", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY, new Pair<String, Boolean>("VersionSeriesCheckedOutBy", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_PARENT_ID, new Pair<String, Boolean>("ParentId", true));
        cmisToRepoPropertiesNamesMapping.put(CMISDictionaryModel.PROP_CONTENT_STREAM_ALLOWED, new Pair<String, Boolean>("ContentStreamAllowed", true));

        for (Map.Entry<String, Pair<String, Boolean>> entry : cmisToRepoPropertiesNamesMapping.entrySet())
        {
            repoToCmisPropertiesNamesMapping.put(entry.getValue().getFirst(), new Pair<String, Boolean>(entry.getKey(), entry.getValue().getSecond()));
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
        return internalName;
        // TODO: remove the above mapping
        //return cmisToRepoPropertiesNamesMapping.get(internalName).getFirst();
    }

    /**
     * Get property name in repository
     *
     * @param cmisName internal property name
     * @return property name in repository
     */
    public static String getRepositoryPropertyName(String cmisName)
    {
        return repoToCmisPropertiesNamesMapping.get(cmisName).getFirst();
    }

    public static boolean isReadOnlyCmisProperty(String internalPropertyName)
    {
        return repoToCmisPropertiesNamesMapping.get(internalPropertyName).getSecond();
    }

    public static boolean isReadOnlyRepositoryProperty(String cmisPropertyName)
    {
        return repoToCmisPropertiesNamesMapping.get(cmisPropertyName).getSecond();
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
            value = ((CmisPropertyBoolean) cmisProperty).getValue() != null && ((CmisPropertyBoolean) cmisProperty).getValue().size() > 0 ? ((CmisPropertyBoolean) cmisProperty)
                    .getValue().get(0) : null;
        }
        else if (cmisProperty instanceof CmisPropertyDateTime)
        {
            value = ((CmisPropertyDateTime) cmisProperty).getValue() != null && ((CmisPropertyDateTime) cmisProperty).getValue().size() > 0 ? ((CmisPropertyDateTime) cmisProperty)
                    .getValue().get(0).toXMLFormat() : null;
        }
        else if (cmisProperty instanceof CmisPropertyDecimal)
        {
            value = ((CmisPropertyDecimal) cmisProperty).getValue() != null && ((CmisPropertyDecimal) cmisProperty).getValue().size() > 0 ? ((CmisPropertyDecimal) cmisProperty)
                    .getValue().get(0) : null;
        }
        else if (cmisProperty instanceof CmisPropertyHtml)
        {
        }
        else if (cmisProperty instanceof CmisPropertyId)
        {
            value = ((CmisPropertyId) cmisProperty).getValue() != null && ((CmisPropertyId) cmisProperty).getValue().size() > 0 ? ((CmisPropertyId) cmisProperty).getValue().get(0)
                    : null;
        }
        else if (cmisProperty instanceof CmisPropertyInteger)
        {
            value = ((CmisPropertyInteger) cmisProperty).getValue() != null && ((CmisPropertyInteger) cmisProperty).getValue().size() > 0 ? ((CmisPropertyInteger) cmisProperty)
                    .getValue().get(0) : null;
        }
        else if (cmisProperty instanceof CmisPropertyString)
        {
            value = ((CmisPropertyString) cmisProperty).getValue() != null && ((CmisPropertyString) cmisProperty).getValue().size() > 0 ? ((CmisPropertyString) cmisProperty)
                    .getValue().get(0) : null;
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
