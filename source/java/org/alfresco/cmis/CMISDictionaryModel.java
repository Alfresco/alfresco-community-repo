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
    // CMIS Type Ids
    public static CMISTypeId DOCUMENT_TYPE_ID = new CMISTypeId(CMISScope.DOCUMENT, CMISMapping.DOCUMENT_QNAME, CMISMapping.CMIS_MODEL_NS + ":" + CMISMapping.DOCUMENT_QNAME.getLocalName(), ContentModel.TYPE_CONTENT);
    public static CMISTypeId FOLDER_TYPE_ID = new CMISTypeId(CMISScope.FOLDER, CMISMapping.FOLDER_QNAME, CMISMapping.CMIS_MODEL_NS + ":" + CMISMapping.FOLDER_QNAME.getLocalName(), ContentModel.TYPE_FOLDER);
    public static CMISTypeId RELATIONSHIP_TYPE_ID = new CMISTypeId(CMISScope.RELATIONSHIP, CMISMapping.RELATIONSHIP_QNAME, CMISMapping.CMIS_MODEL_NS + ":" + CMISMapping.RELATIONSHIP_QNAME.getLocalName(), CMISMapping.RELATIONSHIP_QNAME);
    public static CMISTypeId POLICY_TYPE_ID = new CMISTypeId(CMISScope.POLICY, CMISMapping.POLICY_QNAME, CMISMapping.CMIS_MODEL_NS + ":" + CMISMapping.POLICY_QNAME.getLocalName(), CMISMapping.POLICY_QNAME);

    // CMIS properties
    public static String PROP_OBJECT_ID = "cmis:ObjectId";
    public static String PROP_BASE_TYPE_ID = "cmis:BaseTypeId";
    public static String PROP_OBJECT_TYPE_ID = "cmis:ObjectTypeId";
    public static String PROP_CREATED_BY = "cmis:CreatedBy";
    public static String PROP_CREATION_DATE = "cmis:CreationDate";
    public static String PROP_LAST_MODIFIED_BY = "cmis:LastModifiedBy";
    public static String PROP_LAST_MODIFICATION_DATE = "cmis:LastModificationDate";
    public static String PROP_CHANGE_TOKEN = "cmis:ChangeToken";
    public static String PROP_NAME = "cmis:Name";
    public static String PROP_IS_IMMUTABLE = "cmis:IsImmutable";
    public static String PROP_IS_LATEST_VERSION = "cmis:IsLatestVersion";
    public static String PROP_IS_MAJOR_VERSION = "cmis:IsMajorVersion";
    public static String PROP_IS_LATEST_MAJOR_VERSION = "cmis:IsLatestMajorVersion";
    public static String PROP_VERSION_LABEL = "cmis:VersionLabel";
    public static String PROP_VERSION_SERIES_ID = "cmis:VersionSeriesId";
    public static String PROP_IS_VERSION_SERIES_CHECKED_OUT = "cmis:IsVersionSeriesCheckedOut";
    public static String PROP_VERSION_SERIES_CHECKED_OUT_BY = "cmis:VersionSeriesCheckedOutBy";
    public static String PROP_VERSION_SERIES_CHECKED_OUT_ID = "cmis:VersionSeriesCheckedOutId";
    public static String PROP_CHECKIN_COMMENT = "cmis:CheckinComment";
    public static String PROP_CONTENT_STREAM_LENGTH = "cmis:ContentStreamLength";
    public static String PROP_CONTENT_STREAM_MIME_TYPE = "cmis:ContentStreamMimeType";
    public static String PROP_CONTENT_STREAM_FILENAME = "cmis:ContentStreamFileName";
    public static String PROP_CONTENT_STREAM_ID = "cmis:ContentStreamId";
    public static String PROP_PARENT_ID = "cmis:ParentId";
    public static String PROP_PATH_NAME = "cmis:PathName";
    public static String PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS = "cmis:AllowedChildObjectTypeIds";
    public static String PROP_SOURCE_ID = "cmis:SourceId";
    public static String PROP_TARGET_ID = "cmis:TargetId";
    public static String PROP_POLICY_TEXT = "cmis:PolicyText";

}
