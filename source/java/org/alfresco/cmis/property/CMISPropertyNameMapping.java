
package org.alfresco.cmis.property;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.cmis.dictionary.CMISMapping;

/**
 * Mappings of CMIS properties to names in response
 *
 * TODO: Remove this when spec ambiguity is removed.
 *
 * @author Dmitry Lazurkin
 *
 */
public class CMISPropertyNameMapping
{
    private static Map<String, String> webservice = new HashMap<String, String>();

    static
    {
        webservice.put(CMISMapping.PROP_IS_IMMUTABLE, "isImmutable");
        webservice.put(CMISMapping.PROP_IS_LATEST_VERSION, "isLatestVersion");
        webservice.put(CMISMapping.PROP_IS_MAJOR_VERSION, "isMajorVersion");
        webservice.put(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION, "isLatestMajorVersion");
        webservice.put(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT, "versionSeriesIsCheckedOut");
        webservice.put(CMISMapping.PROP_CREATION_DATE, "creationDate");
        webservice.put(CMISMapping.PROP_LAST_MODIFICATION_DATE, "lastModificationDate");
        webservice.put(CMISMapping.PROP_OBJECT_ID, "objectID");
        webservice.put(CMISMapping.PROP_VERSION_SERIES_ID, "versionSeriesID");
        webservice.put(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID, "versionSeriesCheckedOutID");
        webservice.put(CMISMapping.PROP_CONTENT_STREAM_LENGTH, "contentStreamLength");
        webservice.put(CMISMapping.PROP_NAME, "name");
        webservice.put(CMISMapping.PROP_OBJECT_TYPE_ID, "objectTypeID");
        webservice.put(CMISMapping.PROP_CREATED_BY, "createdBy");
        webservice.put(CMISMapping.PROP_LAST_MODIFIED_BY, "lastModifiedBy");
        webservice.put(CMISMapping.PROP_CONTENT_STREAM_MIME_TYPE, "contentStreamMimeType");
        webservice.put(CMISMapping.PROP_CONTENT_STREAM_FILENAME, "contentStreamFileName");
        webservice.put(CMISMapping.PROP_VERSION_LABEL, "versionLabel");
        webservice.put(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY, "versionSeriesCheckedOutBy");
        webservice.put(CMISMapping.PROP_CHECKIN_COMMENT, "checkinComment");
        webservice.put(CMISMapping.PROP_CONTENT_STREAM_URI, "contentStreamURI");
        webservice.put(CMISMapping.PROP_PARENT, "parent");
    }

    private static Map<String, String> rest = new HashMap<String, String>();

    static
    {
        rest.put(CMISMapping.PROP_IS_IMMUTABLE, "isImmutable");
        rest.put(CMISMapping.PROP_IS_LATEST_VERSION, "isLatestVersion");
        rest.put(CMISMapping.PROP_IS_MAJOR_VERSION, "isMajorVersion");
        rest.put(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION, "isLatestMajorVersion");
        rest.put(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT, "isVersionSeriesCheckedOut");
        rest.put(CMISMapping.PROP_CREATION_DATE, "creationDate");
        rest.put(CMISMapping.PROP_LAST_MODIFICATION_DATE, "lastModificationDate");
        rest.put(CMISMapping.PROP_OBJECT_ID, "objectId");
        rest.put(CMISMapping.PROP_VERSION_SERIES_ID, "versionSeriesID");
        rest.put(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID, "versionSeriesCheckedOutID");
        rest.put(CMISMapping.PROP_CONTENT_STREAM_LENGTH, "contentStreamLength");
        rest.put(CMISMapping.PROP_NAME, "name");
        rest.put(CMISMapping.PROP_OBJECT_TYPE_ID, "objectType");
        rest.put(CMISMapping.PROP_CREATED_BY, "createdBy");
        rest.put(CMISMapping.PROP_LAST_MODIFIED_BY, "lastModifiedBy");
        rest.put(CMISMapping.PROP_CONTENT_STREAM_MIME_TYPE, "contentStreamMimetype");
        rest.put(CMISMapping.PROP_CONTENT_STREAM_FILENAME, "contentStreamName");
        rest.put(CMISMapping.PROP_VERSION_LABEL, "versionLabel");
        rest.put(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY, "versionSeriesCheckedOutBy");
        rest.put(CMISMapping.PROP_CHECKIN_COMMENT, "checkinComment");
        rest.put(CMISMapping.PROP_CONTENT_STREAM_URI, "contentStreamURI");
        rest.put(CMISMapping.PROP_PARENT, "parent");
    }

    
    /**
     * Get property name in web service response
     *
     * @param internalName internal property name
     * @return property name in response
     */
    public static String getWebServiceName(String internalName)
    {
        //return webservice.get(internalName);
        return internalName;
    }

    /**
     * Get property name in web service response
     *
     * @param internalName internal property name
     * @return property name in response
     */
    public static String getRESTName(String internalName)
    {
        String restName = rest.get(internalName);
        return restName == null ? internalName : restName;
    }

}
