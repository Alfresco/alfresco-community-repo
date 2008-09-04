
package org.alfresco.repo.cmis.ws;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.cmis.dictionary.CMISMapping;

/**
 * Mappings of CMIS properties to names in response
 *
 * @author Dmitry Lazurkin
 *
 */
public class CMISPropNamesMapping
{
    private static Map<String, String> cmisPropertiesNamesMapping = new HashMap<String, String>();

    static
    {
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_IS_IMMUTABLE, "isImmutable");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_IS_LATEST_VERSION, "isLatestVersion");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_IS_MAJOR_VERSION, "isMajorVersion");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION, "isLatestMajorVersion");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT, "versionSeriesIsCheckedOut");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_CREATION_DATE, "creationDate");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_LAST_MODIFICATION_DATE, "lastModificationDate");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_OBJECT_ID, "objectID");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_VERSION_SERIES_ID, "versionSeriesID");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID, "versionSeriesCheckedOutID");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_CONTENT_STREAM_LENGTH, "contentStreamLength");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_NAME, "name");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_OBJECT_TYPE_ID, "objectTypeID");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_CREATED_BY, "createdBy");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_LAST_MODIFIED_BY, "lastModifiedBy");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_CONTENT_STREAM_MIME_TYPE, "contentStreamMimeType");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_CONTENT_STREAM_FILENAME, "contentStreamFileName");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_VERSION_LABEL, "versionLabel");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY, "versionSeriesCheckedOutBy");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_CHECKIN_COMMENT, "checkinComment");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_CONTENT_STREAM_URI, "contentStreamURI");
        cmisPropertiesNamesMapping.put(CMISMapping.PROP_PARENT, "parent");
    }

    /**
     * Get property name in response
     *
     * @param internalName internal property name
     * @return property name in response
     */
    public static String getResponsePropertyName(String internalName)
    {
        //return cmisPropertiesNamesMapping.get(internalName);
        return internalName;
    }

}
