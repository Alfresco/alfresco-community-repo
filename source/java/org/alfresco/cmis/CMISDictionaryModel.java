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
package org.alfresco.cmis;

import org.alfresco.cmis.mapping.CMISMapping;
import org.alfresco.model.ContentModel;

/**
 * CMIS <-> Alfresco mappings
 * 
 * @author andyh
 */
public interface CMISDictionaryModel
{
    // CMIS Type Ids
    public static CMISTypeId DOCUMENT_TYPE_ID = new CMISTypeId(CMISScope.DOCUMENT, CMISMapping.DOCUMENT_QNAME, CMISMapping.CMIS_MODEL_NS + ":" + CMISMapping.DOCUMENT_QNAME.getLocalName(), ContentModel.TYPE_CONTENT);
    public static CMISTypeId FOLDER_TYPE_ID = new CMISTypeId(CMISScope.FOLDER, CMISMapping.FOLDER_QNAME, CMISMapping.CMIS_MODEL_NS + ":" + CMISMapping.FOLDER_QNAME.getLocalName(), ContentModel.TYPE_FOLDER);
    public static CMISTypeId RELATIONSHIP_TYPE_ID = new CMISTypeId(CMISScope.RELATIONSHIP, CMISMapping.RELATIONSHIP_QNAME, CMISMapping.CMIS_MODEL_NS + ":" + CMISMapping.RELATIONSHIP_QNAME.getLocalName(), CMISMapping.RELATIONSHIP_QNAME);
    public static CMISTypeId POLICY_TYPE_ID = new CMISTypeId(CMISScope.POLICY, CMISMapping.POLICY_QNAME, CMISMapping.CMIS_MODEL_NS + ":" + CMISMapping.POLICY_QNAME.getLocalName(), CMISMapping.POLICY_QNAME);

    // CMIS properties
    public static String PROP_OBJECT_ID = "cmis:objectId";
    public static String PROP_BASE_TYPE_ID = "cmis:baseTypeId";
    public static String PROP_OBJECT_TYPE_ID = "cmis:objectTypeId";
    public static String PROP_CREATED_BY = "cmis:createdBy";
    public static String PROP_CREATION_DATE = "cmis:creationDate";
    public static String PROP_LAST_MODIFIED_BY = "cmis:lastModifiedBy";
    public static String PROP_LAST_MODIFICATION_DATE = "cmis:lastModificationDate";
    public static String PROP_CHANGE_TOKEN = "cmis:changeToken";
    public static String PROP_NAME = "cmis:name";
    public static String PROP_IS_IMMUTABLE = "cmis:isImmutable";
    public static String PROP_IS_LATEST_VERSION = "cmis:isLatestVersion";
    public static String PROP_IS_MAJOR_VERSION = "cmis:isMajorVersion";
    public static String PROP_IS_LATEST_MAJOR_VERSION = "cmis:isLatestMajorVersion";
    public static String PROP_VERSION_LABEL = "cmis:versionLabel";
    public static String PROP_VERSION_SERIES_ID = "cmis:versionSeriesId";
    public static String PROP_IS_VERSION_SERIES_CHECKED_OUT = "cmis:isVersionSeriesCheckedOut";
    public static String PROP_VERSION_SERIES_CHECKED_OUT_BY = "cmis:versionSeriesCheckedOutBy";
    public static String PROP_VERSION_SERIES_CHECKED_OUT_ID = "cmis:versionSeriesCheckedOutId";
    public static String PROP_CHECKIN_COMMENT = "cmis:checkinComment";
    public static String PROP_CONTENT_STREAM_LENGTH = "cmis:contentStreamLength";
    public static String PROP_CONTENT_STREAM_MIME_TYPE = "cmis:contentStreamMimeType";
    public static String PROP_CONTENT_STREAM_FILENAME = "cmis:contentStreamFileName";
    public static String PROP_CONTENT_STREAM_ID = "cmis:contentStreamId";
    public static String PROP_PARENT_ID = "cmis:parentId";
    public static String PROP_PATH = "cmis:path";
    public static String PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS = "cmis:allowedChildObjectTypeIds";
    public static String PROP_SOURCE_ID = "cmis:sourceId";
    public static String PROP_TARGET_ID = "cmis:targetId";
    public static String PROP_POLICY_TEXT = "cmis:policyText";

}
