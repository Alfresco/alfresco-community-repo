/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.search.impl.elasticsearch.shared;

public class ElasticsearchConstants
{
    // Output properties - Property names used for indexing
    public static final String ALIVE = "ALIVE";
    public static final String READER = "READER";
    public static final String DENIED = "DENIED";
    public static final String OWNER = "OWNER";
    public static final String METADATA_INDEXING_LAST_UPDATE = "METADATA_INDEXING_LAST_UPDATE";
    public static final String CONTENT_INDEXING_LAST_UPDATE = "CONTENT_INDEXING_LAST_UPDATE";
    public static final String PATH_INDEXING_LAST_UPDATE = "PATH_INDEXING_LAST_UPDATE";
    public static final String REINDEXING_START_TIME = "reindexingStartTime";
    public static final String ASPECT = "ASPECT";
    public static final String PATH = "PATH";
    public static final String PATH_KEYWORD = "PATH.keyword";
    public static final String UNPREFIXED_PATH = "UNPREFIXED_PATH";
    public static final String PROPERTIES = "PROPERTIES";
    public static final String TAG = "TAG";
    public static final String CM_TAGGABLE = "cm:taggable";
    public static final String TYPE = "TYPE";
    public static final String PRIMARY_HIERARCHY = "primaryHierarchy";
    public static final String PRIMARY_PARENT = "PRIMARYPARENT";
    public static final String PARENT = "PARENT";
    public static final String ANCESTOR = "ANCESTOR";
    public static final String STANDARD_ANCESTOR = "STANDARD_ANCESTOR";
    public static final String CATEGORY_ANCESTOR = "CATEGORY_ANCESTOR";
    public static final String NAME = "cm:name";
    public static final String USER_CREATOR = "cm:creator";
    public static final String USER_MODIFIER = "cm:modifier";
    public static final String CREATION_DATE_FIELD = "cm:created";
    public static final String MODIFICATION_DATE_FIELD = "cm:modified";
    public static final String ACCESS_DATE_FIELD = "cm:accessed";
    public static final String CONTENT_ATTRIBUTE_NAME = "cm:content";
    public static final String CONTENT_MIME_TYPE = "cm:content.mimetype";
    public static final String CONTENT_SIZE = "cm:content.size";
    public static final String CONTENT_ENCODING = "cm:content.encoding";
    /** The type of category and tag nodes. */
    public static final String CM_CATEGORY = "cm:category";
    /** Name of the categories property in events. */
    public static final String CM_CATEGORIES = "cm:categories";
    public static final String CM_CONTENT_TR_EX = "cm:content.tr_ex";
    public static final String CM_CONTENT_TR_STATUS = "cm:content.tr_status";

    // Input properties - Property names used for taking information from the event
    public static final String OWNER_PROPERTY_NAME = "cm:owner";
    public static final String TYPE_STORE_ROOT = "sys:store_root";
    public static final String TYPE_DELETED = "sys:deleted";

    // Other
    public static final String CM_CATEGORY_ROOT = "/cm:categoryRoot/cm:generalclassifiable/";

    // As part of APPS-2283 Abstraction work
    public enum ScriptType
    {
        EXCEPTION_SCRIPT, TEXT_SCRIPT
    }
}
