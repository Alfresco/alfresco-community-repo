/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
    /**
     * Type id for CMIS documents, from the spec.
     */
    public static String DOCUMENT_OBJECT_TYPE = "document";

    /**
     * Type is for CMIS folders, from the spec.
     */
    public static String FOLDER_OBJECT_TYPE = "folder";

    /**
     * Type Id for CMIS Relationships, from the spec.
     */
    public static String RELATIONSHIP_OBJECT_TYPE = "relationship";

    /**
     * Type Id for CMIS Policies, from the spec.
     */
    public static String POLICY_OBJECT_TYPE = "policy";

    // TODO: spec issue - objectTypeEnum is lower cased - object type ids are repository specific in spec
    public static CMISTypeId DOCUMENT_TYPE_ID = new CMISTypeId(CMISScope.DOCUMENT, DOCUMENT_OBJECT_TYPE.toLowerCase(), ContentModel.TYPE_CONTENT);
    public static CMISTypeId FOLDER_TYPE_ID = new CMISTypeId(CMISScope.FOLDER, FOLDER_OBJECT_TYPE.toLowerCase(), ContentModel.TYPE_FOLDER);
    public static CMISTypeId RELATIONSHIP_TYPE_ID = new CMISTypeId(CMISScope.RELATIONSHIP, RELATIONSHIP_OBJECT_TYPE.toLowerCase(), CMISMapping.RELATIONSHIP_QNAME);
    public static CMISTypeId POLICY_TYPE_ID = new CMISTypeId(CMISScope.POLICY, POLICY_OBJECT_TYPE.toLowerCase(), CMISMapping.POLICY_QNAME);

    // CMIS properties
    public static String PROP_OBJECT_ID = "ObjectId";
    public static String PROP_URI = "Uri";
    public static String PROP_OBJECT_TYPE_ID = "ObjectTypeId";
    public static String PROP_CREATED_BY = "CreatedBy";
    public static String PROP_CREATION_DATE = "CreationDate";
    public static String PROP_LAST_MODIFIED_BY = "LastModifiedBy";
    public static String PROP_LAST_MODIFICATION_DATE = "LastModificationDate";
    public static String PROP_CHANGE_TOKEN = "ChangeToken";
    public static String PROP_NAME = "Name";
    public static String PROP_IS_IMMUTABLE = "IsImmutable";
    public static String PROP_IS_LATEST_VERSION = "IsLatestVersion";
    public static String PROP_IS_MAJOR_VERSION = "IsMajorVersion";
    public static String PROP_IS_LATEST_MAJOR_VERSION = "IsLatestMajorVersion";
    public static String PROP_VERSION_LABEL = "VersionLabel";
    public static String PROP_VERSION_SERIES_ID = "VersionSeriesId";
    public static String PROP_IS_VERSION_SERIES_CHECKED_OUT = "IsVersionSeriesCheckedOut";
    public static String PROP_VERSION_SERIES_CHECKED_OUT_BY = "VersionSeriesCheckedOutBy";
    public static String PROP_VERSION_SERIES_CHECKED_OUT_ID = "VersionSeriesCheckedOutId";
    public static String PROP_CHECKIN_COMMENT = "CheckinComment";
    public static String PROP_CONTENT_STREAM_ALLOWED = "ContentStreamAllowed";
    public static String PROP_CONTENT_STREAM_LENGTH = "ContentStreamLength";
    public static String PROP_CONTENT_STREAM_MIME_TYPE = "ContentStreamMimeType";
    public static String PROP_CONTENT_STREAM_FILENAME = "ContentStreamFilename";
    public static String PROP_CONTENT_STREAM_URI = "ContentStreamUri";
    public static String PROP_PARENT_ID = "ParentId";
    public static String PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS = "AllowedChildObjectTypeIds";
    public static String PROP_SOURCE_ID = "SourceId";
    public static String PROP_TARGET_ID = "TargetId";

}
